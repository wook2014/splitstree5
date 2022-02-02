/*
 * SplitsNetworkAlgorithm.java Copyright (C) 2022 Daniel H. Huson
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
import splitstree5.core.algorithms.views.algo.ConvexHull;
import splitstree5.core.algorithms.views.algo.EqualAngle;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.ViewerBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Taxon;
import splitstree5.core.workflow.UpdateState;
import splitstree5.gui.graphtab.ISplitsViewTab;
import splitstree5.gui.graphtab.SplitsViewTab;
import splitstree5.gui.graphtab.base.EdgeViewBase;
import splitstree5.gui.graphtab.base.Graph2DTab;
import splitstree5.gui.graphtab.base.GraphLayout;
import splitstree5.gui.graphtab.base.NodeViewBase;
import splitstree5.utils.SplitsUtilities;

import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;

/**
 * compute an implementing of a set of splits using the equals angle algorithm
 * Daniel Huson, 11.2017
 */
public class SplitsNetworkAlgorithm extends Algorithm<SplitsBlock, ViewerBlock> implements IFromSplits, IToViewer {
    public enum Algorithm {EqualAngleConvexHull, EqualAngleOnly, ConvexHullOnly}

    private final ObjectProperty<Algorithm> optionAlgorithm = new SimpleObjectProperty<>(Algorithm.EqualAngleConvexHull);

    public enum Layout {Circular, MidPointRooted, MidPointRootedAlt, RootBySelectedOutgroup, RootBySelectedOutgroupAlt}

    private final ObjectProperty<Layout> optionLayout = new SimpleObjectProperty<>(Layout.Circular);

    private final BooleanProperty optionUseWeights = new SimpleBooleanProperty(true);


    private final PhyloSplitsGraph graph = new PhyloSplitsGraph();

    private ChangeListener<UpdateState> changeListener;

    public List<String> listOptions() {
        return Arrays.asList("optionAlgorithm", "optionUseWeights", "optionLayout", "optionBoxOpenIterations", "optionDaylightIterations");
    }

    @Override
    public String getCitation() {
        return "Dress & Huson 2004; " +
                "A.W.M. Dress and D.H. Huson, Constructing splits graphs, " +
                "IEEE/ACM Transactions on Computational Biology and Bioinformatics 1(3):109-115, 2004.";
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock0, SplitsBlock splitsBlock0, ViewerBlock viewerBlock) throws Exception {
        if (splitsBlock0.getNsplits() == 0)
            throw new IOException("No splits in input");
        progress.setTasks("Split network construction", "Init.");

        final TaxaBlock taxaBlock;
        final SplitsBlock splitsBlock;
        if (getOptionLayout() == Layout.Circular) {
            taxaBlock = taxaBlock0;
            splitsBlock = splitsBlock0;
        } else if (getOptionLayout() == Layout.RootBySelectedOutgroup || getOptionLayout() == Layout.RootBySelectedOutgroupAlt) {
            final BitSet selectedTaxa = ((SplitsViewTab) viewerBlock.getTab()).getSelectedTaxa();
            if (selectedTaxa.cardinality() > 0 && selectedTaxa.nextSetBit(1) <= taxaBlock0.getNtax()) {
                taxaBlock = new TaxaBlock();
                splitsBlock = new SplitsBlock();
                final Triplet<Integer, Double, Double> rootingSplit = SplitsUtilities.computeRootLocation(getOptionLayout() == Layout.RootBySelectedOutgroupAlt, taxaBlock0.getNtax(), new HashSet<>(BitSetUtils.asList(selectedTaxa)), splitsBlock0.getCycle(), splitsBlock0, isOptionUseWeights(), progress);
                setupForRootedNetwork(getOptionLayout() == Layout.RootBySelectedOutgroupAlt, rootingSplit, taxaBlock0, splitsBlock0, taxaBlock, splitsBlock, progress);
            } else {
                NotificationManager.showWarning(selectedTaxa.cardinality() == 0 ? "No taxa selected" : "Invalid taxa selected");
                taxaBlock = taxaBlock0;
                splitsBlock = splitsBlock0;
            }
        } else {
            taxaBlock = new TaxaBlock();
            splitsBlock = new SplitsBlock();
            final Triplet<Integer, Double, Double> rootingSplit = SplitsUtilities.computeRootLocation(getOptionLayout() == Layout.MidPointRootedAlt, taxaBlock0.getNtax(), new HashSet<>(), splitsBlock0.getCycle(), splitsBlock0, isOptionUseWeights(), progress);
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

        if (getOptionAlgorithm() == Algorithm.EqualAngleOnly || getOptionAlgorithm() == Algorithm.EqualAngleConvexHull)
            EqualAngle.apply(progress, isOptionUseWeights(), taxaBlock, splitsBlock, graph, node2point, forbiddenSplits, usedSplits);

        if (usedSplits.cardinality() < splitsBlock.getNsplits()) {
            if (getOptionAlgorithm() == Algorithm.EqualAngleConvexHull || getOptionAlgorithm() == Algorithm.ConvexHullOnly) {
                progress.setProgress(60);
                ConvexHull.apply(progress, taxaBlock, splitsBlock, graph, usedSplits);
            }
        }

        EqualAngle.assignAnglesToEdges(taxaBlock.getNtax(), splitsBlock, splitsBlock.getCycle(), graph, forbiddenSplits, getOptionLayout() == Layout.Circular ? 360 : 160);
        EqualAngle.assignCoordinatesToNodes(isOptionUseWeights(), graph, node2point, splitsBlock.getCycle()[1]); // need coordinates

        progress.setProgress(90);

        double factorX = TreeEmbedder.scaleAndCenterToFitTarget(GraphLayout.Radial, viewTab.getTargetDimensions(), node2point, true);
        if (viewerBlock.getTab() instanceof Graph2DTab) {
            ((Graph2DTab) viewTab).getScaleBar().setUnitLengthX(factorX);
        }

        if (splitsBlock.getFit() > 0)
            ((Graph2DTab) viewTab).getFitLabel().setText(String.format("Fit: %.2f", splitsBlock.getFit()));
        else
            ((Graph2DTab) viewTab).getFitLabel().setText("");

        progress.setTasks("Visualization", "Creating nodes");
        progress.setMaximum(graph.getNumberOfNodes());
        progress.setProgress(0);

        // compute all views and put their parts into the appropriate groups
        final Font labelFont = Font.font(ProgramProperties.getDefaultFontFX().getFamily(), taxaBlock.getNtax() <= 64 ? 16 : Math.max(4, 12 - Math.log(taxaBlock.getNtax() - 64) / Math.log(2)));
        for (Node v : graph.nodes()) {
            final String text;

            if (graph.getLabel(v) != null && graph.getLabel(v).length() > 0) {
                if (TaxaBlock.hasDisplayLabels(taxaBlock) && graph.getNumberOfTaxa(v) == 1)
                    text = taxaBlock.get(graph.getTaxa(v).iterator().next()).getDisplayLabelOrName();
                else
                    text = graph.getLabel(v);
            } else if (graph.getNumberOfTaxa(v) > 0)
				text = StringUtils.toString(taxaBlock.getLabels(graph.getTaxa(v)), ",");
            else text = null;

            final NodeViewBase nodeView = viewTab.createNodeView(v, graph.getTaxa(v), node2point.get(v), text);
            if (graph.getNumberOfTaxa(v) > 0)
                BitSetUtils.addAll(nodeView.getWorkingTaxa(), graph.getTaxa(v));
            viewTab.setupNodeView(nodeView);

            viewTab.getNode2view().put(v, nodeView);
            viewTab.getNodesGroup().getChildren().addAll(nodeView.getShapeGroup());
            viewTab.getNodeLabelsGroup().getChildren().addAll(nodeView.getLabelGroup());
            if (nodeView.getLabel() != null)
                nodeView.getLabel().setFont(labelFont);
            progress.incrementProgress();
        }

        progress.setSubtask("Creating edges");
        progress.setMaximum(graph.getNumberOfEdges());
        progress.setProgress(0);

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
            if (false)
                try {
                    if (edgeView.getEdgeShape() != null) {
                        final Tooltip tooltip = new Tooltip("Split " + split);
                        Tooltip.install(edgeView.getEdgeShape(), tooltip);
                    }
                } catch (Exception ignore) {
                }
            progress.incrementProgress();
        }

        Platform.runLater(() -> viewTab.updateSelectionModels(graph, taxaBlock, viewerBlock.getDocument()));
        viewerBlock.show();

        progress.reportTaskCompleted();

        if (changeListener != null)
            getConnector().stateProperty().removeListener(changeListener);
        changeListener = (c, o, n) -> viewerBlock.getTab().getCenter().setDisable(n != UpdateState.VALID);
        getConnector().stateProperty().addListener(new WeakChangeListener<>(changeListener));
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

    public Layout getOptionLayout() {
        return optionLayout.get();
    }

    public ObjectProperty<Layout> optionLayoutProperty() {
        return optionLayout;
    }

    public void setOptionLayout(Layout optionLayout) {
        this.optionLayout.set(optionLayout);
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
        return super.isAssignableFrom(that) || OutlineAlgorithm.class.isAssignableFrom(that);
    }

    public static void setupForRootedNetwork(boolean altLayout, Triplet<Integer, Double, Double> triplet, TaxaBlock taxaBlockSrc, SplitsBlock splitsBlockSrc, TaxaBlock taxaBlockTarget, SplitsBlock splitsBlockTarget, ProgressListener progress) throws IOException {
        //final Triplet<Integer,Double,Double> triplet= SplitsUtilities.getMidpointSplit(taxaBlockSrc.getNtax(), splitsBlockSrc);
        final int mid = triplet.getFirst();
        final double weightWith1 = triplet.getSecond();
        final double weightOpposite1 = triplet.getThird();

        // modify taxa:
        taxaBlockTarget.clear();
        taxaBlockTarget.setNtax(taxaBlockSrc.getNtax() + 1);
        for (Taxon taxon : taxaBlockSrc.getTaxa())
            taxaBlockTarget.add(taxon);
        final Taxon root = new Taxon("Root");
        taxaBlockTarget.add(root);
        final int rootTaxonId = taxaBlockTarget.indexOf(root);

        // modify cycle:
        final int[] cycle0 = splitsBlockSrc.getCycle();
        final int[] cycle = new int[cycle0.length + 1];
        int first = 0; // first taxon on other side of mid split
        if (!altLayout) {
            final BitSet part = splitsBlockSrc.get(mid).getPartNotContaining(1);
            int t = 1;
            for (int value : cycle0) {
                if (value > 0) {
                    if (first == 0 && part.get(value)) {
                        first = value;
                        cycle[t++] = rootTaxonId;
                    }
                    cycle[t++] = value;
                }
            }
        } else { // altLayout
            final BitSet part = splitsBlockSrc.get(mid).getPartNotContaining(1);
            int seen = 0;
            int t = 1;
            for (int value : cycle0) {
                if (value > 0) {
                    cycle[t++] = value;
                    if (part.get(value)) {
                        seen++;
                        if (seen == part.cardinality()) {
                            first = value;
                            cycle[t++] = rootTaxonId;
                        }
                    }
                }
            }
        }
        SplitsUtilities.rotateCycle(cycle, rootTaxonId);

        // setup splits:
        splitsBlockTarget.clear();
        double totalWeight = 0;

        final ASplit mid1 = splitsBlockSrc.get(mid).clone();
        mid1.getPartContaining(1).set(rootTaxonId);
        mid1.setWeight(weightWith1);
        final ASplit mid2 = splitsBlockSrc.get(mid).clone();
        mid2.getPartNotContaining(1).set(rootTaxonId);
        mid2.setWeight(weightOpposite1);

        for (int s = 1; s <= splitsBlockSrc.getNsplits(); s++) {
            if (s == mid) {
                totalWeight += mid1.getWeight();
                splitsBlockTarget.getSplits().add(mid1);
                //splitsBlockTarget.getSplitLabels().put(mid,"BOLD");
            } else {
                final ASplit aSplit = splitsBlockSrc.get(s).clone();

                if (BitSetUtils.contains(mid1.getPartNotContaining(rootTaxonId), aSplit.getA())) {
                    aSplit.getB().set(rootTaxonId);
                } else if (BitSetUtils.contains(mid1.getPartNotContaining(rootTaxonId), aSplit.getB())) {
                    aSplit.getA().set(rootTaxonId);
                } else if (aSplit.getPartContaining(first).cardinality() > 1)
                    aSplit.getPartContaining(first).set(rootTaxonId);
                else
                    aSplit.getPartNotContaining(first).set(rootTaxonId);

                splitsBlockTarget.getSplits().add(aSplit);
                totalWeight += aSplit.getWeight();
            }
        }
        // add  new separator split
        {
            totalWeight += mid2.getWeight();
            splitsBlockTarget.getSplits().add(mid2);
            //splitsBlockTarget.getSplitLabels().put(splitsBlockTarget.getNsplits(),"BOLD");
        }
        // add root split:
        {
            final ASplit aSplit = new ASplit(BitSetUtils.asBitSet(rootTaxonId), taxaBlockTarget.getNtax(), totalWeight > 0 ? totalWeight / splitsBlockTarget.getNsplits() : 1);
            splitsBlockTarget.getSplits().add(aSplit);

        }
        splitsBlockTarget.setCycle(cycle, false);
    }
}
