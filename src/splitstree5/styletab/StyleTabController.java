/*
 *  Copyright (C) 2016 Daniel H. Huson
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

package splitstree5.styletab;

import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import splitstree5.xtra.FontPicker;

public class StyleTabController {

    @FXML
    private Pane fontComboBoxPane;

    @FXML
    private ComboBoxBase<Font> fontComboBox;

    @FXML
    private ColorPicker textColorChooser;

    @FXML
    private ColorPicker lineColorChooser;

    @FXML
    private ColorPicker nodeColorPicker;

    @FXML
    private ComboBox<?> nodeShapeComboBox;

    @FXML
    private TextField lineWidthField;

    @FXML
    private TextField nodeHeightTextField;

    @FXML
    private TextField nodeWidthTextField;

    private FontPicker fontPicker;

    @FXML
    void initialize() {
        fontComboBoxPane.getChildren().remove(fontComboBox);
        fontPicker = new FontPicker(Font.getDefault());
        fontComboBoxPane.getChildren().add(fontPicker);
    }

    public FontPicker getFontComboBox() {
        return fontPicker;
    }

    public ColorPicker getTextColorChooser() {
        return textColorChooser;
    }

    public ColorPicker getLineColorChooser() {
        return lineColorChooser;
    }

    public ColorPicker getNodeColorPicker() {
        return nodeColorPicker;
    }

    public ComboBox<?> getNodeShapeComboBox() {
        return nodeShapeComboBox;
    }

    public TextField getLineWidthField() {
        return lineWidthField;
    }

    public TextField getNodeWidthTextField() {
        return nodeWidthTextField;
    }

    public TextField getNodeHeightTextField() {
        return nodeHeightTextField;
    }
}
