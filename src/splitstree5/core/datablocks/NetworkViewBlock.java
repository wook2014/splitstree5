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
import jloda.phylo.PhyloGraph;
import splitstree5.core.Document;
import splitstree5.core.algorithms.interfaces.IFromNetworkView;
import splitstree5.core.algorithms.interfaces.IToNetworkView;
import splitstree5.core.workflow.DataNode;
import splitstree5.gui.IHasTab;
import splitstree5.gui.graphtab.AlgorithmBreadCrumbsToolBar;
import splitstree5.gui.graphtab.NetworkViewTab;
import splitstree5.gui.graphtab.base.GraphLayout;

/**
 * This block represents the view of a general network
 * Daniel Huson, 1.2018
 */
public class NetworkViewBlock extends ViewDataBlock implements IHasTab {
    private final NetworkViewTab networkViewTab;

    public NetworkViewBlock() {
        this(new NetworkViewTab());
    }

    /**
     * constructor
     */
    public NetworkViewBlock(NetworkViewTab networkViewTab) {
        setTitle("Network Viewer");
        this.networkViewTab = new NetworkViewTab();
        networkViewTab.setDataNode(getDataNode());
        networkViewTab.setLayout(GraphLayout.Radial);
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

    public NetworkViewTab getTab() {
        return networkViewTab;
    }


    /**
     * show the network
     */
    public void show() {
        networkViewTab.show();
        Platform.runLater(() -> {
            networkViewTab.setLayout(GraphLayout.Radial);
            setShortDescription(getInfo());
            if (networkViewTab.getToolBar() == null) {
                networkViewTab.setToolBar(new AlgorithmBreadCrumbsToolBar(getDocument(), getDataNode()));
                ((AlgorithmBreadCrumbsToolBar) networkViewTab.getToolBar()).update();
            }
            if (networkViewTab.getTabPane() != null)
                networkViewTab.getTabPane().getSelectionModel().select(networkViewTab);
        });
    }

    @Override
    public void setDataNode(DataNode dataNode) {
        networkViewTab.setDataNode(dataNode);
        super.setDataNode(dataNode);
    }

    public void updateSelectionModels(PhyloGraph graph, TaxaBlock taxa, Document document) {
        networkViewTab.updateSelectionModels(graph, taxa, document);
    }

    @Override
    public Class getFromInterface() {
        return IFromNetworkView.class;
    }

    @Override
    public Class getToInterface() {
        return IToNetworkView.class;
    }

    @Override
    public int size() {
        return networkViewTab.size();
    }

    @Override
    public String getInfo() {
        if (networkViewTab != null && networkViewTab.getGraph() != null) {
            return "a network with " + networkViewTab.getGraph().getNumberOfNodes() + " nodes and " + networkViewTab.getGraph().getNumberOfEdges() + " edges";
        } else
            return "a network";
    }
}
