/*
 * TreeFilterPaneController.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.gui.algorithmtab.treefilterview;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.HBox;

/**
 * Tree filter controller
 * Daniel Huson, 12/23/16.
 */
public class TreeFilterPaneController {

    @FXML
    private HBox hbox;

    @FXML
    private ListView<String> activeList;

    @FXML
    private Button inactivateSelectedButton;

    @FXML
    private Button inactivateAllButton;

    @FXML
    private Button activateSelectedButton;

    @FXML
    private Button activateAllButton;

    @FXML
    private ListView<String> inactiveList;

    @FXML
    private Button selectFullTrees;

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
        // we do low level stuff here
    void initialize() {
        activeList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        inactiveList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        activeList.setPlaceholder(new Label("- Empty -"));
        inactiveList.setPlaceholder(new Label("- Empty -"));
    }

    public ListView<String> getActiveList() {
        return activeList;
    }

    public ListView<String> getInactiveList() {
        return inactiveList;
    }

    public Button getInactivateSelectedButton() {
        return inactivateSelectedButton;
    }

    public Button getInactivateAllButton() {
        return inactivateAllButton;
    }

    public Button getActivateSelectedButton() {
        return activateSelectedButton;
    }

    public Button getActivateAllButton() {
        return activateAllButton;
    }

    public Button getSelectFullTrees() {
        return selectFullTrees;
    }
}
