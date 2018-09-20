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

import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import jloda.util.IOExceptionWithLineNumber;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.imports.utils.SimpleNewickParser;

import java.io.IOException;
import java.util.*;

/**
 * nexus input parser
 * Daniel Huson, 2.2018
 */
public class TreesNexusInput extends NexusIOBase implements INexusInput<TreesBlock> {
    public static final String SYNTAX = "BEGIN " + TreesBlock.BLOCK_NAME + ";\n" +
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

    @Override
    public String getSyntax() {
        return SYNTAX;
    }

    /**
     * parse a trees block
     *
     * @param np
     * @param taxaBlock
     * @param treesBlock
     * @return taxon names, if found
     * @throws IOException
     */
    @Override
    public List<String> parse(NexusStreamParser np, TaxaBlock taxaBlock, TreesBlock treesBlock) throws IOException {
        treesBlock.clear();

        final TreesNexusFormat format = (TreesNexusFormat) treesBlock.getFormat();

        boolean rootedExplicitySet = false;

        np.matchBeginBlock(TreesBlock.BLOCK_NAME);
        parseTitleAndLink(np);

        if (np.peekMatchIgnoreCase("PROPERTIES")) {
            final List<String> tokens = np.getTokensLowerCase("PROPERTIES", ";");
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
                throw new IOExceptionWithLineNumber(np.lineno(), "'" + tokens + "' unexpected in PROPERTIES");
        }

        final Map<String, Integer> taxName2Id = new HashMap<>();
        final ArrayList<String> taxonNamesFound = new ArrayList<>();
        boolean haveSetKnownTaxonNames = false;

        // setup translator:
        final Map<String, String> translator; // maps node labels to taxon labels

        if (np.peekMatchIgnoreCase("TRANSLATE")) {
            translator = new HashMap<>();
            format.setOptionTranslate(true);
            np.matchIgnoreCase("TRANSLATE");
            while (!np.peekMatchIgnoreCase(";")) {
                final String nodeLabel = np.getWordRespectCase();
                final String taxonLabel = np.getWordRespectCase();
                taxonNamesFound.add(taxonLabel);
                taxName2Id.put(taxonLabel, taxonNamesFound.size());
                translator.put(nodeLabel, taxonLabel);

                if (!np.peekMatchIgnoreCase(";"))
                    np.matchIgnoreCase(",");
            }
            np.matchIgnoreCase(";");
            haveSetKnownTaxonNames = true;
        } else {
            translator = null;
            format.setOptionTranslate(false);
            if (taxaBlock.getTaxa().size() > 0) {
                for (int t = 1; t <= taxaBlock.getNtax(); t++) {
                    final String taxonLabel = taxaBlock.get(t).getName();
                    taxonNamesFound.add(taxonLabel);
                    taxName2Id.put(taxonLabel, t);
                }
                haveSetKnownTaxonNames = true;
            }
        }

        final Set<String> knownTaxonNames = new HashSet<>(taxonNamesFound);

        final SimpleNewickParser parser = new SimpleNewickParser();

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

            final boolean isRooted; // In SplitsTree5 we ignore this because trees are now always rooted
            if (rootedExplicitySet)
                isRooted = treesBlock.isRooted();
            else {
                String comment = np.getComment();
                isRooted = (comment != null && comment.equalsIgnoreCase("&R"));
            }

            // final PhyloTree tree = PhyloTree.valueOf(buf.toString(), isRooted);
            final PhyloTree tree = parser.parse(buf.toString());

            if (translator != null)
                tree.changeLabels(translator);

            if (hasNumbersOnInternalNodes(tree))
                changeNumbersOnInternalNodesToEdgeConfidencies(tree); // todo needs debugging

            for (Node v : tree.nodes()) {
                final String label = tree.getLabel(v);
                if (label != null && label.length() > 0) {
                    if (!knownTaxonNames.contains(label)) {
                        if (haveSetKnownTaxonNames) {
                            throw new IOException("Tree '" + name + "' contains unknown taxon: " + label);
                        } else {
                            knownTaxonNames.add(label);
                            taxonNamesFound.add(label);
                            taxName2Id.put(label, taxonNamesFound.size());
                        }
                    }
                    tree.addTaxon(v, taxName2Id.get(label));
                    //System.err.println(v+" -> "+label+" -> "+Basic.toString(tree.getTaxa(v)," "));
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
     * are there any labeled internal nodes and are all such labels numbers?
     *
     * @param tree
     * @return true, if some internal nodes labeled by numbers
     */
    public boolean hasNumbersOnInternalNodes(PhyloTree tree) {
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
    public void changeNumbersOnInternalNodesToEdgeConfidencies(PhyloTree tree) {
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

    @Override
    public boolean atBeginOfBlock(NexusStreamParser np) {
        return np.peekMatchIgnoreCase("begin " + TreesBlock.BLOCK_NAME + ";");
    }
}
