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

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import jloda.fx.NotificationManager;
import jloda.fx.RecentFilesManager;
import jloda.util.Basic;
import jloda.util.ResourceManager;
import splitstree5.dialogs.importer.FileOpener;
import splitstree5.dialogs.importer.ImporterManager;
import splitstree5.gui.texttab.TextViewTab;
import splitstree5.io.imports.IOExceptionWithLineNumber;
import splitstree5.main.MainWindow;
import splitstree5.main.MainWindowManager;
import splitstree5.menu.MenuController;

import java.io.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        setIcon(ResourceManager.getIcon("sun/toolbarButtonGraphics/general/Import16.gif"));
        setMainWindow(mainWindow);

        final TextArea textArea = getTextArea();

        textArea.setEditable(true);
        textArea.setPromptText("Input data in Nexus format or any of the importable formats");

        textArea.setContextMenu(createContextMenu());

        textArea.focusedProperty().addListener((c, o, n) -> {
            if (n)
                getMainWindow().getMenuController().getPasteMenuItem().disableProperty().set(!Clipboard.getSystemClipboard().hasString());
        });

        // prevent double paste:
        {
            textArea.setOnKeyPressed(event -> {
                final KeyCombination keyCombCtrZ = new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN);
                if (keyCombCtrZ.match(event)) {
                    event.consume();
                }
            });
        }

        final ToolBar toolBar = new ToolBar();
        setToolBar(toolBar);


        final Button applyButton = new Button("Parse and Load");
        applyButton.setTooltip(new Tooltip("Save this data to a temporary file, parse the file and then load the data"));

        toolBar.getItems().addAll(applyButton);
        applyButton.disableProperty().bind(getTextArea().textProperty().isEmpty());

        applyButton.setOnAction((e) -> {
            try {
                if (tmpFile == null) {
                    tmpFile = Basic.getUniqueFileName(System.getProperty("user.dir"), "Untitled", "tmp");
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
                        textArea.requestFocus();
                        gotoLine(ioExceptionWithLineNumber.getLineNumber());
                    }
                };

                FileOpener.open(true, mainWindow, tmpFile.getPath(), exceptionHandler);
            } catch (Exception ex) {
                NotificationManager.showError("Enter data failed: " + ex.getMessage());
            }
        });
    }

    /**
     * replaces the built-in context menu
     *
     * @return context menu
     */
    private ContextMenu createContextMenu() {
        final TextArea textArea = getTextArea();

        return new ContextMenu(
                createMenuItem("Undo", (e) -> textArea.undo()),
                createMenuItem("Redo", (e) -> textArea.redo()),
                createMenuItem("Cut", (e) -> textArea.cut()),
                createMenuItem("Copy", (e) -> textArea.copy()),
                createMenuItem("Paste", (e) -> textArea.paste()),
                createMenuItem("Delete", (e) -> textArea.deleteText(textArea.getSelection())),
                new SeparatorMenuItem(),
                createMenuItem("Select All", (e) -> textArea.selectAll()),
                createMenuItem("Select Brackets", (e) -> selectBrackets(textArea)));
    }

    private MenuItem createMenuItem(String name, EventHandler<ActionEvent> eventHandler) {
        MenuItem menuItem = new MenuItem(name);
        menuItem.setOnAction(eventHandler);
        return menuItem;
    }


    @Override
    public void updateMenus(MenuController controller) {
        super.updateMenus(controller);

        final TextArea textArea = getTextArea();

        controller.getOpenMenuItem().setOnAction((e) -> {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open input file");
            fileChooser.getExtensionFilters().addAll(ImporterManager.getInstance().getAllExtensionFilters());
            final File file = fileChooser.showOpenDialog(getMainWindow().getStage());
            if (file != null)
                loadFile(file.getPath());
        });
        controller.getOpenMenuItem().disableProperty().bind(textArea.textProperty().isNotEmpty());

        RecentFilesManager.getInstance().setFileOpener(this::loadFile);
        RecentFilesManager.getInstance().disableProperty().bind(textArea.textProperty().isNotEmpty());

        controller.getPasteMenuItem().setOnAction((e) -> {
            if (getTextArea().isFocused()) {
                e.consume();
                textArea.paste();
            }
        });

        controller.getSelectFromPreviousMenuItem().setOnAction((e) -> {
            for (String word : MainWindowManager.getInstance().getPreviousSelection()) {
                final Pattern pattern = Pattern.compile(word);
                String source = textArea.getText();
                Matcher matcher = pattern.matcher(source);

                if (matcher.find(0)) {
                    textArea.selectRange(matcher.start(), matcher.end());
                    break;
                }
            }
        });
        controller.getSelectFromPreviousMenuItem().disableProperty().bind(Bindings.isEmpty(MainWindowManager.getInstance().getPreviousSelection()));

        final MenuItem undoMenuItem = controller.getUndoMenuItem();
        undoMenuItem.setOnAction((e) -> textArea.undo());
        //undoMenuItem.setText("Undo edit");
        undoMenuItem.disableProperty().bind(getTextArea().undoableProperty().not());

        final MenuItem redoMenuItem = controller.getRedoMenuItem();
        redoMenuItem.setOnAction((e) -> textArea.redo());
        //redoMenuItem.setText("Redo edit");
        redoMenuItem.disableProperty().bind(getTextArea().redoableProperty().not());

        controller.getReplaceMenuItem().setOnAction((e) -> findToolBar.setShowReplaceToolBar(true));
        controller.getReplaceMenuItem().setDisable(false);

        controller.getCutMenuItem().setOnAction((e) -> {
            e.consume();
            textArea.cut();
        });
        controller.getCutMenuItem().disableProperty().bind(getTextArea().selectedTextProperty().length().isEqualTo(0));
    }

    private void loadFile(String fileName) {
        final StringBuilder buf = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(Basic.getInputStreamPossiblyZIPorGZIP(fileName)))) {
            String aLine;
            while ((aLine = r.readLine()) != null)
                buf.append(aLine).append("\n");
            getTextArea().setText(buf.toString());
        } catch (IOException ex) {
            NotificationManager.showError("Input file failed: " + ex.getMessage());
        }

    }
}
