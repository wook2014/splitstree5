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

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloGraph;
import jloda.util.Basic;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.NetworkBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.imports.IOExceptionWithLineNumber;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class NetworkNexusIO {
    public static final String NAME = "NETWORK";

    public static final String SYNTAX = "BEGIN " + NAME + ";\n" +
            "\t[TITLE title;]\n" +
            "\t[LINK name = title;]\n" +
            "\t[DIMENSIONS [NNODES=number-of-nodes] [NEDGES=number-of-edges];]\n" +
            "\t\t[NETWORK={" + Basic.toString(NetworkBlock.Type.values(), "|") + "};]\n" +
            "\t[FORMAT\n" +
            "\t;]\n" +
            "\t[PROPERTIES\n" +
            "\t;]\n" +
            "\tNODES\n" +
            "\t\tID=number [LABEL=label] [x=number] [y=number] [key=value ...],\n" +
            "\t\t...\n" +
            "\t\tID=number [LABEL=label] [x=number] [y=number] [key=value ...]\n" +
            "\t;\n" +
            "\tEDGES\n" +
            "\t\tID=number SID=number TID=number [LABEL=label] [key=value ...],\n" +
            "\t\t...\n" +
            "\t\tID=number SID=number TID=number [LABEL=label] [key=value ...]\n" +
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
     * parse a network block
     *
     * @param np
     * @param taxaBlock
     * @param networkBlock
     * @return taxon names found in this block
     * @throws IOException
     */
    public static ArrayList<String> parse(NexusStreamParser np, TaxaBlock taxaBlock, NetworkBlock networkBlock) throws IOException {
        networkBlock.clear();

        final ArrayList<String> taxonNamesFound = new ArrayList<>();

        np.matchBeginBlock(NAME);
        UtilitiesNexusIO.readTitleLinks(np, networkBlock);

        np.matchIgnoreCase("dimensions nNodes=");
        final int nNodes = np.getInt(0, Integer.MAX_VALUE);
        np.matchIgnoreCase("nEdges=");
        final int nEdges = np.getInt(0, Integer.MAX_VALUE);
        np.matchIgnoreCase(";");

        if (np.peekMatchIgnoreCase("TYPE")) {
            np.matchIgnoreCase("TYPE=");
            String typeString = np.getWordRespectCase().toUpperCase();
            NetworkBlock.Type type = Basic.valueOfIgnoreCase(NetworkBlock.Type.class, typeString);
            if (type == null)
                throw new IOExceptionWithLineNumber("Unknown network type: " + typeString, np.lineno());
            networkBlock.setNetworkType(type);
            np.matchIgnoreCase(";");
        }

        if (np.peekMatchIgnoreCase("FORMAT")) {
            np.matchIgnoreCase("FORMAT");
            np.matchIgnoreCase(":");
        }

        if (np.peekMatchIgnoreCase("PROPERTIES")) {
            np.matchIgnoreCase("PROPERTIES");
            np.matchIgnoreCase(":");
        }

        final PhyloGraph graph = networkBlock.getGraph();

        final Map<Integer, Node> id2node = new TreeMap<>();

        np.matchIgnoreCase("NODES");
        for (int i = 0; i < nNodes; i++) {
            Node v = graph.newNode();

            np.matchIgnoreCase("id=");
            final int id = np.getInt();
            if (id2node.containsKey(id))
                throw new IOExceptionWithLineNumber("Multiple occurrence of node id: " + id, np.lineno());
            id2node.put(id, v);

            if (np.peekMatchIgnoreCase("label")) {
                np.matchIgnoreCase("label=");
                graph.setLabel(v, np.getWordRespectCase());
                if (taxaBlock.getLabels().size() == 0) {
                    taxonNamesFound.add(graph.getLabel(v));
                }
            }
            while (!np.peekMatchAnyTokenIgnoreCase(", ;")) {
                String key = np.getWordRespectCase();
                np.matchIgnoreCase("=");
                String value = np.getWordRespectCase();
                networkBlock.getNodeData(v).put(key, value);
            }
        }
        np.matchIgnoreCase(";");


        final Map<Integer, Edge> id2edge = new TreeMap<>();

        np.matchIgnoreCase("EDGES");
        for (int i = 0; i < nEdges; i++) {

            np.matchIgnoreCase("id=");
            final int id = np.getInt();
            if (id2edge.containsKey(id))
                throw new IOExceptionWithLineNumber("Multiple occurrence of edge id: " + id, np.lineno());

            np.matchIgnoreCase("sid=");
            final int sid = np.getInt();
            if (!id2node.containsKey(sid))
                throw new IOExceptionWithLineNumber("Unknown node id: " + sid, np.lineno());


            np.matchIgnoreCase("tid=");
            final int tid = np.getInt();
            if (!id2node.containsKey(tid))
                throw new IOExceptionWithLineNumber("Unknown node id: " + tid, np.lineno());

            final Node source = id2node.get(sid);
            final Node target = id2node.get(tid);

            final Edge e = graph.newEdge(source, target);
            id2edge.put(id, e);

            if (np.peekMatchIgnoreCase("label")) {
                np.matchIgnoreCase("label=");
                graph.setLabel(e, np.getWordRespectCase());
            }
            while (!np.peekMatchAnyTokenIgnoreCase(", ;")) {
                String key = np.getWordRespectCase();
                np.matchIgnoreCase("=");
                String value = np.getWordRespectCase();
                networkBlock.getEdgeData(e).put(key, value);
            }
        }
        np.matchIgnoreCase(";");


        np.matchEndBlock();

        return taxonNamesFound;

    }

    /**
     * write a block in nexus format
     *
     * @param w
     * @param taxaBlock
     * @param networkBlock
     * @throws IOException
     */
    public static void write(Writer w, TaxaBlock taxaBlock, NetworkBlock networkBlock) throws IOException {
        w.write("\nBEGIN " + NAME + ";\n");
        UtilitiesNexusIO.writeTitleLinks(w, networkBlock);
        w.write("\tDIMENSIONS nNodes=" + networkBlock.getNumberOfNodes() + " nEdges=" + networkBlock.getNumberOfEdges() + ";\n");

        w.write("\tTYPE=" + networkBlock.getNetworkType() + ";\n");

        final PhyloGraph graph = networkBlock.getGraph();
        // format?

        // properties?

        {
            w.write("\tNODES\n");
            boolean first = true;
            for (Node v : graph.nodes()) {
                if (first)
                    first = false;
                else
                    w.write(",\n");
                w.write("\t\tid=" + v.getId());
                if (graph.getLabel(v) != null && graph.getLabel(v).trim().length() > 0) {
                    w.write(" label='" + graph.getLabel(v).trim() + "'");
                }
                for (String key : networkBlock.getNodeData(v).keySet()) {
                    w.write(" " + key + "='" + networkBlock.getNodeData(v).get(key) + "'");
                }
            }
            w.write("\n\t;\n");
        }

        {
            w.write("\tEDGES\n");
            boolean first = true;
            for (Edge e : graph.edges()) {
                if (first)
                    first = false;
                else
                    w.write(",\n");
                w.write("\t\tid=" + e.getId());
                w.write(" sid=" + e.getSource().getId());
                w.write(" tid=" + e.getTarget().getId());

                if (graph.getLabel(e) != null && graph.getLabel(e).trim().length() > 0) {
                    w.write(" label='" + graph.getLabel(e).trim() + "'");
                }
                for (String key : networkBlock.getEdgeData(e).keySet()) {
                    w.write(" " + key + "='" + networkBlock.getEdgeData(e).get(key) + "'");
                }
            }
            w.write("\n\t;\n");
        }
        w.write("END; [" + NAME + "]\n");
    }
}
