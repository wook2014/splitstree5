/**
 * GraphUpdateAdapter.java
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
 *
 * @version $Id: GraphUpdateAdapter.java,v 1.4 2005-01-07 14:23:05 huson Exp $
 * @author Daniel Huson
 * 11.02
 */
/**
 *@version $Id: GraphUpdateAdapter.java,v 1.4 2005-01-07 14:23:05 huson Exp $
 *
 *@author Daniel Huson
 * 11.02
 */
package jloda.graph;

/** Extend this to get a GraphUpdateListener
 * Daniel Huson, 2003
 */
public class GraphUpdateAdapter implements GraphUpdateListener {
    /** A node has been created
     *@param v the new node
     */
    public void newNode(Node v) {
    }

    /** A node is about to be deleted
     *@param v the node that will be deleted
     */
    public void deleteNode(Node v) {
    }

    /** An edge has been created
     *@param e the new edge
     */
    public void newEdge(Edge e) {
    }

    /** An edge is about to be deleted
     *@param e the edge that will be deleted
     */
    public void deleteEdge(Edge e) {
    }

    /** The graph has changed.
     * This method is called after one of the above specific methods has be
     * called
     */
    public void graphHasChanged() {
    }

    /** (Partial) graph was read from Reader
     *@param nodes the new nodes
     *@param edges the new edges
     */
    public void graphWasRead(NodeSet nodes, EdgeSet edges) {
    }
}
