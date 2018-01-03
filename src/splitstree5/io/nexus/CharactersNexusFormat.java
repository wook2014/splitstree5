/*
 *  Copyright (C) 2018 Daniel H. Huson
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

package splitstree5.io.nexus;

/**
 * Characters nexus format
 * Created by huson on 12/22/16.
 */
public class CharactersNexusFormat {
    private boolean transpose;
    private boolean interleave;
    private boolean labels = true;
    private boolean tokens;
    private char matchChar = 0;
    private int columnsPerBlock = 0;

    private boolean ignoreMatrix = false;

    /**
     * the Constructor
     */
    public CharactersNexusFormat() {
    }

    public boolean isTranspose() {
        return transpose;
    }

    public void setTranspose(boolean transpose) {
        this.transpose = transpose;
    }

    public boolean isInterleave() {
        return interleave;
    }

    public void setInterleave(boolean interleave) {
        this.interleave = interleave;
    }

    public boolean isLabels() {
        return labels;
    }

    public void setLabels(boolean labels) {
        this.labels = labels;
    }


    public boolean isTokens() {
        return tokens;
    }

    public void setTokens(boolean tokens) {
        this.tokens = tokens;
    }

    public char getMatchChar() {
        return matchChar;
    }

    public void setMatchChar(char matchChar) {
        this.matchChar = matchChar;
    }

    /**
     * if set, will not read or write matrix
     *
     * @return true, if matrix ignored
     */
    public boolean isIgnoreMatrix() {
        return ignoreMatrix;
    }

    public void setIgnoreMatrix(boolean ignoreMatrix) {
        this.ignoreMatrix = ignoreMatrix;
    }

    public int getColumnsPerBlock(){
        return this.columnsPerBlock;
    }

    public void setColumnsPerBlock(int columnsPerBlock){
        this.columnsPerBlock = columnsPerBlock;
    }
}
