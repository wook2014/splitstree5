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

package splitstree5.gui.texttab;


import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.StageStyle;
import jloda.fx.find.FindToolBar;
import jloda.fx.find.TextAreaSearcher;
import jloda.fx.util.Print;
import jloda.fx.util.ProgramPropertiesFX;
import jloda.fx.window.MainWindowManager;
import jloda.util.Basic;
import splitstree5.gui.ViewerTab;
import splitstree5.menu.MenuController;

import java.util.Optional;

/**
 * text view tab for displaying ummutable text
 * Daniel Huson, 1.2018
 */
public class TextViewTab extends ViewerTab {
    private final TextArea textArea;
    private final TextAreaSearcher textAreaSearcher;

    /**
     * constructor
     *
     * @param nameProperty
     */
    public TextViewTab(ReadOnlyStringProperty nameProperty) {
        this(nameProperty, null);
    }

    /**
     * constructor
     *
     * @param nameProperty
     * @param textProperty
     */
    public TextViewTab(ReadOnlyStringProperty nameProperty, ReadOnlyStringProperty textProperty) {
        final Label label = new Label();
        label.textProperty().bind(nameProperty);
        setGraphic(label);
        setText("");
        textArea = new TextArea();
        String css = this.getClass().getResource("/resources/css/styles.css").toExternalForm();
        textArea.getStylesheets().add(css);
        //textArea.setFont(Font.font("Courier New")); // gets set by style file
        textArea.setEditable(false);
        if (textProperty != null)
            textArea.textProperty().bind(textProperty);
        textArea.textProperty().addListener((InvalidationListener) -> getUndoManager().clear());

        textArea.selectionProperty().addListener((c, o, n) -> {
            MainWindowManager.getPreviousSelection().clear();
            MainWindowManager.getPreviousSelection().add(textArea.getText(n.getStart(), n.getEnd()));
        });

        // setup find tool bar:
        {
            textAreaSearcher = new TextAreaSearcher("Text", textArea);
            findToolBar = new FindToolBar(textAreaSearcher);
        }

        final BorderPane borderPane = new BorderPane(textArea);
        borderPane.setTop(findToolBar);
        setContent(borderPane);
        setClosable(true);
    }

    /**
     * go to given line and given col
     *
     * @param lineNumber
     * @param col        if col<=1 or col>line length, will select the whole line, else selects line starting at given col
     */
    public void gotoLine(long lineNumber, int col) {
        if (col < 0)
            col = 0;
        else if (col > 0)
            col--; // because col is 1-based

        lineNumber = Math.max(1, lineNumber);
        final String text = textArea.getText();
        int start = 0;
        for (int i = 1; i < lineNumber; i++) {
            start = text.indexOf('\n', start + 1);
            if (start == -1) {
                System.err.println("No such line number: " + lineNumber);
                return;
            }
        }
        start++;
        if (start < text.length()) {
            int end = text.indexOf('\n', start);
            if (end == -1)
                end = text.length();
            if (start + col < end)
                start = start + col;
            textArea.requestFocus();
            textArea.selectRange(start, end);
        }
    }

    /**
     * select matching brackets
     *
     * @param textArea
     */
    protected void selectBrackets(TextArea textArea) {
        int pos = textArea.getCaretPosition() - 1;
        while (pos > 0 && pos < textArea.getText().length()) {
            final char close = textArea.getText().charAt(pos);
            if (close == ')' || close == ']' || close == '}') {
                final int closePos = pos;
                final int open = (close == ')' ? '(' : (close == ']' ? '[' : '}'));

                int balance = 0;
                for (; pos >= 0; pos--) {
                    char ch = textArea.getText().charAt(pos);
                    if (ch == open)
                        balance--;
                    else if (ch == close)
                        balance++;
                    if (balance == 0) {
                        final int fpos = pos;
                        Platform.runLater(() -> textArea.selectRange(fpos, closePos + 1));
                        return;
                    }
                }
            }
            pos++;
        }
    }

    @Override
    public void updateMenus(MenuController controller) {

        // for some reason minus key is ignored otherwise:
        getTextArea().setOnKeyPressed((e) -> {
            if (e.isShortcutDown() && (e.getCode() == KeyCode.MINUS || e.getCode() == KeyCode.UNDERSCORE)) {
                e.consume();
                controller.getDecreaseFontSizeMenuItem().fire();
            }
        });

        controller.getPageSetupMenuItem().setOnAction((e) -> Print.showPageLayout(getMainWindow().getStage()));
        controller.getPrintMenuitem().setOnAction((e) -> Print.print(getMainWindow().getStage(), textArea));

        if (getUndoManager() != null) {
            controller.getUndoMenuItem().setOnAction((e) -> {
                getUndoManager().undo();
            });
            controller.getUndoMenuItem().disableProperty().bind(getUndoManager().canUndoProperty().not());
            controller.getUndoMenuItem().textProperty().bind(getUndoManager().undoNameProperty());

            controller.getRedoMenuItem().setOnAction((e) -> {
                getUndoManager().redo();
            });
            controller.getRedoMenuItem().disableProperty().bind(getUndoManager().canRedoProperty().not());
            controller.getRedoMenuItem().textProperty().bind(getUndoManager().redoNameProperty());
        }

        controller.getCopyMenuItem().setOnAction((e) -> {
            e.consume();
            textArea.copy();
        });
        controller.getCopyMenuItem().disableProperty().bind(textArea.selectedTextProperty().isEmpty());

        controller.getSelectAllMenuItem().setOnAction((e) -> textArea.selectAll());
        controller.getSelectNoneMenuItem().setOnAction((e) -> textArea.selectHome());
        controller.getSelectNoneMenuItem().disableProperty().bind(textArea.selectedTextProperty().isEmpty());

        controller.getSelectBracketsMenuItem().setOnAction((e) -> selectBrackets(textArea));
        controller.getSelectBracketsMenuItem().disableProperty().bind(textArea.textProperty().isEmpty());

        controller.getFindMenuItem().setOnAction((e) -> findToolBar.setShowFindToolBar(true));
        controller.getFindAgainMenuItem().setOnAction((e) -> findToolBar.findAgain());
        controller.getFindAgainMenuItem().disableProperty().bind(findToolBar.canFindAgainProperty().not());

        controller.getGotoLineMenuItem().setOnAction((e) -> {
            final TextInputDialog dialog = new TextInputDialog("");
            dialog.setTitle("Go to Line - " + ProgramPropertiesFX.getProgramName());
            dialog.initStyle(StageStyle.UTILITY);
            dialog.setX(getMainWindow().getStage().getX() + 0.5 * getMainWindow().getStage().getWidth());
            dialog.setY(getMainWindow().getStage().getY() + 0.5 * getMainWindow().getStage().getHeight());
            dialog.setHeaderText("Select line by number");
            dialog.setContentText("[line] [:column]:");
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                final String[] tokens = result.get().split(":");
                if (tokens.length > 0 && Basic.isInteger(tokens[0]))
                    gotoLine(Basic.parseInt(tokens[0]), tokens.length == 2 && Basic.isInteger(tokens[1]) ? Basic.parseInt(tokens[1]) : 0);
            }
        });

        controller.getIncreaseFontSizeMenuItem().setOnAction((x) -> textArea.setStyle(String.format("-fx-font-size: %.0f;", (textArea.getFont().getSize() + 2))));
        controller.getDecreaseFontSizeMenuItem().setOnAction((x) -> {
            if (textArea.getFont().getSize() > 2)
                textArea.setStyle(String.format("-fx-font-size: %.0f;", (textArea.getFont().getSize() - 2)));
        });

        controller.getResetMenuItem().setOnAction((x) -> textArea.setStyle("-fx-font-size: 12;"));

        controller.getWrapTextMenuItem().selectedProperty().unbind();
        controller.getWrapTextMenuItem().setSelected(textArea.isWrapText());
        controller.getWrapTextMenuItem().selectedProperty().bindBidirectional(textArea.wrapTextProperty());
        controller.getWrapTextMenuItem().disableProperty().bind(new SimpleBooleanProperty(false));
    }

    public TextArea getTextArea() {
        return textArea;
    }
}
