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

package splitstree5.core.algorithms.views.treeview;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.control.Label;
import jloda.graph.EdgeArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloTree;

import java.util.ArrayList;

/**
 * node label layouter
 */
public class NodeLabelLayouter {
    private final static Point3D zAxis = new Point3D(0, 0, 10);

    /**
     * layout nodes for radial tree
     *
     * @param tree
     * @param node2view
     * @param edge2view
     */
    public static void radialLayout(PhyloTree tree, NodeArray<PhylogeneticNodeView> node2view, EdgeArray<PhylogeneticEdgeView> edge2view) {
        ArrayList<BoundingBox> boundsList = new ArrayList<>();

        if (true) {
            for (Node v : tree.nodes()) {
                PhylogeneticNodeView nv = node2view.get(v);
                Group parts = nv.getParts();
                Bounds bounds = parts.getLayoutBounds();
                boundsList.add(new BoundingBox(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight()));
            }

            for (Node v : tree.nodes()) {
                PhylogeneticNodeView nv = node2view.get(v);
                Group parts = nv.getParts();
                Bounds partsBounds = parts.getLayoutBounds();
                Point2D sum = null;
                for (Node w : v.adjacentNodes()) {
                    if (sum == null)
                        sum = nv.getLocation().subtract(node2view.get(w).getLocation());
                    else
                        sum = sum.add(nv.getLocation().subtract(node2view.get(w).getLocation()));
                }
                final double angle;
                if (sum == null)
                    angle = 0;
                else {
                    sum = sum.multiply(1);
                    angle = GeometryUtils.computeAngle(sum);
                }

                Group labels = nv.getLabels();
                if (labels != null && labels.getChildren().size() > 0) {
                    javafx.scene.Node label = labels.getChildren().get(0);
                    Point2D location = new Point2D(0.5 * (partsBounds.getMaxX() + partsBounds.getMinX() - label.getLayoutBounds().getWidth()),
                            0.5 * (partsBounds.getMaxY() + partsBounds.getMinY() - label.getLayoutBounds().getHeight()));
                    BoundingBox bbox = new BoundingBox(location.getX(), location.getY(), label.getLayoutBounds().getWidth(), label.getLayoutBounds().getHeight());

                    boolean ok = false;
                    while (!ok) {
                        ok = true;
                        for (BoundingBox other : boundsList) {
                            if (other.intersects(bbox)) {
                                location = GeometryUtils.translateByAngle(location, angle, 0.2 * bbox.getHeight());
                                bbox = new BoundingBox(location.getX(), location.getY(), label.getLayoutBounds().getWidth(), label.getLayoutBounds().getHeight());
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
        } else if (false)
            for (Node v : tree.nodes()) {
                if (v.isLeaf()) {
                    if (v.getInDegree() > 0) {
                        PhylogeneticNodeView nvParent = node2view.get(v.getFirstInEdge().getSource());
                        PhylogeneticNodeView nv = node2view.get(v);

                        Group labels = nv.getLabels();
                        if (labels != null && labels.getChildren().size() > 0) {
                            double angle = GeometryUtils.computeAngle(nv.getLocation().subtract(nvParent.getLocation()));
                            for (javafx.scene.Node node : labels.getChildren()) {
                                if (node instanceof Label) {
                                    final boolean leftHalf = (angle > 90 && angle < 270);
                                    final boolean leftOrRightQuarter = (angle >= 135 && angle <= 225 || angle <= 45 || angle >= 315);

                                    BoundingBox bbox = new BoundingBox(node.getLayoutX(), node.getLayoutY(), node.getLayoutBounds().getWidth(), node.getLayoutBounds().getHeight());

                                    double distance = 0;

                                    if (leftHalf)
                                        distance += bbox.getWidth();
                                    if (leftOrRightQuarter)
                                        distance += 20;
                                    else
                                        distance += 10;

                                    Point2D translate = GeometryUtils.translateByAngle(nv.getLocation(), angle, distance);

                                    node.setLayoutX(translate.getX());
                                    node.setLayoutY(translate.getY());

                                    if (leftOrRightQuarter)
                                        node.setLayoutY(node.getLayoutY() - 0.5 * bbox.getHeight());

                                    final double x = bbox.getMinX();
                                    final double y = bbox.getMinY();

                                    for (int pos = 0; pos < 9; ) {
                                        boolean ok = true;
                                        for (Bounds other : boundsList) {
                                            if (bbox.intersects(other)) {
                                                ok = false;
                                                break;
                                            }
                                        }
                                        if (ok) {
                                            if (pos > 0) {
                                                node.setLayoutX(bbox.getMinX());
                                                node.setLayoutY(bbox.getMinY());
                                            }
                                            boundsList.add(bbox);
                                            break;
                                        } else {
                                            pos++;
                                            switch (pos) {
                                                case 1: {
                                                    node.setLayoutX(x - bbox.getWidth());
                                                    node.setLayoutY(y - bbox.getHeight());
                                                    break;
                                                }
                                                case 2: {
                                                    node.setLayoutY(y - bbox.getHeight());
                                                    break;
                                                }
                                                case 3: {
                                                    node.setLayoutX(x + bbox.getWidth());
                                                    node.setLayoutY(y - bbox.getHeight());
                                                    break;
                                                }
                                                case 4: {
                                                    node.setLayoutX(x + bbox.getWidth());
                                                    break;
                                                }
                                                case 5: {
                                                    node.setLayoutX(x + bbox.getWidth());
                                                    node.setLayoutY(y + bbox.getHeight());
                                                    break;
                                                }
                                                case 6: {
                                                    node.setLayoutX(x + bbox.getWidth());
                                                    break;
                                                }
                                                case 7: {
                                                    node.setLayoutX(x - bbox.getWidth());
                                                    node.setLayoutY(y + bbox.getHeight());
                                                    break;
                                                }
                                                case 8: {
                                                    node.setLayoutX(x - bbox.getWidth());
                                                    break;
                                                }
                                            }
                                            bbox = new BoundingBox(node.getLayoutX(), node.getLayoutY(), node.getLayoutBounds().getWidth(), node.getLayoutBounds().getHeight());
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
    }
}

