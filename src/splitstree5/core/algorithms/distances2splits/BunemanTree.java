package splitstree5.core.algorithms.distances2splits;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromDistances;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Compatibility;
import splitstree5.utils.SplitsUtilities;

import java.util.ArrayList;
import java.util.BitSet;

/**
 * Implements the buneman tree
 * <p>
 * Created on 2007-09-11
 *
 * @author Daniel Huson, David Bryant, Tobias Kloepper and Daria Evseeva
 */

public class BunemanTree extends Algorithm<DistancesBlock, SplitsBlock> implements IFromDistances, IToSplits {

    public final static String DESCRIPTION = "Computes the Buneman tree (Buneman 1971)";

    @Override
    public String getCitation() {
        return "Bandelt and Dress 1992; " +
                "H.-J. Bandelt and A.W.M.Dress. A canonical decomposition theory for metrics on a finite set. " +
                "Advances in Mathematics, 92:47–105, 1992.";
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, DistancesBlock distancesBlock, SplitsBlock splitsBlock) throws Exception {
        ArrayList<ASplit> previousSplits = new ArrayList<>(); // list of previously computed splits
        ArrayList<ASplit> nextSplits; // current list of splits

        // ProgressDialog pd = new ProgressDialog("Split Decomposition...",""); //Set new progress bar.
        // doc.setProgressListener(pd);
        progress.setMaximum(taxaBlock.getNtax());    //initialize maximum progress
        progress.setProgress(0);

        final BitSet previousTaxa = new BitSet(); // taxa already processed
        final int ntax = taxaBlock.getNtax();

        previousTaxa.set(1);

        for (int t = 2; t <= ntax; t++) {
            nextSplits = new ArrayList<>(t); // restart current list of splits

            // Does t vs previous set of taxa form a split?
            {
                final BitSet At = new BitSet();
                At.set(t);
                final float wgt = getIsolationIndex(t, At, previousTaxa, distancesBlock);
                if (wgt > 0) {
                    nextSplits.add(new ASplit((BitSet) At.clone(), t, wgt));
                }
            }

            // consider all previously computed splits:
            for (final ASplit previousSplit : previousSplits) {
                final BitSet A = previousSplit.getA();
                final BitSet B = getComplement(previousSplit.getA(), t - 1);

                // is Au{t} vs B a split?
                {
                    A.set(t);
                    final double wgt = Math.min(previousSplit.getWeight(), getIsolationIndex(t, A, B, distancesBlock));
                    if (wgt > 0) {
                        nextSplits.add(new ASplit((BitSet) A.clone(), t, wgt));

                    }
                    A.set(t, false);
                }

                // is A vs Bu{t} a split?
                {
                    B.set(t);
                    final double wgt = Math.min(previousSplit.getWeight(), getIsolationIndex(t, B, A, distancesBlock));
                    if (wgt > 0) {
                        nextSplits.add(new ASplit((BitSet) B.clone(), t, wgt));
                    }
                }
            }
            previousSplits = nextSplits;

            previousTaxa.set(t);

            progress.incrementProgress();
        }

        // copy splits to splits
        //splitsBlock.setFit(computeFit(distancesBlock, previousSplits));
        splitsBlock.getSplits().addAll(previousSplits);
        splitsBlock.setCompatibility(Compatibility.compute(taxaBlock.getNtax(), splitsBlock.getSplits()));
        splitsBlock.setCycle(SplitsUtilities.computeCycle(taxaBlock.getNtax(), previousSplits));
    }


    /**
     * Returns the isolation index for Au{x} vs B
     *
     * @param t maximal taxon index, assumed to be contained in set A
     * @param A set A
     * @param B set B
     * @param d Distance matrix
     * @return the isolation index
     */
    public static float getIsolationIndex(int t, BitSet A, BitSet B, DistancesBlock d) {
        float min_val = Float.MAX_VALUE;

        for (int i = 1; i <= t; i++) {
            if (A.get(i)) {
                for (int j = 1; j <= t; j++)
                    if (B.get(j)) {
                        for (int k = j; k <= t; k++) {
                            if (B.get(k)) {
                                float val = getIsolationIndex(t, i, j, k, d);
                                if (val < min_val) {
                                    if (val <= 0.0000001)
                                        return 0;
                                    min_val = val;
                                }
                            }
                        }
                    }
            }
        }
        return min_val;
    }

    /**
     * Returns the isolation index of i,j vs k,l
     *
     * @param i a taxon
     * @param j a taxon
     * @param k a taxon
     * @param m a taxon
     * @param d Distance matrix
     * @return the isolation index
     */
    public static float getIsolationIndex(int i, int j, int k, int m, DistancesBlock d) {
        return (float)
                (0.5 * (Math.min(d.get(i, k) + d.get(j, m), d.get(i, m) + d.get(j, k))
                        - d.get(i, j) - d.get(k, m)));
    }

    private static BitSet getComplement(BitSet A, int ntax) {
        BitSet result = new BitSet();
        for (int t = A.nextClearBit(1); t != -1 && t <= ntax; t = A.nextClearBit(t + 1))
            result.set(t);
        return result;
    }

    // GETTER AND SETTER
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, DistancesBlock parent) {
        return parent.getNtax() >= 4;
    }
}
