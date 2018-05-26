/**
 * NodeSet.java
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
package jloda.graph;

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * NodeSet implements a set of nodes contained in a given graph
 * Daniel Huson, 2003
 */
public class NodeSet extends GraphBase implements Set<Node> {
    final BitSet bits;

    /**
     * Constructs a new empty NodeSet for Graph G.
     *
     * @param graph Graph
     */
    public NodeSet(Graph graph) {
        setOwner(graph);
        graph.registerNodeSet(this);
        bits = new BitSet();
    }

    /**
     * Is node v member?
     *
     * @param v Node
     * @return a boolean value
     */
    public boolean contains(Object v) {
        return v instanceof Node && bits.get(((Node) v).getId());
    }

    /**
     * Insert node v.
     *
     * @param v Node
     * @return true, if new
     */
    public boolean add(Node v) {
        if (bits.get(v.getId()))
            return false;
        else {
            bits.set(v.getId(), true);
            return true;
        }
    }

    /**
     * Delete node v from set.
     *
     * @param v Node
     */
    public boolean remove(Object v) {
        if (v instanceof Node) {
            if (bits.get(((Node) v).getId())) {
                bits.set(((Node) v).getId(), false);
                return true;
            }
        }
        return false;

    }

    /**
     * adds all nodes in the given collection
     *
     * @param collection
     * @return true, if some element is new
     */
    public boolean addAll(final Collection<? extends Node> collection) {
        boolean result = false;
        for (Node v : collection) {
            if (add(v))
                result = true;
        }
        return result;
    }

    /**
     * returns true if all elements of collection are contained in this set
     *
     * @param collection
     * @return all contained?
     */
    public boolean containsAll(final Collection<?> collection) {
        for (Object obj : collection) {
            if (!contains(obj))
                return false;
        }
        return true;
    }

    /**
     * equals
     *
     * @param obj
     * @return true, if equal
     */
    public boolean equals(Object obj) {
        if (obj instanceof Collection) {
            Collection collection = (Collection) obj;
            return size() == collection.size() && containsAll(collection);
        } else
            return false;
    }

    /**
     * removes all nodes in the collection
     *
     * @param collection
     * @return true, if something actually removed
     */
    public boolean removeAll(final Collection<?> collection) {
        boolean result = false;
        for (Object obj : collection) {
            if (obj instanceof Node)
                if (remove(obj))
                    result = true;
        }
        return result;
    }

    /**
     * keep only those elements contained in the collection
     *
     * @param collection
     * @return true, if set changes
     */
    public boolean retainAll(final Collection<?> collection) {
        final int old = bits.cardinality();
        final BitSet newBits = new BitSet();

        for (Object obj : collection) {
            if (obj instanceof Node) {
                newBits.set(((Node) obj).getId());
            }
        }
        bits.and(newBits);
        return old != bits.cardinality();
    }

    /**
     * Delete all nodes from set.
     */
    public void clear() {
        bits.clear();
    }

    /**
     * is empty?
     *
     * @return true, if empty
     */
    public boolean isEmpty() {
        return bits.isEmpty();
    }

    /**
     * return all nodes as array
     *
     * @return contained nodes
     */
    public Node[] toArray() {
        final Node[] result = new Node[bits.cardinality()];
        int i = 0;
        for (Node v : getOwner().nodes()) {
            if (contains(v))
                result[i++] = v;
        }
        return result;
    }

    /**
     * Puts all nodes into set.
     */
    public void addAll() {
        for (Node v : getOwner().nodes()) {
            add(v);
        }
    }

    /**
     * Returns an enumeration of the elements in the set.
     *
     * @return an enumeration of the elements in the set
     */
    public Iterator<Node> iterator() {
        return successors(null).iterator();
    }

    /**
     * gets all successors
     *
     * @param afterMe
     * @return all successors
     */
    public Iterable<Node> successors(final Node afterMe) {
        return () -> new Iterator<Node>() {
            Node v = (afterMe == null ? getOwner().getFirstNode() : afterMe.getNext());

            {
                while (v != null) {
                    if (contains(v))
                        break;
                    v = v.getNext();
                }
            }

            @Override
            public boolean hasNext() {
                return v != null;
            }

            @Override
            public Node next() {
                Node result = v;
                {
                    v = v.getNext();
                    while (v != null) {
                        if (contains(v))
                            break;
                        v = v.getNext();
                    }
                }
                return result;
            }
        };
    }

    /**
     * Returns the size of the set.
     *
     * @return size
     */
    public int size() {
        return bits.cardinality();
    }

    /**
     * returns the set as many objects as fit into the given array
     *
     * @param objects
     * @return nodes in this set
     */
    public Node[] toArray(Node[] objects) {
        if (objects == null)
            throw new NullPointerException();
        int i = 0;
        for (Node node : this) {
            if (i == objects.length)
                break;
            objects[i++] = node;
        }
        return objects;
    }

    /**
     * todo: is this correct???
     *
     * @param objects
     * @param <T>
     * @return
     */
    public <T> T[] toArray(T[] objects) {
        int i = 0;
        for (Node node : this) {
            if (i == objects.length)
                break;
            objects[i++] = (T) node;
        }
        return objects;
    }

    /**
     * returns a clone of this set
     *
     * @return a clone
     */
    public Object clone() {
        NodeSet result = new NodeSet(getOwner());
        result.addAll(this);
        return result;
    }

    /**
     * returns string rep
     *
     * @return string
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        boolean first = true;
        for (Object o : this) {
            if (first)
                first = false;
            else
                buf.append(", ");
            buf.append(o);
        }
        buf.append("]");
        return buf.toString();
    }

    /**
     * do the two sets have a non-empty intersection?
     *
     * @param aset
     * @return true, if intersection is non-empty
     */
    public boolean intersects(NodeSet aset) {
        return bits.intersects(aset.bits);
    }

    public void close() {
        getOwner().registerNodeSet(this);
    }
}

// EOF
