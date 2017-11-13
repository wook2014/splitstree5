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

/*
 *  Copyright (C) 2017 Daniel H. Huson
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

package splitstree5.xtra;

import com.sun.istack.internal.Nullable;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.Taxon;
import splitstree5.io.nexus.TreesNexusFormat;
import splitstree5.io.nexus.UtilitiesNexusIO;
import splitstree5.io.nexus.utils.UnrootedNetworkNexusIO;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * input and output of a trees block in Nexus format
 * Daniel Huson, 7.2017
 */
public class SplitNetworksNexusIO {
    public static final String NAME = "ST_NETWORKS";

    public static final String SYNTAX = "BEGIN " + NAME + ";\n" +
            "\t[TITLE title;]\n" +
            "\t[LINK name = title;]\n" +
            "[PARTIAL={YES|NO};]\n" +
            "[TRANSLATE\n" +
            "    id1 taxon1,\n" +
            "    id2 taxon2,\n" +
            "    ...\n" +
            "    idN taxonN\n" +
            ";]\n" +
            "[NETWORK name1 = network1;]\n" +
            "[NETWORK name2 = network2;]\n" +
            "...\n" +
            "[NETWORK nameM = networkM;]\n" +
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
     * parse a trees block
     *
     * @param np
     * @param taxaBlock
     * @param networksBlock
     * @return taxon names found in this block
     * @throws IOException
     */
    public static ArrayList<String> parse(NexusStreamParser np, TaxaBlock taxaBlock, SplitNetworksBlock networksBlock, @Nullable TreesNexusFormat treesNexusFormat) throws IOException {
        networksBlock.clear();
        boolean rootedExplicitySet = false;

        np.matchBeginBlock(NAME);
        UtilitiesNexusIO.readTitleLinks(np, networksBlock);

        if (np.peekMatchIgnoreCase("FORMAT")) {
            final List<String> tokens = np.getTokensLowerCase("format", ";");
            networksBlock.setPartial(np.findIgnoreCase(tokens, "partial=no", false, networksBlock.isPartial()));
            networksBlock.setPartial(np.findIgnoreCase(tokens, "partial=yes", true, networksBlock.isPartial()));
            networksBlock.setPartial(np.findIgnoreCase(tokens, "partialNetworks=no", false, networksBlock.isPartial()));
            networksBlock.setPartial(np.findIgnoreCase(tokens, "partialNetworks=yes", true, networksBlock.isPartial()));

            if (tokens.size() != 0)
                throw new IOException("line " + np.lineno() + ": '" + tokens + "' unexpected in FORMAT");
        }

        final Set<String> knownTaxonNames = new HashSet<>();

        // setup translator:
        final Map<Integer, String> translator; // maps node ids to taxon labels

        if (np.peekMatchIgnoreCase("translate")) {
            translator = new HashMap<>();
            np.matchIgnoreCase("translate");
            boolean first = true;
            while (!np.peekMatchIgnoreCase(";")) {
                if (first)
                    first = false;
                else
                    np.matchIgnoreCase(",");
                final Integer nodeId = np.getInt();
                final String taxonLabel = np.getWordRespectCase();
                knownTaxonNames.add(taxonLabel);
                translator.put(nodeId, taxonLabel);
            }
            np.matchIgnoreCase(";");
        } else {
            translator = null; // assume that nodes are labeled by taxon names
        }

        int treeNumber = 1;
        while (np.peekMatchIgnoreCase("network")) {
            np.matchIgnoreCase("network");
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

            final PhyloTree graph = new PhyloTree();
            graph.setName(name);

            UnrootedNetworkNexusIO.read(np, graph, null, null);
            if (translator != null) {
                for (Node v = graph.getFirstNode(); v != null; v = v.getNext()) {
                    List<Integer> taxa = graph.getNode2Taxa(v);
                    if (taxa != null && taxa.size() > 0) {
                        ArrayList<Integer> newTaxa = new ArrayList<>();
                        for (Integer taxonId : taxa) {
                            if (translator.containsKey(taxonId)) {
                                taxonId = taxaBlock.indexOf(translator.get(taxonId));
                            }
                            newTaxa.add(taxonId);
                        }
                        if (!taxa.equals(newTaxa)) {
                            taxa.clear();
                            taxa.addAll(newTaxa);
                        }
                    }
                }
            }
            np.matchIgnoreCase(";");
            networksBlock.getNetworks().add(graph);
            treeNumber++;
        }

        np.matchEndBlock();

        return new ArrayList<>(knownTaxonNames);
    }

    /**
     * write a block in nexus format
     *
     * @param w
     * @param taxaBlock
     * @param networksBlock
     * @throws IOException
     */
    public static void write(Writer w, TaxaBlock taxaBlock, SplitNetworksBlock networksBlock, TreesNexusFormat treesNexusFormat) throws IOException {
        if (treesNexusFormat == null)
            treesNexusFormat = new TreesNexusFormat();

        w.write("\nBEGIN " + NAME + ";\n");
        UtilitiesNexusIO.writeTitleLinks(w, networksBlock);
        if (networksBlock.isPartial()) {
            w.write("\tFORMAT");
            w.write(" partial=" + (networksBlock.isPartial() ? "yes" : "no"));
            w.write(";\n");
        }

        if (treesNexusFormat.isTranslate()) {
            w.write("\tTRANSLATE\n");
            for (int t = 1; t <= taxaBlock.getNtax(); t++) {
                w.write("\t\t" + t + " '" + taxaBlock.getLabel(t) + "'");
                if (t < taxaBlock.getNtax())
                    w.write(",");
                w.write("\n");
            }
            w.write("\t;\n");
        }

        w.write("\t[NETWORKS]\n");
        int t = 1;
        for (PhyloTree graph : networksBlock.getNetworks()) {
            final String name = (graph.getName() != null && graph.getName().length() > 0 ? graph.getName() : "t" + t);
            w.write("[" + (t++) + "] network '" + name + "'=");

            UnrootedNetworkNexusIO.write(w, graph, true, true, null, null);
            w.write(";\n");
        }
        w.write("END; [" + NAME + "]\n");
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
}
