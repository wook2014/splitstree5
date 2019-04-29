/*
 *  ExportDialogController.java Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.dialogs.exporter;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

public class ExportDialogController {

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Button optionsButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button exportButton;

    @FXML
    private VBox mainVBox;

    @FXML
    private ComboBox<String> dataTypeComboBox;

    @FXML
    private ComboBox<String> fileFormatComboBox;

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public Button getOptionsButton() {
        return optionsButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Button getExportButton() {
        return exportButton;
    }

    public VBox getMainVBox() {
        return mainVBox;
    }

    public ComboBox<String> getDataTypeComboBox() {
        return dataTypeComboBox;
    }

    public ComboBox<String> getFileFormatComboBox() {
        return fileFormatComboBox;
    }
}
