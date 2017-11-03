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

package splitstree5.xtra.phylotreeview;

import java.util.Map;

public abstract class TreeEdge {
    /**
     * get the target node
     *
     * @return target node
     */
    public abstract TreeNode getTarget();

    /**
     * get the label
     *
     * @return label
     */
    public abstract String getLabel();

    /**
     * set the label
     *
     * @param label
     */
    public abstract void setLabel(String label);

    /**
     * get the weight
     *
     * @return weight
     */
    public abstract float getWeight();

    /**
     * set the weight
     */
    public abstract void setWeight(float weight);

    /**
     * get an id
     *
     * @return id
     */
    public abstract int getId();

    /**
     * get data objects
     *
     * @return data or null
     */
    public abstract Map<String, Object> getData();
}
