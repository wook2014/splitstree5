/**
 * EdgeSet.java
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
 * <p>
 * Edge set
 *
 * @author Daniel Huson, 2003
 */
/**
 * Edge set
 * @author Daniel Huson, 2003
 *
 */
package jloda.graph;

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * EdgeSet implements a set of edges contained in a given graph
 */
public class EdgeSet extends GraphBase implements Set<Edge> {
    final BitSet bits;

    /**
     * Constructs a new empty EdgeSet for Graph G.
     *
     * @param graph Graph
     */
    public EdgeSet(Graph graph) {
        setOwner(graph);
        graph.registerEdgeSet(this);
        bits = new BitSet();
    }

    /**
     * Is edge v member?
     *
     * @param e Edge
     * @return a boolean value
     */
    public boolean contains(Object e) {
        return e instanceof Edge && bits.get(((Edge) e).getId());
    }

    /**
     * Insert edge e.
     *
     * @param e Edge
     * @return true, if new
     */
    public boolean add(Edge e) {
        if (contains(e))
            return false;
        else {
            bits.set(e.getId(), true);
            return true;
        }
    }

    /**
     * Delete edge v from set.
     *
     * @param e Edge
     */
    public boolean remove(Object e) {
        if (e instanceof Edge && contains(e)) {
            bits.set(((Edge) e).getId(), false);
            return true;
        } else
            return false;

    }

    /**
     * adds all edges in the given collection
     *
     * @param collection
     * @return true, if some element is new
     */
    public boolean addAll(Collection collection) {
        Iterator it = collection.iterator();

        boolean result = false;
        while (it.hasNext()) {
            if (add((Edge) it.next()))
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
    public boolean containsAll(Collection collection) {

        for (Object aCollection : collection) {
            if (!contains(aCollection))
                return false;
        }
        return true;
    }

    /**
     * removes all edges in the collection
     *
     * @param collection
     * @return true, if something actually removed
     */
    public boolean removeAll(Collection collection) {
        Iterator it = collection.iterator();

        boolean result = false;
        while (it.hasNext()) {
            if (remove(it.next()))
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
    public boolean retainAll(Collection collection) {
        final int old = bits.cardinality();
        final BitSet newBits = new BitSet();

        for (Object obj : collection) {
            if (obj instanceof Edge) {
                newBits.set(((Edge) obj).getId());
            }
        }
        bits.and(newBits);
        return old != bits.cardinality();
    }

    /**
     * Delete all edges from set.
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
     * return all contained edges as edges
     *
     * @return contained edges
     */
    public Edge[] toArray() {
        Edge[] result = new Edge[bits.cardinality()];
        int i = 0;
        for (Edge e : getOwner().edges()) {
            if (contains(e))
                result[i++] = e;
        }
        return result;
    }

    public <T> T[] toArray(T[] ts) {
        int i = 0;
        for (Edge e : getOwner().edges()) {
            if (contains(e))
                ts[i++] = (T) e;
        }
        return ts;
    }

    /**
     * Puts all edges into set.
     */
    public void addAll() {
        for (Edge e : getOwner().edges()) {
            add(e);
        }
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
     * Returns an enumeration of the elements in the set.
     *
     * @return an enumeration of the elements in the set
     */
    public Iterator<Edge> iterator() {
        return new Iterator<Edge>() {
            Edge e = getFirstElement();

            @Override
            public boolean hasNext() {
                return e != null;
            }

            @Override
            public Edge next() {
                Edge result = e;
                e = getNextElement(e);
                return result;
            }
        };
    }

    /**
     * returns the edges in the given array, if they fit, or in a new array, otherwise
     *
     * @param edges
     * @return edges in this set
     */
    public Edge[] toArray(Edge[] edges) {
        return toArray();
        /*
        if (edges == null)
            throw new NullPointerException();
        if (bits.cardinality() > edges.length)
            edges = (Edge[]) Array.newInstance((edges[0]).getClass(), bits.cardinality());

        int i = 0;
        Iterator it = getOwner().edgeIterator();
        while (it.hasNext()) {
            Edge v = it.next();
            if (contains(v) == true)
                edges[i++] = it.next();
        }
        return edges;
        */
    }

    /**
     * Returns the first element in the set.
     *
     * @return v Edge
     */
    public Edge getFirstElement() {
        Edge e;
        for (e = getOwner().getFirstEdge(); e != null; e = getOwner().getNextEdge(e))
            if (contains(e))
                break;
        return e;
    }

    /**
     * Gets the successor element in the set.
     *
     * @param v Edge
     * @return a Edge the successor of edge v
     */
    public Edge getNextElement(Edge v) {
        for (v = getOwner().getNextEdge(v); v != null; v = getOwner().getNextEdge(v))
            if (contains(v))
                break;
        return v;
    }

    /**
     * Gets the predecessor element in the set.
     *
     * @param v Edge
     * @return a Edge the predecessor of edge v
     */
    public Edge getPrevElement(Edge v) {
        for (v = getOwner().getPrevEdge(v); v != null; v = getOwner().getPrevEdge(v))
            if (contains(v))
                break;
        return v;
    }


    /**
     * Returns the last element in the set.
     *
     * @return the Edge the last element in the set
     */
    public Edge getLastElement() {
        Edge v = null;
        for (v = getOwner().getLastEdge(); v != null; v = getOwner().getPrevEdge(v))
            if (contains(v))
                break;
        return v;
    }

    /**
     * returns a clone of this set
     *
     * @return a clone
     */
    public Object clone() {
        EdgeSet result = new EdgeSet(getOwner());
        for (Edge edge : this) result.add(edge);
        return result;
    }

    /**
     * do the two sets have a non-empty intersection?
     *
     * @param aset
     * @return true, if intersection is non-empty
     */
    public boolean intersects(EdgeSet aset) {
        return bits.intersects(aset.bits);
    }
}

// EOF
