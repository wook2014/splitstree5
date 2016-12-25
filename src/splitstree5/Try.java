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

package splitstree5;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import splitstree5.core.Document;
import splitstree5.core.algorithms.ConsensusSplits;
import splitstree5.core.algorithms.NeighborJoining;
import splitstree5.core.analysis.SimpleTaxaAnalysis;
import splitstree5.core.connectors.AConnectorNode;
import splitstree5.core.datablocks.*;
import splitstree5.core.filters.SplitsFilter;
import splitstree5.core.filters.TaxaFilter;
import splitstree5.core.filters.TreesFilter;
import splitstree5.core.misc.UpdateState;
import splitstree5.core.topfilters.DistancesTopFilter;

/**
 * try some ideas
 * Created by huson on 12/9/16.
 */
public class Try extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        final Document document = new Document();

        final ADataNode<TaxaBlock> origTaxaNode = new ADataNode<>(document, new TaxaBlock("OrigTaxa"));
        final ADataNode<TaxaBlock> taxaNode = new ADataNode<>(document, new TaxaBlock("WorkingTaxa"));
        final TaxaFilter taxaFilter = new TaxaFilter(document, origTaxaNode, taxaNode);

        final SimpleTaxaAnalysis simpleTaxaAnalysis = new SimpleTaxaAnalysis(document, taxaNode, new ADataNode<>(document, new AnalysisResultBlock()));

        // print out taxon analysis whenever changed. THis just demonstrates how an analysis is attached to the graph and can be listened for
        simpleTaxaAnalysis.getChild().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == UpdateState.VALID)
                System.err.println("Output: " + simpleTaxaAnalysis.getChild().getDataBlock().getName() + ": " + simpleTaxaAnalysis.getChild().getDataBlock().getInfo());
        });

        final ADataNode<DistancesBlock> origDistancesNode = new ADataNode<>(document, new DistancesBlock("OrigDistances"));
        final ADataNode<DistancesBlock> distancesNode = new ADataNode<>(document, new DistancesBlock("WorkingDistances"));
        final DistancesTopFilter distancesTopFilter = new DistancesTopFilter(document, origTaxaNode, taxaNode, origDistancesNode, distancesNode);

        final ADataNode<TreesBlock> treesNode = new ADataNode<>(document, new TreesBlock());

        final AConnectorNode<DistancesBlock, TreesBlock> dist2trees = new AConnectorNode<>(document, taxaNode.getDataBlock(), distancesNode, treesNode);
        dist2trees.setAlgorithm(new NeighborJoining());

        final ADataNode<TreesBlock> filteredTreesNode = new ADataNode<>(document, new TreesBlock("FilteredTrees"));
        final TreesFilter treesFilter = new TreesFilter(document, taxaNode.getDataBlock(), treesNode, filteredTreesNode);

        final ADataNode<SplitsBlock> splitsNode = new ADataNode<>(document, new SplitsBlock());
        final ADataNode<SplitsBlock> filteredSplitsNode = new ADataNode<>(document, new SplitsBlock("Filtered Splits"));
        final SplitsFilter splitsFilter = new SplitsFilter(document, taxaNode.getDataBlock(), splitsNode, filteredSplitsNode);


        final AConnectorNode<TreesBlock, SplitsBlock> trees2splits = new AConnectorNode<>(document, taxaNode.getDataBlock(), treesNode, splitsNode);
        trees2splits.setAlgorithm(new ConsensusSplits());


        //distancesNode.setState(ANode.State.VALID);
        origTaxaNode.setState(UpdateState.VALID);


        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(5000);
                Platform.runLater(() -> {
                    System.err.println("+++++++++ TRY - select taxa");
                    taxaFilter.forceRecompute();

                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }
}
