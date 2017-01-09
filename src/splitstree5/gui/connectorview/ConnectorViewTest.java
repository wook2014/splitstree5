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

package splitstree5.gui.connectorview;

import javafx.application.Application;
import javafx.stage.Stage;
import splitstree5.core.Document;
import splitstree5.core.algorithms.Report;
import splitstree5.core.algorithms.distances2splits.NeighborNet;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.filters.SplitsFilter;
import splitstree5.core.filters.TaxaFilter;
import splitstree5.core.misc.Taxon;

/**
 * test the connector view
 * Created by huson on 12/31/16.
 */
public class ConnectorViewTest extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        final Document document = new Document();
        final TaxaBlock taxaBlock = new TaxaBlock();
        final DistancesBlock distancesBlock = new DistancesBlock();
        final SplitsBlock splitsBlock1 = new SplitsBlock();
        final SplitsBlock splitsBlock2 = new SplitsBlock();

        document.getDag().setupTopAndWorkingNodes(taxaBlock, distancesBlock);

        taxaBlock.getTaxa().addAll(new Taxon("First"), new Taxon("Second"), new Taxon("Third"), new Taxon("Fourth"), new Taxon("Fifth"), new Taxon("Sixth"));

        document.setupTaxonSelectionModel();

        {
            final AConnector<DistancesBlock, SplitsBlock> nnet = new AConnector<>(taxaBlock, new ADataNode<>(distancesBlock), new ADataNode<>(splitsBlock1), new NeighborNet());
            ConnectorView<DistancesBlock, SplitsBlock> connectorView = new ConnectorView<>(document, nnet);
            connectorView.show();
        }

        {
            final SplitsFilter splitsFilter = new SplitsFilter(taxaBlock, new ADataNode<>(splitsBlock1), new ADataNode<>(splitsBlock2));
            ConnectorView<SplitsBlock, SplitsBlock> connectorView = new ConnectorView<>(document, splitsFilter);
            connectorView.show();
        }

        {
            TaxaFilter taxaFilter = new TaxaFilter(document.getDag().getTopTaxaNode(), document.getDag().getWorkingTaxaNode());
            ConnectorView<TaxaBlock, TaxaBlock> connectorView = new ConnectorView<>(document, taxaFilter);
            connectorView.show();

            new Report<>(document.getDag().getWorkingTaxaNode().getDataBlock(), document.getDag().getWorkingTaxaNode());
        }
    }
}