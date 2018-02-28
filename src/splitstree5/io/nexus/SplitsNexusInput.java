/*
 *  Copyright (C) 2018 Daniel H. Huson
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

import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Compatibility;
import splitstree5.io.imports.IOExceptionWithLineNumber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * nexus input parser
 * Daniel Huson, 2.2018
 */
public class SplitsNexusInput implements INexusInput<SplitsBlock> {
    public static final String NAME = "SPLITS";

    /**
     * is the parser at the beginning of a block that this class can parse?
     *
     * @param np
     * @return true, if can parse from here
     */
    public boolean atBeginOfBlock(NexusStreamParser np) {
        return np.peekMatchIgnoreCase("begin " + NAME + ";");
    }

    public static final String SYNTAX = "BEGIN " + NAME + ";\n" +
            "\t[TITLE title;]\n" +
            "\t[LINK name = title;]\n" +
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
     * @return
     * @throws IOException
     */
    @Override
    public List<String> parse(NexusStreamParser np, TaxaBlock taxaBlock, SplitsBlock splitsBlock) throws IOException {
        splitsBlock.clear();

        final ArrayList<String> taxonNamesFound = new ArrayList<>();

        final SplitsNexusFormat format = (SplitsNexusFormat) splitsBlock.getFormat();

        np.matchBeginBlock(NAME);
        UtilitiesNexusIO.readTitleLinks(np, splitsBlock);

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
            format.setOptionLabels(np.findIgnoreCase(formatTokens, "labels=no", false, format.isOptionLabels()));
            format.setOptionLabels(np.findIgnoreCase(formatTokens, "labels=left", true, format.isOptionLabels()));

            format.setOptionWeights(np.findIgnoreCase(formatTokens, "weights=no", false, format.isOptionWeights()));
            format.setOptionWeights(np.findIgnoreCase(formatTokens, "weights=yes", true, format.isOptionWeights()));

            format.setOptionConfidences(np.findIgnoreCase(formatTokens, "confidences=no", false, format.isOptionConfidences()));
            format.setOptionConfidences(np.findIgnoreCase(formatTokens, "confidences=yes", true, format.isOptionConfidences()));

            // for backward compatiblity, we never used this in SplitsTree4...
            np.findIgnoreCase(formatTokens, "intervals=no", false, false);
            np.findIgnoreCase(formatTokens, "intervals=true", true, false);

            // for backward compatibility:
            format.setOptionLabels(np.findIgnoreCase(formatTokens, "no labels", false, format.isOptionLabels()));
            format.setOptionLabels(np.findIgnoreCase(formatTokens, "labels", true, format.isOptionLabels()));

            format.setOptionWeights(np.findIgnoreCase(formatTokens, "no weights", false, format.isOptionWeights()));
            format.setOptionWeights(np.findIgnoreCase(formatTokens, "weights", true, format.isOptionWeights()));

            format.setOptionConfidences(np.findIgnoreCase(formatTokens, "no confidences", false, format.isOptionConfidences()));
            format.setOptionConfidences(np.findIgnoreCase(formatTokens, "confidences", true, format.isOptionConfidences()));

            // for backward compatiblity, we never used this in SplitsTree4...
            np.findIgnoreCase(formatTokens, "no intervals", false, false);
            np.findIgnoreCase(formatTokens, "intervals", true, false);
            if (formatTokens.size() != 0)
                throw new IOExceptionWithLineNumber(np.lineno(), "'" + formatTokens + "' unexpected in FORMAT");

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
            readMatrix(np, ntax, nsplits, splitsBlock, format);
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

            if (splitsNexusFormat.isOptionLabels()) {
                label = np.getWordRespectCase();
                if (label.equals("null"))
                    label = null;
            }
            if (splitsNexusFormat.isOptionWeights())
                weight = (float) Math.max(0.0, np.getDouble());

            if (splitsNexusFormat.isOptionConfidences())
                confidence = (float) Math.max(0.0, np.getDouble());

            final BitSet set = new BitSet();

            while (!np.peekMatchIgnoreCase(",")) {
                Integer t = new Integer(np.getWordRespectCase());
                set.set(t);

            }
            np.matchIgnoreCase(",");
            if (set.cardinality() == 0 || set.cardinality() == ntax)
                throw new IOExceptionWithLineNumber(np.lineno(), "non-split of size " + set.cardinality());


            final ASplit split = new ASplit(set, ntax, weight);
            if (confidence != -1)
                split.setConfidence(confidence);
            if (label != null)
                split.setLabel(label);
            splitsBlock.getSplits().add(split);
        }
    }
}
