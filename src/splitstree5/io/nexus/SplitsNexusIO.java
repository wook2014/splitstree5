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

package splitstree5.io.nexus;

import com.sun.istack.internal.Nullable;
import jloda.util.Basic;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Compatibility;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * input and output of a splits block in Nexus format
 * Created by huson on 12/28/16.
 */
public class SplitsNexusIO {
    public static final String NAME = "SPLITS";

    public static final String SYNTAX = "BEGIN " + NAME + ";\n" +
            "\t[DIMENSIONS [NTAX=number-of-taxa] [NSPLITS=number-of-splits];]\n" +
            "\t[FORMAT\n" +
            "\t\t[LABELS={LEFT|NO}]\n" +
            "\t\t[WEIGHTS={YES|NO}]\n" +
            "\t\t[CONFIDENCES={YES|NO}]\n" +
            "\t\t[INTERVALS={YES|NO}]\n" +
            "\t;]\n" +
            "\t[THRESHOLD=non-negative-number;]\n" +
            "\t[PROPERTIES\n" +
            "\t\t[FIT=non-negative-number]\n" +
            "\t\t[leastsquares]\n" +
            "\t\t[{COMPATIBLE|CYCLIC|WEAKLY COMPATIBLE|INCOMPATIBLE]\n" +
            "\t;]\n" +
            "\t[CYCLE [taxon_i_1 taxon_i_2 ... taxon_i_ntax];]\n" +
            "\t[SPLITSLABELS label_1 label_2 ... label_nsplits;]\n" +
            "\tMATRIX\n" +
            "\t\t[label_1] [weight_1] [confidence_1] split_1,\n" +
            "\t\t[label_2] [weight_2] [confidence_2] split_2,\n" +
            "\t\t ....\n" +
            "\t \t[label_nsplits] [weight_nsplits] [confidence_nsplits] split_nsplits,\n" +
            "\t;\n" +
            "END;\n";

    /**
     * report the syntax for this block
     *
     * @return syntax string
     */
    public String getSyntax() {
        return SYNTAX;
    }

    /**
     * parse a splits block
     *
     * @param np
     * @param taxaBlock
     * @param splitsBlock
     * @param splitsNexusFormat
     * @return taxon names found in this block
     * @throws IOException
     */
    public static ArrayList<String> parse(NexusStreamParser np, TaxaBlock taxaBlock, SplitsBlock splitsBlock, @Nullable SplitsNexusFormat splitsNexusFormat) throws IOException {
        final ArrayList<String> taxonNamesFound = new ArrayList<>();

        if (splitsNexusFormat == null)
            splitsNexusFormat = new SplitsNexusFormat();

        np.matchBeginBlock(NAME);

        final int ntax = taxaBlock.getNtax();
        np.matchIgnoreCase("dimensions ntax=" + ntax);
        int nsplits = 0;
        if (np.peekMatchIgnoreCase("nsplits=")) {
            np.matchIgnoreCase("nsplits=");
            nsplits = np.getInt();
        }
        np.matchIgnoreCase(";");

        if (np.peekMatchIgnoreCase("FORMAT")) {
            final List<String> formatTokens = np.getTokensLowerCase("format", ";");
            splitsNexusFormat.setLabels(np.findIgnoreCase(formatTokens, "labels=no", false, splitsNexusFormat.isLabels()));
            splitsNexusFormat.setLabels(np.findIgnoreCase(formatTokens, "labels=left", true, splitsNexusFormat.isLabels()));

            splitsNexusFormat.setWeights(np.findIgnoreCase(formatTokens, "weights=no", false, splitsNexusFormat.isWeights()));
            splitsNexusFormat.setWeights(np.findIgnoreCase(formatTokens, "weights=yes", true, splitsNexusFormat.isWeights()));

            splitsNexusFormat.setConfidences(np.findIgnoreCase(formatTokens, "confidences=no", false, splitsNexusFormat.isConfidences()));
            splitsNexusFormat.setConfidences(np.findIgnoreCase(formatTokens, "confidences=yes", true, splitsNexusFormat.isConfidences()));

            // for backward compatiblity, we never used this in SplitsTree4...
            np.findIgnoreCase(formatTokens, "intervals=no", false, false);
            np.findIgnoreCase(formatTokens, "intervals=true", true, false);

            // for backward compatibility:
            splitsNexusFormat.setLabels(np.findIgnoreCase(formatTokens, "no labels", false, splitsNexusFormat.isLabels()));
            splitsNexusFormat.setLabels(np.findIgnoreCase(formatTokens, "labels", true, splitsNexusFormat.isLabels()));

            splitsNexusFormat.setWeights(np.findIgnoreCase(formatTokens, "no weights", false, splitsNexusFormat.isWeights()));
            splitsNexusFormat.setWeights(np.findIgnoreCase(formatTokens, "weights", true, splitsNexusFormat.isWeights()));

            splitsNexusFormat.setConfidences(np.findIgnoreCase(formatTokens, "no confidences", false, splitsNexusFormat.isConfidences()));
            splitsNexusFormat.setConfidences(np.findIgnoreCase(formatTokens, "confidences", true, splitsNexusFormat.isConfidences()));

            // for backward compatiblity, we never used this in SplitsTree4...
            np.findIgnoreCase(formatTokens, "no intervals", false, false);
            np.findIgnoreCase(formatTokens, "intervals", true, false);
            if (formatTokens.size() != 0)
                throw new IOException("line " + np.lineno() + ": '" + formatTokens + "' unexpected in FORMAT");

        }
        if (np.peekMatchIgnoreCase("threshold=")) {
            np.matchIgnoreCase("threshold=");
            splitsBlock.setThreshold((float) np.getDouble());
            np.matchIgnoreCase(";");
        }
        if (np.peekMatchIgnoreCase("PROPERTIES")) {
            List<String> p = np.getTokensLowerCase("properties", ";");

            splitsBlock.setFit((float) np.findIgnoreCase(p, "fit=", -1.0, 100.0, splitsBlock.getFit()));

            if (np.findIgnoreCase(p, "weakly compatible", true, splitsBlock.getCompatibility() == Compatibility.weaklyCompatible))
                splitsBlock.setCompatibility(Compatibility.weaklyCompatible);

            if (np.findIgnoreCase(p, "non compatible", true, splitsBlock.getCompatibility() == Compatibility.incompatible))
                splitsBlock.setCompatibility(Compatibility.incompatible);

            if (np.findIgnoreCase(p, "compatible", true, splitsBlock.getCompatibility() == Compatibility.compatible))
                splitsBlock.setCompatibility(Compatibility.compatible);

            if (np.findIgnoreCase(p, "cyclic", true, splitsBlock.getCompatibility() == Compatibility.cyclic))
                splitsBlock.setCompatibility(Compatibility.cyclic);

            if (np.findIgnoreCase(p, "incompatible", true, splitsBlock.getCompatibility() == Compatibility.incompatible))
                splitsBlock.setCompatibility(Compatibility.incompatible);

            // for compatiblity with splitstree4
            np.findIgnoreCase(p, "leastsquares", true, false);
        }

        if (np.peekMatchIgnoreCase("CYCLE")) {
            np.matchIgnoreCase("cycle");
            int[] cycle = new int[ntax + 1];
            for (int i = 1; i <= ntax; i++)
                cycle[i] = np.getInt();
            np.matchIgnoreCase(";");
            splitsBlock.setCycle(cycle);
        }
        if (np.peekMatchIgnoreCase("matrix")) {
            np.matchIgnoreCase("matrix");
            readMatrix(np, ntax, nsplits, splitsBlock, splitsNexusFormat);
            np.matchIgnoreCase(";");
        }
        np.matchEndBlock();

        return taxonNamesFound;
    }

    /**
     * Read a matrix.
     *
     * @param np the nexus parser
     */
    private static void readMatrix(NexusStreamParser np, int ntax, int nsplits, SplitsBlock splitsBlock, SplitsNexusFormat splitsNexusFormat) throws IOException {

        for (int i = 1; i <= nsplits; i++) {
            float weight = 1;
            float confidence = -1;
            String label = null;

            if (splitsNexusFormat.isLabels()) {
                label = np.getWordRespectCase();
                if (label.equals("null"))
                    label = null;
            }
            if (splitsNexusFormat.isWeights())
                weight = (float) Math.max(0.0, np.getDouble());

            if (splitsNexusFormat.isConfidences())
                confidence = (float) Math.max(0.0, np.getDouble());

            final BitSet set = new BitSet();

            while (!np.peekMatchIgnoreCase(",")) {
                Integer t = new Integer(np.getWordRespectCase());
                set.set(t);

            }
            np.matchIgnoreCase(",");
            if (set.cardinality() == 0 || set.cardinality() == ntax)
                throw new IOException("line " + np.lineno() + ": non-split of size " + set.cardinality());


            final ASplit split = new ASplit(set, ntax, weight);
            if (confidence != -1)
                split.setConfidence(confidence);
            if (label != null)
                split.setLabel(label);
            splitsBlock.getSplits().add(split);
        }
    }

    /**
     * write a block in nexus format
     *
     * @param w
     * @param taxaBlock
     * @param splitsBlock
     * @param splitsNexusFormat - if null
     * @throws IOException
     */
    public static void write(Writer w, TaxaBlock taxaBlock, SplitsBlock splitsBlock, @Nullable SplitsNexusFormat splitsNexusFormat) throws IOException {
        if (splitsNexusFormat == null)
            splitsNexusFormat = new SplitsNexusFormat();

        final int ntax = taxaBlock.getNtax();
        final int nsplits = splitsBlock.getNsplits();

        w.write("\nBEGIN " + NAME + ";\n");
        w.write("DIMENSIONS ntax=" + ntax + " nsplits=" + nsplits + ";\n");

        w.write("FORMAT");
        if (splitsNexusFormat.isLabels())
            w.write(" labels=left");
        else
            w.write(" labels=no");
        if (splitsNexusFormat.isWeights())
            w.write(" weights=yes");
        else
            w.write(" weights=no");
        if (splitsNexusFormat.isConfidences())
            w.write(" confidences=yes");
        else
            w.write(" confidences=no");
        w.write(";\n");
        if (splitsBlock.getThreshold() != 0)
            w.write("THRESHOLD=" + splitsBlock.getThreshold() + "; \n");
        w.write(String.format("PROPERTIES fit=%.2f", splitsBlock.getFit()));
        switch (splitsBlock.getCompatibility()) {
            case compatible:
                w.write(" compatible");
                break;
            case cyclic:
                w.write(" cyclic");
                break;
            case weaklyCompatible:
                w.write(" weakly compatible");
                break;
            case incompatible:
                w.write(" non compatible");
            default:
                break;
        }
        w.write(";\n");

        if (splitsBlock.getCycle() != null) {
            w.write("CYCLE");
            int[] cycle = splitsBlock.getCycle();
            for (int i = 1; i < cycle.length; i++)
                w.write(" " + cycle[i]);
            w.write(";\n");
        }

        w.write("MATRIX\n");

        int t = 1;
        for (ASplit split : splitsBlock.getSplits()) {
            w.write("[" + (t++) + ", size=" + split.size() + "]" + " \t");
            if (splitsNexusFormat.isLabels()) {
                String lab = split.getLabel();
                w.write(" '" + lab + "'" + " \t");
            }
            if (splitsNexusFormat.isWeights()) {
                w.write(" " + split.getWeight() + " \t");
            }
            if (splitsNexusFormat.isConfidences()) {
                w.write(" " + split.getConfidence() + " \t");
            }
            w.write(" " + Basic.toString(split.getA(), ' ') + ",\n");
        }
        w.write(";\n");
        w.write("END; [" + NAME + "]\n");
    }
}
