/*
 * CompareGenomesController.java Copyright (C) 2020. Daniel H. Huson
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
 *
 */
package splitstree5.dialogs.genome;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class CompareGenomesController {

    @FXML
    private TextArea inputTextArea;

    @FXML
    private Button inputBrowseButton;

    @FXML
    private ChoiceBox<CompareGenomesDialog.TaxonIdentification> taxaChoiceBox;

    @FXML
    private ChoiceBox<CompareGenomesDialog.Method> methodChoiceBox;

    @FXML
    private ChoiceBox<CompareGenomesDialog.Sequence> sequenceTypeChoiceBox;

    @FXML
    private TextField minLengthTextField;

    @FXML
    private TextField outputFileTextField;

    @FXML
    private Button outputBrowseButton;

    @FXML
    private TextField mashKTextField;

    @FXML
    private TextField mashSTextField;

    @FXML
    private RadioButton mashPhylogeneticRadioButton;

    @FXML
    private RadioButton mashJaccardRadioButton;

    @FXML
    private TextField dashingPrefixLengthTextField;

    @FXML
    private TextField dashingKmerTextField;

    @FXML
    private Tab taxonLabelsTab;

    @FXML
    private ListView<String> displayLabelsListView;

    @FXML
    private VBox displayLabelsVBox;

    @FXML
    private FlowPane statusFlowPane;

    @FXML
    private Button cancelButton;

    @FXML
    private Button applyButton;

    @FXML
    private Button labelsUndoButton;

    @FXML
    private Button labelsRedoButton;

    public TextArea getInputTextArea() {
        return inputTextArea;
    }

    public Button getInputBrowseButton() {
        return inputBrowseButton;
    }

    public ChoiceBox<CompareGenomesDialog.TaxonIdentification> getTaxaChoiceBox() {
        return taxaChoiceBox;
    }

    public TextField getOutputFileTextField() {
        return outputFileTextField;
    }

    public TextField getMinLengthTextField() {
        return minLengthTextField;
    }

    public Button getOutputBrowseButton() {
        return outputBrowseButton;
    }

    public ChoiceBox<CompareGenomesDialog.Method> getMethodChoiceBox() {
        return methodChoiceBox;
    }

    public ChoiceBox<CompareGenomesDialog.Sequence> getSequenceTypeChoiceBox() {
        return sequenceTypeChoiceBox;
    }

    public TextField getMashKTextField() {
        return mashKTextField;
    }

    public TextField getMashSTextField() {
        return mashSTextField;
    }

    public RadioButton getMashPhylogeneticRadioButton() {
        return mashPhylogeneticRadioButton;
    }

    public RadioButton getMashJaccardRadioButton() {
        return mashJaccardRadioButton;
    }

    public TextField getDashingPrefixLengthTextField() {
        return dashingPrefixLengthTextField;
    }

    public TextField getDashingKmerTextField() {
        return dashingKmerTextField;
    }

    public FlowPane getStatusFlowPane() {
        return statusFlowPane;
    }

    public Tab getTaxonLabelsTab() {
        return taxonLabelsTab;
    }

    public ListView<String> getDisplayLabelsListView() {
        return displayLabelsListView;
    }

    public VBox getDisplayLabelsVBox() {
        return displayLabelsVBox;
    }


    public Button getCancelButton() {
        return cancelButton;
    }

    public Button getApplyButton() {
        return applyButton;
    }

    public Button getLabelsUndoButton() {
        return labelsUndoButton;
    }

    public Button getLabelsRedoButton() {
        return labelsRedoButton;
    }
}
