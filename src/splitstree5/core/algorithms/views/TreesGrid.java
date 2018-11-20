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
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.text.Font;
import jloda.graph.*;
import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import jloda.util.ProgramProperties;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToViewer;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.datablocks.ViewerBlock;
import splitstree5.core.workflow.UpdateState;
import splitstree5.gui.formattab.FormatItem;
import splitstree5.gui.graphtab.TreeViewTab;
import splitstree5.gui.graphtab.TreesGridTab;
import splitstree5.gui.graphtab.base.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static splitstree5.core.algorithms.views.TreeEmbedder.*;

/**
 * computes a grid of trees
 * Daniel Huson, 11.2017
 * todo: add support for rooted networks (as in Dendroscope)
 */
public class TreesGrid extends Algorithm<TreesBlock, ViewerBlock> implements IFromTrees, IToViewer {
    private final IntegerProperty optionRows = new SimpleIntegerProperty(4);
    private final IntegerProperty optionCols = new SimpleIntegerProperty(4);

    private final IntegerProperty optionFirstTree = new SimpleIntegerProperty(1); // 1-based

    private final Property<GraphLayout> optionLayout = new SimpleObjectProperty<>(GraphLayout.LeftToRight);
    private final Property<TreeEmbedder.EdgeLengths> optionEdgeLengths = new SimpleObjectProperty<>(TreeEmbedder.EdgeLengths.Weights);
    private final Property<EdgeView2D.EdgeShape> optionEdgeShape = new SimpleObjectProperty<>(EdgeView2D.EdgeShape.Angular);

    private final BooleanProperty optionShowInternalNodeLabels = new SimpleBooleanProperty();

    private final Map<String, FormatItem> nodeLabel2Style = new HashMap<>();

    private ChangeListener<UpdateState> changeListener;

    @Override
    public String getCitation() {
        return "Huson et al 2012;D.H. Huson, R. Rupp and C. Scornavacca, Phylogenetic Networks, Cambridge, 2012.";
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, TreesBlock parent, ViewerBlock child) throws Exception {
        progress.setTasks("Trees Grid", "Init.");

        final TreesGridTab treesGrid = (TreesGridTab) child.getTab();
        treesGrid.setShape(getOptionRows(), getOptionCols());
        treesGrid.setMaintainAspectRatio(getOptionLayout() == GraphLayout.Radial);

        int count = 0;
        for (int t = getOptionFirstTree() - 1; t < parent.getNTrees() && count < getOptionRows() * getOptionCols(); t++) {
            final TreeViewTab viewTab = treesGrid.getCurrent(count++);
            viewTab.setNodeLabel2Style(nodeLabel2Style);
            viewTab.setDataNode(child.getDataNode());

            if (parent.getNTrees() > 0) {
                final PhyloTree tree = parent.getTrees().get(t);
                viewTab.init(tree);

                if (tree.getRoot() == null && tree.getNumberOfNodes() > 0) {
                    for (Node v : tree.nodes()) {
                        if (v.getDegree() > 1 && tree.getLabel(v) == null) {
                            tree.setRoot(v);
                            break;
                        }
                    }
                    if (tree.getRoot() == null)
                        tree.setRoot(tree.getFirstNode());
                    tree.redirectEdgesAwayFromRoot();
                }

                final Node root = tree.getRoot();

                if (root != null) {
                    // todo: modify all code so that this is not necessary!
                    // compute edge lengths to reflect desired topology
                    final EdgeFloatArray edgeLengths = EdgeLengthsCalculation.computeEdgeLengths(tree, getOptionEdgeLengths());

                    // compute all coordinates:
                    final NodeArray<Point2D> node2point = new NodeArray<>(tree);
                    final EdgeArray<EdgeControlPoints> edge2controlPoints = new EdgeArray<>(tree);

                    switch (getOptionLayout()) {
                        case Radial: {
                            final EdgeFloatArray edge2Angle = new EdgeFloatArray(tree); // angle of edge
                            TreeEmbedder.setAnglesForCircularLayoutRec(root, null, 0, tree.getNumberOfLeaves(), edge2Angle, LEAF_GROUP_GAP_DEFAULT, PARENT_PLACEMENT_DEFAULT);

                            if (getOptionEdgeShape() == EdgeView2D.EdgeShape.Straight)
                                TreeEmbedder.computeNodeLocationsForRadialRec(root, new Point2D(0, 0), edgeLengths, edge2Angle, node2point);
                            else
                                TreeEmbedder.computeNodeLocationsForCircular(root, edgeLengths, edge2Angle, node2point);
                            TreeEmbedder.scaleAndCenterToFitTarget(getOptionLayout(), viewTab.getTargetDimensions(), node2point, false);
                            TreeEmbedder.computeEdgePointsForCircularRec(root, 0, edge2Angle, node2point, edge2controlPoints, CUBIC_CURVE_PARENT_CONTROL_DEFAULT, CUBIC_CURVE_CHILD_CONTROL_DEFAULT);
                            break;
                        }
                        default:
                        case LeftToRight: {
                            if (getOptionEdgeShape() == EdgeView2D.EdgeShape.Straight) {
                                setOptionEdgeLengths(TreeEmbedder.EdgeLengths.Cladogram);
                                TreeEmbedder.computeEmbeddingForTriangularLayoutRec(root, null, 0, 0, edgeLengths, node2point);
                                TreeEmbedder.scaleAndCenterToFitTarget(getOptionLayout(), viewTab.getTargetDimensions(), node2point, false);
                                TreeEmbedder.computeEdgePointsForRectilinearRec(root, node2point, edge2controlPoints, CUBIC_CURVE_PARENT_CONTROL_DEFAULT, CUBIC_CURVE_CHILD_CONTROL_DEFAULT);
                            } else {
                                final NodeFloatArray nodeHeights = new NodeFloatArray(tree); // height of edge
                                TreeEmbedder.setNodeHeightsRec(root, 0, nodeHeights, LEAF_GROUP_GAP_DEFAULT, PARENT_PLACEMENT_DEFAULT);

                                TreeEmbedder.computeNodeLocationsForRectilinearRec(root, 0, edgeLengths, nodeHeights, node2point);
                                TreeEmbedder.scaleAndCenterToFitTarget(getOptionLayout(), viewTab.getTargetDimensions(), node2point, false);
                                TreeEmbedder.computeEdgePointsForRectilinearRec(root, node2point, edge2controlPoints, CUBIC_CURVE_PARENT_CONTROL_DEFAULT, CUBIC_CURVE_CHILD_CONTROL_DEFAULT);
                            }
                            break;
                        }
                    }

                    final Font labelFont = Font.font(ProgramProperties.getDefaultFont().getFamily(), taxaBlock.getNtax() <= 64 ? 16 : Math.max(4, 12 - Math.log(taxaBlock.getNtax() - 64) / Math.log(2)));

                    // compute all views and put their parts into the appropriate groups
                    for (Node v : tree.nodes()) {
                        final StringBuilder buf = new StringBuilder();
                        final String text;
                        final String label = tree.getLabel(v);
                        if (label != null && (v.getOutDegree() == 0 || optionShowInternalNodeLabels.get())) {
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

                        final NodeView2D nodeView = viewTab.createNodeView(v, node2point.getValue(v), null, 0, 0, text);
                        if (text != null && text.length() > 0 && viewTab.getNodeLabel2Style().containsKey(text)) {
                            nodeView.setStyling(viewTab.getNodeLabel2Style().get(text));
                        }
                        if (nodeView.getLabel() != null) {
                            nodeView.getLabel().setFont(labelFont);
                        }
                        viewTab.getNode2view().put(v, nodeView);
                        viewTab.getNodesGroup().getChildren().addAll(nodeView.getShapeGroup());
                        viewTab.getNodeLabelsGroup().getChildren().addAll(nodeView.getLabelGroup());
                    }
                    for (Edge e : tree.edges()) {
                        final EdgeControlPoints controlPoints = edge2controlPoints.getValue(e);
                        final EdgeView2D edgeView = viewTab.createEdgeView(e, getOptionLayout(), getOptionEdgeShape(),
                                node2point.getValue(e.getSource()), controlPoints.getControl1(), controlPoints.getMid(),
                                controlPoints.getControl2(), controlPoints.getSupport(), node2point.getValue(e.getTarget()), null);
                        viewTab.getEdge2view().put(e, edgeView);

                        if (edgeView.getShape() != null)
                            viewTab.getEdgesGroup().getChildren().add(edgeView.getShape());
                        if (edgeView.getLabel() != null) {
                            viewTab.getEdgeLabelsGroup().getChildren().addAll(edgeView.getLabel());
                            if (edgeView.getLabel() != null) {
                                edgeView.getLabel().setFont(labelFont);
                            }
                        }

                    }
                }
                Platform.runLater(() -> viewTab.updateSelectionModels(tree, taxaBlock, child.getDocument()));
            }
        }
        child.show();

        if (changeListener != null)
            getConnector().stateProperty().removeListener(changeListener);
        changeListener = (c, o, n) -> child.getTab().getCenter().setDisable(n != UpdateState.VALID);
        getConnector().stateProperty().addListener(new WeakChangeListener<>(changeListener));

        progress.close();
    }

    public GraphLayout getOptionLayout() {
        return optionLayout.getValue();
    }

    public void setOptionLayout(GraphLayout layoutProperty) {
        this.optionLayout.setValue(layoutProperty);
    }

    public TreeEmbedder.EdgeLengths getOptionEdgeLengths() {
        return optionEdgeLengths.getValue();
    }

    public Property<TreeEmbedder.EdgeLengths> optionEdgeLengthsProperty() {
        return optionEdgeLengths;
    }

    public void setOptionEdgeLengths(TreeEmbedder.EdgeLengths edgeLengthsProperty) {
        this.optionEdgeLengths.setValue(edgeLengthsProperty);
    }

    public EdgeView2D.EdgeShape getOptionEdgeShape() {
        return optionEdgeShape.getValue();
    }

    public void setOptionEdgeShape(EdgeView2D.EdgeShape edgeShapeProperty) {
        this.optionEdgeShape.setValue(edgeShapeProperty);
    }

    public boolean isOptionShowInternalNodeLabels() {
        return optionShowInternalNodeLabels.get();
    }

    public void setOptionShowInternalNodeLabels(boolean optionShowInternalNodeLabels) {
        this.optionShowInternalNodeLabels.set(optionShowInternalNodeLabels);
    }

    public List<String> listOptions() {
        return Arrays.asList("optionRows", "optionCols", "optionFirstTree", "optionLayout", "optionEdgeLengths", "optionEdgeShape");
    }

    public int getOptionRows() {
        return optionRows.get();
    }

    public void setOptionRows(int optionRows) {
        this.optionRows.set(Math.max(1, optionRows));
    }

    public int getOptionCols() {
        return optionCols.get();
    }

    public void setOptionCols(int optionCols) {
        this.optionCols.set(Math.max(1, optionCols));
    }

    public int getOptionFirstTree() {
        return optionFirstTree.get();
    }

    public void setOptionFirstTree(int optionFirstTree) {
        this.optionFirstTree.set(Math.max(1, optionFirstTree));
    }
}

