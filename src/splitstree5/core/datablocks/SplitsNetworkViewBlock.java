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
import javafx.collections.ListChangeListener;
import jloda.fx.ASelectionModel;
import jloda.graph.Edge;
import jloda.phylo.SplitsGraph;
import splitstree5.core.Document;
import splitstree5.core.algorithms.interfaces.IFromSplitsNetworkView;
import splitstree5.core.algorithms.interfaces.IToSplitsNetworkView;
import splitstree5.gui.IHasTab;
import splitstree5.gui.ViewerTab;
import splitstree5.gui.graphtab.AlgorithmBreadCrumbsToolBar;
import splitstree5.gui.graphtab.ISplitsViewTab;
import splitstree5.gui.graphtab.SplitsViewTab;
import splitstree5.gui.graphtab.base.GraphLayout;

import java.util.HashSet;
import java.util.Set;

/**
 * This block represents the 3D view of a split network
 * Daniel Huson, 1.2018
 */
public class SplitsNetworkViewBlock extends ADataBlock implements IHasTab {
    private final ASelectionModel<Integer> splitsSelectionModel = new ASelectionModel<>();
    private final ISplitsViewTab splitsViewTab;

    public SplitsNetworkViewBlock() {
        this(new SplitsViewTab());
    }

    /**
     * constructor
     */
    public SplitsNetworkViewBlock(ISplitsViewTab splitsViewTab) {
        super();
        setTitle("Split Network Viewer");
        //splitsViewTab = new SplitsViewTab();

        this.splitsViewTab = splitsViewTab;

        splitsViewTab.setLayout(GraphLayout.Radial);

        splitsSelectionModel.getSelectedItems().addListener((ListChangeListener<Integer>) c -> {
            final Set<Integer> addedSplits = new HashSet<>();
            final Set<Integer> removedSplits = new HashSet<>();
            while (c.next()) {
                addedSplits.addAll(c.getAddedSubList());
                removedSplits.addAll(c.getRemoved());
            }
            final SplitsGraph graph = splitsViewTab.getGraph();
            for (Edge e : graph.edges()) {
                if (addedSplits.contains(graph.getSplit(e)))
                    splitsViewTab.getEdgeSelectionModel().select(e);
                if (removedSplits.contains(graph.getSplit(e)))
                    splitsViewTab.getEdgeSelectionModel().clearSelection(e);
            }
        });
    }

    @Override
    public void setDocument(Document document) {
        if (getDocument() == null) {
            super.setDocument(document);
            if (document.getMainWindow() != null) {
                Platform.runLater(() -> { // setup tab
                    document.getMainWindow().add(splitsViewTab.getTab());
                });
            }
        }
    }

    @Override
    public void setDataNode(ADataNode dataNode) {
        super.setDataNode(dataNode);
    }

    public ViewerTab getTab() {
        return splitsViewTab.getTab();
    }

    public ISplitsViewTab getSplitsView() {
        return splitsViewTab;
    }


    /**
     * show the splits network
     */
    public void show() {
        splitsViewTab.show();
        Platform.runLater(() -> {
            setShortDescription(getInfo());
            if (splitsViewTab.getToolBar() == null) {
                splitsViewTab.setToolBar(new AlgorithmBreadCrumbsToolBar(getDocument(), getDataNode()));
                ((AlgorithmBreadCrumbsToolBar) splitsViewTab.getToolBar()).update();
            }

        });
    }

    public void updateSelectionModels(SplitsGraph graph, TaxaBlock taxa, Document document) {
        splitsViewTab.updateSelectionModels(graph, taxa, document);
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
        return splitsViewTab.size();
    }

    @Override
    public String getInfo() {
        if (splitsViewTab != null && splitsViewTab.getGraph() != null) {
            return "a split network with " + splitsViewTab.getGraph().getNumberOfNodes() + " nodes and " + splitsViewTab.getGraph().getNumberOfEdges() + " edges";
        } else
            return "a split network";
    }
}
