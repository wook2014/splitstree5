/*
 * CharactersFormat.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package splitstree5.io.imports;

import jloda.util.IOExceptionWithLineNumber;
import splitstree5.core.datablocks.characters.CharactersType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Import characters data in different formats
 * Daria Evseeva, 15.08.2017.
 */
public abstract class CharactersFormat {
    private char gap = '-';
    private char missing = 0; // is set when charactersType is set
    private char matchChar = '.';
    private CharactersType charactersType = CharactersType.Unknown;

    /**
     * add new taxa taxon to a given list of taxa labels
     * if repeating taxa label is found, convert to "label + number" form
     *
     * @param line
     * @param taxonNames
     * @param linesCounter
     */
    static void addTaxaName(String line, ArrayList<String> taxonNames, int linesCounter) {
        int sameNamesCounter = 0;
        if (taxonNames.contains(line.substring(1))) {
            System.err.println("Warning: Repeated taxon name " + line.substring(1) + ". Line: " + linesCounter);
            sameNamesCounter++;
        }
        while (taxonNames.contains(line.substring(1) + "(" + sameNamesCounter + ")")) {
            sameNamesCounter++;
        }

        if (sameNamesCounter == 0)
            taxonNames.add(line.substring(1));
        else
            taxonNames.add(line.substring(1) + "(" + sameNamesCounter + ")");
    }

    /**
     * check if a given sequence contains only numbers, alphabetic symbols and gap/missing/match chars
     *
     * @param line
     * @param counter
     * @param allowedChars
     * @throws IOException
     */
    protected static void checkIfCharactersValid(String line, int counter, String allowedChars) throws IOException {
        if (line.isEmpty())
            throw new IOExceptionWithLineNumber("No characters sequence is given", counter);

        String regex = "[^a-z0-9 \t" + allowedChars + "]";
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(line);
        boolean found = m.find();
        if (found) {
            String foundSymbol = m.group();
            throw new IOExceptionWithLineNumber("Unexpected character: " + foundSymbol
                    + "\nIf the symbol represents gap or missing char set it in the Input Editor.", counter);
        }
    }

    // GETTER AND SETTER

    public char getGap() {
        return gap;
    }

    public void setGap(char newGap) {
        gap = newGap;
    }

    public char getMissing() {
        return missing;
    }

    public void setMissing(char newMissing) {
        missing = newMissing;
    }

    public CharactersType getCharactersType() {
        return charactersType;
    }

    public void setCharactersType(CharactersType charactersType) {
        this.charactersType = charactersType;
    }

    public char getMatchChar() {
        return matchChar;
    }

    public void setMatchChar(char newMatchChar) {
        matchChar = newMatchChar;
    }

}
