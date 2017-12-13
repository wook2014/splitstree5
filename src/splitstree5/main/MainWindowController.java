/*
 *  Copyright (C) 2016 Daniel H. Huson
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
package splitstree5.main;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import jloda.util.AppleStuff;
import jloda.util.ProgramProperties;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class MainWindowController {
    @FXML
    private BorderPane borderPane;

    @FXML
    private VBox topVBox;

    @FXML
    private MenuBar menuBar;

    @FXML
    private Menu fileMenu;

    @FXML
    private MenuItem newMenuItem;

    @FXML
    private MenuItem openMenuItem;

    @FXML
    private Menu openRecentMenu;

    @FXML
    private MenuItem enterDataMenuItem;

    @FXML
    private MenuItem saveMenuItem;

    @FXML
    private MenuItem saveAsMenuItem;

    @FXML
    private MenuItem ImportMenuItem;

    @FXML
    private MenuItem exportMenuItem;

    @FXML
    private MenuItem exportImageMenuItem;

    @FXML
    private MenuItem printMenuitem;

    @FXML
    private MenuItem closeMenuItem;

    @FXML
    private MenuItem quitMenuItem;

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
    private MenuItem pasteMenuItem;

    @FXML
    private MenuItem deleteMenuItem;

    @FXML
    private MenuItem selectAllMenuItem;

    @FXML
    private MenuItem selectNoneMenuItem;

    @FXML
    private MenuItem selectAllNodesMenuItem;

    @FXML
    private MenuItem selectAllLabeledNodesMenuItem;

    @FXML
    private MenuItem selectAllEdgeMenuItem;

    @FXML
    private MenuItem findMenuItem;

    @FXML
    private MenuItem findAgainMenuItem;

    @FXML
    private MenuItem gotoLineMenuItem;

    @FXML
    private MenuItem preferencesMenuItem;

    @FXML
    private MenuItem increaseFontSizeMenuItem;

    @FXML
    private MenuItem decreaseFontSizeMenuItem;

    @FXML
    private MenuItem zoomInMenuItem;

    @FXML
    private MenuItem zoomOutMenuItem;

    @FXML
    private MenuItem resetMenuItem;

    @FXML
    private MenuItem rotateLeftMenuItem;

    @FXML
    private MenuItem rotateRightMenuItem;

    @FXML
    private MenuItem flipMenuItem;

    @FXML
    private MenuItem formatNodesMenuItem;

    @FXML
    private MenuItem formatEdgesMenuItem;

    @FXML
    private MenuItem layoutLabelsMenuItem;

    @FXML
    private HBox bottomHBox;

    @FXML
    private ToolBar topToolBar;

    @FXML
    private TabPane tabPane;

    @FXML
    private ToolBar bottomToolBar;

    @FXML
    private Menu windowMenu;

    @FXML
    private MenuItem aboutMenuItem;


    @FXML
    void initialize() {
        // if we are running on MacOS, put the specific menu items in the right places
        if (ProgramProperties.isMacOS()) {
            final AppleStuff appleStuff = AppleStuff.getInstance();
            appleStuff.setQuitAction(new AbstractAction("Quit") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    getQuitMenuItem().fire();
                }
            });
            fileMenu.getItems().remove(getQuitMenuItem());

            appleStuff.setAboutAction(new AbstractAction("About...") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    getAboutMenuItem().fire();
                }
            });
            windowMenu.getItems().remove(getAboutMenuItem());

            appleStuff.setPreferencesAction(new AbstractAction("Preferences...") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    getPreferencesMenuItem().fire();
                }
            });
            editMenu.getItems().remove(getPreferencesMenuItem());
        }
    }

    public BorderPane getBorderPane() {
        return borderPane;
    }

    public VBox getTopVBox() {
        return topVBox;
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public Menu getFileMenu() {
        return fileMenu;
    }

    public MenuItem getNewMenuItem() {
        return newMenuItem;
    }

    public MenuItem getOpenMenuItem() {
        return openMenuItem;
    }

    public Menu getOpenRecentMenu() {
        return openRecentMenu;
    }

    public MenuItem getEnterDataMenuItem() {
        return enterDataMenuItem;
    }

    public MenuItem getSaveMenuItem() {
        return saveMenuItem;
    }

    public MenuItem getSaveAsMenuItem() {
        return saveAsMenuItem;
    }

    public MenuItem getImportMenuItem() {
        return ImportMenuItem;
    }

    public MenuItem getExportMenuItem() {
        return exportMenuItem;
    }

    public MenuItem getExportImageMenuItem() {
        return exportImageMenuItem;
    }

    public MenuItem getPrintMenuitem() {
        return printMenuitem;
    }

    public MenuItem getCloseMenuItem() {
        return closeMenuItem;
    }

    public MenuItem getQuitMenuItem() {
        return quitMenuItem;
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

    public MenuItem getSelectAllNodesMenuItem() {
        return selectAllNodesMenuItem;
    }

    public MenuItem getSelectAllLabeledNodesMenuItem() {
        return selectAllLabeledNodesMenuItem;
    }

    public MenuItem getSelectAllEdgeMenuItem() {
        return selectAllEdgeMenuItem;
    }

    public MenuItem getFindMenuItem() {
        return findMenuItem;
    }

    public MenuItem getFindAgainMenuItem() {
        return findAgainMenuItem;
    }

    public MenuItem getGotoLineMenuItem() {
        return gotoLineMenuItem;
    }

    public MenuItem getPreferencesMenuItem() {
        return preferencesMenuItem;
    }

    public MenuItem getIncreaseFontSizeMenuItem() {
        return increaseFontSizeMenuItem;
    }

    public MenuItem getDecreaseFontSizeMenuItem() {
        return decreaseFontSizeMenuItem;
    }

    public MenuItem getZoomInMenuItem() {
        return zoomInMenuItem;
    }

    public MenuItem getZoomOutMenuItem() {
        return zoomOutMenuItem;
    }

    public MenuItem getResetMenuItem() {
        return resetMenuItem;
    }

    public MenuItem getRotateLeftMenuItem() {
        return rotateLeftMenuItem;
    }

    public MenuItem getRotateRightMenuItem() {
        return rotateRightMenuItem;
    }

    public MenuItem getFlipMenuItem() {
        return flipMenuItem;
    }

    public MenuItem getFormatNodesMenuItem() {
        return formatNodesMenuItem;
    }

    public MenuItem getFormatEdgesMenuItem() {
        return formatEdgesMenuItem;
    }

    public MenuItem getLayoutLabelsMenuItem() {
        return layoutLabelsMenuItem;
    }

    public HBox getBottomHBox() {
        return bottomHBox;
    }

    public TabPane getTabPane() {
        return tabPane;
    }

    public ToolBar getTopToolBar() {
        return topToolBar;
    }

    public ToolBar getBottomToolBar() {
        return bottomToolBar;
    }

    public Menu getWindowMenu() {
        return windowMenu;
    }

    public MenuItem getAboutMenuItem() {
        return aboutMenuItem;
    }

    /**
     * unbinds and disables all menu items
     */
    public void unbindAndDisableAllMenuItems() {
        for (Menu menu : getMenuBar().getMenus()) {
            for (MenuItem menuItem : menu.getItems()) {
                menuItem.setOnAction(null);
                menuItem.disableProperty().unbind();
                menuItem.setDisable(true);
            }
        }
    }

    /**
     * enables all memnu items whose disable property is not bound and that have an action
     */
    public void enableAllUnboundActionMenuItems() {
        for (Menu menu : getMenuBar().getMenus()) {
            for (MenuItem menuItem : menu.getItems()) {
                if (!menuItem.disableProperty().isBound() && menuItem.getOnAction() != null)
                    menuItem.setDisable(false);
            }
        }
    }
}
