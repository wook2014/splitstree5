package splitstree5.gui.graphlabels;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;

public class LabelsEditorController {

    @FXML
    private Button closeButton;

    @FXML
    private CheckBox bold;

    @FXML
    private TextArea text;

    @FXML
    private Button applyStyle;

    public Button getCloseButton() {
        return closeButton;
    }

    public CheckBox getBold() {
        return bold;
    }

    public TextArea getText() {
        return text;
    }

    public Button getApplyStyle() {
        return applyStyle;
    }
}
