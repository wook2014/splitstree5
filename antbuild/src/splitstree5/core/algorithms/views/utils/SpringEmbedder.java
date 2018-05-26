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

package splitstree5.core.algorithms.views.utils;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.graph.NodeDoubleArray;
import jloda.phylo.PhyloGraph;
import jloda.phylo.SplitsGraph;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.gui.graphtab.base.GeometryUtils;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 * computes a spring embedding
 */
public class SpringEmbedder {
    /**
     * Computes a spring embedding of the graph
     *
     * @param iterations the number of iterations used
     */
    public void computeSpringEmbedding(ProgressListener progress, int iterations, PhyloGraph graph, NodeArray<Point2D> node2location) {
        final NodeDoubleArray xPos = new NodeDoubleArray(graph);
        final NodeDoubleArray yPos = new NodeDoubleArray(graph);

        final Bounds rect = computeBBox(node2location);
        final double width = rect.getWidth();
        final double height = rect.getHeight();

        if (graph.getNumberOfNodes() < 2)
            return;

        try {
            progress.setTasks("Spring embedder", "Iterating");
            progress.setMaximum(iterations);    //initialize maximum progress
            progress.setProgress(0);


            for (Node v : graph.nodes()) {
                Point2D p = node2location.getValue(v);
                xPos.setValue(v, p.getX());
                yPos.setValue(v, p.getY());
            }

            // run iterations of spring embedding:
            double log2 = Math.log(2);
            for (int count = 1; count <= iterations; count++) {
                double k = Math.sqrt(width * height / graph.getNumberOfNodes()) / 2;

                double l2 = 25 * log2 * Math.log(1 + count);

                double tx = width / l2;
                double ty = height / l2;

                NodeDoubleArray xDispl = new NodeDoubleArray(graph);
                NodeDoubleArray yDispl = new NodeDoubleArray(graph);

                // repulsive forces

                for (Node v : graph.nodes()) {
                    double xv = xPos.getValue(v);
                    double yv = yPos.getValue(v);

                    for (Node u : graph.nodes()) {
                        if (u == v)
                            continue;
                        double xdist = xv - xPos.getValue(u);
                        double ydist = yv - yPos.getValue(u);
                        double dist = xdist * xdist + ydist * ydist;
                        if (dist < 1e-3)
                            dist = 1e-3;
                        double frepulse = k * k / dist;
                        xDispl.setValue(v, xDispl.getValue(v) + frepulse * xdist);
                        yDispl.setValue(v, yDispl.getValue(v) + frepulse * ydist);
                    }

                    for (Edge e : graph.edges()) {
                        Node a = e.getSource();
                        Node b = e.getTarget();
                        if (a == v || b == v)
                            continue;
                        double xdist = xv -
                                (xPos.getValue(a) + xPos.getValue(b)) / 2;
                        double ydist = yv -
                                (yPos.getValue(a) + yPos.getValue(b)) / 2;
                        double dist = xdist * xdist + ydist * ydist;
                        if (dist < 1e-3) dist = 1e-3;
                        double frepulse = k * k / dist;
                        xDispl.setValue(v, xDispl.getValue(v) + frepulse * xdist);
                        yDispl.setValue(v, yDispl.getValue(v) + frepulse * ydist);
                    }
                }

                // attractive forces

                for (Edge e : graph.edges()) {
                    Node u = e.getSource();
                    Node v = e.getTarget();

                    double xdist = xPos.getValue(v) - xPos.getValue(u);
                    double ydist = yPos.getValue(v) - yPos.getValue(u);

                    double dist = Math.sqrt(xdist * xdist + ydist * ydist);

                    double f = ((u.getDegree() + v.getDegree()) / 16.0);

                    dist /= f;

                    xDispl.setValue(v, xDispl.getValue(v) - xdist * dist / k);
                    yDispl.setValue(v, yDispl.getValue(v) - ydist * dist / k);
                    xDispl.setValue(u, xDispl.getValue(u) + xdist * dist / k);
                    yDispl.setValue(u, yDispl.getValue(u) + ydist * dist / k);
                }

                // preventions

                for (Node v : graph.nodes()) {
                    double xd = xDispl.getValue(v);
                    double yd = yDispl.getValue(v);

                    double dist = Math.sqrt(xd * xd + yd * yd);

                    xd = tx * xd / dist;
                    yd = ty * yd / dist;

                    double xp = xPos.getValue(v) + xd;
                    double yp = yPos.getValue(v) + yd;

                    xPos.setValue(v, xp);
                    yPos.setValue(v, yp);
                }
                progress.setProgress(count);
            }
        } catch (CanceledException ex) {
            progress.setUserCancelled(false);
        } finally {
            // set node positions
            for (Node v : graph.nodes()) {
                node2location.put(v, new Point2D(xPos.getValue(v), yPos.getValue(v)));
            }
        }
    }

    /**
     * computes the bounding box of all locations
     *
     * @param node2location
     * @return bounding box
     */
    private static Bounds computeBBox(NodeArray<Point2D> node2location) {
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        for (Point2D point : node2location.values()) {
            minX = Math.min(minX, point.getX());
            maxX = Math.max(maxX, point.getX());
            minY = Math.min(minY, point.getY());
            maxY = Math.max(maxY, point.getY());
        }
        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * compute the average split angle
     *
     * @param graph
     * @param node2point
     */
    public void computeAverageSplitAngles(SplitsGraph graph, NodeArray<Point2D> node2point) {
        final Map<Integer, ArrayList<Double>> split2angles = new HashMap<>();
        visitAnglesRec(graph, graph.getFirstNode(), null, node2point, split2angles, new BitSet());

        for (Edge e : graph.edges()) {
            int splitId = graph.getSplit(e);
            double angle = average(split2angles.get(splitId));
            graph.setAngle(e, angle);
        }
    }

    /**
     * recursively do the work
     *
     * @param graph
     * @param v
     * @param e
     * @param node2point
     * @param split2angles
     * @param splitsInPath
     */
    private void visitAnglesRec(SplitsGraph graph, Node v, Edge e, NodeArray<Point2D> node2point, Map<Integer, ArrayList<Double>> split2angles, BitSet splitsInPath) {
        for (Edge f : v.adjacentEdges()) {
            if (f != e) {
                Node w = f.getOpposite(v);
                int splitId = graph.getSplit(f);
                double angle;
                if (!splitsInPath.get(splitId)) {
                    splitsInPath.set(splitId);
                    angle = GeometryUtils.computeAngle(node2point.get(w).subtract(node2point.get(v)));
                    final ArrayList<Double> array = split2angles.computeIfAbsent(splitId, k -> new ArrayList<>());
                    array.add(angle);
                    visitAnglesRec(graph, w, f, node2point, split2angles, splitsInPath);
                    splitsInPath.set(splitId, false);
                }
            }
        }
    }

    /**
     * get the average value
     *
     * @param array
     * @return average
     */
    public static double average(ArrayList<Double> array) {
        if (array.size() > 0) {
            double sum = 0;
            for (Double d : array)
                sum += d;
            return sum / array.size();
        } else
            return 0;
    }
}
