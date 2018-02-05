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
import javafx.scene.control.Tab;
import javafx.stage.FileChooser;
import jloda.util.Basic;
import splitstree5.core.Document;
import splitstree5.core.algorithms.characters2network.MedianJoining;
import splitstree5.core.algorithms.distances2network.MinSpanningNetwork;
import splitstree5.core.algorithms.views.NetworkEmbedder;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.NetworkBlock;
import splitstree5.core.datablocks.NetworkViewBlock;
import splitstree5.core.workflow.DataNode;
import splitstree5.core.workflow.IHasDataNode;
import splitstree5.core.workflow.Workflow;
import splitstree5.dialogs.importer.FileOpener;
import splitstree5.dialogs.importer.ImportDialog;
import splitstree5.dialogs.importer.ImporterManager;
import splitstree5.io.nexus.WorkflowNexusOutput;
import splitstree5.menu.MenuController;

import java.io.File;
import java.io.IOException;

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

        controller.getOpenMenuItem().setOnAction((e) -> {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open input file");
            fileChooser.getExtensionFilters().addAll(ImporterManager.getInstance().getAllExtensionFilters());
            final File file = fileChooser.showOpenDialog(mainWindow.getStage());
            if (file != null && FileOpener.isOpenable(file.getPath())) {
                FileOpener.open(mainWindow, file.getPath()); // todo: add progress bar to window
            }
        });

        controller.getCloseMenuItem().setOnAction((e) -> {
            mainWindow.close();
        });

        mainWindow.getStage().setOnCloseRequest((e) -> {
            mainWindow.close();
            e.consume();
        });

        controller.getSaveMenuItem().setOnAction((e) -> {
            try {
                new WorkflowNexusOutput().save(mainWindow.getDocument());
                mainWindow.getDocument().setDirty(false);
            } catch (IOException ex) {
                Basic.caught(ex);
            }
        });
        controller.getSaveMenuItem().disableProperty().bind(document.dirtyProperty().not().or(document.nameProperty().isNotEmpty()).or(document.updatingProperty()));

        controller.getSaveAsMenuItem().setOnAction((e) -> {
            showSaveDialog(mainWindow);
        });

        controller.getQuitMenuItem().setOnAction((e) -> {
            while (MainWindowManager.getInstance().size() > 0) {
                final MainWindow window = MainWindowManager.getInstance().getMainWindow(MainWindowManager.getInstance().size() - 1);
                if (!window.close())
                    break;
            }
        });
    }

    /**
     * save dialog
     *
     * @param mainWindow
     * @return true if save
     */
    public static boolean showSaveDialog(MainWindow mainWindow) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save SplitsTree5 file");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("SplitsTree5 Files", "*.nxs", "*.nex", "*.nexus"));
        fileChooser.setInitialDirectory((new File(mainWindow.getDocument().getFileName()).getParentFile()));
        fileChooser.setInitialFileName((new File(mainWindow.getDocument().getFileName()).getName()));
        final File file = fileChooser.showSaveDialog(mainWindow.getStage());
        if (file != null) {
            try {
                mainWindow.getDocument().setFileName(file.getPath());
                new WorkflowNexusOutput().save(mainWindow.getDocument());
                mainWindow.getDocument().setDirty(false);
            } catch (IOException e) {
                Basic.caught(e);
            }
        }
        return file != null;
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

        if (selectedTab instanceof IHasDataNode) {
            System.err.println("Data: " + ((IHasDataNode) selectedTab).getDataNode());

            controller.getMedianJoiningMenuItem().setOnAction((e) -> {
                final DataNode dataNode = workflow.getAncestor(((IHasDataNode) selectedTab).getDataNode(), CharactersBlock.class);
                if (dataNode != null) {
                    try {
                        mainWindow.show(workflow.createView(dataNode, MedianJoining.class, NetworkBlock.class, NetworkEmbedder.class, NetworkViewBlock.class));
                    } catch (Exception ex) {
                        Basic.caught(ex);
                    }
                }
            });
            controller.getMedianJoiningMenuItem().disableProperty().bind(Bindings.createBooleanBinding(() -> workflow.getAncestor(((IHasDataNode) selectedTab).getDataNode(), CharactersBlock.class) == null, workflow.updatingProperty()));

            controller.getMinSpanningNetworkMenuItem().setOnAction((e) -> {
                final DataNode dataNode = workflow.getAncestor(((IHasDataNode) selectedTab).getDataNode(), DistancesBlock.class);
                if (dataNode != null) {
                    try {
                        mainWindow.show(workflow.createView(dataNode, MinSpanningNetwork.class, NetworkBlock.class, NetworkEmbedder.class, NetworkViewBlock.class));
                    } catch (Exception ex) {
                        Basic.caught(ex);
                    }
                }
            });
            controller.getMinSpanningNetworkMenuItem().disableProperty().bind(Bindings.createBooleanBinding(() -> workflow.getAncestor(((IHasDataNode) selectedTab).getDataNode(), DistancesBlock.class) == null, workflow.updatingProperty()));
        }
    }
}
