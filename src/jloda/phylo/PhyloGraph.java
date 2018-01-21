/**
 * PhyloGraph.java
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

/**
 * Phylogenetic graph
 *
 * @author Daniel Huson, 2005
 */

import jloda.graph.*;

import java.util.*;

public class PhyloGraph extends Graph {
    protected final NodeArray<String> nodeLabels;
    protected final EdgeDoubleArray edgeWeights;
    protected final EdgeArray<String> edgeLabels;
    protected final EdgeDoubleArray edgeConfidences;
    protected final Vector<Node> taxon2node;
    protected final NodeArray<List<Integer>> node2taxa;


    public boolean edgeConfidencesSet = false; // use this to decide whether to output edge confidences

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

        taxon2node = new Vector<>();
        node2taxa = new NodeArray<>(this);

        addGraphUpdateListener(new GraphUpdateAdapter() {
            public void deleteNode(Node v) {
                List<Integer> list = node2taxa.getValue(v);
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
        taxon2node.clear();
        node2taxa.clear();
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
        }
        for (int i = 0; i < src.taxon2node.size(); i++) {
            Node v = src.getTaxon2Node(i + 1);
            if (v != null)
                setTaxon2Node(i + 1, oldNode2NewNode.getValue(v));
        }
        return oldNode2NewNode;
    }

    /**
     * Gets an enumeration of all node labels.
     */
    public Set<String> getNodeLabels() {
        Set<String> set = new HashSet<>();
        for (Node v : nodes())
            if (getLabel(v) != null && getLabel(v).length() > 0)
                set.add(getLabel(v));
        return set;
    }

    /**
     * iterates over all node labels
     *
     * @return node labels
     */
    public Iterable<String> nodeLabels() {
        return () -> new Iterator<String>() {
            private Node v;

            {
                while (v != null && getLabel(v) == null) {
                    v = v.getNext();
                }
            }

            @Override
            public boolean hasNext() {
                return v != null;
            }

            @Override
            public String next() {
                final String result = getLabel(v);
                {
                    while (v != null && getLabel(v) == null) {
                        v = v.getNext();
                    }
                }
                return result;
            }
        };
    }

    /**
     * Sets the weight of an edge.
     *
     * @param e Edge
     * @param d double
     */
    public void setWeight(Edge e, double d) {
        edgeWeights.put(e, d);
    }

    /**
     * Gets the weight of an edge.
     *
     * @param e Edge
     * @return edgeWeights double
     */
    public double getWeight(Edge e) {
        if (edgeWeights.getValue(e) == null)
            return 1;
        else
            return edgeWeights.getValue(e);
    }


    /**
     * Sets the label of an edge.
     *
     * @param e   Edge
     * @param lab String
     */
    public void setLabel(Edge e, String lab) {
        edgeLabels.put(e, lab);
    }

    /**
     * Gets the label of an edge.
     *
     * @param e Edge
     * @return edgeLabels String
     */
    public String getLabel(Edge e) {
        return edgeLabels.getValue(e);
    }

    /**
     * Sets the confidence of an edge.
     *
     * @param e Edge
     * @param d double
     */
    public void setConfidence(Edge e, double d) {
        edgeConfidencesSet = true;
        edgeConfidences.put(e, d);
    }

    /**
     * Gets the confidence of an edge.
     *
     * @param e Edge
     * @return confidence
     */
    public double getConfidence(Edge e) {
        if (edgeConfidences.getValue(e) == null)
            return 1;
        else
            return edgeConfidences.getValue(e);
    }


    /**
     * Sets the taxon label of a node.
     *
     * @param v   Node
     * @param str String
     */
    public void setLabel(Node v, String str) {
        nodeLabels.setValue(v, str);
    }


    /**
     * Sets the label of a node to a list of taxon names
     *
     * @param v      node
     * @param labels list of labels
     */
    public void setLabels(Node v, List labels) {
        if (labels != null) {
            StringBuilder buf = new StringBuilder();
            boolean first = true;
            for (Object label : labels) {
                if (first)
                    first = false;
                else
                    buf.append(", ");
                buf.append(label);
            }
            setLabel(v, buf.toString());
        }
    }

    /**
     * Gets the taxon label of a node.
     *
     * @param v Node
     * @return nodeLabels String
     */
    public String getLabel(Node v) {
        return nodeLabels.getValue(v);
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
     * returns the number of taxa
     *
     * @return number of taxa
     */
    public int getNumberOfTaxa() {
        return taxon2node.size();
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
        if (node2taxa.getValue(v) == null)
            node2taxa.setValue(v, new ArrayList<Integer>()); // lazy initialization
        return node2taxa.getValue(v);
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
        node2taxa.setValue(node, null);
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
        for (Node v : nodes()) {
            List list = getNode2Taxa(v);
            int which = list.indexOf(id);
            if (which != -1) {
                list.remove(which);
                break; // should only be one mention of this taxon
            }
        }
    }


    /**
     * changes the node labels of the network using the mapping old-to-new
     *
     * @param old2new
     */
    public void changeLabels(Map<String, String> old2new) {
        for (Node v : nodes()) {
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
        for (Node v : graph.nodes()) {
            Node w = newNode();
            old2new.setValue(v, w);
            setLabel(w, graph.getLabel(v));

        }
        try {
            for (Edge e : edges()) {
                Edge f = newEdge(old2new.getValue(e.getSource()), old2new.getValue(e.getTarget()));
                setLabel(f, graph.getLabel(e));
                setWeight(f, graph.getWeight(e));
                if (graph.edgeConfidences.getValue(e) != null)
                    setConfidence(f, graph.getConfidence(e));
            }
        } catch (IllegalSelfEdgeException e1) {
            throw new RuntimeException(e1);
        }
    }

    /**
     * scales all edge weights by the given factor
     *
     * @param factor
     */
    public void scaleEdgeWeights(float factor) {
        for (Edge e : edges()) {
            setWeight(e, factor * getWeight(e));
        }
    }

    /**
     * compute the current set of all leaves
     * @return all leaves
     */
    public NodeSet computeSetOfLeaves() {
        NodeSet nodes = new NodeSet(this);
        for (Node v : nodes())
            if (v.getOutDegree() == 0)
                nodes.add(v);
        return nodes;
    }
}
