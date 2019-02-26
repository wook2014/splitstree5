/*
 *  Copyright (C) 2019 Daniel H. Huson
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
package splitstree5.core.algorithms.views;

import com.sun.javafx.charts.Legend;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Tooltip;
import javafx.scene.transform.Scale;
import jloda.graph.*;
import jloda.phylo.PhyloGraph;
import jloda.util.Basic;
import jloda.util.ProgressListener;
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

import java.util.Arrays;
import java.util.List;

/**
 * Embeds a network
 * Daniel Huson, 2.2018
 */
public class NetworkEmbedder extends Algorithm<NetworkBlock, ViewerBlock> implements IFromNetwork, IToViewer {
    public enum MutationView {Hatches, Labels, Count, None}
    public enum Algorithm {SpringEmbedder}

    private final ObjectProperty<Algorithm> optionAlgorithm = new SimpleObjectProperty<>(Algorithm.SpringEmbedder);

    private final IntegerProperty optionIterations = new SimpleIntegerProperty(1000);

    private final BooleanProperty optionShowPieCharts = new SimpleBooleanProperty();

    private final BooleanProperty optionScaleNodes = new SimpleBooleanProperty();

    private final ObjectProperty<MutationView> optionShowMutations = new SimpleObjectProperty<>(MutationView.None);

    public List<String> listOptions() {
        return Arrays.asList("optionAlgorithm", "optionIterations", "optionShowPieCharts", "optionScaleNodes", "optionShowMutations");
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
            child.getTab().setName(child.getName());
        });

        copyDataFromParent(parent, graph, node2data, edge2data);

        viewTab.init(graph);

        final NodeArray<Point2D> node2point = new NodeArray<>(graph);
        computeSpringEmbedding(graph, node2point, getOptionIterations(), viewTab.getTargetDimensions().getWidth(), viewTab.getTargetDimensions().getHeight(), false);

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
                if (label.startsWith("<")) // multi-labeled node
                {
                    final String[] tokens = Basic.split(label.substring(1, label.length() - 1), ',');
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
            final NodeView2D nodeView = viewTab.createNodeView(v, node2point.getValue(v), null, 0, 0, text);

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

            if (Basic.isArrayOfIntegers(edge2data.get(e).get("sites"))) {
                final int[] mutations = Basic.parseArrayOfIntegers(edge2data.get(e).get("sites"));
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

    public Algorithm getOptionAlgorithm() {
        return optionAlgorithm.get();
    }

    public ObjectProperty<Algorithm> optionAlgorithmProperty() {
        return optionAlgorithm;
    }

    public void setOptionAlgorithm(Algorithm optionAlgorithm) {
        this.optionAlgorithm.set(optionAlgorithm);
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
     *
     * @param iterations       the number of optionIterations used
     * @param startFromCurrent use current node positions
     */
    public void computeSpringEmbedding(PhyloGraph graph, NodeArray<Point2D> node2view, int iterations, double width, double height, boolean startFromCurrent) {
        if (graph.getNumberOfNodes() < 2)
            return;

        // Initial positions are on a circle:
        final NodeDoubleArray xPos = new NodeDoubleArray(graph);
        final NodeDoubleArray yPos = new NodeDoubleArray(graph);

        int i = 0;
        for (Node v : graph.nodes()) {
            if (startFromCurrent) {
                Point2D p = node2view.get(v);
                xPos.set(v, p.getX());
                yPos.set(v, p.getY());
            } else {
                xPos.set(v, 1000 * Math.sin(6.24 * i / graph.getNumberOfNodes()));
                yPos.set(v, 1000 * Math.cos(6.24 * i / graph.getNumberOfNodes()));
                i++;
            }
        }

        // run optionIterations of spring embedding:
        double log2 = Math.log(2);
        for (int count = 1; count < iterations; count++) {
            final double k = Math.sqrt(width * height / graph.getNumberOfNodes()) / 2;
            final double l2 = 25 * log2 * Math.log(1 + count);
            final double tx = width / l2;
            final double ty = height / l2;

            final NodeDoubleArray xDispl = new NodeDoubleArray(graph);
            final NodeDoubleArray yDispl = new NodeDoubleArray(graph);

            // repulsive forces

            for (Node v : graph.nodes()) {
                double xv = xPos.getValue(v);
                double yv = yPos.getValue(v);

                for (Node u : graph.nodes()) {
                    if (u == v)
                        continue;
                    double xDist = xv - xPos.getValue(u);
                    double yDist = yv - yPos.getValue(u);
                    double dist = xDist * xDist + yDist * yDist;
                    if (dist < 1e-3)
                        dist = 1e-3;
                    double repulse = k * k / dist;
                    xDispl.set(v, xDispl.getValue(v) + repulse * xDist);
                    yDispl.set(v, yDispl.getValue(v) + repulse * yDist);
                }

                for (Edge e : graph.edges()) {
                    final Node a = e.getSource();
                    final Node b = e.getTarget();
                    if (a == v || b == v)
                        continue;
                    double xDist = xv - (xPos.getValue(a) + xPos.getValue(b)) / 2;
                    double yDist = yv - (yPos.getValue(a) + yPos.getValue(b)) / 2;
                    double dist = xDist * xDist + yDist * yDist;
                    if (dist < 1e-3)
                        dist = 1e-3;
                    double repulse = k * k / dist;
                    xDispl.set(v, xDispl.getValue(v) + repulse * xDist);
                    yDispl.set(v, yDispl.getValue(v) + repulse * yDist);
                }
            }

            // attractive forces

            for (Edge e : graph.edges()) {
                final Node u = e.getSource();
                final Node v = e.getTarget();

                double xDist = xPos.getValue(v) - xPos.getValue(u);
                double yDist = yPos.getValue(v) - yPos.getValue(u);

                double dist = Math.sqrt(xDist * xDist + yDist * yDist);

                dist /= ((u.getDegree() + v.getDegree()) / 16.0);

                xDispl.set(v, xDispl.getValue(v) - xDist * dist / k);
                yDispl.set(v, yDispl.getValue(v) - yDist * dist / k);
                xDispl.set(u, xDispl.getValue(u) + xDist * dist / k);
                yDispl.set(u, yDispl.getValue(u) + yDist * dist / k);
            }

            // preventions

            for (Node v : graph.nodes()) {
                double xd = xDispl.getValue(v);
                double yd = yDispl.getValue(v);

                final double dist = Math.sqrt(xd * xd + yd * yd);

                xd = tx * xd / dist;
                yd = ty * yd / dist;

                xPos.set(v, xPos.getValue(v) + xd);
                yPos.set(v, yPos.getValue(v) + yd);
            }
        }

        // set node positions
        for (Node v : graph.nodes()) {
            node2view.put(v, new Point2D(xPos.getValue(v), yPos.getValue(v)));
        }
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
}
