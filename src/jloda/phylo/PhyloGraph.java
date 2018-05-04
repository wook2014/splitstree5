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
 * @author Daniel Huson, 2005, 2018
 */

import jloda.graph.*;
import jloda.util.EmptyIterator;

import java.util.*;

public class PhyloGraph extends Graph {
    protected final NodeArray<String> nodeLabels;
    protected final EdgeDoubleArray edgeWeights;
    protected final EdgeArray<String> edgeLabels;
    protected final EdgeDoubleArray edgeConfidences;
    protected final Map<Integer, Node> taxon2node;
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

        taxon2node = new HashMap<>();
        node2taxa = new NodeArray<>(this);

        addGraphUpdateListener(new GraphUpdateAdapter() {
            public void deleteNode(Node v) {
                List<Integer> list = node2taxa.getValue(v);
                if (list != null) {
                    for (Integer t : list) {
                        taxon2node.put(t, null);
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
        copy(src, oldNode2NewNode, new EdgeArray<>(src));
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

        for (Node v : src.nodes()) {
            Node w = (oldNode2NewNode.getValue(v));
            nodeLabels.setValue(w, src.nodeLabels.getValue(v));
            node2taxa.setValue(w, src.node2taxa.getValue(v));
        }
        for (Edge e : src.edges()) {
            Edge f = (oldEdge2NewEdge.getValue(e));
            edgeWeights.put(f, src.edgeWeights.getValue(e));
            edgeLabels.put(f, src.edgeLabels.getValue(e));
            edgeConfidences.put(f, src.edgeConfidences.getValue(e));
        }
        for (Integer t : taxon2node.keySet()) {
            Node v = src.getTaxon2Node(t);
            if (v != null)
                addTaxon(oldNode2NewNode.getValue(v), t);
        }
        return oldNode2NewNode;
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
     * Sets the label of a node.
     *
     * @param v   Node
     * @param str String
     */
    public void setLabel(Node v, String str) {
        nodeLabels.setValue(v, str);
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
        return taxon2node.get(taxId);
    }

    /**
     * returns the number of taxa
     *
     * @return number of taxa
     */
    public int getNumberOfTaxa() {
        return taxon2node.size();
    }

    public boolean hasTaxa(Node v) {
        return getNumberOfTaxa(v) > 0;
    }

    public int getNumberOfTaxa(Node v) {
        final List list = node2taxa.get(v);
        return list == null ? 0 : list.size();
    }

    /**
     * add a taxon to be represented by the specified node
     *
     * @param v     the node.
     * @param taxId the id of the taxon to be added
     */
    public void addTaxon(Node v, int taxId) {
        taxon2node.put(taxId, v);
        List<Integer> list = node2taxa.get(v);
        if (list == null) {
            list = new ArrayList<>();
            list.add(taxId);
            node2taxa.put(v, list);
        } else if (!list.contains(taxId))
            list.add(taxId);
        else
            System.err.println("Already contained");
    }

    /**
     * Clears the taxa entries for the specified node
     *
     * @param v the node
     */
    public void clearTaxa(Node v) {
        final List<Integer> list = node2taxa.get(v);
        if (list != null) {
            for (int t : list) {
                if (taxon2node.get(t) == v)
                    taxon2node.put(t, null);
            }
            node2taxa.put(v, null);
        }
    }

    /**
     * Iterates over all taxon ids of a node
     *
     * @param v the node
     * @return taxa
     */
    public Iterable<Integer> getTaxa(Node v) {
        return () -> {
            if (node2taxa.get(v) == null)
                return new EmptyIterator<>();
            else
                return node2taxa.get(v).iterator();
        };
    }

    /**
     * Clears all taxa
     */
    public void clearTaxa() {
        node2taxa.clear();
        taxon2node.clear();
    }

    /**
     * removes a taxon from the graph, but leaves the corresponding node label, if any
     *
     * @param taxonId
     */
    public void removeTaxon(int taxonId) {
        if (taxonId > 0 && taxonId < taxon2node.size()) {
            taxon2node.put(taxonId, null);
            for (Node v : nodes()) {
                List<Integer> list = node2taxa.get(v);
                if (list != null && list.contains(taxonId)) {
                    list.remove((Integer) taxonId);
                    if (list.size() == 0)
                        node2taxa.put(v, null);
                    return;
                }
            }
        }
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
     * changes the node labels of the graph using the mapping old-to-new
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
}
