/*
 *  CharactersFormat.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.io.imports;

import jloda.util.IOExceptionWithLineNumber;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.characters.AmbiguityCodes;
import splitstree5.core.datablocks.characters.CharactersType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Import characters data in different formats
 * Daria Evseeva, 15.08.2017.
 */
public abstract class CharactersFormat {

    private char gap = '-';
    private char missing = '?';
    private char matchChar = '.';

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

    /**
     * Estimates data type of tha imported characters (dna, protein, standard)
     *
     * @param foundSymbols list of all symbols present in characters
     * @param characters character block
     * @param frequency frequencies list for the "foundSymbols"
     * @throws IOException
     */
    void estimateDataType(String foundSymbols, CharactersBlock characters, Map<Character, Integer> frequency) throws IOException {

        String originalFoundSymbols = foundSymbols;
        foundSymbols = foundSymbols.replace(getGap() + "", "");
        foundSymbols = foundSymbols.replace(getMissing() + "", "");
        foundSymbols = foundSymbols.replace(getMatchChar() + "", "");
        // sort found symbols
        char[] chars = foundSymbols.toCharArray();
        Arrays.sort(chars);
        String sortedSymbols = new String(chars);

        switch (sortedSymbols) {
            case "01":
                characters.setDataType(CharactersType.Standard);
                break;
            case "acgt":
                characters.setDataType(CharactersType.DNA);
                break;
            case "acgu":
                characters.setDataType(CharactersType.RNA);
                break;
            case "acdefghiklmnpqrstvwyz":
                characters.setDataType(CharactersType.Protein);
                break;
            default:  //todo redundant? delete?
                char x = getUnknownSymbol(sortedSymbols);
                if (x == '\u0000') {
                    if (sortedSymbols.contains("b") || hasMostNucleotide(frequency)) {
                        if (sortedSymbols.contains("t")) characters.setDataType(CharactersType.DNA);
                        if (sortedSymbols.contains("u")) characters.setDataType(CharactersType.RNA);
                        if (sortedSymbols.contains("t") && sortedSymbols.contains("u"))
                            throw new IOException("Nucleotide sequences contains Thymine and Uracil at the same time");
                    }
                    if (hasAAOnlySymbols(sortedSymbols))
                        characters.setDataType(CharactersType.Protein);
                } else {
                    characters.setDataType(CharactersType.Unknown);
                    System.err.println("Warning : can not recognize characters type!");
                    System.err.println("Unexpected character: '" + x + "'");
                }
                // todo set new gap/missing/match chars and try again
                break;
        }
        //System.err.println("symbols: " + sortedSymbols);
        //System.err.println("frequencies : " + frequency);

        // set 'N' instead '?' as unknown symbol, if needed
        if (characters.getDataType().equals(CharactersType.DNA) ||
                characters.getDataType().equals(CharactersType.RNA)) {
            if (!originalFoundSymbols.contains("?") && originalFoundSymbols.contains("n"))
                characters.setMissingCharacter('N');
            if (originalFoundSymbols.contains("?") && originalFoundSymbols.contains("n"))
                throw new IOException("Nucleotide sequences contain 2 designations of missing symbol : '?' and 'N'");
        }
    }

    /**
     * Get first symbol, which is not nucleotide or amino acid symbol.
     * If only nucleotide od aa symbols are found return '\u0000' = null character.
     *
     * @param sortedSymbols
     * @return
     */

    private static char getUnknownSymbol(String sortedSymbols) {
        String knownSymbols = CharactersType.Protein.getSymbols() + CharactersType.DNA.getSymbols() +
                CharactersType.RNA.getSymbols() + AmbiguityCodes.CODES;
        for (char c : sortedSymbols.toCharArray()) {
            if (knownSymbols.indexOf(c) == -1) {
                return c;
            }
        }
        return '\u0000';
    }

    private static boolean hasAAOnlySymbols(String foundSymbols) {
        final String IUPAC = ("acgtu" + AmbiguityCodes.CODES);
        final String AA = CharactersType.Protein.getSymbols();
        for (char c : foundSymbols.toCharArray()) {
            if (AA.contains(c + "") && !IUPAC.contains(c + "")) return true;
        }
        return false;
    }

    private static boolean hasMostNucleotide(Map<Character, Integer> frequency) {
        int nFreq = 0;
        int otherFreq = 0;
        for (char c : frequency.keySet()) {
            if (c == 'a' || c == 'g' || c == 'c' || c == 't' || c == 'u')
                nFreq += frequency.get(c);
            else
                otherFreq += frequency.get(c);
        }
        return nFreq >= otherFreq;
    }

    private static boolean isAmbiguous(String foundSymbols) {
        final String IUPAC = "acgtu" + AmbiguityCodes.CODES;
        for (char c : foundSymbols.toCharArray()) {
            if (!IUPAC.contains(c + "")) return false;
        }
        return true;
    }

    private static boolean checkSubset(String sortedSymbols) {
        boolean isSubSet = true;
        for (char c : sortedSymbols.toCharArray()) {
            if ("acdefghiklmnpqrstvwyz".indexOf(c) == -1) {
                isSubSet = false;
                break;
            }
        }
        return isSubSet;
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

    public char getMatchChar() {
        return matchChar;
    }
    public void setMatchChar(char newMatchChar) {
        matchChar = newMatchChar;
    }
}
