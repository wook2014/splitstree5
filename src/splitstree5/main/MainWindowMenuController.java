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

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import jloda.util.Basic;
import splitstree5.core.Document;
import splitstree5.core.project.ProjectManager;
import splitstree5.dialogs.imports.ImportDialog;
import splitstree5.io.nexus.NexusFileParser;
import splitstree5.io.nexus.NexusFileWriter;
import splitstree5.menu.MenuController;

import java.io.File;
import java.io.IOException;
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
            try {
                final MainWindow newMainWindow = new MainWindow();
                newMainWindow.show(null, mainWindow.getStage().getX() + 50, mainWindow.getStage().getY() + 50);
            } catch (IOException ex) {
                Basic.caught(ex);
            }
        });

        controller.getImportMenuItem().setOnAction((e) -> ImportDialog.show(mainWindow.getStage(), mainWindow.getDocument()));

        controller.getOpenMenuItem().setOnAction((e) -> {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open SplitsTree5 file");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("SplitsTree5 Files", "*.nxs", "*.nex", "*.nexus"));
            final File file = fileChooser.showOpenDialog(mainWindow.getStage());
            if (file != null) {
                final Document useDocument;
                try {
                    if (document.getWorkflow().getNumberOfDataNodes() == 0 && !document.getWorkflow().updatingProperty().get()) {
                        useDocument = document;
                    } else {
                        final MainWindow newMainWindow = new MainWindow();
                        newMainWindow.show(null, mainWindow.getStage().getX() + 50, mainWindow.getStage().getY() + 50);
                        useDocument = newMainWindow.getDocument();

                    }
                    useDocument.setFileName(file.getPath());
                    NexusFileParser.parse(useDocument);
                    useDocument.setDirty(false);
                } catch (IOException ex) {
                    Basic.caught(ex);
                }
            }
        });

        controller.getCloseMenuItem().setOnAction((e) -> {
            if (!saveBeforeClose(mainWindow))
                e.consume();
        });

        mainWindow.getStage().setOnCloseRequest((e) -> {
            if (!saveBeforeClose(mainWindow))
                e.consume();
        });

        controller.getSaveMenuItem().setOnAction((e) -> {
            try {
                NexusFileWriter.write(mainWindow.getDocument());
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
            while (ProjectManager.getInstance().size() > 0) {
                final MainWindow window = ProjectManager.getInstance().getMainWindow(ProjectManager.getInstance().size() - 1);
                if (!saveBeforeClose(window))
                    return;
                window.setAllowClose();
                window.close();
            }
        });
    }

    /**
     * ask whether to save before closing
     *
     * @param mainWindow
     * @return false, if canceled
     */
    public static boolean saveBeforeClose(MainWindow mainWindow) {
        final Document document = mainWindow.getDocument();
        if (!document.isDirty()) {
            mainWindow.close();
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(mainWindow.getStage());
            alert.setTitle("Save File Dialog");
            alert.setHeaderText("This document has unsaved changes");
            alert.setContentText("Save changes before closing?");
            ButtonType buttonTypeYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType buttonTypeNo = new ButtonType("No", ButtonBar.ButtonData.NO);
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == buttonTypeYes) {
                    return showSaveDialog(mainWindow);
                } else if (result.get() == buttonTypeNo) {
                    return true;
                }
            }
            return false; // canceled

        }
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
                NexusFileWriter.write(mainWindow.getDocument());
            } catch (IOException e) {
                Basic.caught(e);
            }
        }
        return file != null;
    }
}
