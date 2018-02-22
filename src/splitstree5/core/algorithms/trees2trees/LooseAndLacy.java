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

package splitstree5.core.algorithms.trees2trees;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import jloda.graph.*;
import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TraitsBlock;
import splitstree5.core.datablocks.TreesBlock;

import java.util.*;

/**
 * computes the loose and lacy species for a given tree and taxon trait
 * Daniel Huson, 2.2018
 */
public class LooseAndLacy extends Algorithm<TreesBlock, TreesBlock> implements IFromTrees, IToTrees {
    private final IntegerProperty optionTraitNumber = new SimpleIntegerProperty(1);

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, TreesBlock parent, TreesBlock child) throws Exception {
        final TraitsBlock traitsBlock = taxaBlock.getTraitsBlock();

        final PhyloTree tree = parent.getTrees().get(0);

        final NodeArray<BitSet> traitValuesBelow = new NodeArray<>(tree);
        final NodeArray<BitSet> taxaBelow = new NodeArray<>(tree);

        computeTaxaAndValuesBelowRec(tree, tree.getRoot(), traitsBlock, getOptionTraitNumber(), taxaBelow, traitValuesBelow);

        // compute loose species:
        final Set<BitSet> looseSpecies = new TreeSet<>(createComparator());
        computeLooseSpeciesRec(tree.getRoot(), taxaBelow, traitValuesBelow, looseSpecies);

        // compute loose species tree:
        PhyloTree looseTree = treeFromParition(taxaBlock, looseSpecies);
        looseTree.setName("Loose species");


        // compute lacy species:
        final Set<BitSet> lacySpecies = new TreeSet<>(createComparator());
        computeLacySpeciesRec(tree.getRoot(), taxaBelow, traitValuesBelow, lacySpecies);

        PhyloTree lacyTree = treeFromParition(taxaBlock, lacySpecies);
        lacyTree.setName("Lacy species");

        System.err.println(String.format("Species definitions based on trait: [%d] %s", getOptionTraitNumber(), traitsBlock.getTraitLabel(getOptionTraitNumber())));
        System.err.println(String.format("Loose species (%d):", looseSpecies.size()));
        {
            int count = 0;
            for (BitSet set : looseSpecies) {
                System.err.println(String.format("[%d] %s", ++count, Basic.toString(set, " ")));
            }
        }
        System.err.println(String.format("Lacy species (%d):", lacySpecies.size()));
        {
            int count = 0;
            for (BitSet set : lacySpecies) {
                System.err.println(String.format("[%d] %s", ++count, Basic.toString(set, " ")));
            }
        }

        child.getTrees().addAll(looseTree, lacyTree);
    }

    /**
     * Label each node by all trait values on or below it
     *
     * @param tree
     * @param v
     * @param traitsBlock
     * @param traitNumber
     * @param traitValuesBelow
     */
    private void computeTaxaAndValuesBelowRec(PhyloTree tree, Node v, TraitsBlock traitsBlock, int traitNumber, NodeArray<BitSet> taxaBelow, NodeArray<BitSet> traitValuesBelow) {
        final BitSet taxa = new BitSet();
        final BitSet values = new BitSet();
        if (tree.getNumberOfTaxa(v) > 0) {
            for (int t : tree.getTaxa(v)) {
                taxa.set(t);
                values.set(traitsBlock.getTraitValue(t, traitNumber));
            }
        }
        for (Node w : v.children()) {
            computeTaxaAndValuesBelowRec(tree, w, traitsBlock, traitNumber, taxaBelow, traitValuesBelow);
            taxa.or(taxaBelow.get(w));
            values.or(traitValuesBelow.get(w));
        }
        taxaBelow.put(v, taxa);
        traitValuesBelow.put(v, values);
    }

    /**
     * compute the least upper bound for all traits
     *
     * @param ntax
     * @param traitsBlock
     * @result lub
     */
    private Set<BitSet> computeLeastUpperBound(int ntax, TraitsBlock traitsBlock) {
        final Graph graph = new Graph();
        final Node[] tax2node = new Node[ntax + 1];

        for (int i = 1; i <= ntax; i++) {
            tax2node[i] = graph.newNode();
            tax2node[i].setInfo(i);
        }

        final Map<Integer, Node> state2node = new HashMap<>();

        for (int trait = 1; trait <= traitsBlock.getNTraits(); trait++) {
            state2node.clear();
            for (int tax = 1; tax <= ntax; tax++) {
                final int state = traitsBlock.getTraitValue(tax, trait);
                final Node v = tax2node[tax];
                final Node w = state2node.get(state);
                if (w == null)
                    state2node.put(state, v);
                else if (!v.isAdjacent(w))
                    graph.newEdge(v, w);
            }
        }

        final Set<BitSet> result = new TreeSet<>(createComparator());

        final NodeSet visited = new NodeSet(graph);
        for (int t = 1; t <= ntax; t++) {
            Node v = tax2node[t];
            if (!visited.contains(v)) {
                final BitSet set = new BitSet();
                final Stack<Node> stack = new Stack<>();
                stack.push(v);
                while (stack.size() > 0) {
                    v = stack.pop();
                    for (Node w : v.adjacentNodes()) {
                        if (!visited.contains(w)) {
                            visited.add(w);
                            set.set((Integer) w.getInfo());
                            for (Node u : w.adjacentNodes()) {
                                if (!visited.contains(u))
                                    stack.push(u);
                            }
                            w.deleteAllAdjacentEdges();
                        }
                    }
                }
                if (set.cardinality() > 0)
                    result.add(set);
            }
        }
        return result;
    }


    /**
     * compute the greatest lower bound for all traits
     *
     * @param ntax
     * @param traitsBlock
     * @result glb
     */
    private Set<BitSet> computeGreatestLowerBound(int ntax, TraitsBlock traitsBlock) {
        final Set<BitSet> result = new TreeSet<>(createComparator());

        System.err.println("Not implemented");

        return result;
    }


    /**
     * compute the loose species
     *
     * @param v
     * @param taxaBelow
     * @param traitValuesBelow
     * @param looseSpecies
     */
    private void computeLooseSpeciesRec(Node v, NodeArray<BitSet> taxaBelow, NodeArray<BitSet> traitValuesBelow, Set<BitSet> looseSpecies) {
        final int sizeV = traitValuesBelow.get(v).cardinality();

        int sumSizeChildren = 0;
        for (Node w : v.children()) {
            sumSizeChildren += traitValuesBelow.get(w).cardinality();
        }

        if (sumSizeChildren == 0 || sizeV < sumSizeChildren)
            looseSpecies.add(taxaBelow.get(v));
        else for (Node w : v.children())
            computeLooseSpeciesRec(w, taxaBelow, traitValuesBelow, looseSpecies);
    }

    /**
     * compute the lacy species
     *
     * @param v
     * @param taxaBelow
     * @param traitValuesBelow
     * @param lacySplits
     */
    private void computeLacySpeciesRec(Node v, NodeArray<BitSet> taxaBelow, NodeArray<BitSet> traitValuesBelow, Set<BitSet> lacySplits) {
        final int sizeV = traitValuesBelow.get(v).cardinality();
        if (sizeV == 1)
            lacySplits.add(taxaBelow.get(v));
        else
            for (Node w : v.children())
                computeLacySpeciesRec(w, taxaBelow, traitValuesBelow, lacySplits);
    }


    /**
     * computes a tree from a partition of taxa
     *
     * @param taxaBlock
     * @param partition
     * @return tree
     */
    private PhyloTree treeFromParition(TaxaBlock taxaBlock, Set<BitSet> partition) {
        final PhyloTree tree = new PhyloTree();
        final Node root = tree.newNode();
        tree.setRoot(root);

        for (BitSet set : partition) {
            Node v = tree.newNode();
            Edge e = tree.newEdge(root, v);

            if (set.cardinality() == 1) {
                tree.setWeight(e, 2);
                int taxon = set.nextSetBit(0);
                tree.setLabel(v, taxaBlock.get(taxon).getName());
                tree.addTaxon(v, taxon);
            } else {
                tree.setWeight(e, 1);
                for (Integer t = set.nextSetBit(1); t != -1; t = set.nextSetBit(t + 1)) {
                    Node w = tree.newNode();
                    tree.addTaxon(w, t);
                    tree.setLabel(w, taxaBlock.getLabel(t));
                    Edge f = tree.newEdge(v, w);
                    tree.setWeight(f, 1);
                }
            }
        }
        return tree;
    }


    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, TreesBlock parent, TreesBlock child) {
        return taxaBlock.getTraitsBlock() != null && taxaBlock.getTraitsBlock().getNTraits() > 0 && parent.getNTrees() > 0;

    }

    public int getOptionTraitNumber() {
        return optionTraitNumber.get();
    }

    public IntegerProperty optionTraitNumberProperty() {
        return optionTraitNumber;
    }

    public void setOptionTraitNumber(int optionTraitNumber) {
        this.optionTraitNumber.set(optionTraitNumber);
    }

    @Override
    public String getCitation() {
        return "Hoppe et al (2018);Anica Hoppe, Sonja Türpitz, Mike Steel. Species notions that combine phylogenetic trees and phenotypic partitions. arXiv:1711.08145v1.";

    }

    private static Comparator<BitSet> createComparator() {
        return (s1, s2) -> {
            int a1 = -1;
            int a2 = -1;
            while (true) {
                a1 = s1.nextSetBit(a1 + 1);
                a2 = s2.nextSetBit(a2 + 1);
                if (a1 < a2)
                    return a1 == -1 ? 1 : -1;
                else if (a1 > a2)
                    return 1;
                else if (a1 == -1) // equal and both over
                    return 0;

            }
        };
    }
}
