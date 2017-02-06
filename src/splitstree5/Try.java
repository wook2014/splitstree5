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
import splitstree5.core.algorithms.distances2splits.NeighborNet;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.dag.DAGUtils;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.gui.connectorview.ConnectorView;
import splitstree5.io.nexus.NexusFileParser;

/**
 * try some ideas
 * Created by huson on 12/9/16.
 */
public class Try extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        final Document document = new Document();
        //document.setFileName("test//nexus//distances7-taxa.nex");
        document.setFileName("examples/distances2.nex");

        NexusFileParser.parse(document);

        {
            AConnector<TaxaBlock, TaxaBlock> taxaFilter = document.getDag().getTaxaFilter();
            ConnectorView<TaxaBlock, TaxaBlock> view = new ConnectorView<>(document, taxaFilter);
            view.show();
        }

        if (document.getDag().getWorkingDataNode().getDataBlock() instanceof DistancesBlock) {
            AConnector<DistancesBlock, SplitsBlock> connector = document.getDag().createConnector(document.getDag().getWorkingDataNode(), new ADataNode<>(new SplitsBlock()), new NeighborNet());
            ConnectorView<DistancesBlock, SplitsBlock> view = new ConnectorView<>(document, connector);
            view.show();
            document.getDag().createReporter(connector.getChild());

        }

        DAGUtils.print(document.getDag().getTopTaxaNode(), document.getDag().getTopDataNode());
    }
}
