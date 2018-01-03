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

package splitstree5.main;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import splitstree5.undo.UndoRedoManager;

/**
 * a viewer contained in a tab
 * Daniel Huson, 12.2017
 */
public abstract class ViewerTab extends Tab {
    private final BorderPane borderPane = new BorderPane();
    protected ToolBar toolBar;
    protected final UndoRedoManager undoRedoManager = new UndoRedoManager();

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
    }


    /**
     * setup menu items and bind their disable properties
     */
    public abstract void updateMenus(MainWindowController controller);

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

    public UndoRedoManager getUndoRedoManager() {
        return undoRedoManager;
    }
}
