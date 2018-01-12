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

package splitstree5.core.algorithms.views;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Point2D;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloGraph;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromSplits;
import splitstree5.core.algorithms.interfaces.IToSplitsNetworkView;
import splitstree5.core.algorithms.views.algorithms.ConvexHull;
import splitstree5.core.algorithms.views.algorithms.EqualAngle;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.SplitsNetworkViewBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.workflow.UpdateState;
import splitstree5.gui.graphtab.SplitsViewTab;
import splitstree5.gui.graphtab.base.AEdgeView;
import splitstree5.gui.graphtab.base.ANodeView;
import splitstree5.gui.graphtab.base.GraphLayout;
import splitstree5.gui.style.Style;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 * compute an implementing of a set of splits using the equal angle algorithm
 * Daniel Huson, 11.2017
 */
public class SplitsNetworkAlgorithm extends Algorithm<SplitsBlock, SplitsNetworkViewBlock> implements IFromSplits, IToSplitsNetworkView {
    public enum Algorithm {EqualAngleFollowedByConvexHull, EqualAngleOnly, ConvexHullOnly}

    private final PhyloGraph graph = new PhyloGraph();
    private ObjectProperty<Algorithm> optionAlgorithm = new SimpleObjectProperty<>(Algorithm.EqualAngleFollowedByConvexHull);
    private BooleanProperty optionUseWeights = new SimpleBooleanProperty(true);

    private final Map<String, Style> nodeLabel2Style = new HashMap<>();

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

    private ChangeListener<UpdateState> changeListener;

    @Override
    public String getCitation() {
        return "SplitsNetworkAlgorithm; Dress and Huson 2004; " +
                "A.W.M. Dress and D.H. Huson, \"Constructing splits graphs,\" " +
                "in IEEE/ACM Transactions on Computational Biology and Bioinformatics, vol. 1, no. 3, pp. 109-115, July-Sept. 2004.";
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxa, SplitsBlock parent, SplitsNetworkViewBlock child) throws Exception {
        progress.setTasks("Split network construction", "Init.");
        final SplitsViewTab view = child.getTab();
        view.setNodeLabel2Style(nodeLabel2Style);

        Platform.runLater(() -> {
            child.getTab().setName(child.getName());
        });

        graph.clear();
        view.init(graph);

        final BitSet forbiddenSplits = new BitSet();
        final NodeArray<Point2D> node2point = new NodeArray<>(graph);

        final BitSet usedSplits = new BitSet();

        if (getOptionAlgorithm() != Algorithm.ConvexHullOnly)
            EqualAngle.apply(progress, isOptionUseWeights(), taxa, parent, graph, node2point, forbiddenSplits, usedSplits);

        if (getOptionAlgorithm() != Algorithm.EqualAngleOnly && usedSplits.cardinality() < parent.getNsplits()) {
            progress.setProgress(60);
            ConvexHull.apply(progress, isOptionUseWeights(), taxa, parent, graph, node2point, usedSplits);
        }

        progress.setProgress(90);

        EqualAngle.assignCoordinatesToNodes(isOptionUseWeights(), graph, node2point); // need coordinates

        progress.setProgress(100);   //set progress to 100%

        TreeEmbedder.scaleToFitTarget(GraphLayout.Radial, view.getTargetDimensions(), graph, node2point);

        // compute all views and put their parts into the appropriate groups
        for (Node v : graph.nodes()) {
            String text = graph.getLabel(v);
            //String text = (graph.getLabel(v) != null ? graph.getLabel(v) : "Node " + v.getId());
            final ANodeView nodeView = view.createNodeView(v, node2point.getValue(v), text);
            view.getNode2view().put(v, nodeView);
            if (nodeView.getShape() != null)
                view.getNodesGroup().getChildren().addAll(nodeView.getShape());
            if (nodeView.getLabel() != null)
                view.getNodeLabelsGroup().getChildren().addAll(nodeView.getLabel());
        }
        for (Edge e : graph.edges()) {
            final AEdgeView edgeView = view.createEdgeView(graph, e, graph.getWeight(e), node2point.get(e.getSource()), node2point.get(e.getTarget()));
            view.getEdge2view().put(e, edgeView);
            if (edgeView.getShape() != null)
                view.getEdgesGroup().getChildren().add(edgeView.getShape());
            if (edgeView.getLabel() != null)
                view.getEdgeLabelsGroup().getChildren().addAll(edgeView.getLabel());
        }
        Platform.runLater(() -> child.updateSelectionModels(graph, taxa, child.getDocument()));
        child.show();

        progress.close();

        if (changeListener != null)
            getConnector().stateProperty().removeListener(changeListener);
        changeListener = (c, o, n) -> child.getTab().getCenter().setDisable(n != UpdateState.VALID);
        getConnector().stateProperty().addListener(new WeakChangeListener<>(changeListener));

    }
}