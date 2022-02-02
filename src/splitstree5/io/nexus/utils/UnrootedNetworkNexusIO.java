/*
 * UnrootedNetworkNexusIO.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.io.nexus.utils;

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloSplitsGraph;
import jloda.util.StringUtils;
import jloda.util.parse.NexusStreamParser;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * input and output for an unrooted network
 * Daniel Huson, 9.2017
 */
public class UnrootedNetworkNexusIO {
    /**
     * write an unrooted network
     *
     * @param saveTaxonIds    save taxon ids
     * @param saveSplitIds    save split ids
     * @param node2attributes optional additional node attributes
     * @param edge2attributes optional additional edge attributes
	 */
    public static void write(Writer w, PhyloSplitsGraph graph, boolean saveTaxonIds, boolean saveSplitIds, Map<Node, Map<String, String>> node2attributes, Map<Edge, Map<String, String>> edge2attributes) throws IOException {
        w.write("\n\t{GRAPH\n");
        // report nodes:
        w.write("\t\t{NODES ");
        for (Node v = graph.getFirstNode(); v != null; v = v.getNext()) {
            w.write("{" + v.getId());
            if (saveTaxonIds) {
                if (graph.hasTaxa(v)) {
					w.write(" t=" + StringUtils.toString(graph.getTaxa(v), " "));
                }
            }
            if (node2attributes != null) {
                boolean first = true;
                final Map<String, String> attributes = node2attributes.get(v);
                if (attributes != null) {
                    for (String key : attributes.keySet()) {
                        if (first) {
                            w.write(" {");
                            first = false;
                        } else
                            w.write(" ");
                        w.write(key + "='" + attributes.get(key) + "'");
                    }
                }
                if (!first)
                    w.write("}");
            }
            w.write("} ");
        }
        w.write("}\n");

        // report edges:
        w.write("\t\t{EDGES ");
        for (Edge e = graph.getFirstEdge(); e != null; e = e.getNext()) {
            w.write("{" + e.getSource().getId() + " " + e.getTarget().getId());
            if (saveSplitIds) {
                int splitId = graph.getSplit(e);
                if (splitId != 0) {
                    w.write(" s=" + splitId);
                }
            }

            if (edge2attributes != null) {
                boolean first = true;
                final Map<String, String> attributes = edge2attributes.get(e);
                if (attributes != null) {
                    for (String key : attributes.keySet()) {
                        if (first) {
                            w.write(" {");
                            first = false;
                        } else
                            w.write(" ");
                        w.write(key + "='" + attributes.get(key) + "'");
                    }
                }
                if (!first)
                    w.write("}");
            }
            w.write("} ");
        }
        w.write("}\n");
        w.write("\t}");
    }

    /**
     * parse an unrooted phylogenetic network
     *
	 */
    public static void read(NexusStreamParser np, PhyloSplitsGraph graph, Map<Node, Map<String, String>> node2attributes, Map<Edge, Map<String, String>> edge2attributes) throws IOException {
        graph.clear();

        np.matchIgnoreCase("{GRAPH");

        np.matchIgnoreCase("{NODES");
        final Map<Integer, Node> id2node = new HashMap<>();
        while (!np.peekMatchIgnoreCase("}")) {
            np.matchIgnoreCase("{");
            Node v = graph.newNode();
            id2node.put(np.getInt(), v);
            if (np.peekMatchIgnoreCase("t=")) {
                np.matchIgnoreCase("t=");
                while (!np.peekMatchAnyTokenIgnoreCase("{}")) {
                    graph.addTaxon(v, np.getInt());
                }
            }
            if (np.peekMatchIgnoreCase("{")) {
                np.matchIgnoreCase("{");
                while (!np.peekMatchAnyTokenIgnoreCase("}")) {
                    final String key = np.getWordRespectCase();
                    np.matchIgnoreCase("=");
                    final String value = np.getWordRespectCase();
                    if (node2attributes != null) {
                        Map<String, String> attributes = node2attributes.computeIfAbsent(v, k -> new HashMap<>());
                        attributes.put(key, value);
                    }
                }
                np.matchIgnoreCase("}"); // end attributes
            }
            np.matchIgnoreCase("}"); // end current node
        }
        np.matchIgnoreCase("}"); // end nodes

        np.matchIgnoreCase("{EDGES");
        while (!np.peekMatchIgnoreCase("}")) {
            np.matchIgnoreCase("{");
            final int id1 = np.getInt();
            final Node v = id2node.get(id1);
            if (v == null)
                throw new IOException("Undefined node id: " + id1);
            final int id2 = np.getInt();
            final Node w = id2node.get(id2);
            if (w == null)
                throw new IOException("Undefined node id: " + id2);
            final Edge e = graph.newEdge(v, w);

            if (np.peekMatchIgnoreCase("s=")) {
                np.matchIgnoreCase("s=");
                graph.setSplit(e, np.getInt());
            }
            if (np.peekMatchIgnoreCase("{")) {
                np.matchIgnoreCase("{");
                while (!np.peekMatchAnyTokenIgnoreCase("}")) {
                    final String key = np.getWordRespectCase();
                    np.matchIgnoreCase("=");
                    final String value = np.getWordRespectCase();
                    if (edge2attributes != null) {
                        Map<String, String> attributes = edge2attributes.computeIfAbsent(e, k -> new HashMap<>());
                        attributes.put(key, value);
                    }
                }
                np.matchIgnoreCase("}"); // end attributes
            }
            np.matchIgnoreCase("}"); // end current edge
        }
        np.matchIgnoreCase("}"); // end edges

        np.matchIgnoreCase("}"); // end graph
    }
}
