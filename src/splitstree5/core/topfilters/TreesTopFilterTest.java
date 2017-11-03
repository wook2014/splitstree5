/*
 *  Copyright (C) 2017 Daniel H. Huson
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

package splitstree5.core.topfilters;

import javafx.application.Application;
import javafx.stage.Stage;
import splitstree5.core.Document;
import splitstree5.core.algorithms.ReportNode;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.gui.connectorview.ConnectorView;
import splitstree5.io.nexus.NexusFileParser;


/**
 * test
 * Created by huson on 1/12/17.
 */
public class TreesTopFilterTest extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        final Document document = new Document();

        document.setFileName("test/nexus/tree.nex");
        NexusFileParser.parse(document);

        {
            AConnector<TaxaBlock, TaxaBlock> taxaFilter = new AConnector<>(document.getDag().getTopTaxaNode().getDataBlock(), document.getDag().getTopTaxaNode(), document.getDag().getWorkingTaxaNode(), new splitstree5.core.algorithms.filters.TaxaFilter());
            ConnectorView<TaxaBlock, TaxaBlock> connectorView = new ConnectorView<>(document, taxaFilter);
            connectorView.show();

            new ReportNode<>(document.getDag().getWorkingTaxaNode().getDataBlock(), document.getDag().getWorkingTaxaNode());
        }

        {
            new TreesTopFilter(document.getDag().getTopTaxaNode(), document.getDag().getWorkingTaxaNode(), document.getDag().getTopDataNode(),
                    document.getDag().getWorkingDataNode());
        }
    }
}