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

package splitstree5.dialogs.importer;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import jloda.fx.NotificationManager;
import jloda.fx.RecentFilesManager;
import jloda.util.Basic;
import splitstree5.core.Document;
import splitstree5.core.algorithms.characters2distances.HammingDistances;
import splitstree5.core.algorithms.distances2splits.NeighborNet;
import splitstree5.core.algorithms.filters.SplitsFilter;
import splitstree5.core.algorithms.filters.TreesFilter;
import splitstree5.core.algorithms.trees2splits.ConsensusNetwork;
import splitstree5.core.algorithms.trees2splits.SuperNetwork;
import splitstree5.core.algorithms.views.NetworkEmbedder;
import splitstree5.core.algorithms.views.SplitsNetworkAlgorithm;
import splitstree5.core.algorithms.views.TreeEmbedder;
import splitstree5.core.datablocks.*;
import splitstree5.core.workflow.DataNode;
import splitstree5.core.workflow.UpdateState;
import splitstree5.core.workflow.Workflow;
import splitstree5.main.MainWindow;

import java.util.Optional;

public class DataLoader {

    /**
     * load data into window.
     *
     * @param reload           if true, attempt to reload into current workflow, otherwise open new window
     * @param taxaBlock
     * @param dataBlock
     * @param parentMainWindow
     */
    public static void load(boolean reload, String fileName, TaxaBlock taxaBlock, DataBlock dataBlock, MainWindow parentMainWindow) {

        if (reload) {
            if (parentMainWindow.getWorkflow().canLoadData(dataBlock)) {
                parentMainWindow.getWorkflow().loadData(taxaBlock, dataBlock);
                Platform.runLater(() -> {
                    final Workflow workflow = parentMainWindow.getWorkflow();
                    workflow.getTopTaxaNode().setState(UpdateState.VALID);
                    if (!fileName.endsWith(".tmp"))
                        RecentFilesManager.getInstance().addRecentFile(fileName);
                    final String shortDescription = workflow.getTopTaxaNode() != null ? workflow.getTopDataNode().getShortDescription() : "null";
                    NotificationManager.showInformation("Opened file: " + Basic.getFileNameWithoutPath(fileName) + (shortDescription.length() > 0 ? "\nLoaded " + shortDescription : ""));
                });
                return;
            } else if (parentMainWindow.getDocument().getWorkflow().getWorkingDataNode() != null && parentMainWindow.getWorkflow().getWorkingDataNode().getDataBlock() != null) {
                Platform.runLater(() -> {
                    final String oldName = parentMainWindow.getWorkflow().getWorkingDataNode().getDataBlock().getName();
                    final String newName = dataBlock.getName();
                    NotificationManager.showError("Can't load data, type has changed from " + oldName + " to " + newName);
                });
                return;
            }
        }

        final MainWindow mainWindow;

        if (parentMainWindow.getDocument().getWorkflow().getWorkingDataNode() == null) {
            mainWindow = parentMainWindow;
        } else {
            mainWindow = new MainWindow();
        }
        final Document document = mainWindow.getDocument();

        document.setFileName(Basic.replaceFileSuffix(fileName, ".stree5"));

        final Workflow workflow = document.getWorkflow();

        if (dataBlock instanceof CharactersBlock) {
            workflow.setupTopAndWorkingNodes(taxaBlock, dataBlock);
            final DataNode<DistancesBlock> distances = workflow.createDataNode(new DistancesBlock());
            workflow.createConnector(workflow.getWorkingDataNode(), distances, new HammingDistances());
            final DataNode<SplitsBlock> splits = workflow.createDataNode(new SplitsBlock());
            workflow.createConnector(distances, splits, new NeighborNet());
            final DataNode<ViewBlock> viewNode = workflow.createDataNode(new ViewBlock(ViewBlock.Type.SplitsNetworkViewer));
            workflow.createConnector(splits, viewNode, new SplitsNetworkAlgorithm());

        } else if (dataBlock instanceof CharactersBlock) {
            workflow.setupTopAndWorkingNodes(taxaBlock, dataBlock);
            final DataNode<SplitsBlock> splits = workflow.createDataNode(new SplitsBlock());
            workflow.createConnector(workflow.getWorkingDataNode(), splits, new NeighborNet());
            final DataNode<ViewBlock> viewNode = workflow.createDataNode(new ViewBlock(ViewBlock.Type.SplitsNetworkViewer));
            workflow.createConnector(splits, viewNode, new SplitsNetworkAlgorithm());

        } else if (dataBlock instanceof DistancesBlock) {
            workflow.setupTopAndWorkingNodes(taxaBlock, dataBlock);
            final DataNode<SplitsBlock> splits = workflow.createDataNode(new SplitsBlock());
            workflow.createConnector(workflow.getWorkingDataNode(), splits, new NeighborNet());
            final DataNode<ViewBlock> viewNode = workflow.createDataNode(new ViewBlock(ViewBlock.Type.SplitsNetworkViewer));
            workflow.createConnector(splits, viewNode, new SplitsNetworkAlgorithm());
        } else if (dataBlock instanceof SplitsBlock) {
            workflow.setupTopAndWorkingNodes(taxaBlock, dataBlock);
            final DataNode<SplitsBlock> splits = workflow.createDataNode(new SplitsBlock());
            workflow.createConnector(workflow.getWorkingDataNode(), splits, new SplitsFilter());
            final DataNode<ViewBlock> viewNode = workflow.createDataNode(new ViewBlock(ViewBlock.Type.SplitsNetworkViewer));
            workflow.createConnector(splits, viewNode, new SplitsNetworkAlgorithm());
        } else if (dataBlock instanceof TreesBlock) {
            workflow.setupTopAndWorkingNodes(taxaBlock, dataBlock);
            if (dataBlock.size() == 1) { // only one tree, don't need a filter
                final DataNode<ViewBlock> viewNode = workflow.createDataNode(new ViewBlock(ViewBlock.Type.TreeViewer));
                workflow.createConnector(workflow.getWorkingDataNode(), viewNode, new TreeEmbedder());
            } else { // more than one tree, need a filter:
                final DataNode<TreesBlock> trees = workflow.createDataNode(new TreesBlock());

                workflow.createConnector(workflow.getWorkingDataNode(), trees, new TreesFilter());

                final DataNode<SplitsBlock> splits0 = workflow.createDataNode(new SplitsBlock());
                if (((TreesBlock) dataBlock).isPartial()) {
                    workflow.createConnector(trees, splits0, new SuperNetwork());
                } else {
                    workflow.createConnector(trees, splits0, new ConsensusNetwork());
                }
                final DataNode<SplitsBlock> splits = workflow.createDataNode(new SplitsBlock());
                workflow.createConnector(splits0, splits, new SplitsFilter());

                final DataNode<ViewBlock> viewNode = workflow.createDataNode(new ViewBlock(ViewBlock.Type.SplitsNetworkViewer));
                workflow.createConnector(splits, viewNode, new SplitsNetworkAlgorithm());
            }
        } else if (dataBlock instanceof NetworkBlock) {
            final DataNode<ViewBlock> viewNode = workflow.createDataNode(new ViewBlock(ViewBlock.Type.NetworkViewer));
            workflow.createConnector(workflow.getTopDataNode(), viewNode, new NetworkEmbedder());
        }

        if (taxaBlock.getTraitsBlock() != null)
            workflow.createTopTraitsAndWorkingTraitsNodes(taxaBlock.getTraitsBlock());

        document.setupTaxonSelectionModel();

        Platform.runLater(() -> {
            document.setDirty(true);
            if (mainWindow == parentMainWindow) // using existing document
                mainWindow.getStage().toFront();
            else // new document
                mainWindow.show(new Stage(), parentMainWindow.getStage().getX() + 50, parentMainWindow.getStage().getY() + 50);
            final String shortDescription = workflow.getTopTaxaNode() != null ? workflow.getTopDataNode().getShortDescription() : "null";
            NotificationManager.showInformation("Opened file: " + Basic.getFileNameWithoutPath(fileName) + (shortDescription.length() > 0 ? "\nLoaded " + shortDescription : ""));
        });

        Platform.runLater(() -> {
            document.getWorkflow().getTopTaxaNode().setState(UpdateState.VALID);
            if (!fileName.endsWith(".tmp"))
                RecentFilesManager.getInstance().addRecentFile(fileName);
        });
    }

    public static boolean askOkToOverwrite(String oldDataType, String newDataType) {
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Workflow Change - SplitsTree5");
        alert.setHeaderText("Input data type has changed from " + oldDataType + " to " + newDataType);
        alert.setContentText("Overwrite existing workflow to accommodate new datatype?");

        final ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        final ButtonType buttonTypeOverwrite = new ButtonType("Overwrite", ButtonBar.ButtonData.OK_DONE);

        alert.getButtonTypes().setAll(buttonTypeOverwrite, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == buttonTypeOverwrite;
    }
}
