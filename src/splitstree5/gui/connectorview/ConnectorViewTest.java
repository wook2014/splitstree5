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
import org.junit.Test;
import splitstree5.core.Document;
import splitstree5.core.algorithms.ReportNode;
import splitstree5.core.algorithms.distances2splits.NeighborNet;
import splitstree5.core.algorithms.filters.SplitsFilter;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.dag.DAG;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.Taxon;

/**
 * test the connector view
 * Created by huson on 12/31/16.
 */
public class ConnectorViewTest extends Application {
    @Test
    public void test() throws Exception {
        init();
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final Document document = new Document();
        final TaxaBlock taxaBlock = new TaxaBlock();
        final DistancesBlock distancesBlock = new DistancesBlock();
        final SplitsBlock splitsBlock1 = new SplitsBlock();
        final SplitsBlock splitsBlock2 = new SplitsBlock();

        final DAG dag = document.getDag();
        dag.setupTopAndWorkingNodes(taxaBlock, distancesBlock);

        taxaBlock.getTaxa().addAll(new Taxon("First"), new Taxon("Second"), new Taxon("Third"), new Taxon("Fourth"), new Taxon("Fifth"), new Taxon("Sixth"));

        document.setupTaxonSelectionModel();

        {
            final AConnector<DistancesBlock, SplitsBlock> nnet = dag.createConnector(new ADataNode<>(distancesBlock), new ADataNode<>(splitsBlock1), new NeighborNet());
            ConnectorView<DistancesBlock, SplitsBlock> connectorView = new ConnectorView<>(document, nnet);
            connectorView.show();
        }

        {
            final AConnector<SplitsBlock, SplitsBlock> splitsFilter = dag.createConnector(new ADataNode<>(splitsBlock1), new ADataNode<>(splitsBlock2), new SplitsFilter());
            ConnectorView<SplitsBlock, SplitsBlock> connectorView = new ConnectorView<>(document, splitsFilter);
            connectorView.show();
        }

        {
            AConnector<TaxaBlock, TaxaBlock> taxaFilter = new AConnector<>(document.getDag().getTopTaxaNode().getDataBlock(), document.getDag().getTopTaxaNode(), document.getDag().getWorkingTaxaNode(), new splitstree5.core.algorithms.filters.TaxaFilter());
            ConnectorView<TaxaBlock, TaxaBlock> connectorView = new ConnectorView<>(document, taxaFilter);
            connectorView.show();

            new ReportNode<>(document.getDag().getWorkingTaxaNode().getDataBlock(), document.getDag().getWorkingTaxaNode());
        }

        {
            AConnector<TaxaBlock, TaxaBlock> taxaFilter = new AConnector<>(document.getDag().getTopTaxaNode().getDataBlock(), document.getDag().getTopTaxaNode(), document.getDag().getWorkingTaxaNode(), new splitstree5.core.algorithms.filters.TaxaFilter());
            ConnectorView<TaxaBlock, TaxaBlock> connectorView = new ConnectorView<>(document, taxaFilter);
            connectorView.show();
        }
    }
}