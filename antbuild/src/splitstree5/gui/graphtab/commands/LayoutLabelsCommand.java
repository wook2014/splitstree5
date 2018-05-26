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

package splitstree5.gui.graphtab.commands;

import javafx.geometry.Point2D;
import javafx.scene.control.Labeled;
import jloda.graph.Edge;
import jloda.graph.EdgeArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloGraph;
import jloda.util.Triplet;
import splitstree5.gui.graphtab.base.EdgeViewBase;
import splitstree5.gui.graphtab.base.GraphLayout;
import splitstree5.gui.graphtab.base.NodeLabelLayouter;
import splitstree5.gui.graphtab.base.NodeViewBase;
import splitstree5.undo.UndoableRedoableCommand;

import java.util.ArrayList;

/**
 * layout labels
 * Daniel Huson, 1.2018
 */
public class LayoutLabelsCommand extends UndoableRedoableCommand {
    private final GraphLayout graphLayout;
    private final boolean sparseLabels;
    private final PhyloGraph phyloGraph;
    private final Node root;
    private final NodeArray<NodeViewBase> node2view;
    private final EdgeArray<EdgeViewBase> edge2view;

    private final ArrayList<Triplet<Node, Point2D, Boolean>> oldNodeLabels = new ArrayList<>();
    private final ArrayList<Triplet<Edge, Point2D, Boolean>> oldEdgeLabels = new ArrayList<>();

    public LayoutLabelsCommand(GraphLayout graphLayout, boolean sparseLabels, PhyloGraph phyloGraph, Node root, NodeArray<NodeViewBase> node2view, EdgeArray<EdgeViewBase> edge2view) {
        super("Label Layout");
        this.graphLayout = graphLayout;
        this.sparseLabels = sparseLabels;
        this.phyloGraph = phyloGraph;
        this.root = root;
        this.node2view = node2view;
        this.edge2view = edge2view;

        for (Node v : phyloGraph.nodes()) {
            final NodeViewBase nv = node2view.get(v);
            if (nv.getLabel() != null) {
                oldNodeLabels.add(new Triplet<>(v, new Point2D(nv.getLabel().getLayoutX(), nv.getLabel().getLayoutY()), nv.getLabel().isVisible()));
            }
        }

        for (Edge e : phyloGraph.edges()) {
            final EdgeViewBase ev = edge2view.get(e);
            if (ev.getLabel() != null) {
                oldEdgeLabels.add(new Triplet<>(e, new Point2D(ev.getLabel().getLayoutX(), ev.getLabel().getLayoutY()), ev.getLabel().isVisible()));
            }
        }
    }

    @Override
    public void undo() {
        for (Triplet<Node, Point2D, Boolean> tre : oldNodeLabels) {
            final Labeled label = node2view.get(tre.getFirst()).getLabel();
            final Point2D location = tre.getSecond();
            label.setLayoutX(location.getX());
            label.setLayoutY(location.getY());
            label.setVisible(tre.getThird());

        }
        for (Triplet<Edge, Point2D, Boolean> tre : oldEdgeLabels) {
            final Labeled label = edge2view.get(tre.getFirst()).getLabel();
            final Point2D location = tre.getSecond();
            label.setLayoutX(location.getX());
            label.setLayoutY(location.getY());
            label.setVisible(tre.getThird());
        }
    }

    @Override
    public void redo() {
        if (graphLayout == GraphLayout.Radial)
            NodeLabelLayouter.radialLayout(sparseLabels, phyloGraph, node2view, edge2view);
        else
            NodeLabelLayouter.leftToRightLayout(sparseLabels, phyloGraph, root, node2view, edge2view);

    }
}
