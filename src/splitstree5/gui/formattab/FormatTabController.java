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

package splitstree5.gui.formattab;

import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.layout.Pane;
import jloda.util.ProgramProperties;
import splitstree5.gui.formattab.fontselector.FontSelector;

public class FormatTabController {

    @FXML
    private Pane fontComboBoxPane;

    @FXML
    private ComboBoxBase<?> fontComboBox;

    @FXML
    private ColorPicker labelColorPicker;

    @FXML
    private ColorPicker edgeColorPicker;

    @FXML
    private ColorPicker nodeColorPicker;

    @FXML
    private ComboBox<NodeShape> nodeShapeComboBox;

    @FXML
    private ComboBox<Integer> edgeWidthComboBox;

    @FXML
    private ComboBox<Integer> nodeWidthComboBox;

    @FXML
    private ComboBox<Integer> nodeHeightComboBox;

    private FontSelector fontPicker;

    @FXML
    void initialize() {
        fontComboBoxPane.getChildren().remove(fontComboBox);
        fontPicker = new FontSelector(ProgramProperties.getDefaultFont());
        fontComboBoxPane.getChildren().add(fontPicker);
    }

    public FontSelector getFontComboBox() {
        return fontPicker;
    }

    public ColorPicker getLabelColorPicker() {
        return labelColorPicker;
    }

    public ColorPicker getEdgeColorPicker() {
        return edgeColorPicker;
    }

    public ColorPicker getNodeColorPicker() {
        return nodeColorPicker;
    }

    public ComboBox<NodeShape> getNodeShapeComboBox() {
        return nodeShapeComboBox;
    }

    public ComboBox<Integer> getEdgeWidthComboBox() {
        return edgeWidthComboBox;
    }

    public ComboBox<Integer> getNodeWidthComboBox() {
        return nodeWidthComboBox;
    }

    public ComboBox<Integer> getNodeHeightComboBox() {
        return nodeHeightComboBox;
    }
}
