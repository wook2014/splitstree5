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

import splitstree5.core.Document;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.filters.TaxaFilter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * writes a document in nexus format
 * Created by huson on 12/27/16.
 */
public class NexusFileWriter {
    /**
     * writes a document in nexus format
     *
     * @param document
     */
    public static void write(Document document) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(document.getFileName()))) {
            writer.write("#nexus\n");

            // taxa block:
            (new TaxaNexusIO(document.getTopTaxaNode().getDataBlock())).write(writer, null);

            // taxa filter and second block, if necessary:
            final TaxaFilter taxaFilter = (TaxaFilter) document.getTopTaxaNode().getChildren().get(0);
            if (taxaFilter.getDisabledData().size() > 0) { // some taxa have been filtered
                writer.write("[NEED to report taxa filter]\n");
            }

            if (document.getTopDataNode().getDataBlock() instanceof DistancesBlock) {
                (new DistancesNexusIO((DistancesBlock) document.getTopDataNode().getDataBlock())).write(writer, null);

            }
        }

    }

}
