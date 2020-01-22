/*
 *  TreesNexusOutput.java Copyright (C) 2020 Daniel H. Huson
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

import jloda.phylo.PhyloTree;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.Taxon;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * tree nexus output
 * Daniel Huson, 2.2018
 */
public class TreesNexusOutput extends NexusIOBase implements INexusOutput<TreesBlock> {
    /**
     * write a block in nexus format
     *
     * @param w
     * @param taxaBlock
     * @param treesBlock
     * @throws IOException
     */
    @Override
    public void write(Writer w, TaxaBlock taxaBlock, TreesBlock treesBlock) throws IOException {
        final TreesNexusFormat format = (TreesNexusFormat) treesBlock.getFormat();

        w.write("\nBEGIN " + TreesBlock.BLOCK_NAME + ";\n");
        writeTitleAndLink(w);
        if (treesBlock.size() > 0)
            w.write(String.format("[Number of trees: %,d]\n", treesBlock.getNTrees()));
        if (treesBlock.isPartial() || treesBlock.isRooted()) {
            w.write("PROPERTIES");
            w.write(" partialTrees=" + (treesBlock.isPartial() ? "yes" : "no"));
            w.write(" rooted=" + (treesBlock.isRooted() ? "yes" : "no"));
            w.write(";\n");
        }

        final Map<String, String> translator;
        if (format.isOptionTranslate()) {
            translator = computeTranslationName2Number(taxaBlock);
            w.write("TRANSLATE\n");

            for (int t = 1; t <= taxaBlock.getNtax(); t++) {
                w.write("\t" + t + " '" + taxaBlock.getLabel(t) + "',\n");
            }
            w.write(";\n");
        } else
            translator = computeTranslationNumber2Name(taxaBlock);

        w.write("[TREES]\n");
        int t = 1;
        for (PhyloTree tree : treesBlock.getTrees()) {
            final String name = (tree.getName() != null && tree.getName().length() > 0 ? tree.getName() : "t" + t);
            w.write("\t\t[" + (t++) + "] tree '" + name + "'=" + getFlags(tree) + " ");
            tree.write(w, format.isOptionWeights(), translator);
            w.write(";\n");
        }
        w.write("END; [" + TreesBlock.BLOCK_NAME + "]\n");
    }

    /**
     * compute translation from taxon names to number
     *
     * @param taxaBlock
     * @return translation
     */
    private static Map<String, String> computeTranslationName2Number(TaxaBlock taxaBlock) {
        final Map<String, String> translation = new HashMap<>();
        for (Taxon taxon : taxaBlock.getTaxa()) {
            translation.put(taxon.getName(), "" + taxaBlock.indexOf(taxon));
        }
        return translation;
    }

    /**
     * compute translation from taxon names to number
     *
     * @param taxaBlock
     * @return translation
     */
    private static Map<String, String> computeTranslationNumber2Name(TaxaBlock taxaBlock) {
        final Map<String, String> translation = new HashMap<>();
        for (Taxon taxon : taxaBlock.getTaxa()) {
            translation.put("" + taxaBlock.indexOf(taxon), taxon.getName());
        }
        return translation;
    }

    /**
     * Returns the nexus flag [&R] indicating whether the tree should be considered
     * as rooted
     *
     * @param tree
     * @return String  Returns [&R] if rooted, and "" otherwise.
     */
    private static String getFlags(PhyloTree tree) {
        if (tree.getRoot() != null)
            return "[&R]";
        else
            return "";
    }

}
