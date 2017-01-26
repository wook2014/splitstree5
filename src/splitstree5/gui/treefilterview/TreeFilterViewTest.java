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

package splitstree5.gui.treefilterview;


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


import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.Test;
import splitstree5.core.Document;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.filters.TreeFilter;
import splitstree5.gui.connectorview.ConnectorView;
import splitstree5.io.nexus.NexusFileParser;
import splitstree5.io.nexus.NexusFileWriter;

/**
 * test tree filter view
 * Created by huson on 1/26/17.
 */
public class TreeFilterViewTest extends Application {
    @Test
    public void test() throws Exception {
        init();
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Document document = new Document();
        document.setFileName("test/nexus/trees6-translate.nex");
        NexusFileParser.parse(document);

        {
            ConnectorView<TaxaBlock, TaxaBlock> connectorView = new ConnectorView<>(document, document.getDag().getTaxaFilter());
            connectorView.show();
        }

        ADataNode<TreesBlock> treesNode = document.getDag().getWorkingDataNode();
        System.err.println(NexusFileWriter.toString(document.getDag().getWorkingTaxaNode().getDataBlock(), treesNode.getDataBlock()));

        ADataNode<TreesBlock> filtered = document.getDag().createDataNode(new TreesBlock());

        AConnector connector = document.getDag().addConnector(new TreeFilter(document.getDag().getWorkingTaxaNode().getDataBlock(), treesNode, filtered));
        ConnectorView<TaxaBlock, TaxaBlock> connectorView = new ConnectorView<>(document, connector);
        connectorView.show();

        document.getDag().createReporter(filtered);
    }
}