package splitstree5.gui.dagview;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;

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
    private FlowPane flowPane;


    @FXML
    void initialize() {
        assert doneButton != null : "fx:id=\"doneButton\" was not injected: check your FXML file 'DAGView.fxml'.";
        assert flowPane != null : "fx:id=\"flowPane\" was not injected: check your FXML file 'DAGView.fxml'.";
    }

    public Button getDoneButton() {
        return doneButton;
    }

    public FlowPane getFlowPane() {
        return flowPane;
    }
}
