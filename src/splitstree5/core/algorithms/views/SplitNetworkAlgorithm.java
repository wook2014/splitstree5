/*
 *  Copyright (C) 2016 Daniel H. Huson
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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloGraph;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromSplits;
import splitstree5.core.algorithms.interfaces.IToSplitsView;
import splitstree5.core.algorithms.views.algorithms.ConvexHull;
import splitstree5.core.algorithms.views.algorithms.EqualAngle;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.SplitsViewBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreeViewBlock;
import splitstree5.core.datablocks.view.AEdgeView;
import splitstree5.core.datablocks.view.ANodeView;

import java.util.BitSet;

/**
 * compute an implementing of a set of splits using the equal angle algorithm
 * Daniel Huson, 11.2017
 */
public class SplitNetworkAlgorithm extends Algorithm<SplitsBlock, SplitsViewBlock> implements IFromSplits, IToSplitsView {
    public enum Algorithm {EqualAngleFollowedByConvexHull, EqualAngleOnly, ConvexHullOnly}

    private final PhyloGraph graph = new PhyloGraph();
    private ObjectProperty<Algorithm> optionAlgorithm = new SimpleObjectProperty<>(Algorithm.EqualAngleFollowedByConvexHull);
    private BooleanProperty optionUseWeights = new SimpleBooleanProperty(true);

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

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxa, SplitsBlock splits, SplitsViewBlock child) throws Exception {
        progress.setTasks("Split network calculation", "Init.");

        graph.clear();
        child.init(graph);


        final BitSet forbiddenSplits = new BitSet();
        final NodeArray<Point2D> node2point = new NodeArray<>(graph);

        final BitSet usedSplits = new BitSet();

        if (getOptionAlgorithm() != Algorithm.ConvexHullOnly)
            EqualAngle.apply(progress, isOptionUseWeights(), taxa, splits, graph, node2point, forbiddenSplits, usedSplits);

        if (getOptionAlgorithm() != Algorithm.EqualAngleOnly && usedSplits.cardinality() < splits.getNsplits()) {
            progress.setProgress(60);
            ConvexHull.apply(progress, isOptionUseWeights(), taxa, splits, graph, node2point, usedSplits);
        }

        progress.setProgress(90);

        EqualAngle.assignCoordinatesToNodes(isOptionUseWeights(), graph, node2point); // need coordinates

        progress.setProgress(100);   //set progress to 100%

        TreeEmbedder.scaleToFitTarget(TreeViewBlock.Layout.Radial, child.getTargetDimensions(), graph, node2point);

        // compute all views and put their parts into the appropriate groups
        for (Node v : graph.nodes()) {
            String text = graph.getLabel(v);
            //String text = (graph.getLabel(v) != null ? graph.getLabel(v) : "Node " + v.getId());
            final ANodeView nodeView = SplitsViewBlock.createNodeView(v, node2point.getValue(v), text, child.getNodeSelectionModel());
            child.getNode2view().put(v, nodeView);
            if (nodeView.getShape() != null)
                child.getNodesGroup().getChildren().addAll(nodeView.getShape());
            if (nodeView.getLabel() != null)
                child.getNodeLabelsGroup().getChildren().addAll(nodeView.getLabel());
        }
        for (Edge e : graph.edges()) {
            final AEdgeView edgeView = SplitsViewBlock.createEdgeView(graph, e, graph.getWeight(e), node2point.get(e.getSource()), node2point.get(e.getTarget()), child.getNodeSelectionModel(), child.getSplitsSelectionModel());
            child.getEdge2view().put(e, edgeView);
            if (edgeView.getShape() != null)
                child.getEdgesGroup().getChildren().add(edgeView.getShape());
            if (edgeView.getLabel() != null)
                child.getEdgeLabelsGroup().getChildren().addAll(edgeView.getLabel());
        }

        child.updateSelectionModels(graph); // need to do this after graph has been computed
        child.show();

        progress.close();
    }
}
