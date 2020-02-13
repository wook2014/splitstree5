/*
 * LabelsEditorController.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.gui.graphlabels;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.web.HTMLEditor;

public class LabelsEditorController {

    @FXML
    private Button b;
    @FXML
    private Button i;
    @FXML
    private Button search;
    @FXML
    private Button updateHTML;
    @FXML
    private ColorPicker textColor;

    @FXML
    private HTMLEditor htmlEditor;

    @FXML
    private CheckBox bold;

    @FXML
    private Button applyStyle;

    @FXML
    private StackPane area;

    @FXML
    private TextArea html_area;

    public Button getBoldButton() {
        return b;
    }

    public Button getItalicButton() {
        return i;
    }

    public ColorPicker getTextColor() {
        return textColor;
    }

    public CheckBox getBold() {
        return bold;
    }

    public Button getApplyStyle() {
        return applyStyle;
    }

    public Button getUpdateHTMLButton() {
        return updateHTML;
    }

    public StackPane getArea() {
        return area;
    }

    public HTMLEditor getHtmlEditor() {
        return htmlEditor;
    }

    public TextArea getHTML_Area() {
        return html_area;
    }

    public Button getSearch() {
        return search;
    }
}
