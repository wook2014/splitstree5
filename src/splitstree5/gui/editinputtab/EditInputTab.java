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

package splitstree5.gui.editinputtab;

import com.sun.org.apache.bcel.internal.classfile.Code;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import jloda.fx.NotificationManager;
import jloda.fx.RecentFilesManager;
import jloda.util.Basic;
import jloda.util.ProgramProperties;
import jloda.util.ResourceManager;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpan;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;
import org.reactfx.value.Val;
import splitstree5.dialogs.importer.FileOpener;
import splitstree5.dialogs.importer.ImporterManager;
import splitstree5.gui.editinputtab.highlighters.Highlighter;
import splitstree5.gui.editinputtab.highlighters.NexusHighlighter;
import splitstree5.gui.editinputtab.highlighters.XMLHighlighter;
import splitstree5.gui.texttab.TextViewTab;
import splitstree5.io.imports.IOExceptionWithLineNumber;
import splitstree5.main.MainWindow;
import splitstree5.main.MainWindowManager;
import splitstree5.menu.MenuController;

import javax.swing.event.ChangeListener;
import java.io.*;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * tab for entering data
 * Daniel Huson, 2.2018
 */
public class EditInputTab extends EditTextViewTab {
    private File tmpFile;

    private Highlighter highlighter = new NexusHighlighter();

    /**
     * constructor
     *
     * @param mainWindow
     */
    public EditInputTab(MainWindow mainWindow) {
        super(new SimpleStringProperty("Input"));
        setIcon(ResourceManager.getIcon("sun/toolbarButtonGraphics/general/Import16.gif"));
        setMainWindow(mainWindow);

        //final TextArea textArea = getTextArea();
        final CodeArea codeArea = getCodeArea();

        // controls highlighter updating (change if >= 6 symbols = #nexus)
        Val.map(codeArea.lengthProperty(), n -> n >= 6).addListener(new javafx.beans.value.ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                System.err.println(newValue);
                if (newValue) {
                    if (codeArea.getText().startsWith("#nexus"))
                        highlighter = new NexusHighlighter();
                    else
                        highlighter = new XMLHighlighter();
                    System.err.println(highlighter.getClass().toString());
                }
            }
        });

        String css = this.getClass().getResource("styles.css").toExternalForm();
        //String css = this.getClass().getResource(highlighter.getCSS()).toExternalForm();
        codeArea.getStylesheets().add(css);
        codeArea.setEditable(true);

        // recompute the syntax highlighting 500 ms after user stops editing area
        Subscription cleanupWhenNoLongerNeedIt = codeArea

                // plain changes = ignore style changes that are emitted when syntax highlighting is reapplied
                // multi plain changes = save computation by not rerunning the code multiple times
                // when making multiple changes (e.g. renaming a method at multiple parts in file)
                .multiPlainChanges()

                // do not emit an event until 500 ms have passed since the last emission of previous stream
                .successionEnds(Duration.ofMillis(500))

                // run the following code block when previous stream emits an event
                //.subscribe(ignore -> codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText())));
                .subscribe(ignore -> codeArea.setStyleSpans(0, highlighter.computeHighlighting(codeArea.getText())));
        //codeArea.replaceText(0, 0, sampleCode);
        //////////

        /////codeArea.setAccessibleText("Input and edit data in any supported format");
        //setPromptText("Input and edit data in any supported format");

        codeArea.setContextMenu(createContextMenu());

        codeArea.focusedProperty().addListener((c, o, n) -> {
            if (n)
                getMainWindow().getMenuController().getPasteMenuItem().disableProperty().set(!Clipboard.getSystemClipboard().hasString());
        });

        // prevent double paste:
        {
            codeArea.addEventFilter(KeyEvent.ANY, e -> {
                if (e.getCode() == KeyCode.V && e.isShortcutDown()) {
                    e.consume();
                }
            });
        }

        final ToolBar toolBar = new ToolBar();
        setToolBar(toolBar);


        final Button applyButton = new Button("Parse and Load");
        applyButton.setTooltip(new Tooltip("Save this data to a temporary file, parse the file and then load the data"));

        final Button highlightButton = new Button("Highlight"); //+++

        toolBar.getItems().addAll(applyButton, highlightButton); //+++
        //toolBar.getItems().addAll(applyButton);
        applyButton.disableProperty().bind(Val.map(codeArea.lengthProperty(), n -> n == 0));
        //highlightButton.disableProperty().bind(getCodeArea().textProperty().isEmpty()); //+++

        applyButton.setOnAction((e) -> {
            try {
                if (tmpFile == null) {
                    tmpFile = Basic.getUniqueFileName(System.getProperty("user.dir"), "Untitled", "tmp");
                    tmpFile.deleteOnExit();
                }

                try (BufferedWriter w = new BufferedWriter(new FileWriter(tmpFile))) {
                    w.write(getCodeArea().getText());
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
                        getTabPane().getSelectionModel().select(EditInputTab.this);
                        codeArea.requestFocus();
                        gotoLine(ioExceptionWithLineNumber.getLineNumber(), 0);
                    }
                };

                FileOpener.open(true, mainWindow, tmpFile.getPath(), exceptionHandler);
            } catch (Exception ex) {
                NotificationManager.showError("Enter data failed: " + ex.getMessage());
            }
        });

        //+++
        highlightButton.setOnAction((e) -> {
            System.err.println("highlighting pressed");
            try {
                System.err.println("highlighting");
                /*for (StyleSpan styleSpan : computeHighlighting(getCodeArea().getText())){
                    System.err.println(styleSpan.getLength());
                    System.err.println(styleSpan.toString());
                    System.err.println(styleSpan.getStyle());
                }*/

                //codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));
                //codeArea.replaceText(0, 0, codeArea.getText());
                //getCodeArea().setStyle("-fx-font-weight: bold;");

                //textArea.setParagraphGraphicFactory(LineNumberFactory.get(textArea));
                //textArea.setStyleSpans(0, computeHighlighting(textArea.getText()));


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
        final CodeArea textArea = getCodeArea();

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

        final CodeArea codeArea = getCodeArea();

        controller.getOpenMenuItem().setOnAction((e) -> {
            final File previousDir = new File(ProgramProperties.get("InputDir", ""));

            final FileChooser fileChooser = new FileChooser();
            if (previousDir.isDirectory())
                fileChooser.setInitialDirectory(previousDir);
            fileChooser.setTitle("Open input file");
            fileChooser.getExtensionFilters().addAll(ImporterManager.getInstance().getAllExtensionFilters());
            final File selectedFile = fileChooser.showOpenDialog(getMainWindow().getStage());
            if (selectedFile != null) {
                if (selectedFile.getParentFile().isDirectory())
                    ProgramProperties.put("InputDir", selectedFile.getParent());
                loadFile(selectedFile.getPath());
            }
        });
        // not empty
        controller.getOpenMenuItem().disableProperty().bind(Val.map(codeArea.lengthProperty(), n -> n != 0));

        RecentFilesManager.getInstance().setFileOpener(this::loadFile);
        // not empty
        controller.getOpenRecentMenu().disableProperty().bind(Val.map(codeArea.lengthProperty(), n -> n != 0));

        controller.getPasteMenuItem().setOnAction((e) -> {
            e.consume();
            codeArea.paste();
        });

        controller.getSelectFromPreviousMenuItem().setOnAction((e) -> {
            for (String word : MainWindowManager.getInstance().getPreviousSelection()) {
                final Pattern pattern = Pattern.compile(word);
                String source = codeArea.getText();
                Matcher matcher = pattern.matcher(source);

                if (matcher.find(0)) {
                    codeArea.selectRange(matcher.start(), matcher.end());
                    break;
                }
            }
        });
        controller.getSelectFromPreviousMenuItem().disableProperty().bind(Bindings.isEmpty(MainWindowManager.getInstance().getPreviousSelection()));

        final MenuItem undoMenuItem = controller.getUndoMenuItem();
        undoMenuItem.setOnAction((e) -> codeArea.undo());
        //undoMenuItem.setText("Undo edit");
        //undoMenuItem.disableProperty().bind(getTextArea().undoableProperty().not());
        undoMenuItem.disableProperty().bind(Val.map(codeArea.undoAvailableProperty(), n -> !n));

        final MenuItem redoMenuItem = controller.getRedoMenuItem();
        redoMenuItem.setOnAction((e) -> codeArea.redo());
        //redoMenuItem.setText("Redo edit");
        //redoMenuItem.disableProperty().bind(getTextArea().redoableProperty().not());
        redoMenuItem.disableProperty().bind(Val.map(codeArea.redoAvailableProperty(), n -> !n));

        controller.getReplaceMenuItem().setOnAction((e) -> findToolBar.setShowReplaceToolBar(true));
        controller.getReplaceMenuItem().setDisable(false);

        controller.getCutMenuItem().setOnAction((e) -> {
            e.consume();
            codeArea.cut();
        });
        //controller.getCutMenuItem().disableProperty().bind(getTextArea().selectedTextProperty().length().isEqualTo(0));
        controller.getCutMenuItem().disableProperty().bind(Val.map(codeArea.selectedTextProperty(), n -> n.length() == 0));

        controller.getDeleteMenuItem().setOnAction((e) -> {
            e.consume();
            codeArea.deleteText(getCodeArea().getSelection());
        });
        controller.getDeleteMenuItem().disableProperty().bind(Val.map(codeArea.selectedTextProperty(), n -> n.length() == 0));

        controller.getDuplicateMenuItem().setOnAction((e) -> {
            e.consume();
            codeArea.replaceSelection(codeArea.getSelectedText() + codeArea.getSelectedText());
        });
        controller.getDuplicateMenuItem().disableProperty().bind(Val.map(codeArea.selectedTextProperty(), n -> n.length() == 0));


    }

    private void loadFile(String fileName) {
        final StringBuilder buf = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(Basic.getInputStreamPossiblyZIPorGZIP(fileName)))) {
            String aLine;
            while ((aLine = r.readLine()) != null)
                buf.append(aLine).append("\n");
            getCodeArea().setAccessibleText(buf.toString());
        } catch (IOException ex) {
            NotificationManager.showError("Input file failed: " + ex.getMessage());
        }

    }
}
