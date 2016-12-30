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

package splitstree5.core.algorithms;

import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ASplit;

import java.util.ArrayList;
import java.util.BitSet;

/**
 * split decomposition
 * Created by huson on 12/30/16.
 */
public class SplitDecomposition extends Algorithm<DistancesBlock, SplitsBlock> {
    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, DistancesBlock distancesBlock, SplitsBlock splitsBlock) throws InterruptedException, CanceledException {
        splitsBlock.getSplits().clear();

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
                final BitSet B = previousSplit.getComplement(t - 1);

                // is Au{t} vs B a split?
                {
                    A.set(t);
                    final float wgt = Math.min(previousSplit.getWeight(), getIsolationIndex(t, A, B, distancesBlock));
                    if (wgt > 0) {
                        nextSplits.add(new ASplit((BitSet) A.clone(), t, wgt));

                    }
                    A.set(t, false);
                }

                // is A vs Bu{t} a split?
                {
                    B.set(t);
                    final float wgt = Math.min(previousSplit.getWeight(), getIsolationIndex(t, B, A, distancesBlock));
                    if (wgt > 0) {
                        nextSplits.add(new ASplit((BitSet) B.clone(), t, wgt));
                    }
                }
            }
            previousSplits = nextSplits;

            previousTaxa.set(t);

            progress.setProgress(t);
        }


        // copy splits to splits
        splitsBlock.getSplits().addAll(previousSplits);
        //System.err.println(" "+splits.splits.getNsplits());
        float[] fit = computeFit(distancesBlock, splitsBlock);
        splitsBlock.setFit(fit[0]);
        splitsBlock.setLeastSquares(false);
        splitsBlock.setLeastSquaresFit(fit[1]);

        progress.setProgress(ntax);   //set progress to 100%
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
                for (int j = 1; j <= t; j++) {
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
        return (float) (0.5 * (Math.max(d.get(i, k) + d.get(j, m), d.get(i, m) + d.get(j, k)) - d.get(i, j) - d.get(k, m)));
    }

    /**
     * computes the fit, ls fit and stress
     *
     * @param distancesBlock
     * @param splitsBlock
     * @return fit, ls fit and stress
     */
    public static float[] computeFit(DistancesBlock distancesBlock, SplitsBlock splitsBlock) {
        double dsum = 0;
        double ssum = 0;
        double dsumSquare = 0;
        double ssumSquare = 0;
        double netsumSquare = 0;

        final int ntax = distancesBlock.getNtax();
        final double[][] sdist = new double[ntax + 1][ntax + 1];

        for (int i = 1; i <= ntax; i++) {
            for (int j = i + 1; j <= ntax; j++) {
                double dij = 0.0;
                for (ASplit split : splitsBlock.getSplits()) {
                    if (split.isContainedInA(i) != split.isContainedInA(j))
                        dij += split.getWeight();
                }
                sdist[i][j] = sdist[j][i] = dij;
            }
        }
        for (int i = 1; i <= ntax; i++) {
            for (int j = i + 1; j <= ntax; j++) {
                double sij = sdist[i][j];
                double dij = distancesBlock.get(i, j);
                double x = Math.abs(sij - dij);
                ssum += x;
                ssumSquare += x * x;
                dsum += dij;
                dsumSquare += dij * dij;
                netsumSquare += sij * sij;
            }
        }
        double fit = 100 * (1.0 - ssum / dsum);
        fit = Math.max(fit, 0.0);

        double lsfit = 100.0 * (1.0 - ssumSquare / dsumSquare);


        lsfit = Math.max(lsfit, 0.0);

        double stress = Math.sqrt(ssumSquare / netsumSquare);

        System.err.println("\nRecomputed fit:\n\tfit = " + fit + "\n\tLS fit =" + lsfit + "\n\tstress =" + stress + "\n");

        return new float[]{(float) fit, (float) lsfit, (float) stress};

    }
}
