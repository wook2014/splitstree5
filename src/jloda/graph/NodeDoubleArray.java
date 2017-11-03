/**
 * NodeDoubleArray.java
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
 * @version $Id: NodeDoubleArray.java,v 1.7 2007-10-23 13:10:53 huson Exp $
 * @author Daniel Huson
 */
/**
 * @version $Id: NodeDoubleArray.java,v 1.7 2007-10-23 13:10:53 huson Exp $
 *
 * @author Daniel Huson
 *
 */
package jloda.graph;


/**
 * Node array
 * Daniel Huson, 2003
 */

public class NodeDoubleArray extends GraphBase implements NodeAssociation<Double> {
    private double[] data;
    private boolean isClear = true;

    /**
     * Construct a node array.
     *
     * @param g Graph
     */
    public NodeDoubleArray(Graph g) {
        setOwner(g);
        data = new double[g.getMaxNodeId() + 1];
        g.registerNodeAssociation(this);
    }

    /**
     * Construct a node array for the given graph and initialize all entries
     * to obj.
     *
     * @param g   Graph
     * @param obj Object
     */
    public NodeDoubleArray(Graph g, Double obj) {
        this(g);
        setAll(obj);
    }

    /**
     * Copy constructor.
     *
     * @param src NodeArray
     */
    public NodeDoubleArray(NodeAssociation<Double> src) {
        this(src.getOwner());
        for (Node v = getOwner().getFirstNode(); v != null; v = v.getNext())
            set(v, src.get(v));
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
     * Get the entry for node v or the default object
     *
     * @param v Node
     * @return an Object the entry for node v
     */
    public Double get(Node v) {
        checkOwner(v);
        if (v.getId() < data.length)
            return data[v.getId()];
        else
            return 0.0;
    }

    /**
     * Set the entry for node v to obj.
     *
     * @param v   Node
     * @param obj Object
     */
    public void set(Node v, Double obj) {
        checkOwner(v);

        if (obj == null)
            obj = 0.0;
        else if (isClear)
            isClear = false;

        if (v.getId() >= data.length) {
            grow(v.getId());
        }
        data[v.getId()] = obj;
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
            double[] newData = new double[newSize];
            for (Node v = getOwner().getFirstNode(); v != null; v = v.getNext())
                if (v.getId() < data.length)
                    newData[v.getId()] = data[v.getId()];
            data = newData;
        }
    }

    /**
     * Set the entry for all nodes to obj.
     *
     * @param obj Object
     */
    public void setAll(Double obj) {
        if (obj == null)
            obj = 0.0;
        for (Node v = getOwner().getFirstNode(); v != null; v = v.getNext()) {
            if (v.getId() >= data.length) {
                grow(v.getId());
            }
            data[v.getId()] = obj;
        }
        isClear = (obj == 0.0);
    }


    /**
     * is array erase, that is, has nothing been set
     *
     * @return true, if erase
     */
    public boolean isClear() {
        return isClear;
    }

    /**
     * create a clone
     *
     * @return clone
     */
    public Object clone() {
        Graph graph = getOwner();
        NodeDoubleArray result = new NodeDoubleArray(graph);
        result.data = new double[data.length];
        System.arraycopy(data, 0, result.data, 0, data.length);
        return result;
    }
}

// EOF
