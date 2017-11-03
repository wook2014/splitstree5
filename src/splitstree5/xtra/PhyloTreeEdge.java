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

package splitstree5.xtra;

import jloda.graph.Edge;
import jloda.phylo.PhyloTree;
import splitstree5.xtra.phylotreeview.TreeEdge;
import splitstree5.xtra.phylotreeview.TreeNode;

import java.util.HashMap;
import java.util.Map;

/**
 * implements a rooted tree edge for a phylo tree
 * Daniel Huson, 10.2017
 */
public class PhyloTreeEdge extends TreeEdge {
    private final Edge e;
    private Map<String, Object> data;
    private TreeNode target;

    public PhyloTreeEdge(Edge e) {
        this.e = e;
    }

    @Override
    public TreeNode getTarget() {
        if (target == null) {
            target = new PhyloTreeNode(e.getTarget());
        }
        return target;
    }

    @Override
    public String getLabel() {
        return ((PhyloTree) e.getOwner()).getLabel(e);
    }

    @Override
    public void setLabel(String label) {
        ((PhyloTree) e.getOwner()).setLabel(e, label);
    }

    @Override
    public float getWeight() {
        return (float) ((PhyloTree) e.getOwner()).getWeight(e);
    }

    @Override
    public void setWeight(float weight) {
        ((PhyloTree) e.getOwner()).setWeight(e, weight);
    }

    @Override
    public int getId() {
        return e.getId();
    }

    @Override
    public Map<String, Object> getData() {
        if (data == null)
            data = new HashMap<>();
        return data;
    }
}
