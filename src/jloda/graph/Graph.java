/**
 * Graph.java
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
 *
 * @version $Id: Graph.java,v 1.51 2008-10-10 08:42:37 huson Exp $
 * @author Daniel Huson
 */
/**
 * @version $Id: Graph.java,v 1.51 2008-10-10 08:42:37 huson Exp $
 *
 * @author Daniel Huson
 *
 */
package jloda.graph;

import jloda.util.Basic;
import jloda.util.IteratorAdapter;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * A graph
 * <p/>
 * The nodes and edges are stored in several doubly-linked lists.
 * The set of nodes in the graph is stored in a list
 * The set of edges in the graph is stored in a list
 * Around each node, the set of incident edges is stored in a list.
 * Daniel Huson, 2002
 * <p/>
 */
public class Graph<V, E> extends GraphBase {
    private Node firstNode;
    private Node lastNode;
    private int numberNodes;
    private int idsNodes; // number of ids assigned to nodes

    private Edge firstEdge;
    protected Edge lastEdge;
    private int numberEdges;
    private int idsEdges; // number of ids assigned to edges

    private boolean ignoreGraphHasChanged = false; // set this when we are deleting a whole graph

    private final List<GraphUpdateListener> graphUpdateListeners = new LinkedList<>();  //List of listeners that are fired when the graph changes.

    private final List<WeakReference<NodeSet>> nodeSets = new LinkedList<>();
    // created node arrays are kept here. When an node is deleted, it's
    // entry in all node arrays is set to null
    private final List<WeakReference<NodeAssociation>> nodeAssociations = new LinkedList<>();

    // created edge arrays are kept here. When an edge is deleted, it's
    // entry in all edge arrays is set to null
    private final List<WeakReference<EdgeAssociation>> edgeAssociations = new LinkedList<>();
    // keep track of edge sets
    private final List<WeakReference<EdgeSet>> edgeSets = new LinkedList<>();

    /**
     * Constructs a new empty graph.
     */
    public Graph() {
        setOwner(this);
    }

    /**
     * Constructs a new node of the type used in the graph. This does not add the node to the graph
     * structure
     *
     * @return Node a new node
     */
    public Node newNode() {
        return newNode(null);
    }

    /**
     * Constructs a new node and set its info to obj.  This does not add the node to the graph
     * structure
     *
     * @param obj the info object
     * @return Node a new node
     */
    public Node newNode(Object obj) {
        return new Node(this, obj);
    }

    /**
     * Adds a node to the graph. The information in the node is replaced with obj. The node
     * is added to the end of the list of nodes.
     *
     * @param info the info object
     * @param v    the new node
     */
    void registerNewNode(Object info, Node v) {
        v.init(this, lastNode, null, ++idsNodes, info);
        if (firstNode == null)
            firstNode = v;
        if (lastNode != null)
            lastNode.next = v;
        lastNode = v;
        numberNodes++;
    }

    /**
     * Constructs a new edge between nodes v and w. This edge is not added to the graph.
     *
     * @param v source node
     * @param w target node
     * @return a new edge between nodes v and w
     */
    public Edge newEdge(Node v, Node w) throws IllegalSelfEdgeException {
        return new Edge(this, v, w);
    }

    /**
     * Constructs a new edge between nodes v and w and sets its info to obj. This edge is not added to the graph.
     *
     * @param v   source node
     * @param w   target node
     * @param obj the info object
     * @return a new edge between nodes v and w and sets its info to obj
     */
    public Edge newEdge(Node v, Node w, Object obj) throws IllegalSelfEdgeException {
        return new Edge(this, v, w, obj);
    }

    /**
     * Constructs a new edge between nodes v and w. The edge is inserted into the list of edges incident with
     * v and the list of edges incident with w. The place it is inserted into these list for edges
     * incident with v is determined by e_v and dir_v: if dir_v = Edge.AFTER then it is inserted after
     * e_v in the list, otherwise it is inserted before e_v. Likewise for the list of edges incident with w.
     * <p/>
     * The info is set using the obj.
     *
     * @param v     source node
     * @param e_v   reference edge incident to v
     * @param w     target node
     * @param e_w   reference edge incident to w
     * @param dir_v before or after reference e_v
     * @param dir_w before or after reference e_w
     * @param obj   the info object
     * @return a new edge
     */
    public Edge newEdge(Node v, Edge e_v, Node w, Edge e_w,
                        int dir_v, int dir_w, Object obj) throws IllegalSelfEdgeException {
        return new Edge(this, v, e_v, w, e_w, dir_v, dir_w, obj);
    }

    /**
     * Adds a  edge to the graph. The edge is inserted into the list of edges incident with
     * v and the list of edges incident with w. The place it is inserted into these list for edges
     * incident with v is determined by e_v and dir_v: if dir_v = Edge.AFTER then it is inserted after
     * e_v in the list, otherwise it is inserted before e_v. Likewise for the list of edges incident with w.
     * <p/>
     * The info is set using the obj.
     *
     * @param v     source
     * @param e_v   reference source edge
     * @param w     target
     * @param e_w   reference target edge
     * @param dir_v insert before/after source reference edge
     * @param dir_w insert before/after target reference edge
     * @param obj   info object
     * @param e     the new edge
     * @
     */
    void registerNewEdge(Node v, Edge e_v, Node w, Edge e_w, int dir_v, int dir_w, Object obj, Edge e) {
        checkOwner(v);
        checkOwner(w);
        v.incrementOutDegree();
        w.incrementInDegree();

        e.init(this, ++idsEdges, v, e_v, dir_v, w, e_w, dir_w, obj);
        if (firstEdge == null)
            firstEdge = e;
        if (lastEdge != null)
            lastEdge.next = e;
        lastEdge = e;
        numberEdges++;
    }

    /**
     * Removes edge e from the graph.
     *
     * @param e the edge
     */
    public void deleteEdge(Edge e) {
        checkOwner(e);
        // note: firstEdge and lastEdge are set in unregisterEdge
        e.deleteEdge();
    }

    /**
     * called from edge when being deleted
     *
     * @param e
     */
    void unregisterEdge(Edge e) {
        checkOwner(e);
        deleteEdgeFromArrays(e);
        deleteEdgeFromSets(e);

        getSource(e).decrementOutDegree();
        getTarget(e).decrementInDegree();
        if (firstEdge == e)
            firstEdge = (Edge) e.next;
        if (lastEdge == e)
            lastEdge = (Edge) e.prev;
        numberEdges--;
        if (numberEdges == 0)
            idsEdges = 0;
    }

    /**
     * Removes node v from the graph.
     *
     * @param v the node
     */
    public void deleteNode(Node v) {
        // note: firstNode and lastNode are set in unregisterNode
        v.deleteNode();
    }

    /**
     * called from node when being deleted
     *
     * @param v
     */
    void unregisterNode(Node v) {
        checkOwner(v);
        deleteNodeFromArrays(v);
        deleteNodeFromSets(v);
        if (firstNode == v)
            firstNode = (Node) v.next;
        if (lastNode == v)
            lastNode = (Node) v.prev;
        numberNodes--;
        if (numberNodes == 0)
            idsNodes = 0;
    }

    /**
     * Deletes all edges.
     */
    public void deleteAllEdges() {
        while (firstEdge != null)
            deleteEdge(firstEdge);
    }

    /**
     * Deletes all nodes.
     */
    public void deleteAllNodes() {
        ignoreGraphHasChanged = true;
        while (firstNode != null) {
            deleteNode(firstNode);
        }
        ignoreGraphHasChanged = false;
    }

    /**
     * Clears the graph.
     */
    public void clear() {
        deleteAllNodes();
    }

    /**
     * Change the order of edges adjacent to a node.
     *
     * @param v        the node in question.
     * @param newOrder the desired sequence of edges.
     */
    public void rearrangeAdjacentEdges(Node v, List<Edge> newOrder) {
        checkOwner(v);
        v.rearrangeAdjacentEdges(newOrder);
    }

    /**
     * move the node to the front of the list of nodes
     *
     * @param v
     */
    public void moveToFront(Node v) {
        if (v != null && v != firstNode) {
            checkOwner(v);
            if (v.prev != null)
                v.prev.next = v.next;
            if (v.next != null)
                v.next.prev = v.prev;
            v.prev = null;
            Node w = firstNode;
            firstNode = v;
            v.next = w;
            if (w != null)
                w.prev = v;
            fireGraphHasChanged();
        }
    }

    /**
     * move the node to the bacl of the list of nodes
     *
     * @param v
     */
    public void moveToBack(Node v) {
        if (v != null && v != lastNode) {
            checkOwner(v);
            if (v.prev != null)
                v.prev.next = v.next;
            if (v.next != null)
                v.next.prev = v.prev;
            v.prev = null;
            Node w = lastNode;
            lastNode = v;
            v.prev = w;
            if (w != null)
                w.next = v;
            fireGraphHasChanged();
        }
    }

    /**
     * move the edge to the front of the list of edges
     *
     * @param e
     */
    public void moveToFront(Edge e) {
        if (e != null && e != firstEdge) {
            checkOwner(e);
            if (e.prev != null)
                e.prev.next = e.next;
            if (e.next != null)
                e.next.prev = e.prev;
            e.prev = null;
            Edge f = firstEdge;
            firstEdge = e;
            e.next = f;
            if (f != null)
                f.prev = e;
            fireGraphHasChanged();
        }
    }

    /**
     * move the edge to the back of the list of edges
     *
     * @param e
     */
    public void moveToBack(Edge e) {
        if (e != null && e != lastEdge) {
            checkOwner(e);
            if (e.prev != null)
                e.prev.next = e.next;
            if (e.next != null)
                e.next.prev = e.prev;
            e.prev = null;
            Edge f = lastEdge;
            lastEdge = f;
            e.prev = f;
            if (f != null)
                f.next = e;
            fireGraphHasChanged();
        }
    }

    /**
     * Returns the node opposite node v via edge e.
     *
     * @param v the node
     * @param e the edge
     * @return the opposite node
     */
    public Node getOpposite(Node v, Edge e) {
        checkOwner(e);
        return e.getOpposite(v);
    }

    /**
     * Get the first adjacent edge to v.
     *
     * @param v the node
     * @return the first adjacent edge
     */
    public Edge getFirstAdjacentEdge(Node v) {
        checkOwner(v);
        return v.getFirstAdjacentEdge();
    }

    /**
     * Get the last adjacent edge to v.
     *
     * @param v the node
     * @return the last adjacent edge
     */
    public Edge getLastAdjacentEdge(Node v) {
        checkOwner(v);
        return v.getLastAdjacentEdge();
    }


    /**
     * Get the successor of e adjacent to v
     *
     * @param e the edge
     * @param v the node
     * @return the successor of edge adjacent to v
     */
    public Edge getNextAdjacentEdge(Edge e, Node v) {
        checkOwner(v);
        return v.getNextAdjacentEdge(e);
    }

    /**
     * Get the predecessor of e adjacent to v
     *
     * @param e the edge
     * @param v the node
     * @return the predecessor of edge adjacent to v
     */
    public Edge getPrevAdjacentEdge(Edge e, Node v) {
        checkOwner(v);
        return v.getPrevAdjacentEdge(e);
    }

    /**
     * Get the cyclic successor of e adjacent to v.
     *
     * @param e the edge
     * @param v the node
     * @return the cyclic successor of edge adjacent to v
     */
    public Edge getNextAdjacentEdgeCyclic(Edge e, Node v) {
        checkOwner(v);
        return v.getNextAdjacentEdgeCyclic(e);
    }

    /**
     * Get the cyclic predecessor of e adjacent to v.
     *
     * @param e the edge
     * @param v the node
     * @return the cyclic predecessor of edge adjacent to v
     */
    public Edge getPrevAdjacentEdgeCyclic(Edge e, Node v) {
        checkOwner(v);
        return v.getPrevAdjacentEdgeCyclic(e);
    }

    /**
     * Get the first edge in the graph.
     *
     * @return the first edge
     */
    public Edge getFirstEdge() {
        if (firstEdge != null && firstEdge.isHidden()) {
            return firstEdge.getNext();
        }
        return firstEdge;
    }

    /**
     * Get the last edge in the graph.
     *
     * @return the last edge
     */
    public Edge getLastEdge() {
        if (lastEdge != null && lastEdge.isHidden())
            return lastEdge.getPrev();
        return lastEdge;
    }

    /**
     * Get the successor of edge e.
     *
     * @param e edge
     * @return the successor edge
     */
    public Edge getNextEdge(Edge e) {
        checkOwner(e);
        return e.getNext();
    }

    /**
     * Get the predecessor of edge e.
     *
     * @param e edge
     * @return the predecessor edge
     */
    public Edge getPrevEdge(Edge e) {
        checkOwner(e);
        return e.getPrev();
    }

    /**
     * Get an edge between the two nodes v and w, if it exists
     *
     * @param v source node
     * @param w target node
     * @return an edge between v and w
     */
    public Edge getCommonEdge(Node v, Node w) {
        checkOwner(v);
        return v.getCommonEdge(w);
    }

    /**
     * Get the first node in the graph.
     *
     * @return the first node
     */
    public Node getFirstNode() {
        if (firstNode != null && firstNode.isHidden())
            return firstNode.getNext();
        return firstNode;
    }

    /**
     * Get the last node in the graph.
     *
     * @return the last node
     */
    public Node getLastNode() {
        if (lastNode != null && lastNode.isHidden())
            return lastNode.getPrev();
        return lastNode;
    }

    /**
     * Get the successor node of v
     *
     * @param v the node
     * @return the successor node
     */
    public Node getNextNode(Node v) {
        checkOwner(v);
        return v.getNext();
    }

    /**
     * Get the predecessor of v.
     *
     * @param v the node
     * @return the predecessor node
     */
    public Node getPrevNode(Node v) {
        checkOwner(v);
        return v.getPrev();
    }

    /**
     * Get the number of nodes.
     *
     * @return the number of nodes
     */
    public int getNumberOfNodes() {
        return numberNodes;
    }

    /**
     * Get number of edges.
     *
     * @return the number of edges
     */
    public int getNumberOfEdges() {
        return numberEdges;
    }

    /**
     * Get the source node of e.
     *
     * @param e the edge
     * @return the source of e
     */
    public Node getSource(Edge e) {
        checkOwner(e);
        return e.getSource();
    }

    /**
     * Get the target node of e.
     *
     * @param e the edge
     * @return the target of e
     */
    public Node getTarget(Edge e) {
        checkOwner(e);
        return e.getTarget();
    }

    /**
     * Get the degree of node v.
     *
     * @return the degree
     */
    public int getDegree(Node v) {
        checkOwner(v);
        return v.getDegree();
    }

    /**
     * Get the in-degree of node v.
     *
     * @return the in-degree
     */
    public int getInDegree(Node v) {
        checkOwner(v);
        return v.getInDegree();
    }

    /**
     * Get the out-degree of node v.
     *
     * @return the out-degree
     */
    public int getOutDegree(Node v) {
        checkOwner(v);
        return v.getOutDegree();
    }

    /**
     * Get an iterator over all edges
     *
     * @return edge iterator
     */
    public Iterator<Edge> edgeIterator() {
        return new IteratorAdapter<Edge>() {
            private Edge e = getFirstEdge();

            protected Edge findNext() throws NoSuchElementException {
                if (e != null) {
                    final Edge result = e;
                    e = getNextEdge(e);
                    return result;
                } else {
                    throw new NoSuchElementException("at end");
                }
            }
        };
    }

    /**
     * Get an iterator over all edges, including hidden ones
     *
     * @return edge iterator
     */
    public Iterator<Edge> edgeIteratorIncludingHidden() {
        return new IteratorAdapter<Edge>() {
            private Edge e = firstEdge;

            protected Edge findNext() throws NoSuchElementException {
                if (e != null) {
                    final Edge result = e;
                    checkOwner(e);
                    e = (Edge) result.next;
                    return result;
                } else {
                    throw new NoSuchElementException("at end");
                }
            }
        };
    }

    /**
     * Get an iterator over all nodes
     *
     * @return node iterator
     */
    public Iterator<Node> nodeIterator() {
        return new IteratorAdapter<Node>() {
            private Node v = getFirstNode();

            protected Node findNext() throws NoSuchElementException {
                if (v != null) {
                    final Node result = v;
                    v = getNextNode(v);
                    return result;
                } else {
                    throw new NoSuchElementException("at end");
                }
            }

            public boolean hasNext() {
                return v != null;
            }
        };
    }

    /**
     * Get an iterator over all nodes
     *
     * @return node iterator
     */
    public Iterator<Node> nodeIteratorIncludingHidden() {
        return new IteratorAdapter<Node>() {
            private Node v = firstNode;

            protected Node findNext() throws NoSuchElementException {
                if (v != null) {
                    final Node result = v;
                    v = (Node) v.next;
                    return result;
                } else {
                    throw new NoSuchElementException("at end");
                }
            }

            public boolean hasNext() {
                return v != null;
            }
        };
    }

    /**
     * Get an iterator over all nodes adjacent to v.
     *
     * @param v node
     * @return all nodes adjacent to v
     */
    public Iterator<Node> getAdjacentNodes(Node v) {
        checkOwner(v);
        return v.getAdjacentNodes();
    }

    /**
     * Get an iterator over all edges adjacent to v.
     *
     * @param v node
     * @return all edges adjacent to v
     */
    public Iterator<Edge> getAdjacentEdges(Node v) {
        checkOwner(v);
        return v.getAdjacentEdges();
    }

    /**
     * Get an iterator over all all in-edges adjacent to v.
     *
     * @param v node
     * @return all in-edges adjacent to v
     */
    public Iterator<Edge> getInEdges(Node v) {
        checkOwner(v);
        return v.getInEdges();
    }

    /**
     * Get an iterator over all all out-edges adjacent to v.
     *
     * @param v node
     * @return all out-edges adjacent to v
     */
    public Iterator<Edge> getOutEdges(Node v) {
        checkOwner(v);
        return v.getOutEdges();
    }

    /**
     * Get the id of node v.
     *
     * @param v node
     * @return the id
     */
    public int getId(Node v) {
        checkOwner(v);
        return v.getId();
    }

    /**
     * Get the id of edge e.
     *
     * @param e edge
     * @return the id
     */
    public int getId(Edge e) {
        checkOwner(e);
        return e.getId();
    }

    /**
     * Get a string representation of the graph.
     *
     * @return the string
     */
    public String toString() {
        StringBuilder buf = new StringBuilder("Graph:\n");
        buf.append("Nodes: ").append(String.valueOf(getNumberOfNodes())).append("\n");

        for (Node v = getFirstNode(); v != null; v = getNextNode(v))
            buf.append(v.toString()).append("\n");
        buf.append("Edges: ").append(String.valueOf(getNumberOfEdges())).append("\n");
        for (Edge e = getFirstEdge(); e != null; e = getNextEdge(e))
            buf.append(e.toString()).append("\n");

        return buf.toString();
    }

    /**
     * Get the info associated with node v.
     *
     * @param v node
     * @return the info
     */
    @SuppressWarnings("unchecked")
    public V getInfo(Node v) {
        checkOwner(v);
        return (V) v.getInfo();
    }

    /**
     * set the info associated with node v.
     *
     * @param v   node
     * @param obj the info object
     */
    public void setInfo(Node v, V obj) {
        checkOwner(v);
        v.setInfo(obj);
    }

    /**
     * Get the info associated with edge e.
     *
     * @param e edge
     * @return the info
     */
    @SuppressWarnings("unchecked")
    public E getInfo(Edge e) {
        checkOwner(e);
        return (E) e.getInfo();
    }

    /**
     * set the info associated with edge e.
     *
     * @param e   edge
     * @param obj the info object
     */
    public void setInfo(Edge e, E obj) {
        checkOwner(e);
        e.setInfo(obj);
    }

    /**
     * Get an edge directed from one given node to another, if it exists.
     *
     * @param v source node
     * @param w target node
     * @return edge from v tp w, if it exists, else null
     */
    public Edge findDirectedEdge(Node v, Node w) {
        checkOwner(v);
        return v.findDirectedEdge(w);
    }

    /**
     * Adds a GraphUpdateListener
     *
     * @param graphUpdateListener the listener to be added
     */
    public void addGraphUpdateListener(GraphUpdateListener graphUpdateListener) {
        graphUpdateListeners.add(graphUpdateListener);
    }

    /**
     * Removes a GraphUpdateListener
     *
     * @param graphUpdateListener the listener to be removed
     */
    public void removeGraphUpdateListener
    (GraphUpdateListener graphUpdateListener) {
        graphUpdateListeners.remove(graphUpdateListener);
    }

    /* Fires the newNode event for all GraphUpdateListeners
    *@param v the node
    */

    protected void fireNewNode(Node v) {
        checkOwner(v);

        for (GraphUpdateListener gul : graphUpdateListeners) {
            gul.newNode(v);
        }
    }

    /* Fires the deleteNode event for all GraphUpdateListeners
    *@param v the node
    */

    protected void fireDeleteNode(Node v) {
        checkOwner(v);

        for (GraphUpdateListener gul : graphUpdateListeners) {
            gul.deleteNode(v);
        }
    }

    /* Fires the newEdge event for all GraphUpdateListeners
    *@param e the edge
    */

    protected void fireNewEdge(Edge e) {
        checkOwner(e);

        for (GraphUpdateListener gul : graphUpdateListeners) {
            gul.newEdge(e);
        }
    }

    /* Fires the deleteEdge event for all GraphUpdateListeners
    *@param e the edge
    */

    protected void fireDeleteEdge(Edge e) {
        checkOwner(e);

        for (GraphUpdateListener gul : graphUpdateListeners) {
            gul.deleteEdge(e);
        }
    }

    /* Fires the graphHasChanged event for all GraphUpdateListeners
    */

    protected void fireGraphHasChanged() {
        if (!ignoreGraphHasChanged) {

            for (GraphUpdateListener gul : graphUpdateListeners) {
                gul.graphHasChanged();
            }
        }
    }

    /*
     * Fires the graphWasRead event for all GraphUpdateListeners
    */

    protected void fireGraphRead(NodeSet nodes, EdgeSet edges) {
        checkOwner(nodes);
        checkOwner(edges);
        GraphUpdateListener[] a = graphUpdateListeners.toArray(new GraphUpdateListener[graphUpdateListeners.size()]);
        for (GraphUpdateListener gul : a) {
            gul.graphWasRead(nodes, edges);
        }
    }

    /**
     * copies one graph onto another
     *
     * @param src the source graph
     */
    public void copy(Graph src) {
        copy(src, null, null);
    }

    /**
     * Copies one graph onto another. Maintains the ids of nodes and edges
     *
     * @param src             the source graph
     * @param oldNode2newNode if not null, returns map: old node id onto new node id
     * @param oldEdge2newEdge if not null, returns map: old edge id onto new edge id
     */
    public void copy(Graph src, NodeAssociation<Node> oldNode2newNode, EdgeAssociation<Edge> oldEdge2newEdge) {
        clear();

        if (oldNode2newNode == null)
            oldNode2newNode = new NodeArray<>(src);
        if (oldEdge2newEdge == null)
            oldEdge2newEdge = new EdgeArray<>(src);

        for (Node v = src.getFirstNode(); v != null; v = src.getNextNode(v)) {
            Node w = newNode();
            w.setId(v.getId());
            setInfo(w, (V) src.getInfo(v));
            oldNode2newNode.set(v, w);
        }
        idsNodes = src.idsNodes;

        for (Edge e = src.getFirstEdge(); e != null; e = src.getNextEdge(e)) {
            Node p = oldNode2newNode.get(src.getSource(e));
            Node q = oldNode2newNode.get(src.getTarget(e));
            Edge f = null;
            try {
                f = newEdge(p, q);
                f.setId(e.getId());
            } catch (IllegalSelfEdgeException e1) {
                Basic.caught(e1);
            }
            setInfo(f, (E) src.getInfo(e));
            oldEdge2newEdge.set(e, f);
        }
        idsEdges = src.idsEdges;

        // change all adjacencies to reflect order in old graph:
        for (Node v = src.getFirstNode(); v != null; v = src.getNextNode(v)) {
            Node w = oldNode2newNode.get(v);
            List<Edge> newOrder = new LinkedList<>();
            for (Edge e = v.getFirstAdjacentEdge(); e != null; e = v.getNextAdjacentEdge(e)) {
                newOrder.add(oldEdge2newEdge.get(e));
            }
            w.rearrangeAdjacentEdges(newOrder);
        }
    }

    /**
     * produces a clone of this graph
     *
     * @return a clone of this graph
     */
    public Object clone() {
        Graph result = new Graph();
        result.copy(this);
        return result;
    }

    /**
     * determines whether two nodes are adjacent
     *
     * @param a
     * @param b
     * @return true, if adjacent
     */
    public boolean areAdjacent(Node a, Node b) {
        checkOwner(a);
        return a.isAdjacent(b);
    }

    /**
     * called from constructor of NodeAssociation to register with graph
     *
     * @param array
     */
    void registerNodeAssociation(NodeAssociation array) {
        nodeAssociations.add(new WeakReference<>(array));
    }

    /**
     * called from deleteNode to clean all array entries for the node
     *
     * @param v
     */
    void deleteNodeFromArrays(Node v) {
        checkOwner(v);
        List<WeakReference> toDelete = new LinkedList<>();
        for (WeakReference<NodeAssociation> ref : nodeAssociations) {
            NodeAssociation<?> as = ref.get();
            if (as == null)
                toDelete.add(ref); // reference is dead
            else {
                as.set(v, null);
            }
        }
        for (WeakReference ref : toDelete) {
            nodeAssociations.remove(ref);
        }
    }

    /**
     * called from constructor of NodeSet to register with graph
     *
     * @param set
     */
    void registerNodeSet(NodeSet set) {
        nodeSets.add(new WeakReference<>(set));
    }

    /**
     * called from deleteNode to clean all array entries for the node
     *
     * @param v
     */
    void deleteNodeFromSets(Node v) {
        checkOwner(v);
        List<WeakReference> toDelete = new LinkedList<>();
        for (WeakReference<NodeSet> ref : nodeSets) {
            NodeSet set = ref.get();
            if (set == null)
                toDelete.add(ref); // reference is dead
            else {
                set.remove(v);
            }
        }
        for (WeakReference ref : toDelete) {
            nodeSets.remove(ref);
        }
    }

    /**
     * called from constructor of EdgeAssociation to register with graph
     *
     * @param array
     */
    void registerEdgeAssociation(EdgeAssociation array) {
        edgeAssociations.add(new WeakReference<>(array));
    }

    /**
     * called from deleteEdge to clean all array entries for the edge
     *
     * @param edge
     */
    void deleteEdgeFromArrays(Edge edge) {
        checkOwner(edge);
        List<WeakReference> toDelete = new LinkedList<>();
        for (WeakReference<EdgeAssociation> ref : edgeAssociations) {
            EdgeAssociation<?> as = ref.get();
            if (as == null)
                toDelete.add(ref); // reference is dead
            else {
                as.set(edge, null);
            }
        }
        for (WeakReference ref : toDelete) {
            edgeAssociations.remove(ref);
        }
    }

    /**
     * called from constructor of EdgeSet to register with graph
     *
     * @param set
     */
    void registerEdgeSet(EdgeSet set) {
        edgeSets.add(new WeakReference<>(set));
    }

    /**
     * called from deleteEdge to clean all array entries for the edge
     *
     * @param v
     */
    void deleteEdgeFromSets(Edge v) {
        checkOwner(v);
        List<WeakReference> toDelete = new LinkedList<>();
        for (WeakReference<EdgeSet> ref : edgeSets) {
            EdgeSet set = ref.get();
            if (set == null)
                toDelete.add(ref); // reference is dead
            else {
                set.remove(v);
            }
        }
        for (WeakReference ref : toDelete) {
            edgeSets.remove(ref);
        }
    }

    /**
     * gets the number of connected components of the graph
     *
     * @return connected components
     */
    public int getNumberConnectedComponents() {
        int result = 0;
        NodeSet used = new NodeSet(this);

        for (Node v = getFirstNode(); v != null; v = v.getNext()) {
            if (!used.contains(v)) {
                visitConnectedComponent(v, used);
                result++;
            }
        }
        return result;
    }

    /**
     * visit all nodes in a connected component
     *
     * @param v
     * @param used
     */
    public void visitConnectedComponent(Node v, NodeSet used) {
        used.add(v);
        for (Edge f = getFirstAdjacentEdge(v); f != null; f = v.getNextAdjacentEdge(f)) {
            Node w = f.getOpposite(v);
            if (!used.contains(w))
                visitConnectedComponent(w, used);
        }
    }

    /**
     * gets the current maximal node id
     *
     * @return max node id
     */
    int getMaxNodeId() {
        return idsNodes;
    }

    /**
     * gets the current maximal edge id
     *
     * @return max edge id
     */
    int getMaxEdgeId() {
        return idsEdges;
    }


    /**
     * erase all data components
     */
    public void clearData() {
        for (Node v = getFirstNode(); v != null; v = v.getNext()) {
            v.setData(null);
        }
    }

    /*public NodeSet computeSetOfLeaves(Node v) {
        NodeSet sons = new NodeSet(this);
        getLeavesRec(v,sons);
        return sons;
   }
    public void getLeavesRec(Node v, NodeSet nodes) {
        for (Edge f = v.getFirstOutEdge(); f != null; f = v.getNextOutEdge(f)) {
           Node w = f.getTarget();
           getLeavesRec(w,sons);
       }
                  if (v.getOutDegree() == 0)
           nodes.add(w);
        return sons;
   }*/

    /**
     * gets all nodes
     *
     * @return node set of nodes
     */
    public NodeSet getNodes() {
        NodeSet nodes = new NodeSet(this);
        for (Node v = getFirstNode(); v != null; v = getNextNode(v))
            nodes.add(v);
        return nodes;
    }

    /**
     * gets all edges
     *
     * @return edge set of edges
     */
    public EdgeSet getEdges() {
        EdgeSet edges = new EdgeSet(this);
        for (Edge v = getFirstEdge(); v != null; v = getNextEdge(v))
            edges.add(v);
        return edges;
    }


    /**
     * get the unhidden subset
     *
     * @param nodes
     * @return
     */
    public NodeSet getUnhiddenSubset(Collection<Node> nodes) {
        NodeSet unhidden = new NodeSet(this);
        for (Node v : nodes) {
            if (!v.isHidden())
                unhidden.add(v);
        }
        return unhidden;
    }

    /**
     * reorders nodes in graph. These nodes are put at the front of the list of nodes
     *
     * @param nodes
     */
    public void reorderNodes(List<Node> nodes) {
        final List<Node> newOrder = new ArrayList<>(numberNodes);
        final Set<Node> toMove = new HashSet<>();
        for (Node v : nodes) {
            if (v.getOwner() != null) {
                newOrder.add(v);
                toMove.add(v);
            }
        }

        if (toMove.size() > 0) {
            for (Iterator<Node> it = nodeIteratorIncludingHidden(); it.hasNext(); ) {
                Node v = it.next();
                if (!toMove.contains(v))
                    newOrder.add(v);
            }

            Node previousNode = null;
            for (Node v : newOrder) {
                if (previousNode == null) {
                    firstNode = v;
                    v.prev = null;
                } else {
                    previousNode.next = v;
                    v.prev = previousNode;
                }
                previousNode = v;
            }
            if (previousNode != null) {
                previousNode.next = null;
            }
            lastNode = previousNode;
        }
    }

}

// EOF
