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

package splitstree5.gui.dagview;

import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.Test;
import splitstree5.core.Document;
import splitstree5.core.algorithms.filters.SplitsFilter;
import splitstree5.core.algorithms.filters.TreeFilter;
import splitstree5.core.algorithms.trees2splits.TreeSelector;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.dag.DAG;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.nexus.NexusFileParser;

/**
 * test the DAG view
 * Created by huson on 12/31/16.
 */
public class DAGViewTest extends Application {
    @Test
    public void test() throws Exception {
        init();
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final Document document = new Document();
        document.setFileName("test/nexus/trees49-notaxa.nex");
        //document.setFileName("test/nexus/characters-simple.nex");
        NexusFileParser.parse(document);

        DAG dag = document.getDag();

        if (dag.getWorkingDataNode().getDataBlock() instanceof TreesBlock) {
            final TreeSelector treeSelector = new TreeSelector();
            AConnector connector = dag.createConnector(dag.getWorkingDataNode(), new ADataNode<>(new SplitsBlock()), treeSelector);
            dag.createConnector(connector.getChild(), new ADataNode<>(new SplitsBlock()), new SplitsFilter());
        }


        if (dag.getWorkingDataNode().getDataBlock() instanceof TreesBlock) {
            dag.createConnector(dag.getWorkingDataNode(), new ADataNode<>(new TreesBlock()), new TreeFilter());
        }

        final DAGView dagView = new DAGView(document);
        dagView.show();
    }
}