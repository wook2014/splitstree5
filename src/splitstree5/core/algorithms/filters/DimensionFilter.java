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

package splitstree5.core.algorithms.filters;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import jloda.graph.Edge;
import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.graph.NodeSet;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.Pair;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromSplits;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.Compatibility;
import splitstree5.core.misc.SplitsUtilities;

import java.util.BitSet;

/**
 * heuristic dimension filter
 * Daniel Huson, 5.2004
 */
public class DimensionFilter extends Algorithm<SplitsBlock, SplitsBlock> implements IFromSplits, IToSplits {
    private final IntegerProperty optionMaxDimension = new SimpleIntegerProperty(4);

    /**
     * heuristically remove high-dimension configurations in split graph
     *
     * @param taxaBlock
     * @param parent
     * @param child
     * @throws CanceledException
     */
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, SplitsBlock parent, SplitsBlock child) throws CanceledException {
        final int COMPUTE_DSUBGRAPH_MAXDIMENSION = 5;
        progress.setTasks("Dimension filter", "optionMaxDimension=" + optionMaxDimension.get());
        System.err.println("\nRunning Dimension-Filter for d=" + optionMaxDimension.get());
        BitSet toDelete = new BitSet(); // set of splits to be removed from split set

        try {
            // build initial incompatibility graph:
            final Graph graph = buildIncompatibilityGraph(parent);

            //System.err.println("Init: "+graph);
            int origNumberOfNodes = graph.getNumberOfNodes();
            progress.setMaximum(origNumberOfNodes);    //initialize maximum progress
            progress.setProgress(0);

            if (optionMaxDimension.get() <= COMPUTE_DSUBGRAPH_MAXDIMENSION) {
                System.err.println("(Small D: using D-subgraph)");
                computeDSubgraph(progress, graph, optionMaxDimension.get() + 1);
            } else {
                System.err.println("(Large D: using maxDegree heuristic)");
                relaxGraph(progress, graph, optionMaxDimension.get() - 1);
            }
            //System.err.println("relaxed: "+graph);

            while (graph.getNumberOfNodes() > 0) {
                Node worstNode = getWorstNode(graph);
                int s = ((Pair<Integer, Integer>) (worstNode.getInfo())).getFirst();
                toDelete.set(s);
                graph.deleteNode(worstNode);
                //System.err.println("deleted: "+graph);

                if (optionMaxDimension.get() <= COMPUTE_DSUBGRAPH_MAXDIMENSION)
                    computeDSubgraph(progress, graph, optionMaxDimension.get() + 1);
                else
                    relaxGraph(progress, graph, optionMaxDimension.get() - 1);
                //System.err.println("relaxed: "+graph);
                progress.setProgress(origNumberOfNodes - graph.getNumberOfNodes());
            }
        } catch (Exception ex) {
            Basic.caught(ex);
        }

        copySplits(parent, child, toDelete);
        if (toDelete.cardinality() == 0)
            child.copy(parent);
        else
            child.setCycle(SplitsUtilities.computeCycle(taxaBlock.getNtax(), child.getSplits()));
        System.err.println("Splits removed: " + toDelete.cardinality());
    }

    /**
     * build the incompatibility graph
     *
     * @param splits
     * @return incompatibility graph
     */
    Graph buildIncompatibilityGraph(SplitsBlock splits) {
        Node[] split2node = new Node[splits.getNsplits() + 1];
        Graph graph = new Graph();

        for (int s = 0; s < splits.getNsplits(); s++) {
            final Pair<Integer, Integer> pair = new Pair<>(s, (int) (10000 * splits.getWeight(s)));
            split2node[s] = graph.newNode(pair);
        }
        for (int s = 0; s < splits.getNsplits(); s++) {

            for (int t = s + 1; t < splits.getNsplits(); t++)
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
    private void computeDSubgraph(ProgressListener progress, Graph graph, int d) throws CanceledException {
        //System.err.print("Compute D-subgraph: ");
        NodeSet keep = new NodeSet(graph);
        NodeSet discard = new NodeSet(graph);
        NodeSet clique = new NodeSet(graph);
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
    private boolean findClique(Graph graph, Node v, Edge e, int i, int d, NodeSet clique, NodeSet discard) {
        if (i == d)
            return true;  // found clique, retreat
        else {
            while (e != null) {
                Node w = graph.getOpposite(v, e);
                e = v.getNextAdjacentEdge(e);

                if (isConnectedTo(w, clique) && !discard.contains(w)) {
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
     * @param w
     * @param U
     * @return true, if w is connected to all nodes in U
     */
    private boolean isConnectedTo(Node w, NodeSet U) {
        int count = 0;
        for (Node u : w.adjacentNodes()) {
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
    private void relaxGraph(ProgressListener progress, Graph graph, int maxDegree) throws CanceledException {
        System.err.print("Relax graph: ");

        int maxDegreeHeuristicThreshold = 6; // use heuristic for max degrees above this threshold
        NodeSet active = new NodeSet(graph);
        for (Node v = graph.getFirstNode(); v != null; v = graph.getNextNode(v)) {
            if (v.getDegree() < maxDegree
                    || (maxDegree <= maxDegreeHeuristicThreshold && hasDegreeDButNotInClique(maxDegree + 1, graph, v)))
                active.add(v);
        }

        while (!active.isEmpty()) {
            Node v = active.getFirstElement();
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
     * gets the node will the lowest compatability score
     *
     * @param graph
     * @return worst node
     */
    private Node getWorstNode(Graph graph) {
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
     * This is the weight of the splits minus the weight of all contradicting splits
     *
     * @param v
     * @return compatibility score
     */
    private int getCompatibilityScore(Node v) {
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
    private boolean hasDegreeDButNotInClique(int d, Graph graph, Node v) {
        if (v.getDegree() != d)
            return false;
        for (Edge e = v.getFirstAdjacentEdge(); e != null; e = v.getNextAdjacentEdge(e)) {
            Node a = graph.getOpposite(v, e);
            for (Edge f = v.getNextAdjacentEdge(e); f != null; f = v.getNextAdjacentEdge(f)) {
                Node b = graph.getOpposite(v, f);
                if (!graph.areAdjacent(a, b))
                    return true;
            }
        }
        return false;
    }

    /**
     * copy splits from parent to child
     *
     * @param parent
     * @param child
     * @param toDelete
     */
    private void copySplits(SplitsBlock parent, SplitsBlock child, BitSet toDelete) {
        for (int s = 0; s < parent.size(); s++) {
            if (!toDelete.get(s))
                child.getSplits().add(parent.get(s));
        }
    }

    public int getOptionMaxDimension() {
        return optionMaxDimension.get();
    }

    public IntegerProperty optionMaxDimensionProperty() {
        return optionMaxDimension;
    }

    public void setOptionMaxDimension(int optionMaxDimension) {
        this.optionMaxDimension.set(optionMaxDimension);
    }
}
