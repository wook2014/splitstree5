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
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import splitstree5.xtra.phylotreeview.TreeEdge;
import splitstree5.xtra.phylotreeview.TreeNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * implements a rooted tree node for a phylo tree
 * Daniel Huson, 10.2017
 */
public class PhyloTreeNode extends TreeNode {
    final private Node v;
    private Map<String, Object> data;
    private Collection<TreeEdge> outEdges;

    /**
     * constructor
     *
     * @param v
     */
    public PhyloTreeNode(Node v) {
        this.v = v;
    }

    @Override
    public String getLabel() {
        return ((PhyloTree) v.getOwner()).getLabel(v);
    }

    public void setLabel(String label) {
        ((PhyloTree) v.getOwner()).setLabel(v, label);
    }

    @Override
    public int getId() {
        return v.getId();
    }

    @Override
    public Map<String, Object> getData() {
        if (data == null)
            data = new HashMap<>();
        return data;
    }

    @Override
    public Collection<TreeEdge> getOutEdges() {
        if (outEdges == null) {
            outEdges = new ArrayList<>(v.getOutDegree());
            for (Edge e : v.outEdges()) {
                outEdges.add(new PhyloTreeEdge(e));
            }
        }
        return outEdges;
    }
}
