/**
 * NodeDoubleMap.java
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
 * @version $Id: NodeDoubleMap.java,v 1.2 2005-12-05 13:25:44 huson Exp $
 * @author Daniel Huson
 */
/**
 * @version $Id: NodeDoubleMap.java,v 1.2 2005-12-05 13:25:44 huson Exp $
 *
 * @author Daniel Huson
 *
 */
package jloda.graph;


/**
 * Node map
 * Daniel Huson, 2003
 */

public class NodeDoubleMap extends NodeMap<Double> {
    /**
     * Construct a node double map for the given graph and initialize all
     * entries to value.
     *
     * @param g   Graph
     * @param val double
     */
    public NodeDoubleMap(Graph g, double val) {
        super(g, val);
    }

    /**
     * Construct a node double map for the given graph.
     *
     * @param g Graph
     */
    public NodeDoubleMap(Graph g) {
        super(g);
    }

    /**
     * Construct a node double map.
     *
     * @param src
     */
    public NodeDoubleMap(NodeDoubleArray src) {
        super(src);
    }

    /**
     * Construct a node double map.
     *
     * @param src
     */
    public NodeDoubleMap(NodeDoubleMap src) {
        super(src);
    }

    /**
     * Get the entry for node v.
     *
     * @param v Node
     * @return a double value the entry for node v
     */
    public double getValue(Node v) {
        if (super.get(v) == null)
            return 0;
        else
            return super.get(v);
    }

    /**
     * Set the entry for node v to obj.
     *
     * @param v   Node
     * @param val double
     */
    public void set(Node v, double val) {
        super.set(v, val);
    }

    /**
     * Set the entry for all nodes to val.
     *
     * @param val double
     */
    public void setAll(double val) {
        super.setAll(val);
    }
}

// EOF
