/*
 * MainToolBarController.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.toolbar;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;

import java.util.HashSet;
import java.util.Set;

/**
 * main toolbar controller
 * Daniel Huson, 1.2018
 */
public class MainToolBarController {
    private final Set<Button> alwaysEnabled = new HashSet<>();

    @FXML
    private ToolBar toolBar;

    @FXML
    private Button openCloseLeft;

    @FXML
    private Button openButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button printButton;

    @FXML
    private Button findButton;

    @FXML
    private Button zoomButton;

    @FXML
    private Button zoomInButton;

    @FXML
    private Button zoomOutButton;

    @FXML
    private Button rotateLeftButton;

    @FXML
    private Button rotateRightButton;

    @FXML
    private Button treeButton;


    @FXML
    private Button networkButton;

    public ToolBar getToolBar() {
        return toolBar;
    }

    @FXML
    private void initialize() {
        for (Node item : toolBar.getItems()) {
            if (item instanceof Button && ((Button) item).getGraphic() != null) {
                ((Button) item).setText(null);
            }
        }
        alwaysEnabled.add(openCloseLeft);
    }

    public Button getOpenCloseLeft() {
        return openCloseLeft;
    }

    public Button getOpenButton() {
        return openButton;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public Button getPrintButton() {
        return printButton;
    }

    public Button getFindButton() {
        return findButton;
    }

    public Button getZoomButton() {
        return zoomButton;
    }

    public Button getZoomInButton() {
        return zoomInButton;
    }

    public Button getZoomOutButton() {
        return zoomOutButton;
    }

    public Button getRotateLeftButton() {
        return rotateLeftButton;
    }

    public Button getRotateRightButton() {
        return rotateRightButton;
    }

    public Button getTreeButton() {
        return treeButton;
    }

    public Button getNetworkButton() {
        return networkButton;
    }
}