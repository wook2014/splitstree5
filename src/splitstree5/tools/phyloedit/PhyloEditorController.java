/*
 *  PhyloEditorController.java Copyright (C) 2020 Daniel H. Huson
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
 */

package splitstree5.tools.phyloedit;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import jloda.util.ProgramProperties;

import java.net.URL;
import java.util.ResourceBundle;

public class PhyloEditorController {

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
    private Button copyNewickButton;

    @FXML
    private FlowPane statusFlowPane;

    @FXML
    private StackPane stackPane;

    @FXML
    private Pane mainPane;

    @FXML
    void initialize() {
        assert topVBox != null : "fx:id=\"topVBox\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert menuBar != null : "fx:id=\"menuBar\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert fileMenu != null : "fx:id=\"fileMenu\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert closeMenuItem != null : "fx:id=\"closeMenuItem\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert editMenu != null : "fx:id=\"editMenu\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert undoMenuItem != null : "fx:id=\"undoMenuItem\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert redoMenuItem != null : "fx:id=\"redoMenuItem\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert cutMenuItem != null : "fx:id=\"cutMenuItem\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert copyMenuItem != null : "fx:id=\"copyMenuItem\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert copyNewickMenuItem != null : "fx:id=\"copyNewickMenuItem\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert copyNewickButton != null : "fx:id=\"copyNewickButton\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert pasteMenuItem != null : "fx:id=\"pasteMenuItem\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert deleteMenuItem != null : "fx:id=\"deleteMenuItem\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert selectAllMenuItem != null : "fx:id=\"selectAllMenuItem\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert selectNoneMenuItem != null : "fx:id=\"selectNoneMenuItem\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert selectInvertMenuItem != null : "fx:id=\"selectInvertMenuItem\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert selectLeavesMenuItem != null : "fx:id=\"selectLeavesMenuItem\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert selectTreeNodesMenuItem != null : "fx:id=\"selectTreeNodesMenuItem\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert selectReticulateNodesMenuitem != null : "fx:id=\"selectReticulateNodesMenuitem\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert selectAllBelowMenuItem != null : "fx:id=\"selectAllBelowMenuItem\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert selectAllAboveMenuItem != null : "fx:id=\"selectAllAboveMenuItem\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert helpMenu != null : "fx:id=\"helpMenu\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert aboutMenuItem != null : "fx:id=\"aboutMenuItem\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert toolBar != null : "fx:id=\"toolBar\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert statusFlowPane != null : "fx:id=\"statusFlowPane\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert stackPane != null : "fx:id=\"stackPane\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";
        assert mainPane != null : "fx:id=\"mainPane\" was not injected: check your FXML file 'PhyloEditorMain.fxml'.";

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

    public Button getCopyNewickButton() {
        return copyNewickButton;
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