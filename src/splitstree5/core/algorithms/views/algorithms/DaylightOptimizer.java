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

package splitstree5.core.algorithms.views.algorithms;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point2D;
import jloda.graph.*;
import jloda.phylo.SplitsGraph;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.Pair;
import jloda.util.ProgressListener;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.gui.graphtab.base.GeometryUtils;

/**
 * run the daylight optimizer
 * Phillipp Gambette and Daniel Huson, 2004
 */
public class DaylightOptimizer {
    private static DaylightOptimizer instance;

    private ProgressListener progress;
    private final IntegerProperty optionIterations = new SimpleIntegerProperty(20);
    private final BooleanProperty optionUseWeights = new SimpleBooleanProperty(true);

    public static DaylightOptimizer getInstance() {
        if (instance == null)
            instance = new DaylightOptimizer();
        return instance;
    }

    /**
     * apply the algorithm to build a new graph
     *
     * @param progress
     * @param taxa
     * @param phyloGraph
     * @param node2point
     */
    public void apply(ProgressListener progress, TaxaBlock taxa, SplitsGraph phyloGraph, NodeArray<javafx.geometry.Point2D> node2point) throws CanceledException {
        this.progress = progress;
        System.err.println("Running daylight optimizer");
        runOptimizeDayLight(taxa, phyloGraph, node2point);
    }

    /**
     * runs the optimize daylight algorithm
     *
     * @param taxa
     */
    private void runOptimizeDayLight(TaxaBlock taxa, SplitsGraph graph, NodeArray<javafx.geometry.Point2D> node2point) throws CanceledException {
        NodeSet ignore = new NodeSet(graph);

        progress.setTasks("Optimize daylight", "iterating");
        progress.setMaximum(getOptionIterations() * graph.getNumberOfNodes());
        progress.setProgress(0);

        final Node[] nodes = new Node[graph.getNumberOfNodes()];
        {
            int i = 0;
            for (Node v : graph.nodes()) {
                nodes[i++] = v;
            }
        }

        // use different randomized orders
        for (int i = 1; i <= getOptionIterations(); i++) {
            for (Node v : Basic.randomize(nodes, 77 * i)) {
                if (v.getDegree() > 1 && !ignore.contains(v)) {
                    if (!optimizeDaylightNode(taxa.getNtax(), graph, v, node2point))
                        ignore.add(v);
                    else
                        EqualAngle.assignCoordinatesToNodes(isOptionUseWeights(), graph, node2point);
                }
                progress.incrementProgress();
            }
        }
    }

    /**
     * optimize the daylight angles of the graph
     *
     * @param v
     */
    private boolean optimizeDaylightNode(int ntax, SplitsGraph graph, Node v, NodeArray<javafx.geometry.Point2D> node2point) throws NotOwnerException, CanceledException {
        int numComp = 0;
        EdgeIntegerArray edge2comp = new EdgeIntegerArray(graph);
        double[] comp2MinAngle = new double[ntax + 1];
        double[] comp2MaxAngle = new double[ntax + 1];

        // for all edges adjacent to v
        for (Edge e : v.adjacentEdges()) {
            progress.checkForCancel();

            if (edge2comp.getValue(e) == 0) {
                edge2comp.set(e, ++numComp);
                Node w = graph.getOpposite(v, e);

                // as observed from v
                final double angle;
                {
                    Point2D vp = node2point.get(v);
                    Point2D wp = node2point.get(w);
                    angle = GeometryUtils.computeAngle(wp.subtract(vp));
                }
                Pair<Double, Double> minMaxAngle = new Pair<>(angle, angle); // will contain min and max angles of component

                NodeSet visited = new NodeSet(graph);
                visitComponentRec(v, w, null, edge2comp, numComp, graph, node2point, visited, angle, minMaxAngle);
                if (visited.size() == graph.getNumberOfNodes())
                    return false; // visited all nodes, forget it.

                comp2MinAngle[numComp] = minMaxAngle.getFirst();
                comp2MaxAngle[numComp] = minMaxAngle.getSecond();
            }
        }
        if (numComp > 1) {
            double total = 0;
            for (int c = 1; c <= numComp; c++) {
                total += comp2MaxAngle[c] - comp2MinAngle[c];
            }
            if (total < 360) {
                double daylightGap = (360.0 - total) / numComp;
                double[] comp2epsilon = new double[numComp + 1];
                for (int c = 1; c <= numComp; c++) {
                    double alpha = 0;
                    for (int i = 1; i < c; i++)
                        alpha += comp2MaxAngle[i] - comp2MinAngle[i];
                    alpha += (c - 1) * daylightGap;
                    comp2epsilon[c] = alpha - comp2MinAngle[c];
                }
                for (Edge e : graph.edges()) {
                    int c = edge2comp.getValue(e);
                    graph.setAngle(e, GeometryUtils.modulo360(graph.getAngle(e) + comp2epsilon[c]));
                }
            }
        }
        return true;
    }

    /**
     * recursively visit the whole subgraph, obtaining the min and max observed angle
     *
     * @param root
     * @param v
     * @param e
     * @param edge2comp
     * @param numComp
     * @param graph
     * @param visited
     * @param minMaxAngle
     */
    private void visitComponentRec(Node root, Node v, Edge e, EdgeIntegerArray edge2comp, int numComp, SplitsGraph graph, NodeArray<javafx.geometry.Point2D> node2point, NodeSet visited,
                                   double angle, Pair<Double, Double> minMaxAngle) throws CanceledException {

        if (v != root && !visited.contains(v)) {
            progress.checkForCancel();

            visited.add(v);
            for (Edge f : v.adjacentEdges()) {
                if (f != e && edge2comp.getValue(f) == 0) {
                    edge2comp.set(f, numComp);
                    Node w = graph.getOpposite(v, f);
                    double newAngle = angle + GeometryUtils.computeObservedAngle(node2point.get(root), node2point.get(v), node2point.get(w));
                    if (newAngle < minMaxAngle.getFirst())
                        minMaxAngle.setFirst(newAngle);
                    if (newAngle > minMaxAngle.getSecond())
                        minMaxAngle.setSecond(newAngle);
                    visitComponentRec(root, w, f, edge2comp, numComp, graph, node2point, visited, newAngle, minMaxAngle);
                }
            }
        }
    }

    public int getOptionIterations() {
        return optionIterations.get();
    }

    public IntegerProperty optionIterationsProperty() {
        return optionIterations;
    }

    public void setOptionIterations(int optionIterations) {
        this.optionIterations.set(optionIterations);
    }

    public boolean isOptionUseWeights() {
        return optionUseWeights.get();
    }

    public BooleanProperty optionUseWeightsProperty() {
        return optionUseWeights;
    }

    public void setOptionUseWeights(boolean optionUseWeights) {
        this.optionUseWeights.set(optionUseWeights);
    }
}
