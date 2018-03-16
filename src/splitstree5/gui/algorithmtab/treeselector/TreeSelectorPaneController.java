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

package splitstree5.gui.algorithmtab.treeselector;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;


/**
 * tree selector pane controller
 * Daniel Huson, 3.2018
 */
public class TreeSelectorPaneController {

    @FXML
    private TextField treeIdTextField;

    @FXML
    private Label label;

    @FXML
    private Button gotoFirstButton;

    @FXML
    private Button gotoPreviousButton;

    @FXML
    private Button gotoNextButton;

    @FXML
    private Button gotoLastButton;

    public TextField getTreeIdTextField() {
        return treeIdTextField;
    }

    public Label getLabel() {
        return label;
    }

    public Button getGotoFirstButton() {
        return gotoFirstButton;
    }

    public Button getGotoPreviousButton() {
        return gotoPreviousButton;
    }

    public Button getGotoNextButton() {
        return gotoNextButton;
    }

    public Button getGotoLastButton() {
        return gotoLastButton;
    }
}
