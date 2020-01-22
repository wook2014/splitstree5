/*
 *  TreesFilter3.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.core.algorithms.filters;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import jloda.fx.window.NotificationManager;
import jloda.phylo.PhyloTree;
import jloda.util.BitSetUtils;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.workflow.Workflow;
import splitstree5.utils.TreesUtilities;

import java.util.*;

/**
 * additional tree filtering
 * Daniel Huson, 1/2019
 */
public class TreesFilter3 extends Algorithm<TreesBlock, TreesBlock> implements IFromTrees, IToTrees, IFilter {
    private final BooleanProperty optionBestTaxonSubset = new SimpleBooleanProperty(true);
    private final BooleanProperty optionFilterTaxa = new SimpleBooleanProperty(true);
    private final BooleanProperty optionPreferFullTaxa = new SimpleBooleanProperty(true);

    public List<String> listOptions() {
        return Arrays.asList("BestTaxonSubset", "FilterTaxa", "PreferFullTaxa");
    }

    @Override
    public String getToolTip(String optionName) {
        switch (optionName) {
            case "BestTaxonSubset":
                return "Determine the best taxon subset contained in any tree.\n" +
                        "It maximizes size of taxon set times number of trees that contain the taxa.\n" +
                        "Subtrees are then induced on that taxon set.";
            case "FilterTaxa":
                return "Use the best taxon subset to filter all taxa";
            case "PreferFullTaxa":
                return "Prefer full taxon set: this ensure that running the filter multiple times doesn't choose smaller and smaller taxon sets";
            default:
                return optionName;
        }
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, TreesBlock parent, TreesBlock child) throws CanceledException {
        if (!isActive()) {
            child.getTrees().addAll(parent.getTrees());
            child.setPartial(parent.isPartial());
        } else {
            final Map<BitSet, BitSet> taxa2trees = new HashMap<>();

            progress.setMaximum(parent.getNTrees());
            progress.setProgress(0);
            for (int t = 1; t <= parent.getNTrees(); t++) {
                final PhyloTree tree = parent.getTree(t);
                final BitSet taxa = TreesUtilities.getTaxa(tree);

                if (taxa2trees.get(taxa) == null)
                    taxa2trees.put(taxa, new BitSet());
                taxa2trees.get(taxa).set(t);
                progress.incrementProgress();
            }
            // sort taxa sets by decreasing size:
            final SortedSet<BitSet> sorted = new TreeSet<>(BitSetUtils.getComparatorByDecreasingCardinality());
            sorted.addAll(taxa2trees.keySet());

            progress.setMaximum(sorted.size());
            progress.setProgress(0);

            for (BitSet taxa : sorted) {
                for (BitSet other : sorted) {
                    if (other == taxa)
                        break;
                    if (BitSetUtils.contains(other, taxa)) {
                        for (Integer t : BitSetUtils.members(taxa2trees.get(other))) {
                            taxa2trees.get(taxa).set(t);
                        }
                    }
                }
                progress.incrementProgress();
            }

            // find the best bit set:
            progress.setMaximum(sorted.size());
            progress.setProgress(0);

            BitSet best = null;
            int bestScore = 0;
            for (BitSet taxa : sorted) {
                final int score = taxa.cardinality() * taxa2trees.get(taxa).cardinality() * (isOptionPreferFullTaxa() && taxa.equals(taxaBlock.getTaxaSet()) ? 10 : 1); // 10: prefer to use all taxa, if possible
                if (score > bestScore) {
                    bestScore = score;
                    best = taxa;
                }
                progress.incrementProgress();
            }

            if (best != null) {
                NotificationManager.showInformation("Best taxon set size:  " + best.cardinality() + "\nBest number of trees: " + taxa2trees.get(best).cardinality());
                //System.err.println("Best taxon set size:  "+best.cardinality());
                //System.err.println("Best number of trees: "+taxa2trees.get(best).cardinality());

                {
                    for (Integer t : BitSetUtils.members(taxa2trees.get(best))) {
                        if (!BitSetUtils.contains(TreesUtilities.getTaxa(parent.getTree(t)), best))
                            System.err.println("Incorrect tree: " + t);
                    }
                }

                progress.setMaximum(taxa2trees.get(best).cardinality());
                progress.setProgress(0);

                for (Integer t : BitSetUtils.members(taxa2trees.get(best))) {
                    PhyloTree tree = parent.getTree(t);
                    final BitSet taxa = TreesUtilities.getTaxa(tree);
                    if (taxa.equals(best))
                        child.getTrees().add(tree);
                    else // need to compute induced tree
                    {
                        final PhyloTree induced = TreesUtilities.computeInducedTree(BitSetUtils.asArrayWith0s(BitSetUtils.max(taxa), best), tree);
                        if (induced != null) {
                            if (TreesUtilities.getTaxa(induced).equals(best))
                                child.getTrees().add(induced);
                            else
                                System.err.println("Warning: induced tree has wrong number of taxa: " + TreesUtilities.getTaxa(induced).cardinality());
                        }
                    }
                    progress.incrementProgress();
                }

                final Workflow workflow = taxaBlock.getDocument().getWorkflow();

                if (isOptionFilterTaxa() && best.cardinality() < workflow.getTopTaxaNode().getDataBlock().getNtax()) {
                    TaxaFilter taxaFilter = (TaxaFilter) workflow.getTaxaFilter().getAlgorithm();
                    taxaFilter.getOptionEnabledTaxa().clear();
                    taxaFilter.getOptionDisabledTaxa().clear();
                    final TaxaBlock topTaxaBlock = workflow.getTopTaxaNode().getDataBlock();
                    for (int t = 1; t <= topTaxaBlock.getNtax(); t++) {
                        final int index = taxaBlock.indexOf(topTaxaBlock.get(t));
                        if (index > 0 && best.get(index))
                            taxaFilter.getOptionEnabledTaxa().add(topTaxaBlock.get(t));
                        else
                            taxaFilter.getOptionDisabledTaxa().add(topTaxaBlock.get(t));
                    }
                    setOptionFilterTaxa(false);
                    workflow.getTaxaFilter().forceRecompute();
                    throw new CanceledException("Restart: to update taxa filter");
                }

                System.err.println("best: " + best.cardinality());
                System.err.println("taxa: " + taxaBlock.getNtax());

                child.setPartial(best.cardinality() < taxaBlock.getNtax());

            }
        }

        child.setRooted(parent.isRooted());

        if (child.getNTrees() == parent.getNTrees())
            setShortDescription("using all " + parent.size() + " trees");
        else
            setShortDescription("using " + child.size() + " of " + parent.size() + " trees");
    }

    @Override
    public void clear() {
    }

    @Override
    public boolean isActive() {
        return isOptionBestTaxonSubset();
    }

    public boolean isOptionBestTaxonSubset() {
        return optionBestTaxonSubset.get();
    }

    public BooleanProperty optionBestTaxonSubsetProperty() {
        return optionBestTaxonSubset;
    }

    public void setOptionBestTaxonSubset(boolean optionBestTaxonSubset) {
        this.optionBestTaxonSubset.set(optionBestTaxonSubset);
    }

    public boolean isOptionFilterTaxa() {
        return optionFilterTaxa.get();
    }

    public BooleanProperty optionFilterTaxaProperty() {
        return optionFilterTaxa;
    }

    public void setOptionFilterTaxa(boolean optionFilterTaxa) {
        this.optionFilterTaxa.set(optionFilterTaxa);
    }

    public boolean isOptionPreferFullTaxa() {
        return optionPreferFullTaxa.get();
    }

    public BooleanProperty optionPreferFullTaxaProperty() {
        return optionPreferFullTaxa;
    }

    public void setOptionPreferFullTaxa(boolean optionPreferFullTaxa) {
        this.optionPreferFullTaxa.set(optionPreferFullTaxa);
    }
}
