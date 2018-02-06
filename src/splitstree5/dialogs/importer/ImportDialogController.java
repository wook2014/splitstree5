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

package splitstree5.dialogs.importer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/**
 * import dialog controller
 * Daniel Huson, 1.2018
 */
public class ImportDialogController {

    @FXML
    private FlowPane progressBarPane;

    @FXML
    private Button cancelButton;

    @FXML
    private Button importButton;

    @FXML
    private VBox mainVBox;

    @FXML
    private TextField fileTextField;

    @FXML
    private Button browseButton;

    @FXML
    private ComboBox<String> dataTypeComboBox;

    @FXML
    private ComboBox<String> fileFormatComboBox;

    public FlowPane getProgressBarPane() {
        return progressBarPane;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Button getImportButton() {
        return importButton;
    }

    public VBox getMainVBox() {
        return mainVBox;
    }

    public TextField getFileTextField() {
        return fileTextField;
    }

    public Button getBrowseButton() {
        return browseButton;
    }

    public ComboBox<String> getDataTypeComboBox() {
        return dataTypeComboBox;
    }

    public ComboBox<String> getFileFormatComboBox() {
        return fileFormatComboBox;
    }
}
