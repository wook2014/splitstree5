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

package splitstree5.gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import splitstree5.core.misc.Taxon;

/**
 * controller
 * Created by huson on 12/23/16.
 */
public class TaxaFilterController {
    @FXML // fx:id="activeList"
    private ListView<Taxon> activeList; // Value injected by FXMLLoader

    @FXML // fx:id="inactivateSelectedButton"
    private Button inactivateSelectedButton; // Value injected by FXMLLoader

    @FXML // fx:id="inactivateAllButton"
    private Button inactivateAllButton; // Value injected by FXMLLoader

    @FXML // fx:id="activateSelectedButton"
    private Button activateSelectedButton; // Value injected by FXMLLoader

    @FXML // fx:id="activateAllButton"
    private Button activateAllButton; // Value injected by FXMLLoader

    @FXML // fx:id="inactiveList"
    private ListView<Taxon> inactiveList; // Value injected by FXMLLoader

    @FXML // fx:id="aboutMenuItem"
    private MenuItem aboutMenuItem; // Value injected by FXMLLoader

    @FXML // fx:id="closeMenuItem"
    private MenuItem closeMenuItem; // Value injected by FXMLLoader

    @FXML // fx:id="undoMenuItem"
    private MenuItem undoMenuItem; // Value injected by FXMLLoader

    @FXML // fx:id="redoMenuItem"
    private MenuItem redoMenuItem; // Value injected by FXMLLoader

    @FXML // fx:id="cancelButton"
    private Button cancelButton; // Value injected by FXMLLoader

    @FXML // fx:id="applyButton"
    private Button applyButton; // Value injected by FXMLLoader


    @FXML
        // This method is called by the FXMLLoader when initialization is complete
        // we do low level stuff here
    void initialize() {
        activeList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        inactiveList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        activeList.setPlaceholder(new Label("- Empty -"));
        inactiveList.setPlaceholder(new Label("- Empty -"));
    }

    public ListView<Taxon> getActiveList() {
        return activeList;
    }

    public ListView<Taxon> getInactiveList() {
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

    public MenuItem getUndoMenuItem() {
        return undoMenuItem;
    }

    public MenuItem getRedoMenuItem() {
        return redoMenuItem;
    }

    public MenuItem getCloseMenuItem() {
        return closeMenuItem;
    }

    public MenuItem getAboutMenuItem() {
        return aboutMenuItem;
    }

    public Button getApplyButton() {
        return applyButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }
}
