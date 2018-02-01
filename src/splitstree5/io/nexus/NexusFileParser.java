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

package splitstree5.io.nexus;

import javafx.application.Platform;
import jloda.util.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.Document;
import splitstree5.core.datablocks.*;
import splitstree5.core.misc.Taxon;
import splitstree5.core.workflow.UpdateState;
import splitstree5.core.workflow.WorkflowUtils;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * parses a nexus file
 * Daniel Huson, 12/27/16.
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
        document.getWorkflow().clear();

        try (NexusStreamParser np = new NexusStreamParser(new FileReader(document.getFileName()))) {
            np.matchIgnoreCase("#nexus");

            if (np.peekMatchIgnoreCase("begin splitstree5;")) { // this is a complete file written by splitstree5
                System.err.println("Not implemented");
            } else { // this is a user input file

                final TaxaBlock topTaxaBlock = new TaxaBlock("OrigTaxa");

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

                document.getWorkflow().setupTopAndWorkingNodes(topTaxaBlock, topDataBlock);
                document.setupTaxonSelectionModel();

                // todo: for debugging:
                // new ReportConnector<>(document.getWorkflow().getWorkingTaxaNode().getDataBlock(), document.getWorkflow().getWorkingDataNode());

                if (Platform.isFxApplicationThread())
                    document.getWorkflow().getTopTaxaNode().setState(UpdateState.VALID);
                else {
                    TaxaBlock topTaxa = document.getWorkflow().getTopTaxaNode().getDataBlock();
                    TaxaBlock workingTaxa = document.getWorkflow().getWorkingTaxaNode().getDataBlock();
                    ADataBlock topData = document.getWorkflow().getTopDataNode().getDataBlock();
                    ADataBlock workingData = document.getWorkflow().getWorkingDataNode().getDataBlock();
                    try {
                        document.getWorkflow().getTaxaFilter().getAlgorithm().compute(new ProgressPercentage(), topTaxa, topTaxa, workingTaxa);
                        document.getWorkflow().getTopFilter().getAlgorithm().compute0(new ProgressPercentage(), topTaxa, topData, workingData);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                WorkflowUtils.print(document.getWorkflow().getTopTaxaNode(), document.getWorkflow().getTopDataNode());
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
            } else if (np.peekMatchIgnoreCase("begin st_analysis_result;")) {
                final AnalysisResultBlock analysisResult = new AnalysisResultBlock();
                taxonNamesFound.addAll(AnalysisResultIO.parse(np, analysisResult));
                return analysisResult;
            } else if (np.peekMatchIgnoreCase("begin characters;")) {
                final CharactersBlock characters = new CharactersBlock();
                taxonNamesFound.addAll(CharactersNexusIO.parse(np, taxaBlock, characters, null));
                return characters;
            } else if (np.peekMatchIgnoreCase("begin distances;")) {
                final DistancesBlock distances = new DistancesBlock();
                taxonNamesFound.addAll(DistancesNexusIO.parse(np, taxaBlock, distances, null));
                return distances;
            } else if (np.peekMatchIgnoreCase("begin trees;")) {
                final TreesBlock treesBlock = new TreesBlock();
                taxonNamesFound.addAll(TreesNexusIO.parse(np, taxaBlock, treesBlock, null));
                return treesBlock;
            } else if (np.peekMatchIgnoreCase("begin splits;")) {
                final SplitsBlock splitsBlock = new SplitsBlock();
                taxonNamesFound.addAll(SplitsNexusIO.parse(np, taxaBlock, splitsBlock, null));
                return splitsBlock;
            } else if (np.peekMatchIgnoreCase("begin network;")) {
                final NetworkBlock networkBlock = new NetworkBlock();
                taxonNamesFound.addAll(NetworkNexusIO.parse(np, taxaBlock, networkBlock));
                return networkBlock;
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
