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

package splitstree5.core.datablocks.characters;

/**
 * characters type
 * Daniel Huson, 1/16/17.
 */
public enum CharactersType {
    standard("01"),
    DNA("atgc"),
    RNA("augc"),
    protein("arndcqeghilkmfpstwyvz"),
    microsat(""),
    unknown("");

    private final String symbols;

    CharactersType(String symbols) {
        this.symbols = symbols;
    }

    /**
     * get symbols for a characters type
     *
     * @return symbols
     */
    public String getSymbols() {
        return this.symbols;
    }

    public static CharactersType valueOfIgnoreCase(String str) {
        for (CharactersType type : values()) {
            if (type.toString().equalsIgnoreCase(str))
                return type;
        }
        return unknown;
    }
}
