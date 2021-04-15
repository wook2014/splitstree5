/*
 * TreesNexusImporter.java Copyright (C) 2021. Daniel H. Huson
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
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.imports.interfaces.IImportTrees;
import splitstree5.io.nexus.TreesNexusInput;

import java.io.IOException;
import java.util.List;

/**
 * nexus trees importer
 * daniel huson, 2.2018
 */
public class TreesNexusImporter extends NexusImporterBase<TreesBlock> implements IImportTrees {

    @Override
    public boolean isApplicable(String fileName) throws IOException {
        return isApplicable(fileName, TreesBlock.BLOCK_NAME);
    }

    @Override
    public List<String> parseBlock(NexusStreamParser np, TaxaBlock taxaBlock, TreesBlock dataBlock) throws IOException {
        final TreesNexusInput input = new TreesNexusInput();
        final List<String> taxa = input.parse(np, taxaBlock, dataBlock);
        setTitleAndLink(input.getTitle(), input.getLink());
        return taxa;
    }
}
