/*
 *  Copyright (C) 2016 Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 *  Copyright (C) 2016 Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.gui.utils;

import jloda.fx.Alert;
import splitstree5.core.datablocks.CharactersBlock;

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
