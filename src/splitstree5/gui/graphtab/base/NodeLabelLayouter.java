/*
 * NodeLabelLayouter.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package splitstree5.gui.graphtab.base;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import jloda.fx.control.RichTextLabel;
import jloda.fx.util.GeometryUtilsFX;
import jloda.fx.util.ProgramExecutorService;
import jloda.graph.Edge;
import jloda.graph.EdgeArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloGraph;
import jloda.util.Basic;
import jloda.util.Single;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * node label layouter
 * Daniel Huson, 12.2017
 */
public class NodeLabelLayouter {
    /**
     * layout nodes for radial tree
     *
     * @param phyloGraph
     * @param node2view
     * @param edge2view
     */
    public static void radialLayout(boolean sparseLabels, PhyloGraph phyloGraph, NodeArray<NodeViewBase> node2view, EdgeArray<EdgeViewBase> edge2view) {
        final ArrayList<BoundingBox> shapeBoundsList = new ArrayList<>();

        for (Node v : phyloGraph.nodes()) {
            final NodeView2D nv = (NodeView2D) node2view.getValue(v);
            if (phyloGraph.getLabel(v) != null) {
                final javafx.scene.Node shape = nv.getShapeGroup();
                if (shape != null)
                    shapeBoundsList.add(new BoundingBox(shape.getTranslateX() - 0.5 * shape.getBoundsInLocal().getWidth(), shape.getTranslateY() - 0.5 * shape.getBoundsInLocal().getHeight(), shape.getLayoutBounds().getWidth(), shape.getLayoutBounds().getHeight()));
            }
        }

        final Single<NodeArray<Point2D>> best_node2labelTranslate = new Single<>(null);
        final Single<Double> best_strain = new Single<>(Double.POSITIVE_INFINITY);

        final int runs = (sparseLabels ? 1 : 100);

        ExecutorService executorService = Executors.newFixedThreadPool(ProgramExecutorService.getNumberOfCoresToUse());

        for (int i = 0; i < runs; i++) {
            final long seed = 666L * (i + 13);
            executorService.submit(() -> {
                final List<Node> nodes = Basic.randomize(phyloGraph.nodesList(), seed);

                final ArrayList<BoundingBox> labelBoundsList = new ArrayList<>();
                final NodeArray<Point2D> node2labelTranslate = new NodeArray<>(phyloGraph);
                double strain = 0;
                for (Node v : nodes) {
                    if (v.getDegree() > 0) {
                        final NodeView2D nv = (NodeView2D) node2view.getValue(v);
                        final javafx.scene.Node shape = nv.getShapeGroup();
                        final Bounds shapeBounds;
                        if (shape != null)
                            shapeBounds = new BoundingBox(shape.getTranslateX() - 0.5 * shape.getBoundsInLocal().getWidth(), shape.getTranslateY() - 0.5 * shape.getBoundsInLocal().getHeight(), shape.getBoundsInLocal().getWidth(), shape.getBoundsInLocal().getHeight());
                        else
                            shapeBounds = new BoundingBox(nv.getLocation().getX() - 1, nv.getLocation().getY() - 1, 2, 2);

                        final double angle;
                        if (v.getDegree() == 1) {
                            Edge e = v.getFirstAdjacentEdge();
                            EdgeView2D ev = (EdgeView2D) edge2view.getValue(e);
                            angle = GeometryUtilsFX.computeAngle(nv.getLocation().subtract(ev.getReferencePoint()));
                        } else {
                            final ArrayList<Integer> array = new ArrayList<>(v.getDegree());
                            for (Edge e : v.adjacentEdges()) {
                                EdgeView2D ev = (EdgeView2D) edge2view.getValue(e);
                                final double alpha = GeometryUtilsFX.modulo360(GeometryUtilsFX.computeAngle(ev.getReferencePoint().subtract(nv.getLocation())));
                                array.add((int) Math.round(alpha));
                            }
                            array.sort(Comparator.naturalOrder());
                            array.add(360 + array.get(0));
                            int gap = 0;
                            int best = 0;
                            for (int next = 0; next < array.size() - 1; next++) {
                                final int nextGap = (int) Math.round(GeometryUtilsFX.modulo360(array.get(next + 1) - array.get(next)));
                                if (nextGap > gap) {
                                    best = next;
                                    gap = nextGap;
                                }
                            }
                            angle = 0.5 * (array.get(best) + array.get(best + 1));
                        }

                        final RichTextLabel label = nv.getLabel();
                        if (label != null) {
                            nv.getLabel().setVisible(true);

                            Point2D location = new Point2D(0.5 * (shapeBounds.getMaxX() + shapeBounds.getMinX() - label.getLayoutBounds().getWidth()),
                                    0.5 * (shapeBounds.getMaxY() + shapeBounds.getMinY() - label.getLayoutBounds().getHeight()));
                            BoundingBox bbox = new BoundingBox(location.getX(), location.getY(), label.getLayoutBounds().getWidth(), label.getLayoutBounds().getHeight());


                            if (false) // debugging
                            {
                                ArrayList<javafx.scene.Node> rectangles = new ArrayList<>();
                                for (javafx.scene.Node node : nv.getShapeGroup().getChildren()) {
                                    if (node instanceof Rectangle)
                                        rectangles.add(node);
                                }
                                nv.getShapeGroup().getChildren().removeAll(rectangles);
                            }

                            if (false)// debugging
                            {
                                Rectangle rect = new Rectangle(shapeBounds.getMinX(), shapeBounds.getMinY(), shapeBounds.getWidth(), shapeBounds.getHeight());
                                rect.setFill(Color.TRANSPARENT);
                                rect.setStroke(Color.PINK);
                                nv.getShapeGroup().getChildren().add(rect);
                            }


                            boolean ok = false;
                            while (!ok) {
                                ok = true;
                                int count = 0;
                                for (BoundingBox other : iterator(shapeBoundsList.iterator(), labelBoundsList.iterator())) {
                                    if (other.intersects(bbox)) {
                                        if (count >= shapeBoundsList.size() && sparseLabels) {
                                            nv.getLabel().setVisible(false);
                                            ok = true; // done with this, it will be invisible
                                            break;
                                        }

                                        location = GeometryUtilsFX.translateByAngle(location, angle, 0.2 * bbox.getHeight());
                                        bbox = new BoundingBox(location.getX(), location.getY(), Math.max(1, label.getLayoutBounds().getWidth()), Math.max(1, label.getLayoutBounds().getHeight()));
                                        ok = false;
                                        if (false)// debugging
                                        {
                                            Rectangle rect = new Rectangle(bbox.getMinX(), bbox.getMinY(), label.getLayoutBounds().getWidth(), label.getLayoutBounds().getHeight());
                                            rect.setFill(Color.TRANSPARENT);
                                            rect.setStroke(Color.LIGHTGREEN);
                                            nv.getShapeGroup().getChildren().add(rect);
                                        }
                                        break;
                                    }
                                    count++;
                                }
                            }
                            if (nv.getLabel().isVisible())
                                labelBoundsList.add(bbox);
                            final Point2D offset = new Point2D(bbox.getMinX(), bbox.getMinY());
                            node2labelTranslate.put(v, offset);
                            strain += offset.magnitude() * offset.magnitude();
                            // todo: this algorithm needs improving!
                        }
                    }
                }
                synchronized (best_strain) {
                    if (strain < best_strain.get()) {
                        // System.err.println(best_strain + " -> " + strain);
                        best_strain.set(strain);
                        best_node2labelTranslate.set(node2labelTranslate);
                    }
                }
            });
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(1000, TimeUnit.DAYS);
        } catch (InterruptedException ignored) {
        }

        if (best_node2labelTranslate.get() != null) {
            for (var v : phyloGraph.nodes()) {
                Point2D translate = best_node2labelTranslate.get().get(v);
                if (translate != null) {
                    final NodeView2D nv = (NodeView2D) node2view.getValue(v);
                    if (nv.getLabel() != null) {
                        nv.getLabel().setTranslateX(translate.getX());
                        nv.getLabel().setTranslateY(translate.getY());
                    }
                }
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
    public static void leftToRightLayout(boolean sparseLabels, PhyloGraph tree, Node root, NodeArray<NodeViewBase> node2view, EdgeArray<EdgeViewBase> edge2view) {
        final ArrayList<BoundingBox> labelBoundsList = new ArrayList<>();

        for (Node v : tree.nodes()) {
            final NodeView2D nv = (NodeView2D) node2view.getValue(v);
            if (nv.getLabel() != null) {
                nv.getLabel().setVisible(true);
                final RichTextLabel label = nv.getLabel();

                final javafx.scene.Node shape = nv.getShapeGroup();
                final Point2D reference = new Point2D(shape.getTranslateX() + shape.getLayoutBounds().getWidth(),
                        shape.getTranslateY() - 0.5 * shape.getLayoutBounds().getHeight());

                if (v.isLeaf()) {
                    label.setTranslateX(reference.getX() + 5);
                    label.setTranslateY(reference.getY() - 0.5 * label.getHeight());
                } else if (v == root) {
                    label.setTranslateX(reference.getX() - label.getWidth() - 5);
                    label.setTranslateY(reference.getY() - 0.5 * label.getHeight());
                } else // internal node
                {
                    if (((NodeView2D) node2view.getValue(v.getFirstInEdge().getSource())).getLocation().getY() > nv.getLocation().getY()) {
                        // parent lies above
                        label.setTranslateX(reference.getX() - label.getWidth() - 5);
                        label.setTranslateY(reference.getY() - 0.5 * label.getHeight() - 4);
                    } else // below
                    {
                        label.setTranslateX(reference.getX() - label.getWidth() - 5);
                        label.setTranslateY(reference.getY() + 4);
                    }
                }

                if (sparseLabels) {
                    final BoundingBox bbox = new BoundingBox(label.getTranslateX(), label.getTranslateY(), label.getLayoutBounds().getWidth(), label.getLayoutBounds().getHeight());
                    for (BoundingBox other : labelBoundsList) {
                        if (bbox.intersects(other)) {
                            label.setVisible(false);
                            break;
                        }
                    }
                    if (label.isVisible())
                        labelBoundsList.add(bbox);
                }
            }
        }
    }

    /**
     * iterate over multiple iterators sequentially
     *
     * @param iterators
     * @param <T>
     * @return iterable over all given iterators
     */
    @SafeVarargs
    public static <T> Iterable<T> iterator(Iterator<T>... iterators) {
        return () -> new Iterator<T>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < iterators.length && iterators[i].hasNext();
            }

            @Override
            public T next() {
                T result = iterators[i].next();
                if (!iterators[i].hasNext())
                    i++;
                return result;
            }
        };
    }
}

