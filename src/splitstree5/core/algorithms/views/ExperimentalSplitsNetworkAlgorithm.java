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
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.text.Font;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.SplitsGraph;
import jloda.util.ProgramProperties;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromSplits;
import splitstree5.core.algorithms.interfaces.IToViewer;
import splitstree5.core.algorithms.views.utils.ConvexHull;
import splitstree5.core.algorithms.views.utils.EqualAngle;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.ViewerBlock;
import splitstree5.core.workflow.UpdateState;
import splitstree5.gui.graphtab.ISplitsViewTab;
import splitstree5.gui.graphtab.base.EdgeViewBase;
import splitstree5.gui.graphtab.base.GraphLayout;
import splitstree5.gui.graphtab.base.NodeViewBase;

import java.util.BitSet;
import java.util.Random;

/**
 * new experimental code for computing the embedding of a split network
 */
public class ExperimentalSplitsNetworkAlgorithm extends Algorithm<SplitsBlock, ViewerBlock> implements IFromSplits, IToViewer {
    private final SplitsGraph graph = new SplitsGraph();

    private ChangeListener<UpdateState> changeListener;


    private final BooleanProperty optionUseWeights = new SimpleBooleanProperty(true);
    private final BooleanProperty optionRandomJiggle = new SimpleBooleanProperty(true);


    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, SplitsBlock parent, ViewerBlock child) throws Exception {
        progress.setTasks("Split network construction", "Init.");
        final ISplitsViewTab viewTab = (ISplitsViewTab) child.getTab();

        Platform.runLater(() -> {
            child.getTab().setName(child.getName());
        });

        graph.clear();
        viewTab.init(graph);

        // this code creates the graph and computes initial coordinates for the the graph:
        final NodeArray<Point2D> node2point = new NodeArray<>(graph);
        {
            final BitSet forbiddenSplits = new BitSet();

            final BitSet usedSplits = new BitSet();

            EqualAngle.apply(progress, isOptionUseWeights(), taxaBlock, parent, graph, node2point, forbiddenSplits, usedSplits);
            progress.setProgress(60);
            ConvexHull.apply(progress, taxaBlock, parent, graph, usedSplits);

            EqualAngle.assignAnglesToEdges(taxaBlock.getNtax(), parent, parent.getCycle(), graph, forbiddenSplits);
            EqualAngle.assignCoordinatesToNodes(isOptionUseWeights(), graph, node2point); // need coordinates
        }

        // Mario Wassmer: Your improvement of embedding goes here:
        {
            // to illustrate, jiggle all points by small random amount:
            if (isOptionRandomJiggle()) {
                final Random random = new Random();
                for (Node v : graph.nodes()) {
                    final Point2D point = node2point.get(v);

                    node2point.put(v, new Point2D(point.getX() * (1 + 0.1 * (random.nextFloat() - 0.5)), point.getY() * (1 + 0.1 * (random.nextFloat() - 0.5))));
                }
            }
        }


        progress.setProgress(90);


        // code below here is for setting up the visualization:
        TreeEmbedder.scaleAndCenterToFitTarget(GraphLayout.Radial, viewTab.getTargetDimensions(), node2point, true);

        progress.setProgress(100);   //set progress to 100%

        // compute all views and put their parts into the appropriate groups
        final Font labelFont = Font.font(ProgramProperties.getDefaultFont().getFamily(), taxaBlock.getNtax() <= 64 ? 16 : Math.max(4, 12 - Math.log(taxaBlock.getNtax() - 64) / Math.log(2)));
        for (Node v : graph.nodes()) {
            final String text = graph.getLabel(v);
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
        }

        Platform.runLater(() -> viewTab.updateSelectionModels(graph, taxaBlock, child.getDocument()));
        child.show();

        progress.close();

        if (changeListener != null)
            getConnector().stateProperty().removeListener(changeListener);
        changeListener = (c, o, n) -> child.getTab().getCenter().setDisable(n != UpdateState.VALID);
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

    public boolean isOptionRandomJiggle() {
        return optionRandomJiggle.get();
    }

    public BooleanProperty optionRandomJiggleProperty() {
        return optionRandomJiggle;
    }

    public void setOptionRandomJiggle(boolean optionRandomJiggle) {
        this.optionRandomJiggle.set(optionRandomJiggle);
    }
}
