/*
 *  MashSketch.java Copyright (C) 2019. Daniel H. Huson
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

package splitstree5.core.algorithms.genomes2distances.dashing;

import jloda.seq.DNA5Alphabet;
import jloda.thirdparty.MurmurHash;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.genomes2distances.utils.KMerUtils;
import splitstree5.core.algorithms.genomes2distances.utils.bloomfilter.BloomFilter;

import java.io.IOException;
import java.util.Collection;

/**
 * a Dashing sketch
 * Daniel Huson, 3.2020, based on code by Benjamin Kaestle, https://github.com/BenKaestle/DEMAD
 */
public class DashingSketch {
    final private static double ALPHA_M = (1.0 / (2.0 * Math.log(2)));

    private final String name;
    private final int[] register;
    private final int kmerSize;
    private final int prefixSize;
    private double harmonicMean = Double.NEGATIVE_INFINITY;

    /**
     * construct a new sketch
     *
     * @param kmerSize
     */
    public DashingSketch(String name, int kmerSize, int prefixSize) {
        this.name = name;
        this.kmerSize = kmerSize;
        this.prefixSize = prefixSize;
        register = new int[(int) Math.pow(2, prefixSize)];
    }

    /**
     * for a given genome size and the desired probability prob of observing a random k-mer calculate the optimal k value
     *
     * @param genomeSize
     * @param prob       - probability of observing a random k-mer
     * @return
     */
    public static int optimalK(long genomeSize, float prob) {
        return (int) Math.ceil(Math.log(genomeSize * (1 - prob) / prob) / Math.log(4));
    }

    /**
     * compute a dashing sketch
     *
     * @param name
     * @param sequences
     * @param kMerSize
     * @return
     * @throws IOException
     */
    public static DashingSketch compute(String name, Collection<byte[]> sequences, int kMerSize, int prefixSize, boolean filterUniqueKMers, ProgressListener progress) {
        final DashingSketch sketch = new DashingSketch(name, kMerSize, prefixSize);

        // todo: compute:
        final int minSize = 1000;

        final int seed = 777;

        final int[] register = sketch.getRegister();
        final int registerLength = register.length;

        final BloomFilter bloomFilter;
        if (filterUniqueKMers)
            bloomFilter = new BloomFilter(sequences.stream().mapToInt(s -> s.length).sum(), 500000000);
        else
            bloomFilter = null;

        try {
            int count = 0;
            for (byte[] sequence : sequences) {
                final byte[] reverseComplement = DNA5Alphabet.reverseComplement(sequence, new byte[sequence.length]);

                final int top = sequence.length - kMerSize;
                for (int offset = 0; offset < top; offset++) {
                    final int ambiguous = Basic.lastIndexOf(sequence, offset, kMerSize, 'N'); // don't use k-mers with ambiguity letters
                    if (ambiguous != -1) {
                        offset = ambiguous; // skip to last ambiguous so that increment will move past
                        continue;
                    }
                    final int offsetUse;
                    final byte[] seqUse;

                    if (KMerUtils.isCanonical(offset, kMerSize, sequence, reverseComplement)) {
                        offsetUse = offset;
                        seqUse = sequence;
                    } else {
                        offsetUse = sequence.length - offset - kMerSize;
                        seqUse = reverseComplement;
                    }

                    if (bloomFilter != null && bloomFilter.add(seqUse, offsetUse, kMerSize)) {
                        continue; // first time we have seen this k-mer
                    }

                    final long hash = MurmurHash.hash64(seqUse, offsetUse, kMerSize, seed);
                    int registerKey = (int) (hash % (long) registerLength);
                    if (registerKey < 0)
                        registerKey = registerKey + registerLength;

                    register[registerKey] = Math.max(register[registerKey], countZerosAfterPrefix(hash, prefixSize));

                    count++;
                    progress.checkForCancel();
                }
            }
            if (count < minSize) {
                System.err.println("Computing sketch " + sketch.getName() + ": Too few k-mers: " + count);
            } else {
                sketch.setHarmonicMean(computeHarmonicMean(register));
            }


            progress.incrementProgress();
        } catch (CanceledException ignored) {
        }
        return sketch;
    }

    /**
     * estimates cardinalities of sets with the register values by calculating harmonic means
     *
     * @param register
     * @return
     */
    public static double computeHarmonicMean(int[] register) {
        final int registerSize = register.length;
        float denominator = 0;
        for (int value : register) {
            denominator += Math.pow(2, -(value + 1));
        }
        return ALPHA_M * registerSize * registerSize / denominator;
    }

    /**
     * count leading zeros after prefix
     *
     * @param val
     * @param prefixSize
     * @return number of zeros
     */
    public static int countZerosAfterPrefix(long val, int prefixSize) {
        int n = 64;
        long y = val >> 32;
        if (y != 0) {
            n = n - 32;
            val = y;
        }
        y = val >> 16;
        if (y != 0) {
            n = n - 16;
            val = y;
        }
        y = val >> 8;
        if (y != 0) {
            n = n - 8;
            val = y;
        }
        y = val >> 4;
        if (y != 0) {
            n = n - 4;
            val = y;
        }
        y = val >> 2;
        if (y != 0) {
            n = n - 2;
            val = y;
        }
        y = val >> 1;
        if (y != 0)
            return Math.min(n - 2, 64 - prefixSize);
        return Math.min(n - (int) val, 64 - prefixSize);
    }

    /**
     * compute the union of two sketches
     *
     * @param sketch1
     * @param sketch2
     * @return union
     */
    public static DashingSketch union(DashingSketch sketch1, DashingSketch sketch2) {
        final DashingSketch union = new DashingSketch("union", sketch1.getKmerSize(), sketch1.getPrefixSize());
        for (int i = 0; i < union.getRegister().length; i++) {
            union.getRegister()[i] = Math.max(sketch1.getRegister()[i], sketch2.getRegister()[i]);
        }
        union.setHarmonicMean(computeHarmonicMean(union.getRegister()));
        return union;
    }

    public String getHeader() {
        return String.format("##ComputeDashingSketch name='%s' kSize=%d prefixSize=%d\n", name, kmerSize, prefixSize);
    }

    public String getName() {
        return name;
    }

    public int[] getRegister() {
        return register;
    }

    public int getKmerSize() {
        return kmerSize;
    }

    public int getPrefixSize() {
        return prefixSize;
    }

    public double getHarmonicMean() {
        return harmonicMean;
    }

    public void setHarmonicMean(double harmonicMean) {
        this.harmonicMean = harmonicMean;
    }

    public static boolean canCompare(DashingSketch a, DashingSketch b) {
        return a.getKmerSize() == b.getKmerSize() && a.getPrefixSize() == b.getPrefixSize();
    }
}
