/*
 *  Copyright (C) 2019 Daniel H. Huson
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

import javafx.geometry.Point2D;
import jloda.graph.*;
import jloda.phylo.SplitsGraph;
import jloda.util.CanceledException;
import jloda.util.Pair;
import jloda.util.ProgressListener;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.gui.graphtab.base.GeometryUtils;
import splitstree5.utils.PhyloGraphUtils;
import splitstree5.utils.SplitsUtilities;

import java.util.*;

/**
 * the equal angle algorithm for computing a split network for circular splits
 * Daniel Huson, 11.2017
 */
public class EqualAngle {
    /**
     * apply the algorithm to build a new graph
     *
     * @param progress
     * @param useWeights
     * @param taxaBlock
     * @param splits
     * @param graph
     * @param node2point
     */
    public static void apply(ProgressListener progress, boolean useWeights, TaxaBlock taxaBlock, SplitsBlock splits, SplitsGraph graph, NodeArray<Point2D> node2point, BitSet forbiddenSplits, BitSet usedSplits) throws CanceledException {
        //System.err.println("Running equal angle algorithm");
        graph.clear();
        usedSplits.clear();

        progress.setTasks("Equal Angle", null);
        progress.setMaximum(100);    //initialize maximum progress
        progress.setProgress(-1);    //set progress to 0


        final int[] cycle = normalizeCycle(splits.getCycle());

        progress.setProgress(2);

        initGraph(taxaBlock, splits, cycle, graph);

        final List<Integer> interiorSplits = getNonTrivialSplitsOrdered(splits);

        progress.setSubtask("process internal splits");
        progress.setMaximum(interiorSplits.size());    //initialize maximum progress

        {
            int count = 0;
            for (Integer s : interiorSplits) {
                if (SplitsUtilities.isCircular(taxaBlock, cycle, splits.get(s))) {
                    wrapSplit(taxaBlock, splits, s, cycle, graph);
                    usedSplits.set(s, true);
                    progress.setProgress(++count);
                }
            }
        }

        progress.setProgress(-1);
        removeTemporaryTrivialEdges(graph);

        assignAnglesToEdges(taxaBlock.getNtax(), splits, cycle, graph, forbiddenSplits);

        progress.setProgress(90);

        // rotateAbout so that edge leaving first taxon ist pointing at 9 o'clock
        if (graph.getNumberOfNodes() > 0 && graph.getNumberOfEdges() > 0) {
            Node v = graph.getTaxon2Node(1);
            double angle = GeometryUtils.modulo360(180 + graph.getAngle(v.getFirstAdjacentEdge())); // add 180 to be consist with Embed
            for (Edge e : graph.edges()) {
                graph.setAngle(e, GeometryUtils.modulo360(graph.getAngle(e) - angle));
            }
        }
        if (node2point != null)
            assignCoordinatesToNodes(useWeights, graph, node2point); // need coordinates

        PhyloGraphUtils.addLabels(taxaBlock, graph);
        progress.setProgress(100);   //set progress to 100%
    }


    /**
     * initializes the graph
     *
     * @param taxa
     * @param splits
     * @param posOfTaxonInCycle
     * @param graph
     */
    private static void initGraph(TaxaBlock taxa, SplitsBlock splits, int[] posOfTaxonInCycle, SplitsGraph graph) {
        graph.clear();

        // map from each taxon to it's trivial split in splits
        final int[] taxon2TrivialSplit = new int[taxa.getNtax() + 1];

        for (int s = 1; s <= splits.getNsplits(); s++) {
            final ASplit split = splits.get(s);
            if (split.size() == 1) {
                final int t = split.getSmallerPart().nextSetBit(1);
                taxon2TrivialSplit[t] = s;
            }
        }

        final Node center = graph.newNode();
        for (int i = 1; i <= taxa.getNtax(); i++) {
            int t = posOfTaxonInCycle[i];

            Node v = graph.newNode();

            graph.addTaxon(v, t);

            Edge e = graph.newEdge(center, v);
            if (taxon2TrivialSplit[t] != 0) {
                int s = taxon2TrivialSplit[t];
                graph.setWeight(e, splits.get(s).getWeight());
                graph.setSplit(e, s);
            } else
                graph.setSplit(e, -1); // mark as temporary split
        }
    }

    /**
     * returns the list of all non-trivial splits, ordered by by increasing size
     * of the split part containing taxon 1
     *
     * @param splits
     * @return non-trivial splits
     */
    private static ArrayList<Integer> getNonTrivialSplitsOrdered(SplitsBlock splits) {
        final SortedSet<Pair<Integer, Integer>> interiorSplits = new TreeSet<>(new Pair<Integer, Integer>()); // first component is cardinality, second is id

        for (int s = 1; s <= splits.getNsplits(); s++) {
            final ASplit split = splits.get(s);
            if (split.size() > 1) {
                interiorSplits.add(new Pair<>(split.getPartContaining(1).cardinality(), s));
            }
        }
        final ArrayList<Integer> interiorSplitIDs = new ArrayList<>(interiorSplits.size());
        for (Pair<Integer, Integer> interiorSplit : interiorSplits) {
            interiorSplitIDs.add(interiorSplit.getSecond());
        }
        return interiorSplitIDs;
    }


    /**
     * normalizes cycle so that cycle[1]=1
     *
     * @param cycle
     * @return normalized cycle
     */
    private static int[] normalizeCycle(int[] cycle) {
        int[] result = new int[cycle.length];

        int i = 1;
        while (cycle[i] != 1 && i < cycle.length - 1)
            i++;
        int j = 1;
        while (i < cycle.length) {
            result[j] = cycle[i];
            i++;
            j++;
        }
        i = 1;
        while (j < result.length) {
            result[j] = cycle[i];
            i++;
            j++;
        }
        return result;
    }

    /**
     * adds an interior split using the wrapping algorithm
     *
     * @param taxa
     * @param cycle
     * @param splits
     * @param s
     * @param graph
     */
    private static void wrapSplit(TaxaBlock taxa, SplitsBlock splits, int s, int[] cycle, SplitsGraph graph) throws IllegalStateException {
        final BitSet part = splits.get(s).getPartNotContaining(1);

        int xp = 0; // first member of split part not containing taxon 1
        int xq = 0; // last member of split part not containing taxon 1
        for (int i = 1; i <= taxa.getNtax(); i++) {
            int t = cycle[i];
            if (part.get(t)) {
                if (xp == 0)
                    xp = t;
                xq = t;
            }
        }
        Node v = graph.getTaxon2Node(xp);
        final Node z = graph.getTaxon2Node(xq);
        final Edge targetLeafEdge = z.getFirstAdjacentEdge();

        Edge e = v.getFirstAdjacentEdge();
        v = graph.getOpposite(v, e);
        Node u = null;
        final ArrayList<Edge> leafEdges = new ArrayList<>(taxa.getNtax());
        leafEdges.add(e);
        Edge nextE;

        final NodeSet nodesVisited = new NodeSet(graph);

        do {
            Edge f = e;
            if (nodesVisited.contains(v)) {
                System.err.println(graph);

                throw new IllegalStateException("Node already visited: " + v);
            }
            nodesVisited.add(v);

            Edge f0 = f; // f0 is edge by which we enter the node
            f = v.getNextAdjacentEdgeCyclic(f0);
            while (isLeafEdge(f)) {
                leafEdges.add(f);
                if (f == targetLeafEdge) {
                    break;
                }
                if (f == f0)
                    throw new RuntimeException("Node wraparound: f=" + f + " f0=" + f0);
                f = v.getNextAdjacentEdgeCyclic(f);

            }
            if (isLeafEdge(f))
                nextE = null; // at end of chain
            else
                nextE = f; // continue along boundary
            Node w = graph.newNode();
            Edge h = graph.newEdge(w, null, v, f0, Edge.AFTER, Edge.AFTER, null);
            // here we make sure that new edge is inserted after f0

            graph.setSplit(h, s);
            graph.setWeight(h, splits.get(s).getWeight());
            if (u != null) {
                h = graph.newEdge(w, u, null);
                graph.setSplit(h, graph.getSplit(e));
                graph.setWeight(h, graph.getWeight(e));
            }
            for (Object leafEdge : leafEdges) {
                f = (Edge) leafEdge;
                h = graph.newEdge(w, graph.getOpposite(v, f));

                graph.setSplit(h, graph.getSplit(f));
                graph.setWeight(h, graph.getWeight(f));
                graph.deleteEdge(f);
            }
            leafEdges.clear();

            if (nextE != null) {
                v = graph.getOpposite(v, nextE);
                e = nextE;
                u = w;
            }
        } while (nextE != null);
    }

    /**
     * does this edge lead to a leaf?
     *
     * @param f
     * @return is leaf edge
     */
    private static boolean isLeafEdge(Edge f) {
        return f.getSource().getDegree() == 1 || f.getTarget().getDegree() == 1;
    }

    /**
     * this removes all temporary trivial edges added to the graph
     *
     * @param graph
     */
    private static void removeTemporaryTrivialEdges(SplitsGraph graph) {
        final EdgeSet tempEdges = new EdgeSet(graph);
        for (Edge e : graph.edges()) {
            if (graph.getSplit(e) == -1) // temporary leaf edge
                tempEdges.add(e);
        }

        for (Edge e : tempEdges) {
            Node v, w;
            if (e.getSource().getDegree() == 1) {
                v = e.getSource();
                w = e.getTarget();
            } else {
                w = e.getSource();
                v = e.getTarget();
            }
            for (Integer t : graph.getTaxa(v)) {
                graph.addTaxon(w, t);
            }
            graph.clearTaxa(v);
            graph.deleteNode(v);
        }
    }

    /**
     * assigns angles to all edges in the graph
     *
     * @param splits
     * @param cycle
     * @param graph
     * @param forbiddenSplits : set of all the splits such as their edges won't have their angles changed
     */
    public static void assignAnglesToEdges(int ntaxa, SplitsBlock splits, int[] cycle, SplitsGraph graph, BitSet forbiddenSplits) {
        //We create the list of angles representing the taxas on a circle.
        double[] TaxaAngles = new double[ntaxa + 1];
        for (int t = 1; t < ntaxa + 1; t++) {
            TaxaAngles[t] = (360 * t / (double) ntaxa);
        }

        double[] split2angle = new double[splits.getNsplits() + 1];

        assignAnglesToSplits(ntaxa, TaxaAngles, split2angle, splits, cycle);

        for (Edge e : graph.edges()) {
            if (!forbiddenSplits.get(graph.getSplit(e))) {
                graph.setAngle(e, split2angle[graph.getSplit(e)]);
            }
        }
    }

    /**
     * assigns angles to the splits in the graph, considering that they are located exactly "in the middle" of two taxa
     * so we fill split2angle using TaxaAngles.
     *
     * @param splits
     * @param cycle
     * @param TaxaAngles  for each taxa, its angle
     * @param split2angle for each split, its angle
     */
    private static void assignAnglesToSplits(int ntaxa, double[] TaxaAngles, double[] split2angle, SplitsBlock splits, int[] cycle) {
        for (int s = 1; s <= splits.getNsplits(); s++) {
            final BitSet part = splits.get(s).getPartNotContaining(1);
            int xp = 0; // first position of split part not containing taxon 1
            int xq = 0; // last position of split part not containing taxon 1
            for (int i = 1; i <= ntaxa; i++) {
                int t = cycle[i];
                if (part.get(t)) {
                    if (xp == 0)
                        xp = i;
                    xq = i;
                }
            }

            final int xpNeighbor = (xp - 2) % ntaxa + 1;
            final int xqNeighbor = (xq) % ntaxa + 1;
            //the split, when represented on the circle of the taxas, is a line which interescts the circle in two
            //places : SplitsByAngle is a sorted list (sorted by the angle of these intersections), where every
            // split thus appears 2 times (once per intersection)
            double TaxaAngleP;
            double TaxaAngleQ;
            TaxaAngleP = GeometryUtils.midAngle(TaxaAngles[xp], TaxaAngles[xpNeighbor]);
            TaxaAngleQ = GeometryUtils.midAngle(TaxaAngles[xq], TaxaAngles[xqNeighbor]);

            split2angle[s] = GeometryUtils.modulo360((TaxaAngleQ + TaxaAngleP) / 2);
            if (xqNeighbor == 1) {
                split2angle[s] = GeometryUtils.modulo360(split2angle[s] + 180);
            }
            //System.out.println("split from "+xp+","+xpneighbour+" ("+TaxaAngleP+") to "+xq+","+xqneighbour+" ("+TaxaAngleQ+") -> "+split2angle[s]+" $ "+(180 * (xp + xq)) / (double) ntaxa);s
        }
    }


    /**
     * assigns coordinates to nodes
     *
     * @param useWeights
     * @param graph
     */
    public static void assignCoordinatesToNodes(boolean useWeights, SplitsGraph graph, NodeArray<Point2D> node2point) {
        if (graph.getNumberOfNodes() == 0)
            return;
        final Node v = graph.getTaxon2Node(1);
        node2point.put(v, new Point2D(0, 0));

        final BitSet splitsInPath = new BitSet();
        NodeSet nodesVisited = new NodeSet(graph);

        assignCoordinatesToNodesRec(useWeights, v, splitsInPath, nodesVisited, graph, node2point);
    }


    /**
     * recursively assigns coordinates to all nodes
     *
     * @param useWeights
     * @param v
     * @param splitsInPath
     * @param nodesVisited
     * @param graph
     */
    private static void assignCoordinatesToNodesRec(boolean useWeights, Node v, BitSet splitsInPath, NodeSet nodesVisited, SplitsGraph graph, NodeArray<Point2D> node2point) {
        if (!nodesVisited.contains(v)) {
            nodesVisited.add(v);
            for (Edge e : v.adjacentEdges()) {
                int s = graph.getSplit(e);
                if (!splitsInPath.get(s)) {
                    Node w = graph.getOpposite(v, e);
                    Point2D p = GeometryUtils.translateByAngle(node2point.getValue(v), graph.getAngle(e), useWeights ? graph.getWeight(e) : 1);
                    node2point.setValue(w, p);
                    splitsInPath.set(s, true);
                    assignCoordinatesToNodesRec(useWeights, w, splitsInPath, nodesVisited, graph, node2point);
                    splitsInPath.set(s, false);
                }
            }
        }
    }

}
