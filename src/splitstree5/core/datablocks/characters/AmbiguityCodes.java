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

package splitstree5.core.datablocks.characters;

/**
 * Ambiguity codes
 * Created by huson on 1/16/17.
 */
public class AmbiguityCodes {
    public static String CODES = "wrkysmbhdvn";

    /**
     * gets all nucleotides associated with a given code
     *
     * @param code
     * @return all (lowercase) letters associated with the given code, or the nucleotide it self, if not a code
     */
    public static String getNucleotides(char code) {
        switch (Character.toLowerCase(code)) {
            case 'w':
                return "at";

            case 'r':
                return "ag";

            case 'k':
                return "gt";

            case 'y':
                return "ct";

            case 's':
                return "cg";

            case 'm':
                return "ac";

            case 'b':
                return "cgt";

            case 'h':
                return "act";

            case 'd':
                return "agt";

            case 'v':
                return "acg";

            case 'n':
                return "acgt";

            default:
                return "" + code; // this is not a code, but a nucleotide
        }
    }

    /**
     * is the given letter an ambiguity code?
     *
     * @param ch
     * @return true, if code, false otherwise
     */
    public static boolean isAmbiguityCode(char ch) {
        return CODES.indexOf(Character.toLowerCase(ch)) != -1;
    }
}
