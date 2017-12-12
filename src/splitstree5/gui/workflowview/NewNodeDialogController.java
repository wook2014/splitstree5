/*
 *  Copyright (C) 2017 Daniel H. Huson
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

package splitstree5.gui.workflowview;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.ADataBlock;

public class NewNodeDialogController {

    @FXML
    private ChoiceBox<ADataBlock> targetDataChoiceBox;

    @FXML
    private ChoiceBox<Algorithm> algorithmChoiceBox;

    @FXML
    private Button cancelButton;

    @FXML
    private Button applyButton;

    @FXML
    private Label sourceDataLabel;

    public ChoiceBox<ADataBlock> getTargetDataChoiceBox() {
        return targetDataChoiceBox;
    }

    public ChoiceBox<? extends Algorithm> getAlgorithmChoiceBox() {
        return algorithmChoiceBox;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Button getApplyButton() {
        return applyButton;
    }

    public Label getSourceDataLabel() {
        return sourceDataLabel;
    }
}
