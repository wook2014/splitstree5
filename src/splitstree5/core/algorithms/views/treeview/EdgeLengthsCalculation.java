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

/*
 *  Copyright (C) 2017 Daniel H. Huson
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
package splitstree5.core.algorithms.views.treeview;

import jloda.graph.Edge;
import jloda.graph.EdgeFloatArray;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import splitstree5.core.algorithms.views.TreeDrawer;
import splitstree5.view.phylotreeview.IntArray;

/**
 * compute edge length
 * Daniel Huson, 10.2017
 */
public class EdgeLengthsCalculation {
    /**
     * compute edge edge lengths of given type
     *
     * @param tree
     * @param type
     */
    public static EdgeFloatArray computeEdgeLengths(PhyloTree tree, TreeDrawer.EdgeLengths type) {
        final EdgeFloatArray edgeLengths = new EdgeFloatArray(tree);
        computeEdgeLengths(tree, type, edgeLengths);
        return edgeLengths;
    }

    /**
     * compute edge edge lengths of given type
     *
     * @param tree
     * @param type
     */
    public static void computeEdgeLengths(final PhyloTree tree, TreeDrawer.EdgeLengths type, EdgeFloatArray edgeLengths) {
        switch (type) {
            case Cladogram: {
                final IntArray node2depth = new IntArray();
                computeNode2DepthRec(tree.getRoot(), node2depth);
                setEdgeLengthsRec(tree.getRoot(), node2depth, edgeLengths);
                break;
            }
            case CladogramEarlyBranching: {
                final int maxDepth = computeMaxDepthRec(tree.getRoot());
                setEdgeLengthsEarlyBranchingRec(maxDepth, 0, tree.getRoot(), edgeLengths);
                break;
            }
            case Uniform: {
                for (Edge e : tree.getEdges()) {
                    edgeLengths.set(e, 1f);
                }
                break;
            }
            default:
            case Weights: {
                for (Edge e : tree.getEdges()) {
                    edgeLengths.set(e, (float) (Math.max(0, tree.getWeight(e))));
                }
                break;
            }
        }
    }

    /**
     * compute the max depth below
     *
     * @param v
     * @return max depth below
     */
    private static int computeMaxDepthRec(Node v) {
        if (v.getOutDegree() == 0)
            return 0;
        else {
            int depthBelow = 0;
            for (Edge e = v.getFirstOutEdge(); e != null; e = v.getNextOutEdge(e)) {
                depthBelow = Math.max(depthBelow, computeMaxDepthRec(e.getTarget()));
            }
            return depthBelow + 1;
        }
    }

    /**
     * compute the max depth
     *
     * @param v
     * @return max depth
     */
    private static int computeNode2DepthRec(Node v, final IntArray node2depth) {
        int depth = 0;
        for (Edge e = v.getFirstOutEdge(); e != null; e = v.getNextOutEdge(e)) {
            depth = Math.max(depth, computeNode2DepthRec(e.getTarget(), node2depth));
        }
        node2depth.set(v.getId(), depth);
        return depth + 1;
    }

    /**
     * computeedge lengths for early branching cladogram
     *
     * @param maxDepth
     * @param depth
     * @param v
     * @param edgeLengths
     */
    private static void setEdgeLengthsEarlyBranchingRec(int maxDepth, int depth, Node v, EdgeFloatArray edgeLengths) {
        for (Edge e = v.getFirstOutEdge(); e != null; e = v.getNextOutEdge(e)) {
            final Node w = e.getTarget();
            if (w.getOutDegree() == 0) {
                edgeLengths.set(e, (float) (maxDepth - depth));
            } else {
                edgeLengths.set(e, 1f);
                setEdgeLengthsEarlyBranchingRec(maxDepth, depth + 1, w, edgeLengths);
            }
        }
    }

    /**
     * set the weights
     *
     * @param v
     * @param node2depth
     * @return depth
     */
    private static float setEdgeLengthsRec(Node v, IntArray node2depth, EdgeFloatArray edgeLengths) {
        float depth = 0;
        for (Edge e = v.getFirstOutEdge(); e != null; e = v.getNextOutEdge(e)) {
            depth = Math.max(depth, setEdgeLengthsRec(e.getTarget(), node2depth, edgeLengths) + 1);
        }
        for (Edge e = v.getFirstOutEdge(); e != null; e = v.getNextOutEdge(e)) {
            edgeLengths.set(e, depth - node2depth.get(e.getTarget().getId()));
        }
        return depth;
    }
}