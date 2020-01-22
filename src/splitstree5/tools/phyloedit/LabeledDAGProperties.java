/*
 *  LabeledDAGProperties.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.tools.phyloedit;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.layout.FlowPane;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import jloda.fx.graph.GraphFX;
import jloda.graph.EdgeSet;
import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import splitstree5.treebased.OffspringGraphMatching;

/**
 * maintains network properties
 * Daniel Huson, 1.2020
 */
public class LabeledDAGProperties {
    /**
     * setup network properties
     *
     * @param statusFlowPane
     * @param graphFX
     * @param <G>
     */
    public static <G extends Graph> void setup(FlowPane statusFlowPane, GraphFX<G> graphFX, BooleanProperty leafLabeledDAGProperty) {

        graphFX.getNodeList().addListener((InvalidationListener) z -> update(statusFlowPane, graphFX, leafLabeledDAGProperty));
        graphFX.getEdgeList().addListener((InvalidationListener) z -> update(statusFlowPane, graphFX, leafLabeledDAGProperty));
        graphFX.getNodeList().addListener((ListChangeListener<Node>) (z) -> {
            while (z.next()) {
                for (Node v : z.getAddedSubList()) {
                    graphFX.nodeLabelProperty(v).addListener(c -> update(statusFlowPane, graphFX, leafLabeledDAGProperty));
                }
            }
        });
    }

    public static <G extends Graph> void update(FlowPane statusFlowPane, GraphFX<G> graphFX, BooleanProperty leafLabeledDAGProperty) {
        statusFlowPane.getChildren().clear();

        final PhyloTree graph = (PhyloTree) graphFX.getGraph();
        statusFlowPane.getChildren().add(new Text("Nodes: " + graph.getNumberOfNodes()));
        statusFlowPane.getChildren().add(new Text("Edges: " + graph.getNumberOfEdges()));
        final boolean idRootedDag = isDAG(graph);
        statusFlowPane.getChildren().add(new Text("Rooted DAG: " + idRootedDag));
        final boolean isLeafLabeled = isLeafLabeled(graph);
        statusFlowPane.getChildren().add(new Text("Leaf-labeled: " + isLeafLabeled));
        leafLabeledDAGProperty.set(idRootedDag && isLeafLabeled);

        if (idRootedDag && isLeafLabeled) {
            final EdgeSet matching = OffspringGraphMatching.compute(graph);
            if (OffspringGraphMatching.isTreeBased(graph, matching))
                statusFlowPane.getChildren().add(new Text("Tree-Based"));
            else
                statusFlowPane.getChildren().add(new Text("Tree-Based Discrepancy: " + OffspringGraphMatching.discrepancy(graph, matching)));

        }

        for (Object object : statusFlowPane.getChildren()) {
            if (object instanceof Shape) {
                ((Shape) object).prefWidth(30);
            }
        }
    }

    public static boolean isDAG(Graph graph) {
        if (findRoot(graph) == null)
            return false;
        final Graph g = new Graph();
        g.copy(graph);

        while (g.getNumberOfNodes() > 0) {
            boolean found = false;
            for (Node v : g.nodes()) {
                if (v.getOutDegree() == 0) {
                    g.deleteNode(v);
                    found = true;
                    break;
                }
            }
            if (!found)
                return false;
        }
        return true;
    }

    public static boolean isLeafLabeled(Graph graph) {
        for (Node v : graph.nodes()) {
            if (v.getOutDegree() == 0 && (graph.getLabel(v) == null || (graph.getLabel(v).length() == 0)))
                return false;
        }
        return true;
    }

    public static Node findRoot(Graph graph) {
        for (Node v : graph.nodes()) {
            if (v.getInDegree() == 0)
                return v;
        }
        return null;

    }
}
