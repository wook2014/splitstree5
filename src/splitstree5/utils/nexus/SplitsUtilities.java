package splitstree5.utils.nexus;

import jloda.util.ProgressListener;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ASplit;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Tools for analyzing a splits block
 * Daniel Huson, 2005
 * Created by Daria on 23.01.2017.
 */
public class SplitsUtilities {

    /**
     * verify that all splits are proper and are contained in the taxon set
     *
     * @param splits
     * @param taxa
     * @throws SplitsException
     */
    public static void verifySplits(SplitsBlock splits, TaxaBlock taxa) throws SplitsException {
        final Set<BitSet> seen = new HashSet<>();

        for (ASplit split : splits.getSplits()) {
            final BitSet aSet = split.getA();
            if (seen.contains(aSet))
                throw new SplitsException("Split " + aSet + " occurs multiple times");
            if (aSet.cardinality() == 0)
                throw new SplitsException("Split " + aSet + " not proper, size is 0");
            if (aSet.cardinality() == taxa.getNtax())
                throw new SplitsException("Split " + aSet + " not proper, size is ntax");
            if (aSet.nextSetBit(0) == 0 || aSet.nextSetBit(taxa.getNtax() + 1) != -1)
                throw new SplitsException("Split " + aSet + " not contained in taxa set <" + taxa.getTaxaSet() + ">");
            seen.add(aSet);
        }
    }

    /**
     * Determines the fit of a splits system, ie how well it
     * represents a given distance matrix, in percent. Computes two different values.
     * //ToDo: Fix variances.
     * // todo no lsfit?
     *
     * @param forceRecalculation always recompute the fit, even if there is a valid value stored.
     * @param splits             the splits
     * @param dist               the distances
     */
    static public void computeFits(boolean forceRecalculation, SplitsBlock splits, DistancesBlock dist, ProgressListener pl) {
        float dsum = 0;
        float ssum = 0;
        float dsumSquare = 0;
        float ssumSquare = 0;
        float netsumSquare = 0;

        int ntax = dist.getNtax();

        if (splits == null || dist == null)
            return;

        if (!forceRecalculation && splits.getFit() >= 0)
            return; //No need to recalculate.

        splits.setFit(-1);
        //splits.getProperties().setLSFit(-1); //A fit of -1 means that we don't have a valid value.

        pl.setSubtask("Recomputing fit");

        double[][] sdist = new double[ntax + 1][ntax + 1];

        for (int i = 1; i <= ntax; i++) {
            sdist[i][i] = 0;
            for (int j = i + 1; j <= ntax; j++) {
                float dij = 0;
                for (int s = 1; s <= splits.getNsplits(); s++) {
                    BitSet split = splits.getSplits().get(s - 1).getA();
                    if (split.get(i) != split.get(j))
                        dij += splits.getSplits().get(s - 1).getWeight();
                }
                sdist[i][j] = sdist[j][i] = dij;
            }
        }
        for (int i = 1; i <= ntax; i++) {
            for (int j = i + 1; j <= ntax; j++) {
                double sij = sdist[i][j];
                double dij = dist.get(i, j);
                double x = Math.abs(sij - dij);
                ssum += x;
                ssumSquare += x * x;
                dsum += dij;
                dsumSquare += dij * dij;
                netsumSquare += sij * sij;
            }
        }
        float fit = 100 * (1 - ssum / dsum);
        fit = Math.max(fit, 0);
        splits.setFit(fit);

        double lsfit = 100.0 * (1.0 - ssumSquare / dsumSquare);


        lsfit = Math.max(lsfit, 0.0);
        //splits.getProperties().setLSFit(lsfit);

        double stress = Math.sqrt(ssumSquare / netsumSquare);

        System.err.println("\nRecomputed fit:\n\tfit = " + fit + "\n\tLS fit =" + lsfit + "\n\tstress =" + stress + "\n");
    }
}
