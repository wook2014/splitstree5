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
 * datablock that represents a tree or network viewer
 * Daniel Huson, 2.2018
 */
public class ViewerBlock extends DataBlock {
    public enum Type {TreeViewer, SplitsNetworkViewer, SplitsNetwork3DViewer, NetworkViewer}

    private GraphTabBase viewerTab;
    private Type type;

    /**
     * constructor
     *
     * @param viewerTab
     */
    private ViewerBlock(GraphTabBase viewerTab, Type type) {
        setup(viewerTab, type);
    }

    /**
     * setup the viewer block
     *
     * @param viewerTab
     * @param type
     */
    private void setup(GraphTabBase viewerTab, Type type) {
        this.viewerTab = viewerTab;
        this.type = type;
        setTitle(viewerTab.getName());
        setName(viewerTab.getName());
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

    public Type getType() {
        return type;
    }

    /**
     * This block represents the 3D view of a split network
     * Daniel Huson, 1.2018
     */
    public static class SplitsNetwork3DViewerBlock extends ViewerBlock {
        public SplitsNetwork3DViewerBlock() {
            super(new SplitsView3DTab(), Type.SplitsNetwork3DViewer);
        }
    }

    /**
     * This block represents the view of a split network
     * Daniel Huson, 1.2018
     */
    public static class SplitsNetworkViewerBlock extends ViewerBlock {
        public SplitsNetworkViewerBlock() {
            super(new SplitsViewTab(), Type.SplitsNetworkViewer);
        }
    }

    /**
     * This block represents the view of a general network
     * Daniel Huson, 1.2018
     */
    public static class NetworkViewerBlock extends ViewerBlock {
        public NetworkViewerBlock() {
            super(new NetworkViewTab(), Type.NetworkViewer);
        }
    }

    /**
     * This block represents the view of a tree
     * Daniel Huson, 11.2017
     */
    public static class TreeViewerBlock extends ViewerBlock {
        public TreeViewerBlock() {
            super(new TreeViewTab(), Type.TreeViewer);
        }
    }

    /**
     * change the type of a block
     *
     * @param type
     */
    public void changeType(Type type) {
        switch (type) {
            case SplitsNetwork3DViewer:
                setup(new SplitsView3DTab(), type);
                break;
            case SplitsNetworkViewer:
                setup(new SplitsViewTab(), type);
                break;
            case NetworkViewer:
                setup(new NetworkViewTab(), type);
                break;
            case TreeViewer:
                setup(new TreeViewTab(), type);
                break;
            default:
                throw new RuntimeException("Unknown type: " + type);
        }
    }
}
