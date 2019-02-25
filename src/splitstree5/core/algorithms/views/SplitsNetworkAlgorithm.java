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

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.text.Font;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.SplitsGraph;
import jloda.util.Basic;
import jloda.util.ProgramProperties;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromSplits;
import splitstree5.core.algorithms.interfaces.IToViewer;
import splitstree5.core.algorithms.views.utils.BoxOptimizer;
import splitstree5.core.algorithms.views.utils.ConvexHull;
import splitstree5.core.algorithms.views.utils.DaylightOptimizer;
import splitstree5.core.algorithms.views.utils.EqualAngle;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.ViewerBlock;
import splitstree5.core.workflow.UpdateState;
import splitstree5.gui.graphtab.ISplitsViewTab;
import splitstree5.gui.graphtab.base.EdgeViewBase;
import splitstree5.gui.graphtab.base.Graph2DTab;
import splitstree5.gui.graphtab.base.GraphLayout;
import splitstree5.gui.graphtab.base.NodeViewBase;

import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * compute an implementing of a set of splits using the equal angle algorithm
 * Daniel Huson, 11.2017
 */
public class SplitsNetworkAlgorithm extends Algorithm<SplitsBlock, ViewerBlock> implements IFromSplits, IToViewer {
    private final SplitsGraph graph = new SplitsGraph();

    private ChangeListener<UpdateState> changeListener;

    public enum Algorithm {EqualAngleConvexHull, EqualAngleOnly, ConvexHullOnly}

    private final ObjectProperty<Algorithm> optionAlgorithm = new SimpleObjectProperty<>(Algorithm.EqualAngleConvexHull);
    private final BooleanProperty optionUseWeights = new SimpleBooleanProperty(true);

    private final IntegerProperty optionDaylightIterations = new SimpleIntegerProperty(0);

    private final IntegerProperty optionBoxOpenIterations = new SimpleIntegerProperty(0);

    public List<String> listOptions() {
        return Arrays.asList("optionAlgorithm", "optionUseWeights", "optionBoxOpenIterations", "optionDaylightIterations");
    }

    @Override
    public String getCitation() {
        return "Dress & Huson 2004; " +
                "A.W.M. Dress and D.H. Huson, Constructing splits graphs, " +
                "in IEEE/ACM Transactions on Computational Biology and Bioinformatics, vol. 1, no. 3, pp. 109-115, July-Sept. 2004.";
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, SplitsBlock parent, ViewerBlock child) throws Exception {
        if (parent.getNsplits() == 0)
            throw new IOException("No splits in input");
        progress.setTasks("Split network construction", "Init.");
        final ISplitsViewTab viewTab = (ISplitsViewTab) child.getTab();
        //splitsViewTab.setNodeLabel2Style(nodeLabel2Style);

        Platform.runLater(() -> {
            child.getTab().setName(child.getName());
        });

        graph.clear();
        viewTab.init(graph);

        final BitSet forbiddenSplits = new BitSet();
        final NodeArray<Point2D> node2point = new NodeArray<>(graph);

        final BitSet usedSplits = new BitSet();

        if (getOptionAlgorithm() != Algorithm.ConvexHullOnly) {
            EqualAngle.apply(progress, isOptionUseWeights(), taxaBlock, parent, graph, node2point, forbiddenSplits, usedSplits);
        }

        if (getOptionBoxOpenIterations() > 0) {
            final BoxOptimizer boxOptimizer = new BoxOptimizer();
            boxOptimizer.setOptionIterations(getOptionBoxOpenIterations());
            boxOptimizer.setOptionUseWeights(isOptionUseWeights());
            boxOptimizer.apply(progress, taxaBlock, parent.getNsplits(), graph, node2point);
        }

        if (getOptionAlgorithm() != Algorithm.EqualAngleOnly && usedSplits.cardinality() < parent.getNsplits()) {
            progress.setProgress(60);
            ConvexHull.apply(progress, taxaBlock, parent, graph, usedSplits);
        }

        EqualAngle.assignAnglesToEdges(taxaBlock.getNtax(), parent, parent.getCycle(), graph, forbiddenSplits);
        EqualAngle.assignCoordinatesToNodes(isOptionUseWeights(), graph, node2point); // need coordinates

        if (getOptionDaylightIterations() > 0) {
            final DaylightOptimizer daylightOptimizer = new DaylightOptimizer();
            daylightOptimizer.setOptionIterations(getOptionDaylightIterations());
            daylightOptimizer.setOptionUseWeights(isOptionUseWeights());
            daylightOptimizer.apply(progress, taxaBlock, graph, node2point);
            EqualAngle.assignCoordinatesToNodes(isOptionUseWeights(), graph, node2point);
        }

        progress.setProgress(90);

        double factorX = TreeEmbedder.scaleAndCenterToFitTarget(GraphLayout.Radial, viewTab.getTargetDimensions(), node2point, true);
        if (child.getTab() instanceof Graph2DTab) {
            ((Graph2DTab) child.getTab()).getScaleBar().setUnitLengthX(factorX);
        }

        progress.setProgress(100);   //set progress to 100%

        // compute all views and put their parts into the appropriate groups
        final Font labelFont = Font.font(ProgramProperties.getDefaultFont().getFamily(), taxaBlock.getNtax() <= 64 ? 16 : Math.max(4, 12 - Math.log(taxaBlock.getNtax() - 64) / Math.log(2)));
        for (Node v : graph.nodes()) {
            final String text;
            if (graph.getLabel(v) != null && graph.getLabel(v).length() > 0)
                text = graph.getLabel(v);
            else if (graph.getNumberOfTaxa(v) > 0)
                text = Basic.toString(taxaBlock.getLabels(graph.getTaxa(v)), ",");
            else text = null;

            final NodeViewBase nodeView = viewTab.createNodeView(v, node2point.getValue(v), text);
            viewTab.setupNodeView(nodeView);

            viewTab.getNode2view().put(v, nodeView);
            viewTab.getNodesGroup().getChildren().addAll(nodeView.getShapeGroup());
            viewTab.getNodeLabelsGroup().getChildren().addAll(nodeView.getLabelGroup());
            if (nodeView.getLabel() != null)
                nodeView.getLabel().setFont(labelFont);
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
            final String label = parent.getSplitLabels().get(split);
            if (label != null) {
                if (label.contains("BOLD")) {
                    edgeView.setStrokeWidth(3);
                }
            }
        }

        Platform.runLater(() -> viewTab.updateSelectionModels(graph, taxaBlock, child.getDocument()));
        child.show();

        progress.close();

        if (changeListener != null)
            getConnector().stateProperty().removeListener(changeListener);
        changeListener = (c, o, n) -> child.getTab().getCenter().setDisable(n != UpdateState.VALID);
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

    public boolean isOptionUseWeights() {
        return optionUseWeights.get();
    }

    public BooleanProperty optionUseWeightsProperty() {
        return optionUseWeights;
    }

    public void setOptionUseWeights(boolean optionUseWeights) {
        this.optionUseWeights.set(optionUseWeights);
    }


    public int getOptionDaylightIterations() {
        return optionDaylightIterations.get();
    }

    public IntegerProperty optionDaylightIterationsProperty() {
        return optionDaylightIterations;
    }

    public void setOptionDaylightIterations(int optionDaylightIterations) {
        this.optionDaylightIterations.set(optionDaylightIterations);
    }

    public int getOptionBoxOpenIterations() {
        return optionBoxOpenIterations.get();
    }

    public IntegerProperty optionBoxOpenIterationsProperty() {
        return optionBoxOpenIterations;
    }

    public void setOptionBoxOpenIterations(int optionBoxOpenIterations) {
        this.optionBoxOpenIterations.set(optionBoxOpenIterations);
    }
}
