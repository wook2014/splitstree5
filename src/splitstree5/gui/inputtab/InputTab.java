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

package splitstree5.gui.inputtab;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import jloda.fx.NotificationManager;
import jloda.util.Basic;
import splitstree5.dialogs.importer.FileOpener;
import splitstree5.gui.texttab.TextViewTab;
import splitstree5.io.imports.IOExceptionWithLineNumber;
import splitstree5.main.MainWindow;
import splitstree5.menu.MenuController;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.function.Consumer;

/**
 * tab for entering data
 * Daniel Huson, 2.2018
 */
public class InputTab extends TextViewTab {
    private File tmpFile;

    /**
     * constructor
     *
     * @param mainWindow
     */
    public InputTab(MainWindow mainWindow) {
        super(new SimpleStringProperty("Input"));
        setMainWindow(mainWindow);
        getTextArea().setEditable(true);
        getTextArea().setPromptText("Input data in Nexus format or any of the importable formats");

        getTextArea().focusedProperty().addListener((c, o, n) -> {
            if (n)
                getMainWindow().getMenuController().getPasteMenuItem().disableProperty().set(!Clipboard.getSystemClipboard().hasString());
        });

        // prevent double paste:
        {
            getTextArea().setOnKeyPressed(event -> {
                final KeyCombination keyCombCtrZ = new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN);
                if (keyCombCtrZ.match(event)) {
                    event.consume();
                }
            });
        }

        final ToolBar toolBar = new ToolBar();
        setToolBar(toolBar);

        final Button applyButton = new Button("Apply");
        toolBar.getItems().add(applyButton);
        applyButton.disableProperty().bind(getTextArea().textProperty().isEmpty());

        applyButton.setOnAction((e) -> {
            try {
                if (tmpFile == null) {
                    tmpFile = Basic.getUniqueFileName(System.getProperty("user.home"), "Untitled", "tmp");
                    tmpFile.deleteOnExit();
                }

                try (BufferedWriter w = new BufferedWriter(new FileWriter(tmpFile))) {
                    w.write(getTextArea().getText());
                }
                final Consumer<Throwable> exceptionHandler = throwable -> {
                    final IOExceptionWithLineNumber ioExceptionWithLineNumber;
                    if (throwable instanceof IOExceptionWithLineNumber) {
                        ioExceptionWithLineNumber = (IOExceptionWithLineNumber) throwable;
                    } else if (throwable.getCause() instanceof IOExceptionWithLineNumber) {
                        ioExceptionWithLineNumber = (IOExceptionWithLineNumber) throwable.getCause();
                    } else ioExceptionWithLineNumber = null;

                    // this highlights the line that has the problem
                    if (ioExceptionWithLineNumber != null) {
                        getTabPane().getSelectionModel().select(InputTab.this);
                        getTextArea().requestFocus();
                        gotoLine(ioExceptionWithLineNumber.getLineNumber());
                    }
                };

                FileOpener.open(true, mainWindow, tmpFile.getPath(), exceptionHandler);
            } catch (Exception ex) {
                NotificationManager.showError("Enter data failed: " + ex.getMessage());
            }
        });
    }

    @Override
    public void updateMenus(MenuController controller) {
        super.updateMenus(controller);

        controller.getPasteMenuItem().setOnAction((e) -> {
            if (getTextArea().isFocused()) {
                e.consume();
                getTextArea().paste();
            }
        });

        final MenuItem undoMenuItem = controller.getUndoMenuItem();
        undoMenuItem.setOnAction((e) -> getTextArea().undo());
        //undoMenuItem.setText("Undo edit");
        undoMenuItem.disableProperty().bind(getTextArea().undoableProperty().not());

        final MenuItem redoMenuItem = controller.getRedoMenuItem();
        redoMenuItem.setOnAction((e) -> getTextArea().redo());
        //redoMenuItem.setText("Redo edit");
        redoMenuItem.disableProperty().bind(getTextArea().redoableProperty().not());

        controller.getReplaceMenuItem().setOnAction((e) -> findToolBar.setShowReplaceToolBar(true));
        controller.getReplaceMenuItem().setDisable(false);
    }
}
