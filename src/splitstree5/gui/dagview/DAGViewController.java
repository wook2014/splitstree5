package splitstree5.gui.dagview;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class DAGViewController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button doneButton;

    @FXML
    private Pane centerPane;

    @FXML
    void initialize() {
        // reset these things so that button and menu are on top of center pane
    }

    public Button getDoneButton() {
        return doneButton;
    }

    public Pane getCenterPane() {
        return centerPane;
    }
}