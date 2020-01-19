package splitstree5.treebased.editor;

import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import jloda.util.ProgramProperties;

import java.net.URL;
import java.util.ResourceBundle;

public class RootedNetworkEditorController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private VBox topVBox;

    @FXML
    private MenuBar menuBar;

    @FXML
    private Menu fileMenu;

    @FXML
    private MenuItem closeMenuItem;

    @FXML
    private Menu editMenu;

    @FXML
    private MenuItem undoMenuItem;

    @FXML
    private MenuItem redoMenuItem;

    @FXML
    private MenuItem cutMenuItem;

    @FXML
    private MenuItem copyMenuItem;

    @FXML
    private MenuItem copyNewickMenuItem;

    @FXML
    private MenuItem pasteMenuItem;

    @FXML
    private MenuItem deleteMenuItem;

    @FXML
    private MenuItem selectAllMenuItem;

    @FXML
    private MenuItem selectNoneMenuItem;

    @FXML
    private MenuItem selectInvertMenuItem;

    @FXML
    private MenuItem selectLeavesMenuItem;

    @FXML
    private MenuItem selectTreeNodesMenuItem;

    @FXML
    private MenuItem selectReticulateNodesMenuitem;

    @FXML
    private MenuItem selectAllBelowMenuItem;

    @FXML
    private MenuItem selectAllAboveMenuItem;

    @FXML
    private Menu helpMenu;

    @FXML
    private MenuItem aboutMenuItem;

    @FXML
    private ToolBar toolBar;

    @FXML
    private FlowPane statusFlowPane;

    @FXML
    private StackPane stackPane;

    @FXML
    private Pane mainPane;

    @FXML
    void initialize() {
        assert topVBox != null : "fx:id=\"topVBox\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert menuBar != null : "fx:id=\"menuBar\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert fileMenu != null : "fx:id=\"fileMenu\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert closeMenuItem != null : "fx:id=\"closeMenuItem\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert editMenu != null : "fx:id=\"editMenu\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert undoMenuItem != null : "fx:id=\"undoMenuItem\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert redoMenuItem != null : "fx:id=\"redoMenuItem\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert cutMenuItem != null : "fx:id=\"cutMenuItem\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert copyMenuItem != null : "fx:id=\"copyMenuItem\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert copyNewickMenuItem != null : "fx:id=\"copyNewickMenuItem\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert pasteMenuItem != null : "fx:id=\"pasteMenuItem\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert deleteMenuItem != null : "fx:id=\"deleteMenuItem\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert selectAllMenuItem != null : "fx:id=\"selectAllMenuItem\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert selectNoneMenuItem != null : "fx:id=\"selectNoneMenuItem\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert selectInvertMenuItem != null : "fx:id=\"selectInvertMenuItem\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert selectLeavesMenuItem != null : "fx:id=\"selectLeavesMenuItem\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert selectTreeNodesMenuItem != null : "fx:id=\"selectTreeNodesMenuItem\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert selectReticulateNodesMenuitem != null : "fx:id=\"selectReticulateNodesMenuitem\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert selectAllBelowMenuItem != null : "fx:id=\"selectAllBelowMenuItem\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert selectAllAboveMenuItem != null : "fx:id=\"selectAllAboveMenuItem\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert helpMenu != null : "fx:id=\"helpMenu\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert aboutMenuItem != null : "fx:id=\"aboutMenuItem\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert toolBar != null : "fx:id=\"toolBar\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert statusFlowPane != null : "fx:id=\"statusFlowPane\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert stackPane != null : "fx:id=\"stackPane\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";
        assert mainPane != null : "fx:id=\"mainPane\" was not injected: check your FXML file 'RootedNetworkEditor.fxml'.";

        if (ProgramProperties.isMacOS()) {
            getMenuBar().setUseSystemMenuBar(true);
            //fileMenu.getItems().remove(getQuitMenuItem());
            // windowMenu.getItems().remove(getAboutMenuItem());
            //editMenu.getItems().remove(getPreferencesMenuItem());
        }
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public Menu getFileMenu() {
        return fileMenu;
    }

    public MenuItem getCloseMenuItem() {
        return closeMenuItem;
    }

    public Menu getEditMenu() {
        return editMenu;
    }

    public MenuItem getUndoMenuItem() {
        return undoMenuItem;
    }

    public MenuItem getRedoMenuItem() {
        return redoMenuItem;
    }

    public MenuItem getCutMenuItem() {
        return cutMenuItem;
    }

    public MenuItem getCopyMenuItem() {
        return copyMenuItem;
    }

    public MenuItem getCopyNewickMenuItem() {
        return copyNewickMenuItem;
    }

    public MenuItem getPasteMenuItem() {
        return pasteMenuItem;
    }

    public MenuItem getDeleteMenuItem() {
        return deleteMenuItem;
    }

    public MenuItem getSelectAllMenuItem() {
        return selectAllMenuItem;
    }

    public MenuItem getSelectNoneMenuItem() {
        return selectNoneMenuItem;
    }

    public MenuItem getSelectInvertMenuItem() {
        return selectInvertMenuItem;
    }

    public MenuItem getSelectLeavesMenuItem() {
        return selectLeavesMenuItem;
    }

    public MenuItem getSelectTreeNodesMenuItem() {
        return selectTreeNodesMenuItem;
    }

    public MenuItem getSelectReticulateNodesMenuitem() {
        return selectReticulateNodesMenuitem;
    }

    public MenuItem getSelectAllBelowMenuItem() {
        return selectAllBelowMenuItem;
    }

    public MenuItem getSelectAllAboveMenuItem() {
        return selectAllAboveMenuItem;
    }

    public Menu getHelpMenu() {
        return helpMenu;
    }

    public MenuItem getAboutMenuItem() {
        return aboutMenuItem;
    }

    public ToolBar getToolBar() {
        return toolBar;
    }

    public FlowPane getStatusFlowPane() {
        return statusFlowPane;
    }

    public StackPane getStackPane() {
        return stackPane;
    }

    public Pane getMainPane() {
        return mainPane;
    }
}