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

package splitstree5.io.nexus;

/**
 * Characters nexus format
 * Created by huson on 12/22/16.
 */
public class CharactersNexusFormat {
    private boolean respectCase;
    private boolean transpose;
    private boolean interleave;
    private boolean labels;
    private boolean labelQuotes;
    private boolean tokens;
    private char matchChar = 0;

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

    public boolean isLabelQuotes() {
        return labelQuotes;
    }

    public void setLabelQuotes(boolean labelQuotes) {
        this.labelQuotes = labelQuotes;
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

    public boolean isRespectCase() {
        return respectCase;
    }

    public void setRespectCase(boolean respectCase) {
        this.respectCase = respectCase;
    }
}
