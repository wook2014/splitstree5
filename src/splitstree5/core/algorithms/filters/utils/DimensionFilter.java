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

package splitstree5.core.algorithms.filters.utils;

import jloda.graph.Edge;
import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.graph.NodeSet;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.Pair;
import jloda.util.ProgressListener;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Compatibility;

import java.util.*;

/**
 * dimension filter
 * Daniel Huson, 12/31/16.
 */
public class DimensionFilter {
    /**
     * apply the dimension filter
     *
     * @param maxDimension
     * @param splits
     * @return filtered splits
     */
    public static ArrayList<ASplit> apply(ProgressListener progress, int maxDimension, List<ASplit> splits) {
        final int COMPUTE_DSUBGRAPH_MAXDIMENSION = 5;
        progress.setSubtask("maxDimension=" + maxDimension);
        System.err.println("\nRunning Dimension-Filter for d=" + maxDimension);

        final BitSet toDelete = new BitSet(); // set of splits to be removed from split set

        try {
            // build initial incompatibility graph:
            Graph graph = buildIncompatibilityGraph(splits);

            //System.err.println("Init: "+graph);
            int origNumberOfNodes = graph.getNumberOfNodes();
            progress.setMaximum(origNumberOfNodes);    //initialize maximum progress
            progress.setProgress(0);

            if (maxDimension <= COMPUTE_DSUBGRAPH_MAXDIMENSION) {
                System.err.println("(Small D: using D-subgraph)");
                computeDSubgraph(progress, graph, maxDimension + 1);
            } else {
                System.err.println("(Large D: using maxDegree heuristic)");
                relaxGraph(progress, graph, maxDimension - 1);
            }
            //System.err.println("relaxed: "+graph);

            while (graph.getNumberOfNodes() > 0) {
                Node worstNode = getWorstNode(graph);
                int s = ((Pair<Integer, Integer>) worstNode.getInfo()).getFirst();
                toDelete.set(s);
                graph.deleteNode(worstNode);
                //System.err.println("deleted: "+graph);

                if (maxDimension <= COMPUTE_DSUBGRAPH_MAXDIMENSION)
                    computeDSubgraph(progress, graph, maxDimension + 1);
                else
                    relaxGraph(progress, graph, maxDimension - 1);
                //System.err.println("relaxed: "+graph);
                progress.setProgress(origNumberOfNodes - graph.getNumberOfNodes());
            }
        } catch (Exception ex) {
            Basic.caught(ex);
        }

        final ArrayList<ASplit> result = new ArrayList<>(splits.size() - toDelete.cardinality());
        for (int i = toDelete.nextClearBit(0); i != -1 && i < splits.size(); i = toDelete.nextClearBit(i + 1))
            result.add(splits.get(i));

        System.err.println("Splits removed: " + toDelete.cardinality());
        return result;

    }

    /**
     * build the incompatibility graph
     *
     * @param splits
     * @return incompatibility graph
     */
    private static Graph buildIncompatibilityGraph(List<ASplit> splits) {
        final Graph graph = new Graph();

        final Node[] split2node = new Node[splits.size()];
        for (int s = 0; s < splits.size(); s++) {
            final Pair<Integer, Integer> pair = new Pair<>(s, (int) (10000 * splits.get(s).getWeight()));
            split2node[s] = graph.newNode(pair);
        }
        for (int s = 0; s < splits.size(); s++) {

            for (int t = s + 1; t < splits.size(); t++)
                if (!Compatibility.areCompatible(splits.get(s), splits.get(t))) {
                    graph.newEdge(split2node[s], split2node[t]);
                }
        }
        return graph;
    }

    /**
     * computes the subgraph in which every node is contained in a d-clique
     *
     * @param graph
     * @param d     clique size
     */
    private static void computeDSubgraph(ProgressListener progress, Graph graph, int d) throws CanceledException {
        //System.err.print("Compute D-subgraph: ");
        final NodeSet keep = new NodeSet(graph);
        final NodeSet discard = new NodeSet(graph);
        final NodeSet clique = new NodeSet(graph);
        for (Node v : graph.nodes()) {
            if (!keep.contains(v)) {
                clique.clear();
                clique.add(v);
                if (findClique(graph, v, v.getFirstAdjacentEdge(), 1, d, clique, discard))
                    keep.addAll(clique);
                else
                    discard.add(v);
            }
            progress.checkForCancel();
        }

        // remove all nodes not contained in a d-clique
        for (Node v : discard) {
            graph.deleteNode(v);
        }
        //System.err.println(" "+graph.getNumberOfNodes());
    }

    /**
     * recursively determine whether v is contained in a d-clique.
     *
     * @param graph
     * @param v
     * @param e
     * @param i
     * @param d
     * @param clique
     * @param discard
     * @return true, if v contained in a d-clique
     */
    private static boolean findClique(Graph graph, Node v, Edge e, int i, int d, NodeSet clique, NodeSet discard) {
        if (i == d)
            return true;  // found clique, retreat
        else {
            while (e != null) {
                Node w = v.getOpposite(e);
                e = v.getNextAdjacentEdge(e);

                if (isConnectedTo(graph, w, clique) && !discard.contains(w)) {
                    clique.add(w);
                    if (findClique(graph, v, e, i + 1, d, clique, discard))
                        return true;
                    clique.remove(w);
                }
            }
            return false; // didn't work out, try different combination
        }
    }

    /**
     * determines whether node w is connected to all nodes in U
     *
     * @param graph
     * @param w
     * @param U
     * @return true, if w is connected to all nodes in U
     */
    private static boolean isConnectedTo(Graph graph, Node w, NodeSet U) {
        int count = 0;
        for (Edge e : w.adjacentEdges()) {
            Node u = graph.getOpposite(w, e);
            if (U.contains(u)) {
                count++;
                if (count == U.size())
                    return true;
            }
        }
        return false;
    }

    /**
     * Modify graph to become the maximal induced graph in which all nodes have degree >maxDegree
     * If maxDegree==1, then we additionally require that all remaining nodes are contained in a triangle
     *
     * @param graph
     * @param maxDegree
     */
    private static void relaxGraph(ProgressListener progress, Graph graph, int maxDegree) throws CanceledException {
        System.err.print("Relax graph: ");

        int maxDegreeHeuristicThreshold = 6; // use heuristic for max degrees above this threshold
        Set<Node> active = new HashSet<>();
        for (Node v : graph.nodes()) {
            if (v.getDegree() < maxDegree
                    || (maxDegree <= maxDegreeHeuristicThreshold && hasDegreeDButNotInClique(maxDegree + 1, graph, v)))
                active.add(v);
        }

        while (!active.isEmpty()) {
            Node v = active.iterator().next();
            if (v.getDegree() < maxDegree || (maxDegree <= maxDegreeHeuristicThreshold && hasDegreeDButNotInClique(maxDegree + 1, graph, v))) {
                for (Node w : v.adjacentNodes()) {
                    active.add(w);
                }
                active.remove(v);
                graph.deleteNode(v);
            } else
                active.remove(v);
            progress.checkForCancel();
        }
        System.err.println("" + graph.getNumberOfNodes());
    }


    /**
     * gets the node will the lowest compatibility score
     *
     * @param graph
     * @return worst node
     */
    private static Node getWorstNode(Graph graph) {
        float worstCompatibility = 0;
        Node worstNode = null;
        for (Node v : graph.nodes()) {
            float compatibility = getCompatibilityScore(v);
            if (worstNode == null || compatibility < worstCompatibility) {
                worstNode = v;
                worstCompatibility = compatibility;
            }
        }
        return worstNode;
    }

    /**
     * gets the compatibility score of a node.
     * This is the weight oif the splits minus the weight of all contradicting splits
     *
     * @param v
     * @return compatibility score
     */
    private static int getCompatibilityScore(Node v) {
        int score = ((Pair<Integer, Integer>) v.getInfo()).getSecond();

        for (Node w : v.adjacentNodes()) {
            score -= ((Pair<Integer, Integer>) w.getInfo()).getSecond();
        }
        return score;
    }

    /**
     * determines whether the node v has degree==d but  is not contained in a clique of size d+1
     *
     * @param d* @param graph
     * @param v
     * @return false, if the node v has degree!=d or is contained in a d+1 clique
     */
    private static boolean hasDegreeDButNotInClique(int d, Graph graph, Node v) {
        if (v.getDegree() != d)
            return false;
        for (Edge e = v.getFirstAdjacentEdge(); e != null; e = v.getNextAdjacentEdge(e)) {
            Node a = graph.getOpposite(v, e);
            for (Edge f = v.getNextAdjacentEdge(e); f != null; f = v.getNextAdjacentEdge(f)) {
                Node b = graph.getOpposite(v, f);
                if (!a.isAdjacent(b))
                    return true;
            }
        }
        return false;
    }
}
