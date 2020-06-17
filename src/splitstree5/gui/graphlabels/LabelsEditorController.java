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
import javafx.scene.control.*;
import javafx.scene.web.HTMLEditor;

public class LabelsEditorController {

    @FXML
    private CheckBox findAll;
    @FXML
    private Button updateHTML;
    @FXML
    private Button updateView;
    @FXML
    private HTMLEditor htmlEditor;
    @FXML
    private Button applyStyle;
    @FXML
    private Button apply2all;
    @FXML
    private TextArea html_area;
    @FXML
    private Button reset;
    @FXML
    private Separator separator;

    public Button getApplyStyle() {
        return applyStyle;
    }

    public Button getApply2all() {
        return apply2all;
    }

    public Button getUpdateHTMLButton() {
        return updateHTML;
    }

    public Button getUpdateView() {
        return updateView;
    }

    public HTMLEditor getHtmlEditor() {
        return htmlEditor;
    }

    public TextArea getHTML_Area() {
        return html_area;
    }

    public CheckBox getFindAll() {
        return findAll;
    }

    public Button getReset() {
        return reset;
    }

    public Separator getSeparator() {
        return separator;
    }
}
