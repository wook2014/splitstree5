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

import jloda.thirdparty.MurmurHash;
import jloda.util.*;
import splitstree5.core.algorithms.genomes2distances.utils.bloomfilter.BloomFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;

/**
 * a Mash sketch
 * Daniel Huson, 1.2019
 */
public class MashSketch {
    public static int MAGIC_INT = 1213415757; // 1213415757

    private final static Long MASK_32BIT = (1L << 32) - 1L;
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
    public static MashSketch compute(String name, Collection<byte[]> sequences, boolean isNucleotides, int sketchSize, int kMerSize, int seed, boolean use64Bits, boolean filterUniqueKMers, ProgressListener progress) {
        final MashSketch sketch = new MashSketch(sketchSize, kMerSize, name, isNucleotides, use64Bits);

        final TreeSet<Long> sortedSet = new TreeSet<>();
        sortedSet.add(Long.MAX_VALUE);

        final BloomFilter bloomFilter;
        if (filterUniqueKMers)
            bloomFilter = new BloomFilter(sequences.stream().mapToInt(s -> s.length).sum(), 500000000);
        else
            bloomFilter = null;

        try {
            byte[] kMerReverseComplement = new byte[kMerSize]; // will reuse

            for (byte[] sequence : sequences) {
                final int top = sequence.length - kMerSize;
                for (int offset = 0; offset < top; offset++) {
                    if (isNucleotides) {
                        final int ambiguousPos = Basic.lastIndexOf(sequence, offset, kMerSize, 'N'); // don't use k-mers with ambiguity letters
                        if (ambiguousPos != -1) {
                            // offset = ambiguousPos; // skip to last ambiguous so that increment will move past
                            continue;
                        }
                    }
                    final int offsetUse;
                    final byte[] seqUse;

                    if (!isNucleotides) {
                        offsetUse = offset;
                        seqUse = sequence;
                    } else {
                        kMerReverseComplement = SequenceUtils.getReverseComplement(sequence, offset, kMerSize, kMerReverseComplement);

                        if (SequenceUtils.compare(sequence, offset, kMerReverseComplement, 0, kMerSize) <= 0) {
                            offsetUse = offset;
                            seqUse = sequence;
                        } else {
                            offsetUse = 0;
                            seqUse = kMerReverseComplement;
                        }
                    }

                    if (bloomFilter != null && bloomFilter.add(seqUse, offsetUse, kMerSize)) {
                        continue; // first time we have seen this k-mer
                    }

                    final long hash = (use64Bits ? MurmurHash.hash64(seqUse, offsetUse, kMerSize, seed) : (long) MurmurHash.hash32(seqUse, offsetUse, kMerSize, seed));

                    //  final long hash=(use64Bits? NTHash.NTP64(seqUse,kMerSize,offsetUse):MASK_32BIT&NTHash.NTP64(seqUse,kMerSize,offsetUse));

                    if (hash < sortedSet.last()) {
                        if (sortedSet.add(hash) && sortedSet.size() > sketchSize)
                            sortedSet.pollLast();
                    }
                    progress.checkForCancel();
                }
            }
            {
                final long[] values = new long[sortedSet.size()];
                int pos = 0;
                for (Long value : sortedSet)
                    values[pos++] = value;
                sketch.setValues(values);
                //System.err.println(sketch.getName()+" min: "+values[0]+" max: "+values[values.length-1]);
            }
            if (sortedSet.size() < sketchSize) {
                System.err.println(String.format("Warning: Computing sketch %s: Too few k-mers: %,d of %,d", sketch.getName(), sortedSet.size(), sketchSize));
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

    public String getString() {
        return String.format("s=%d k=%d b=%d:%s\n", sketchSize, kSize, use64Bits ? 64 : 32, Basic.toString(getValues(), ","));
    }

    public static MashSketch parse(String string) throws IOException {
        int sketchSize = Basic.parseInt(Basic.getWordAfter("s=", string));
        int kMerSize = Basic.parseInt(Basic.getWordAfter("k=", string));
        int bits = Basic.parseInt(Basic.getWordAfter("b=", string));
        String[] numbers = Basic.split(Basic.getWordAfter(":", string), ',');

        if (numbers.length != sketchSize)
            throw new IOException("Expected sketch size " + sketchSize + ", found: " + numbers.length);

        long[] values = new long[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            values[i] = Basic.parseLong(numbers[i]);
        }
        final MashSketch sketch = new MashSketch(sketchSize, kMerSize, "", true, bits != 32);
        sketch.setValues(values);
        return sketch;
    }

    public byte[] getBytes() {
        ByteOutputBuffer bytes = new ByteOutputBuffer();
        bytes.writeIntLittleEndian(MAGIC_INT);
        bytes.writeIntLittleEndian(sketchSize);
        bytes.writeIntLittleEndian(kSize);
        bytes.writeIntLittleEndian(use64Bits ? 64 : 32);
        for (long value : values) {
            if (use64Bits)
                bytes.writeLongLittleEndian(value);
            else
                bytes.writeIntLittleEndian((int) value);
        }
        return bytes.copyBytes();
    }

    public static MashSketch parse(byte[] bytes) throws IOException {
        final ByteInputBuffer buffer = new ByteInputBuffer(bytes);

        if (buffer.readIntLittleEndian() != MAGIC_INT)
            throw new IOException("Incorrect magic number");
        int sketchSize = buffer.readIntLittleEndian();
        int kMerSize = buffer.readIntLittleEndian();
        boolean use64Bits = (buffer.readIntLittleEndian() != 32);

        final long[] values = new long[sketchSize];
        for (int i = 0; i < values.length; i++) {
            if (use64Bits)
                values[i] = buffer.readLongLittleEndian();
            else
                values[i] = buffer.readIntLittleEndian();
        }
        final MashSketch sketch = new MashSketch(sketchSize, kMerSize, "", true, use64Bits);
        sketch.setValues(values);
        return sketch;
    }
}
