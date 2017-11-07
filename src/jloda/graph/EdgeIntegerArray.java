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

package jloda.graph;


/**
 * edge float array
 * Daniel Huson, 2003
 */
public class EdgeIntegerArray extends GraphBase implements EdgeAssociation<Integer> {
    private int data[];
    private boolean isClear = true;

    /**
     * Construct an edge array.
     *
     * @param g Graph
     */
    public EdgeIntegerArray(Graph g) {
        setOwner(g);
        data = new int[g.getMaxEdgeId() + 1];
        g.registerEdgeAssociation(this);
    }

    /**
     * Construct an edge array for the given graph and initialize all entries
     * to obj.
     *
     * @param g   Graph
     * @param obj Object
     */
    public EdgeIntegerArray(Graph g, Integer obj) {
        this(g);
        putAll(obj);
    }

    /**
     * Copy constructor.
     *
     * @param src EdgeArray
     */
    public EdgeIntegerArray(EdgeAssociation<Integer> src) {
        setOwner(src.getOwner());
        for (Edge e = getOwner().getFirstEdge(); e != null; e = e.getNext())
            put(e, src.get(e));
        isClear = src.isClear();
    }

    /**
     * Clear all entries.
     */
    public void clear() {
        for (int i = 0; i < data.length; i++)
            data[i] = 0;
        isClear = true;
    }


    /**
     * Get the entry for edge e.
     *
     * @param e Edge
     * @return an object the entry for edge e
     */
    public Integer get(Edge e) {
        checkOwner(e);
        if (e.getId() < data.length)
            return data[e.getId()];
        else
            return 0;
    }

    /**
     * Set the entry for edge e to obj.
     *
     * @param e   Edge
     * @param obj Object
     */
    public void put(Edge e, Integer obj) {
        checkOwner(e);
        if (obj == null)
            obj = 0;
        else if (isClear)
            isClear = false;

        if (e.getId() >= data.length) {
            grow(e.getId());
        }
        data[e.getId()] = obj;
    }

    @Override
    public void set(Edge e, Integer obj) {
        this.put(e, obj);
    }

    @Override
    public void setAll(Integer obj) {
        this.putAll(obj);
    }

    /**
     * grows the array. Repeatedly doubles the size of the array until it contains index n
     *
     * @param n index to be included in array
     */
    private void grow(int n) {
        int newSize = Math.max(1, 2 * data.length);
        while (newSize <= n)
            newSize *= 2;
        if (newSize > data.length) {
            int[] newData = new int[newSize];
            for (Edge e = getOwner().getFirstEdge(); e != null; e = e.getNext()) {
                int id = e.getId();
                if (id < data.length)
                    newData[id] = data[id];
            }
            data = newData;
        }
    }

    /**
     * Set the entry for all edges.
     *
     * @param obj Object
     */
    public void putAll(Integer obj) {
        if (obj == null)
            obj = 0;
        for (Edge e = getOwner().getFirstEdge(); e != null; e = e.getNext()) {
            if (e.getId() >= data.length) {
                grow(e.getId());
            }
            data[e.getId()] = obj;
        }
        isClear = (obj == 0);
    }

    /**
     * is clean, that is, has never been set since last erase
     *
     * @return true, if erase
     */
    public boolean isClear() {
        return isClear;
    }
}

// EOF
