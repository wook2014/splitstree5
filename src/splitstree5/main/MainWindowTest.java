/*
 *  Copyright (C) 2018 Daniel H. Huson
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

package splitstree5.main;

import javafx.application.Application;
import javafx.stage.Stage;
import splitstree5.core.Document;
import splitstree5.core.algorithms.characters2distances.HammingDistances;
import splitstree5.core.algorithms.distances2splits.NeighborNet;
import splitstree5.core.algorithms.distances2trees.NeighborJoining;
import splitstree5.core.algorithms.views.SplitsNetworkAlgorithm;
import splitstree5.core.algorithms.views.TreeEmbedder;
import splitstree5.core.datablocks.*;
import splitstree5.core.workflow.Workflow;
import splitstree5.io.nexus.NexusFileParser;

public class MainWindowTest extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        final MainWindow mainWindow = new MainWindow();

        final Document document = mainWindow.getDocument();

        document.setFileName("test/nexus/characters-simple.nex");
        NexusFileParser.parse(document);

        final Workflow workflow = document.getWorkflow();

        if (workflow.getWorkingDataNode().getDataBlock() instanceof CharactersBlock) {
            final ADataNode<DistancesBlock> distances = workflow.createDataNode(new DistancesBlock());
            workflow.createConnector(workflow.getWorkingDataNode(), distances, new HammingDistances());

            final ADataNode<SplitsBlock> splits = workflow.createDataNode(new SplitsBlock());
            workflow.createConnector(distances, splits, new NeighborNet());
            final ADataNode<SplitsNetworkViewBlock> splitsView = workflow.createDataNode(new SplitsNetworkViewBlock());
            workflow.createConnector(splits, splitsView, new SplitsNetworkAlgorithm());

            final ADataNode<TreesBlock> trees = workflow.createDataNode(new TreesBlock());
            workflow.createConnector(distances, trees, new NeighborJoining());
            final ADataNode<TreeViewBlock> treeView = workflow.createDataNode(new TreeViewBlock());
            workflow.createConnector(trees, treeView, new TreeEmbedder());

        }

        mainWindow.show(primaryStage, 100, 100);
    }
}

