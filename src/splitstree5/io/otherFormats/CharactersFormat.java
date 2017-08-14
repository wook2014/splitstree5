package splitstree5.io.otherFormats;

import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.characters.AmbiguityCodes;
import splitstree5.core.datablocks.characters.CharactersType;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Daria on 15.08.2017.
 */
public abstract class CharactersFormat {

    private static char gap = '-';
    private static char missing = '?';
    private static char matchChar='.';


    public static void estimateDataType(String foundSymbols, CharactersBlock characters) throws IOException {
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
