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
package splitstree5.view.phylotreeview;

import java.util.Iterator;

/**
 * compute edge length
 * Daniel Huson, 10.2017
 */
public class EdgeLengthsCalculation {
    /**
     * compute edge edge lengths of given type
     *
     * @param root
     * @param type
     */
    public static FloatArray computeEdgeLengths(TreeNode root, PhylogeneticTreeView.EdgeLengths type) {
        final FloatArray edgeLengths = new FloatArray();
        computeEdgeLengths(root, type, edgeLengths);
        return edgeLengths;
    }

    /**
     * compute edge edge lengths of given type
     *
     * @param root
     * @param type
     */
    public static void computeEdgeLengths(final TreeNode root, PhylogeneticTreeView.EdgeLengths type, FloatArray edgeLengths) {
        switch (type) {
            case Cladogram: {
                final IntArray node2depth = new IntArray();
                computeNode2DepthRec(root, node2depth);
                setEdgeLengthsRec(root, node2depth, edgeLengths);
                break;
            }
            case CladogramEarlyBranching: {
                final int maxDepth = computeMaxDepthRec(root);
                setEdgeLengthsEarlyBranchingRec(maxDepth, 0, root, edgeLengths);
                break;
            }
            case Uniform: {
                final Iterator<TreeEdge> it = root.iteratorAllEdgesBelow();
                while (it.hasNext()) {
                    final TreeEdge e = it.next();
                    edgeLengths.set(e.getId(), 1f);
                }
                break;
            }
            default:
            case Weights: {
                final Iterator<TreeEdge> it = root.iteratorAllEdgesBelow();
                while (it.hasNext()) {
                    final TreeEdge e = it.next();
                    edgeLengths.set(e.getId(), (float) e.getWeight());

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
    private static int computeMaxDepthRec(TreeNode v) {
        if (v.isLeaf())
            return 0;
        else {
            int depthBelow = 0;
            for (TreeEdge e : v.getOutEdges()) {
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
    private static int computeNode2DepthRec(TreeNode v, final IntArray node2depth) {
        int depth = 0;
        for (TreeEdge e : v.getOutEdges()) {
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
    private static void setEdgeLengthsEarlyBranchingRec(int maxDepth, int depth, TreeNode v, FloatArray edgeLengths) {
        for (TreeEdge e : v.getOutEdges()) {
            final TreeNode w = e.getTarget();
            if (w.isLeaf()) {
                edgeLengths.set(e.getId(), (float) (maxDepth - depth));
            } else {
                edgeLengths.set(e.getId(), 1f);
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
    private static float setEdgeLengthsRec(TreeNode v, IntArray node2depth, FloatArray edgeLengths) {
        float depth = 0;
        for (TreeEdge e : v.getOutEdges()) {
            depth = Math.max(depth, setEdgeLengthsRec(e.getTarget(), node2depth, edgeLengths) + 1);
        }
        for (TreeEdge e : v.getOutEdges()) {
            edgeLengths.set(e.getId(), depth - node2depth.get(e.getTarget().getId()));
        }
        return depth;
    }
}
