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

import jloda.util.parse.NexusStreamParser;
import splitstree5.core.Document;
import splitstree5.core.algorithms.NeighborJoining;
import splitstree5.core.algorithms.Report;
import splitstree5.core.dag.UpdateState;
import splitstree5.core.datablocks.*;
import splitstree5.core.misc.Taxon;
import splitstree5.gui.TaxaFilterView;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * parses a nexus file
 * Created by huson on 12/27/16.
 */
public class NexusFileParser {
    /**
     * parses a nexus file
     *
     * @param document
     * @return original taxa and original data
     * @throws IOException
     */
    public static void parse(Document document) throws IOException {
        document.getDag().clear();

        try (NexusStreamParser np = new NexusStreamParser(new FileReader(document.getFileName()))) {
            np.matchIgnoreCase("#nexus");

            if (np.peekMatchIgnoreCase("begin splitstree5;")) { // this is a complete file written by splitstree5
                System.err.println("Not implemented");
            } else { // this is a user input file

                final TaxaBlock topTaxaBlock = new TaxaBlock("TopTaxa");

                boolean needToDetectTaxa = true;

                if (np.peekMatchIgnoreCase("begin taxa;")) {
                    TaxaNexusIO.parse(np, topTaxaBlock);
                    needToDetectTaxa = (topTaxaBlock.getTaxa().size() == 0);
                }
                final ArrayList<String> namesFound = new ArrayList<>();
                final ADataBlock topDataBlock = parseBlock(np, topTaxaBlock, namesFound);
                if (needToDetectTaxa) {
                    if (namesFound.size() == 0)
                        throw new IOException("Couldn't detect taxon names in input file");
                    for (String name : namesFound) {
                        topTaxaBlock.add(new Taxon(name));
                    }
                }

                document.getDag().setupTopAndWorkingNodes(topTaxaBlock, topDataBlock);
                document.getDag().getTopTaxaNode().setState(UpdateState.VALID);
                document.getDag().getTopDataNode().setState(UpdateState.VALID);

                // todo: just for debugging, report on changes of working taxa and changes of distances:
                document.getDag().addConnector(new Report<>(document.getDag().getWorkingTaxaNode().getDataBlock(), document.getDag().getWorkingTaxaNode()));
                document.getDag().addConnector(new Report<>(document.getDag().getWorkingTaxaNode().getDataBlock(), document.getDag().getWorkingDataNode()));

                // todo: just for debugging, open taxa filter view here:
                {
                    final TaxaFilterView taxaFilterView = new TaxaFilterView(document, document.getDag().getTaxaFilter());
                    taxaFilterView.show();
                }

                // todo: for debugging, add fake NJ and trees node:
                if (document.getDag().getWorkingDataNode().getDataBlock() instanceof DistancesBlock) {
                    ADataNode<TreesBlock> treesNode = document.getDag().createDataNode(new TreesBlock());
                    document.getDag().createConnector(document.getDag().getWorkingDataNode(), treesNode, new NeighborJoining());
                    document.getDag().addConnector(new Report<>(document.getDag().getWorkingTaxaNode().getDataBlock(), treesNode));
                }
            }
        }
    }

    /**
     * parse a block
     *
     * @param np
     * @param taxaBlock
     * @return block parsed
     * @throws IOException
     */
    public static ADataBlock parseBlock(NexusStreamParser np, TaxaBlock taxaBlock, List<String> taxonNamesFound) throws IOException {
        if (np.peekMatchIgnoreCase("begin")) {
            if (np.peekMatchIgnoreCase("begin taxa;")) {
                final TaxaBlock taxaBlock2 = new TaxaBlock();
                TaxaNexusIO.parse(np, taxaBlock2);
                return taxaBlock2;
            } else if (np.peekMatchIgnoreCase("begin distances;")) {
                final DistancesBlock distances = new DistancesBlock();
                taxonNamesFound.addAll(DistancesNexusIO.parse(np, taxaBlock, distances, null));
                return distances;
            } else {
                final String blockName = np.skipBlock();
                System.err.println("Parse nexus block: " + blockName + ": not implemented;");
                return null;
            }
        } else // at end of file?
        {
            return null;
        }
    }
}
