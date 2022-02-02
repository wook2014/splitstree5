/*
 * OutlineAlgorithm.java Copyright (C) 2022 Daniel H. Huson
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

package splitstree5.core.algorithms.views;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import jloda.fx.window.NotificationManager;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloSplitsGraph;
import jloda.util.BitSetUtils;
import jloda.util.ProgramProperties;
import jloda.util.StringUtils;
import jloda.util.Triplet;
import jloda.util.progress.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromSplits;
import splitstree5.core.algorithms.interfaces.IToViewer;
import splitstree5.core.algorithms.views.algo.NetworkOutlineAlgorithm;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.ViewerBlock;
import splitstree5.core.workflow.UpdateState;
import splitstree5.gui.graphtab.ISplitsViewTab;
import splitstree5.gui.graphtab.SplitsViewTab;
import splitstree5.gui.graphtab.base.*;
import splitstree5.utils.SplitsUtilities;

import java.io.IOException;
import java.util.*;

import static splitstree5.core.algorithms.views.SplitsNetworkAlgorithm.setupForRootedNetwork;

/**
 * compute an embedding of a set of splits using the outline algorithm
 * Daniel Huson, 3.2020
 */
public class OutlineAlgorithm extends Algorithm<SplitsBlock, ViewerBlock> implements IFromSplits, IToViewer {
    public enum Layout {Circular, MidPointRooted, MidPointRootedAlt, RootBySelectedOutgroup, RootBySelectedOutgroupAlt}

    private final ObjectProperty<Layout> optionLayout = new SimpleObjectProperty<>(Layout.Circular);

    private final BooleanProperty optionUseWeights = new SimpleBooleanProperty(true);

    private final PhyloSplitsGraph graph = new PhyloSplitsGraph();

    private ChangeListener<UpdateState> changeListener;

    public List<String> listOptions() {
        return Arrays.asList("optionUseWeights", "optionLayout");
    }

    @Override
    public String getCitation() {
        return "Huson et al, 2021; D.H. Huson, C. Bagci, B. Centikaya and D.J. Bryant (2021). Phylogenetic outlines. In preparation.";
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock0, SplitsBlock splitsBlock0, ViewerBlock viewerBlock) throws Exception {
        if (splitsBlock0.getNsplits() == 0)
            throw new IOException("No splits in input");
        progress.setTasks("Outline algorithm", "Init.");

        final TaxaBlock taxaBlock;
        final SplitsBlock splitsBlock;
        if (getOptionLayout() == Layout.Circular) {
            taxaBlock = taxaBlock0;
            splitsBlock = splitsBlock0;
        } else if (getOptionLayout() == Layout.RootBySelectedOutgroup || getOptionLayout() == Layout.RootBySelectedOutgroupAlt) {
            final BitSet selectedTaxa = ((SplitsViewTab) viewerBlock.getTab()).getSelectedTaxa();
            while (true) {
                if (selectedTaxa.nextSetBit(taxaBlock0.getNtax() + 1) == -1)
                    break;
                else
                    selectedTaxa.clear(selectedTaxa.nextSetBit(taxaBlock0.getNtax() + 1));
            }
            if (selectedTaxa.cardinality() > 0 && selectedTaxa.nextSetBit(1) <= taxaBlock0.getNtax()) {
                taxaBlock = new TaxaBlock();
                splitsBlock = new SplitsBlock();
                final Triplet<Integer, Double, Double> rootingSplit = SplitsUtilities.computeRootLocation(getOptionLayout() == Layout.RootBySelectedOutgroupAlt, taxaBlock0.getNtax(), new HashSet<>(BitSetUtils.asList(selectedTaxa)), splitsBlock0.getCycle(), splitsBlock0, isOptionUseWeights(), progress);
                setupForRootedNetwork(getOptionLayout() == Layout.RootBySelectedOutgroupAlt, rootingSplit, taxaBlock0, splitsBlock0, taxaBlock, splitsBlock, progress);
            } else {
                NotificationManager.showWarning(selectedTaxa.cardinality() == 0 ? "No out-group taxa selected" : "Invalid out-group taxa selected");
                taxaBlock = taxaBlock0;
                splitsBlock = splitsBlock0;
            }
        } else { //  midpoint rooting
            taxaBlock = new TaxaBlock();
            splitsBlock = new SplitsBlock();
            final Triplet<Integer, Double, Double> rootingSplit = SplitsUtilities.computeRootLocation(getOptionLayout() == Layout.MidPointRootedAlt, taxaBlock0.getNtax(), new HashSet<>(), splitsBlock0.getCycle(), splitsBlock0, isOptionUseWeights(), progress);

            System.err.println(splitsBlock0.get(rootingSplit.getFirst()) + ", " + rootingSplit.getSecond() + ", " + rootingSplit.getThird());

            setupForRootedNetwork(getOptionLayout() == Layout.MidPointRootedAlt, rootingSplit, taxaBlock0, splitsBlock0, taxaBlock, splitsBlock, progress);
        }
        final ISplitsViewTab viewTab = (ISplitsViewTab) viewerBlock.getTab();
        //splitsViewTab.setNodeLabel2Style(nodeLabel2Style);

        Platform.runLater(() -> viewerBlock.getTab().setText(viewerBlock.getName()));

        graph.clear();
        viewTab.init(graph);

        final BitSet forbiddenSplits = new BitSet();
        final NodeArray<Point2D> node2point = new NodeArray<>(graph);

        final BitSet usedSplits = new BitSet();

        final ArrayList<ArrayList<Node>> loops = new ArrayList<>();

        NetworkOutlineAlgorithm.apply(progress, isOptionUseWeights(), taxaBlock, splitsBlock, graph, node2point, forbiddenSplits, usedSplits, loops, getOptionLayout() != Layout.Circular);
        if (splitsBlock.getNsplits() - usedSplits.cardinality() > 0)
            NotificationManager.showWarning(String.format("Outline algorithm: skipped %d non-circular splits", splitsBlock.getNsplits() - usedSplits.cardinality()));

        double factorX = TreeEmbedder.scaleAndCenterToFitTarget(GraphLayout.Radial, viewTab.getTargetDimensions(), node2point, true);
        if (viewerBlock.getTab() instanceof Graph2DTab) {
            ((Graph2DTab) viewTab).getScaleBar().setUnitLengthX(factorX);
        }
        if (splitsBlock.getFit() > 0)
            ((Graph2DTab) viewTab).getFitLabel().setText(String.format("Fit: %.1f", splitsBlock.getFit()));
        else
            ((Graph2DTab) viewTab).getFitLabel().setText("");

        // compute all views and put their parts into the appropriate groups
        final Font labelFont = Font.font(ProgramProperties.getDefaultFontFX().getFamily(), taxaBlock.getNtax() <= 64 ? 16 : Math.max(4, 12 - Math.log(taxaBlock.getNtax() - 64) / Math.log(2)));
        for (Node v : graph.nodes()) {
            final String text;

            if (graph.getLabel(v) != null && graph.getLabel(v).length() > 0)
                if (TaxaBlock.hasDisplayLabels(taxaBlock) && graph.getNumberOfTaxa(v) == 1)
                    text = taxaBlock.get(graph.getTaxa(v).iterator().next()).getDisplayLabelOrName();
                else
                    text = graph.getLabel(v);
            else if (graph.getNumberOfTaxa(v) > 0)
				text = StringUtils.toString(taxaBlock.getLabels(graph.getTaxa(v)), ",");
            else text = null;

            final NodeViewBase nodeView = viewTab.createNodeView(v, graph.getTaxa(v), node2point.get(v), text);
            viewTab.setupNodeView(nodeView);

            viewTab.getNode2view().put(v, nodeView);
            viewTab.getNodesGroup().getChildren().addAll(nodeView.getShapeGroup());
            viewTab.getNodeLabelsGroup().getChildren().addAll(nodeView.getLabelGroup());
            if (nodeView.getLabel() != null)
                nodeView.getLabel().setFont(labelFont);
        }

        ((Graph2DTab) viewTab).getPolygons().clear();
        for (ArrayList<Node> loop : loops) {
            final PolygonView2D polygon = new PolygonView2D(loop, viewTab.getNode2view());
            ((Graph2DTab) viewTab).getPolygons().add(polygon);
            viewTab.getEdgesGroup().getChildren().add(polygon.getShape());
        }

        for (Edge e : graph.edges()) {
            final EdgeViewBase edgeView = viewTab.createEdgeView(e, node2point.get(e.getSource()), node2point.get(e.getTarget()), null);
            viewTab.getEdge2view().put(e, edgeView);
            viewTab.getEdgesGroup().getChildren().addAll(edgeView.getShapeGroup().getChildren());
            if (edgeView.getLabel() != null) {
                viewTab.getEdgeLabelsGroup().getChildren().addAll(edgeView.getLabel());
                edgeView.getLabel().setFont(labelFont);
            }

            final int split = graph.getSplit(e);
            final String label = splitsBlock.getSplitLabels().get(split);
            if (label != null) {
                if (label.contains("BOLD")) {
                    edgeView.setStrokeWidth(3);
                }
            }
            final Tooltip tooltip = new Tooltip("Split " + split);
            Tooltip.install(edgeView.getEdgeShape(), tooltip);
        }

        Platform.runLater(() -> viewTab.updateSelectionModels(graph, taxaBlock, viewerBlock.getDocument()));
        viewerBlock.show();

        progress.close();

        if (changeListener != null)
            getConnector().stateProperty().removeListener(changeListener);
        changeListener = (c, o, n) -> viewerBlock.getTab().getCenter().setDisable(n != UpdateState.VALID);
        getConnector().stateProperty().addListener(new WeakChangeListener<>(changeListener));
    }


    public boolean isOptionUseWeights() {
        return optionUseWeights.get();
    }

    public BooleanProperty optionUseWeightsProperty() {
        return optionUseWeights;
    }

    public void setOptionUseWeights(boolean optionUseWeights) {
        this.optionUseWeights.set(optionUseWeights);
    }

    @Override
    public boolean isAssignableFrom(Class that) {
        return super.isAssignableFrom(that) || SplitsNetworkAlgorithm.class.isAssignableFrom(that);
    }

    public Layout getOptionLayout() {
        return optionLayout.get();
    }

    public ObjectProperty<Layout> optionLayoutProperty() {
        return optionLayout;
    }

    public void setOptionLayout(Layout optionLayout) {
        this.optionLayout.set(optionLayout);
    }
}
