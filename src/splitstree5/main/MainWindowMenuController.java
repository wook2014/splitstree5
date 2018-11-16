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

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.stage.FileChooser;
import jloda.fx.NotificationManager;
import jloda.fx.RecentFilesManager;
import jloda.util.Basic;
import jloda.util.Pair;
import jloda.util.ProgramProperties;
import splitstree5.core.Document;
import splitstree5.core.algorithms.characters2distances.*;
import splitstree5.core.algorithms.characters2network.MedianJoining;
import splitstree5.core.algorithms.characters2splits.ParsimonySplits;
import splitstree5.core.algorithms.distances2network.MinSpanningNetwork;
import splitstree5.core.algorithms.distances2network.PCoA2D;
import splitstree5.core.algorithms.distances2splits.BunemanTree;
import splitstree5.core.algorithms.distances2splits.NeighborNet;
import splitstree5.core.algorithms.distances2splits.SplitDecomposition;
import splitstree5.core.algorithms.distances2trees.BioNJ;
import splitstree5.core.algorithms.distances2trees.NeighborJoining;
import splitstree5.core.algorithms.distances2trees.UPGMA;
import splitstree5.core.algorithms.filters.SplitsFilter;
import splitstree5.core.algorithms.filters.TreeSelector;
import splitstree5.core.algorithms.trees2splits.ConsensusNetwork;
import splitstree5.core.algorithms.trees2splits.SuperNetwork;
import splitstree5.core.algorithms.trees2trees.ConsensusTree;
import splitstree5.core.algorithms.trees2trees.RootByMidpointAlgorithm;
import splitstree5.core.algorithms.trees2trees.RootByOutGroupAlgorithm;
import splitstree5.core.algorithms.views.NetworkEmbedder;
import splitstree5.core.algorithms.views.SplitsNetworkAlgorithm;
import splitstree5.core.algorithms.views.TreeEmbedder;
import splitstree5.core.datablocks.*;
import splitstree5.core.workflow.DataNode;
import splitstree5.core.workflow.Workflow;
import splitstree5.core.workflow.WorkflowDataLoader;
import splitstree5.core.workflow.WorkflowEditing;
import splitstree5.dialogs.importer.FileOpener;
import splitstree5.dialogs.importer.ImportDialog;
import splitstree5.dialogs.importer.ImportMultipleTreeFilesDialog;
import splitstree5.dialogs.importer.ImporterManager;
import splitstree5.dialogs.message.MessageWindow;
import splitstree5.gui.ViewerTab;
import splitstree5.gui.utils.CheckForUpdate;
import splitstree5.io.nexus.workflow.WorkflowNexusInput;
import splitstree5.io.nexus.workflow.WorkflowNexusOutput;
import splitstree5.menu.MenuController;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

/**
 * controller class for main window menus
 * Daniel Huson, 12.2017
 */
public class MainWindowMenuController {
    /**
     * setup the main menus
     */
    public static void setupMainMenus(MainWindow mainWindow) {
        final MenuController controller = mainWindow.getMenuController();
        final Document document = mainWindow.getDocument();

        controller.getNewMenuItem().setOnAction((e) -> {
            final MainWindow newMainWindow = new MainWindow();
            newMainWindow.show(null, mainWindow.getStage().getX() + 50, mainWindow.getStage().getY() + 50);
        });

        controller.getImportMenuItem().setOnAction((e) -> ImportDialog.show(mainWindow));
        controller.getImportMultipleTreeFilesMenuItem().setOnAction((e) -> ImportMultipleTreeFilesDialog.apply(mainWindow));

        controller.getOpenMenuItem().setOnAction((e) -> {
            final File previousDir = new File(ProgramProperties.get("InputDir", ""));
            final FileChooser fileChooser = new FileChooser();
            if (previousDir.isDirectory())
                fileChooser.setInitialDirectory(previousDir);
            fileChooser.setTitle("Open input file");
            fileChooser.getExtensionFilters().addAll(ImporterManager.getInstance().getAllExtensionFilters());
            final File selectedFile = fileChooser.showOpenDialog(mainWindow.getStage());
            if (selectedFile != null) {
                if (selectedFile.getParentFile().isDirectory())
                    ProgramProperties.put("InputDir", selectedFile.getParent());
                if (WorkflowNexusInput.isApplicable(selectedFile.getPath()))
                    WorkflowNexusInput.open(mainWindow, selectedFile.getPath());
                else
                    FileOpener.open(false, mainWindow, selectedFile.getPath(), null);
            }
        });

        controller.getCloseMenuItem().setOnAction((e) -> {
            mainWindow.clear(true, true);
        });

        mainWindow.getStage().setOnCloseRequest((e) -> {
            mainWindow.clear(true, true);
            e.consume();
        });

        controller.getInputEditorMenuItem().setOnAction((e) -> {
            if (false) {
                if (!mainWindow.getWorkflow().hasWorkingTaxonNodeForFXThreadProperty().get() || mainWindow.getInputTab() != null) {
                    mainWindow.showInputTab();
                } else {
                    final MainWindow newMainWindow = new MainWindow();
                    newMainWindow.show(null, mainWindow.getStage().getX() + 50, mainWindow.getStage().getY() + 50);
                    newMainWindow.showInputTab();
                }
            } else {
                if (!mainWindow.getWorkflow().hasWorkingTaxonNodeForFXThreadProperty().get() || mainWindow.getEditedInputTab() != null) {
                    mainWindow.showEditInputTab();
                } else {
                    final MainWindow newMainWindow = new MainWindow();
                    newMainWindow.show(null, mainWindow.getStage().getX() + 50, mainWindow.getStage().getY() + 50);
                    newMainWindow.showEditInputTab();
                }
            }
        });

        controller.getSaveMenuItem().setOnAction((e) -> {
            try {
                new WorkflowNexusOutput().save(mainWindow.getWorkflow(), new File(document.getFileName()), false);
                document.setDirty(false);
                document.setHasSplitsTree5File(true);
                if (!document.getFileName().endsWith(".tmp"))
                    RecentFilesManager.getInstance().insertRecentFile(document.getFileName());
                mainWindow.getDocument().setDirty(false);
            } catch (IOException ex) {
                Basic.caught(ex);
            }
        });
        controller.getSaveMenuItem().disableProperty().bind((document.hasSplitsTree5FileProperty().not()).or(document.dirtyProperty().not())
                .or(document.updatingProperty()).or(document.nameProperty().isEmpty()).or(mainWindow.getWorkflow().hasTopTaxaProperty().not()));

        controller.getSaveAsMenuItem().setOnAction((e) -> {
            showSaveDialog(mainWindow, false);
        });

        controller.getReplaceDataMenuItem().setOnAction((e) -> {
            final Workflow workflow = mainWindow.getWorkflow();
            if (workflow.hasTopTaxaProperty().get() && !askOkToOverwrite())
                return;

            final ImporterManager.DataType dataType = ImporterManager.getDataType(workflow.getTopDataNode().getDataBlock());
            if (dataType != ImporterManager.DataType.Unknown) {
                final File previousDir = new File(ProgramProperties.get("InputDir", ""));
                final FileChooser fileChooser = new FileChooser();
                if (previousDir.isDirectory())
                    fileChooser.setInitialDirectory(previousDir);
                fileChooser.setTitle("Load data file");
                fileChooser.getExtensionFilters().setAll(ImporterManager.getInstance().getExtensionFilters(dataType));

                final File selectedFile = fileChooser.showOpenDialog(mainWindow.getStage());
                if (selectedFile != null) {
                    if (ImporterManager.getInstance().getDataType(selectedFile.getPath()) != dataType) {
                        NotificationManager.showError("Can't replace data, selected file must contain data of type: " + dataType);
                        return;
                    }
                    final String inputFormat = ImporterManager.getInstance().getFileFormat(selectedFile.getPath());

                    try {
                        ProgramProperties.put("InputDir", selectedFile.getParent());
                        document.setFileName(Basic.replaceFileSuffix(document.getFileName(), ".splt5"));
                        if (mainWindow.getInputTab() != null)
                            mainWindow.getInputTab().loadFile(selectedFile.getPath());
                        if (mainWindow.getEditedInputTab() != null)
                            mainWindow.getEditedInputTab().loadFile(selectedFile.getPath());
                        WorkflowDataLoader.load(workflow, selectedFile.getPath(), inputFormat);
                        if (workflow.getTopTaxaNode().getChildren().size() > 0)
                            workflow.getTopTaxaNode().getChildren().get(0).forceRecompute();
                    } catch (Exception ex) {
                        NotificationManager.showError("Load data failed: " + ex.getMessage());
                    }
                }
            }
        });
        controller.getReplaceDataMenuItem().disableProperty().bind(mainWindow.getWorkflow().hasWorkingTaxonNodeForFXThreadProperty().not());

        controller.getExportWorkflowMenuItem().setOnAction((e) -> {
            showSaveDialog(mainWindow, true);
        });

        controller.getQuitMenuItem().setOnAction((e) -> {
            while (MainWindowManager.getInstance().size() > 0) {
                final MainWindow window = MainWindowManager.getInstance().getMainWindow(MainWindowManager.getInstance().size() - 1);
                if (!window.clear(true, true))
                    break;
            }
        });

        controller.getShowWorkflowMenuItem().setOnAction((e) -> mainWindow.showWorkflow());

        controller.getCommunityWebsiteMenuItem().setOnAction((e) -> {
            try {
                Basic.openWebPage(new URL("http://splitstree.informatik.uni-tuebingen.de"));
            } catch (IOException ex) {
                Basic.caught(ex);
            }
        });
    }

    public static boolean askOkToOverwrite() {
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Replacement - SplitsTree5");
        alert.setHeaderText("There are unsaved changes.");
        alert.setContentText("Overwrite existing data?");

        final ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        final ButtonType buttonTypeOverwrite = new ButtonType("Overwrite", ButtonBar.ButtonData.OK_DONE);

        alert.getButtonTypes().setAll(buttonTypeOverwrite, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == buttonTypeOverwrite;
    }

    /**
     * save dialog
     *
     * @param mainWindow
     * @return true if save
     */
    public static boolean showSaveDialog(MainWindow mainWindow, boolean asWorkflowOnly) {

        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(asWorkflowOnly ? "Export Workflow" : "Save SplitsTree5 file");

        final File previousDir = new File(ProgramProperties.get("SaveDir", ""));
        if (previousDir.isDirectory()) {
            fileChooser.setInitialDirectory(previousDir);
        } else
            fileChooser.setInitialDirectory((new File(mainWindow.getDocument().getFileName()).getParentFile()));

        if (!asWorkflowOnly) {
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("SplitsTree5 Files", "*.stree5", "*.nxs", "*.nex"));
            fileChooser.setInitialFileName(Basic.getFileNameWithoutPath(Basic.replaceFileSuffix(mainWindow.getDocument().getFileName(), ".stree5")));
        } else {
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("SplitsTree5 Workflow Files", "*.wflow5"));
            fileChooser.setInitialFileName(Basic.getFileNameWithoutPath(Basic.replaceFileSuffix(mainWindow.getDocument().getFileName(), ".wflow5")));
        }

        final File selectedFile = fileChooser.showSaveDialog(mainWindow.getStage());
        if (selectedFile != null) {
            if (selectedFile.getParentFile().isDirectory())
                ProgramProperties.put("SaveDir", selectedFile.getParent());
            try {
                final Document document = mainWindow.getDocument();
                new WorkflowNexusOutput().save(mainWindow.getWorkflow(), selectedFile, asWorkflowOnly);
                if (!asWorkflowOnly) {
                    document.setFileName(selectedFile.getPath());
                    mainWindow.getDocument().setDirty(false);
                    document.setHasSplitsTree5File(true);
                }
                if (!document.getFileName().endsWith(".tmp"))
                    RecentFilesManager.getInstance().insertRecentFile(document.getFileName());
            } catch (IOException e) {
                Basic.caught(e);
            }
        }
        return selectedFile != null;
    }

    /**
     * this is where we setup all the construction menu item that add nodes to the workflow
     *
     * @param mainWindow
     * @param selectedTab
     * @param controller
     */
    public static void updateConstructionMenuItems(MainWindow mainWindow, final Tab selectedTab, MenuController controller) {
        final Workflow workflow = mainWindow.getDocument().getWorkflow();

        controller.getFilterTaxaMenuItem().setOnAction((e) -> mainWindow.showAlgorithmView(workflow.getTaxaFilter()));
        controller.getFilterTaxaMenuItem().disableProperty().bind(workflow.hasWorkingTaxonNodeForFXThreadProperty().not());

        final DataNode viewDataNode = (selectedTab instanceof ViewerTab ? ((ViewerTab) selectedTab).getDataNode() : null);

        final BooleanBinding disableCharactersMethods = Bindings.createBooleanBinding(() -> workflow.getAncestorForClass(viewDataNode, CharactersBlock.class) == null, workflow.updatingProperty());
        final BooleanBinding disableSplitsMethods = Bindings.createBooleanBinding(() -> workflow.getAncestorForClass(viewDataNode, SplitsBlock.class) == null, workflow.updatingProperty());
        final BooleanBinding disableTreesMethods = Bindings.createBooleanBinding(() -> workflow.getAncestorForClass(viewDataNode, TreesBlock.class) == null, workflow.updatingProperty());
        final BooleanBinding disableDistancesMethods = Bindings.createBooleanBinding(() -> workflow.getAncestorForClass(viewDataNode, DistancesBlock.class) == null, workflow.updatingProperty());
        final BooleanBinding disableNetworkMethods = Bindings.createBooleanBinding(() -> workflow.getAncestorForClass(viewDataNode, NetworkBlock.class) == null, workflow.updatingProperty());

        // filters:
        controller.getFilterCharactersMenuItem().setOnAction((e) -> {
            final DataNode dataNode = workflow.getAncestorForClass(viewDataNode, CharactersBlock.class);
            mainWindow.showAlgorithmView(WorkflowEditing.findOrInsertFilter(workflow, dataNode));
        });
        controller.getFilterCharactersMenuItem().disableProperty().bind(disableCharactersMethods);

        controller.getFilterSplitsMenuItem().setOnAction((e) -> {
            final DataNode dataNode = workflow.getAncestorForClass(viewDataNode, SplitsBlock.class);
            mainWindow.showAlgorithmView(WorkflowEditing.findOrInsertFilter(workflow, dataNode));
        });
        controller.getFilterSplitsMenuItem().disableProperty().bind(disableSplitsMethods);

        controller.getFilterTreesMenuItem().setOnAction((e) -> {
            final DataNode dataNode = workflow.getAncestorForClass(viewDataNode, TreesBlock.class);
            mainWindow.showAlgorithmView(WorkflowEditing.findOrInsertFilter(workflow, dataNode));
        });
        controller.getFilterTreesMenuItem().disableProperty().bind(disableTreesMethods);

        controller.getTraitsMenuItem().setOnAction((e) -> {
            if (workflow.getWorkingTraitsNode() != null) {
                mainWindow.showDataView(workflow.getWorkingTraitsNode());
            }
        });

        controller.getTraitsMenuItem().disableProperty().bind(workflow.hasWorkingTraitsNodeForFXThreadProperty().not());

        // distances:

        controller.getUncorrectedPMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, CharactersBlock.class, Uncorrected_P.class, DistancesBlock.class)));
        controller.getUncorrectedPMenuItem().disableProperty().bind(disableCharactersMethods);

        controller.getLogDetMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, CharactersBlock.class, LogDet.class, DistancesBlock.class)));
        controller.getLogDetMenuItem().disableProperty().bind(disableCharactersMethods);

        controller.getHky85MenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, CharactersBlock.class, HKY85.class, DistancesBlock.class)));
        controller.getHky85MenuItem().disableProperty().bind(disableCharactersMethods);

        controller.getJukesCantorMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, CharactersBlock.class, JukesCantor.class, DistancesBlock.class)));
        controller.getJukesCantorMenuItem().disableProperty().bind(disableCharactersMethods);

        controller.getK2pMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, CharactersBlock.class, K2P.class, DistancesBlock.class)));
        controller.getK2pMenuItem().disableProperty().bind(disableCharactersMethods);

        controller.getK3stMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, CharactersBlock.class, K3ST.class, DistancesBlock.class)));
        controller.getK3stMenuItem().disableProperty().bind(disableCharactersMethods);

        controller.getF81MenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, CharactersBlock.class, F81.class, DistancesBlock.class)));
        controller.getF81MenuItem().disableProperty().bind(disableCharactersMethods);

        controller.getF84MenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, CharactersBlock.class, F84.class, DistancesBlock.class)));
        controller.getF84MenuItem().disableProperty().bind(disableCharactersMethods);

        controller.getProteinMLDistanceMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, CharactersBlock.class, ProteinMLdist.class, DistancesBlock.class)));
        controller.getProteinMLDistanceMenuItem().disableProperty().bind(disableCharactersMethods);

        controller.getGeneContentDistanceMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, CharactersBlock.class, GeneContentDistance.class, DistancesBlock.class)));
        controller.getGeneContentDistanceMenuItem().disableProperty().bind(disableCharactersMethods);


        // trees:

        controller.getBioNJMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, DistancesBlock.class,
                BioNJ.class, TreesBlock.class, TreeEmbedder.class, ViewerBlock.TreeViewerBlock.class)));
        controller.getBioNJMenuItem().disableProperty().bind(disableDistancesMethods);

        controller.getNjMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, DistancesBlock.class,
                NeighborJoining.class, TreesBlock.class, TreeEmbedder.class, ViewerBlock.TreeViewerBlock.class)));
        controller.getNjMenuItem().disableProperty().bind(disableDistancesMethods);

        controller.getUpgmaMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, DistancesBlock.class,
                UPGMA.class, TreesBlock.class, TreeEmbedder.class, ViewerBlock.TreeViewerBlock.class)));
        controller.getUpgmaMenuItem().disableProperty().bind(disableDistancesMethods);

        controller.getBunemanTreeMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, DistancesBlock.class,
                BunemanTree.class, SplitsBlock.class, SplitsNetworkAlgorithm.class, ViewerBlock.SplitsNetworkViewerBlock.class)));
        controller.getBunemanTreeMenuItem().disableProperty().bind(disableDistancesMethods);

        controller.getSelectTreeMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, TreesBlock.class,
                TreeSelector.class, TreesBlock.class, TreeEmbedder.class, ViewerBlock.TreeViewerBlock.class)));
        controller.getSelectTreeMenuItem().disableProperty().bind(disableTreesMethods);

        controller.getConsensusTreeMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, TreesBlock.class,
                ConsensusTree.class, TreesBlock.class, TreeEmbedder.class, ViewerBlock.TreeViewerBlock.class)));
        controller.getConsensusTreeMenuItem().disableProperty().bind(disableTreesMethods);

        controller.getTreeViewMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, TreesBlock.class, TreeEmbedder.class, ViewerBlock.TreeViewerBlock.class)));
        controller.getConsensusTreeMenuItem().disableProperty().bind(disableTreesMethods);

        controller.getRootByOutgroupMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrInsertTreeRootAlgorithm(workflow, viewDataNode, new RootByOutGroupAlgorithm())));
        controller.getRootByOutgroupMenuItem().disableProperty().bind(disableTreesMethods);

        controller.getRootByMidpointMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrInsertTreeRootAlgorithm(workflow, viewDataNode, new RootByMidpointAlgorithm())));
        controller.getRootByMidpointMenuItem().disableProperty().bind(disableTreesMethods);

        // networks:

        controller.getNeighborNetMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, DistancesBlock.class,
                NeighborNet.class, SplitsBlock.class, SplitsNetworkAlgorithm.class, ViewerBlock.SplitsNetworkViewerBlock.class)));
        controller.getNeighborNetMenuItem().disableProperty().bind(disableDistancesMethods);

        controller.getSplitDecompositionMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, DistancesBlock.class,

                SplitDecomposition.class, SplitsBlock.class, SplitsNetworkAlgorithm.class, ViewerBlock.SplitsNetworkViewerBlock.class)));
        controller.getSplitDecompositionMenuItem().disableProperty().bind(disableDistancesMethods);

        controller.getParsimonySplitsMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, CharactersBlock.class,
                ParsimonySplits.class, SplitsBlock.class, SplitsNetworkAlgorithm.class, ViewerBlock.SplitsNetworkViewerBlock.class)));
        controller.getParsimonySplitsMenuItem().disableProperty().bind(disableCharactersMethods);

        controller.getConsensusNetworkMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, TreesBlock.class,
                new Pair<>(ConsensusNetwork.class, SplitsBlock.class), new Pair<>(SplitsFilter.class, SplitsBlock.class), new Pair<>(SplitsNetworkAlgorithm.class, ViewerBlock.SplitsNetworkViewerBlock.class))));
        controller.getConsensusNetworkMenuItem().disableProperty().bind(disableTreesMethods);

        controller.getFilteredSuperNetworkMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, TreesBlock.class,
                new Pair<>(SuperNetwork.class, SplitsBlock.class), new Pair<>(SplitsFilter.class, SplitsBlock.class), new Pair<>(SplitsNetworkAlgorithm.class, ViewerBlock.SplitsNetworkViewerBlock.class))));
        controller.getFilteredSuperNetworkMenuItem().disableProperty().bind(disableTreesMethods);

        controller.getMedianJoiningMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, CharactersBlock.class,
                MedianJoining.class, NetworkBlock.class, NetworkEmbedder.class, ViewerBlock.NetworkViewerBlock.class)));
        controller.getMedianJoiningMenuItem().disableProperty().bind(disableCharactersMethods);

        controller.getMinSpanningNetworkMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, DistancesBlock.class,
                MinSpanningNetwork.class, NetworkBlock.class, NetworkEmbedder.class, ViewerBlock.NetworkViewerBlock.class)));
        controller.getMinSpanningNetworkMenuItem().disableProperty().bind(disableDistancesMethods);

        //todo PCoA
        controller.getPcoaMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, DistancesBlock.class,
                PCoA2D.class, NetworkBlock.class, NetworkEmbedder.class, ViewerBlock.NetworkViewerBlock.class)));
        controller.getPcoaMenuItem().disableProperty().bind(disableDistancesMethods);

        controller.getSplitsNetworkViewMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, SplitsBlock.class,
                SplitsNetworkAlgorithm.class, ViewerBlock.SplitsNetworkViewerBlock.class)));
        controller.getSplitsNetworkViewMenuItem().disableProperty().bind(disableSplitsMethods);

        controller.getHaplotypeNetworkViewMenuItem().setOnAction((e) -> mainWindow.show(WorkflowEditing.findOrCreatePath(workflow, viewDataNode, NetworkBlock.class,
                NetworkEmbedder.class, ViewerBlock.NetworkViewerBlock.class)));
        controller.getHaplotypeNetworkViewMenuItem().disableProperty().bind(disableNetworkMethods);

        controller.getShowMessageWindowMenuItem().setOnAction((e) -> MessageWindow.getInstance().setVisible(true));

        controller.getCheckForUpdatesMenuItem().setOnAction((e) -> CheckForUpdate.apply());
        controller.getCheckForUpdatesMenuItem().disableProperty().bind(Bindings.isNotEmpty(workflow.dataNodes()).or(MainWindowManager.getInstance().sizeProperty().greaterThan(1)));
    }
}
