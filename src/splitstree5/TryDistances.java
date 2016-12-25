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
import splitstree5.core.Document;
import splitstree5.core.algorithms.NeighborJoining;
import splitstree5.core.connectors.AConnectorNode;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.filters.TaxaFilter;
import splitstree5.core.topfilters.DistancesTopFilter;
import splitstree5.gui.TaxaFilterView;
import splitstree5.io.DistancesNexusIO;

import java.io.FileReader;

/**
 * try some ideas
 * Created by huson on 12/9/16.
 */
public class TryDistances extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Document document = new Document();

        final ADataNode<TaxaBlock> origTaxaNode = new ADataNode<>(document, new TaxaBlock("OrigTaxa"));
        final ADataNode<TaxaBlock> taxaNode = new ADataNode<>(document, new TaxaBlock("WorkingTaxa"));
        final TaxaFilter taxaFilter = new TaxaFilter(document, origTaxaNode, taxaNode);

        final ADataNode<DistancesBlock> origDistancesNode = new ADataNode<>(document, new DistancesBlock("OrigDistances"));
        final ADataNode<DistancesBlock> distancesNode = new ADataNode<>(document, new DistancesBlock("WorkingDistances"));
        final DistancesTopFilter distancesTopFilter = new DistancesTopFilter(document, origTaxaNode, taxaNode, origDistancesNode, distancesNode);

        DistancesNexusIO distancesNexusIO = new DistancesNexusIO(origDistancesNode.getDataBlock());
        distancesNexusIO.read(new FileReader("examples/distances.nex"), origTaxaNode.getDataBlock());
        origTaxaNode.getDataBlock().addTaxaByNames(distancesNexusIO.getTaxonNamesFound());


        final TaxaFilterView taxaFilterView = new TaxaFilterView(document, taxaFilter);
        taxaFilterView.show();

        {
            final TaxaFilterView taxaFilterView2 = new TaxaFilterView(document, taxaFilter);
            taxaFilterView2.show();
        }

        final ADataNode<TreesBlock> treesNode = new ADataNode<>(document, new TreesBlock());

        final AConnectorNode<DistancesBlock, TreesBlock> dist2trees = new AConnectorNode<>(document, taxaNode.getDataBlock(), distancesNode, treesNode);
        dist2trees.setAlgorithm(new NeighborJoining());

    }
}
