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

package splitstree5.gui.connectorview;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import splitstree5.core.algorithms.Algorithm;

/**
 * connector view controller
 * Daniel Huson 12/2016
 */
public class ConnectorViewController {

    @FXML
    private Button cancelButton;

    @FXML
    private Button applyButton;

    @FXML
    private ComboBox<Algorithm> algorithmComboBox;

    @FXML
    private BorderPane borderPane;

    @FXML
    private Pane centerPane;

    @FXML
    private TextField statusBar;

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
        // we do low level stuff here
    void initialize() {
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Button getApplyButton() {
        return applyButton;
    }

    public ComboBox<Algorithm> getAlgorithmComboBox() {
        return algorithmComboBox;
    }

    public BorderPane getBorderPane() {
        return borderPane;
    }

    public Pane getCenterPane() {
        return centerPane;
    }

    public TextField getStatusBar() {
        return statusBar;
    }
}
