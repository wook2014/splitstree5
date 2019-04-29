/*
 *  AlgorithmTabController.java Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.gui.algorithmtab;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import splitstree5.core.algorithms.Algorithm;

/**
 * Algorithm tab controller
 * Daniel Huson 1/2018
 */
public class AlgorithmTabController {
    @FXML
    private Button applyButton;

    @FXML
    private ComboBox<Algorithm> algorithmComboBox;

    @FXML
    private BorderPane borderPane;

    @FXML
    private Pane centerPane;

    @FXML
    void initialize() {
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
}
