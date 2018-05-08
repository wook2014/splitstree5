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

import jloda.util.Basic;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ASplit;

import java.io.IOException;
import java.io.Writer;

/**
 * splits nexus output
 * Daniel Huson, 2.2018
 */
public class SplitsNexusOutput extends NexusIOBase implements INexusOutput<SplitsBlock> {
    /**
     * write a block in nexus format
     *
     * @param w
     * @param taxaBlock
     * @param splitsBlock
     * @throws IOException
     */
    @Override
    public void write(Writer w, TaxaBlock taxaBlock, SplitsBlock splitsBlock) throws IOException {
        final SplitsNexusFormat format = (SplitsNexusFormat) splitsBlock.getFormat();

        final int ntax = taxaBlock.getNtax();
        final int nsplits = splitsBlock.getNsplits();

        w.write("\nBEGIN " + SplitsBlock.BLOCK_NAME + ";\n");
        writeTitleAndLink(w);
        w.write("DIMENSIONS ntax=" + ntax + " nsplits=" + nsplits + ";\n");

        w.write("FORMAT");
        if (format.isOptionLabels())
            w.write(" labels=left");
        else
            w.write(" labels=no");
        if (format.isOptionWeights())
            w.write(" weights=yes");
        else
            w.write(" weights=no");
        if (format.isOptionConfidences())
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
            if (format.isOptionLabels()) {
                String lab = split.getLabel();
                w.write(" '" + lab + "'" + " \t");
            }
            if (format.isOptionWeights()) {
                w.write(" " + split.getWeight() + " \t");
            }
            if (format.isOptionConfidences()) {
                w.write(" " + split.getConfidence() + " \t");
            }
            w.write(" " + Basic.toString(split.getA(), " ") + ",\n");
        }
        w.write(";\n");
        w.write("END; [" + SplitsBlock.BLOCK_NAME + "]\n");
    }
}
