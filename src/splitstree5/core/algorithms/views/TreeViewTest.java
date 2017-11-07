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

package splitstree5.core.algorithms.views;

import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.Test;
import splitstree5.core.Document;
import splitstree5.core.algorithms.characters2distances.HammingDistances;
import splitstree5.core.algorithms.distances2trees.BioNJ;
import splitstree5.core.dag.DAG;
import splitstree5.core.datablocks.*;
import splitstree5.gui.dagview.DAGView;
import splitstree5.io.nexus.NexusFileParser;

/**
 * test the tree view
 * Daniel Huson, 11.2017
 */
public class TreeViewTest extends Application {
    @Test
    public void test() throws Exception {
        init();
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final Document document = new Document();
        //document.setFileName("test/nexus/trees49-notaxa.nex");
        document.setFileName("test/nexus/characters-simple.nex");
        NexusFileParser.parse(document);

        DAG dag = document.getDag();

        if (dag.getWorkingDataNode().getDataBlock() instanceof CharactersBlock) {
            final ADataNode<DistancesBlock> distances = dag.createDataNode(new DistancesBlock());
            dag.createConnector(dag.getWorkingDataNode(), distances, new HammingDistances());
            final ADataNode<TreesBlock> trees = dag.createDataNode(new TreesBlock());
            dag.createConnector(distances, trees, new BioNJ());
            final ADataNode<TreeViewBlock> treeView = dag.createDataNode(new TreeViewBlock());
            dag.createConnector(trees, treeView, new TreeDrawer());
        }

        final DAGView dagView = new DAGView(document);
        dagView.show();
    }
}