/*
 *  NeighborNetSplitsLP.java Copyright (C) 2022 Daniel H. Huson
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

package splitstree5.core.algorithms.distances2splits;

import jloda.thirdparty.LinearProgramming;
import jloda.util.progress.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.distances2splits.neighbornet.NeighborNetCycle;
import splitstree5.core.algorithms.interfaces.IFromDistances;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Compatibility;
import splitstree5.utils.SplitsUtilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;

/**
 * Given a circular ordering and a distance matrix, computes the splits weights using linear programming
 * <p>
 * Daniel Huson, 2.2020
 */
public class NeighborNetSplitsLP extends Algorithm<DistancesBlock, SplitsBlock> implements IFromDistances, IToSplits {

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, DistancesBlock distancesBlock, SplitsBlock splitsBlock) throws InterruptedException, IOException {
        if (SplitsUtilities.computeSplitsForLessThan4Taxa(taxaBlock, distancesBlock, splitsBlock))
            return;

        progress.setMaximum(-1);

        final var cycle = NeighborNetCycle.compute(progress, distancesBlock.size(), distancesBlock.getDistances());
        var splits = new ArrayList<>(compute(taxaBlock.getNtax(), cycle, distancesBlock.getDistances(), 0.0001, progress));

        if (Compatibility.isCompatible(splits))
            splitsBlock.setCompatibility(Compatibility.compatible);
        else
            splitsBlock.setCompatibility(Compatibility.cyclic);
        splitsBlock.setCycle(cycle);
        splitsBlock.setFit(SplitsUtilities.computeLeastSquaresFit(distancesBlock, splits));

        splitsBlock.getSplits().addAll(splits);

    }

    /**
     * Compute splits and weights using linear programming
     *
     * @param nTax      number of taxa
     * @param cycle     taxon cycle, 1-based
     * @param distances pairwise distances, 0-based
     * @param cutoff    min split weight
     * @param progress  progress listener
     * @return weighted splits
     */
    public ArrayList<ASplit> compute(int nTax, int[] cycle, double[][] distances, double cutoff, ProgressListener progress) throws IOException {
        //Handle n=1,2 separately.
        if (nTax == 1) {
            return new ArrayList<>();
        }
        if (nTax == 2) {
            var splits = new ArrayList<ASplit>();
            var d_ij = (float) distances[cycle[1] - 1][cycle[2] - 1];
            if (d_ij > 0.0) {
                var A = new BitSet();
                A.set(cycle[1]);
                splits.add(new ASplit(A, 2, d_ij));
            }
            return splits;
        }

        final var sideBs = computeAllCircularSideB(nTax, cycle);
        final var nSplits = sideBs.size();
        final var nPairs = (nTax * (nTax - 1)) / 2;

        System.err.println("LP: " + nPairs + " rows, " + nSplits + " cols");

        progress.setSubtask("Setting up LP");
        progress.setMaximum(nPairs);
        progress.setProgress(0);

        final var A = new double[nPairs][nSplits];
        final var b = new double[nPairs];
        final var c = new double[nSplits]; // 0-based

        {
            var numConstraints = 0;
            for (var i = 0; i < nTax; i++) {
                for (var j = i + 1; j < nTax; j++) {
                    final var row = new double[nSplits];
                    for (var s = 0; s < nSplits; s++) {
                        var sideB = sideBs.get(s);
                        if (sideB.get(i + 1) != sideB.get(j + 1)) {
                            c[s]++;
                            row[s] = 1;
                        } else
                            row[s] = 0;
                    }
                    A[numConstraints] = row;
                    b[numConstraints] = distances[i][j];
                    numConstraints++;
                    progress.incrementProgress();
                }
            }
        }

        progress.setSubtask("Running LP");
        progress.setMaximum(-1);

        var linearProgram = new LinearProgramming(A, b, c);

        final var weights = linearProgram.primal();

        final var splits = new ArrayList<ASplit>();

        for (var s = 0; s < nSplits; s++) {
            if (weights[s] >= cutoff) {
                splits.add(new ASplit(sideBs.get(s), nTax, weights[s]));
            }
        }
        return splits;
    }

    static private ArrayList<BitSet> computeAllCircularSideB(int nTax, int[] cycle) {
        final ArrayList<BitSet> sideBs = new ArrayList<>();

        for (var p = 2; p < cycle.length; p++)
            for (var q = p; q < cycle.length; q++) {
                final var sideB = new BitSet();
                for (var i = p; i <= q; i++) {
                    sideB.set(cycle[i]);
                }
                sideBs.add(sideB);
            }
        return sideBs;
    }
}

