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

package splitstree5.core.datablocks;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToChararacters;
import splitstree5.core.datablocks.characters.AmbiguityCodes;
import splitstree5.core.datablocks.characters.CharactersType;
import splitstree5.io.nexus.stateLabeler.StateLabeler;

import java.util.HashMap;
import java.util.Map;

/**
 * A characters block
 * Created by huson on 14/1/2017
 */
public class CharactersBlock extends ADataBlock {
    // characters matrix
    private char[][] matrix;

    // set of symbols used
    private String symbols = "";

    private char gapCharacter = '-';
    private char missingCharacter = '?';

    // array of doubles giving the weights for each site. Set to null means all weights are 1.0
    private double[] characterWeights;

    private boolean hasAmbiguousStates = false;
    private boolean diploid = false;

    private boolean respectCase = false; // respectCase==true hasn't been implemented

    private CharactersType dataType = CharactersType.unknown;

    private StateLabeler stateLabeler;
    private Map<Integer, String> charLabeler;

    // todo: SplitsTree4 uses two tables ambigStates and replacedStates to store replaced ambig

    private float gammaParam = Float.MAX_VALUE;
    private float pInvar = Float.MAX_VALUE;

    /**
     * Number of colors used.
     */
    public int ncolors = 0;

    // maps every symbol in the matrix to an integer "color" (ignoring the case). This map is fixed for known datatypes.

    private final Map<Character, Integer> symbol2color;
    // maps every color to an array of symbols
    private final Map<Integer, char[]> color2symbols;

    /**
     * constructor
     */
    public CharactersBlock() {
        matrix = new char[0][0];
        symbol2color = new HashMap<>();
        color2symbols = new HashMap<>();
    }

    /**
     * named constructor
     *
     * @param name
     */
    public CharactersBlock(String name) {
        this();
        setName(name);
    }

    @Override
    public void clear() {
        super.clear();
        matrix = new char[0][0];
        setShortDescription("");
    }

    /**
     * gets the size
     *
     * @return number of characters
     */
    public int size() {
        return getNchar();
    }

    public void setDimension(int ntax, int nchar) {
        matrix = new char[ntax][nchar];
    }

    /**
     * gets the number of taxa
     *
     * @return taxa
     */
    public int getNtax() {
        return matrix.length;
    }

    /**
     * gets the number of characters
     *
     * @return characters
     */
    public int getNchar() {
        return matrix.length == 0 ? 0 : matrix[0].length;
    }

    /**
     * gets the value for i and j. If the letter is an ambiguity code, then returns the missing character.
     * In the case of an ambiguity code, use getAmbiguousNucleotides() to get all nucleotides associated with the code
     *
     * @param t   in range 1..nTax
     * @param pos in range 1..nchar
     * @return value
     */
    public char get(int t, int pos) {
        return matrix[t - 1][pos - 1];
    }

    /**
     * is the letter at this position an ambiguity code?
     *
     * @param t
     * @param pos
     * @return true if is amb. code
     */
    public boolean isAmbiguityCode(int t, int pos) {
        return isHasAmbiguousStates() && AmbiguityCodes.isAmbiguityCode(matrix[t - 1][pos - 1]);
    }

    /**
     * gets all the nucleotides associated with an  ambiguity
     *
     * @param t
     * @param pos
     * @return all nucleotides or null
     */
    public String getNucleotidesForAmbiguityCode(int t, int pos) {
        return AmbiguityCodes.getNucleotides(matrix[t - 1][pos - 1]);
    }

    /**
     * sets the value
     *
     * @param t     in range 1-nTax
     * @param pos   in range 1-nChar
     * @param value
     */
    public void set(int t, int pos, char value) {
        matrix[t - 1][pos - 1] = value;
    }


    /**
     * set characters, change dimensions if necessary.
     *
     * @param matrix
     */
    public void set(@NotNull char[][] matrix) {
        setDimension(matrix.length, matrix[0].length);
        for (int i = 0; i < matrix.length; i++) {
            System.arraycopy(matrix[i], 0, this.matrix[i], 0, matrix.length);
        }
    }

    public char[][] getMatrix() {
        return matrix;
    }

    public boolean isUseCharacterWeights() {
        return characterWeights != null;
    }

    public void setUseCharacterWeights(boolean use) {
        if (use) {
            if (characterWeights == null)
                characterWeights = new double[getNchar()];
        } else {
            characterWeights = null;
        }
    }

    public void setCharacterWeights(@Nullable double[] characterWeights) {
        if (characterWeights == null)
            this.characterWeights = null;
        else {
            this.characterWeights = new double[getNchar()];
            System.arraycopy(characterWeights, 0, this.characterWeights, 0, characterWeights.length);
        }
    }

    public double[] getCharacterWeights() {
        return this.characterWeights;
    }

    public double getCharacterWeight(int pos) {
        return characterWeights == null ? 1 : characterWeights[pos - 1];
    }

    public void setCharacterWeight(int pos, double w) {
        this.characterWeights[pos - 1] = w;
    }

    public CharactersType getDataType() {
        return dataType;
    }

    public void setDataType(CharactersType dataType) {
        this.dataType = dataType;
        resetSymbols();
    }

    public boolean isHasAmbiguousStates() {
        return hasAmbiguousStates;
    }

    public void setHasAmbiguousStates(boolean hasAmbiguousStates) {
        this.hasAmbiguousStates = hasAmbiguousStates;
    }

    public boolean isDiploid() {
        return diploid;
    }

    public void setDiploid(boolean diploid) {
        this.diploid = diploid;
    }

    public char getGapCharacter() {
        return gapCharacter;
    }

    public void setGapCharacter(char gapCharacter) {
        this.gapCharacter = gapCharacter;
    }

    public char getMissingCharacter() {
        return missingCharacter;
    }

    public void setMissingCharacter(char missingCharacter) {
        this.missingCharacter = missingCharacter;
    }

    public boolean hasGamma() {
        return gammaParam != Float.MAX_VALUE;
    }

    public float getGammaParam() {
        return gammaParam;
    }

    /**
     * set gamma shape parameter (use Float.MAX_VALUE to unset)
     *
     * @param gammaParam
     */
    public void setGammaParam(float gammaParam) {
        this.gammaParam = gammaParam;
    }

    public boolean hasPInvar() {
        return pInvar != Float.MAX_VALUE;
    }

    public float getpInvar() {
        return pInvar;
    }

    /**
     * set proprotion invariable sites (use Float.MAX_VALUE to unset)
     *
     * @param pInvar
     */
    public void setPInvar(float pInvar) {
        this.pInvar = pInvar;
    }

    public String getSymbolsForColor() {
        return symbols;
    }

    public void resetSymbols() {
        if (dataType != null)
            symbols = dataType.getSymbols();
        else
            symbols = "";
        computeColors();
    }

    public String getSymbols() {
        return symbols;
    }

    public void setSymbols(String symbols) {
        this.symbols = symbols;
        computeColors();
    }

    /**
     * Gets the number of colors.
     *
     * @return the number of colors
     */
    public int getNcolors() {
        return this.ncolors;
    }

    /**
     * Returns the color of a character. Colors start at 1
     *
     * @param ch a character
     * @return the color of the character or -1 if the character is not found.
     */
    public int getColor(final char ch) {
        if (symbol2color.get(ch) != null)
            return this.symbol2color.get(ch);
        else
            return -1;
    }

    /**
     * get the color for a given taxon and position
     *
     * @param t
     * @param pos
     * @return color
     */
    public int getColor(int t, int pos) {
        return getColor(matrix[t - 1][pos - 1]);
    }

    /**
     * Returns the symbols associated with a color
     *
     * @param color a color
     * @return an Array with the List of Symbols matching the color
     */
    public char[] getSymbols(int color) {
        return this.color2symbols.get(color);
    }

    /**
     * Computes the color2symbols and symbol2color maps
     */
    public void computeColors() {
        this.symbol2color.clear();
        this.color2symbols.clear();
        int count = 1;
        if (symbols != null) {

            for (byte p = 0; p < symbols.length(); p++) {
                final char lowerCase = Character.toLowerCase(symbols.charAt(p));
                final char upperCase = Character.toUpperCase(symbols.charAt(p));
                if (!this.symbol2color.containsKey(lowerCase)) {
                    this.symbol2color.put(lowerCase, count);
                    if (upperCase != lowerCase) {
                        this.symbol2color.put(Character.toUpperCase(lowerCase), count);
                        this.color2symbols.put(count, new char[]{lowerCase, upperCase});
                    } else
                        this.color2symbols.put(count, new char[]{lowerCase});
                    count++;
                }
            }

        }
        this.ncolors = this.color2symbols.size();
    }

    @Override
    public Class getFromInterface() {
        return IFromChararacters.class;
    }

    @Override
    public Class getToInterface() {
        return IToChararacters.class;
    }

    public boolean isRespectCase() {
        return respectCase;
    }

    public void setRespectCase(boolean respectCase) {
        this.respectCase = respectCase; // todo: respect case isn't implemented and is ignored
    }

    public StateLabeler getStateLabeler() {
        return stateLabeler;
    }

    public void setStateLabeler(StateLabeler stateLabeler) {
        this.stateLabeler = stateLabeler;
    }

    public Map<Integer, String> getCharLabeler() {
        return charLabeler;
    }

    public void setCharLabeler(Map<Integer, String> charLabeler) {
        this.charLabeler = charLabeler;
    }

    /**
     * make a shallow copy of a row
     *
     * @param parent
     * @param parentIndex
     * @param targetIndex
     */
    public void copyRow(CharactersBlock parent, int parentIndex, int targetIndex) {
        matrix[targetIndex - 1] = parent.matrix[parentIndex - 1];
    }

    /**
     * gets row with coordinates starting at 1
     *
     * @param t 1-based index
     * @return row, 1-based
     */
    public char[] getRow(int t) {
        if (t == matrix.length)
            throw new IllegalArgumentException("" + t);
        final char[] src = matrix[t - 1];
        final char[] dest = new char[src.length + 1];
        System.arraycopy(src, 0, dest, 1, src.length);
        return dest;
    }
}


