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
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.Taxon;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * input and output of a trees block in Nexus format
 * Created by huson on 12/28/16.
 */
public class TreesNexusIO {
    public static final String NAME = "TREES";

    public static final String SYNTAX = "BEGIN " + NAME + ";\n" +
            "\t[TITLE title;]\n" +
            "\t[LINK name = title;]\n" +
            "[PROPERTIES PARTIALTREES={YES|NO} ROOTED={YES|NO};]\n" +
            "[TRANSLATE\n" +
            "    nodeLabel1 taxon1,\n" +
            "    nodeLabel2 taxon2,\n" +
            "    ...\n" +
            "    nodeLabelN taxonN\n" +
            ";]\n" +
            "[TREE name1 = tree1-in-Newick-format;]\n" +
            "[TREE name2 = tree2-in-Newick-format;]\n" +
            "...\n" +
            "[TREE nameM = treeM-in-Newick-format;]\n" +
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
     * @param treesBlock
     * @return taxon names found in this block
     * @throws IOException
     */
    public static ArrayList<String> parse(NexusStreamParser np, TaxaBlock taxaBlock, TreesBlock treesBlock, @Nullable TreesNexusFormat treesNexusFormat) throws IOException {
        treesBlock.clear();
        if (treesNexusFormat == null)
            treesNexusFormat = new TreesNexusFormat();

        boolean rootedExplicitySet = false;

        np.matchBeginBlock(NAME);
        UtilitiesNexusIO.readTitleLinks(np, treesBlock);

        if (np.peekMatchIgnoreCase("PROPERTIES")) {
            final List<String> tokens = np.getTokensLowerCase("format", ";");
            treesBlock.setPartial(np.findIgnoreCase(tokens, "partialTrees=no", false, treesBlock.isPartial()));
            treesBlock.setPartial(np.findIgnoreCase(tokens, "partialTrees=yes", true, treesBlock.isPartial()));

            if (np.findIgnoreCase(tokens, "rooted=no", false, treesBlock.isRooted())) {
                treesBlock.setRooted(false);
                rootedExplicitySet = true;
            }
            if (np.findIgnoreCase(tokens, "rooted=yes", true, treesBlock.isRooted())) {
                treesBlock.setRooted(true);
                rootedExplicitySet = true;
            }

            treesBlock.setPartial(np.findIgnoreCase(tokens, "no partialTrees", false, treesBlock.isPartial()));
            treesBlock.setPartial(np.findIgnoreCase(tokens, "partialTrees", true, treesBlock.isPartial()));
            if (np.findIgnoreCase(tokens, "no rooted", false, treesBlock.isRooted())) {
                treesBlock.setRooted(false);
                rootedExplicitySet = true;
            }
            if (np.findIgnoreCase(tokens, "rooted", true, treesBlock.isRooted())) {
                treesBlock.setRooted(true);
                rootedExplicitySet = true;
            }

            if (tokens.size() != 0)
                throw new IOException("line " + np.lineno() + ": '" + tokens + "' unexpected in FORMAT");
        }

        final ArrayList<String> taxonNamesFound = new ArrayList<>();
        boolean haveSetKnownTaxonNames = false;

        // setup translator:
        final Map<String, String> translator; // maps node labels to taxon labels

        if (np.peekMatchIgnoreCase("translate")) {
            translator = new HashMap<>();
            treesNexusFormat.setTranslate(true);
            np.matchIgnoreCase("translate");
            while (!np.peekMatchIgnoreCase(";")) {
                final String nodelabel = np.getWordRespectCase();
                final String taxlabel = np.getWordRespectCase();
                taxonNamesFound.add(taxlabel);
                translator.put(nodelabel, taxlabel);

                if (!np.peekMatchIgnoreCase(";"))
                    np.matchIgnoreCase(",");
            }
            np.matchIgnoreCase(";");
            haveSetKnownTaxonNames = true;
        } else {
            translator = null;
            treesNexusFormat.setTranslate(false);
            if (taxaBlock.getTaxa().size() > 0) {
                for (Taxon taxon : taxaBlock.getTaxa()) {
                    taxonNamesFound.add(taxon.getName());
                }
                haveSetKnownTaxonNames = true;
            }
        }

        final Set<String> knownTaxonNames = new HashSet<>();
        knownTaxonNames.addAll(taxonNamesFound);

        int treeNumber = 1;
        while (np.peekMatchIgnoreCase("tree")) {
            np.matchIgnoreCase("tree");
            if (np.peekMatchRespectCase("*"))
                np.matchRespectCase("*"); // don't know why PAUP puts this star in the file....

            String name = np.getWordRespectCase();
            name = name.replaceAll("\\s+", "_");
            name = name.replaceAll("[:;,]+", ".");
            name = name.replaceAll("\\[", "(");
            name = name.replaceAll("\\]", ")");
            name = name.trim();

            if (name.length() == 0)
                name = "t" + treeNumber;

            np.matchIgnoreCase("=");
            np.getComment(); // clears comments

            final StringBuilder buf = new StringBuilder();

            final List<String> tokensToCome = np.getTokensRespectCase(null, ";");
            for (String s : tokensToCome) {
                buf.append(s);
            }

            final boolean isRooted;
            if (rootedExplicitySet)
                isRooted = treesBlock.isRooted();
            else {
                String comment = np.getComment();
                isRooted = (comment != null && comment.equalsIgnoreCase("&R"));
            }

            final PhyloTree tree = PhyloTree.valueOf(buf.toString(), isRooted);
            if (translator != null)
                tree.changeLabels(translator);

            if (hasNumbersOnInternalNodes(tree))
                changeNumbersOnInternalNodesToEdgeConfidencies(tree); // todo needs debugging

            for (Node v = tree.getFirstNode(); v != null; v = tree.getNextNode(v)) {
                final String label = tree.getLabel(v);
                if (label != null && label.length() > 0 && !knownTaxonNames.contains(label)) {
                    if (haveSetKnownTaxonNames) {
                        throw new IOException("Tree '" + name + "' contains unknown taxon: " + label);
                    } else {
                        knownTaxonNames.add(label);
                        taxonNamesFound.add(label);
                    }
                }
            }
            tree.setName(name);
            treesBlock.getTrees().add(tree);
            treeNumber++;
        }

        np.matchEndBlock();

        return taxonNamesFound;
    }


    /**
     * write a block in nexus format
     *
     * @param w
     * @param taxaBlock
     * @param treesBlock
     * @throws IOException
     */
    public static void write(Writer w, TaxaBlock taxaBlock, TreesBlock treesBlock, @Nullable TreesNexusFormat treesNexusFormat) throws IOException {
        if (treesNexusFormat == null)
            treesNexusFormat = new TreesNexusFormat();

        w.write("\nBEGIN " + NAME + ";\n");
        UtilitiesNexusIO.writeTitleLinks(w, treesBlock);
        if (treesBlock.isPartial() || treesBlock.isRooted()) {
            w.write("\tPROPERTIES");
            w.write(" partialTrees=" + (treesBlock.isPartial() ? "yes" : "no"));
            w.write(" rooted=" + (treesBlock.isRooted() ? "yes" : "no"));
            w.write(";\n");
        }

        final Map<String, String> translator;
        if (treesNexusFormat.isTranslate()) {
            translator = computeTranslationName2Number(taxaBlock);
            w.write("\tTRANSLATE\n");

            for (int t = 1; t <= taxaBlock.getNtax(); t++) {
                w.write("\t\t" + t + " '" + taxaBlock.getLabel(t) + "',\n");
            }
            w.write(";\n");
        } else
            translator = null;

        w.write("\t[TREES]\n");
        int t = 1;
        for (PhyloTree tree : treesBlock.getTrees()) {
            final String name = (tree.getName() != null && tree.getName().length() > 0 ? tree.getName() : "t" + t);
            w.write("\t\t[" + (t++) + "] tree '" + name + "'=" + getFlags(tree) + " ");
            tree.write(w, treesNexusFormat.isShowWeights(), translator);
            w.write(";\n");
        }
        w.write("END; [" + NAME + "]\n");
    }

    /**
     * Returns the nexus flag [&R] indicating whether the tree should be considered
     * as rooted
     *
     * @param tree
     * @return String  Returns [&R] if rooted, and "" otherwise.
     */
    public static String getFlags(PhyloTree tree) {
        if (tree.getRoot() != null)
            return "[&R]";
        else
            return "";
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
     * are there any labeled internal nodes and are all such labels numbers?
     *
     * @param tree
     * @return true, if some internal nodes labeled by numbers
     */
    public static boolean hasNumbersOnInternalNodes(PhyloTree tree) {
        boolean hasNumbersOnInternalNodes = false;
        for (Node v = tree.getFirstNode(); v != null; v = v.getNext()) {
            if (v.getOutDegree() != 0 && v.getInDegree() != 0) {
                String label = tree.getLabel(v);
                if (label != null) {
                    if (Basic.isDouble(label))
                        hasNumbersOnInternalNodes = true;
                    else
                        return false;
                }
            }
        }
        return hasNumbersOnInternalNodes;
    }

    /**
     * reinterpret an numerical label of an internal node as the confidence associated with the incoming edge
     *
     * @param tree
     */
    public static void changeNumbersOnInternalNodesToEdgeConfidencies(PhyloTree tree) {
        for (Node v = tree.getFirstNode(); v != null; v = v.getNext()) {
            if (v.getOutDegree() != 0 && v.getInDegree() == 1) {
                String label = tree.getLabel(v);
                if (label != null) {
                    if (Basic.isDouble(label)) {
                        tree.setConfidence(v.getFirstInEdge(), Basic.parseDouble(label));
                        tree.setLabel(v, null);
                    }
                }
            }
        }
    }
}
