package splitstree5.core.algorithms.distances2splits;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromDistances;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ASplit;

import java.util.ArrayList;
import java.util.BitSet;

public class BunemanTree extends Algorithm<DistancesBlock, SplitsBlock> implements IFromDistances, IToSplits {

    public final static String CITATION ="P. Buneman.  The recovery of trees from measures of dissimilarity.c"+
            "In F. R. Hodson, D. G. Kendall,  and  P.  Tautu,  editors, " +
            "Mathematics  in  the  Archaeological  and  Historical  Sciences, pages 387–395. Edinburgh University Press, 1971.";

    public final static String DESCRIPTION = "Computes the Buneman tree (Buneman 1971)";

    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, DistancesBlock distancesBlock, SplitsBlock splitsBlock)
            throws Exception {

        splitsBlock.getSplits().clear();

        ArrayList<ASplit> previousSplits = new ArrayList<>(); // list of previously computed splits
        ArrayList<ASplit> nextSplits; // current list of splits

        // ProgressDialog pd = new ProgressDialog("Split Decomposition...",""); //Set new progress bar.
        // doc.setProgressListener(pd);
        progressListener.setMaximum(taxaBlock.getNtax());    //initialize maximum progress
        progressListener.setProgress(0);

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

            progressListener.setProgress(t);
        }


        // copy splits to splits
        //splitsBlock.setFit(computeFit(distancesBlock, previousSplits));
        splitsBlock.getSplits().addAll(previousSplits);

        progressListener.setProgress(ntax);   //set progress to 100%
        progressListener.close();

        /*ASplit previous = new ASplit(new BitSet(), distancesBlock.getNtax());
        ASplit current;
        BitSet taxa_prev = new BitSet(); // taxa already processed


        progressListener.setTasks("Buneman tree","Init.");
        progressListener.setMaximum(taxaBlock.getNtax());

        for (int t = 1; t <= distancesBlock.getNtax(); t++) {
            // initally, just add 1 to set of previous taxa
            if (t == 1) {
                taxa_prev.set(t);
                continue;
            }

            current = new ASplit(new BitSet(), t); // restart current list of splits

            // Does t vs previous set of taxa form a split?
            BitSet At = new BitSet(); // taxa set
            At.set(t);

            float wgt = getIsolationIndex(t, At, taxa_prev, distancesBlock);
            if (wgt > 0) {
                current.add((TaxaSet) (At.clone()), wgt);
            }

            // consider all previously computed splits:
            for (int s = 1; s <= previous.size(); getNsplits(); s++) {
                BitSet A = previous.get(s);
                BitSet B = A.getComplement(t - 1);

                // is Au{t} vs B a split?
                A.set(t);
                wgt = Math.min(previous.getWeight(s), getIsolationIndex(t, A, B, d));
                if (wgt > 0) {
                    current.add((TaxaSet) (A.clone()), wgt);
                }
                A.unset(t);

                // is A vs Bu{t} a split?
                B.set(t);
                wgt = Math.min(previous.getWeight(s), getIsolationIndex(t, B, A, d));
                if (wgt > 0) {
                    current.add((TaxaSet) (B.clone()), wgt);
                }
            }
            previous = current;
            taxa_prev.set(t);
            doc.notifySetProgress(t);
        }

        // copy splits to splits
        Splits splits = new Splits(taxa.getNtax());
        splits.addSplitsSet(previous);
        splits.getProperties().setCompatibility(Splits.Properties.COMPATIBLE);
        //System.err.println(" "+splits.splits.getNsplits());
        doc.notifySetProgress(taxa.getNtax());   //set progress to 100%
        return splits;*/

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
}
