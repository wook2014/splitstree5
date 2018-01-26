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

/*
 *  Copyright (C) 2018 Daniel H. Huson
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

package splitstree5.gui;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import jloda.find.FindToolBar;
import splitstree5.main.MainWindow;
import splitstree5.menu.MenuController;
import splitstree5.undo.UndoManager;

/**
 * a viewer contained in a tab
 * Daniel Huson, 12.2017
 */
public abstract class ViewerTab extends Tab {
    private MainWindow mainWindow;
    private final BorderPane borderPane = new BorderPane();
    protected ToolBar toolBar;
    protected FindToolBar findToolBar;

    private final UndoManager undoManager = new UndoManager();

    /**
     * constructor
     */
    public ViewerTab() {
        contentProperty().addListener((c, o, n) -> {
            if (n != borderPane) {
                borderPane.setCenter(n);
                setContent(borderPane);
            }
        });
        //setClosable(true);
    }

    /**
     * setup menu items and bind their disable properties
     */
    public abstract void updateMenus(MenuController controller);

    public Node getCenter() {
        return borderPane.getCenter();
    }

    /**
     * get associated toolbar
     */
    public ToolBar getToolBar() {
        return toolBar;
    }

    /**
     * set associated toolbar
     */
    public void setToolBar(ToolBar toolBar) {
        this.toolBar = toolBar;
        borderPane.setTop(toolBar);
    }

    /**
     * select this tab
     */
    public void selectTab() {
        if (getTabPane() != null)
            getTabPane().getSelectionModel().select(this);
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public void setMainWindow(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    public FindToolBar getFindToolBar() {
        return findToolBar;
    }

}
