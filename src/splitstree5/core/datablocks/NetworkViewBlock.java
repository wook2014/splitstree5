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
import jloda.phylo.SplitsGraph;
import splitstree5.core.Document;
import splitstree5.core.algorithms.interfaces.IFromSplitsNetworkView;
import splitstree5.core.algorithms.interfaces.IToSplitsNetworkView;
import splitstree5.gui.IHasTab;
import splitstree5.gui.ViewerTab;
import splitstree5.gui.graphtab.AlgorithmBreadCrumbsToolBar;
import splitstree5.gui.graphtab.NetworkViewTab;
import splitstree5.gui.graphtab.base.GraphLayout;

/**
 * This block represents the view of a general network
 * Daniel Huson, 1.2018
 */
public class NetworkViewBlock extends ADataBlock implements IHasTab {
    private final NetworkViewTab networkViewTAB;

    public NetworkViewBlock() {
        this(new NetworkViewTab());
    }

    /**
     * constructor
     */
    public NetworkViewBlock(NetworkViewTab networkViewTAB) {
        super();
        setTitle("Network Viewer");

        this.networkViewTAB = new NetworkViewTab();

        networkViewTAB.setLayout(GraphLayout.Radial);

    }

    @Override
    public void setDocument(Document document) {
        if (getDocument() == null) {
            super.setDocument(document);
            if (document.getMainWindow() != null) {
                Platform.runLater(() -> { // setup tab
                    document.getMainWindow().add(networkViewTAB);
                });
            }
        }
    }

    @Override
    public void setDataNode(ADataNode dataNode) {
        super.setDataNode(dataNode);
    }

    public ViewerTab getTab() {
        return networkViewTAB;
    }


    /**
     * show the network
     */
    public void show() {
        networkViewTAB.show();
        Platform.runLater(() -> {
            setShortDescription(getInfo());
            if (networkViewTAB.getToolBar() == null) {
                networkViewTAB.setToolBar(new AlgorithmBreadCrumbsToolBar(getDocument(), getDataNode()));
                ((AlgorithmBreadCrumbsToolBar) networkViewTAB.getToolBar()).update();
            }

        });
    }

    public void updateSelectionModels(SplitsGraph graph, TaxaBlock taxa, Document document) {
        networkViewTAB.updateSelectionModels(graph, taxa, document);
    }

    @Override
    public Class getFromInterface() {
        return IFromSplitsNetworkView.class;
    }

    @Override
    public Class getToInterface() {
        return IToSplitsNetworkView.class;
    }

    @Override
    public int size() {
        return networkViewTAB.size();
    }

    @Override
    public String getInfo() {
        if (networkViewTAB != null && networkViewTAB.getGraph() != null) {
            return "a network with " + networkViewTAB.getGraph().getNumberOfNodes() + " nodes and " + networkViewTAB.getGraph().getNumberOfEdges() + " edges";
        } else
            return "a network";
    }
}
