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

package splitstree5.gui.graphtab.base;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import jloda.graph.Edge;
import jloda.graph.EdgeArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloGraph;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * node label layouter
 */
public class NodeLabelLayouter {
    /**
     * layout nodes for radial tree
     *
     * @param phyloGraph
     * @param node2view
     * @param edge2view
     */
    public static void radialLayout(PhyloGraph phyloGraph, NodeArray<ANodeView> node2view, EdgeArray<AEdgeView> edge2view) {
        ArrayList<BoundingBox> boundsList = new ArrayList<>();

        for (Node v : phyloGraph.nodes()) {
            final ANodeView nv = node2view.getValue(v);
            if (phyloGraph.getLabel(v) != null) {
                final javafx.scene.Node shape = nv.getShape();
                boundsList.add(new BoundingBox(shape.getLayoutX(), shape.getLayoutY(), shape.getLayoutBounds().getWidth(), shape.getLayoutBounds().getHeight()));
            }
        }

        for (Node v : phyloGraph.nodes()) {
            final ANodeView nv = node2view.getValue(v);
            final javafx.scene.Node shape = nv.getShape();
            final Bounds shapeBounds = new BoundingBox(shape.getLayoutX(), shape.getLayoutY(), shape.getBoundsInLocal().getWidth(), shape.getBoundsInLocal().getHeight());

            final double angle;
            if (v.getDegree() == 1) {
                Edge e = v.getFirstAdjacentEdge();
                AEdgeView ev = edge2view.getValue(e);
                angle = GeometryUtils.computeAngle(nv.getLocation().subtract(ev.getReferencePoint()));
            } else {
                final ArrayList<Integer> array = new ArrayList<>(v.getDegree());
                for (Edge e : v.adjacentEdges()) {
                    final AEdgeView ev = edge2view.getValue(e);
                    final double alpha = GeometryUtils.modulo360(GeometryUtils.computeAngle(ev.getReferencePoint().subtract(nv.getLocation())));
                    array.add((int) Math.round(alpha));
                }
                array.sort(Comparator.naturalOrder());
                array.add(360 + array.get(0));
                int gap = 0;
                int best = 0;
                for (int next = 0; next < array.size() - 1; next++) {
                    final int nextGap = (int) Math.round(GeometryUtils.modulo360(array.get(next + 1) - array.get(next)));
                    if (nextGap > gap) {
                        best = next;
                        gap = nextGap;
                    }
                }
                angle = 0.5 * (array.get(best) + array.get(best + 1));
            }

            final javafx.scene.Node label = nv.getLabel();
            if (label != null) {
                // System.err.println("Label " + label.getLayoutBounds());

                Point2D location = new Point2D(0.5 * (shapeBounds.getMaxX() + shapeBounds.getMinX() - label.getLayoutBounds().getWidth()),
                        0.5 * (shapeBounds.getMaxY() + shapeBounds.getMinY() - label.getLayoutBounds().getHeight()));
                BoundingBox bbox = new BoundingBox(location.getX(), location.getY(), label.getLayoutBounds().getWidth(), label.getLayoutBounds().getHeight());

                boolean ok = false;
                while (!ok) {
                    ok = true;
                    for (BoundingBox other : boundsList) {
                        if (other.intersects(bbox)) {
                            location = GeometryUtils.translateByAngle(location, angle, 0.2 * bbox.getHeight());
                            bbox = new BoundingBox(location.getX(), location.getY(), Math.max(1, label.getLayoutBounds().getWidth()), Math.max(1, label.getLayoutBounds().getHeight()));
                            ok = false;
                            break;
                        }
                    }
                }
                boundsList.add(bbox);
                label.setLayoutX(bbox.getMinX());
                label.setLayoutY(bbox.getMinY());
            }
        }
    }

    /**
     * layout nodes in left-to-right tree layout
     *
     * @param tree
     * @param node2view
     * @param edge2view
     */
    public static void leftToRightLayout(PhyloGraph tree, Node root, NodeArray<ANodeView> node2view, EdgeArray<AEdgeView> edge2view) {
        for (Node v : tree.nodes()) {
            final ANodeView nv = node2view.getValue(v);
            if (nv.getLabel() != null && nv.getLabel() instanceof Label) {
                final Label label = (Label) nv.getLabel();
                final Point2D reference;
                if (nv.getShape() != null) {
                    final javafx.scene.Node shape = nv.getShape();
                    reference = new Point2D(shape.getLayoutX() + shape.getLayoutBounds().getWidth(),
                            shape.getLayoutY() - 0.5 * shape.getLayoutBounds().getHeight());
                } else
                    reference = nv.getLocation();

                if (v.isLeaf()) {
                    label.setLayoutX(reference.getX() + 5);
                    label.setLayoutY(reference.getY() - 0.5 * label.getHeight());
                } else if (v == root) {
                    label.setLayoutX(reference.getX() - label.getWidth() - 5);
                    label.setLayoutY(reference.getY() - 0.5 * label.getHeight());
                } else // internal node
                {
                    if (node2view.getValue(v.getFirstInEdge().getSource()).getLocation().getY() > nv.getLocation().getY()) {
                        // parent lies above
                        label.setLayoutX(reference.getX() - label.getWidth() - 5);
                        label.setLayoutY(reference.getY() - 0.5 * label.getHeight() - 4);
                    } else // below
                    {
                        label.setLayoutX(reference.getX() - label.getWidth() - 5);
                        label.setLayoutY(reference.getY() + 4);
                    }
                }
            }
        }
    }
}

