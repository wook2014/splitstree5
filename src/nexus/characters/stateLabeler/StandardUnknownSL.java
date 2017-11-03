package nexus.characters.stateLabeler;

import splitstree4.core.SplitsException;
import splitstree4.nexus.Characters;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Daria on 30.10.2016.
 */
public class StandardUnknownSL implements StateLabeler{

    int nChars; //todo: changed, instead of getNchar function

    HashMap<String, Character>[] token2charMaps; //Map from strings to char, one for each site
    HashMap<Character, String>[] char2tokenMaps;  //Reverse of the above map

    int maxState; //The states used will be characters 0....maxStates (incl) in availableChars
    String availableChars; //List of ascii characters for use in standard mode

    /**
     * Constructor
     * Build up a string containing all characters we can use.
     *
     * @param characters
     */
    public StandardUnknownSL(Characters characters){
        maxState = -1;

        availableChars = "1234567890";  //These are the standard ones for paup, mesquite etc.
        availableChars += "abcdefghijklmnopqrstuvwxyz";    //augment them with lower case letters
        for (char ch = 192; ch <= 255; ch++)           //and misc. ascii characters.
            availableChars += "" + ch;

        //Now remove characters that are forbidden
        String forbidden = ";\\[\\],\\(\\)/"; //punctuation characters  // todo: there is a problem with this expression
        forbidden += regString(characters.getFormat().getMissing());
        forbidden += regString(characters.getFormat().getMatchchar());
        forbidden += regString(characters.getFormat().getGap());
        availableChars = availableChars.replaceAll("[" + forbidden + "]", "");

        //Initialise the maps at each site.
        nChars = characters.getNchar();
        token2charMaps = new HashMap[nChars + 1];
        char2tokenMaps = new HashMap[nChars + 1];
        for (int i = 1; i <= nChars; i++) {
            token2charMaps[i] = new HashMap();
            char2tokenMaps[i] = new HashMap();
        }
    }

    //TODO: The following don't apply to microsat or protein+token

    /**
     * Check if a site has states stored for it
     *
     * @param site NUmber of site (character)
     * @return true if states/tokens have been stored for that site.
     */
    protected boolean hasStates(int site) {
        return !(token2charMaps.length <= site || token2charMaps[site] == null) && (!token2charMaps[site].isEmpty());
    }


    String[] getStates(int site) {
        int size = char2tokenMaps[site].size();
        String[] stateArray = new String[size];
        int i = 0;
        char ch = availableChars.charAt(i);
        while (char2tokenMaps[site].containsKey(ch)) {
            stateArray[i] = char2tokenMaps[site].get(ch);
            i++;
            ch = availableChars.charAt(i);
        }
        return stateArray;
    }

    /**
     * Encode ch for use in a reg exp.
     *
     * @param ch character
     * @return String the character, possibly with a backslash before.
     */
    private String regString(char ch) {
        if (ch == '^' || ch == '-' || ch == ']' || ch == '\\')
            return "\\" + ch;
        else
            return "" + ch;
    }

    @Override
    public char token2char(int site, String token) throws SplitsException {
        if (token2charMaps[site].containsKey(token)) {
            return token2charMaps[site].get(token);
        } else {
            int id = token2charMaps[site].size() + 1;
            if (id >= availableChars.length())
                throw new SplitsException("Too many alleles per site: please contact authors");
            Character ch = availableChars.charAt(id - 1);
            maxState = Math.max(maxState, id - 1);
            token2charMaps[site].put(token, ch);
            char2tokenMaps[site].put(ch, token);
            return ch;
        }
    }

    @Override
    public String char2token(int site, char ch) {
        return char2tokenMaps[site].get(ch);
    }

    @Override
    public String parseSequence(List<String> tokens, int firstSite, boolean transposed) throws SplitsException {
        char[] chars = new char[tokens.size()]; //For efficiency, allocate this as an array, then convert to string.
        int site = firstSite;
        int index = 0;
        for (String token : tokens) {
            chars[index] = token2char(site, token);
            if (!transposed)
                site++;
            index++;
        }
        return new String(chars);
    }

    @Override
    public int getMaximumLabelLength() {
        int max = 0;
        for (int i = 1; i <= nChars; i++)
            for (String token : token2charMaps[i].keySet())
                max = Math.max(max, (token).length());
        return max;
    }

    @Override
    public String getSymbolsUsed() {
        if (maxState < 0)
            return null;
        else
            return availableChars.substring(0, maxState + 1);
    }
}
