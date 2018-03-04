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

package splitstree5.core.topfilters;

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.BitSetUtils;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.workflow.DataNode;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

/**
 * tree top taxon filter
 * Daniel Huson, 12/12/16.
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
    public TreesTopFilter(DataNode<TaxaBlock> originalTaxaNode, DataNode<TaxaBlock> modifiedTaxaNode, DataNode<TreesBlock> parentBlock, DataNode<TreesBlock> childBlock) {
        super(originalTaxaNode.getDataBlock(), modifiedTaxaNode, parentBlock, childBlock);

        setAlgorithm(new Algorithm<TreesBlock, TreesBlock>("TopFilter") {
            public void compute(ProgressListener progress, TaxaBlock modifiedTaxaBlock, TreesBlock parent, TreesBlock child) throws CanceledException {
                final TaxaBlock originalTaxa = originalTaxaNode.getDataBlock();

                if (originalTaxa.getTaxa().equals(modifiedTaxaBlock.getTaxa())) {
                    child.copy(parent);
                    setShortDescription("using all " + modifiedTaxaBlock.size() + " taxa");
                } else {
                    final int[] oldTaxonId2NewTaxonId = new int[originalTaxa.getNtax() + 1];
                    for (int t = 1; t <= originalTaxa.getNtax(); t++) {
                        oldTaxonId2NewTaxonId[t] = modifiedTaxaBlock.indexOf(originalTaxa.get(t).getName());
                    }

                    progress.setMaximum(parent.getNTrees());

                    for (PhyloTree tree : parent.getTrees()) {
                        // PhyloTree inducedTree = computeInducedTree(tree, modifiedTaxaBlock.getLabels());
                        PhyloTree inducedTree = computeInducedTree(oldTaxonId2NewTaxonId, tree);
                        if (inducedTree != null) {
                            child.getTrees().add(inducedTree);
                        }
                        progress.incrementProgress();
                    }

                    setShortDescription("using " + modifiedTaxaBlock.size() + " of " + getOriginalTaxaBlock().size() + " taxa");
                }
                child.setPartial(parent.isPartial());
                child.setRooted(parent.isRooted());
            }
        });
    }

    /**
     * computes the induced tree
     * @param oldTaxonId2NewTaxonId - old to new mapping, where removed taxa map to 0
     * @param originalTree
     * @return induced tree
     */
    private PhyloTree computeInducedTree(int[] oldTaxonId2NewTaxonId, PhyloTree originalTree) {
        final PhyloTree inducedTree = new PhyloTree();
        inducedTree.copy(originalTree);

        final List<Node> toRemove = new LinkedList<>(); // initially, set to all leaves that have lost their taxa

        // change taxa:
        for (Node v : inducedTree.nodes()) {
            if (inducedTree.getNumberOfTaxa(v) > 0) {
                BitSet taxa = new BitSet();
                for (Integer t : inducedTree.getTaxa(v)) {
                    if (oldTaxonId2NewTaxonId[t] > 0)
                        taxa.set(oldTaxonId2NewTaxonId[t]);
                }
                inducedTree.clearTaxa(v);
                if (taxa.cardinality() == 0)
                    toRemove.add(v);
                else {
                    for (Integer t : BitSetUtils.members(taxa)) {
                        inducedTree.addTaxon(v, t);
                    }
                }
            }
        }

        // delete all nodes that don't belong to the induced tree
        while (toRemove.size() > 0) {
            final Node v = toRemove.remove(0);
            for (Edge e : v.inEdges()) {
                final Node w = e.getSource();
                if (w.getOutDegree() == 1 && inducedTree.getNumberOfTaxa(w) == 0) {
                    toRemove.add(w);
                }
            }
            if (inducedTree.getRoot() == v) {
                inducedTree.deleteNode(v);
                inducedTree.setRoot(null);
                return null; // tree has completely disappeared...
            }
            inducedTree.deleteNode(v);
        }

        // remove path from original root to new root:

        Node root = inducedTree.getRoot();
        while (inducedTree.getNumberOfTaxa(root) == 0 && root.getOutDegree() == 1) {
            root = root.getFirstOutEdge().getTarget();
            inducedTree.deleteNode(inducedTree.getRoot());
            inducedTree.setRoot(root);
        }

        // remove all divertices
        final List<Node> diVertices = new LinkedList<>();
        for (Node v : inducedTree.nodes()) {
            if (v.getInDegree() == 1 && v.getOutDegree() == 1)
                diVertices.add(v);
        }
        for (Node v : diVertices) {
            inducedTree.delDivertex(v);
        }

        return inducedTree;
    }
}
