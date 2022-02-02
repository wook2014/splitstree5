/*
 * EqualAngle.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.core.algorithms.views.algo;

import javafx.geometry.Point2D;
import jloda.fx.util.GeometryUtilsFX;
import jloda.graph.*;
import jloda.phylo.PhyloSplitsGraph;
import jloda.util.CanceledException;
import jloda.util.Pair;
import jloda.util.progress.ProgressListener;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.utils.PhyloGraphUtils;
import splitstree5.utils.SplitsUtilities;

import java.util.*;

/**
 * the equals angle algorithm for computing a split network for circular splits
 * Daniel Huson, 11.2017
 */
public class EqualAngle {
    /**
     * apply the algorithm to build a new graph
     *
	 */
    public static void apply(ProgressListener progress, boolean useWeights, TaxaBlock taxaBlock, SplitsBlock splits, PhyloSplitsGraph graph, NodeArray<Point2D> node2point, BitSet forbiddenSplits, BitSet usedSplits) throws CanceledException {
        //System.err.println("Running equals angle algorithm");
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

        assignAnglesToEdges(taxaBlock.getNtax(), splits, cycle, graph, forbiddenSplits, 360);

        progress.setProgress(90);

        // rotateAbout so that edge leaving first taxon ist pointing at 9 o'clock
        if (graph.getNumberOfNodes() > 0 && graph.getNumberOfEdges() > 0) {
            Node v = graph.getTaxon2Node(1);
            double angle = GeometryUtilsFX.modulo360(180 + graph.getAngle(v.getFirstAdjacentEdge())); // add 180 to be consist with Embed
            for (Edge e : graph.edges()) {
                graph.setAngle(e, GeometryUtilsFX.modulo360(graph.getAngle(e) - angle));
            }
        }
        if (node2point != null)
            assignCoordinatesToNodes(useWeights, graph, node2point, 1); // need coordinates

        PhyloGraphUtils.addLabels(taxaBlock, graph);
        progress.setProgress(100);   //set progress to 100%

        if (false) {
            for (Node v : graph.nodes()) {
                if (graph.getLabel(v) != null)
                    System.err.println("Node " + v.getId() + " " + graph.getLabel(v));
            }
            for (Edge e : graph.edges()) {
                System.err.println("Edge " + e.getSource().getId() + " - " + e.getTarget().getId() + " split: " + graph.getSplit(e));
            }
        }

        progress.setSubtask("");
        progress.setMaximum(-1);
        progress.setProgress(-1);
    }


    /**
     * initializes the graph
     *
	 */
    private static void initGraph(TaxaBlock taxa, SplitsBlock splits, int[] posOfTaxonInCycle, PhyloSplitsGraph graph) {
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
     * @return non-trivial splits
     */
    private static ArrayList<Integer> getNonTrivialSplitsOrdered(SplitsBlock splits) {
        final SortedSet<Pair<Integer, Integer>> interiorSplits = new TreeSet<>(new Pair<>()); // first component is cardinality, second is id

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
	 */
    private static void wrapSplit(TaxaBlock taxa, SplitsBlock splits, int s, int[] cycle, PhyloSplitsGraph graph) throws IllegalStateException {
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
        final Node vp = graph.getTaxon2Node(xp);
        final Node innerP = vp.getFirstAdjacentEdge().getOpposite(vp);
        final Node vq = graph.getTaxon2Node(xq);
        final Node innerQ = vq.getFirstAdjacentEdge().getOpposite(vq);
        final Edge targetLeafEdge = vq.getFirstAdjacentEdge();

        Edge e = vp.getFirstAdjacentEdge();
        Node v = graph.getOpposite(vp, e);  // node on existing boundary path

        final ArrayList<Edge> leafEdges = new ArrayList<>(taxa.getNtax());
        leafEdges.add(e);

        final NodeSet nodesVisited = new NodeSet(graph);

        Node prevU = null; // previous node on newly created boundary path from vp.opposite to vq.opposite
        Edge nextE;
        do {
            if (nodesVisited.contains(v)) {
                System.err.println(graph);
                throw new IllegalStateException("Node already visited: " + v);
            }
            nodesVisited.add(v);

            final Edge f0 = e; // f0 is edge by which we enter the node
            {
                Edge f = v.getNextAdjacentEdgeCyclic(f0);
                while (isLeafEdge(f)) {
                    leafEdges.add(f);
                    if (f == targetLeafEdge) {
                        break;
                    }
                    if (f == f0)
                        throw new RuntimeException("Node wraparound: f=" + f + " f0=" + f0);
                    f = v.getNextAdjacentEdgeCyclic(f);

                }
                if (f == targetLeafEdge)
                    nextE = null; // at end of chain
                else
                    nextE = f; // continue along boundary
            }

            final Node u = graph.newNode(); // new node on new path
            {
                final Edge f = graph.newEdge(u, null, v, f0, Edge.AFTER, Edge.AFTER, null); // edge from new node on new path to old node on existing path
                // here we make sure that new edge is inserted after f0
                graph.setSplit(f, s);
                graph.setWeight(f, splits.get(s).getWeight());
            }

            if (prevU != null) {
                final Edge f = graph.newEdge(u, prevU, null); // edge from current node to previous node on new path
                graph.setSplit(f, graph.getSplit(e));
                graph.setWeight(f, graph.getWeight(e));
            }
            for (Edge f : leafEdges) { // copy leaf edges over to new path
                final Edge fCopy = graph.newEdge(u, graph.getOpposite(v, f));
                graph.setSplit(fCopy, graph.getSplit(f));
                graph.setWeight(fCopy, graph.getWeight(f));
                graph.deleteEdge(f);
            }
            leafEdges.clear();

            if (nextE != null) {
                v = graph.getOpposite(v, nextE);
                e = nextE;
                prevU = u;
            }
        } while (nextE != null);
    }

    /**
     * does this edge lead to a leaf?
     *
     * @return is leaf edge
     */
    private static boolean isLeafEdge(Edge f) {
        return f.getSource().getDegree() == 1 || f.getTarget().getDegree() == 1;
    }

    /**
     * this removes all temporary trivial edges added to the graph
     *
	 */
    private static void removeTemporaryTrivialEdges(PhyloSplitsGraph graph) {
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
     * @param forbiddenSplits : set of all the splits such as their edges won't have their angles changed
     */
    public static void assignAnglesToEdges(int ntaxa, SplitsBlock splits, int[] cycle, PhyloSplitsGraph graph, BitSet forbiddenSplits, double totalAngle) {
        //We create the list of angles representing the positions on a circle.
        double[] angles = assignAnglesToSplits(ntaxa, splits, cycle, totalAngle);

        for (Edge e : graph.edges()) {
            if (!forbiddenSplits.get(graph.getSplit(e))) {
                graph.setAngle(e, angles[graph.getSplit(e)]);
            }
        }
    }

    /**
     * assigns angles to all edges in the graph
     *
	 */
    public static double[] assignAnglesToSplits(int ntaxa, SplitsBlock splits, int[] cycle, double totalAngle) {
        //We create the list of angles representing the positions on a circle.
        double[] angles = new double[ntaxa + 1];
        for (int t = 1; t <= ntaxa; t++) {
            angles[t] = (totalAngle * (t - 1) / (double) ntaxa) + 270 - 0.5 * totalAngle;
        }

        double[] split2angle = new double[splits.getNsplits() + 1];

        assignAnglesToSplits(ntaxa, angles, split2angle, splits, cycle);
        return split2angle;
    }


    /**
     * assigns angles to the splits in the graph, considering that they are located exactly "in the middle" of two taxa
     * so we fill split2angle using TaxaAngles.
     *
     * @param angles      for each taxa, its angle
     * @param split2angle for each split, its angle
     */
    private static void assignAnglesToSplits(int ntaxa, double[] angles, double[] split2angle, SplitsBlock splits, int[] cycle) {
        for (int s = 1; s <= splits.getNsplits(); s++) {
            int xp = 0; // first position of split part not containing taxon cycle[1]
            int xq = 0; // last position of split part not containing taxon cycle[1]
            final BitSet part = splits.get(s).getPartNotContaining(cycle[1]);
            for (int i = 2; i <= ntaxa; i++) {
                int t = cycle[i];
                if (part.get(t)) {
                    if (xp == 0)
                        xp = i;
                    xq = i;
                }
            }

            split2angle[s] = GeometryUtilsFX.modulo360(0.5 * (angles[xp] + angles[xq]));

            //System.out.println("split from "+xp+","+xpneighbour+" ("+TaxaAngleP+") to "+xq+","+xqneighbour+" ("+TaxaAngleQ+") -> "+split2angle[s]+" $ "+(180 * (xp + xq)) / (double) ntaxa);s
        }
    }


    /**
     * assigns coordinates to nodes
     *
	 */
    public static void assignCoordinatesToNodes(boolean useWeights, PhyloSplitsGraph graph, NodeArray<Point2D> node2point, int startTaxonId) {
        if (graph.getNumberOfNodes() == 0)
            return;
        final Node v = graph.getTaxon2Node(startTaxonId);
        node2point.put(v, new Point2D(0, 0));

        final BitSet splitsInPath = new BitSet();
        NodeSet nodesVisited = new NodeSet(graph);

        assignCoordinatesToNodesRec(useWeights, v, splitsInPath, nodesVisited, graph, node2point);
    }

    /**
     * recursively assigns coordinates to all nodes
     *
	 */
    private static void assignCoordinatesToNodesRec(boolean useWeights, Node v, BitSet splitsInPath, NodeSet nodesVisited, PhyloSplitsGraph graph, NodeArray<Point2D> node2point) {
        if (!nodesVisited.contains(v)) {
            nodesVisited.add(v);
            for (Edge e : v.adjacentEdges()) {
                int s = graph.getSplit(e);
                if (!splitsInPath.get(s)) {
                    Node w = graph.getOpposite(v, e);
                    Point2D p = GeometryUtilsFX.translateByAngle(node2point.get(v), graph.getAngle(e), useWeights ? graph.getWeight(e) : 1);
                    node2point.put(w, p);
                    splitsInPath.set(s, true);
                    assignCoordinatesToNodesRec(useWeights, w, splitsInPath, nodesVisited, graph, node2point);
                    splitsInPath.set(s, false);
                }
            }
        }
    }
}
