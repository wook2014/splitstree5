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
package splitstree5.core.algorithms.views;

import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.Test;
import splitstree5.core.Document;
import splitstree5.core.dag.DAG;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.TreeViewBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.gui.dagview.DAGView;
import splitstree5.io.nexus.NexusFileParser;

/**
 * test the tree view
 * Daniel Huson, 11.2017
 */
public class TreeEmbedderTest extends Application {
    @Test
    public void test() throws Exception {
        init();
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final Document document = new Document();
        document.setFileName("test/trees/beesUPGMA.nex");
        NexusFileParser.parse(document);

        DAG dag = document.getDag();

        if (dag.getWorkingDataNode().getDataBlock() instanceof TreesBlock) {
            final ADataNode<TreeViewBlock> treeViewBlock = dag.createDataNode(new TreeViewBlock());
            dag.createConnector(dag.getWorkingDataNode(), treeViewBlock, new TreeEmbedder());
        }

        final DAGView dagView = new DAGView(document);
        dagView.show();
    }
}