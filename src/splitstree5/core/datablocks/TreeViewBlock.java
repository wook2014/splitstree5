/*
 *  Copyright (C) 2017 Daniel H. Huson
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
package splitstree5.core.datablocks;


import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import splitstree5.core.Document;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToTreeView;
import splitstree5.main.graphtab.TreeViewTab;

/**
 * This block represents the view of a tree
 * Daniel Huson, 11.2017
 */
public class TreeViewBlock extends ADataBlock {
    private final TreeViewTab treeViewTab;

    /**
     * constructor
     */
    public TreeViewBlock() {
        super();
        setTitle("Tree Viewer");
        treeViewTab = new TreeViewTab();
    }

    @Override
    public void setDocument(Document document) {
        if (getDocument() == null) {
            super.setDocument(document);
            if (document.getMainWindow() != null) {
                Platform.runLater(() -> { // setup tab
                    document.getMainWindow().add(treeViewTab);
                });
            } else { // this is for testing only: this opens the view in a standalone window
                Platform.runLater(() -> {
                    Stage stage = new Stage();
                    final TabPane tabPane = new TabPane(treeViewTab);
                    stage.setScene(new Scene(tabPane));
                    stage.setWidth(800);
                    stage.setHeight(800);
                    stage.show();
                });
            }
        }
    }

    /**
     * show the phyloGraph or network
     */
    public void show() {
        treeViewTab.show();
    }

    public TreeViewTab getTreeViewTab() {
        return treeViewTab;
    }

    @Override
    public int size() {
        return treeViewTab.size();
    }

    @Override
    public Class getFromInterface() {
        return IFromTrees.class;
    }

    @Override
    public Class getToInterface() {
        return IToTreeView.class;
    }

    @Override
    public String getInfo() {
        return "Phylogenetic tree";
    }
}
