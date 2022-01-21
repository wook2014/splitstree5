/*
 * ViewerNexusInput.java Copyright (C) 2022 Daniel H. Huson
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

package splitstree5.io.nexus;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import jloda.graph.Node;
import jloda.phylo.PhyloGraph;
import jloda.phylo.PhyloSplitsGraph;
import jloda.phylo.PhyloTree;
import jloda.util.IOExceptionWithLineNumber;
import jloda.util.StringUtils;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.ViewerBlock;
import splitstree5.gui.graphtab.NetworkViewTab;
import splitstree5.gui.graphtab.SplitsViewTab;
import splitstree5.gui.graphtab.TreeViewTab;
import splitstree5.gui.graphtab.base.EdgeView2D;
import splitstree5.gui.graphtab.base.Graph2DTab;
import splitstree5.gui.graphtab.base.NodeView2D;
import splitstree5.gui.graphtab.base.PolygonView2D;
import splitstree5.io.nexus.graph.EdgeViewIO;
import splitstree5.io.nexus.graph.NodeViewIO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * viewer nexus input parser
 * Daniel Huson, 3.2018
 */
public class ViewerNexusInput extends NexusIOBase {
	public static final String SYNTAX = "BEGIN " + ViewerBlock.BLOCK_NAME + ";\n" +
										"\t[TITLE {title};]\n" +
										"\t[LINK {type} = {title};]\n" +
										"\t[DIMENSIONS NNODES=number-of-nodes NEDGES=number-of-edges [NLABELS=number-of-labels] [NLOOPS=number-of-polygons];]\n" +
										"\tFORMAT type={" + StringUtils.toString(ViewerBlock.Type.values(), "|") + " [alignLeafLabels]\n" +
										"\tNODES\n" +
										"\t\tN: id x y [S: shape-name x y w h color] [L: label x y color font],\n" +
										"\t\t...\n" +
										"\t\tN: id x y [S: shape-name x y w h color] [L: label x y color font];\n" +
										"\tEDGES\n" +
										"\t\tE: sid tid S: svg-path w color [L: label x y color font] [I: split-id],\n" +
										"\t\t...\n" +
										"\t\tE: sid tid S: svg-path w color [L: label x y color font]  [I: split-id];\n" +
										"\t[LABELS\n" +
										"\t\tL: label x y color font,\n" +
										"\t\t...\n" +
										"\t\tL: label x y color font;]\n" +
										"\t{LOOPS\n" +
										"\t\tnode-id node-id...,\n" +
										"\t\t...\n" +
										"\t\tnode-id node-id;]\n" +
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
        final int nLoops;

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
            if (np.peekMatchIgnoreCase("NLOOPS")) {
                np.matchIgnoreCase("NLOOPS=");
                nLoops = np.getInt();
            } else
                nLoops = 0;

            np.matchIgnoreCase(";");
        } else {
            nNodes = -1;
            nEdges = -1;
            nLabels = -1;
            nLoops = -1;
        }

        np.matchIgnoreCase("FORMAT type=");
		final ViewerBlock.Type type = StringUtils.valueOfIgnoreCase(ViewerBlock.Type.class, np.getWordMatchesIgnoringCase(StringUtils.toString(ViewerBlock.Type.values(), " ")));
		boolean alignLeafLabels;
        if (np.peekMatchIgnoreCase("alignLeafLabels")) {
            np.matchIgnoreCase("alignLeafLabels");
            alignLeafLabels = true;
        } else
            alignLeafLabels = false;
        np.matchIgnoreCase(";");

        if (type == null)
            throw new IOExceptionWithLineNumber("Unknown type", np.lineno());

        final ViewerBlock viewerBlock = ViewerBlock.create(type);

        if (!(viewerBlock.getTab() instanceof Graph2DTab))
            throw new IOExceptionWithLineNumber("Import of 3D viewer, not implemented", np.lineno());

        final Map<Integer, Node> inputId2Node = new HashMap<>(); // maps input node ids to current nodes
        {
            np.matchIgnoreCase("NODES");
            int countNodes = 0;

            switch (type) {
                case SplitsNetworkViewer: {
                    final SplitsViewTab graphTab = (SplitsViewTab) viewerBlock.getTab();
                    graphTab.init(new PhyloSplitsGraph());
                    final PhyloSplitsGraph graph = graphTab.getGraph();

                    if (!np.peekMatchIgnoreCase(";")) {
                        while (true) {
                            final NodeView2D nv = NodeViewIO.valueOf(np, graph, graphTab, inputId2Node);

                            // older files don't explicitly contain taxon ids, so need try to infer them here:
                            final Node v = graph.getLastNode();
                            if (!graph.getTaxa(v).iterator().hasNext() && nv.getLabel() != null && taxaBlock.indexOf(nv.getLabel().getRawText()) != -1) {
                                nv.getWorkingTaxa().set(taxaBlock.indexOf(nv.getLabel().getRawText()));
                            }

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
                    final var graphTab = (TreeViewTab) viewerBlock.getTab();
                    graphTab.init(new PhyloTree());
                    final var tree = graphTab.getGraph();

                    if (!np.peekMatchIgnoreCase(";")) {
                        while (true) {
                            final var nv = NodeViewIO.valueOf(np, tree, graphTab, inputId2Node);

                            // older files don't explicitly contain taxon ids, so need try to infer them here:
                            final var v = tree.getLastNode();

                            if (!tree.getTaxa(v).iterator().hasNext() && nv.getLabel() != null && taxaBlock.indexOf(nv.getLabel().getRawText()) != -1) {
                                nv.getWorkingTaxa().set(taxaBlock.indexOf(nv.getLabel().getRawText()));
                            }

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
                    final var graphTab = (NetworkViewTab) viewerBlock.getTab();
                    graphTab.init(new PhyloGraph());
                    final var graph = graphTab.getGraph();

                    if (!np.peekMatchIgnoreCase(";")) {
                        while (true) {
                            final var nv = NodeViewIO.valueOf(np, graph, graphTab, inputId2Node);

                            // older files don't explicitly contain taxon ids, so need try to infer them here:
                            final var v = graph.getLastNode();
                            if (!graph.getTaxa(v).iterator().hasNext() && nv.getLabel() != null && taxaBlock.indexOf(nv.getLabel().getRawText()) != -1) {
                                nv.getWorkingTaxa().set(taxaBlock.indexOf(nv.getLabel().getRawText()));
                            }

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
                        final EdgeView2D ev = EdgeViewIO.valueOf(np, graph, graphTab, inputId2Node);
                        graphTab.getEdge2view().put(ev.getEdge(), ev);
                        graphTab.getEdgesGroup().getChildren().add(ev.getShapeGroup());
                        graphTab.getEdgeLabelsGroup().getChildren().add(ev.getLabelGroup());

                        if (graph instanceof PhyloSplitsGraph && np.peekMatchIgnoreCase("I:")) {
                            np.matchIgnoreCase("I:");
                            final int s = np.getInt(0, graph.getNumberOfNodes());
                            if (s > 0)
                                ((PhyloSplitsGraph) graph).setSplit(ev.getEdge(), s);
                        }

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

        if (alignLeafLabels) {
            if (type == ViewerBlock.Type.TreeViewer) {
                final var graphTab = (TreeViewTab) viewerBlock.getTab();
                graphTab.alignLeafLabelsProperty().set(alignLeafLabels);
                final var tree = graphTab.getGraph();

                tree.nodeStream().filter(v -> v.getOutDegree() == 0).forEach(v -> {
                    try {
                        var nv = graphTab.getNode2view().get(v);
                        var line = new Line();
                        line.startXProperty().bind(nv.getShapeGroup().translateXProperty().add(2));
                        line.startYProperty().bind(nv.getShapeGroup().translateYProperty());
                        line.endXProperty().bind(nv.getLabel().translateXProperty().subtract(2));
                        line.endYProperty().bind(nv.getShapeGroup().translateYProperty());
                        line.getStrokeDashArray().addAll(2d, 4d);
                        line.setStroke(Color.GRAY);
                        nv.getLabelGroup().getChildren().add(line);
                    } catch (Exception ignored) {
                    }
                });
            }
        }

        if (type == ViewerBlock.Type.SplitsNetworkViewer && np.peekMatchIgnoreCase("LOOPS")) {
            np.matchIgnoreCase("LOOPS");
            int countLoops = 0;
            while (!np.peekMatchIgnoreCase(";")) {
                final ArrayList<Node> loop = new ArrayList<>();
                while (!np.peekMatchIgnoreCase(",") && !np.peekMatchIgnoreCase(";")) {
                    loop.add(inputId2Node.get(np.getInt()));
                }
                if (loop.size() > 0) {
                    countLoops++;
                    final Graph2DTab graphTab = (Graph2DTab) viewerBlock.getTab();
                    final PolygonView2D polygon = new PolygonView2D(loop, graphTab.getNode2view());
                    graphTab.getPolygons().add(polygon);
                    graphTab.getEdgesGroup().getChildren().add(polygon.getShape());
                }
                if (np.peekMatchIgnoreCase(","))
                    np.matchIgnoreCase(",");
            }
            np.matchIgnoreCase(";");
            if (nLoops != -1 && nLoops != countLoops)
                throw new IOExceptionWithLineNumber(String.format("Expected %d loops, got %d", nLoops, countLoops), np.lineno());
        }

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