/*
 * NetworkEmbedder.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.core.algorithms.views;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Tooltip;
import javafx.scene.transform.Scale;
import jloda.graph.Edge;
import jloda.graph.EdgeArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.graph.fmm.FastMultiLayerMethodLayout;
import jloda.phylo.PhyloGraph;
import jloda.util.Basic;
import jloda.util.progress.ProgressListener;
import jloda.util.StringUtils;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromNetwork;
import splitstree5.core.algorithms.interfaces.IToViewer;
import splitstree5.core.datablocks.NetworkBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TraitsBlock;
import splitstree5.core.datablocks.ViewerBlock;
import splitstree5.core.workflow.UpdateState;
import splitstree5.gui.graphtab.NetworkViewTab;
import splitstree5.gui.graphtab.base.EdgeViewBase;
import splitstree5.gui.graphtab.base.GraphLayout;
import splitstree5.gui.graphtab.base.NodeView2D;
import splitstree5.utils.Legend;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Embeds a network
 * Daniel Huson, 2.2018
 */
public class NetworkEmbedder extends Algorithm<NetworkBlock, ViewerBlock> implements IFromNetwork, IToViewer {
    public enum MutationView {Hatches, Labels, Count, None}


    private final IntegerProperty optionIterations = new SimpleIntegerProperty(1000);

    private final BooleanProperty optionShowPieCharts = new SimpleBooleanProperty();

    private final BooleanProperty optionScaleNodes = new SimpleBooleanProperty();

    private final ObjectProperty<MutationView> optionShowMutations = new SimpleObjectProperty<>(MutationView.None);

    private final BooleanProperty optionEdgesToScale = new SimpleBooleanProperty(false);

    public List<String> listOptions() {
        return Arrays.asList("optionShowPieCharts", "optionScaleNodes", "optionShowMutations", "optionEdgesToScale");
    }

    private final PhyloGraph graph = new PhyloGraph();
    private final NodeArray<NetworkBlock.NodeData> node2data = new NodeArray<>(graph);
    private final EdgeArray<NetworkBlock.EdgeData> edge2data = new EdgeArray<>(graph);

    private ChangeListener<UpdateState> changeListener;

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, NetworkBlock parent, ViewerBlock child) throws Exception {
        progress.setTasks("Network embedding", "Init.");
        final NetworkViewTab viewTab = (NetworkViewTab) child.getTab();

        Platform.runLater(() -> {
            child.getTab().setText(child.getName());
        });

        copyDataFromParent(parent, graph, node2data, edge2data);

        viewTab.init(graph);

        final NodeArray<Point2D> node2point = new NodeArray<>(graph);
        computeSpringEmbedding(graph, node2point, isOptionEdgesToScale() ? graph::getWeight : e -> 1d, viewTab.getTargetDimensions().getWidth(),
                viewTab.getTargetDimensions().getHeight());

        /* Check if the node data contains the x, y coordinates
            if present - use the x, y coordinates in view
            if not - use standard function
         */
        for (Node v : graph.nodes()) {
            if (node2data.get(v).get("x") != null & node2data.get(v).get("y") != null)
                node2point.put(v, new Point2D(Double.parseDouble(node2data.get(v).get("x")), Double.parseDouble(node2data.get(v).get("y"))));
        }

        TreeEmbedder.scaleAndCenterToFitTarget(GraphLayout.Radial, viewTab.getTargetDimensions(), node2point, true);

        progress.setProgress(100);   //set progress to 100%

        final TraitsBlock traitsBlock = taxaBlock.getTraitsBlock();

        int maxCount = -1;
        if (traitsBlock != null) {
            for (Node v : graph.nodes()) {
                for (int trait = 1; trait <= traitsBlock.getNTraits(); trait++) {
                    int count = 0;
                    for (Integer taxon : graph.getTaxa(v)) {
                        count += traitsBlock.getTraitValue(taxon, trait);
                    }
                    maxCount = Math.max(count, maxCount);
                }

            }
        } else if (isOptionScaleNodes()) {
            for (Node v : graph.nodes()) {
                maxCount = Math.max(maxCount, graph.getNumberOfTaxa(v));
            }
        }

        viewTab.setLegend(null);

        // compute all views and put their parts into the appropriate groups
        for (Node v : graph.nodes()) {
            final String text;
            final String label = graph.getLabel(v);
            if (label != null) {
                final StringBuilder buf = new StringBuilder();
                if (label.startsWith("{") && label.endsWith("}")) // multi-labeled node //todo: differentiate between multi label and html?
                {
					final String[] tokens = StringUtils.split(label.substring(1, label.length() - 1), ',');
                    for (String token : tokens) {
                        if (Basic.isInteger(token)) {
                            if (buf.length() > 0)
                                buf.append(", ");
                            buf.append(taxaBlock.get(Basic.parseInt(token)));
                        }

                    }
                } else if (Basic.isInteger(label))
                    buf.append(taxaBlock.get(Basic.parseInt(label)));
                else
                    buf.append(label);
                text = buf.toString();
            } else
                text = null;

            //String text = (graph.getLabel(v) != null ? graph.getLabel(v) : "Node " + v.getId());
            final NodeView2D nodeView = viewTab.createNodeView(v, graph.getTaxa(v), node2point.get(v), null, 0, 0, text);

            viewTab.getNode2view().put(v, nodeView);
            viewTab.getNodesGroup().getChildren().addAll(nodeView.getShapeGroup());
            viewTab.getNodeLabelsGroup().getChildren().addAll(nodeView.getLabelGroup());

            if (traitsBlock != null && traitsBlock.getNTraits() > 0 && graph.getNumberOfTaxa(v) > 0) {
                PieChart pieChart = new PieChart();
                pieChart.setLabelsVisible(false);
                pieChart.setLegendVisible(false);
                final StringBuilder buf = new StringBuilder();

                int count = 0;
                for (int trait = 1; trait <= traitsBlock.getNTraits(); trait++) {
                    for (Integer taxon : graph.getTaxa(v)) {
                        count += traitsBlock.getTraitValue(taxon, trait);
                        if (trait == 1) {
                            if (buf.length() > 0)
                                buf.append(", ");
                            buf.append(taxaBlock.get(taxon));
                        }
                    }
                    pieChart.getData().add(new PieChart.Data(traitsBlock.getTraitLabel(trait), count));
                    if (trait == 1)
                        buf.append(": ");
                    else
                        buf.append(", ");
                    buf.append(String.format("%s: %d", traitsBlock.getTraitLabel(trait), count));
                }

                if (maxCount > 0) {
                    if (getOptionShowPieCharts()) {
                        double scale = 0.1 * (isOptionScaleNodes() ? (double) count / (double) maxCount : 1);
                        pieChart.getTransforms().add(new Scale(scale, scale));
                        pieChart.layoutXProperty().bind(pieChart.widthProperty().multiply(-0.5 * scale));
                        pieChart.layoutYProperty().bind(pieChart.heightProperty().multiply(-0.5 * scale));

                        nodeView.setShape(null);
                        nodeView.getShapeGroup().getChildren().add(pieChart);
                        Tooltip.install(nodeView.getShapeGroup(), new Tooltip(buf.toString()));

                        if (viewTab.getLegend() == null) {
                            final Legend traitsLegend = new Legend();
                            for (PieChart.Data item : pieChart.getData()) {
                                Legend.LegendItem legenditem = new Legend.LegendItem(item.getName());
                                legenditem.getSymbol().getStyleClass().addAll(item.getNode().getStyleClass());
                                legenditem.getSymbol().getStyleClass().add("pie-legend-symbol");
                                traitsLegend.getItems().add(legenditem);
                            }
                            nodeView.getShapeGroup().getChildren().add(traitsLegend);
                            viewTab.setLegend(traitsLegend);
                        }
                    } else if (isOptionScaleNodes()) {
                        double scale = 18 * (double) count / (double) maxCount;
                        nodeView.setWidth(scale * nodeView.getWidth());
                        nodeView.setHeight(scale * nodeView.getHeight());
                        nodeView.setStrokeWidth(1.0 / scale * nodeView.getStrokeWidth());
                    }
                }
            } else if (isOptionScaleNodes()) { // no traits given, scale by number of nodes assigned
                double scale = 18 * (double) graph.getNumberOfTaxa(v) / maxCount;
                nodeView.setWidth(scale * nodeView.getWidth());
                nodeView.setHeight(scale * nodeView.getHeight());
                nodeView.setStrokeWidth(1.0 / scale * nodeView.getStrokeWidth());
            }
        }
        for (Edge e : graph.edges()) {
			final EdgeViewBase edgeView;

			if (StringUtils.isArrayOfIntegers(edge2data.get(e).get("sites"))) {
				final int[] mutations = StringUtils.parseArrayOfIntegers(edge2data.get(e).get("sites"));
				edgeView = viewTab.createEdgeView(e, node2point.get(e.getSource()), node2point.get(e.getTarget()), mutations, getOptionShowMutations());
			} else
				edgeView = viewTab.createEdgeView(e, node2point.get(e.getSource()), node2point.get(e.getTarget()), null);

			viewTab.getEdge2view().put(e, edgeView);
			viewTab.getEdgesGroup().getChildren().add(edgeView.getShapeGroup());
			if (edgeView.getLabel() != null)
				viewTab.getEdgeLabelsGroup().getChildren().add(edgeView.getLabel());
		}
        Platform.runLater(() -> viewTab.updateSelectionModels(graph, taxaBlock, child.getDocument()));
        child.show();

        progress.close();

        if (changeListener != null)
            getConnector().stateProperty().removeListener(changeListener);

        changeListener = (c, o, n) -> child.getTab().getCenter().setDisable(n != UpdateState.VALID);
        getConnector().stateProperty().addListener(new WeakChangeListener<>(changeListener));
    }

    /**
     * make working copy of data
     *
     * @param networkBlock
     * @param tarGraph
     * @param tarNode2Data
     * @param tarEdge2Data
     */
    private void copyDataFromParent(NetworkBlock networkBlock, PhyloGraph tarGraph, NodeArray<NetworkBlock.NodeData> tarNode2Data, EdgeArray<NetworkBlock.EdgeData> tarEdge2Data) {
        final PhyloGraph srcGraph = networkBlock.getGraph();

        tarGraph.clear();
        final NodeArray<Node> src2tar = new NodeArray<>(srcGraph);
        for (Node src : srcGraph.nodes()) {
            final Node tar = tarGraph.newNode();
            src2tar.put(src, tar);
            for (int t : srcGraph.getTaxa(src))
                tarGraph.addTaxon(tar, t);
            tarGraph.setLabel(tar, srcGraph.getLabel(src));
            tarNode2Data.put(tar, networkBlock.getNodeData(src));
        }

        for (Edge src : srcGraph.edges()) {
            final Edge tar = tarGraph.newEdge(src2tar.get(src.getSource()), src2tar.get(src.getTarget()));
            tarGraph.setWeight(tar, srcGraph.getWeight(src));
            tarGraph.setLabel(tar, srcGraph.getLabel(src));
            tarEdge2Data.put(tar, networkBlock.getEdgeData(src));
        }
    }

    public int getOptionIterations() {
        return optionIterations.get();
    }

    public IntegerProperty optionIterationsProperty() {
        return optionIterations;
    }

    public void setOptionIterations(int optionIterations) {
        this.optionIterations.set(optionIterations);
    }

    /**
     * Computes a spring embedding of the graph
     */
    public static void computeSpringEmbedding(PhyloGraph graph, NodeArray<Point2D> node2view, Function<Edge, Double> edge2weight, double width, double height) throws Exception {
        FastMultiLayerMethodLayout.apply(null, graph, edge2weight, (v, p) -> node2view.put(v, new Point2D(p.getX(), p.getY())));
    }

    public boolean getOptionShowPieCharts() {
        return optionShowPieCharts.get();
    }


    public boolean isOptionShowPieCharts() {
        return optionShowPieCharts.get();
    }

    public BooleanProperty optionShowPieChartsProperty() {
        return optionShowPieCharts;
    }

    public void setOptionShowPieCharts(boolean optionShowPieCharts) {
        this.optionShowPieCharts.set(optionShowPieCharts);
    }

    public boolean isOptionScaleNodes() {
        return optionScaleNodes.get();
    }

    public BooleanProperty optionScaleNodesProperty() {
        return optionScaleNodes;
    }

    public void setOptionScaleNodes(boolean optionScaleNodes) {
        this.optionScaleNodes.set(optionScaleNodes);
    }


    public MutationView getOptionShowMutations() {
        return optionShowMutations.get();
    }

    public ObjectProperty<MutationView> optionShowMutationsProperty() {
        return optionShowMutations;
    }

    public void setOptionShowMutations(MutationView optionShowMutations) {
        this.optionShowMutations.set(optionShowMutations);
    }

    public boolean isOptionEdgesToScale() {
        return optionEdgesToScale.get();
    }

    public BooleanProperty optionEdgesToScaleProperty() {
        return optionEdgesToScale;
    }

    public void setOptionEdgesToScale(boolean optionEdgesToScale) {
        this.optionEdgesToScale.set(optionEdgesToScale);
    }
}
