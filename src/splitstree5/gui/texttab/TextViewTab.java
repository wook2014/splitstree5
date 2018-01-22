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

package splitstree5.gui.texttab;


import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.text.Font;
import javafx.stage.StageStyle;
import jloda.util.Basic;
import splitstree5.gui.ViewerTab;
import splitstree5.menu.MenuController;
import splitstree5.utils.Print;

import java.util.Optional;

/**
 * text view tab for displaying ummutable text
 * Daniel Huson, 1.2018
 */
public class TextViewTab extends ViewerTab {
    private final TextArea textArea;

    /**
     * constructor
     *
     * @param nameProperty
     */
    public TextViewTab(ReadOnlyStringProperty nameProperty) {
        final Label label = new Label();
        label.textProperty().bind(nameProperty);
        setGraphic(label);
        setText("");
        textArea = new TextArea();
        textArea.setFont(Font.font("Courier New"));
        textArea.setEditable(false);
        setContent(textArea);
        setClosable(true);
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
        textArea.setFont(Font.font("Courier New"));
        textArea.setEditable(false);
        textArea.textProperty().bind(textProperty);
        textArea.textProperty().addListener((InvalidationListener) -> getUndoManager().clear());
        setContent(textArea);
        setClosable(true);
    }

    public void gotoLine(int lineNumber) {
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
            textArea.selectRange(start, end);
        }
    }

    @Override
    public void updateMenus(MenuController controller) {
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

        controller.getCopyMenuItem().setOnAction((e) -> textArea.copy());
        controller.getCopyMenuItem().disableProperty().bind(textArea.selectedTextProperty().length().isEqualTo(0));

        controller.getSelectAllMenuItem().setOnAction((e) -> textArea.selectAll());
        controller.getSelectNoneMenuItem().setOnAction((e) -> textArea.selectHome());
        controller.getSelectNoneMenuItem().disableProperty().bind(textArea.selectedTextProperty().length().isEqualTo(0));

        controller.getGotoLineMenuItem().setOnAction((e) -> {
            final TextInputDialog dialog = new TextInputDialog("");
            dialog.setTitle("Go to Line - SplitsTree5");
            dialog.initStyle(StageStyle.UTILITY);
            dialog.setX(getMainWindow().getStage().getX() + 0.5 * getMainWindow().getStage().getWidth());
            dialog.setY(getMainWindow().getStage().getY() + 0.5 * getMainWindow().getStage().getHeight());
            dialog.setHeaderText("Select line by number");
            dialog.setContentText("Line number:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> gotoLine(Basic.parseInt(result.get())));
        });

        controller.getIncreaseFontSizeMenuItem().setOnAction((x) -> textArea.setStyle(String.format("-fx-font-size: %.0f;", (textArea.getFont().getSize() + 2))));
        controller.getDecreaseFontSizeMenuItem().setOnAction((x) -> textArea.setStyle(String.format("-fx-font-size: %.0f;", (textArea.getFont().getSize() - 2))));
    }

    public TextArea getTextArea() {
        return textArea;
    }
}
