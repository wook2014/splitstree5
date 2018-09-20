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

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import jloda.fx.NotificationManager;
import jloda.fx.RecentFilesManager;
import jloda.util.Basic;
import jloda.util.IOExceptionWithLineNumber;
import jloda.util.ProgramProperties;
import jloda.util.ResourceManager;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.event.MouseOverTextEvent;
import org.reactfx.Subscription;
import org.reactfx.value.Val;
import splitstree5.dialogs.importer.FileOpener;
import splitstree5.dialogs.importer.ImporterManager;
import splitstree5.gui.editinputtab.highlighters.Highlighter;
import splitstree5.gui.editinputtab.highlighters.NexusHighlighter;
import splitstree5.gui.editinputtab.highlighters.UniversalHighlighter;
import splitstree5.gui.editinputtab.highlighters.XMLHighlighter;
import splitstree5.main.MainWindow;
import splitstree5.main.MainWindowManager;
import splitstree5.menu.MenuController;

import java.io.*;
import java.time.Duration;
import java.util.HashMap;
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

    //private int blocksCounter = 0;
    //private HashMap<Integer, String> tmpBlocksKeeper = new HashMap<>();

    // todo undo doesn't work: collapse-uncollapce-undo. solution:dont delete blocks from the tmpBlocksKeeper after uncollapsing
    private boolean hold;
    private int chIdx;
    private HashMap<String, String> tmpBlocksKeeper = new HashMap<>();

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
        /*Val.map(codeArea.lengthProperty(), n -> n >= 6).addListener(new javafx.beans.value.ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                System.err.println(newValue);
                if (newValue) {
                    if (codeArea.getText().startsWith("#nexus"))
                        highlighter = new NexusHighlighter();
                    else if (codeArea.getText().startsWith("<"))
                        highlighter = new XMLHighlighter();
                    else
                        highlighter = new UniversalHighlighter();
                    System.err.println(highlighter.getClass().toString());
                }
            }
        });*/

        // controls highlighter updating (change if the first line changed)
        /*Val.map(codeArea.textProperty(), n -> n.substring(0, n.indexOf(System.lineSeparator())))
            .addListener(new javafx.beans.value.ChangeListener<String>() {
                 @Override
                 public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                     if (newValue.length() != 0) {
                         System.err.println(newValue);
                         if (codeArea.getText().startsWith("#nexus"))
                             highlighter = new NexusHighlighter();
                         else
                             highlighter = new XMLHighlighter();
                         System.err.println(highlighter.getClass().toString());
                     }
                 }
            });*/

        Val.map(codeArea.textProperty(), n -> n.length() >= 6
                && n.replaceAll("^\\n+", "").substring(0, 6).toLowerCase().equals("#nexus"))
            .addListener(new javafx.beans.value.ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        System.err.println("Use Nexus highlighter");
                        highlighter = new NexusHighlighter();
                    }
                }
            });

        Val.map(codeArea.textProperty(), n -> n.length() != 0 &&
                n.replaceAll("^\\n+", "").startsWith("<"))
                .addListener(new javafx.beans.value.ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        if (newValue) {
                            System.err.println("Use xml highlighter");
                            highlighter = new XMLHighlighter();
                        }
                    }
                });

        Val.map(codeArea.textProperty(), n -> n.length() >= 6
                && !n.replaceAll("^\\n+", "").substring(0, 6).toLowerCase().equals("#nexus")
                && !n.replaceAll("^\\n+", "").startsWith("<"))
                .addListener(new javafx.beans.value.ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        if (newValue) {
                            System.err.println("Use universal highlighter");
                            highlighter = new UniversalHighlighter();
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

        final Button collapseButton = new Button("Collapse nexus block"); //+++

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

                FileOpener.open(true, mainWindow, tmpFile.getPath(), exceptionHandler);
            } catch (Exception ex) {
                NotificationManager.showError("Enter data failed: " + ex.getMessage());
            }
        });

        //+++
        collapseButton.setDisable(true);
        collapseButton.setOnAction((e) -> {
            System.err.println("collapseButton pressed");
            try {
                System.err.println("Collapse current block");
                int position = codeArea.getCaretPosition();
                collapseBlock(position);
            } catch (Exception ex) {
                NotificationManager.showError("Nexus block collapsing failed: " + ex.getMessage());
            }
        });

        codeArea.setMouseOverTextDelay(Duration.ofMillis(200));
        codeArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, e -> {

            int i = e.getCharacterIndex();
            Point2D pos = e.getScreenPosition();

            if (codeArea.getStyleAtPosition(i).toString().contains("block")
                    || codeArea.getStyleAtPosition(i).toString().contains("collapsed")) {
                codeArea.setCursor(Cursor.HAND);
                hold = true;
                chIdx = e.getCharacterIndex();
            }
        });
        codeArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_END, e -> {
            codeArea.setCursor(Cursor.TEXT);
        });

        codeArea.setOnMouseClicked(click -> {

            if (hold) {

                //int chIdx = click.getCharacterIndex();
                System.out.println(codeArea.getStyleAtPosition(chIdx).toString());

                if (codeArea.getStyleAtPosition(chIdx).toString().contains("block"))
                    collapseBlock(chIdx);

                if (codeArea.getStyleAtPosition(chIdx).toString().contains("collapsed")) {

                    for (String i : tmpBlocksKeeper.keySet()) {
                        System.out.println(tmpBlocksKeeper.get(i).substring(0, 10));
                    }

                    final String CB = "(<< Collapsed )(\\w+)(Block >>)";
                    Pattern PATTERN = Pattern.compile(CB);
                    Matcher matcher = PATTERN.matcher(getCodeArea().getText());

                    int cbStart = 0, cbEnd = 0;
                    String key;
                    while (matcher.find()) {
                        cbStart = matcher.start();
                        cbEnd = matcher.end();
                        if (matcher.end() > chIdx)
                            break;
                    }

                    System.out.println(codeArea.getText().substring(cbStart, cbEnd));

                    //int blockNr = Integer.parseInt(matcher.group(2));
                    //System.out.println("collapsed nr. "+matcher.group(2));

                    if (cbStart != 0 || cbEnd != 0) {
                        key = matcher.group(2);
                        codeArea.replaceText(cbStart, cbEnd, tmpBlocksKeeper.get(key));
                        tmpBlocksKeeper.remove(key);
                    }

                    /*int cbStart = chIdx, cbEnd = chIdx;
                    while(getCodeArea().getText().charAt(cbStart) != '<')
                        cbStart--;
                    while(getCodeArea().getText().charAt(cbEnd) != '>')
                        cbEnd++;
                    cbStart--;
                    cbEnd++;

                    final String CB = "(< Collapsed )(\\w+)(Block >)";
                    Pattern PATTERN = Pattern.compile(CB);

                    String CB_string = getKeyWord(chIdx, false);
                    System.out.println(CB_string);
                    Matcher matcher = PATTERN.matcher(CB_string);

                    if (matcher.find()) {
                        String key = matcher.group(2);
                        codeArea.replaceText(cbStart, cbEnd, tmpBlocksKeeper22.get(key));
                        tmpBlocksKeeper22.remove(key);
                    }*/
                }
                hold = false; // prevents double collapsing after continuous clicks
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

                // check running time
                long startTime = System.nanoTime();
                loadFile(selectedFile.getPath());
                long endTime   = System.nanoTime();
                long totalTime = endTime - startTime;
                System.err.println();
                System.err.println(this.getClass()+":"+selectedFile.getPath());
                System.err.println("Running time: "+totalTime);
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

    // todo performance test

    private void loadFile(String fileName) {
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

    private static String returnFirstLine(String s){
        if (s.length() == 0 || !s.contains("\\n"))
            return s;
        else
            return s.substring(0, s.indexOf("\\n"));
    }

    private void collapseBlock(int charIndex) {

        int start2remove = 0;
        int end2remove = 0;
        //int endOffset = codeArea.getText().substring(0, chIdx).length();

        Pattern PATTERN1 = Pattern.compile("(?i)\\b(end|endblock)\\b;");
        Pattern PATTERN2 = Pattern.compile("(?i)\\bbegin\\b");
        Matcher matcher1 = PATTERN1.matcher(getCodeArea().getText().substring(charIndex));
        Matcher matcher2 = PATTERN2.matcher(getCodeArea().getText().substring(0, charIndex));

        // the first end
        if (matcher1.find())
            end2remove = matcher1.end() + charIndex + 1;
        // the last begin
        while (matcher2.find())
            start2remove = matcher2.start();

        System.out.println("start2remove "+start2remove);
        System.out.println("end2remove "+end2remove);
        //System.out.println("Block Nr. "+beginCounter);

        //tmpBlocksKeeper.put(beginCounter, getCodeArea().getText().substring(start2remove, end2remove));

        if (start2remove != 0 || end2remove != 0) {
            /*blocksCounter ++;
            tmpBlocksKeeper.put(blocksCounter, getCodeArea().getText().substring(start2remove, end2remove));
            getCodeArea().replaceText(start2remove, end2remove, "<< Collapsed block "+blocksCounter+">>");
            System.out.println("Block Nr. "+blocksCounter);*/

            String keyWord = getKeyWord(charIndex, true);
            tmpBlocksKeeper.put(keyWord, getCodeArea().getText().substring(start2remove, end2remove));
            getCodeArea().replaceText(start2remove, end2remove, "<< Collapsed "+keyWord+"Block >>");
            System.out.println("Block Nr. "+keyWord);
        }
    }

    private String getKeyWord(int position, boolean collapse) {

        int leftPos = position;
        int rightPos = position;

        if (collapse) {
            while(Character.isLetter(getCodeArea().getText().charAt(leftPos)))
                leftPos--;
            while(Character.isLetter(getCodeArea().getText().charAt(rightPos)))
                rightPos++;
            leftPos++;
        } else {
            while(getCodeArea().getText().charAt(leftPos) != '<')
                leftPos--;
            while(getCodeArea().getText().charAt(leftPos) != '>')
                rightPos++;
            rightPos++;
        }
        return getCodeArea().getText().substring(leftPos, rightPos);
    }
}
