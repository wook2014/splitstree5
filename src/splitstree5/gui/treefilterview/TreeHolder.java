/*
 *  Copyright (C) 2018 Daniel H. Huson
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

package splitstree5.gui.treefilterview;

import jloda.phylo.PhyloTree;

import java.io.Serializable;

/**
 * holder for tree. We use this because making PhyloTree Serializable causes problems
 */
public class TreeHolder implements Serializable {
    private final String treeString;
    private final int ref;

    public TreeHolder(PhyloTree tree, int ref) {
        if (tree.getName() != null)
            treeString = tree.getName();
        else {
            String label = String.format("[%d] %s", (ref + 1), tree.toBracketString());
            if (label.length() > 85)
                label = label.substring(0, 80) + "...";
            treeString = label;
        }
        this.ref = ref;
    }

    public Integer getRef() {
        return ref;
    }

    public String toString() {
        return treeString;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TreeHolder) {
            final TreeHolder that = (TreeHolder) obj;
            return ref == that.ref;
        }
        return false;
    }
}
