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

import javafx.stage.FileChooser;
import jloda.util.Basic;
import splitstree5.core.Document;
import splitstree5.dialogs.imports.ImportDialog;
import splitstree5.io.nexus.NexusFileParser;
import splitstree5.io.nexus.NexusFileWriter;
import splitstree5.menu.MenuController;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

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
            mainWindow.close();
        });

        mainWindow.getStage().setOnCloseRequest((e) -> {
            mainWindow.close();
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
                System.err.println("Save!");
                mainWindow.getDocument().setDirty(false);
                try (Writer w = new FileWriter(new File(file.getPath()))) {
                }
                // NexusFileWriter.write(mainWindow.getDocument());
            } catch (IOException e) {
                Basic.caught(e);
            }
        }
        return file != null;
    }
}
