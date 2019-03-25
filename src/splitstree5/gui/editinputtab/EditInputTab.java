/*
 *  Copyright (C) 2019 Daniel H. Huson
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

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import jloda.fx.util.NotificationManager;
import jloda.fx.util.ProgramPropertiesFX;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.util.ResourceManagerFX;
import jloda.util.Basic;
import jloda.util.IOExceptionWithLineNumber;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.reactfx.Subscription;
import org.reactfx.value.Val;
import splitstree5.dialogs.importer.FileOpener;
import splitstree5.dialogs.importer.ImporterManager;
import splitstree5.gui.editinputtab.collapsing.LineNumberFactoryWithCollapsing;
import splitstree5.gui.editinputtab.collapsing.NexusBlockCollapseInfo;
import splitstree5.gui.editinputtab.collapsing.NexusBlockCollapser;
import splitstree5.gui.editinputtab.highlighters.NexusHighlighter;
import splitstree5.io.nexus.workflow.WorkflowNexusInput;
import splitstree5.main.MainWindow;
import splitstree5.main.MainWindowManager;
import splitstree5.menu.MenuController;

import java.io.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * tab for entering data
 * Daniel Huson, 2.2018
 */
public class EditInputTab extends EditTextViewTab {
    private File tmpFile;

    // todo undo doesn't work: collapse-uncollapce-undo. solution:dont delete blocks from the tmpBlocksKeeper after uncollapsing
    // todo zeilennummern, finding, performance?

    /**
     * constructor
     *
     * @param mainWindow
     */
    public EditInputTab(MainWindow mainWindow) {
        super(new SimpleStringProperty("Input"));
        setIcon(ResourceManagerFX.getIcon("sun/toolbarButtonGraphics/general/Import16.gif"));
        setMainWindow(mainWindow);

        final CodeArea codeArea = getCodeArea();
        CodeAreaStyler codeAreaStyler = new CodeAreaStyler(codeArea);

        String css = this.getClass().getResource("/resources/css/styles.css").toExternalForm();
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
                .subscribe(ignore -> codeArea.setStyleSpans(0,
                        codeAreaStyler.getHighlighter().computeHighlighting(codeArea.getText())));

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

        //final Button collapseButton = new Button("Activate Collapse NexusBlock"); //+++
        final CheckBox collapseButton = new CheckBox("Activate Collapse NexusBlock");

        toolBar.getItems().addAll(applyButton, collapseButton); //+++
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

                if (WorkflowNexusInput.isApplicable(tmpFile.getPath()))
                    WorkflowNexusInput.open(mainWindow, tmpFile.getPath());
                else
                    FileOpener.open(true, mainWindow, tmpFile.getPath(), exceptionHandler);
            } catch (Exception ex) {
                NotificationManager.showError("Enter data failed: " + ex.getMessage());
            }
        });


        collapseButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(oldValue) {
                    System.err.println("Block collapsing is disabled");
                    getCodeArea().setParagraphGraphicFactory(LineNumberFactory.get(getCodeArea()));
                    codeAreaStyler.setCollapsingActive(false);
                    ((NexusHighlighter) codeAreaStyler.getHighlighter()).setCollapsingActive(false);
                }
                if(newValue) {
                    System.err.println("Block collapsing is active");

                    ArrayList<NexusBlockCollapseInfo> info =
                            ((NexusHighlighter) codeAreaStyler.getHighlighter()).getNexusBlockCollapseInfos();
                    System.err.println("number ob blocks: "+info.size());
                    NexusBlockCollapser nexusBlockCollapser = new NexusBlockCollapser(getCodeArea(), info);
                    for(NexusBlockCollapseInfo i : info){
                        //int[] a = CodeAreaStyler.getLinesRangeByIndex(i.getStartPosition(), i.getEndPosition(), codeArea);
                        //System.err.println("lines" + a[0]+ a[1]);
                        System.err.println("block: "+i.getStartPosition()+"-"+i.getEndPosition()+" lines: "+
                                i.getStartLine()+"-"+i.getEndLine());
                    }
                    System.err.println("Indices!");
                    for(Integer i : nexusBlockCollapser.getLineIndices())
                        System.err.print(i+"-");

                    getCodeArea().setParagraphGraphicFactory(LineNumberFactoryWithCollapsing.get(getCodeArea(), nexusBlockCollapser));
                    codeAreaStyler.setCollapsingActive(true);
                    ((NexusHighlighter) codeAreaStyler.getHighlighter()).setCollapsingActive(true);
                }
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
            final File previousDir = new File(ProgramPropertiesFX.get("InputDir", ""));

            final FileChooser fileChooser = new FileChooser();
            if (previousDir.isDirectory())
                fileChooser.setInitialDirectory(previousDir);
            fileChooser.setTitle("Open input file");
            fileChooser.getExtensionFilters().addAll(ImporterManager.getInstance().getAllExtensionFilters());
            final File selectedFile = fileChooser.showOpenDialog(getMainWindow().getStage());
            if (selectedFile != null) {
                if (selectedFile.getParentFile().isDirectory())
                    ProgramPropertiesFX.put("InputDir", selectedFile.getParent());

                // todo check running time
                long startTime = System.nanoTime() / (long) Math.pow(10, 9);
                loadFile(selectedFile.getPath());
                long endTime   = System.nanoTime() / (long) Math.pow(10, 9);
                long totalTime = endTime - startTime;
                System.err.println();
                System.err.println(this.getClass()+":"+selectedFile.getPath());
                System.err.println("Running time EditInputTab: "+totalTime+" sec");
            }
        });
        controller.getOpenMenuItem().disableProperty().bind(Val.map(codeArea.lengthProperty(), n -> n != 0));

        RecentFilesManager.getInstance().setFileOpener(this::loadFile);
        controller.getOpenRecentMenu().disableProperty().bind(Val.map(codeArea.lengthProperty(), n -> n != 0));

        controller.getImportMenuItem().disableProperty().bind(new SimpleBooleanProperty(true));
        controller.getInputEditorMenuItem().disableProperty().bind(new SimpleBooleanProperty(true));


        controller.getPasteMenuItem().setOnAction((e) -> {
            e.consume();
            codeArea.paste();
        });

        controller.getSelectFromPreviousMenuItem().setOnAction((e) -> {
            for (String word : MainWindowManager.getPreviousSelection()) {
                final Pattern pattern = Pattern.compile(word);
                String source = codeArea.getText();
                Matcher matcher = pattern.matcher(source);

                if (matcher.find(0)) {
                    codeArea.selectRange(matcher.start(), matcher.end());
                    break;
                }
            }
        });
        controller.getSelectFromPreviousMenuItem().disableProperty().bind(Bindings.isEmpty(MainWindowManager.getPreviousSelection()));

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

    // todo performance test

    public void loadFile(String fileName) {
        final StringBuilder buf = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(Basic.getInputStreamPossiblyZIPorGZIP(fileName)))) {
            String aLine;
            while ((aLine = r.readLine()) != null)
                buf.append(aLine).append("\n");
            getCodeArea().replaceText(buf.toString());
        } catch (IOException ex) {
            NotificationManager.showError("Input file failed: " + ex.getMessage());
        }

    }
}
