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
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.imports.interfaces.IImportDistances;
import splitstree5.io.nexus.DistancesNexusInput;

import java.io.IOException;
import java.util.List;

/**
 * nexus distances importer
 * daniel huson, 2.2018
 */
public class DistancesNexusImporter extends NexusImporterBase<DistancesBlock> implements IImportDistances {

    @Override
    public boolean isApplicable(String fileName) throws IOException {
        return isApplicable(fileName, DistancesBlock.BLOCK_NAME);
    }

    @Override
    public List<String> parseBlock(NexusStreamParser np, TaxaBlock taxaBlock, DistancesBlock dataBlock) throws IOException {
        final DistancesNexusInput input = new DistancesNexusInput();
        final List<String> taxa = input.parse(np, taxaBlock, dataBlock);
        setTitleAndLink(input.getTitle(), input.getLink());
        return taxa;
    }

    @Override
    public void setSimilarities(boolean similarities) {
        // todo: use the nexus format or import here?
    }

    @Override
    public void setSimilaritiesCalculation(String similaritiesCalculation) {
    }
}
