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
package splitstree5.core.datablocks;


import javafx.application.Platform;
import splitstree5.core.Document;
import splitstree5.core.algorithms.interfaces.IFromTreeView;
import splitstree5.core.algorithms.interfaces.IToTreeView;
import splitstree5.gui.IHasTab;
import splitstree5.gui.graphtab.AlgorithmBreadCrumbsToolBar;
import splitstree5.gui.graphtab.TreeViewTab;
import splitstree5.gui.graphtab.base.GraphLayout;

/**
 * This block represents the view of a tree
 * Daniel Huson, 11.2017
 */
public class TreeViewBlock extends ADataBlock implements IHasTab {
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
            }
        }
    }

    @Override
    public void setDataNode(ADataNode dataNode) {
        super.setDataNode(dataNode);
        Platform.runLater(() -> { // setup tab
            treeViewTab.setToolBar(new AlgorithmBreadCrumbsToolBar(getDocument(), getDataNode()));
            ((AlgorithmBreadCrumbsToolBar) treeViewTab.getToolBar()).update();
        });
    }

    /**
     * show the phyloGraph or network
     */
    public void show() {
        treeViewTab.show();
        Platform.runLater(() -> setShortDescription(getInfo()));
    }

    public TreeViewTab getTab() {
        return treeViewTab;
    }

    @Override
    public int size() {
        return treeViewTab.size();
    }

    @Override
    public Class getFromInterface() {
        return IFromTreeView.class;
    }

    @Override
    public Class getToInterface() {
        return IToTreeView.class;
    }

    @Override
    public String getInfo() {
        return "a " + (treeViewTab.getLayout() == GraphLayout.Radial ? "unrooted" : "rooted") + " tree drawing with "
                + treeViewTab.getPhyloGraph().getNumberOfNodes() + " nodes and "
                + treeViewTab.getPhyloGraph().getNumberOfEdges() + " edge";
    }
}
