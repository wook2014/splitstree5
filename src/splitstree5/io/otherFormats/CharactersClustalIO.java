package splitstree5.io.otherFormats;

/**
 * Created by Daria on 05.08.2017.
 */

import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.characters.AmbiguityCodes;
import splitstree5.core.datablocks.characters.CharactersType;
import splitstree5.io.nexus.CharactersNexusFormat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The sequence alignment outputs from CLUSTAL software often are given the default extension .aln.
 * CLUSTAL is an interleaved format. In a page-wide arrangement the
 * sequence name is in the first column and a part of the sequence’s data is right justified.
 *
 * http://www.clustal.org/download/clustalw_help.txt
 */

public class CharactersClustalIO {

    private static char gap = '-';
    private static char missing = '?';
    private static char matchChar='.';

    public static void parse(String inputFile, TaxaBlock taxa, CharactersBlock characters, CharactersNexusFormat format) throws IOException {

        Map<String, String> taxa2seq = new LinkedHashMap<>();

        int ntax = 0;
        int nchar = 0;
        int sequenceInLineLength = 0;
        int counter = 0;
        try (BufferedReader in = new BufferedReader(new FileReader(inputFile))) {
            String line;
            String sequence = "";
            boolean startedNewSequence = false;

            while ((line = in.readLine()) != null) {
                counter ++;
                if (line.startsWith("CLUSTAL"))
                    continue;
                if(!line.equals("") && hasAlphabeticalSymbols(line)){

                    int lastSeqIndex = line.length();
                    while(Character.isDigit(line.charAt(lastSeqIndex-1)))
                        lastSeqIndex--;
                    line = line.substring(0, lastSeqIndex);

                    int labelIndex = line.indexOf(' ');
                    String label = line.substring(0, labelIndex);

                    line = line.replace(" ", "");
                    line = line.replace("\t", "");

                    if(sequenceInLineLength==0) sequenceInLineLength = line.substring(labelIndex).length();

                    if (!taxa2seq.containsKey(label)){
                        taxa2seq.put(label, line.substring(labelIndex));
                    }else{
                        taxa2seq.put(label, taxa2seq.get(label)+line.substring(labelIndex));
                    }
                }
            }
        }

        if(taxa2seq.isEmpty())
            throw new IOException("No sequences was found");

        ntax = taxa2seq.size();
        nchar = taxa2seq.get(taxa2seq.keySet().iterator().next()).length();
        for(String s : taxa2seq.keySet()){
            if(nchar != taxa2seq.get(s).length())
                throw new IOException("Sequences must be the same length." +
                        "Wrong number of chars at the sequence " + s);
            else
                nchar =  taxa2seq.get(s).length();
        }

        System.err.println("ntax: "+ntax+" nchar: "+nchar);
        for(String s : taxa2seq.keySet()){
            System.err.println(s);
            System.err.println(taxa2seq.get(s));
        }

        taxa.clear();
        taxa.addTaxaByNames(taxa2seq.keySet());
        setCharacters(taxa2seq, ntax, nchar, characters);

        format.setInterleave(true);
        format.setColumnsPerBlock(sequenceInLineLength);
    }

    private static void setCharacters(Map<String, String> taxa2seq, int ntax, int nchar, CharactersBlock characters) throws IOException {
        characters.clear();
        characters.setDimension(ntax, nchar);

        int labelsCounter = 1;
        String foundSymbols = "";
        for(String label : taxa2seq.keySet()){
            for(int j=1; j<=nchar; j++){

                char symbol = taxa2seq.get(label).charAt(j-1);
                if(foundSymbols.indexOf(Character.toLowerCase(symbol)) == -1)
                    foundSymbols+=Character.toLowerCase(symbol);

                characters.set(labelsCounter, j, symbol);
            }
            labelsCounter++;
        }

        foundSymbols = foundSymbols.replace(getStringGap(), "");
        foundSymbols = foundSymbols.replace(getStringMissing(), "");
        foundSymbols = foundSymbols.replace(getStringMatchChar(), "");
        // sort found symbols
        char[] chars = foundSymbols.toCharArray();
        Arrays.sort(chars);
        String sortedSymbols = new String(chars);

        // todo check subset!
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

                if (checkSubset(foundSymbols)) {
                    characters.setDataType(CharactersType.protein);
                    break;
                }

                // todo statistic to distinguish protein and ambiguous dna

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
        System.err.println("symbols: "+sortedSymbols);

    }

    private static boolean isAmbiguous(String foundSymbols){
        final String IUPAC = "acgtu"+ AmbiguityCodes.CODES;
        for(char c : foundSymbols.toCharArray()){
            if(!IUPAC.contains(c+"")) return false;
        }
        return true;
    }

    private static boolean checkSubset(String sortedSymbols){
        boolean isSubSet=true;
        for(char c : sortedSymbols.toCharArray()){
            if("acdefghiklmnpqrstvwyz".indexOf(c)==-1){
                isSubSet=false;
                break;
            }
        }
        return isSubSet;
    }

    private static boolean hasAlphabeticalSymbols(String line){
        for(char c : line.toCharArray()){
            if(Character.isLetter(c)) return true;
        }
        return false;
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
