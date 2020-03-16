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

package splitstree5.core.algorithms.genomes2distances.mash;

import jloda.seq.DNA5Alphabet;
import jloda.thirdparty.MurmurHash;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.genomes2distances.utils.KMerUtils;
import splitstree5.core.algorithms.genomes2distances.utils.bloomfilter.BloomFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;

/**
 * a Mash sketch
 * Daniel Huson, 1.2019
 */
public class MashSketch {
    private final int sketchSize;
    private final int kSize;
    private final String name;
    private final boolean isNucleotides;
    private final boolean use64Bits;

    private long[] values;

    /**
     * construct a new sketch
     *
     * @param sketchSize
     * @param kMerSize
     * @param name
     * @param isNucleotides
     * @param use64Bits
     */
    public MashSketch(int sketchSize, int kMerSize, String name, boolean isNucleotides, boolean use64Bits) {
        this.sketchSize = sketchSize;
        this.kSize = kMerSize;
        this.name = name;
        this.isNucleotides = isNucleotides;
        this.use64Bits = use64Bits;
    }

    /**
     * compute a mash sketch
     *
     * @param name
     * @param sequences
     * @param isNucleotides
     * @param sketchSize
     * @param kMerSize
     * @param use64Bits
     * @return
     * @throws IOException
     */
    public static MashSketch compute(String name, Collection<byte[]> sequences, boolean isNucleotides, int sketchSize, int kMerSize, boolean use64Bits, boolean filterUniqueKMers, ProgressListener progress) {
        final MashSketch sketch = new MashSketch(sketchSize, kMerSize, name, isNucleotides, use64Bits);

        final TreeSet<Long> sortedSet = new TreeSet<>();
        sortedSet.add(Long.MAX_VALUE);
        final int seed = 666;

        final BloomFilter bloomFilter;
        if (filterUniqueKMers)
            bloomFilter = new BloomFilter(sequences.stream().mapToInt(s -> s.length).sum(), 500000000);
        else
            bloomFilter = null;

        try {
            for (byte[] sequence : sequences) {
                final byte[] reverseComplement = (isNucleotides ? DNA5Alphabet.reverseComplement(sequence, new byte[sequence.length]) : null);

                final int top = sequence.length - kMerSize;
                for (int offset = 0; offset < top; offset++) {
                    if (isNucleotides) {
                        final int ambiguous = Basic.lastIndexOf(sequence, offset, kMerSize, 'N'); // don't use k-mers with ambiguity letters
                        if (ambiguous != -1) {
                            offset = ambiguous; // skip to last ambiguous so that increment will move past
                            continue;
                        }
                    }
                    final int offsetUse;
                    final byte[] seqUse;

                    if (!isNucleotides || KMerUtils.isCanonical(offset, kMerSize, sequence, reverseComplement)) {
                        offsetUse = offset;
                        seqUse = sequence;
                    } else {
                        offsetUse = sequence.length - offset - kMerSize;
                        seqUse = reverseComplement;
                    }

                    if (bloomFilter != null && bloomFilter.add(seqUse, offsetUse, kMerSize)) {
                        continue; // first time we have seen this k-mer
                    }

                    final long hash = (use64Bits ? MurmurHash.hash64(seqUse, offsetUse, kMerSize, seed) : (long) MurmurHash.hash32(seqUse, offsetUse, kMerSize, seed));

                    if (hash < sortedSet.last()) {
                        if (sortedSet.add(hash) && sortedSet.size() > sketchSize)
                            sortedSet.pollLast();
                    }
                    progress.checkForCancel();
                }
            }
            if (sortedSet.size() == sketchSize) {
                final long[] values = new long[sortedSet.size()];
                int pos = 0;
                for (Long value : sortedSet)
                    values[pos++] = value;
                sketch.setValues(values);
                //System.err.println(sketch.getName()+" min: "+values[0]+" max: "+values[values.length-1]);
            } else {
                sketch.setValues(new long[0]);
                System.err.println("Computing sketch " + sketch.getName() + ": Too few k-mers: " + sortedSet.size());
            }
            progress.incrementProgress();
        } catch (CanceledException ignored) {
        }
        return sketch;
    }

    public String getHeader() {
        return String.format("##ComputeMashSketch name='%s' sketchSize=%d kSize=%d type=%s bits=%d\n", name, sketchSize, kSize, isNucleotides ? "nucl" : "aa", use64Bits ? 64 : 32);
    }

    public String toString() {
        return getHeader();
    }

    public int getSketchSize() {
        return sketchSize;
    }

    public int getkSize() {
        return kSize;
    }

    public String getName() {
        return name;
    }

    public boolean isNucleotides() {
        return isNucleotides;
    }

    public boolean isUse64Bits() {
        return use64Bits;
    }

    public long[] getValues() {
        return values;
    }

    public void setValues(long[] values) {
        this.values = values;
    }

    public static boolean canCompare(MashSketch a, MashSketch b) {
        return a.getSketchSize() == b.getSketchSize() && a.getkSize() == b.getkSize() && a.isNucleotides() == b.isNucleotides();
    }
}
