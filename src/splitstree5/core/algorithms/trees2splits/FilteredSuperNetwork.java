/*
 *  Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.core.algorithms.trees2splits;

import jloda.phylo.PhyloTree;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Distortion;
import splitstree5.utils.TreesUtilities;

import java.util.BitSet;

/**
 * filtered super network
 * Daniel Huson, 2006, 3.2018
 */
public class FilteredSuperNetwork extends SuperNetwork implements IFromTrees, IToSplits {
    private int optionMinNumberTrees = 1;
    private int optionMaxDistortionScore = 0;
    private boolean optionAllTrivial = true;
    private boolean optionUseTotalScore = false;


    @Override
    public String getCitation() {
        return "Whitfield et al.;James B. Whitfield, Sydney A. Cameron, Daniel H. Huson, Mike A. Steel. " +
                "Filtered Z-Closure Supernetworks for Extracting and Visualizing Recurrent Signal from Incongruent Gene Trees, " +
                "Systematic Biology, Volume 57, Issue 6, 1 December 2008, Pages 939–947.";
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, TreesBlock trees, SplitsBlock child) throws Exception {
        // first compute splits using Z-closure method:
        SplitsBlock splits = new SplitsBlock();
        super.compute(progress, taxaBlock, trees, splits);

        progress.setSubtask("Processing trees");
        progress.setMaximum(splits.getNsplits());

        final BitSet[] tree2taxa = new BitSet[trees.getNTrees() + 1];
        for (int t = 1; t <= trees.getNTrees(); t++) {
            tree2taxa[t] = TreesUtilities.getTaxa(trees.getTree(t));
            //System.err.println("number of taxa in tree " + t + ":" + tree2taxa[t].cardinality());
            progress.setProgress(t);
        }

        progress.setSubtask("Processing splits");
        progress.setMaximum(splits.getNsplits() * trees.getNTrees());
        progress.setProgress(0);

        System.err.println("Filtering splits:");
        if (getOptionUseTotalScore()) {
            for (int s = 1; s <= splits.getNsplits(); s++) {
                int totalScore = 0;
                BitSet A = splits.get(s).getA();
                BitSet B = splits.get(s).getB();
                for (int t = 1; t <= trees.getNTrees(); t++) {
                    final BitSet treeTaxa = tree2taxa[t];
                    final BitSet treeTaxaAndA = (BitSet) (treeTaxa.clone());
                    treeTaxaAndA.and(A);
                    final BitSet treeTaxaAndB = (BitSet) (treeTaxa.clone());
                    treeTaxaAndB.and(B);

                    if (treeTaxaAndA.cardinality() > 1 && treeTaxaAndB.cardinality() > 1) {
                        final PhyloTree tree = trees.getTree(t);
                            totalScore += Distortion.computeDistortionForSplit(tree, A, B);
                    }
                    progress.incrementProgress();
                }
                if (totalScore <= getOptionMaxDistortionScore()) {
                    final ASplit aSplit = splits.get(s);
                    child.getSplits().add(new ASplit(aSplit.getA(), aSplit.getB(), aSplit.getWeight()));
                }
            }
        } else // do not use total score
        {
            for (int s = 1; s <= splits.getNsplits(); s++) {
                //System.err.print("s " + s + ":");
                final BitSet A = splits.get(s).getA();
                final BitSet B = splits.get(s).getB();
                int count = 0;
                for (int t = 1; t <= trees.getNTrees(); t++) {
                    BitSet treeTaxa = tree2taxa[t];
                    BitSet treeTaxaAndA = (BitSet) (treeTaxa.clone());
                    treeTaxaAndA.and(A);
                    BitSet treeTaxaAndB = (BitSet) (treeTaxa.clone());
                    treeTaxaAndB.and(B);

                    if (treeTaxaAndA.cardinality() > 1 && treeTaxaAndB.cardinality() > 1) {
                        final PhyloTree tree = trees.getTree(t);
                            int score = Distortion.computeDistortionForSplit(tree, A, B);
                            //System.err.print(" " + score);
                            if (score <= getOptionMaxDistortionScore())
                                count++;
                            if (count + (trees.getNTrees() - t) < getOptionMinNumberTrees())
                                break; // no hope to get above threshold
                    } else if ((A.cardinality() == 1 || B.cardinality() == 1)
                            && treeTaxaAndB.cardinality() > 0 && treeTaxaAndB.cardinality() > 0) {
                        count++; // is confirmed split
                        //System.err.print(" +");
                    } else {
                        //System.err.print(" .");
                    }
                    progress.incrementProgress();
                }
                //System.err.println(" sum=" + count);
                if ((getOptionAllTrivial() && (A.cardinality() == 1 || B.cardinality() == 1))
                        || count >= getOptionMinNumberTrees()) {
                    final ASplit aSplit = splits.get(s);
                    child.getSplits().add(new ASplit(aSplit.getA(), aSplit.getB(), aSplit.getWeight(), (float) count / (float) trees.getNTrees()));
                }
            }
        }
        System.err.println("Splits: " + splits.getNsplits() + " -> " + child.getNsplits());
    }

    /**
     * * gets the threshold (value between 0 and 1)
     * * @return the threshold
     */
    public int getOptionMinNumberTrees() {
        return optionMinNumberTrees;
    }

    /**
     * sets the mininum number of trees for which a split but have a good enough score
     *
     * @param optionMinNumberTrees
     */
    public void setOptionMinNumberTrees(int optionMinNumberTrees) {
        this.optionMinNumberTrees = Math.max(1, optionMinNumberTrees);
    }

    public int getOptionMaxDistortionScore() {
        return optionMaxDistortionScore;
    }

    /**
     * set the max homoplasy score that we will allows per tree
     *
     * @param optionMaxDistortionScore
     */
    public void setOptionMaxDistortionScore(int optionMaxDistortionScore) {
        this.optionMaxDistortionScore = Math.max(0, optionMaxDistortionScore);
    }

    public boolean getOptionAllTrivial() {
        return optionAllTrivial;
    }

    public void setOptionAllTrivial(boolean optionAllTrivial) {
        this.optionAllTrivial = optionAllTrivial;
    }

    public boolean getOptionUseTotalScore() {
        return optionUseTotalScore;
    }

    public void setOptionUseTotalScore(boolean optionUseTotalScore) {
        this.optionUseTotalScore = optionUseTotalScore;
    }
}
