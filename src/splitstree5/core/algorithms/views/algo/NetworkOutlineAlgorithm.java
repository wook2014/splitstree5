/*
 * OutlineCircularNetwork.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.core.algorithms.views.algo;

import javafx.geometry.Point2D;
import jloda.fx.util.GeometryUtilsFX;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloSplitsGraph;
import jloda.util.Basic;
import jloda.util.BitSetUtils;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.utils.PhyloGraphUtils;
import splitstree5.utils.SplitsUtilities;

import java.util.*;

/**
 * runs the outline algorithm due to Bryant and Huson, 2020
 * Daniel Huson, 1.2020
 */
public class NetworkOutlineAlgorithm {
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
    public static void apply(ProgressListener progress, boolean useWeights, TaxaBlock taxaBlock, SplitsBlock splits, PhyloSplitsGraph graph, NodeArray<Point2D> node2point, BitSet forbiddenSplits, BitSet usedSplits,
                             ArrayList<ArrayList<Node>> loops) throws CanceledException {
        progress.setTasks("Outline", null);

        if (node2point == null)
            node2point = new NodeArray<>(graph);

        loops.clear();

        final Map<BitSet, Node> splits2node = new HashMap<>();

        final int origNSplits = splits.getNsplits();
        addAllTrivial(taxaBlock.getNtax(), splits); // these will be removed again

        try {
            final int[] cycle = SplitsUtilities.normalizeCycle(splits.getCycle());
            final double[] split2angle = EqualAngle.assignAnglesToSplits(taxaBlock.getNtax(), splits, cycle);

            final ArrayList<Event> events = new ArrayList<>();
            {
                for (int s = 1; s <= splits.getNsplits(); s++) {
                    final ASplit split = splits.get(s);
                    if (split.isTrivial() || SplitsUtilities.isCircular(taxaBlock, cycle, split)) {
                        events.add(new Event(Event.Type.start, s, cycle, split));
                        events.add(new Event(Event.Type.end, s, cycle, split));
                        if (s <= origNSplits)
                            usedSplits.set(s, true);
                    }
                }
            }
            events.sort(Event.comparator());

            final BitSet currentSplits = new BitSet();
            Point2D location = new Point2D(0, 0);
            final Node start = graph.newNode();
            node2point.setValue(start, new Point2D(location.getX(), location.getY()));

            splits2node.put(new BitSet(), start);

            Event previousEvent = null;

            progress.setMaximum((int) (1.1 * events.size()));

            // System.err.println("Algorithm:");
            // System.err.println("Start: " + start.getId());

            final BitSet taxaFound = new BitSet();

            Node previousNode = start;
            for (Event event : events) {
                // System.err.println(event);

                if (event.getType() == Event.Type.start) {
                    currentSplits.set(event.getS(), true);
                    location = GeometryUtilsFX.translateByAngle(location, split2angle[event.getS()], useWeights ? event.getSplit().getWeight() : 1);
                } else {
                    currentSplits.set(event.getS(), false);
                    location = GeometryUtilsFX.translateByAngle(location, split2angle[event.getS()] + 180, useWeights ? event.getSplit().getWeight() : 1);
                }

                final boolean mustCreateNode = (splits2node.get(currentSplits) == null);
                final Node v;
                if (mustCreateNode) {
                    v = graph.newNode();
                    splits2node.put(BitSetUtils.copy(currentSplits), v);
                    node2point.setValue(v, new Point2D(location.getX(), location.getY()));
                } else {
                    v = splits2node.get(currentSplits);
                    location = node2point.get(v);
                }
                // System.err.println("Node: " + v.getId());

                if (!v.isAdjacent(previousNode)) {
                    final Edge e = graph.newEdge(previousNode, v);
                    graph.setSplit(e, event.getS());
                    graph.setWeight(e, useWeights ? event.getSplit().getWeight() : 1);
                    graph.setAngle(e, split2angle[event.getS()]);

                    if (!mustCreateNode) // just closed loop
                    {
                        loops.add(createLoop(v, e));
                    }
                }

                if (previousEvent != null) {
                    if (event.getS() == previousEvent.getS()) {
                        for (int t : BitSetUtils.members(event.getSplit().getPartNotContaining(1))) {
                            graph.addTaxon(previousNode, t);
                            taxaFound.set(t);
                        }
                    }
                }

                previousNode = v;
                previousEvent = event;

                progress.incrementProgress();
            }

            for (int t = 1; t <= taxaBlock.getNtax(); t++) {
                if (!taxaFound.get(t))
                    graph.addTaxon(start, t);
            }

            {
                final ArrayList<Edge> edgesToRemove = new ArrayList<>();
                final BitSet splitsToRemove = new BitSet();
                for (Edge e : graph.edges()) {
                    final int s = graph.getSplit(e);
                    if (s > origNSplits) {
                        edgesToRemove.add(e);
                        splitsToRemove.set(s);
                    }
                }
                for (Edge e : edgesToRemove) {
                    final Node leaf = (e.getSource().getDegree() == 1 ? e.getSource() : e.getTarget());
                    final Node other = (e.getSource().getDegree() == 1 ? e.getTarget() : e.getSource());
                    //System.err.println("Removing node: " + leaf.getId() + " with taxa " + Basic.toString(graph.getTaxa(leaf), ","));
                    ArrayList<Integer> taxa = new ArrayList<>();
                    for (int t : graph.getTaxa(leaf))
                        taxa.add(t);
                    graph.deleteNode(leaf);
                    for (int t : taxa)
                        graph.addTaxon(other, t);
                }
                if (splitsToRemove.cardinality() != splits.getNsplits() - origNSplits)
                    throw new IllegalArgumentException("splits to remove " + splitsToRemove.cardinality() + " should be " + (splits.getNsplits() - origNSplits));
            }

            progress.setMaximum(100);
            progress.setProgress(90);

            PhyloGraphUtils.addLabels(taxaBlock, graph);
            progress.setProgress(100);   //set progress to 100%

            if (false) {
                for (Node v : graph.nodes()) {
                    // if (graph.getLabel(v) != null)
                    System.err.println("Node " + v.getId() + " " + graph.getLabel(v) + " point: " + node2point.get(v));
                }
                for (Edge e : graph.edges()) {
                    System.err.println("Edge " + e.getSource().getId() + " - " + e.getTarget().getId() + " split: " + graph.getSplit(e));
                }
            }
        } finally {
            splits.getSplits().subList(origNSplits, splits.getNsplits()).clear(); // this is 0-based
        }
    }

    /**
     * determines loop that is closed by reentering v
     *
     * @param v
     * @param inEdge
     * @return
     */
    private static ArrayList<Node> createLoop(Node v, Edge inEdge) {
        final ArrayList<Node> loop = new ArrayList<>();
        Node w = v;
        Edge e = inEdge;
        do {
            loop.add(w);
            w = e.getOpposite(w);
            e = w.getNextAdjacentEdgeCyclic(e);
        }
        while (w != v);
        return loop;
    }

    private static void addAllTrivial(int ntaxa, SplitsBlock splits) {
        final BitSet taxaWithTrivialSplit = new BitSet();

        for (int s = 1; s <= splits.getNsplits(); s++) {
            final ASplit split = splits.get(s);
            if (split.isTrivial())
                taxaWithTrivialSplit.set(split.getSmallerPart().nextSetBit(0));
        }
        for (int t = taxaWithTrivialSplit.nextClearBit(1); t != -1 && t <= ntaxa; t = taxaWithTrivialSplit.nextClearBit(t + 1)) {
            splits.getSplits().add(new ASplit(BitSetUtils.asBitSet(t), ntaxa, 0.0001));
        }
    }

    static class Event {
        enum Type {start, end}

        private final ASplit split;
        private int minCyclePos;
        private int maxCyclePos;
        private int size;
        private final int s;
        private final Type type;

        public Event(Type type, int s, int[] cycle, ASplit split) {
            this.type = type;
            this.s = s;
            this.split = split;

            minCyclePos = Integer.MAX_VALUE;
            maxCyclePos = Integer.MIN_VALUE;
            final BitSet farSide = split.getPartNotContaining(1);
            for (int i = 0; i < cycle.length; i++) {
                int t = cycle[i];
                if (t > 0 && farSide.get(t)) {
                    minCyclePos = Math.min(minCyclePos, i);
                    maxCyclePos = Math.max(maxCyclePos, i);
                }
            }
            size = farSide.cardinality();
        }

        public int getS() {
            return s;
        }

        public Type getType() {
            return type;
        }

        public ASplit getSplit() {
            return split;
        }

        private int getMinCyclePos() {
            return minCyclePos;
        }

        private int getMaxCyclePos() {
            return maxCyclePos;
        }

        private int size() {
            return size;
        }


        public static Comparator<Event> comparator() {
            return (a, b) -> {
                final int aPos;
                if (a.getType() == Type.start)
                    aPos = a.getMinCyclePos();
                else {
                    aPos = a.getMaxCyclePos();
                }

                final int bPos;
                if (b.getType() == Type.start)
                    bPos = b.getMinCyclePos();
                else
                    bPos = b.getMaxCyclePos();

                final int result;
                if (aPos < bPos)
                    result = -1;
                else if (aPos > bPos)
                    result = 1;
                else if (a.type == Type.start && b.type == Type.end)
                    result = -1;
                else if (a.type == Type.end && b.type == Type.start)
                    result = 1;
                else result = (a.type == Type.start ? -1 : 1) * Integer.compare(a.size, b.size);
                //System.err.println(a+" vs "+b+": "+result);
                return result;
            };
        }

        public String toString() {
            return type.name() + " S" + s + " (" + minCyclePos + "-" + maxCyclePos + "): " + Basic.toString(split.getPartNotContaining(1), ",");
        }
    }
}
