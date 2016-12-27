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
import splitstree5.core.algorithms.Report;
import splitstree5.core.datablocks.*;
import splitstree5.core.filters.TaxaFilter;
import splitstree5.core.misc.Taxon;
import splitstree5.core.misc.UpdateState;
import splitstree5.core.topfilters.DistancesTopFilter;
import splitstree5.core.topfilters.SplitsTopFilter;
import splitstree5.core.topfilters.TreesTopFilter;
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

        try (NexusStreamParser np = new NexusStreamParser(new FileReader(document.getFileName()))) {
            np.matchIgnoreCase("#nexus");

            if (np.peekMatchIgnoreCase("begin splitstree5;")) { // this is a complete file written by splitstree5
                System.err.println("Not implemented");
            } else { // this is a user input file

                final TaxaBlock topTaxaBlock = new TaxaBlock("TopTaxa");
                document.setTopTaxaNode(new ADataNode<>(document, topTaxaBlock));

                boolean needToDetectTaxa = true;

                if (np.peekMatchIgnoreCase("begin taxa;")) {
                    final TaxaNexusIO io = new TaxaNexusIO(topTaxaBlock);
                    io.parse(np, null);
                    needToDetectTaxa = (topTaxaBlock.getTaxa().size() == 0);
                }
                final ArrayList<String> namesFound = new ArrayList<>();
                final ADataBlock originalDataBlock = parseBlock(np, topTaxaBlock, namesFound);
                if (needToDetectTaxa) {
                    if (namesFound.size() == 0)
                        throw new IOException("Couldn't detect taxon names in input file");
                    for (String name : namesFound) {
                        topTaxaBlock.add(new Taxon(name));
                    }
                }
                document.getTopTaxaNode().setState(UpdateState.VALID);

                final ADataNode topDataNode = new ADataNode<>(document, originalDataBlock);
                document.setTopDataNode(topDataNode);
                document.getTopDataNode().setState(UpdateState.VALID);

                final ADataNode workingDataNode = new ADataNode<>(document, originalDataBlock.newInstance());

                ADataNode<TaxaBlock> workingTaxaNode = new ADataNode<>(document, new TaxaBlock("Taxa"));
                TaxaFilter taxaFilter = new TaxaFilter(document, document.getTopTaxaNode(), workingTaxaNode);

                // todo: just for debugging, report on changes of working taxa and changes of distances:
                new Report<>(document, workingTaxaNode.getDataBlock(), workingTaxaNode);
                new Report<>(document, workingTaxaNode.getDataBlock(), workingDataNode);

                // todo: just for debugging, open taxa filter view here:
                {
                    final TaxaFilterView taxaFilterView = new TaxaFilterView(document, taxaFilter);
                    taxaFilterView.show();
                }

                if (document.getTopDataNode().getDataBlock() instanceof DistancesBlock) {
                    new DistancesTopFilter(document, document.getTopTaxaNode(), workingTaxaNode, document.getTopDataNode(), workingDataNode);
                } else if (document.getTopDataNode().getDataBlock() instanceof SplitsBlock) {
                    new SplitsTopFilter(document, document.getTopTaxaNode(), workingTaxaNode, document.getTopDataNode(), workingDataNode);
                } else if (document.getTopDataNode().getDataBlock() instanceof TreesBlock) {
                    new TreesTopFilter(document, document.getTopTaxaNode(), workingTaxaNode, document.getTopDataNode(), workingDataNode);
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
                TaxaNexusIO taxaBlockIO = new TaxaNexusIO(taxaBlock2);
                taxaBlockIO.parse(np, null);
                return taxaBlock2;
            } else if (np.peekMatchIgnoreCase("begin distances;")) {
                final DistancesBlock distances = new DistancesBlock();
                DistancesNexusIO distancesIO = new DistancesNexusIO(distances);
                distancesIO.parse(np, taxaBlock);
                taxonNamesFound.addAll(distancesIO.getTaxonNamesFound());
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
