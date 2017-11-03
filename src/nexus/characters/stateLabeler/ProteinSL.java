package nexus.characters.stateLabeler;

import nexus.characters.DataTypesChar;
import splitstree4.core.SplitsException;
import splitstree4.nexus.Characters;

import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by Daria on 30.10.2016.
 */
public class ProteinSL implements StateLabeler {

    HashMap<String, Character>[] token2charMaps; //Map from strings to char, one for each site
    HashMap<Character, String>[] char2tokenMaps;  //Reverse of the above map

    int maxState; //The states used will be characters 0....maxStates (incl) in availableChars
    String availableChars; //List of ascii characters for use in standard mode

    TreeSet charsUsed; //Set of characters used in microsatelite data.

    /**
     * Constructor sets up maps to go from 3 letter code to 1 letter AA code.
     *
     * @param characters Characters block, from which we get number sites, gap, match and missing characters.
     */

    public ProteinSL(Characters characters){
        maxState = -1;
        availableChars = DataTypesChar.PROTEIN.getSymbols();
        String[] codes = {"ala", "arg", "asn", "asp", "cys", "gln", "glu", "gly", "his", "ile", "leu",
                "lys", "met", "phe", "pro", "ser", "thr", "trp", "tyr", "val"};

        token2charMaps = new HashMap[1];
        char2tokenMaps = new HashMap[1];
        token2charMaps[0] = new HashMap();
        char2tokenMaps[0] = new HashMap();

        for (int i = 0; i < 20; i++) {
            token2charMaps[0].put(codes[i], availableChars.charAt(i));
            char2tokenMaps[0].put(availableChars.charAt(i), codes[i]);
        }
    }

    @Override
    public char token2char(int site, String token) throws SplitsException {
        if (token2charMaps[0].containsKey(token))
            return token2charMaps[site].get(token);
        else
            throw new SplitsException("Unidentified amino acid: " + token);
    }

    @Override
    public String char2token(int site, char ch) {
        return char2tokenMaps[0].get(ch);
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
        return 3;
    }

    @Override
    public String getSymbolsUsed() {
        return DataTypesChar.PROTEIN.getSymbols();
    }
}
