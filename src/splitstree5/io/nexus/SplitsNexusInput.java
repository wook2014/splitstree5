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

import jloda.util.BitSetUtils;
import jloda.util.IOExceptionWithLineNumber;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Compatibility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * nexus input parser
 * Daniel Huson, 2.2018
 */
public class SplitsNexusInput extends NexusIOBase implements INexusInput<SplitsBlock> {
    public static final String SYNTAX = "BEGIN " + SplitsBlock.BLOCK_NAME + ";\n" +
            "\t[TITLE title;]\n" +
            "\t[LINK name = title;]\n" +
            "\t[DIMENSIONS [NTAX=number-of-taxa] [NSPLITS=number-of-splits];]\n" +
            "\t[FORMAT\n" +
            "\t\t[Labels={LEFT|NO}]\n" +
            "\t\t[Weights={YES|NO}]\n" +
            "\t\t[Confidences={YES|NO}]\n" +
            "\t\t[Intervals={YES|NO}]\n" +
            "\t\t[ShowBothSides={NO|YES}]\n" +
            "\t;]\n" +
            "\t[Threshold=non-negative-number;]\n" +
            "\t[PROPERTIES\n" +
            "\t\t[Fit=non-negative-number]\n" +
            // "\t\t[LeastSquares]\n" + // only present for compatibility with SplitsTree4
            "\t\t[{Compatible|Cyclic|Weakly Compatible|Incompatible]\n" +
            "\t;]\n" +
            "\t[CYCLE [taxon_i_1 taxon_i_2 ... taxon_i_ntax];]\n" +
            "\t[SPLITSLABELS label_1 label_2 ... label_nsplits;]\n" +
            "MATRIX\n" +
            "[label_1] [weight_1] [confidence_1] split_1,\n" +
            "[label_2] [weight_2] [confidence_2] split_2,\n" +
            " ....\n" +
            "[label_nsplits] [weight_nsplits] [confidence_nsplits] split_nsplits[,]\n" +
            ";\n" +
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

        np.matchBeginBlock(SplitsBlock.BLOCK_NAME);
        parseTitleAndLink(np);

        final int ntax = taxaBlock.getNtax();
        np.matchIgnoreCase("dimensions nTax=" + ntax);
        int nsplits = 0;
        if (np.peekMatchIgnoreCase("nSplits=")) {
            np.matchIgnoreCase("nSplits=");
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

            format.setOptionShowBothSides(np.findIgnoreCase(formatTokens, "showBothSides=no", false, format.isOptionShowBothSides()));
            format.setOptionShowBothSides(np.findIgnoreCase(formatTokens, "showBothSides=yes", true, format.isOptionShowBothSides()));

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
            np.findIgnoreCase(p, "leastSquares", true, false);
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
            readMatrix(np, taxaBlock, nsplits, splitsBlock, format);
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
    private static void readMatrix(NexusStreamParser np, TaxaBlock taxaBlock, int nsplits, SplitsBlock splitsBlock, SplitsNexusFormat splitsNexusFormat) throws IOException {
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


            final ASplit split;

            if (splitsNexusFormat.isOptionShowBothSides()) {
                final BitSet setA = new BitSet();
                while (!np.peekMatchIgnoreCase("|")) {
                    setA.set(Integer.parseInt(np.getWordRespectCase()));
                    if (setA.cardinality() == 0 || setA.cardinality() == taxaBlock.getNtax())
                        throw new IOExceptionWithLineNumber(np.lineno(), "non-split of size " + setA.cardinality());
                }
                np.matchIgnoreCase("|");
                final BitSet setB = new BitSet();
                while (!np.peekMatchIgnoreCase(",") && !(i == nsplits && np.peekMatchIgnoreCase(";"))) {
                    setB.set(Integer.parseInt(np.getWordRespectCase()));
                    if (setB.cardinality() == 0 || setB.cardinality() == taxaBlock.getNtax())
                        throw new IOExceptionWithLineNumber(np.lineno(), "non-split of size " + setB.cardinality());
                }
                if (BitSetUtils.intersection(setA, setB).cardinality() > 0)
                    throw new IOExceptionWithLineNumber(np.lineno(), "Split sides not disjoint");
                if (BitSetUtils.compare(BitSetUtils.union(setA, setB), taxaBlock.getTaxaSet()) != 0)
                    throw new IOExceptionWithLineNumber(np.lineno(), "Union of split doesn't equal complete taxon set");

                split = new ASplit(setA, setB, weight);
            } else {
                final BitSet setA = new BitSet();
                while (!np.peekMatchIgnoreCase(",") && !(i == nsplits && np.peekMatchIgnoreCase(";"))) {
                    setA.set(Integer.parseInt(np.getWordRespectCase()));
                    if (setA.cardinality() == 0 || setA.cardinality() == taxaBlock.getNtax() || !BitSetUtils.contains(taxaBlock.getTaxaSet(), setA))
                        throw new IOExceptionWithLineNumber(np.lineno(), "Illegal split part of size" + setA.cardinality());

                }
                split = new ASplit(setA, taxaBlock.getTaxaSet(), weight);
            }
            if (np.peekMatchIgnoreCase(","))
                np.matchIgnoreCase(",");

            if (confidence != -1)
                split.setConfidence(confidence);
            if (label != null)
                split.setLabel(label);
            splitsBlock.getSplits().add(split);
        }
    }

    /**
     * is the parser at the beginning of a block that this class can parse?
     *
     * @param np
     * @return true, if can parse from here
     */
    public boolean atBeginOfBlock(NexusStreamParser np) {
        return np.peekMatchIgnoreCase("begin " + SplitsBlock.BLOCK_NAME + ";");
    }
}
