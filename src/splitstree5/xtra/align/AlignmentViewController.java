/*
 * AlignmentViewController.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.xtra.align;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import jloda.util.Pair;

/**
 * alignment view controller
 * Daniel Huson, 2.2018
 */
public class AlignmentViewController {

    @FXML
    private ToolBar toolBar;

    @FXML
    private ComboBox<?> colorSchemeComboBox;

    @FXML
    private MenuItem selectConstantMenuItem;

    @FXML
    private MenuItem selectUniformativeMenuItem;

    @FXML
    private MenuItem selectCodon1MenuItem;

    @FXML
    private MenuItem selectCodon2MenuItem;

    @FXML
    private MenuItem selectCodon3MenuItem;

    @FXML
    private TableView<Pair<String, String>> alignmentTableView;

    @FXML
    private void initialize() {

    }

    public ToolBar getToolBar() {
        return toolBar;
    }

    public ComboBox<?> getColorSchemeComboBox() {
        return colorSchemeComboBox;
    }

    public MenuItem getSelectConstantMenuItem() {
        return selectConstantMenuItem;
    }

    public MenuItem getSelectUniformativeMenuItem() {
        return selectUniformativeMenuItem;
    }

    public MenuItem getSelectCodon1MenuItem() {
        return selectCodon1MenuItem;
    }

    public MenuItem getSelectCodon2MenuItem() {
        return selectCodon2MenuItem;
    }

    public MenuItem getSelectCodon3MenuItem() {
        return selectCodon3MenuItem;
    }


    public TableView<Pair<String, String>> getAlignmentTableView() {
        return alignmentTableView;
    }
}
