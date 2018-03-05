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
import splitstree5.core.algorithms.interfaces.IFromView;
import splitstree5.core.algorithms.interfaces.IToView;
import splitstree5.core.workflow.DataNode;
import splitstree5.gui.graph3dtab.SplitsView3DTab;
import splitstree5.gui.graphtab.AlgorithmBreadCrumbsToolBar;
import splitstree5.gui.graphtab.NetworkViewTab;
import splitstree5.gui.graphtab.SplitsViewTab;
import splitstree5.gui.graphtab.TreeViewTab;
import splitstree5.gui.graphtab.base.GraphTabBase;

/**
 * datablock that represents a tree or network view
 * Daniel Huson, 2.2018
 */
public class ViewBlock extends DataBlock {
    public enum Type {TreeViewer, SplitsNetworkViewer, SplitsNetwork3DViewer, NetworkViewer}

    private final GraphTabBase viewerTab;

    /**
     * constructor
     *
     * @param viewerTab
     */
    private ViewBlock(GraphTabBase viewerTab) {
        setTitle(viewerTab.getName());
        setName(viewerTab.getName());

        this.viewerTab = viewerTab;
        viewerTab.setDataNode(getDataNode()); // todo: do we need this?
    }

    /**
     * show the network
     */
    public void show() {
        viewerTab.show();
        Platform.runLater(() -> {
            setShortDescription(getInfo());
            if (viewerTab.getToolBar() == null) {
                viewerTab.setToolBar(new AlgorithmBreadCrumbsToolBar(getDocument(), getDataNode()));
                ((AlgorithmBreadCrumbsToolBar) viewerTab.getToolBar()).update();
            }
            if (viewerTab.getTabPane() != null)
                viewerTab.getTabPane().getSelectionModel().select(viewerTab);
        });
    }

    @Override
    public void setDocument(Document document) {
        if (getDocument() == null) {
            super.setDocument(document);
            if (document.getMainWindow() != null) {
                Platform.runLater(() -> { // setup tab
                    document.getMainWindow().showDataView(getDataNode());
                });
            }
        }
    }

    @Override
    public void setDataNode(DataNode dataNode) {
        viewerTab.setDataNode(dataNode);
        super.setDataNode(dataNode);
    }

    public GraphTabBase getTab() {
        return viewerTab;
    }

    @Override
    public int size() {
        return viewerTab.size();
    }

    @Override
    public Class getFromInterface() {
        return IFromView.class;
    }

    @Override
    public Class getToInterface() {
        return IToView.class;
    }

    @Override
    public String getInfo() {
        return viewerTab.getInfo();
    }

    /**
     * This block represents the 3D view of a split network
     * Daniel Huson, 1.2018
     */
    public static class SplitsNetwork3DViewerBlock extends ViewBlock {
        public SplitsNetwork3DViewerBlock() {
            super(new SplitsView3DTab());
        }
    }

    /**
     * This block represents the view of a split network
     * Daniel Huson, 1.2018
     */
    public static class SplitsNetworkViewerBlock extends ViewBlock {
        public SplitsNetworkViewerBlock() {
            super(new SplitsViewTab());
        }
    }

    /**
     * This block represents the view of a general network
     * Daniel Huson, 1.2018
     */
    public static class NetworkViewBlock extends ViewBlock {
        public NetworkViewBlock() {
            super(new NetworkViewTab());
        }
    }

    /**
     * This block represents the view of a tree
     * Daniel Huson, 11.2017
     */
    public static class TreeViewerBlock extends ViewBlock {
        public TreeViewerBlock() {
            super(new TreeViewTab());
        }

    }
}
