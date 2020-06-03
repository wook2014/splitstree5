/*
 * SplitsUtilities.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package splitstree5.utils;

import jloda.util.*;
import splitstree5.core.algorithms.distances2splits.NeighborNet;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Compatibility;

import java.io.PrintStream;
import java.util.*;

/**
 * utilities for splits
 * Daniel Huson, 2005, 2016
 * Daria Evseeva,23.01.2017.
 */
public class SplitsUtilities {
    /**
     * computes the least squares fit
     *
     * @param distancesBlock
     * @param splits
     * @return squares fit
     */
    public static float computeLeastSquaresFit(DistancesBlock distancesBlock, List<ASplit> splits) {
        final int ntax = distancesBlock.getNtax();
        final double[][] sdist = new double[ntax + 1][ntax + 1];

        double dsumSquare = 0;
        double ssumSquare = 0;
        for (int i = 1; i <= ntax; i++) {
            for (int j = i + 1; j <= ntax; j++) {
                double dij = 0.0;
                for (ASplit split : splits) {
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
                ssumSquare += x * x;
                dsumSquare += dij * dij;
            }
        }

        return (float) (dsumSquare > 0 ? (100.0 * (1.0 - ssumSquare / dsumSquare)) : 0);
    }


    /**
     * Computes a cycle for the given splits system
     *
     * @param ntax   number of taxa
     * @param splits the splits
     */
    static public int[] computeCycle(int ntax, List<ASplit> splits) {
        try {
            final PrintStream pso = jloda.util.Basic.hideSystemOut();
            final PrintStream pse = jloda.util.Basic.hideSystemErr();
            try {
                return NeighborNet.computeNeighborNetOrdering(ntax, splitsToDistances(ntax, splits));
            } finally {
                jloda.util.Basic.restoreSystemErr(pse);
                jloda.util.Basic.restoreSystemOut(pso);
            }
        } catch (Exception ex) {
            Basic.caught(ex);
            final int[] order = new int[ntax + 1];
            for (int t = 1; t <= ntax; t++) {
                order[t] = t;
            }
            return order;
        }
    }

    /**
     * Given splits, returns the matrix split distances, as the number of splits separating each pair of taxa
     *
     * @param ntax
     * @param splits
     * @return distance matrix indexed 0 .. nTax-1 and 0 .. nTax-1
     */
    public static double[][] splitsToDistances(int ntax, List<ASplit> splits) {
        final double[][] dist = new double[ntax][ntax];

        for (int i = 1; i <= ntax; i++) {
            for (int j = i + 1; j <= ntax; j++) {
                for (ASplit split : splits) {
                    BitSet A = split.getA();

                    if (A.get(i) != A.get(j)) {
                        dist[i - 1][j - 1]++;
                        dist[j - 1][i - 1]++;
                    }
                }
            }
        }
        return dist;
    }

    /**
     * normalize cycle so that it is lexicographically smallest
     *
     * @param cycle
     * @return normalized cycle
     */
    public static int[] normalizeCycle(int[] cycle) {
        int posOf1 = -1;
        for (int i = 1; i < cycle.length; i++) {
            if (cycle[i] == 1) {
                posOf1 = i;
                break;
            }
        }
        final int posPrev = (posOf1 == 1 ? cycle.length - 1 : posOf1 - 1);
        final int posNext = (posOf1 == cycle.length - 1 ? 1 : posOf1 + 1);
        if (cycle[posPrev] > cycle[posNext]) { // has correct orientation, ensure that taxon 1 is at first position
            if (posOf1 != 1) {
                int[] tmp = new int[cycle.length];
                int i = posOf1;
                for (int j = 1; j < tmp.length; j++) {
                    tmp[j] = cycle[i];
                    if (++i == cycle.length)
                        i = 1;
                }
                return tmp;
            } else
                return cycle;
        } else // change orientation, as well
        {
            int[] tmp = new int[cycle.length];
            int i = posOf1;
            for (int j = 1; j < tmp.length; j++) {
                tmp[j] = cycle[i];
                if (--i == 0)
                    i = cycle.length - 1;
            }
            return tmp;
        }
    }

    /**
     * sort splits by decreasing weight
     *
     * @param splits
     */
    public static ArrayList<ASplit> sortByDecreasingWeight(List<ASplit> splits) {
        final ASplit[] array = splits.toArray(new ASplit[splits.size()]);
        Arrays.sort(array, (a, b) -> {
            if (a.getWeight() > b.getWeight())
                return -1;
            else if (a.getWeight() < b.getWeight())
                return 1;
            return 0;
        });
        return new ArrayList<>(Arrays.asList(array)); // this construction ensures that the resulting list can grow
    }

    /**
     * is split circular with respect to the given cycle?
     *
     * @param taxa
     * @param cycle uses indices 1 to number-of-taxa
     * @param split
     * @return true if circular
     */
    public static boolean isCircular(TaxaBlock taxa, int[] cycle, ASplit split) {
        final BitSet part = (!split.getA().get(cycle[1]) ? split.getA() : split.getB()); // choose part that doesn't go around the horn
        int prev = 0;
        for (int t = 1; t <= taxa.getNtax(); t++) {
            if (part.get(cycle[t])) {
                if (prev != 0 && t != prev + 1)
                    return false;
                prev = t;
            }
        }
        return true;
    }

    /**
     * verify that all splits are proper and are contained in the taxon set
     *
     * @param splits
     * @param taxa
     * @throws SplitsException
     */
    public static void verifySplits(Collection<ASplit> splits, TaxaBlock taxa) throws SplitsException {
        final Set<BitSet> seen = new HashSet<>();

        for (ASplit split : splits) {
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

    public static void rotateCycle(int[] cycle, int first) {
        final int[] tmp = new int[2 * cycle.length - 1];
        System.arraycopy(cycle, 0, tmp, 0, cycle.length);
        System.arraycopy(cycle, 1, tmp, cycle.length, cycle.length - 1);
        for (int i = 1; i < tmp.length; i++) {
            if (tmp[i] == first) {
                for (int j = 1; j < cycle.length; j++) {
                    cycle[j] = tmp[i++];
                }
                return;
            }
        }
    }

    public static int getTighestSplit(BitSet taxa, SplitsBlock splitsBlock) {
        int best = 0;
        int bestSideCardinality = Integer.MAX_VALUE;
        for (int s = 1; s <= splitsBlock.getNsplits(); s++) {
            final ASplit split = splitsBlock.get(s);
            if (BitSetUtils.contains(split.getA(), taxa) && split.getA().cardinality() < bestSideCardinality) {
                best = s;
                bestSideCardinality = split.getA().cardinality();
            }
            if (BitSetUtils.contains(split.getB(), taxa) && (split.getB().cardinality() < bestSideCardinality)) {
                best = s;
                bestSideCardinality = split.getB().cardinality();
            }
        }
        return best;
    }

    public static Triplet<Integer, Double, Double> computeMidpoint(boolean useWeights, int ntaxa, SplitsBlock splitsBlock, ProgressListener progress) throws CanceledException {
        progress.setSubtask("Computing root location");

        final ArrayList<Pair<Integer, Integer>> pairs = new ArrayList<>();
        for (int a = 1; a <= ntaxa; a++) {
            for (int b = a + 1; b <= ntaxa; b++) {
                pairs.add(new Pair<>(a, b));
            }
        }

        final Single<Double> bestLength = new Single<>(0.0);
        final Single<ArrayList<ASplit>> bestPath = new Single<>(new ArrayList<>());
        final Single<Integer> start = new Single<>(0);

        progress.setMaximum(pairs.size());

        final Single<CanceledException> exception = new Single<>();

        pairs.parallelStream().forEach(pair -> {
            final int a = pair.getFirst();
            final int b = pair.getSecond();

            double remainingLength = 0;
            final ArrayList<ASplit> separators = new ArrayList<>();
            for (int s = 1; s <= splitsBlock.getNsplits(); s++) {
                final ASplit split = splitsBlock.get(s);
                if (split.isContainedInA(a) != split.isContainedInA(b)) {
                    separators.add(split);
                    remainingLength += (useWeights ? split.getWeight() : 1);
                }
            }
            separators.sort(Comparator.comparingInt(p -> p.getPartContaining(a).cardinality()));
            final ArrayList<ASplit> path = new ArrayList<>();
            final Single<Double> length = new Single<>(0.0);
            computeLongestPathRec(useWeights, separators, remainingLength, 0, new ArrayList<>(), 0, path, length);
            synchronized (bestLength) {
                if (length.get() > bestLength.get()) {
                    bestLength.set(length.get());
                    bestPath.set(path);
                    start.set(a);
                }
            }
            try {
                progress.incrementProgress();
            } catch (CanceledException e) {
                exception.setIfCurrentValueIsNull(e);
            }
        });
        if (exception.get() != null)
            throw exception.get();

        double accummulated = 0;
        for (ASplit split : bestPath.get()) {
            if (accummulated + (useWeights ? split.getWeight() : 1.0) >= 0.5 * bestLength.get()) {
                double part1 = 0.5 * bestLength.get() - accummulated;
                double part2 = (useWeights ? split.getWeight() : 1.0) - part1;

                if (split.getPartContaining(1).get(start.get()))
                    return new Triplet<>(splitsBlock.indexOf(split), part1, part2);
                else
                    return new Triplet<>(splitsBlock.indexOf(split), part2, part1);
            }
            accummulated += (useWeights ? split.getWeight() : 1.0);
        }
        return new Triplet<>(0, 0.0, 0.0);
    }

    private static void computeLongestPathRec(boolean useWeights, ArrayList<ASplit> splits, double remainingLength, int pos, ArrayList<ASplit> path, double length, ArrayList<ASplit> bestPath, Single<Double> bestLength) {
        if (pos >= splits.size()) {
            if (length > bestLength.get()) {
                bestPath.clear();
                bestPath.addAll(path);
                bestLength.set(length);
            }
        } else {
            final ASplit split = splits.get(pos);
            final double weight = (useWeights ? split.getWeight() : 1);

            if (length + remainingLength - weight > bestLength.get())
                computeLongestPathRec(useWeights, splits, remainingLength - weight, pos + 1, path, length, bestPath, bestLength);
            if (length + remainingLength > bestLength.get() && isCompatibleWithAll(splits, splits.get(pos))) {
                path.add(split);
                computeLongestPathRec(useWeights, splits, remainingLength - weight, pos + 1, path, length + weight, bestPath, bestLength);
                path.remove(splits.get(pos));
            }
        }
    }

    private static boolean isCompatibleWithAll(ArrayList<ASplit> splits, ASplit other) {
        for (ASplit split : splits) {
            if (!split.equals(other)) {
                if (!Compatibility.areCompatible(split, other))
                    return false;
            }
        }
        return true;
    }
}
