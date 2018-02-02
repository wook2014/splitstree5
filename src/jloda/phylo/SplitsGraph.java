/**
 * SplitsGraph.java
 * Copyright (C) 2018 Daniel H. Huson
 * <p>
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jloda.phylo;

import jloda.graph.*;
import jloda.util.Basic;
import jloda.util.Pair;

import java.util.*;

/**
 * Splits graph
 * Daniel Huson, 1.2018
 */
public class SplitsGraph extends PhyloGraph {
    final EdgeIntegerArray splits;
    protected final EdgeDoubleArray edgeAngles;

    /**
     * Construct a new empty phylogenetic tree.
     */
    public SplitsGraph() {
        super();
        splits = new EdgeIntegerArray(this);
        edgeAngles = new EdgeDoubleArray(this);
    }

    /**
     * Clears the tree.
     */
    public void clear() {
        super.clear();
        splits.clear();
    }

    /**
     * copies a phylogenetic tree
     *
     * @param src original tree
     * @return mapping of old nodes to new nodes
     */
    public NodeArray<Node> copy(SplitsGraph src) {
        NodeArray<Node> oldNode2NewNode = super.copy(src);

        setName(src.getName());

        return oldNode2NewNode;
    }

    /**
     * copies a phylogenetic tree
     *
     * @param src
     * @param oldNode2NewNode
     * @param oldEdge2NewEdge
     */
    public NodeArray<Node> copy(SplitsGraph src, NodeArray<Node> oldNode2NewNode, EdgeArray<Edge> oldEdge2NewEdge) {
        clear();
        if (oldNode2NewNode == null)
            oldNode2NewNode = new NodeArray<>(src);
        if (oldEdge2NewEdge == null)
            oldEdge2NewEdge = new EdgeArray<>(src);

        super.copy(src, oldNode2NewNode, oldEdge2NewEdge);
        edgeConfidencesSet = src.edgeConfidencesSet;

        for (Node v : nodes()) {
            Node w = (oldNode2NewNode.getValue(v));
            nodeLabels.setValue(w, src.nodeLabels.getValue(v));
            node2taxa.setValue(w, src.node2taxa.getValue(v));
        }
        for (Edge e : edges()) {
            Edge f = (oldEdge2NewEdge.getValue(e));
            edgeWeights.put(f, src.edgeWeights.getValue(e));
            edgeLabels.put(f, src.edgeLabels.getValue(e));
            edgeConfidences.put(f, src.edgeConfidences.getValue(e));
            edgeAngles.put(f, src.edgeAngles.getValue(e));
            splits.put(f, src.splits.getValue(e));
        }
        for (int i = 0; i < src.taxon2node.size(); i++) {
            Node v = src.getTaxon2Node(i + 1);
            if (v != null)
                addTaxon(oldNode2NewNode.getValue(v), i + 1);
        }
        return oldNode2NewNode;
    }

    /**
     * gets the set of split ids in the graph as a sorted array
     *
     * @return number of splits
     */
    public Integer[] getSplitIds() {
        final Set<Integer> ids = new TreeSet<>();
        for (Edge e : edges()) {
            ids.add(splits.getValue(e));
        }
        return ids.toArray(new Integer[ids.size()]);
    }

    /**
     * sets the split-id of an edge
     *
     * @param e  the edge
     * @param id the id
     */
    public void setSplit(Edge e, int id) {
        splits.put(e, id);
    }

    /**
     * gets the split-id of an edge
     *
     * @param e the edge
     * @return the split-id of the given edge
     */
    public int getSplit(Edge e) {
        if (splits.getValue(e) == null)
            return 0;
        return splits.getValue(e);
    }


    /**
     * clones the current tree
     *
     * @return a clone of the current tree
     */
    public Object clone() {
        SplitsGraph tree = new SplitsGraph();
        tree.copy(this);
        return tree;
    }

    /**
     * removes a split from the graph by contracting all edges associated with the split
     *
     * @param splitId
     */
    public void removeSplit(int splitId) {
        Node one = getTaxon2Node(1);

        if (one != null) {
            // determine all nodes and edges that separate S(1) from X-S(1)
            List<Pair<Node, Edge>> separators = new ArrayList<>(); // each is a pair consisting of a node and edge
            NodeSet seen = new NodeSet(this);

            getAllSeparators(splitId, one, null, seen, separators);

            // determine all nodes on opposite end of separating edges
            NodeSet opposites = new NodeSet(this);
            Iterator it = separators.iterator();
            while (it.hasNext()) {
                Pair pair = (Pair) it.next();
                opposites.add(getOpposite((Node) pair.getFirst(), (Edge) pair.getSecond()));
            }

            // reconnect edges that are adjacent to opposite ends of separators:

            it = separators.iterator();
            while (it.hasNext()) {
                Pair pair = (Pair) it.next();
                Node v = (Node) pair.getFirst();
                Edge e = (Edge) pair.getSecond();
                Node w = getOpposite(v, e);

                for (Edge f : w.adjacentEdges()) {
                    if (f != e) {
                        Node u = getOpposite(w, f);
                        if (u != v && !opposites.contains(u)) {
                            Edge g = null;
                            try {
                                g = newEdge(u, v);
                            } catch (IllegalSelfEdgeException e1) {
                                Basic.caught(e1);
                            }
                            setSplit(g, getSplit(f));
                            setWeight(g, getWeight(f));
                            setAngle(g, getAngle(f));
                        }
                    }
                }

                if (getLabel(w) != null && getLabel(w).length() > 0) {
                    if (getLabel(v) == null)
                        setLabel(v, getLabel(w));
                    else
                        setLabel(v, getLabel(v) + ", " + getLabel(w));
                }

                if (getTaxa(w) != null) // node is labeled by taxa, move labels to v
                {
                    for (Integer t : getTaxa(w)) {
                        addTaxon(v, t);
                        addTaxon(v, t);
                    }
                    clearTaxa(w);
                }
                // delete old node w.
                deleteNode(w);
            }
        }
    }

    /**
     * recursively finds all edges representing the named split.
     *
     * @param splitId
     * @param v
     * @param e
     * @param seen
     * @param separators adds the resulting pair of (node,edge) into this list
     * @throws NotOwnerException
     */
    public void getAllSeparators(int splitId, Node v, Edge e, NodeSet seen, List<Pair<Node, Edge>> separators) {
        if (!seen.contains(v)) {
            seen.add(v);
            for (Edge f : v.adjacentEdges()) {
                if (f != e) {
                    if (getSplit(f) == splitId) {
                        separators.add(new Pair<>(v, f));
                    } else
                        getAllSeparators(splitId, getOpposite(v, f), f, seen, separators);
                }
            }
        }
    }

    /**
     * finds an edge with the given split id that separates 1 from rest of graph
     *
     * @param splitId
     * @param v
     * @param e
     * @param seen
     * @return Pair consisting of node and edge
     * @throws NotOwnerException
     */
    public Pair<Node, Edge> getSeparator(int splitId, Node v, Edge e, NodeSet seen) {
        if (!seen.contains(v)) {
            seen.add(v);
            for (Edge f : v.adjacentEdges()) {
                if (f != e) {
                    if (getSplit(f) == splitId)
                        return new Pair<>(v, f);
                    else {
                        Pair<Node, Edge> pair = getSeparator(splitId, getOpposite(v, f), f, seen);
                        if (pair != null)
                            return pair;
                    }
                }
            }
        }
        return null;
    }

    /**
     * returns a labeling of all nodes by the sets of characters in state 1
     *
     * @param split2chars
     * @param firstChars
     * @return labeling of all nodes by 01 strings
     */
    public NodeArray labelNodesBySequences(Map split2chars, char[] firstChars) {
        final NodeArray labels = new NodeArray(this);
        System.err.println("base-line= " + (new String(firstChars)));
        Node v = getTaxon2Node(1);
        BitSet used = new BitSet(); // set of splits used in current path

        labelNodesBySequencesRec(v, used, split2chars, firstChars, labels);
        return labels;
    }

    /**
     * recursively do the work
     *
     * @param v
     * @param used
     * @param split2chars
     * @param firstChars
     * @param labels
     */
    private void labelNodesBySequencesRec(Node v, BitSet used, Map split2chars, char[] firstChars, NodeArray<String> labels) {
        if (labels.getValue(v) == null) {
            BitSet flips = new BitSet();
            for (int s = used.nextSetBit(1); s >= 0; s = used.nextSetBit(s + 1)) {
                if (s > 0)
                    flips.or((BitSet) split2chars.get(s));
                // s=0 happens in rooted graph
            }
            StringBuilder label = new StringBuilder();
            for (int c = 1; c < firstChars.length; c++) {
                if (flips.get(c) == (firstChars[c] == '1'))
                    label.append("0");
                else
                    label.append("1");
            }
            labels.setValue(v, label.toString());
            for (Edge e : v.adjacentEdges()) {
                int s = getSplit(e);
                if (!used.get(s)) {
                    used.set(s);
                    labelNodesBySequencesRec(v.getOpposite(e), used, split2chars, firstChars, labels);
                    used.set(s, false);
                }
            }
        }
    }

    /**
     * Sets the angle of an edge.
     *
     * @param e Edge
     * @param d angle
     */
    public void setAngle(Edge e, double d) {
        edgeAngles.put(e, d);
    }

    /**
     * Gets the angle of an edge.
     *
     * @param e Edge
     * @return angle
     */
    public double getAngle(Edge e) {
        if (edgeAngles.getValue(e) == null)
            return 0;
        else
            return edgeAngles.getValue(e);
    }
}

// EOF
