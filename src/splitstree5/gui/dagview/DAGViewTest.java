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

package splitstree5.gui.dagview;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import org.junit.Test;
import splitstree5.core.Document;
import splitstree5.core.algorithms.trees2splits.TreeSelector;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.dag.ANode;
import splitstree5.core.dag.DAG;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.filters.SplitsFilter;
import splitstree5.core.filters.TreeFilter;
import splitstree5.io.nexus.NexusFileParser;

import java.util.HashMap;
import java.util.Map;

/**
 * test the DAG view
 * Created by huson on 12/31/16.
 */
public class DAGViewTest extends Application {
    @Test
    public void test() throws Exception {
        init();
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final Document document = new Document();
        document.setFileName("test/nexus/trees49-notaxa.nex");
        NexusFileParser.parse(document);

        DAG dag = document.getDag();

        if (dag.getWorkingDataNode().getDataBlock() instanceof TreesBlock) {
            final TreeSelector treeSelector = new TreeSelector();
            AConnector connector = dag.createConnector(dag.getWorkingDataNode(), new ADataNode<>(new SplitsBlock()), treeSelector);
            dag.addConnector(new SplitsFilter(dag.getWorkingTaxaNode().getDataBlock(), connector.getChild(), new ADataNode<>(new SplitsBlock())));
        }


        if (dag.getWorkingDataNode().getDataBlock() instanceof TreesBlock) {
            final AConnector connector = new TreeFilter(dag.getWorkingTaxaNode().getDataBlock(), dag.getWorkingDataNode(), new ADataNode<>(new TreesBlock()));
        }

        final DAGView dagView = new DAGView(document);


        final Map<ANode, DagNodeView> node2nodeView = new HashMap<>();

        node2nodeView.put(dag.getTopTaxaNode(), new DagNodeView(dagView, dag.getTopTaxaNode()));
        node2nodeView.put(dag.getTaxaFilter(), new DagNodeView(dagView, dag.getTaxaFilter()));

        node2nodeView.put(dag.getWorkingTaxaNode(), new DagNodeView(dagView, dag.getWorkingTaxaNode()));
        node2nodeView.put(dag.getTopDataNode(), new DagNodeView(dagView, dag.getTopDataNode()));
        node2nodeView.put(dag.getTopFilter(), new DagNodeView(dagView, dag.getTopFilter()));
        node2nodeView.put(dag.getWorkingDataNode(), new DagNodeView(dagView, dag.getWorkingDataNode()));

        final double yDelta = 150;
        final double xDelta = 250;
        node2nodeView.get(dag.getTopTaxaNode()).setXY(20, 20);
        node2nodeView.get(dag.getTopDataNode()).setXY(20 + xDelta, 20);
        node2nodeView.get(dag.getTaxaFilter()).setXY(20, 20 + yDelta);
        node2nodeView.get(dag.getWorkingTaxaNode()).setXY(20, 20 + 2 * yDelta);
        node2nodeView.get(dag.getTopFilter()).setXY(20 + xDelta, 20 + 2 * yDelta);
        node2nodeView.get(dag.getWorkingDataNode()).setXY(20 + 2 * xDelta, 20 + yDelta);

        assignNodeViewsAndCoordinatesForChildrenRec(dagView, dag.getWorkingDataNode(), node2nodeView, xDelta, yDelta, true);

        final ObservableList<DagEdgeView> edgeViews = FXCollections.observableArrayList();

        for (DagNodeView a : node2nodeView.values()) {
            for (DagNodeView b : node2nodeView.values()) {
                if (a.getANode().getChildren().contains(b.getANode()))
                    edgeViews.add(new DagEdgeView(a, b));
                else if (a.getANode() == dag.getWorkingTaxaNode() && b.getANode() == dag.getTopFilter()) {
                    edgeViews.add(new DagEdgeView(a, b));
                }
            }
        }

        dagView.getCenterPane().getChildren().addAll(edgeViews);
        dagView.getCenterPane().getChildren().addAll(node2nodeView.values());
    }

    /**
     * recursively
     *
     * @param v
     * @param node2nodeView
     * @param xDelta
     * @param yDelta
     * @param horizontal
     */
    private void assignNodeViewsAndCoordinatesForChildrenRec(DAGView dagView, ANode v, Map<ANode, DagNodeView> node2nodeView, double xDelta, double yDelta, boolean horizontal) {
        double x = node2nodeView.get(v).xProperty().get();
        double y = node2nodeView.get(v).yProperty().get();

        int count = 0;
        for (ANode w : v.getChildren()) {
            if (count == 1)
                horizontal = !horizontal;
            else if (count == 2) {
                x += (count - 1) * xDelta;
                y += (count - 1) * yDelta;
            }

            final DagNodeView nodeView = node2nodeView.computeIfAbsent(w, k -> new DagNodeView(dagView, w));
            if (horizontal) {
                nodeView.setXY(x + xDelta, y);
            } else {
                nodeView.setXY(x, y + yDelta);
            }
            assignNodeViewsAndCoordinatesForChildrenRec(dagView, w, node2nodeView, xDelta, yDelta, horizontal);
            count++;
        }
    }
}