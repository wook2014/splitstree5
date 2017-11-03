/**
 * GraphBase.java
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
 * @version $Id: GraphBase.java,v 1.6 2005-01-30 13:00:39 huson Exp $
 * <p>
 * Base class for all graph related stuff.
 * @author Daniel Huson
 */
/**
 * @version $Id: GraphBase.java,v 1.6 2005-01-30 13:00:39 huson Exp $
 *
 * Base class for all graph related stuff.
 *
 * @author Daniel Huson
 */
package jloda.graph;


/**
 * graph base class
 * Daniel Huson, 2002
 */
public class GraphBase {
    private Graph owner;

    /**
     * Sets the owner.
     *
     * @param G Graph
     */
    void setOwner(Graph G) {
        owner = G;
    }

    /**
     * Returns the owning graph.
     *
     * @return owner a Graph
     */
    public Graph getOwner() {
        return owner;
    }

    /**
     * If this and obj do not have the same graph, throw NotOwnerException
     *
     * @param obj GraphBase
     */
    public void checkOwner(GraphBase obj) {
        if (obj == null)
            throw new IllegalArgumentException("object is null");
        if (obj.owner == null)
            throw new IllegalArgumentException("object's owner is null");
        if (owner == null)
            throw new IllegalArgumentException("reference's owner is null");
        if (owner != obj.owner) {
            throw new IllegalArgumentException("wrong owner");
        }
    }
}

// EOF
