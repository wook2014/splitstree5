package splitstree5.gui.graphlabels;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.HTMLEditor;

public class LabelsEditorController {

    @FXML
    private Button b;
    @FXML
    private Button i;
    @FXML
    private Button u;
    @FXML
    private Button openFile;
    @FXML
    private ColorPicker textColor;

    @FXML
    private HTMLEditor htmlEditor;

    @FXML
    private CheckBox bold;

    @FXML
    private Button applyStyle;

    @FXML
    private StackPane area;

    @FXML
    private TextArea html_area;

    public Button getBoldButton() {
        return b;
    }
    public Button getItalicButton() {
        return i;
    }
    public Button getUnderlineButton() {
        return u;
    }
    public Button getOpenFileButton() {
        return openFile;
    }
    public ColorPicker getTextColor() {
        return textColor;
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

    public HTMLEditor getHtmlEditor() {
        return htmlEditor;
    }

    public TextArea getHTML_Area() {
        return html_area;
    }
}
