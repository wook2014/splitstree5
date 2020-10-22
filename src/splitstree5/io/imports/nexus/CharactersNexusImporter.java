/*
 * CharactersNexusImporter.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.io.imports.nexus;

import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.imports.interfaces.IImportCharacters;
import splitstree5.io.nexus.CharactersNexusInput;

import java.io.IOException;
import java.util.List;

/**
 * nexus characters importer
 * daniel huson, 2.2018
 */
public class CharactersNexusImporter extends NexusImporterBase<CharactersBlock> implements IImportCharacters {
    @Override
    public boolean isApplicable(String fileName) throws IOException {
        return isApplicable(fileName, CharactersBlock.BLOCK_NAME);
    }

    @Override
    public List<String> parseBlock(NexusStreamParser np, TaxaBlock taxaBlock, CharactersBlock dataBlock) throws IOException {
        final CharactersNexusInput input = (new CharactersNexusInput());
        final List<String> taxa = input.parse(np, taxaBlock, dataBlock);
        setTitleAndLink(input.getTitle(), input.getLink());
        return taxa;
    }

    @Override
    public void setGap(char c) {
    }

    @Override
    public void setMissing(char m) {
    }
}
