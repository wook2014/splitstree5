/*
 * DataLoader.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package splitstree5.dialogs.importer;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.window.MainWindowManager;
import jloda.fx.window.NotificationManager;
import jloda.util.Basic;
import splitstree5.core.Document;
import splitstree5.core.algorithms.characters2distances.HammingDistances;
import splitstree5.core.algorithms.distances2splits.NeighborNet;
import splitstree5.core.algorithms.filters.SplitsFilter;
import splitstree5.core.algorithms.filters.TreesFilter;
import splitstree5.core.algorithms.genomes2distances.Mash;
import splitstree5.core.algorithms.trees2splits.ConsensusNetwork;
import splitstree5.core.algorithms.trees2splits.SuperNetwork;
import splitstree5.core.algorithms.views.NetworkEmbedder;
import splitstree5.core.algorithms.views.OutlineAlgorithm;
import splitstree5.core.algorithms.views.SplitsNetworkAlgorithm;
import splitstree5.core.algorithms.views.TreeEmbedder;
import splitstree5.core.datablocks.*;
import splitstree5.core.workflow.DataNode;
import splitstree5.core.workflow.UpdateState;
import splitstree5.core.workflow.Workflow;
import splitstree5.main.MainWindow;

import java.util.Optional;

/**
 * loads data in document during import
 * Daniel Huson, 3.2018
 */
public class DataLoader {

    /**
     * loads data into document
     *
     * @param reload       if true, attempt to reload into current workflow, otherwise open new window
     * @param taxaBlock
     * @param dataBlock
     * @param parentWindow
     */
    public static void load(boolean reload, String fileName, TaxaBlock taxaBlock, DataBlock dataBlock, MainWindow parentWindow) {
        if (reload && parentWindow != null) {
            final Workflow workflow = parentWindow.getWorkflow();

            if (workflow.canLoadData(dataBlock)) {
                workflow.loadData(taxaBlock, dataBlock);
                Platform.runLater(() -> {
                    workflow.getTopTaxaNode().setState(UpdateState.VALID);
                    if (!fileName.endsWith(".tmp"))
                        RecentFilesManager.getInstance().insertRecentFile(fileName);
                    final String shortDescription = workflow.getTopTaxaNode() != null ? workflow.getTopDataNode().getShortDescription() : "null";
                    NotificationManager.showInformation("Opened file: " + Basic.getFileNameWithoutPath(fileName) + (shortDescription.length() > 0 ? "\nLoaded " + shortDescription : ""));
                });
                return;
            } else if (workflow.getWorkingDataNode() != null && workflow.getWorkingDataNode().getDataBlock() != null) {
                Platform.runLater(() -> {
                    final String oldName = workflow.getWorkingDataNode().getDataBlock().getName();
                    final String newName = dataBlock.getName();
                    NotificationManager.showError("Can't load data, type has changed from " + oldName + " to " + newName);
                });
                return;
            }
        }

        final MainWindow mainWindow;
        if (parentWindow != null && parentWindow.getWorkflow().getWorkingDataNode() == null) {
            mainWindow = parentWindow;
        } else {
            mainWindow = new MainWindow();
        }
        final Document document = mainWindow.getDocument();

        document.setFileName(Basic.replaceFileSuffix(fileName, ".stree5"));

        final Workflow workflow = mainWindow.getWorkflow();

        Platform.runLater(() -> {

            if (dataBlock instanceof CharactersBlock) {
                workflow.setupTopAndWorkingNodes(taxaBlock, dataBlock);
                final DataNode<DistancesBlock> distances = workflow.createDataNode(new DistancesBlock());
                workflow.createConnector(workflow.getWorkingDataNode(), distances, new HammingDistances());
                final DataNode<SplitsBlock> splits = workflow.createDataNode(new SplitsBlock());
                workflow.createConnector(distances, splits, new NeighborNet());
                final DataNode<ViewerBlock> viewNode = workflow.createDataNode(new ViewerBlock.SplitsNetworkViewerBlock());
                workflow.createConnector(splits, viewNode, new SplitsNetworkAlgorithm());

            } else if (dataBlock instanceof GenomesBlock) {
                workflow.setupTopAndWorkingNodes(taxaBlock, dataBlock);
                final DataNode<DistancesBlock> distances = workflow.createDataNode(new DistancesBlock());
                workflow.createConnector(workflow.getWorkingDataNode(), distances, new Mash());
                final DataNode<SplitsBlock> splits = workflow.createDataNode(new SplitsBlock());
                workflow.createConnector(distances, splits, new NeighborNet());
                final DataNode<ViewerBlock> viewNode = workflow.createDataNode(new ViewerBlock.SplitsNetworkViewerBlock());
                workflow.createConnector(splits, viewNode, new OutlineAlgorithm());
            } else if (dataBlock instanceof DistancesBlock) {
                workflow.setupTopAndWorkingNodes(taxaBlock, dataBlock);
                final DataNode<SplitsBlock> splits = workflow.createDataNode(new SplitsBlock());
                workflow.createConnector(workflow.getWorkingDataNode(), splits, new NeighborNet());
                final DataNode<ViewerBlock> viewNode = workflow.createDataNode(new ViewerBlock.SplitsNetworkViewerBlock());
                workflow.createConnector(splits, viewNode, new SplitsNetworkAlgorithm());
            } else if (dataBlock instanceof SplitsBlock) {
                workflow.setupTopAndWorkingNodes(taxaBlock, dataBlock);
                final DataNode<SplitsBlock> splits = workflow.createDataNode(new SplitsBlock());
                workflow.createConnector(workflow.getWorkingDataNode(), splits, new SplitsFilter());
                final DataNode<ViewerBlock> viewNode = workflow.createDataNode(new ViewerBlock.SplitsNetworkViewerBlock());
                workflow.createConnector(splits, viewNode, new SplitsNetworkAlgorithm());
            } else if (dataBlock instanceof TreesBlock) {
                workflow.setupTopAndWorkingNodes(taxaBlock, dataBlock);
                if (dataBlock.size() == 1) { // only one tree, don't need a filter
                    final DataNode<ViewerBlock> viewNode = workflow.createDataNode(new ViewerBlock.TreeViewerBlock());
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

                    final DataNode<ViewerBlock> viewNode = workflow.createDataNode(new ViewerBlock.SplitsNetworkViewerBlock());
                    workflow.createConnector(splits, viewNode, new SplitsNetworkAlgorithm());
                }
            } else if (dataBlock instanceof NetworkBlock) {
                final DataNode<ViewerBlock> viewNode = workflow.createDataNode(new ViewerBlock.SplitsNetworkViewerBlock());
                workflow.createConnector(workflow.getTopDataNode(), viewNode, new NetworkEmbedder());
            }

            if (taxaBlock.getTraitsBlock() != null)
                workflow.createTopTraitsAndWorkingTraitsNodes(taxaBlock.getTraitsBlock());

            document.setupTaxonSelectionModel();

            document.setDirty(true);
            if (mainWindow == parentWindow) // are using an existing window
                mainWindow.getStage().toFront();
            else // is new window
            {
                final Stage refWindow = (parentWindow != null ? parentWindow.getStage() : MainWindowManager.getInstance().getLastFocusedMainWindow().getStage());
                mainWindow.show(new Stage(), refWindow.getX() + 50, refWindow.getY() + 50, refWindow.getWidth(), refWindow.getHeight());
            }
            final String shortDescription = workflow.getTopTaxaNode() != null ? workflow.getTopDataNode().getShortDescription() : "null";
            NotificationManager.showInformation("Opened file: " + Basic.getFileNameWithoutPath(fileName) + (shortDescription.length() > 0 ? "\nLoaded " + shortDescription : ""));
        });

        Platform.runLater(() -> {
            workflow.getTopTaxaNode().setState(UpdateState.VALID);
            if (!fileName.endsWith(".tmp"))
                RecentFilesManager.getInstance().insertRecentFile(fileName);
        });
    }

    public static boolean askOkToOverwrite(MainWindow mainWindow, String oldDataType, String newDataType) {
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(mainWindow.getStage());

        alert.setResizable(true);

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
