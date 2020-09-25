/*
 * EditInputTab.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.gui.editinputtab;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.util.ResourceManagerFX;
import jloda.fx.window.MainWindowManager;
import jloda.fx.window.NotificationManager;
import jloda.util.Basic;
import jloda.util.IOExceptionWithLineNumber;
import jloda.util.ProgramProperties;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import splitstree5.dialogs.importer.FileOpener;
import splitstree5.dialogs.importer.ImporterManager;
import splitstree5.gui.editinputtab.highlighters.NexusHighlighter;
import splitstree5.io.nexus.workflow.WorkflowNexusInput;
import splitstree5.main.MainWindow;
import splitstree5.menu.MenuController;

import java.io.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * tab for entering data
 * Daniel Huson, Daria Evseeva, 2.2018
 */
public class EditInputTab extends EditTextViewTab {
    private static boolean debug = false;
    private final int MAX_LENGTH_SPECIAL_CHAR_FIELD = 1;
    private StringProperty missing = new SimpleStringProperty("");
    private StringProperty gap = new SimpleStringProperty("");

    private File tmpFile;

    // todo undo doesn't work: collapse-uncollapce-undo. solution:dont delete blocks from the tmpBlocksKeeper after uncollapsing
    // todo zeilennummern, finding, performance?


    // todo check all properties!
    /**
     * constructor
     *
     * @param mainWindow
     */
    public EditInputTab(MainWindow mainWindow) {
        super(new SimpleStringProperty("Input"));
        setGraphic(new HBox(new ImageView(ResourceManagerFX.getIcon("sun/Import16.gif")), new Label("Input")));
        setMainWindow(mainWindow);

        final CodeArea codeArea = getCodeArea();
        CodeAreaStyler codeAreaStyler = new CodeAreaStyler(codeArea);
        codeArea.setEditable(true);

        codeArea.textProperty().addListener((observableValue, s, t1) ->
                codeArea.setStyleSpans(0, codeAreaStyler.getHighlighter().computeHighlighting(codeArea.getText())));

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
        toolBar.setVisible(false);
        setToolBar(toolBar);

        final Button applyButton = new Button("Parse and Load");
        applyButton.setTooltip(new Tooltip("Save this data to a temporary file, parse the file and then load the data"));

        //final Button collapseButton = new Button("Activate Collapse NexusBlock"); //+++
        final CheckBox collapseButton = new CheckBox("Activate Collapse NexusBlock");


        //++++++ SPECIAL CHARACTERS EDITOR

        final Label labelMissingChar = new Label("missingChar");
        final TextField missingChar = new TextField();
        missingChar.setPrefColumnCount(1);
        missingChar.setOnKeyTyped(event ->{
            if(missingChar.getText().length() >= MAX_LENGTH_SPECIAL_CHAR_FIELD)
                event.consume();  // todo use getDataType from ImporterManager
        });
        missingChar.disableProperty().bind(emptyProperty);
        missing.bind(missingChar.textProperty());

        final Label labelGapChar = new Label("gapChar");
        final TextField gapChar = new TextField();
        gapChar.setPrefColumnCount(1);
        gapChar.setOnKeyTyped(event -> {
            if (gapChar.getText().length() >= MAX_LENGTH_SPECIAL_CHAR_FIELD)
                event.consume();
        });
        gapChar.disableProperty().bind(emptyProperty);
        gap.bind(gapChar.textProperty());

        //++++++ SPECIAL CHARACTERS EDITOR END

        toolBar.setVisible(true);

        if (ProgramProperties.get("enable-activiate-collapse", false)) {
            toolBar.getItems().addAll(collapseButton, applyButton,
                    labelMissingChar, missingChar,
                    labelGapChar, gapChar); //+++
        } else
            toolBar.getItems().add(applyButton);

        applyButton.disableProperty().bind(emptyProperty);
        //highlightButton.disableProperty().bind(getCodeArea().textProperty().isEmpty()); //+++

        applyButton.setOnAction(e -> {
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
                    FileOpener.open(true, mainWindow, mainWindow.getMainWindowController().getBottomPane(), tmpFile.getPath(), exceptionHandler);
            } catch (Exception ex) {
                NotificationManager.showError("Enter data failed: " + ex.getMessage());
            }
        });


        collapseButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(oldValue) {
                    if (debug)
                        System.err.println("Block collapsing is disabled");
                    getCodeArea().setParagraphGraphicFactory(LineNumberFactory.get(getCodeArea()));
                    codeAreaStyler.setCollapsingActive(false);
                    ((NexusHighlighter) codeAreaStyler.getHighlighter()).setCollapsingActive(false);
                }
                if(newValue) {
                    if (debug)
                        System.err.println("Block collapsing is active");

                    /*ArrayList<NexusBlockCollapseInfo> info =
                            ((NexusHighlighter) codeAreaStyler.getHighlighter()).getNexusBlockCollapseInfos();
                    if (debug)
                        System.err.println("number ob blocks: " + info.size());
                    NexusBlockCollapser nexusBlockCollapser = new NexusBlockCollapser(getCodeArea(), info);*/
                    /*for (NexusBlockCollapseInfo i : info) {
                        //int[] a = CodeAreaStyler.getLinesRangeByIndex(i.getStartPosition(), i.getEndPosition(), codeArea);
                        //System.err.println("lines" + a[0]+ a[1]);
                        if (debug)
                            System.err.println("block: " + i.getStartPosition() + "-" + i.getEndPosition() + " lines: " +
                                    i.getStartLine() + "-" + i.getEndLine());
                    }*/
                    /*if (debug)
                        System.err.println("Indices!");
                    if (debug)
                        for (Integer i : nexusBlockCollapser.getLineIndices())
                            System.err.print(i + "-");*/

                    //getCodeArea().setParagraphGraphicFactory(LineNumberFactoryWithCollapsing.get(getCodeArea(), nexusBlockCollapser));
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
                createMenuItem("Undo", e -> textArea.undo()),
                createMenuItem("Redo", e -> textArea.redo()),
                createMenuItem("Cut", e -> textArea.cut()),
                createMenuItem("Copy", e -> textArea.copy()),
                createMenuItem("Paste", e -> textArea.paste()),
                createMenuItem("Delete", e -> textArea.deleteText(textArea.getSelection())),
                new SeparatorMenuItem(),
                createMenuItem("Select All", e -> textArea.selectAll()),
                createMenuItem("Select Brackets", e -> selectBrackets(textArea)));
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

        controller.getOpenMenuItem().setOnAction(e -> {
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

                // todo check running time
                long startTime = System.nanoTime() / (long) Math.pow(10, 9);
                loadFile(selectedFile.getPath());
                long endTime = System.nanoTime() / (long) Math.pow(10, 9);
                long totalTime = endTime - startTime;
                if (debug)
                    System.err.println();
                if (debug)
                    System.err.println(this.getClass() + ":" + selectedFile.getPath());
                if (debug)
                    System.err.println("Running time EditInputTab: " + totalTime + " sec");
            }
        });
        controller.getOpenMenuItem().disableProperty().bind(emptyProperty.not());

        RecentFilesManager.getInstance().setFileOpener(this::loadFile);
        controller.getOpenRecentMenu().disableProperty().bind(emptyProperty.not());

        controller.getImportMenuItem().disableProperty().bind(new SimpleBooleanProperty(true));
        controller.getInputEditorMenuItem().disableProperty().bind(new SimpleBooleanProperty(true));

        controller.getAnalyzeGenomesMenuItem().disableProperty().bind(new SimpleBooleanProperty(true));


        controller.getPasteMenuItem().setOnAction(e -> {
            e.consume();
            codeArea.paste();
        });

        controller.getSelectFromPreviousMenuItem().setOnAction(e -> {
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
        undoMenuItem.setOnAction(e -> codeArea.undo());
        //undoMenuItem.setText("Undo edit");
        //undoMenuItem.disableProperty().bind(getTextArea().undoableProperty().not());
        undoMenuItem.disableProperty().bind(emptyProperty);

        final MenuItem redoMenuItem = controller.getRedoMenuItem();
        redoMenuItem.setOnAction(e -> codeArea.redo());
        //redoMenuItem.setText("Redo edit");
        //redoMenuItem.disableProperty().bind(getTextArea().redoableProperty().not());
        redoMenuItem.disableProperty().bind(emptyProperty);

        controller.getReplaceMenuItem().setOnAction(e -> findToolBar.setShowReplaceToolBar(true));
        controller.getReplaceMenuItem().setDisable(false);

        controller.getCutMenuItem().setOnAction(e -> {
            e.consume();
            codeArea.cut();
        });
        //controller.getCutMenuItem().disableProperty().bind(getTextArea().selectedTextProperty().length().isEqualTo(0));
        controller.getCutMenuItem().disableProperty().bind(selectionEmpty);

        controller.getDeleteMenuItem().setOnAction(e -> {
            e.consume();
            codeArea.deleteText(getCodeArea().getSelection());
        });
        controller.getDeleteMenuItem().disableProperty().bind(selectionEmpty);

        controller.getDuplicateMenuItem().setOnAction(e -> {
            e.consume();
            codeArea.replaceSelection(codeArea.getSelectedText() + codeArea.getSelectedText());
        });
        controller.getDuplicateMenuItem().disableProperty().bind(selectionEmpty);
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

    public String getMissing(){
        return this.missing.getValue();
    }
    public String getGap() {
        return this.gap.getValue();
    }
}
