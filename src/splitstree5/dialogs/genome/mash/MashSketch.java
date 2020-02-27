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

package splitstree5.dialogs.genome.mash;

import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import jloda.util.parse.NexusStreamParser;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.GZIPOutputStream;

/**
 * a Mash sketch
 * Daniel Huson, 1.2019
 */
public class MashSketch {
    public static byte[] MAGIC_NUMBER = "MASHV0".getBytes();
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
     * @param kSize
     * @param name
     * @param isNucleotides
     * @param use64Bits
     */
    public MashSketch(int sketchSize, int kSize, String name, boolean isNucleotides, boolean use64Bits) {
        this.sketchSize = sketchSize;
        this.kSize = kSize;
        this.name = name;
        this.isNucleotides = isNucleotides;
        this.use64Bits = use64Bits;
    }

    /**
     * construct a sketch from a data input stream
     *
     * @param ins
     */
    public MashSketch(DataInputStream ins) throws IOException {
        final int headerLength = ins.readInt();
        final byte[] headerBytes = new byte[headerLength];
        int got = 0;
        do {
            got += ins.read(headerBytes, got, headerLength - got);
        }
        while (got < headerLength);
        // "##ComputeMashSketch name='%s' sketchSize=%d kSize=%d type=%s bits=%b\n
        NexusStreamParser np = new NexusStreamParser(new StringReader(new String(headerBytes)));
        np.matchIgnoreCase("##ComputeMashSketch name=");
        name = np.getWordRespectCase();
        np.matchIgnoreCase("sketchSize=");
        sketchSize = np.getInt();
        np.matchIgnoreCase("kSize=");
        this.kSize = np.getInt();
        np.matchIgnoreCase("type=");
        final String type = np.getWordRespectCase();
        isNucleotides = type.equals("nucl");
        if (np.peekMatchIgnoreCase("bits=64")) {
            np.matchIgnoreCase("bits=64");
            use64Bits = true;
        } else {
            np.matchIgnoreCase("bits=32");
            use64Bits = false;
        }

        values = new long[sketchSize];
        for (int i = 0; i < sketchSize; i++) {
            if (use64Bits)
                values[i] = ins.readLong();
            else
                values[i] = (long) ins.readInt();
        }
    }

    public String getHeader() {
        return String.format("##ComputeMashSketch name='%s' sketchSize=%d kSize=%d type=%s bits=%d\n", name, sketchSize, kSize, isNucleotides ? "nucl" : "aa", use64Bits ? 64 : 32);
    }

    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append(getHeader());
        for (long value : getValues()) {
            buf.append(String.format("%d\n", value));
        }
        return buf.toString();
    }

    /**
     * write a sketch in binary
     *
     * @param outs
     * @throws IOException
     */
    public void write(DataOutputStream outs) throws IOException {
        final byte[] headerBytes = getHeader().getBytes();
        outs.writeInt(headerBytes.length);
        outs.write(headerBytes);
        for (long value : values) {
            if (use64Bits)
                outs.writeLong(value);
            else
                outs.writeInt((int) value);
        }
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

    /**
     * read a sketch file
     *
     * @param fileName
     * @return sketches contained in file
     * @throws IOException
     */
    public static Collection<MashSketch> readFile(String fileName) throws IOException {
        try (DataInputStream ins = new DataInputStream(new FileInputStream(fileName))) {
            Basic.readAndVerifyMagicNumber(ins, MAGIC_NUMBER);
            final ArrayList<MashSketch> list = new ArrayList<>();
            while (ins.available() > 0) {
                list.add(new MashSketch(ins));
            }
            return list;
        }
    }

    /**
     * write a collection of sketches to a file
     *
     * @param sketches
     * @param outputFile - can be stdout or  .gz file
     * @throws IOException
     */
    public static void write(Collection<MashSketch> sketches, String outputFile, ProgressListener progress) throws IOException, CanceledException {
        progress.setMaximum(sketches.size());
        progress.setProgress(0);
        try (DataOutputStream outs = new DataOutputStream(outputFile.endsWith(".gz") ?
                new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(outputFile)))
                : outputFile.equals("stdout") ? new PrintStream(System.out)
                : new BufferedOutputStream(new FileOutputStream(outputFile)))) {
            outs.write(MashSketch.MAGIC_NUMBER);
            for (MashSketch sketch : sketches) {
                if (sketch != null)
                    sketch.write(outs);
                progress.incrementProgress();
            }
        }
    }
}
