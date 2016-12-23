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
import javafx.stage.Stage;
import splitstree5.core.algorithms.NeighborJoining;
import splitstree5.core.connectors.AConnectorNode;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.filters.TaxaFilter;
import splitstree5.core.topfilters.DistancesTopFilter;
import splitstree5.io.DistancesNexusIO;

import java.io.FileReader;
import java.io.StringWriter;

/**
 * try some ideas
 * Created by huson on 12/9/16.
 */
public class TryDistances extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

        final ADataNode<TaxaBlock> origTaxaNode = new ADataNode<>(new TaxaBlock("OrigTaxa"));
        final ADataNode<TaxaBlock> taxaNode = new ADataNode<>(new TaxaBlock("WorkingTaxa"));
        final TaxaFilter taxaFilter = new TaxaFilter(origTaxaNode, taxaNode);


        final ADataNode<DistancesBlock> origDistancesNode = new ADataNode<>(new DistancesBlock("OrigDistances"));
        final ADataNode<DistancesBlock> distancesNode = new ADataNode<>(new DistancesBlock("WorkingDistances"));
        final DistancesTopFilter distancesTopFilter = new DistancesTopFilter(origTaxaNode, taxaNode, origDistancesNode, distancesNode);

        DistancesNexusIO distancesNexusIO = new DistancesNexusIO(origDistancesNode.getDataBlock());
        distancesNexusIO.read(new FileReader("examples/distances.nex"), origTaxaNode.getDataBlock());
        origTaxaNode.getDataBlock().addTaxaByNames(distancesNexusIO.getTaxonNamesFound());


        final ADataNode<TreesBlock> treesNode = new ADataNode<>(new TreesBlock());

        final AConnectorNode<DistancesBlock, TreesBlock> dist2trees = new AConnectorNode<>(taxaNode.getDataBlock(), distancesNode, treesNode);
        dist2trees.setAlgorithm(new NeighborJoining());

        taxaFilter.forceRecompute();

        Thread.sleep(500);
        StringWriter w = new StringWriter();
        distancesNexusIO.write(w, taxaNode.getDataBlock());
        System.err.println(w.toString());

        /*
        final ADataNode<TreesBlock> filteredTreesNode = new ADataNode<>(new TreesBlock("FilteredTrees"));
        final TreesFilter treesFilter = new TreesFilter(taxaNode.getDataBlock(),treesNode, filteredTreesNode);

        final ADataNode<SplitsBlock> splitsNode = new ADataNode<>(new SplitsBlock());
        final ADataNode<SplitsBlock> filteredSplitsNode = new ADataNode<>(new SplitsBlock("Filtered Splits"));
        final SplitsFilter splitsFilter = new SplitsFilter(taxaNode.getDataBlock(), splitsNode, filteredSplitsNode);


        final AConnectorNode<TreesBlock, SplitsBlock> trees2splits = new AConnectorNode<>(taxaNode.getDataBlock(), treesNode, splitsNode);
        trees2splits.setAlgorithm(new ConsensusSplits());


        //distancesNode.setState(ANode.State.VALID);
        origTaxaNode.setState(ANode.State.VALID);


        Thread thread=new Thread(() -> {
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
        */
    }
}
