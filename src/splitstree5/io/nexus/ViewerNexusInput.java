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
import jloda.phylo.PhyloGraph;
import jloda.phylo.PhyloTree;
import jloda.phylo.SplitsGraph;
import jloda.util.Basic;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.ViewerBlock;
import splitstree5.gui.graphtab.NetworkViewTab;
import splitstree5.gui.graphtab.SplitsViewTab;
import splitstree5.gui.graphtab.TreeViewTab;
import splitstree5.gui.graphtab.base.EdgeView2D;
import splitstree5.gui.graphtab.base.Graph2DTab;
import splitstree5.gui.graphtab.base.NodeView2D;
import splitstree5.io.imports.IOExceptionWithLineNumber;
import splitstree5.io.nexus.graph.EdgeViewIO;
import splitstree5.io.nexus.graph.NodeViewIO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * viewer nexus input parser
 * Daniel Huson, 3.2018
 */
public class ViewerNexusInput extends NexusIOBase {
    public static final String SYNTAX = "BEGIN " + ViewerBlock.BLOCK_NAME + ";\n" +
            "\t[TITLE title;]\n" +
            "\t[LINK name = title;]\n" +
            "\t[DIMENSIONS NNODES=number-of-nodes NEDGES=number-of-edges [NLABELS=number-of-labels];]\n" +
            "\tFORMAT type={" + Basic.toString(ViewerBlock.Type.values(), "|") + "}\n" +
            "\tNODES\n" +
            "\t\tN: id x y [S: shape-name x y w h color] [L: label x y color font],\n" +
            "\t\t...\n" +
            "\t\tN: id x y [S: shape-name x y w h color] [L: label x y color font];\n" +
            "\tEDGES\n" +
            "\t\tE: sid tid S: svg-path w color [L: label x y color font],\n" +
            "\t\t...\n" +
            "\t\tE: sid tid S: svg-path w color [L: label x y color font];\n" +
            "\t[LABELS\n" +
            "\t\tL: label x y color font,\n" +
            "\t\t...\n" +
            "\t\tL: label x y color font;]\n" +
            "END;\n";

    public String getSyntax() {
        return SYNTAX;
    }

    /**
     * parse a trees block
     *
     * @param np
     * @param taxaBlock
     * @return taxon names, if found
     * @throws IOException
     */
    public ViewerBlock parse(NexusStreamParser np, TaxaBlock taxaBlock) throws IOException {

        np.matchBeginBlock(ViewerBlock.BLOCK_NAME);
        parseTitleAndLink(np);

        final int nNodes;
        final int nEdges;
        final int nLabels;

        if (np.peekMatchIgnoreCase("DIMENSIONS")) {
            np.matchIgnoreCase("DIMENSIONS NNODES=");
            nNodes = np.getInt();
            np.matchIgnoreCase("NEDGES=");
            nEdges = np.getInt();
            if (np.peekMatchIgnoreCase("NLABELS")) {
                np.matchIgnoreCase("NLABELS=");
                nLabels = np.getInt();
            } else
                nLabels = 0;
            np.matchIgnoreCase(";");
        } else {
            nNodes = -1;
            nEdges = -1;
            nLabels = -1;
        }

        np.matchIgnoreCase("FORMAT type=");
        final ViewerBlock.Type type = Basic.valueOfIgnoreCase(ViewerBlock.Type.class, np.getWordMatchesIgnoringCase(Basic.toString(ViewerBlock.Type.values(), " ")));
        np.matchIgnoreCase(";");
        if (type == null)
            throw new IOExceptionWithLineNumber("Unknown type", np.lineno());

        final ViewerBlock viewerBlock = ViewerBlock.create(type);

        if (!(viewerBlock.getTab() instanceof Graph2DTab))
            throw new IOExceptionWithLineNumber("Import of 3D viewer, not implemented", np.lineno());

        final Map<Integer, Node> id2node = new HashMap<>(); // maps node input ids to current ids
        {
            np.matchIgnoreCase("NODES");
            int countNodes = 0;

            switch (type) {
                case SplitsNetworkViewer: {
                    final SplitsViewTab graphTab = (SplitsViewTab) viewerBlock.getTab();
                    graphTab.init(new SplitsGraph());
                    final SplitsGraph graph = graphTab.getGraph();

                    if (!np.peekMatchIgnoreCase(";")) {
                        while (true) {
                            final NodeView2D nv = NodeViewIO.valueOf(np, graph, graphTab, id2node);
                            graphTab.getNode2view().put(nv.getNode(), nv);
                            graphTab.setupNodeView(nv);
                            graphTab.getNodesGroup().getChildren().add(nv.getShapeGroup());
                            graphTab.getNodeLabelsGroup().getChildren().add(nv.getLabelGroup());
                            countNodes++;
                            if (np.peekMatchIgnoreCase(","))
                                np.matchIgnoreCase(",");
                            else
                                break;
                        }
                    }
                    break;
                }
                case TreeViewer: {
                    final TreeViewTab graphTab = (TreeViewTab) viewerBlock.getTab();
                    graphTab.init(new PhyloTree());
                    final PhyloTree graph = graphTab.getGraph();

                    if (!np.peekMatchIgnoreCase(";")) {
                        while (true) {
                            final NodeView2D nv = NodeViewIO.valueOf(np, graph, graphTab, id2node);
                            graphTab.getNode2view().put(nv.getNode(), nv);
                            graphTab.getNodesGroup().getChildren().add(nv.getShapeGroup());
                            graphTab.getNodeLabelsGroup().getChildren().add(nv.getLabelGroup());
                            countNodes++;
                            if (np.peekMatchIgnoreCase(","))
                                np.matchIgnoreCase(",");
                            else
                                break;
                        }
                    }
                    break;
                }
                case NetworkViewer: {
                    final NetworkViewTab graphTab = (NetworkViewTab) viewerBlock.getTab();
                    graphTab.init(new PhyloGraph());
                    final PhyloGraph graph = graphTab.getGraph();

                    if (!np.peekMatchIgnoreCase(";")) {
                        while (true) {
                            final NodeView2D nv = NodeViewIO.valueOf(np, graph, graphTab, id2node);
                            graphTab.getNode2view().put(nv.getNode(), nv);
                            graphTab.getNodesGroup().getChildren().add(nv.getShapeGroup());
                            graphTab.getNodeLabelsGroup().getChildren().add(nv.getLabelGroup());
                            countNodes++;
                            if (np.peekMatchIgnoreCase(","))
                                np.matchIgnoreCase(",");
                            else
                                break;
                        }
                    }
                    break;
                }
                default:
                    throw new IOExceptionWithLineNumber("Not implemented: " + type, np.lineno());
            }
            np.matchIgnoreCase(";");
            if (nNodes != -1 && nNodes != countNodes)
                throw new IOExceptionWithLineNumber(String.format("Expected %d nodes, got %d", nNodes, countNodes), np.lineno());
        }

        np.matchIgnoreCase("EDGES");
        int countEdges = 0;
        switch (type) {
            case TreeViewer:
            case SplitsNetworkViewer:
            case NetworkViewer: {
                final Graph2DTab graphTab = (Graph2DTab) viewerBlock.getTab();
                final PhyloGraph graph = graphTab.getGraph();

                if (!np.peekMatchIgnoreCase(";")) {
                    while (true) {
                        final EdgeView2D ev = EdgeViewIO.valueOf(np, graph, graphTab, id2node);
                        graphTab.getEdge2view().put(ev.getEdge(), ev);
                        graphTab.getEdgesGroup().getChildren().add(ev.getShapeGroup());
                        graphTab.getEdgeLabelsGroup().getChildren().add(ev.getLabelGroup());
                        countEdges++;
                        if (np.peekMatchIgnoreCase(","))
                            np.matchIgnoreCase(",");
                        else
                            break;
                    }
                }
            }
            np.matchIgnoreCase(";");
        }
        if (nEdges != -1 && nEdges != countEdges)
            throw new IOExceptionWithLineNumber(String.format("Expected %d edges, got %d", nEdges, countEdges), np.lineno());

        np.matchEndBlock();

        switch (type) {
            case TreeViewer: {
                final TreeViewTab graphTab = (TreeViewTab) viewerBlock.getTab();
                graphTab.updateSelectionModels(graphTab.getGraph(), taxaBlock, taxaBlock.getDocument());
                break;
            }
            case SplitsNetworkViewer: {
                final SplitsViewTab graphTab = (SplitsViewTab) viewerBlock.getTab();
                graphTab.updateSelectionModels(graphTab.getGraph(), taxaBlock, taxaBlock.getDocument());
                break;
            }
        }

        return viewerBlock;
    }

    public boolean atBeginOfBlock(NexusStreamParser np) {
        return np.peekMatchIgnoreCase("begin " + ViewerBlock.BLOCK_NAME + ";");
    }
}