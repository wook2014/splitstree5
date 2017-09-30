package splitstree5.io.otherFormats;

import com.sun.xml.internal.fastinfoset.util.CharArray;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.characters.AmbiguityCodes;
import splitstree5.core.datablocks.characters.CharactersType;

import java.io.IOException;
import java.util.*;

/**
 * Created by Daria on 15.08.2017.
 */
public abstract class CharactersFormat {

    private static char gap = '-';
    private static char missing = '?';
    private static char matchChar='.';


    public static void estimateDataType(String foundSymbols, CharactersBlock characters, Map<Character, Integer> frequency) throws IOException {
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
                char x = getUnknownSymbols(sortedSymbols);
                if(x =='\u0000'){
                    if(sortedSymbols.contains("b") || hasMostNucleotide(frequency)){
                        characters.setHasAmbiguousStates(true);
                        if(sortedSymbols.contains("t")) characters.setDataType(CharactersType.DNA);
                        if(sortedSymbols.contains("u")) characters.setDataType(CharactersType.RNA);
                        if(sortedSymbols.contains("t") && sortedSymbols.contains("u"))
                            throw new IOException("Nucleotide sequence contains Thymine and Uracil at the same time");
                    }
                    if(hasAAOnlySybols(sortedSymbols))
                        characters.setDataType(CharactersType.protein);
                }else{
                    characters.setDataType(CharactersType.unknown);
                    System.err.println("Warning : can not recognize characters type!");
                    System.err.println("Unexpected character: '"+x+"'");
                }

               /* if (checkSubset(foundSymbols)) {
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
                }*/
                break;
        }
        System.err.println("symbols: "+sortedSymbols);
        System.err.println("frequencies : "+ frequency);
    }

    private static char getUnknownSymbols(String sortedSymbols){
        String knownSymbols = CharactersType.protein.getSymbols() + CharactersType.DNA.getSymbols()+
                CharactersType.RNA.getSymbols() + AmbiguityCodes.CODES;
        for(char c : sortedSymbols.toCharArray()){
            if(knownSymbols.indexOf(c)==-1){
                return c;
            }
        }
        return '\u0000';
    }

    private static boolean hasAAOnlySybols (String foundSymbols){
        final String IUPAC = ("acgtu"+ AmbiguityCodes.CODES);
        final String AA = CharactersType.protein.getSymbols();
        for(char c : foundSymbols.toCharArray()){
            if(AA.contains(c+"") && !IUPAC.contains(c+"")) return true;
        }
        return false;
    }

    private static boolean hasMostNucleotide(Map<Character, Integer> frequency){
        int nFreq = 0;
        int otherFreq = 0;
        for(char c : frequency.keySet()){
            if(c=='a' || c=='g' || c=='c' || c=='t' || c =='u')
                nFreq+=frequency.get(c);
            else
                otherFreq+=frequency.get(c);
        }
        return nFreq>= otherFreq;
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
