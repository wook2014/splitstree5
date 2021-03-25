/*
 * IImportCharacters.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.io.imports.interfaces;

import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.characters.CharactersType;

import java.io.IOException;

public interface IImportCharacters extends IImporter {

    /**
     * parse a file
     *
     * @param fileName
     * @param taxaBlock
     * @param dataBlock
     */
    void parse(ProgressListener progressListener, String fileName, TaxaBlock taxaBlock, CharactersBlock dataBlock)
            throws CanceledException, IOException;

    void setGap(char g);

    void setMissing(char m);

    void setCharactersType(CharactersType charactersType);
}
