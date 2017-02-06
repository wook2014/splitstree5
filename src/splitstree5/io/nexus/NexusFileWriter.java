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

import jloda.util.Basic;
import splitstree5.core.Document;
import splitstree5.core.algorithms.filters.TaxaFilter;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.*;

import java.io.*;

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
            TaxaNexusIO.write(writer, document.getDag().getTopTaxaNode().getDataBlock());

            // top data-block
            write(writer, document.getDag().getTopTaxaNode().getDataBlock(), document.getDag().getTopDataNode().getDataBlock());

            // taxa filter and second block, if necessary:
            AConnector<TaxaBlock, TaxaBlock> taxaFilter = new AConnector<>(document.getDag().getTopTaxaNode().getDataBlock(), document.getDag().getTopTaxaNode(), document.getDag().getWorkingTaxaNode(), new splitstree5.core.algorithms.filters.TaxaFilter());
            if (((TaxaFilter) taxaFilter.getAlgorithm()).getDisabledTaxa().size() > 0) { // some taxa have been filtered
                writer.write("[NEED to report taxa filter]\n");
            }

            // need to report the rest of the blocks
        }

    }

    /**
     * writes a datablock in nexus format
     *
     * @param w
     * @param taxaBlock
     * @param dataBlock
     * @throws IOException
     */
    public static void write(Writer w, TaxaBlock taxaBlock, ADataBlock dataBlock) throws IOException {
        if (dataBlock instanceof TaxaBlock) {
            TaxaNexusIO.write(w, (TaxaBlock) dataBlock);
        } else if (dataBlock instanceof AnalysisResultBlock) {
            AnalysisResultIO.write(w, (AnalysisResultBlock) dataBlock);
        } else if (dataBlock instanceof DistancesBlock) {
            DistancesNexusIO.write(w, taxaBlock, (DistancesBlock) dataBlock, null);
        } else if (dataBlock instanceof TreesBlock) {
            TreesNexusIO.write(w, taxaBlock, (TreesBlock) dataBlock, null);
        } else if (dataBlock instanceof SplitsBlock) {
            SplitsNexusIO.write(w, taxaBlock, (SplitsBlock) dataBlock, null);
        } else if (dataBlock instanceof CharactersBlock) {
            CharactersNexusIO.write(w, taxaBlock, (CharactersBlock) dataBlock, null);
        } else {
            System.err.println("Nexus write not implemented for block of type " + Basic.getShortName(dataBlock.getClass()));
        }
    }

    /**
     * write to string
     *
     * @param taxaBlock
     * @param dataBlock
     * @return block as string
     */
    public static String toString(TaxaBlock taxaBlock, ADataBlock dataBlock) {
        final StringWriter w = new StringWriter();
        try {
            write(w, taxaBlock, dataBlock);
        } catch (IOException e) {
            return e.getMessage();
        }
        return w.toString();
    }

}
