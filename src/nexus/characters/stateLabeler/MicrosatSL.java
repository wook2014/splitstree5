package nexus.characters.stateLabeler;

import splitstree4.core.SplitsException;
import splitstree4.nexus.Characters;

import java.util.List;
import java.util.TreeSet;

/**
 * Created by Daria on 30.10.2016.
 */
public class MicrosatSL implements StateLabeler {

    TreeSet charsUsed; //Set of characters used in microsatelite data.

    /**
     * Constructor
     * initialises constructs the token handler. Records the chars used for missing, gap and match characters,
     * so these don't get used in the encoding and cause havoc.
     *
     * @param characters Characters block, from which we get number sites, gap, match and missing characters.
     */
    public MicrosatSL(Characters characters) {
        charsUsed = new TreeSet();
    }


    @Override
    public char token2char(int site, String token) throws SplitsException {
        int val = Integer.parseInt(token);
        char ch = (char) (val + OFFSET);
        charsUsed.add(ch);
        return ch;
    }

    @Override
    public String char2token(int site, char ch) {
        return (new Integer((int) ch - OFFSET)).toString();
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
        Character ch = (Character) charsUsed.last();
        Integer maxVal = (int) ch;
        return (maxVal.toString()).length();
    }

    @Override
    public String getSymbolsUsed() {
        StringBuilder symbols = new StringBuilder();
        for (Object aCharsUsed : charsUsed) {
            symbols.append(((Character) aCharsUsed).charValue());
        }
        return symbols.toString();
    }
}
