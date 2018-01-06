package splitstree5.utils.nexus;

import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.utils.Alert;

public class CharactersUtilities {

    /**
     * Computes the frequencies matrix from *all* taxa
     *
     * @param chars  the chars
     * @param warned Throw an alert if an unexpected symbol appears.
     * @return the frequencies matrix
     */

    // todo alert from st5 or jloda?

    //TODO: Replace System.err with code throwing exceptions
    //ToDo: BaseFrequencies should be stored somewhere, perhaps characters.properties
    static public double[] computeFreqs(CharactersBlock chars, boolean warned) {
        int ncolors = chars.getNcolors();
        int numNotMissing = 0;
        String symbols = chars.getSymbols();
        int numStates = symbols.length();
        double[] Fcount = new double[numStates];
        char missingchar = chars.getMissingCharacter();
        char gapchar = chars.getGapCharacter();

        for (int i = 1; i < chars.getNtax(); i++) {
            //char[] seq = chars.getRow(i); // todo can do this?
            char[] seq = chars.getMatrix()[i];
            for (int k = 1; k < chars.getNchar(); k++) {
                //if (!chars.isMasked(k)) { // todo not implemented yet
                char c = seq[k];

                //Convert to lower case if the respectCase option is not set
                if (!chars.isRespectCase()) {
                    if (c != missingchar && c != gapchar)
                        c = Character.toLowerCase(c);
                }
                if (c != missingchar && c != gapchar) {
                    numNotMissing = numNotMissing + 1;

                    int state = symbols.indexOf(c);

                    if (state >= 0) {
                        Fcount[state] += 1.0;
                    } else if (state < 0 && !warned) {

                        new Alert("Unknown symbol encountered in characters: " + c);
                        warned = true;
                    }
                }
                //}
            }
        }

        for (int i = 0; i < numStates; i++)
            Fcount[i] = Fcount[i] / (double) numNotMissing;

        return Fcount;

    }
}
