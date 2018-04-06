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
import javafx.geometry.Dimension2D;
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
import splitstree5.gui.graphtab.base.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * compute a visualization of a tree
 * Daniel Huson, 11.2017
 * todo: add support for rooted networks (as in Dendroscope)
 */
public class TreeEmbedder extends Algorithm<TreesBlock, ViewerBlock> implements IFromTrees, IToViewer {

    public enum EdgeLengths {Weights, Uniform, Cladogram, CladogramEarlyBranching}

    public enum ParentPlacement {LeafAverage, ChildrenAverage}

    private final Property<GraphLayout> optionLayout = new SimpleObjectProperty<>(GraphLayout.LeftToRight);
    private final Property<EdgeLengths> optionEdgeLengths = new SimpleObjectProperty<>(EdgeLengths.Weights);

    private final Property<EdgeView2D.EdgeShape> optionEdgeShape = new SimpleObjectProperty<>(EdgeView2D.EdgeShape.Angular);

    public static final ParentPlacement PARENT_PLACEMENT_DEFAULT = ParentPlacement.ChildrenAverage;
    private final Property<ParentPlacement> optionParentPlacement = new SimpleObjectProperty<>(PARENT_PLACEMENT_DEFAULT);

    public static final int CUBIC_CURVE_PARENT_CONTROL_DEFAULT = 20;
    private final IntegerProperty optionCubicCurveParentControl = new SimpleIntegerProperty(CUBIC_CURVE_PARENT_CONTROL_DEFAULT);

    public static final int CUBIC_CURVE_CHILD_CONTROL_DEFAULT = 50;
    private final IntegerProperty optionCubicCurveChildControl = new SimpleIntegerProperty(CUBIC_CURVE_CHILD_CONTROL_DEFAULT);

    public static final int LEAF_GROUP_GAP_DEFAULT = 20;
    private final IntegerProperty optionLeafGroupGapProperty = new SimpleIntegerProperty(LEAF_GROUP_GAP_DEFAULT);

    private final BooleanProperty optionShowInternalNodeLabels = new SimpleBooleanProperty();

    private final Map<String, FormatItem> nodeLabel2Style = new HashMap<>();

    private ChangeListener<UpdateState> changeListener;

    @Override
    public String getCitation() {
        return "Huson et al 2012;D.H. Huson, R. Rupp and C. Scornavacca, Phylogenetic Networks, Cambridge, 2012.";
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, TreesBlock parent, ViewerBlock child) throws Exception {
        progress.setTasks("Tree viewer", "Init.");

        final TreeViewTab viewTab = (TreeViewTab) child.getTab();
        viewTab.setNodeLabel2Style(nodeLabel2Style);
        viewTab.setDataNode(child.getDataNode());

        Platform.runLater(() -> {
            child.getTab().setName(child.getName());
            viewTab.setLayout(getOptionLayout());
        });

        if (parent.getNTrees() > 0) {
            final PhyloTree tree = parent.getTrees().get(0);
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
                        setAnglesForCircularLayoutRec(root, null, 0, tree.getNumberOfLeaves(), edge2Angle, optionLeafGroupGapProperty.get(), optionParentPlacement.getValue());

                        if (getOptionEdgeShape() == EdgeView2D.EdgeShape.Straight)
                            computeNodeLocationsForRadialRec(root, new Point2D(0, 0), edgeLengths, edge2Angle, node2point);
                        else
                            computeNodeLocationsForCircular(root, edgeLengths, edge2Angle, node2point);
                        scaleAndCenterToFitTarget(getOptionLayout(), viewTab.getTargetDimensions(), node2point, false);
                        computeEdgePointsForCircularRec(root, 0, edge2Angle, node2point, edge2controlPoints, getOptionCubicCurveParentControl(), getOptionCubicCurveChildControl());
                        break;
                    }
                    default:
                    case LeftToRight: {
                        if (getOptionEdgeShape() == EdgeView2D.EdgeShape.Straight) {
                            setOptionEdgeLengths(EdgeLengths.Cladogram);
                            computeEmbeddingForTriangularLayoutRec(root, null, 0, 0, edgeLengths, node2point);
                            scaleAndCenterToFitTarget(getOptionLayout(), viewTab.getTargetDimensions(), node2point, false);
                            computeEdgePointsForRectilinearRec(root, node2point, edge2controlPoints, optionCubicCurveParentControl.get(), getOptionCubicCurveChildControl());
                        } else {
                            final NodeFloatArray nodeHeights = new NodeFloatArray(tree); // height of edge
                            setNodeHeightsRec(root, 0, nodeHeights, optionLeafGroupGapProperty.get(), optionParentPlacementProperty().getValue());

                            computeNodeLocationsForRectilinearRec(root, 0, edgeLengths, nodeHeights, node2point);
                            scaleAndCenterToFitTarget(getOptionLayout(), viewTab.getTargetDimensions(), node2point, false);
                            computeEdgePointsForRectilinearRec(root, node2point, edge2controlPoints, optionCubicCurveParentControl.get(), getOptionCubicCurveChildControl());
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
        child.show();

        if (changeListener != null)
            getConnector().stateProperty().removeListener(changeListener);
        changeListener = (c, o, n) -> child.getTab().getCenter().setDisable(n != UpdateState.VALID);
        getConnector().stateProperty().addListener(new WeakChangeListener<>(changeListener));

        progress.close();
    }


    /**
     * scale all node coordinates so that they fit into the current scene
     *
     * @param optionLayout
     * @param target
     * @param node2point
     */
    static void scaleAndCenterToFitTarget(GraphLayout optionLayout, Dimension2D target, NodeArray<Point2D> node2point, boolean center) {
        // scale to target dimensions:
        final float factorX;
        final float factorY;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;

        {
            for (Point2D point : node2point.values()) {
                if (point != null) {
                    minX = Math.min(minX, (float) point.getX());
                    maxX = Math.max(maxX, (float) point.getX());
                    minY = Math.min(minY, (float) point.getY());
                    maxY = Math.max(maxY, (float) point.getY());
                }
            }

            if (optionLayout == GraphLayout.LeftToRight) {
                factorX = (float) (target.getWidth() - 50) / (maxX - minX);
                factorY = (float) (target.getHeight() - 50) / (maxY - minY);
            } else {
                factorX = factorY = (float) Math.min(((target.getWidth() - 50) / (maxX - minX)), ((target.getHeight() - 50) / (maxY - minY)));
            }
        }
        float midX = 0.5f * (maxX + minX);
        float midY = 0.5f * (maxY + minY);

        if (factorX != 1 || factorY != 1) {
            for (Node v : node2point.keys()) {
                final Point2D point = node2point.getValue(v);
                if (point != null)
                    node2point.setValue(v, new Point2D(factorX * (point.getX() - (center ? midX : 0)), factorY * (point.getY() - (center ? midY : 0))));
            }
        }
    }

    /**
     * Recursively determines the angle of every edge in a circular layout
     *
     * @param v
     * @param f
     * @param nextLeafNum
     * @param angleParts
     * @param edgeAngles
     * @param leafGroupGap
     * @param parentPlacement
     * @return number of leaves visited
     */
    static int setAnglesForCircularLayoutRec(final Node v, final Edge f, int nextLeafNum, final int angleParts, final EdgeFloatArray edgeAngles, float leafGroupGap, ParentPlacement parentPlacement) {
        if (v.getOutDegree() == 0) {
            if (f != null)
                edgeAngles.put(f, 360f / angleParts * nextLeafNum);
            return nextLeafNum + 1;
        } else if (v.getDegree() >= 2 && isAllChildrenAreLeaves(v)) { // treat these separately because we want to place them all slightly closer together
            final int numberOfChildren = v.getOutDegree();
            final float firstAngle = (360f / angleParts) * (nextLeafNum + leafGroupGap / 200f);
            final float lastAngle = (360f / angleParts) * (nextLeafNum + numberOfChildren - 1 - leafGroupGap / 200f);
            final float deltaAngle = (lastAngle - firstAngle) / (numberOfChildren - 1);
            float angle = firstAngle;
            for (Edge e : v.outEdges()) {
                edgeAngles.put(e, angle);
                angle += deltaAngle;
            }
            edgeAngles.put(f, (360f / angleParts) * (nextLeafNum + 0.5f * (numberOfChildren - 1)));
            nextLeafNum += numberOfChildren;
            //edgeAngles.set(f, 0.5f * (firstAngle + lastAngle));
            return nextLeafNum;
        } else {
            final float firstLeaf = nextLeafNum;
            float firstAngle = Float.MIN_VALUE;
            float lastAngle = Float.MIN_VALUE;

            for (Edge e : v.outEdges()) {
                nextLeafNum = setAnglesForCircularLayoutRec(e.getTarget(), e, nextLeafNum, angleParts, edgeAngles, leafGroupGap, parentPlacement);
                final float angle = edgeAngles.getValue(e);
                if (firstAngle == Float.MIN_VALUE)
                    firstAngle = angle;
                lastAngle = angle;
            }

            if (f != null) {
                if (parentPlacement == ParentPlacement.ChildrenAverage)
                    edgeAngles.put(f, 0.5f * (firstAngle + lastAngle));
                else {
                    edgeAngles.put(f, 180f / angleParts * (firstLeaf + nextLeafNum - 1));
                }
            }
            return nextLeafNum;
        }
    }

    /**
     * set the locations of all nodes in a radial tree layout
     *
     * @param v
     * @param vPoint
     * @param edgeLengths
     * @param edgeAngles
     * @param node2point
     */
    static void computeNodeLocationsForRadialRec(Node v, Point2D vPoint, EdgeFloatArray edgeLengths, EdgeFloatArray edgeAngles, NodeArray<Point2D> node2point) {
        node2point.setValue(v, vPoint);
        for (Edge e : v.outEdges()) {
            final Node w = e.getTarget();
            final Point2D wLocation = GeometryUtils.translateByAngle(vPoint, edgeAngles.getValue(e), 1000 * edgeLengths.getValue(e));
            node2point.setValue(w, wLocation);
            computeNodeLocationsForRadialRec(w, wLocation, edgeLengths, edgeAngles, node2point);
        }
    }

    /**
     * set the coordinates for all nodes and interior edge points
     *
     * @param root       root of tree
     * @param edgeAngles assignment of angles to edges
     */
    static void computeNodeLocationsForCircular(Node root, EdgeFloatArray edgeLengths, EdgeFloatArray edgeAngles, NodeArray<Point2D> node2point) {
        Point2D rootLocation = new Point2D(0.25, 0);
        node2point.setValue(root, rootLocation);
        for (Edge e : root.outEdges()) {
            final Node w = e.getTarget();
            final Point2D wLocation = GeometryUtils.translateByAngle(rootLocation, edgeAngles.getValue(e), 1000 * edgeLengths.getValue(e));
            node2point.setValue(w, wLocation);
            computeNodeLocationAndViewForCicularRec(rootLocation, w, wLocation, e, edgeLengths, edgeAngles, node2point);
        }
    }

    /**
     * recursively compute node coordinates and view from edge angles:
     *
     * @param origin     location of origin
     * @param v          Node
     * @param e          Edge
     * @param edgeAngles EdgeDouble
     */
    static void computeNodeLocationAndViewForCicularRec(Point2D origin, Node v, Point2D vLocation, Edge e, EdgeFloatArray edgeLengths,
                                                        EdgeFloatArray edgeAngles, NodeArray<Point2D> node2point) {
        for (Edge f : v.outEdges()) {
            final Node w = f.getTarget();
            final Point2D b = GeometryUtils.rotateAbout(vLocation, edgeAngles.getValue(f) - edgeAngles.getValue(e), origin);
            final Point2D c = GeometryUtils.translateByAngle(b, edgeAngles.getValue(f), 1000 * edgeLengths.getValue(f));
            node2point.setValue(w, c);
            computeNodeLocationAndViewForCicularRec(origin, w, c, f, edgeLengths, edgeAngles, node2point);
        }
    }

    /**
     * compute all edge points and setup edge views
     * @param v
     * @param vAngle
     * @param angles
     * @param cubicCurveParentControl
     * @param cubicCurveChildControl
     */
    static void computeEdgePointsForCircularRec(Node v, float vAngle, EdgeFloatArray angles, NodeArray<Point2D> node2points, EdgeArray<EdgeControlPoints> edge2controlPoints, int cubicCurveParentControl, int cubicCurveChildControl) {
        Point2D start = node2points.getValue(v);
        for (Edge e : v.outEdges()) {
            final Node w = e.getTarget();
            final Point2D end = node2points.getValue(w);

            float wAngle = angles.getValue(e);
            double distance = Math.max(1, end.magnitude() - start.magnitude());
            final Point2D control1 = start.multiply(1 + cubicCurveParentControl * distance / (100 * start.magnitude()));
            final Point2D control2 = end.multiply(1 - cubicCurveChildControl * distance / (100 * end.magnitude()));
            final Point2D mid = GeometryUtils.rotate(start, wAngle - vAngle);
            final EdgeControlPoints edgeControlPoints = new EdgeControlPoints(control1, mid, control2);
            edge2controlPoints.put(e, edgeControlPoints);

            computeEdgePointsForCircularRec(w, wAngle, angles, node2points, edge2controlPoints, cubicCurveParentControl, cubicCurveChildControl);
        }
    }

    /**
     * set the node heights for a rectilinear layout
     *
     * @param v
     * @param nextLeafRank
     * @param nodeHeights
     * @param leafGroupGap
     * @param parentPlacement
     * @return next leaf height
     */
    static float setNodeHeightsRec(final Node v, float nextLeafRank, final NodeFloatArray nodeHeights, float leafGroupGap, ParentPlacement parentPlacement) {
        if (v.getOutDegree() == 0) {
            nodeHeights.setValue(v, nextLeafRank);
            return nextLeafRank + 1;
        } else if (v.getDegree() >= 2 && isAllChildrenAreLeaves(v)) { // treat these separately because we want to place them all slightly closer together
            final int numberOfChildren = v.getOutDegree();
            final float firstHeight = (nextLeafRank + leafGroupGap / 200.0f);
            final float lastHeight = (nextLeafRank + (numberOfChildren - 1) - leafGroupGap / 200.0f);
            final float deltaHeight = (lastHeight - firstHeight) / (numberOfChildren - 1);
            float height = firstHeight;
            for (Edge e : v.outEdges()) {
                nodeHeights.setValue(e.getTarget(), height);
                height += deltaHeight;
            }
            nodeHeights.setValue(v, 0.5f * (firstHeight + lastHeight));
            return nextLeafRank + numberOfChildren;
        } else {
            final float firstLeafRank = nextLeafRank;
            float lastLeaf = 0;
            float firstHeight = Float.MIN_VALUE;
            float lastHeight = 0;

            for (Edge e : v.outEdges()) {
                nextLeafRank = setNodeHeightsRec(e.getTarget(), nextLeafRank, nodeHeights, leafGroupGap, parentPlacement);
                final float eh = nodeHeights.getValue(e.getTarget());
                if (firstHeight == Float.MIN_VALUE)
                    firstHeight = eh;
                lastHeight = eh;
                lastLeaf = nextLeafRank;
            }
            if (parentPlacement == ParentPlacement.ChildrenAverage)
                nodeHeights.setValue(v, 0.5f * (firstHeight + lastHeight));
            else
                nodeHeights.setValue(v, 0.5f * (firstLeafRank + lastLeaf - 1));
            return nextLeafRank;
        }
    }

    static boolean isAllChildrenAreLeaves(Node v) {
        for (Edge e : v.outEdges()) {
            if (e.getTarget().getOutDegree() > 0)
                return false;
        }
        return true;
    }

    /**
     * recursively set node coordinates for rectilinear view
     */
    static void computeNodeLocationsForRectilinearRec(Node v, final float x0, EdgeFloatArray edgeLengths, NodeFloatArray nodeHeights, NodeArray<Point2D> node2point) {
        node2point.setValue(v, new Point2D(x0, nodeHeights.getValue(v)));
        for (Edge e : v.outEdges()) {
            computeNodeLocationsForRectilinearRec(e.getTarget(), x0 + edgeLengths.getValue(e), edgeLengths, nodeHeights, node2point);
        }
    }

    /**
     * compute edge points
     */
    static void computeEdgePointsForRectilinearRec(Node v, NodeArray<Point2D> node2point, EdgeArray<EdgeControlPoints> edge2controlPoints, int cubicCurveParentControl, int cubicCurveChildControl) {
        Point2D closestChild = null;
        for (Edge e : v.outEdges()) {
            final Point2D point = node2point.getValue(e.getTarget());
            if (closestChild == null || point.getX() < closestChild.getX())
                closestChild = point;
        }

        if (closestChild != null) {
            final Point2D start = node2point.getValue(v);
            for (Edge e : v.outEdges()) {
                final Node w = e.getTarget();
                final Point2D end = node2point.getValue(w);
                final Point2D mid = new Point2D(start.getX(), end.getY());
                final Point2D support = new Point2D(closestChild.getX(), end.getY());
                final Point2D control1 = start.add(cubicCurveParentControl / 100.0 * (support.getX() - start.getX()), 0);
                final Point2D control2 = support.add(-cubicCurveChildControl / 100.0 * (support.getX() - start.getX()), 0);

                edge2controlPoints.put(e, new EdgeControlPoints(control1, mid, control2, support));

                computeEdgePointsForRectilinearRec(w, node2point, edge2controlPoints, cubicCurveParentControl, cubicCurveChildControl);
            }
        }
    }

    /**
     * recursively compute the embedding
     *
     * @param v
     * @param e
     * @param hDistToRoot horizontal distance from node to root
     * @param leafNumber  rank of leaf in vertical ordering
     * @return index of last leaf
     */
    static int computeEmbeddingForTriangularLayoutRec(Node v, Edge e, double hDistToRoot, int leafNumber, EdgeFloatArray edgeLengths, NodeArray<Point2D> node2point) {
        if (v.getOutDegree() == 0 && e != null)  // hit a leaf
        {
            node2point.setValue(v, new Point2D(0, ++leafNumber)); // root node
        } else {
            int old = leafNumber + 1;
            for (Edge f = v.getFirstAdjacentEdge(); f != null; f = v.getNextAdjacentEdge(f)) {
                if (f != e) {
                    Node w = f.getOpposite(v);
                    leafNumber = computeEmbeddingForTriangularLayoutRec(w, f, hDistToRoot + edgeLengths.getValue(f), leafNumber, edgeLengths, node2point);
                }
            }
            node2point.setValue(v, new Point2D(-0.5 * (leafNumber - old), 0.5 * (leafNumber + old)));
        }
        return leafNumber;
    }


    public GraphLayout getOptionLayout() {
        return optionLayout.getValue();
    }

    public Property<GraphLayout> optionLayoutProperty() {
        return optionLayout;
    }

    public String getShortDescriptionOptionLayout() {
        return "Sets the tree optionLayout to radial or fromLeft";
    }

    public void setOptionLayout(GraphLayout layoutProperty) {
        this.optionLayout.setValue(layoutProperty);
    }

    public EdgeLengths getOptionEdgeLengths() {
        return optionEdgeLengths.getValue();
    }

    public Property<EdgeLengths> optionEdgeLengthsProperty() {
        return optionEdgeLengths;
    }

    public void setOptionEdgeLengths(EdgeLengths edgeLengthsProperty) {
        this.optionEdgeLengths.setValue(edgeLengthsProperty);
    }

    public ParentPlacement getOptionParentPlacement() {
        return optionParentPlacement.getValue();
    }

    public Property<ParentPlacement> optionParentPlacementProperty() {
        return optionParentPlacement;
    }

    public void setOptionParentPlacement(ParentPlacement parentPlacementProperty) {
        this.optionParentPlacement.setValue(parentPlacementProperty);
    }

    public EdgeView2D.EdgeShape getOptionEdgeShape() {
        return optionEdgeShape.getValue();
    }

    public Property<EdgeView2D.EdgeShape> optionEdgeShapeProperty() {
        return optionEdgeShape;
    }

    public void setOptionEdgeShape(EdgeView2D.EdgeShape edgeShapeProperty) {
        this.optionEdgeShape.setValue(edgeShapeProperty);
    }

    public int getOptionCubicCurveParentControl() {
        return optionCubicCurveParentControl.get();
    }

    public IntegerProperty optionCubicCurveParentControlProperty() {
        return optionCubicCurveParentControl;
    }

    public void setOptionCubicCurveParentControl(int parentScale) {
        this.optionCubicCurveParentControl.set(parentScale);
    }

    public int getOptionCubicCurveChildControl() {
        return optionCubicCurveChildControl.get();
    }

    public IntegerProperty optionCubicCurveChildControlProperty() {
        return optionCubicCurveChildControl;
    }

    public void setOptionCubicCurveChildControl(int childScale) {
        this.optionCubicCurveChildControl.set(childScale);
    }

    public int getOptionLeafGroupGapProperty() {
        return optionLeafGroupGapProperty.get();
    }

    public IntegerProperty optionLeafGroupGapPropertyProperty() {
        return optionLeafGroupGapProperty;
    }

    public void setOptionLeafGroupGapProperty(int leafGroupGapProperty) {
        this.optionLeafGroupGapProperty.set(leafGroupGapProperty);
    }

    public boolean isOptionShowInternalNodeLabels() {
        return optionShowInternalNodeLabels.get();
    }

    public BooleanProperty optionShowInternalNodeLabelsProperty() {
        return optionShowInternalNodeLabels;
    }

    public void setOptionShowInternalNodeLabels(boolean optionShowInternalNodeLabels) {
        this.optionShowInternalNodeLabels.set(optionShowInternalNodeLabels);
    }

    public List<String> listOptions() {
        return Arrays.asList("optionLayout", "optionEdgeLengths", "optionEdgeShape", "optionParentPlacement",
                "optionLeafGroupGapProperty", "optionCublicCurveChildControl", "optionCubicCurveParentControl");
    }
}

