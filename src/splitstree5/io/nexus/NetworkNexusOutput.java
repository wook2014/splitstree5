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
import splitstree5.core.datablocks.NetworkBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.io.IOException;
import java.io.Writer;

/**
 * network nexus output
 * Daniel Huson, 2.2018
 */
public class NetworkNexusOutput extends NexusIOBase implements INexusOutput<NetworkBlock> {
    /**
     * write the block in nexus format
     *
     * @param w
     * @param taxaBlock
     * @param networkBlock
     * @throws IOException
     */
    @Override
    public void write(Writer w, TaxaBlock taxaBlock, NetworkBlock networkBlock) throws IOException {
        w.write("\nBEGIN " + NetworkBlock.BLOCK_NAME + ";\n");
        writeTitleAndLink(w);
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
        w.write("END; [" + NetworkBlock.BLOCK_NAME + "]\n");
    }
}
