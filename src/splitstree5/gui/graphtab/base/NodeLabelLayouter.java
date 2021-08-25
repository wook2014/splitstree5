/*
 * NodeLabelLayouter.java Copyright (C) 2021. Daniel H. Huson
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

import javafx.application.Platform;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
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
import jloda.util.Triplet;
import jloda.util.interval.Interval;
import jloda.util.interval.IntervalTree;

import java.util.*;
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
        final ArrayList<BoundingBox> nodeShapes = new ArrayList<>(phyloGraph.getNumberOfNodes());
        final ArrayList<Triplet<BoundingBox, Node, Double>> labelShapes = new ArrayList<>(phyloGraph.getNumberOfNodes());

        for (Node v : phyloGraph.nodes()) {
            final NodeView2D nv = (NodeView2D) node2view.get(v);
            if (nv.getLabel() != null) {
                final RichTextLabel label = nv.getLabel();
                if (label != null) {
                    final BoundingBox shapeBounds;
                    final javafx.scene.Node shape = nv.getShapeGroup();
                    if (shape != null)
                        shapeBounds = new BoundingBox(shape.getTranslateX(), shape.getTranslateY(), shape.getBoundsInLocal().getWidth(), shape.getBoundsInLocal().getHeight());
                    else
                        shapeBounds = new BoundingBox(nv.getLocation().getX() - 1, nv.getLocation().getY() - 1, 2, 2);
                    nodeShapes.add(shapeBounds);

                    nv.getLabel().setVisible(true);
                    final Point2D location = new Point2D(shapeBounds.getMinX() + 0.5 * (shapeBounds.getWidth() - label.getLayoutBounds().getWidth()),
                            shapeBounds.getMinY() + 0.5 * (shapeBounds.getHeight() - label.getLayoutBounds().getHeight()));
                    final BoundingBox bbox = new BoundingBox(location.getX(), location.getY(), label.getLayoutBounds().getWidth(), label.getLayoutBounds().getHeight());

                    final double angle;

                    if (v.getDegree() == 1) {
                        Edge e = v.getFirstAdjacentEdge();
                        EdgeView2D ev = (EdgeView2D) edge2view.get(e);
                        angle = GeometryUtilsFX.computeAngle(nv.getLocation().subtract(ev.getReferencePoint()));
                    } else {
                        final ArrayList<Integer> array = new ArrayList<>(v.getDegree());
                        for (Edge e : v.adjacentEdges()) {
                            EdgeView2D ev = (EdgeView2D) edge2view.get(e);
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
                    labelShapes.add(new Triplet<>(bbox, v, angle));
                }
            }
        }

        if (sparseLabels) {
            final IntervalTree<BoundingBox> xIntervals = new IntervalTree<>();
            final IntervalTree<BoundingBox> yIntervals = new IntervalTree<>();

            for (BoundingBox bbox : nodeShapes) {
                xIntervals.add(scaledInt(bbox.getMinX()), scaledInt(bbox.getMaxX()), bbox);
                yIntervals.add(scaledInt(bbox.getMinY()), scaledInt(bbox.getMaxY()), bbox);
            }

            final IntervalTree<BoundingBox> xIntervalsLabels = new IntervalTree<>();
            final IntervalTree<BoundingBox> yIntervalsLabels = new IntervalTree<>();

            for (Triplet<BoundingBox, Node, Double> triplet : labelShapes) {
                BoundingBox bbox = triplet.getFirst();
                final Node v = triplet.getSecond();
                final double angle = triplet.getThird();

                while (overlaps(bbox, xIntervals, yIntervals)) {
                    final Point2D translated = GeometryUtilsFX.translateByAngle(new Point2D(bbox.getMinX(), bbox.getMinY()), angle, 5);
                    bbox = new BoundingBox(translated.getX(), translated.getY(), bbox.getWidth(), bbox.getHeight());
                }
                final NodeView2D nv = (NodeView2D) node2view.get(v);
                if (!overlaps(bbox, xIntervalsLabels, yIntervalsLabels)) {
                    xIntervalsLabels.add(new Interval<>(scaledInt(bbox.getMinX()), scaledInt(bbox.getMaxX()), bbox));
                    yIntervalsLabels.add(new Interval<>(scaledInt(bbox.getMinY()), scaledInt(bbox.getMaxY()), bbox));
                } else
                    nv.getLabel().setVisible(false);
                nv.getLabel().setTranslateX(bbox.getMinX());
                nv.getLabel().setTranslateY(bbox.getMinY());
            }
        } else {

            final Single<ArrayList<Triplet<BoundingBox, Node, Double>>> bestLabelShapes = new Single<>(null);
            final Single<Double> best_stress = new Single<>(Double.POSITIVE_INFINITY);

            final int runs = 100;

            ExecutorService executorService = Executors.newFixedThreadPool(ProgramExecutorService.getNumberOfCoresToUse());

            for (int i = 0; i < runs; i++) {
                final long seed = 666L * (i + 13);
                executorService.submit(() -> {
                    final IntervalTree<BoundingBox> xIntervals = new IntervalTree<>();
                    final IntervalTree<BoundingBox> yIntervals = new IntervalTree<>();

                    for (BoundingBox bbox : nodeShapes) {
                        xIntervals.add(scaledInt(bbox.getMinX()), scaledInt(bbox.getMaxX()), bbox);
                        yIntervals.add(scaledInt(bbox.getMinY()), scaledInt(bbox.getMaxY()), bbox);
                    }

                    final ArrayList<Triplet<BoundingBox, Node, Double>> labels = new ArrayList<>();
                    double stress = 0;

                    for (Triplet<BoundingBox, Node, Double> triplet : Basic.randomize(labelShapes, seed)) {
                        BoundingBox bbox = triplet.getFirst();
                        final Node v = triplet.getSecond();
                        final double angle = triplet.getThird();
                        while (overlaps(bbox, xIntervals, yIntervals)) {
                            final Point2D translated = GeometryUtilsFX.translateByAngle(new Point2D(bbox.getMinX(), bbox.getMinY()), angle, 5);
                            bbox = new BoundingBox(translated.getX(), translated.getY(), bbox.getWidth(), bbox.getHeight());
                        }
                        stress += (triplet.getFirst().getMinX() - bbox.getMinX()) * (triplet.getFirst().getMinX() - bbox.getMinX()) +
                                (triplet.getFirst().getMinY() - bbox.getMinY()) * (triplet.getFirst().getMinY() - bbox.getMinY());

                        xIntervals.add(new Interval<>(scaledInt(bbox.getMinX()), scaledInt(bbox.getMaxX()), bbox));
                        yIntervals.add(new Interval<>(scaledInt(bbox.getMinY()), scaledInt(bbox.getMaxY()), bbox));
                        labels.add(new Triplet<>(bbox, v, angle));
                    }
                    synchronized (best_stress) {
                        if (stress < best_stress.get()) {
                            best_stress.set(stress);
                            bestLabelShapes.set(labels);
                        }
                    }
                });
            }

            final ExecutorService single = Executors.newSingleThreadExecutor();
            single.submit(() -> {
                executorService.shutdown();
                try {
                    executorService.awaitTermination(1000, TimeUnit.DAYS);
                } catch (InterruptedException ignored) {
                }
                if (bestLabelShapes.get() != null) {
                    Platform.runLater(() -> {
                        for (Triplet<BoundingBox, Node, Double> triplet : bestLabelShapes.get()) {
                            if (triplet.getSecond().getOwner() != null) {
                                final NodeView2D nv = (NodeView2D) node2view.get(triplet.getSecond());
                                nv.getLabel().setTranslateX(triplet.getFirst().getMinX());
                                nv.getLabel().setTranslateY(triplet.getFirst().getMinY());
                            }
                        }
                    });
                    single.shutdown();
                }
            });
        }
    }

    private static boolean overlaps(BoundingBox bbox, IntervalTree<BoundingBox> xIntervals, IntervalTree<BoundingBox> yIntervals) {
        final Set<BoundingBox> yBoxes = new HashSet<>(yIntervals.get(scaledInt(bbox.getMinY()), scaledInt(bbox.getMaxY())));
        return xIntervals.get(scaledInt(bbox.getMinX()), scaledInt(bbox.getMaxX())).stream().anyMatch(yBoxes::contains);
    }

    public static int scaledInt(double value) {
        return (int) Math.round(10.0 * value);
    }

    /**
     * layout nodes in left-to-right tree layout
     *
     * @param tree
     * @param node2view
     * @param edge2view
     */
    public static void leftToRightLayout(boolean sparseLabels, boolean alignLeafLabels, PhyloGraph tree, Node root, NodeArray<NodeViewBase> node2view, EdgeArray<EdgeViewBase> edge2view) {
        final ArrayList<BoundingBox> labelBoundsList = new ArrayList<>();

        for (Node v : tree.nodes()) {
            final NodeView2D nv = (NodeView2D) node2view.get(v);
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
                    if (((NodeView2D) node2view.get(v.getFirstInEdge().getSource())).getLocation().getY() > nv.getLocation().getY()) {
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
        if (alignLeafLabels) {
            var labelMaxX = tree.nodeStream().filter(v -> v.getOutDegree() == 0).map(v -> node2view.get(v).getLabel())
                    .mapToDouble(javafx.scene.Node::getTranslateX).max().orElse(0.0);
            if (labelMaxX > 0.0) {
                tree.nodeStream().filter(v -> v.getOutDegree() == 0).map(v -> node2view.get(v).getLabel())
                        .forEach(label -> label.setTranslateX(labelMaxX));
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

