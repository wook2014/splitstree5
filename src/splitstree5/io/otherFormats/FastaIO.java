package splitstree5.io.otherFormats;

import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.characters.AmbiguityCodes;
import splitstree5.core.datablocks.characters.CharactersType;
import splitstree5.io.otherFormats.CharactersFormat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Import Characters in FastA format.
 * Daria Evseeva, 07.2017
 */
public class FastaIO extends CharactersFormat{

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

        int counter = 0;
        try (BufferedReader in = new BufferedReader(new FileReader(inputFile))) {
            counter ++;
            String line;
            int sequenceLength = 0;
            String sequence = "";
            boolean startedNewSequence = false;


            while ((line = in.readLine()) != null) {

                if (line.startsWith(";"))
                    continue;
                if (line.equals(">"))
                    throw new IOException("No taxa label given at the sequence " + (ntax + 1));

                if (line.startsWith(">")) {
                    startedNewSequence = true;
                    addTaxaName(line, taxonNamesFound);
                    ntax++;
                } else {
                    if (startedNewSequence) {
                        if (!sequence.equals("")) matrix.add(sequence);
                        if (nchar != 0 && nchar != sequenceLength) {
                            throw new IOException("Sequences must be the same length. Wrong number of chars at the sequence "
                                    + (ntax - 1)+" line: "+counter);
                        }
                        nchar = sequenceLength;
                        sequenceLength = 0;
                        sequence = "";
                        startedNewSequence = false;
                    }
                    sequenceLength += line.length();
                    sequence += line;
                }
            }

            if (sequence.length() == 0)
                throw new IOException("Sequence " + ntax + " is zero");
            matrix.add(sequence);
            if (nchar != sequenceLength)
                throw new IOException("Sequences must be the same length. Wrong number of chars at the sequence " + ntax);

        }
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

        Map<Character, Integer> frequency = new LinkedHashMap<>();
        String foundSymbols = "";
        for(int i = 1; i<=characters.getNtax(); i++){
            for(int j = 1; j<=characters.getNchar(); j++){
                char symbol = Character.toLowerCase(matrix.get(i-1).charAt(j-1));
                if(foundSymbols.indexOf(symbol) == -1) {
                    foundSymbols+=symbol;
                    frequency.put(symbol, 1);
                } else
                    frequency.put(symbol, frequency.get(symbol)+1);
                characters.set(i, j, matrix.get(i-1).charAt(j-1));
            }
        }

        estimateDataType(foundSymbols, characters, frequency);
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
        // todo ? boolean doubleID; first ID

        // todo option make names unique

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
            System.err.println("name-"+afterID.substring(index1,index2).toUpperCase());
            taxonNamesFound.add(afterID.substring(index1,index2).toUpperCase());
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
