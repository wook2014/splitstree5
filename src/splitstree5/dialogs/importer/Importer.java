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

package splitstree5.dialogs.importer;

import javafx.application.Platform;
import javafx.stage.Stage;
import jloda.fx.NotificationManager;
import jloda.fx.RecentFilesManager;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.Document;
import splitstree5.core.algorithms.characters2distances.HammingDistances;
import splitstree5.core.algorithms.distances2splits.NeighborNet;
import splitstree5.core.algorithms.filters.DimensionFilter;
import splitstree5.core.algorithms.filters.TreeSelector;
import splitstree5.core.algorithms.filters.TreesFilter;
import splitstree5.core.algorithms.trees2splits.ConsensusNetwork;
import splitstree5.core.algorithms.trees2splits.SuperNetwork;
import splitstree5.core.algorithms.views.SplitsNetworkAlgorithm;
import splitstree5.core.algorithms.views.TreeEmbedder;
import splitstree5.core.datablocks.*;
import splitstree5.core.workflow.DataNode;
import splitstree5.core.workflow.UpdateState;
import splitstree5.core.workflow.Workflow;
import splitstree5.io.imports.interfaces.*;
import splitstree5.io.imports.nexus.TraitsNexusIn;
import splitstree5.main.MainWindow;

import java.io.IOException;

/**
 * performs an import
 * Daniel Huson, 1.2018
 */
public class Importer {
    /**
     * import from a file
     *
     * @param parentMainWindow
     * @param importer
     * @param fileName
     */
    public static void apply(ProgressListener progress, MainWindow parentMainWindow, IImporter importer, String fileName) {
        if (importer != null) {
            try {
                final MainWindow mainWindow;

                if (parentMainWindow.getDocument().getWorkflow().getWorkingDataNode() == null) {
                    mainWindow = parentMainWindow;
                } else {
                    mainWindow = new MainWindow();
                }
                final Document document = mainWindow.getDocument();

                document.setFileName(Basic.replaceFileSuffix(fileName, ".stree5"));
                final Workflow workflow = document.getWorkflow();
                TaxaBlock taxaBlock = new TaxaBlock();

                if (importer instanceof IImportCharacters) {
                    final CharactersBlock dataBlock = new CharactersBlock();
                    ((IImportCharacters) importer).parse(progress, fileName, taxaBlock, dataBlock);
                    dataBlock.check();
                    workflow.setupTopAndWorkingNodes(taxaBlock, dataBlock);
                    final DataNode<DistancesBlock> distances = workflow.createDataNode(new DistancesBlock());
                    workflow.createConnector(workflow.getWorkingDataNode(), distances, new HammingDistances());
                    final DataNode<SplitsBlock> splits = workflow.createDataNode(new SplitsBlock());
                    workflow.createConnector(distances, splits, new NeighborNet());
                    final DataNode<SplitsNetworkViewBlock> splitsView = workflow.createDataNode(new SplitsNetworkViewBlock());
                    workflow.createConnector(splits, splitsView, new SplitsNetworkAlgorithm());
                } else if (importer instanceof IImportDistances) {
                    final DistancesBlock dataBlock = new DistancesBlock();
                    ((IImportDistances) importer).parse(progress, fileName, taxaBlock, dataBlock);
                    workflow.setupTopAndWorkingNodes(taxaBlock, dataBlock);
                    final DataNode<SplitsBlock> splits = workflow.createDataNode(new SplitsBlock());
                    workflow.createConnector(workflow.getWorkingDataNode(), splits, new NeighborNet());
                    final DataNode<SplitsNetworkViewBlock> splitsView = workflow.createDataNode(new SplitsNetworkViewBlock());
                    workflow.createConnector(splits, splitsView, new SplitsNetworkAlgorithm());

                } else if (importer instanceof IImportTrees) {
                    final TreesBlock dataBlock = new TreesBlock();
                    ((IImportTrees) importer).parse(progress, fileName, taxaBlock, dataBlock);
                    workflow.setupTopAndWorkingNodes(taxaBlock, dataBlock);
                    if (dataBlock.size() == 1) { // only one tree, don't need a filter
                        final DataNode<TreeViewBlock> treesView = workflow.createDataNode(new TreeViewBlock());
                        workflow.createConnector(workflow.getWorkingDataNode(), treesView, new TreeEmbedder());
                    } else { // more than one tree, need a filter:
                        final DataNode<TreesBlock> trees = workflow.createDataNode(new TreesBlock());

                        workflow.createConnector(workflow.getWorkingDataNode(), trees, new TreesFilter());

                        final DataNode<SplitsBlock> splits0 = workflow.createDataNode(new SplitsBlock());
                        if (dataBlock.isPartial()) {
                            workflow.createConnector(trees, splits0, new SuperNetwork());
                        } else {
                            workflow.createConnector(trees, splits0, new ConsensusNetwork());
                        }
                        final DataNode<SplitsBlock> splits = workflow.createDataNode(new SplitsBlock());
                        workflow.createConnector(splits0, splits, new DimensionFilter());

                        final DataNode<SplitsNetworkViewBlock> splitsNetworkViewBlockDataNode = workflow.createDataNode(new SplitsNetworkViewBlock());
                        workflow.createConnector(splits, splitsNetworkViewBlockDataNode, new SplitsNetworkAlgorithm());

                        final DataNode<TreesBlock> singleTree = workflow.createDataNode(new TreesBlock());
                        workflow.createConnector(trees, singleTree, new TreeSelector());

                        final DataNode<TreeViewBlock> treesView = workflow.createDataNode(new TreeViewBlock());
                        workflow.createConnector(singleTree, treesView, new TreeEmbedder());
                    }
                } else if (importer instanceof IImportSplits) {
                    final SplitsBlock dataBlock = new SplitsBlock();
                    ((IImportSplits) importer).parse(progress, fileName, taxaBlock, dataBlock);
                    workflow.setupTopAndWorkingNodes(taxaBlock, dataBlock);
                    final DataNode<SplitsBlock> splits = workflow.createDataNode(new SplitsBlock());
                    workflow.createConnector(workflow.getWorkingDataNode(), splits, new DimensionFilter());
                    final DataNode<SplitsNetworkViewBlock> splitsView = workflow.createDataNode(new SplitsNetworkViewBlock());
                    workflow.createConnector(splits, splitsView, new SplitsNetworkAlgorithm());
                }
                if (new TraitsNexusIn().isApplicable(fileName)) {
                    final TraitsBlock traitsBlock = new TraitsBlock();
                    new TraitsNexusIn().parse(progress, fileName, taxaBlock, traitsBlock);
                    workflow.createTopTraitsAndWorkingTraitsNodes(traitsBlock);
                }
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
            } catch (IOException ex) {
                NotificationManager.showError("Import failed: " + ex.getMessage());
            } catch (CanceledException ex) {
                NotificationManager.showWarning("Import CANCELED");
            }
        }
    }
}
