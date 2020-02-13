/*
 * ViewerNexusOutput.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package splitstree5.io.nexus;

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloGraph;
import jloda.phylo.PhyloSplitsGraph;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.ViewerBlock;
import splitstree5.gui.graphtab.base.EdgeView2D;
import splitstree5.gui.graphtab.base.GraphTabBase;
import splitstree5.gui.graphtab.base.NodeView2D;
import splitstree5.gui.graphtab.base.PolygonView2D;
import splitstree5.io.nexus.graph.EdgeViewIO;
import splitstree5.io.nexus.graph.NodeViewIO;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 * viewer nexus output
 * Daniel Huson, 3.2018
 */
public class ViewerNexusOutput extends NexusIOBase implements INexusOutput<ViewerBlock> {
    /**
     * write a block in nexus format
     *
     * @param w
     * @param taxaBlock
     * @param viewerBlock
     * @throws IOException
     */
    @Override
    public void write(Writer w, TaxaBlock taxaBlock, ViewerBlock viewerBlock) throws IOException {
        w.write("\nBEGIN " + ViewerBlock.BLOCK_NAME + ";\n");
        writeTitleAndLink(w);

        final GraphTabBase graphTab = viewerBlock.getTab();
        final PhyloGraph graph = (graphTab.getGraph() != null ? graphTab.getGraph() : new PhyloGraph());

        int nLabels = 0; // if we ever implement floating labels, then this must be set to their number

        w.write(String.format("DIMENSIONS nNodes=%d nEdges=%d", graph.getNumberOfNodes(), graph.getNumberOfEdges()));
        if (nLabels > 0)
            w.write(" nLabels=" + nLabels);
        if (graphTab.getPolygons().size() > 0)
            w.write(" nLoops=" + graphTab.getPolygons().size());
        w.write(";\n");

        w.write(String.format("FORMAT type=%s;\n", viewerBlock.getType().toString()));

        // nodes:
        w.write("NODES\n");
        {
            boolean first = true;
            for (Node node : graph.nodes()) {
                if (graphTab.getNode2view().get(node) instanceof NodeView2D) {
                    if (first)
                        first = false;
                    else
                        w.write(",\n");
                    NodeView2D nodeView2D = (NodeView2D) graphTab.getNode2view().get(node);
                    w.write("\t" + NodeViewIO.toString(nodeView2D));
                } else {
                    System.err.println("Not implemented");
                    break;
                }
            }
            w.write(";\n");
        }
        // edges
        w.write("EDGES\n");
        {
            boolean first = true;
            for (Edge edge : graph.edges()) {
                if (graphTab.getEdge2view().get(edge) instanceof EdgeView2D) {
                    if (first)
                        first = false;
                    else
                        w.write(",\n");
                    EdgeView2D ev = (EdgeView2D) graphTab.getEdge2view().get(edge);
                    w.write("\t" + EdgeViewIO.toString(ev));
                    if (graph instanceof PhyloSplitsGraph) {
                        final int s = ((PhyloSplitsGraph) graph).getSplit(edge);
                        if (s > 0)
                            w.write(" I: " + s);
                    }
                } else {
                    System.err.println("Not implemented");
                    break;
                }
            }
            w.write(";\n");
        }

        if (viewerBlock.getType() == ViewerBlock.Type.SplitsNetworkViewer) {
            if (graphTab.getPolygons().size() > 0) {
                w.write("LOOPS\n");
                {
                    final ArrayList<PolygonView2D> polygons = graphTab.getPolygons();
                    for (int i = 0; i < polygons.size(); i++) {
                        final PolygonView2D polygonView2D = polygons.get(i);
                        if (i > 0)
                            w.write(",\n");
                        w.write("\t");
                        boolean first = true;
                        for (Node v : polygonView2D.getNodes()) {
                            if (first)
                                first = false;
                            else
                                w.write(" ");
                            w.write("" + v.getId());
                        }
                    }
                }
                w.write(";\n");
            }
        }

        w.write("END; [" + ViewerBlock.BLOCK_NAME + "]\n");
    }
}
