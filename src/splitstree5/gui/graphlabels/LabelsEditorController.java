package splitstree5.gui.graphlabels;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class LabelsEditorController {

    @FXML
    private Button closeButton;

    @FXML
    private CheckBox bold;

    @FXML
    private Button applyStyle;

    @FXML
    private StackPane area;

    public Button getCloseButton() {
        return closeButton;
    }

    public CheckBox getBold() {
        return bold;
    }

    public Button getApplyStyle() {
        return applyStyle;
    }

    public StackPane getArea() {
        return area;
    }
}
