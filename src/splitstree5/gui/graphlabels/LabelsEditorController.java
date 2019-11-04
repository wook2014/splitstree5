package splitstree5.gui.graphlabels;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
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

    @FXML
    private TextArea html_area;

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

    public TextArea getHTML_Area() {
        return html_area;
    }
}
