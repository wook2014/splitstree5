/*
 * DistancesNexusImporter.java Copyright (C) 2020. Daniel H. Huson
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
import splitstree5.core.datablocks.GenomesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.imports.interfaces.IImportGenomes;
import splitstree5.io.nexus.GenomesNexusInput;

import java.io.IOException;
import java.util.List;

/**
 * nexus genomes importer
 * daniel huson, 3.2020
 */
public class GenomesNexusImporter extends NexusImporterBase<GenomesBlock> implements IImportGenomes {

    @Override
    public boolean isApplicable(String fileName) throws IOException {
        return isApplicable(fileName, GenomesBlock.BLOCK_NAME);
    }

    @Override
    public List<String> parseBlock(NexusStreamParser np, TaxaBlock taxaBlock, GenomesBlock dataBlock) throws IOException {
        final GenomesNexusInput input = new GenomesNexusInput();
        final List<String> taxa = input.parse(np, taxaBlock, dataBlock);
        setTitleAndLink(input.getTitle(), input.getLink());
        return taxa;
    }
}
