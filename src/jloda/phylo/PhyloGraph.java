/**
 * PhyloGraph.java
 * Copyright (C) 2017 Daniel H. Huson
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

/**
 *
 * Phylogenetic graph
 *
 * @author Daniel Huson, 2005
 */

import jloda.graph.*;
import jloda.util.Basic;
import jloda.util.Pair;

import java.util.*;

public class PhyloGraph extends Graph {
    final NodeArray<String> nodeLabels;
    final EdgeDoubleArray edgeWeights;
    final EdgeDoubleArray edgeAngles;
    final EdgeArray<String> edgeLabels;
    final EdgeDoubleArray edgeConfidences;
    final EdgeIntegerArray splits;
    final Vector<Node> taxon2node;
    final Vector<Integer> taxon2cycle;
    final NodeArray<List<Integer>> node2taxa;

    final BitSet specialEdges = new BitSet();

    public boolean edgeConfidencesSet = false; // use this to decide whether to output edge confidences
    private String name = null;

    // if you add anything here, make sure it gets added to copy, too!

    /**
     * Construct a new empty phylogenetic graph. Also registers a listener that will update the taxon2node
     * array if nodes are deleted.
     */
    public PhyloGraph() {
        super();
        nodeLabels = new NodeArray<>(this);
        edgeWeights = new EdgeDoubleArray(this);
        edgeLabels = new EdgeArray<>(this);
        edgeConfidences = new EdgeDoubleArray(this);

        edgeAngles = new EdgeDoubleArray(this);
        splits = new EdgeIntegerArray(this);
        taxon2node = new Vector<>();
        taxon2cycle = new Vector<>();
        node2taxa = new NodeArray<>(this);

        addGraphUpdateListener(new GraphUpdateAdapter() {
            public void deleteNode(Node v) {
                List<Integer> list = node2taxa.get(v);
                if (list != null) {
                    for (Integer t : list) {
                        taxon2node.set(t - 1, null);
                    }
                }
            }
        });
    }

    /**
     * Clears the graph.  All auxiliary arrays are cleared.
     */
    public void clear() {
        deleteAllNodes();
        nodeLabels.clear();
        edgeWeights.clear();
        edgeLabels.clear();
        edgeConfidences.clear();
        splits.clear();
        taxon2node.clear();
        taxon2cycle.clear();
        node2taxa.clear();
        specialEdges.clear();
    }

    /**
     * copies one phylo graph to another
     *
     * @param src the source graph
     * @return old node to new node mapping
     */
    public NodeArray<Node> copy(PhyloGraph src) {
        clear();
        NodeArray<Node> oldNode2NewNode = new NodeArray<>(src);
        copy(src, oldNode2NewNode, new EdgeArray<Edge>(src));
        return oldNode2NewNode;
    }

    /**
     * copies one phylo graph to another
     *
     * @param src             the source graph
     * @param oldNode2NewNode
     * @param oldEdge2NewEdge
     */
    public NodeArray<Node> copy(PhyloGraph src, NodeArray<Node> oldNode2NewNode, EdgeArray<Edge> oldEdge2NewEdge) {
        clear();
        if (oldNode2NewNode == null)
            oldNode2NewNode = new NodeArray<>(src);
        if (oldEdge2NewEdge == null)
            oldEdge2NewEdge = new EdgeArray<>(src);

        super.copy(src, oldNode2NewNode, oldEdge2NewEdge);
        edgeConfidencesSet = src.edgeConfidencesSet;

        for (Node v = src.getFirstNode(); v != null; v = src.getNextNode(v)) {
            Node w = (oldNode2NewNode.get(v));
            nodeLabels.set(w, src.nodeLabels.get(v));
            node2taxa.set(w, src.node2taxa.get(v));
        }
        for (Edge e = src.getFirstEdge(); e != null; e = src.getNextEdge(e)) {
            Edge f = (oldEdge2NewEdge.get(e));
            edgeWeights.set(f, src.edgeWeights.get(e));
            edgeLabels.set(f, src.edgeLabels.get(e));
            edgeConfidences.set(f, src.edgeConfidences.get(e));
            edgeAngles.set(f, src.edgeAngles.get(e));
            splits.set(f, src.splits.get(e));
        }
        for (int i = 0; i < src.taxon2node.size(); i++) {
            Node v = src.getTaxon2Node(i + 1);
            if (v != null)
                setTaxon2Node(i + 1, oldNode2NewNode.get(v));
        }
        for (int i = 0; i < src.taxon2cycle.size(); i++) {

            int c = src.getTaxon2Cycle(i + 1);
            setTaxon2Cycle(i + 1, c);
        }
        return oldNode2NewNode;
    }

    /**
     * Gets an enumeration of all node labels.
     */
    public Set<String> getNodeLabels() {
        Set<String> set = new HashSet<>();

        for (Node v = getFirstNode(); v != null; v = getNextNode(v))
            if (getLabel(v) != null && getLabel(v).length() > 0)
                set.add(getLabel(v));

        return set;
    }

    public void setSpecial(Edge e, boolean special) {
        specialEdges.set(e.getId(), special);
    }

    public boolean isSpecial(Edge e) {
        return specialEdges.get(e.getId());
    }

    /**
     * returns the number of splits.
     * number of splits: amount of different split-id's in the graph.
     *
     * @return number of splits
     */
    public Integer[] getSplitIds() {
        int count = 0;
        ArrayList<Integer> ids = new ArrayList<>();
        for (Edge e = getFirstEdge(); e != null; e = getNextEdge(e)) {
            if (!ids.contains(splits.get(e))) {
                ids.add(splits.get(e));
                count++;
            }
        }
        return ids.toArray(new Integer[count]);
    }


    /**
     * Sets the angle of an edge.
     *
     * @param e Edge
     * @param d angle
     */
    public void setAngle(Edge e, double d) {
        edgeAngles.set(e, d);
    }

    /**
     * Gets the angle of an edge.
     *
     * @param e Edge
     * @return angle
     */
    public double getAngle(Edge e) {
        if (edgeAngles.get(e) == null)
            return 0;
        else
            return edgeAngles.get(e);
    }

    /**
     * Sets the weight of an edge.
     *
     * @param e Edge
     * @param d double
     */
    public void setWeight(Edge e, double d) {
        edgeWeights.set(e, d);
    }

    /**
     * Gets the weight of an edge.
     *
     * @param e Edge
     * @return edgeWeights double
     */
    public double getWeight(Edge e) {
        if (edgeWeights.get(e) == null)
            return 1;
        else
            return edgeWeights.get(e);
    }


    /**
     * Sets the label of an edge.
     *
     * @param e   Edge
     * @param lab String
     */
    public void setLabel(Edge e, String lab) {
        edgeLabels.set(e, lab);
    }

    /**
     * Gets the label of an edge.
     *
     * @param e Edge
     * @return edgeLabels String
     */
    public String getLabel(Edge e) {
        return edgeLabels.get(e);
    }

    /**
     * Sets the confidence of an edge.
     *
     * @param e Edge
     * @param d double
     */
    public void setConfidence(Edge e, double d) {
        edgeConfidencesSet = true;
        edgeConfidences.set(e, d);
    }

    /**
     * Gets the confidence of an edge.
     *
     * @param e Edge
     * @return confidence
     */
    public double getConfidence(Edge e) {
        if (edgeConfidences.get(e) == null)
            return 1;
        else
            return edgeConfidences.get(e);
    }


    /**
     * Sets the taxon label of a node.
     *
     * @param v   Node
     * @param str String
     */
    public void setLabel(Node v, String str) {
        nodeLabels.set(v, str);
    }


    /**
     * Sets the label of a node to a list of taxon names
     *
     * @param v      node
     * @param labels list of labels
     */
    public void setLabels(Node v, List labels) {
        if (labels != null) {
            String str = "";
            Iterator it = labels.iterator();

            while (it.hasNext()) {
                String label = (String) it.next();
                str += label;
                if (it.hasNext())
                    str += ", ";
            }
            setLabel(v, str);
        }
    }

    /**
     * Gets the taxon label of a node.
     *
     * @param v Node
     * @return nodeLabels String
     */
    public String getLabel(Node v) {
        return nodeLabels.get(v);
    }

    /**
     * sets the split-id of an edge
     *
     * @param e  the edge
     * @param id the id
     */
    public void setSplit(Edge e, int id) {
        splits.set(e, id);
    }

    /**
     * gets the split-id of an edge
     *
     * @param e the edge
     * @return the split-id of the given edge
     */
    public int getSplit(Edge e) {
        if (splits.get(e) == null)
            return 0;
        return splits.get(e);
    }

    /**
     * find the corresponding node for a given taxon-id.
     *
     * @param taxId the taxon-id
     * @return the Node representing the taxon with id <code>taxId</code>.
     */
    public Node getTaxon2Node(int taxId) {
        if (taxId <= taxon2node.size())
            return taxon2node.get(taxId - 1);
        else {
            //  System.err.println("getTaxon2Node: no Node set for taxId " + taxId + " (taxa2Nodes.size(): " + taxon2node.size() + ")");
            return null;
        }
    }

    /**
     * find the position of a taxon in the cyclic ordering.
     *
     * @param taxId the taxon-id.
     * @return the index of taxon with id <code>taxId</code> in the cyclic ordering.
     */
    public int getTaxon2Cycle(int taxId) {
        if (taxId <= taxon2cycle.size())
            return taxon2cycle.get(taxId - 1);
        else {
            System.err.println("getTaxon2Cycle: no cycle-index set for taxId " + taxId + " (taxon2cycle.size(): " + taxon2cycle.size() + ")");
            return -1;
        }
    }

    /**
     * returns the number of taxa
     *
     * @return number of taxa
     */
    public int getNumberOfTaxa() {
        return taxon2node.size();
    }

    /**
     * gets the cycle of taxa
     *
     * @return cyclic ordering of taxa
     */
    public int[] getCycle() {
        int[] cycle = new int[taxon2node.size() + 1];
        for (int t = 1; t <= taxon2node.size(); t++)
            cycle[getTaxon2Cycle(t)] = t;
        return cycle;
    }

    /**
     * set the position of a taxon in the cyclic ordering.
     *
     * @param taxId      the taxon-id.
     * @param cycleIndex the index of taxon with id <code>taxId</code> in the cyclic ordering.
     */
    public void setTaxon2Cycle(int taxId, int cycleIndex) {
        if (taxId <= taxon2cycle.size()) {
            taxon2cycle.setElementAt(cycleIndex, taxId - 1);
        } else {
            taxon2cycle.setSize(taxId);
            taxon2cycle.setElementAt(cycleIndex, taxId - 1);
        }
    }

    /**
     * set which Node represents the taxon with id <code>taxId</code>.
     *
     * @param taxId   the taxon-id.
     * @param taxNode the Node representing the taxon with id <code>taxId</code>.
     * @throws NotOwnerException
     */
    public void setTaxon2Node(int taxId, Node taxNode) {
        this.checkOwner(taxNode);
        if (taxId <= taxon2node.size()) {
            taxon2node.setElementAt(taxNode, taxId - 1);
        } else {
            taxon2node.setSize(taxId);
            taxon2node.setElementAt(taxNode, taxId - 1);
        }
    }

    /**
     * add a taxon to be represented by the specified node
     *
     * @param v     the node.
     * @param taxon the id of the taxon to be added
     */
    public void setNode2Taxa(Node v, int taxon) {
        getNode2Taxa(v).add(taxon);
    }

    /**
     * Gets a list of all taxa represented by this node
     *
     * @param v the node
     * @return list containing ids of taxa associated with that node
     */
    public List<Integer> getNode2Taxa(Node v) {
        if (node2taxa.get(v) == null)
            node2taxa.set(v, new ArrayList<Integer>()); // lazy initialization
        return node2taxa.get(v);
    }

    /**
     * Clears the taxon 2 node3 map
     */
    public void clearTaxon2Node() {
        taxon2node.clear();
    }

    /**
     * Clears the taxon 2 node3 map
     */
    public void clearNode2Taxa() {
        node2taxa.clear();
    }

    /**
     * Clears the taxa entries for the specified node
     *
     * @param node the node
     */
    public void clearNode2Taxa(Node node) {
        node2taxa.set(node, null);
    }

    /**
     * produces a clone of this graph
     *
     * @return a clone of this graph
     */
    public Object clone() {
        super.clone();
        PhyloGraph result = new PhyloGraph();
        result.copy(this);
        return result;
    }

    /**
     * removes a taxon from the graph, but leaves the corresponding node label, if any
     *
     * @param id
     */
    public void removeTaxon(int id) {
        taxon2node.set(id - 1, null);
        taxon2cycle.remove(id - 1);
        for (Node v = getFirstNode(); v != null; v = getNextNode(v)) {
            List list = getNode2Taxa(v);
            int which = list.indexOf(id);
            if (which != -1) {
                list.remove(which);
                break; // should only be one mention of this taxon
            }
        }
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

                for (Edge f = getFirstAdjacentEdge(w); f != null; f = getNextAdjacentEdge(f, w)) {
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

                if (getNode2Taxa(w) != null) // node is labeled by taxa, move labels to v
                {
                    for (Integer t : getNode2Taxa(w)) {
                        setTaxon2Node(t, v);
                        setNode2Taxa(v, t);
                    }
                    getNode2Taxa(w).clear();
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
            for (Edge f = getFirstAdjacentEdge(v); f != null; f = getNextAdjacentEdge(f, v)) {
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
            for (Edge f = getFirstAdjacentEdge(v); f != null; f = getNextAdjacentEdge(f, v)) {
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
        if (labels.get(v) == null) {
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
            labels.set(v, label.toString());
            for (Edge e = v.getFirstAdjacentEdge(); e != null; e = v.getNextAdjacentEdge(e)) {
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
     * changes the node labels of the network using the mapping old-to-new
     *
     * @param old2new
     */
    public void changeLabels(Map<String, String> old2new) {
        for (Node v = getFirstNode(); v != null; v = getNextNode(v)) {
            String label = getLabel(v);
            if (label != null && old2new.containsKey(label))
                setLabel(v, old2new.get(label));
        }
    }

    /**
     * add the nodes and edges of another graph to this graph. Doesn't make the graph connected, though!
     *
     * @param graph
     */
    public void add(PhyloGraph graph) {
        NodeArray<Node> old2new = new NodeArray<>(graph);
        for (Node v = graph.getFirstNode(); v != null; v = graph.getNextNode(v)) {
            Node w = newNode();
            old2new.set(v, w);
            setLabel(w, graph.getLabel(v));

        }
        for (Edge e = graph.getFirstEdge(); e != null; e = graph.getNextEdge(e)) {
            Edge f = null;
            try {
                f = newEdge(old2new.get(graph.getSource(e)), old2new.get(graph.getTarget(e)));
            } catch (IllegalSelfEdgeException e1) {
                Basic.caught(e1);
            }
            setLabel(f, graph.getLabel(e));
            setWeight(f, graph.getWeight(e));
            if (graph.edgeConfidences.get(e) != null)
                setConfidence(f, graph.getConfidence(e));
        }
    }

    /**
     * scales all edge weights by the given factor
     *
     * @param factor
     */
    public void scaleEdgeWeights(float factor) {
        for (Edge e = getFirstEdge(); e != null; e = getNextEdge(e)) {
            setWeight(e, factor * getWeight(e));
        }
    }

    /**
     * compute the current set of all leaves
     * @return all leaves
     */
    public NodeSet computeSetOfLeaves() {
        NodeSet nodes = new NodeSet(this);
        for (Node v = getFirstNode(); v != null; v = getNextNode(v))
            if (v.getOutDegree() == 0)
                nodes.add(v);
        return nodes;
    }

    /**
     * compute the max id of any node
     *
     * @return max id
     */
    public int computeMaxId() {
        int max = 0;
        for (Node v = getFirstNode(); v != null; v = getNextNode(v)) {
            if (max < v.getId())
                max = v.getId();
        }
        return max;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
