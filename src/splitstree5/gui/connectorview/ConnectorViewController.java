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

package splitstree5.gui.connectorview;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import splitstree5.core.algorithms.Algorithm;

/**
 * connector view controller
 * Created by huson on 12/31/16.
 */
public class ConnectorViewController {

    @FXML
    private Button resetButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button applyButton;

    @FXML
    private MenuItem closeMenuItem;

    @FXML
    private MenuItem undoMenuItem;

    @FXML
    private MenuItem redoMenuItem;

    @FXML
    private MenuItem aboutMenuItem;

    @FXML
    private ChoiceBox<Algorithm> algorithmChoiceBox;

    @FXML
    private Pane centerPane;

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
        // we do low level stuff here
    void initialize() {
    }

    public Button getResetButton() {
        return resetButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Button getApplyButton() {
        return applyButton;
    }

    public MenuItem getCloseMenuItem() {
        return closeMenuItem;
    }

    public MenuItem getUndoMenuItem() {
        return undoMenuItem;
    }

    public MenuItem getRedoMenuItem() {
        return redoMenuItem;
    }

    public MenuItem getAboutMenuItem() {
        return aboutMenuItem;
    }

    public ChoiceBox<Algorithm> getAlgorithmChoiceBox() {
        return algorithmChoiceBox;
    }

    public Pane getCenterPane() {
        return centerPane;
    }
}
