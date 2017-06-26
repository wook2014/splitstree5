package splitstree5.gui.dagview;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
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
    private MenuItem deleteMenuItem;

    @FXML
    private MenuItem undoMenuItem;

    @FXML
    private MenuItem redoMenuItem;

    @FXML
    private TextArea methodTextArea;

    public Button getDoneButton() {
        return doneButton;
    }

    public Pane getCenterPane() {
        return centerPane;
    }

    public MenuItem getDeleteMenuItem() {
        return deleteMenuItem;
    }

    public MenuItem getUndoMenuItem() {
        return undoMenuItem;
    }

    public MenuItem getRedoMenuItem() {
        return redoMenuItem;
    }

    public TextArea getMethodTextArea() {
        return methodTextArea;
    }
}