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

import java.util.*;

/**
 * base class for rooted tree node
 * Daniel Huson, 10.2017
 */
public abstract class TreeNode {
    private ArrayList<TreeNode> children;
    private int sizeOfSubTree; // number of nodes in the subtree rooted at this node

    /**
     * get all out edges
     *
     * @return out edges
     */
    public abstract Collection<TreeEdge> getOutEdges();

    /**
     * get label
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
     * get id
     *
     * @return id
     */
    public abstract int getId();

    /**
     * get data map
     *
     * @return data
     */
    public abstract Map<String, Object> getData();

    /**
     * get all children below
     *
     * @return children
     */
    public Collection<TreeNode> getChildren() {
        if (children == null) {
            children = new ArrayList<>();
            for (TreeEdge edge : getOutEdges()) {
                children.add(edge.getTarget());
            }
        }
        return children;
    }

    /**
     * iterator over this node and all below, top down
     *
     * @return node iterator
     */
    public Iterator<TreeNode> iteratorAllNodesBelow() {
        return new Iterator<TreeNode>() {
            private final Stack<TreeNode> stack = new Stack<>();

            {
                stack.push(TreeNode.this);
            }

            @Override
            public boolean hasNext() {
                return stack.size() > 0;
            }

            @Override
            public TreeNode next() {
                if (stack.size() == 0)
                    return null;
                final TreeNode node = stack.pop();
                stack.addAll(node.getChildren());
                return node;
            }
        };
    }

    public Collection<TreeNode> collectAllNodesBelow() {
        final ArrayList<TreeNode> result = new ArrayList<>();
        for (Iterator<TreeNode> it = iteratorAllNodesBelow(); it.hasNext(); ) {
            result.add(it.next());
        }
        return result;
    }

    /**
     * iterator over all edges below this node
     *
     * @return edge iterator
     */
    public Iterator<TreeEdge> iteratorAllEdgesBelow() {
        return new Iterator<TreeEdge>() {
            private final ArrayList<TreeEdge> list = new ArrayList<>();

            {
                list.addAll(TreeNode.this.getOutEdges());
            }

            @Override
            public boolean hasNext() {
                return list.size() > 0;
            }

            @Override
            public TreeEdge next() {
                if (list.size() == 0)
                    return null;
                final TreeEdge edge = list.remove(0);
                list.addAll(edge.getTarget().getOutEdges());
                return edge;
            }
        };
    }

    public Collection<TreeEdge> collectAllEdgesBelow() {
        final ArrayList<TreeEdge> result = new ArrayList<>();
        for (Iterator<TreeEdge> it = iteratorAllEdgesBelow(); it.hasNext(); ) {
            result.add(it.next());
        }
        return result;
    }

    /**
     * compute the number of nodes in the tree rooted at this node
     *
     * @return number of nodes in subtree
     */
    public int numberOfNodes() {
        if (sizeOfSubTree == 0) {
            for (Iterator<TreeNode> it = iteratorAllNodesBelow(); it.hasNext(); it.next())
                sizeOfSubTree++;
        }
        return sizeOfSubTree;
    }

    /**
     * get number of edges in subtree
     *
     * @return edges in subtree
     */
    public int numberOfEdges() {
        return numberOfNodes() - 1;
    }

    public boolean isLeaf() {
        return getOutEdges().size() == 0;
    }

    /**
     * are all children leaves?
     *
     * @return true, if all children are leaves
     */
    public boolean isAllChildrenAreLeaves() {
        for (TreeEdge treeEdge : getOutEdges())
            if (!treeEdge.getTarget().isLeaf())
                return false;
        return true;
    }

    /**
     * count all leaves
     *
     * @return leaves
     */
    public int countLeaves() {
        if (isLeaf())
            return 1;
        else {
            int count = 0;
            for (TreeEdge e : getOutEdges()) {
                count += e.getTarget().countLeaves();
            }
            return count;
        }
    }
}
