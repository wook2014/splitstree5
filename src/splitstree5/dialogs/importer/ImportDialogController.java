/*
 * ImportDialogController.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.dialogs.importer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import splitstree5.core.datablocks.characters.CharactersType;

/**
 * import dialog controller
 * Daniel Huson, 1.2018
 */
public class ImportDialogController {

    @FXML
    private FlowPane progressBarPane;

    @FXML
    private Button closeButton;

    @FXML
    private Button importButton;

    @FXML
    private VBox mainVBox;

    @FXML
    private TextField fileTextField;

    @FXML
    private Button browseButton;

    @FXML
    private ComboBox<ImporterManager.DataType> dataTypeComboBox;

    @FXML
    private ComboBox<CharactersType> charactersTypeCBox;

    @FXML
    private ComboBox<String> fileFormatComboBox;

    @FXML
    private Tab otherTab;

    @FXML
    private Tab charactersTab;

    @FXML
    private Tab distancesTab;

    @FXML
    private Tab treesTab;

    /*
    IMPORT SETTINGS
     */

    @FXML
    private TextField gapInput;

    @FXML
    private TextField missingInput;

    @FXML
    private TextField matchInput;

    @FXML
    private CheckBox similarityValues;

    @FXML
    private CheckBox innerNodesLabeling;

    @FXML
    private ComboBox<String> similarityCalculation;

    public FlowPane getProgressBarPane() {
        return progressBarPane;
    }

    public Button getCancelButton() {
        return closeButton;
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

    public ComboBox<ImporterManager.DataType> getDataTypeComboBox() {
        return dataTypeComboBox;
    }

    public ComboBox<String> getFileFormatComboBox() {
        return fileFormatComboBox;
    }

    /*
    IMPORT SETTINGS
     */

    public Button getCloseButton() {
        return closeButton;
    }

    public ComboBox<CharactersType> getCharactersTypeCBox() {
        return charactersTypeCBox;
    }

    public Tab getOtherTab() {
        return otherTab;
    }

    public Tab getCharactersTab() {
        return charactersTab;
    }

    public Tab getDistancesTab() {
        return distancesTab;
    }

    public Tab getTreesTab() {
        return treesTab;
    }

    public TextField getGapInput() {
        return gapInput;
    }

    public TextField getMatchInput() {
        return matchInput;
    }

    public TextField getMissingInput() {
        return missingInput;
    }

    public CheckBox getSimilarityValues() {
        return similarityValues;
    }

    public CheckBox getInnerNodesLabeling() {
        return innerNodesLabeling;
    }

    public ComboBox<String> getSimilarityCalculation() {
        return similarityCalculation;
    }
}
