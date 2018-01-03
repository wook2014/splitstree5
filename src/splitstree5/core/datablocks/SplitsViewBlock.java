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
import jloda.phylo.PhyloGraph;
import splitstree5.core.Document;
import splitstree5.core.algorithms.interfaces.IFromSplits;
import splitstree5.core.algorithms.interfaces.IToSplitsView;
import splitstree5.main.IHasTab;
import splitstree5.main.graphtab.SplitsViewTab;
import splitstree5.main.graphtab.base.GraphLayout;

import java.util.HashSet;
import java.util.Set;

/**
 * This block represents the view of a split network
 * Daniel Huson, 11.2017
 */
public class SplitsViewBlock extends ADataBlock implements IHasTab {
    private final ASelectionModel<Integer> splitsSelectionModel = new ASelectionModel<>();
    private final SplitsViewTab splitsViewTab;

    /**
     * constructor
     */
    public SplitsViewBlock() {
        super();
        setTitle("Split Network Viewer");
        splitsViewTab = new SplitsViewTab();

        splitsViewTab.setLayout(GraphLayout.Radial);

        splitsSelectionModel.getSelectedItems().addListener((ListChangeListener<Integer>) c -> {
            final Set<Integer> addedSplits = new HashSet<>();
            final Set<Integer> removedSplits = new HashSet<>();
            while (c.next()) {
                addedSplits.addAll(c.getAddedSubList());
                removedSplits.addAll(c.getRemoved());
            }
            final PhyloGraph graph = splitsViewTab.getPhyloGraph();
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
                    document.getMainWindow().add(splitsViewTab);
                });
            }
        }
    }

    public SplitsViewTab getTab() {
        return splitsViewTab;
    }

    /**
     * show the splits network
     */
    public void show() {
        splitsViewTab.show();
    }

    public void updateSelectionModels(PhyloGraph graph) {
        splitsViewTab.updateSelectionModels(graph);
        splitsSelectionModel.setItems(graph.getSplitIds());
    }

    @Override
    public Class getFromInterface() {
        return IFromSplits.class;
    }

    @Override
    public Class getToInterface() {
        return IToSplitsView.class;
    }

    @Override
    public int size() {
        return splitsViewTab.size();
    }

    @Override
    public String getInfo() {
        if (splitsViewTab != null && splitsViewTab.getPhyloGraph() != null) {
            return "a split network with " + splitsViewTab.getPhyloGraph().getNumberOfNodes() + " nodes and " + splitsViewTab.getPhyloGraph().getNumberOfEdges() + " edges";
        } else
            return "a split network";
    }
}
