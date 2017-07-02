package splitstree5.io.fasta;

import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.characters.AmbiguityCodes;
import splitstree5.core.datablocks.characters.CharactersType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Daria on 01.07.2017.
 */
public class CharactersFastaIO {

    // todo : check parameter for all sequences or only for the first one?
    //public enum ID {ncbi, gb, emb, dbj, pir, prf, sp, pdb, pat, bbs, gnl, ref, lcl, unknown}

    private static final String[] possibleIDs =
            {"gb", "emb", "ena", //???
                    "dbj", "pir", "prf", "sp", "pdb", "pat", "bbs", "gnl", "ref", "lcl"};

    public final String[] extensions = {"fasta", "fas", "fa", "seq", "fsa"};

    private static char gap = '-';
    private static char missing = '?';
    private static char matchChar='.';

    public static void parse(String inputFile, TaxaBlock taxa, CharactersBlock characters) throws IOException {

        ArrayList<String> taxonNamesFound = new ArrayList<>();
        ArrayList<String> matrix = new ArrayList<>();
        int ntax = 0;
        int nchar = 0;

        BufferedReader in = new BufferedReader(new FileReader(inputFile));
        String line;
        int sequenceLength = 0;
        String sequence = "";
        boolean startedNewSequence = false;

        while((line=in.readLine())!=null) {
            if(line.startsWith(";"))
                continue;
            if(line.startsWith(">")) {
                startedNewSequence = true;
                addTaxaName(line, taxonNamesFound);
                ntax++;
            }else{
                if(startedNewSequence){
                    if(!sequence.equals("")) matrix.add(sequence);
                    if(nchar!=0 && nchar!=sequenceLength){
                        throw new IOException("Sequences must be the same length");
                    }
                    nchar = sequenceLength;
                    sequenceLength = 0;
                    sequence = "";
                    startedNewSequence = false;
                }
                sequenceLength+=line.length();
                sequence+=line;
            }
        }
        matrix.add(sequence);
        if(nchar!=sequenceLength) throw new IOException("Sequences must be the same length");
        in.close();

        System.err.println("ntax: "+ntax+" nchar: "+nchar);
        for(String s : matrix){
            System.err.println(s);
        }
        for(String s : taxonNamesFound){
            System.err.println(s);
        }

        taxa.addTaxaByNames(taxonNamesFound);
        characters.clear();
        characters.setDimension(ntax, nchar);
        readMatrix(matrix, characters);
    }

    private static void readMatrix(ArrayList<String> matrix, CharactersBlock characters) throws IOException {
        // todo check if valid and set parameters here

        String foundSymbols = "";
        for(int i = 1; i<=characters.getNtax(); i++){
            for(int j = 1; j<=characters.getNchar(); j++){
                char symbol = Character.toLowerCase(matrix.get(i-1).charAt(j-1));
                if(foundSymbols.indexOf(symbol) == -1) foundSymbols+=symbol;
                characters.set(i, j, matrix.get(i-1).charAt(j-1));
            }
        }

        foundSymbols = foundSymbols.replace(getStringGap(), "");
        foundSymbols = foundSymbols.replace(getStringMissing(), "");
        foundSymbols = foundSymbols.replace(getStringMatchChar(), "");
        // sort found symbols
        char[] chars = foundSymbols.toCharArray();
        Arrays.sort(chars);
        String sortedSymbols = new String(chars);

        switch (sortedSymbols) {
            case "01": characters.setDataType(CharactersType.standard);
                break;
            case "acgt": characters.setDataType(CharactersType.DNA);
                break;
            case "acgu": characters.setDataType(CharactersType.RNA);
                break;
            case "acdefghiklmnpqrstvwyz": characters.setDataType(CharactersType.protein);
                break;
            default:
                if(isAmbiguous(sortedSymbols)){
                    characters.setHasAmbiguousStates(true);
                    if(sortedSymbols.contains("t")) characters.setDataType(CharactersType.DNA);
                    if(sortedSymbols.contains("u")) characters.setDataType(CharactersType.RNA);
                    if(sortedSymbols.contains("t") && sortedSymbols.contains("u"))
                        throw new IOException("Nucleotide sequence contains Thymine and Uracil at tha same time");
                }else{
                    characters.setDataType(CharactersType.unknown);
                    System.err.println("Warning : can not recognize characters type!");
                    // todo set new gap/missing/match chars and try again
                }
                break;
        }
        System.err.println("symbols: "+foundSymbols);
    }

    private static boolean isAmbiguous(String foundSymbols){
        final String IUPAC = "acgtu"+ AmbiguityCodes.CODES;
        for(char c : foundSymbols.toCharArray()){
            if(!IUPAC.contains(c+"")) return false;
        }
        return true;
    }

    private static void addTaxaName(String infoLine, ArrayList<String> taxonNamesFound) throws IOException {

        if(infoLine.contains("[organism=")){
            int index1 = infoLine.indexOf("[organism=")+10;
            int index2 = infoLine.indexOf(']');

            // todo test whole line or only taxon name???
            if(taxonNamesFound.contains(infoLine.substring(index1,index2))){
                throw new IOException("Double taxon name: "+infoLine.substring(index1,index2));
            }
            taxonNamesFound.add(infoLine.substring(index1,index2));
            return;
        }

        // check SeqID
        // todo ? boolean doubleID; ?

        infoLine = infoLine.toLowerCase();
        String foundID = "";
        for(String id : possibleIDs){
            if(infoLine.contains(">"+id+"|") || infoLine.contains("|"+id+"|")){
                foundID = id;
                break;
            }
        }

        if(!foundID.equals("")){
            String afterID = infoLine.substring(infoLine.indexOf(foundID)+foundID.length());
            int index1 = afterID.indexOf('|')+1;
            int index2 = afterID.substring(index1+1).indexOf('|')+2;

            if(taxonNamesFound.contains(afterID.substring(index1,index2))){
                throw new IOException("Double taxon name: "+afterID.substring(index1,index2));
            }
            taxonNamesFound.add(" "+afterID.substring(index1,index2).toUpperCase());
            return;
        }


        if(taxonNamesFound.contains(infoLine.substring(1))){
            throw new IOException("Double taxon name: "+infoLine.substring(1));
        }
        taxonNamesFound.add(infoLine.substring(1));
    }

    // GETTER AND SETTER

    public static char getGap(){
        return gap;
    }

    public static String getStringGap(){
        return gap+"";
    }

    public static void setGap(char newGap){
        gap = newGap;
    }

    public static char getMissing(){
        return missing;
    }

    public static String getStringMissing(){
        return missing+"";
    }

    public static void setMissing(char newMissing){
        missing = newMissing;
    }

    public static char getMatchChar(){
        return matchChar;
    }

    public static String getStringMatchChar(){
        return matchChar+"";
    }

    public static void setMatchChar(char newMatchChar){
        matchChar = newMatchChar;
    }
}
