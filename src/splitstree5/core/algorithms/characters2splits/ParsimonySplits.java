package splitstree5.core.algorithms.characters2splits;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Compatibility;
import splitstree5.utils.SplitsUtilities;

import java.util.ArrayList;
import java.util.BitSet;

/**
 * p-splits method
 * Daniel Huson, 2003
 */
public class ParsimonySplits extends Algorithm<CharactersBlock, SplitsBlock> implements IFromChararacters, IToSplits {
    private boolean optionGapsAsMissing = true;

    @Override
    public String getCitation() {
        return "Bandelt and Dress 1992; H.-J.Bandelt and A.W.M.Dress. A canonical decomposition theory for metrics on a finite set. Advances in Mathematics, 92:47–105, 1992.";
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock chars, SplitsBlock splitsBlock) throws Exception {
        ArrayList<ASplit> previousSplits = new ArrayList<>(); // list of previously computed splits
        ArrayList<ASplit> currentSplits = new ArrayList<>(); // current list of splits
        BitSet taxaPrevious = new BitSet(); // taxa already processed

        progress.setMaximum(taxaBlock.getNtax());
        progress.setProgress(0);

        for (int t = 1; t <= taxaBlock.getNtax(); t++) {
            // initally, just add 1 to set of previous taxa
            if (t == 1) {
                taxaPrevious.set(t);
                continue;
            }

            // Does t vs previous set of taxa form a split?
            BitSet At = new BitSet();
            At.set(t);

            int wgt = pIndex(optionGapsAsMissing, t, At, chars);
            if (wgt > 0) {
                currentSplits.add(new ASplit(At, t, wgt));
            }

            // consider all previously computed splits:
            for (ASplit prevSplit : previousSplits) {
                final BitSet A = prevSplit.getA();
                final BitSet B = prevSplit.getB();
                // is Au{t} vs B a split?
                A.set(t);
                wgt = Math.min((int) prevSplit.getWeight(), pIndex(optionGapsAsMissing, t, A, chars));
                if (wgt > 0) {
                    currentSplits.add(new ASplit(A, t, wgt));
                }
                A.set(t, false);

                // is A vs Bu{t} a split?
                B.set(t);
                wgt = Math.min((int) prevSplit.getWeight(), pIndex(optionGapsAsMissing, t, B, chars));
                if (wgt > 0)
                    currentSplits.add(new ASplit(B, t, wgt));
                B.set(t, false);
            }

            // swap lists:
            {
                final ArrayList<ASplit> tmp = previousSplits;
                previousSplits = currentSplits;
                currentSplits = tmp;
                currentSplits.clear();
            }

            taxaPrevious.set(t);
            progress.incrementProgress();
        }

        splitsBlock.getSplits().addAll(previousSplits);
        splitsBlock.setCompatibility(Compatibility.compute(taxaBlock.getNtax(), splitsBlock.getSplits()));
        splitsBlock.setCycle(SplitsUtilities.computeCycle(taxaBlock.getNtax(), previousSplits));
    }


    /**
     * Computes the p-index of a split:
     *
     * @param gapmissingmode
     * @param t
     * @param A
     * @param characters
     * @return
     */
    private int pIndex(boolean gapmissingmode, int t, BitSet A, CharactersBlock characters) {
        int value = Integer.MAX_VALUE;
        int a1, a2, b1, b2;

        a1 = t;
        if (!A.get(a1))
            System.err.println("pIndex(): a1=" + a1 + " not in A");

        for (a2 = 1; a2 <= t; a2++) {
            if (A.get(a2))
                for (b1 = 1; b1 <= t; b1++) {
                    if (!A.get(b1))
                        for (b2 = b1; b2 <= t; b2++) {
                            if (!A.get(b2)) {
                                int val_a1a2b1b2 =
                                        pScore(gapmissingmode, a1, a2, b1, b2, characters);
                                if (val_a1a2b1b2 != 0)
                                    value = Math.min(value, val_a1a2b1b2);
                                else
                                    return 0;
                            }
                        }
                }
        }
        return value;
    }

    /**
     * Computes the parsimony-score for the four given taxa:
     *
     * @param gapMissingMode
     * @param a1
     * @param a2
     * @param b1
     * @param b2
     * @param characters
     * @return
     */
    private int pScore(boolean gapMissingMode, int a1, int a2, int b1, int b2, CharactersBlock characters) {
        final char missingChar = characters.getMissingCharacter();
        final char gapChar = characters.getGapCharacter();
        final int nchar = characters.getNchar();
        final char[] row_a1 = characters.getRow1(a1);
        final char[] row_a2 = characters.getRow1(a2);
        final char[] row_b1 = characters.getRow1(b1);
        final char[] row_b2 = characters.getRow1(b2);

        int a1a2_b1b2 = 0, a1b1_a2b2 = 0, a1b2_a2b1 = 0;
        for (int i = 1; i <= nchar; i++) {
            char c_a1 = row_a1[i];
            char c_a2 = row_a2[i];
            char c_b1 = row_b1[i];
            char c_b2 = row_b2[i];

            if (c_a1 == missingChar || c_a2 == missingChar || c_b1 == missingChar || c_b2 == missingChar)
                continue;
            if (gapMissingMode && (c_a1 == gapChar || c_a2 == gapChar || c_b1 == gapChar || c_b2 == gapChar))
                continue;
            if (c_a1 == c_a2 && c_b1 == c_b2)
                a1a2_b1b2++;
            if (c_a1 == c_b1 && c_a2 == c_b2)
                a1b1_a2b2++;
            if (c_a1 == c_b2 && c_a2 == c_b1)
                a1b2_a2b1++;
        }
        int min_val = Math.min(a1b1_a2b2, a1b2_a2b1);
        if (a1a2_b1b2 > min_val)
            return a1a2_b1b2 - min_val;
        else
            return 0;
    }

    public boolean isOptionGapsAsMissing() {
        return optionGapsAsMissing;
    }

    public void setOptionGapsAsMissing(boolean optionGapsAsMissing) {
        this.optionGapsAsMissing = optionGapsAsMissing;
    }
}
