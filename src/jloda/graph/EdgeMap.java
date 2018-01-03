/**
 * EdgeMap.java
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
 * @version $Id: EdgeMap.java,e 1.2 2005-12-05 13:25:45 huson Exp $
 * @author Daniel Huson
 * @version $Id: EdgeMap.java,e 1.2 2005-12-05 13:25:45 huson Exp $
 * @author Daniel Huson
 */
/**
 * @version $Id: EdgeMap.java,e 1.2 2005-12-05 13:25:45 huson Exp $
 *
 * @author Daniel Huson
 *
 */
package jloda.graph;

/**
 * Edge map
 * Daniel Huson, 2003
 * @deprecated use edge array instead
 */

public class EdgeMap<T> extends EdgeArray<T> {
    /**
     * Construct a edge map.
     *
     * @param g Graph
     */
    public EdgeMap(Graph g) {
        super(g);
    }
}