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

package splitstree5.core.topfilters;

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * splits top taxon filter
 * Created by huson on 12/12/16.
 */
public class TreesTopFilter extends ATopFilter<TreesBlock> {
    /**
     * /**
     * constructor
     *
     * @param originalTaxaNode
     * @param modifiedTaxaNode
     * @param parentBlock
     * @param childBlock
     */
    public TreesTopFilter(ADataNode<TaxaBlock> originalTaxaNode, ADataNode<TaxaBlock> modifiedTaxaNode, ADataNode<TreesBlock> parentBlock, ADataNode<TreesBlock> childBlock) {
        super(originalTaxaNode.getDataBlock(), modifiedTaxaNode, parentBlock, childBlock);

        setAlgorithm(new Algorithm<TreesBlock, TreesBlock>("TopFilter") {
            public void compute(ProgressListener progressListener, TaxaBlock modifiedTaxaBlock, TreesBlock original, TreesBlock modified) {
                if (originalTaxaNode.getDataBlock().getTaxa().equals(modifiedTaxaBlock.getTaxa())) {
                    modified.copy(original);
                } else {
                    for (PhyloTree tree : original.getTrees()) {
                        PhyloTree induced = computeInducedTree(tree, modifiedTaxaBlock.getLabels());
                        if (induced != null) {
                            modified.getTrees().add(induced);
                        }
                    }
                }
            }
        });
    }

    /**
     * compute an induced tree
     *
     * @param originalTree
     * @param keep
     * @return induced tree or null
     */
    private PhyloTree computeInducedTree(PhyloTree originalTree, ArrayList<String> keep) {
        final PhyloTree inducedTree = new PhyloTree();
        inducedTree.copy(originalTree);

        final List<Node> nakedLeaves = new LinkedList<>();

        for (Node v : inducedTree.getNodes()) {
            if (inducedTree.getLabel(v) == null || !keep.contains(inducedTree.getLabel(v))) {
                if (v.getOwner() != null && inducedTree.getLabel(v) != null) // not yet deleted
                {
                    if (v.getOutDegree() > 0) {
                        inducedTree.setLabel(v, null);
                    } else {
                        for (Edge e = v.getFirstInEdge(); e != null; e = v.getNextInEdge(e)) {
                            Node w = e.getSource();
                            if (w.getOutDegree() == 1 && inducedTree.getLabel(w) == null) {
                                nakedLeaves.add(w);
                            }
                        }
                        if (inducedTree.getRoot() == v)
                            inducedTree.setRoot(null);
                        inducedTree.deleteNode(v);
                    }
                }
            }
        }

        while (nakedLeaves.size() > 0) {
            final Node v = nakedLeaves.remove(0);
            for (Edge e = v.getFirstInEdge(); e != null; e = v.getNextInEdge(e)) {
                final Node w = e.getSource();
                if (w.getOutDegree() == 1 && inducedTree.getLabel(w) == null) {
                    nakedLeaves.add(w);
                }
            }
            if (inducedTree.getRoot() == v)
                inducedTree.setRoot(null);
            inducedTree.deleteNode(v);
        }

        final List<Node> diVertices = new LinkedList<>();
        for (Node v = inducedTree.getFirstNode(); v != null; v = v.getNext()) {
            if (v.getInDegree() == 1 && v.getOutDegree() == 1)
                diVertices.add(v);
        }
        for (Node v : diVertices) {
            inducedTree.delDivertex(v);
            if (inducedTree.getRoot() == v)
                inducedTree.setRoot(null);
        }

        Node root = inducedTree.getRoot();

        if (root == null || root.getOwner() == null) {
            inducedTree.setRoot((Node) null);
            for (Node v = inducedTree.getFirstNode(); v != null; v = v.getNext()) {
                if (v.getInDegree() == 0) {
                    inducedTree.setRoot(v);
                    root = v;
                    break;
                }
            }
        }

        if (root != null) {
            if (root.getOutDegree() == 1 && inducedTree.getLabel(root) == null) {
                final Node newRoot = root.getFirstOutEdge().getTarget();
                inducedTree.deleteNode(root);
                inducedTree.setRoot(newRoot);
            }
        }

        return inducedTree;
    }
}
