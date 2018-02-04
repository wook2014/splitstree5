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
import jloda.graph.*;
import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToTreeView;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreeViewBlock;
import splitstree5.core.datablocks.TreesBlock;
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
public class TreeEmbedder extends Algorithm<TreesBlock, TreeViewBlock> implements IFromTrees, IToTreeView {

    public enum EdgeLengths {Weights, Uniform, Cladogram, CladogramEarlyBranching}

    public enum ParentPlacement {LeafAverage, ChildrenAverage}

    private final Property<GraphLayout> optionLayout = new SimpleObjectProperty<>(GraphLayout.LeftToRight);
    private final Property<EdgeLengths> optionEdgeLengths = new SimpleObjectProperty<>(EdgeLengths.Weights);
    private final Property<ParentPlacement> optionParentPlacement = new SimpleObjectProperty<>(ParentPlacement.ChildrenAverage);
    private final Property<EdgeView2D.EdgeShape> optionEdgeShape = new SimpleObjectProperty<>(EdgeView2D.EdgeShape.Angular);

    private final IntegerProperty optionCubicCurveParentControl = new SimpleIntegerProperty(20);
    private final IntegerProperty optionCubicCurveChildControl = new SimpleIntegerProperty(50);

    private final IntegerProperty optionLeafGroupGapProperty = new SimpleIntegerProperty(20);

    private final BooleanProperty optionShowInternalNodeLabels = new SimpleBooleanProperty();

    private final Map<String, FormatItem> nodeLabel2Style = new HashMap<>();

    private ChangeListener<UpdateState> changeListener;

    public TreeEmbedder() {
    }

    @Override
    public String getCitation() {
        return "Huson et al 2012;D.H. Huson, R. Rupp and C. Scornavacca, Phylogenetic Networks, Cambridge, 2012.";
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, TreesBlock parent, TreeViewBlock child) throws Exception {
        progress.setTasks("Tree viewer", "Init.");

        final TreeViewTab view = child.getTab();
        view.setNodeLabel2Style(nodeLabel2Style);
        view.setDataNode(child.getDataNode());

        Platform.runLater(() -> {
            child.getTab().setName(child.getName());
            view.setLayout(getOptionLayout());
        });

        if (parent.getNTrees() > 0) {
            final PhyloTree tree = parent.getTrees().get(0);
            view.init(tree);

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
                        setAnglesForCircularLayoutRec(root, null, 0, tree.getNumberOfLeaves(), edge2Angle);

                        if (getOptionEdgeShape() == EdgeView2D.EdgeShape.Straight)
                            computeNodeLocationsForRadialRec(root, new Point2D(0, 0), edgeLengths, edge2Angle, node2point);
                        else
                            computeNodeLocationsForCircular(root, edgeLengths, edge2Angle, node2point);
                        scaleToFitTarget(getOptionLayout(), view.getTargetDimensions(), node2point);
                        computeEdgePointsForCircularRec(root, 0, edge2Angle, node2point, edge2controlPoints);

                        break;
                    }
                    default:
                    case LeftToRight: {
                        if (getOptionEdgeShape() == EdgeView2D.EdgeShape.Straight) {
                            setOptionEdgeLengths(EdgeLengths.Cladogram);
                            computeEmbeddingForTriangularLayoutRec(root, null, 0, 0, edgeLengths, node2point);
                            scaleToFitTarget(getOptionLayout(), view.getTargetDimensions(), node2point);
                            computeEdgePointsForRectilinearRec(root, node2point, edge2controlPoints);
                        } else {
                            final NodeFloatArray nodeHeights = new NodeFloatArray(tree); // angle of edge
                            setNodeHeightsRec(root, 0, nodeHeights);
                            computeNodeLocationsForRectilinearRec(root, 0, edgeLengths, nodeHeights, node2point);
                            scaleToFitTarget(getOptionLayout(), view.getTargetDimensions(), node2point);
                            computeEdgePointsForRectilinearRec(root, node2point, edge2controlPoints);
                        }
                        break;
                    }
                }

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

                    final NodeView2D nodeView = view.createNodeView(v, node2point.getValue(v), text);
                    if (text != null && text.length() > 0 && view.getNodeLabel2Style().containsKey(text)) {
                        nodeView.setStyling(view.getNodeLabel2Style().get(text));
                    }

                    view.getNode2view().put(v, nodeView);
                    view.getNodesGroup().getChildren().addAll(nodeView.getShapeGroup());
                    view.getNodeLabelsGroup().getChildren().addAll(nodeView.getLabelGroup());
                }
                for (Edge e : tree.edges()) {
                    final EdgeControlPoints controlPoints = edge2controlPoints.getValue(e);
                    final EdgeView2D edgeView = view.createEdgeView(e, getOptionLayout(), getOptionEdgeShape(), tree.getWeight(e),
                            node2point.getValue(e.getSource()), controlPoints.getControl1(), controlPoints.getMid(),
                            controlPoints.getControl2(), controlPoints.getSupport(), node2point.getValue(e.getTarget()));
                    view.getEdge2view().put(e, edgeView);
                    if (edgeView.getShape() != null)
                        view.getEdgesGroup().getChildren().add(edgeView.getShape());
                    if (edgeView.getLabel() != null)
                        view.getEdgeLabelsGroup().getChildren().addAll(edgeView.getLabel());
                }
            }
            Platform.runLater(() -> view.updateSelectionModels(tree, taxaBlock, child.getDocument()));
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
    public static void scaleToFitTarget(GraphLayout optionLayout, Dimension2D target, NodeArray<Point2D> node2point) {
        // scale to target dimensions:
        final float factorX;
        final float factorY;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        {
            float minX = Float.MAX_VALUE;
            float minY = Float.MAX_VALUE;
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

        if (factorX != 1 || factorY != 1) {
            for (Node v : node2point.keys()) {
                final Point2D point = node2point.getValue(v);
                if (point != null)
                    node2point.setValue(v, new Point2D(factorX * point.getX(), factorY * point.getY()));
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
     * @return number of leaves visited
     */
    private int setAnglesForCircularLayoutRec(final Node v, final Edge f, int nextLeafNum, final int angleParts, final EdgeFloatArray edgeAngles) {
        if (v.getOutDegree() == 0) {
            if (f != null)
                edgeAngles.put(f, (float) (360.0 / angleParts * nextLeafNum));
            return nextLeafNum + 1;
        } else {
            if (isAllChildrenAreLeaves(v)) { // treat these separately because we want to place them all slightly closer together
                final int numberOfChildren = v.getOutDegree();
                final float firstAngle = (float) ((360.0 / angleParts) * (nextLeafNum + optionLeafGroupGapProperty.get() / 200f));
                final float deltaAngle = (float) ((360.0 / angleParts) * (nextLeafNum + numberOfChildren - 1.0 - optionLeafGroupGapProperty.get() / 200f)) - firstAngle;
                float angle = firstAngle;
                for (Edge e : v.outEdges()) {
                    edgeAngles.put(e, angle);
                    angle += deltaAngle;
                }
                edgeAngles.put(f, (float) ((360.0 / angleParts) * (nextLeafNum + 0.5 * (numberOfChildren - 1))));
                nextLeafNum += numberOfChildren;
                //edgeAngles.set(f, 0.5f * (firstAngle + lastAngle));
            } else {
                final float firstLeaf = nextLeafNum;
                float firstAngle = Float.MIN_VALUE;
                float lastAngle = Float.MIN_VALUE;

                for (Edge e : v.outEdges()) {
                    nextLeafNum = setAnglesForCircularLayoutRec(e.getTarget(), e, nextLeafNum, angleParts, edgeAngles);
                    final float angle = edgeAngles.getValue(e);
                    if (firstAngle == Float.MIN_VALUE)
                        firstAngle = angle;
                    lastAngle = angle;
                }

                if (f != null) {
                    if (optionParentPlacement.getValue() == ParentPlacement.ChildrenAverage)
                        edgeAngles.put(f, 0.5f * (firstAngle + lastAngle));
                    else {
                        edgeAngles.put(f, (float) (180.0 / angleParts * (firstLeaf + nextLeafNum - 1)));
                    }
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
    private void computeNodeLocationsForRadialRec(Node v, Point2D vPoint, EdgeFloatArray edgeLengths, EdgeFloatArray edgeAngles, NodeArray<Point2D> node2point) {
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
    private void computeNodeLocationsForCircular(Node root, EdgeFloatArray edgeLengths, EdgeFloatArray edgeAngles, NodeArray<Point2D> node2point) {
        Point2D rootLocation = new Point2D(0.2, 0);
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
    private void computeNodeLocationAndViewForCicularRec(Point2D origin, Node v, Point2D vLocation, Edge e, EdgeFloatArray edgeLengths,
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
     *
     * @param v
     * @param vAngle
     * @param angles
     */
    protected void computeEdgePointsForCircularRec(Node v, float vAngle, EdgeFloatArray angles, NodeArray<Point2D> node2points, EdgeArray<EdgeControlPoints> edge2controlPoints) {
        Point2D start = node2points.getValue(v);
        for (Edge e : v.outEdges()) {
            final Node w = e.getTarget();
            final Point2D end = node2points.getValue(w);

            float wAngle = angles.getValue(e);
            double distance = Math.max(1, end.magnitude() - start.magnitude());
            final Point2D control1 = start.multiply(1 + getOptionCubicCurveParentControl() * distance / (100 * start.magnitude()));
            final Point2D control2 = end.multiply(1 - getOptionCubicCurveChildControl() * distance / (100 * end.magnitude()));
            final Point2D mid = GeometryUtils.rotate(start, wAngle - vAngle);
            final EdgeControlPoints edgeControlPoints = new EdgeControlPoints(control1, mid, control2);
            edge2controlPoints.put(e, edgeControlPoints);

            computeEdgePointsForCircularRec(w, wAngle, angles, node2points, edge2controlPoints);
        }
    }

    /**
     * set the node heights for a rectilinear layout
     *
     * @param v
     * @param nextLeafRank
     * @param nodeHeights
     * @return next leaf height
     */
    private float setNodeHeightsRec(final Node v, float nextLeafRank, final NodeFloatArray nodeHeights) {
        if (v.getOutDegree() == 0) {
            nodeHeights.setValue(v, nextLeafRank);
            return nextLeafRank + 1;
        } else {

            if (isAllChildrenAreLeaves(v)) { // treat these separately because we want to place them all slightly closer together
                final int numberOfChildren = v.getOutDegree();
                final float firstHeight = (nextLeafRank + optionLeafGroupGapProperty.get() / 200.0f);
                final float deltaHeight = (nextLeafRank + numberOfChildren - 1 - optionLeafGroupGapProperty.get() / 200.0f) - firstHeight;
                float lastHeight = 0;
                float height = firstHeight;
                for (Edge e : v.outEdges()) {
                    nodeHeights.setValue(e.getTarget(), height);
                    lastHeight = height;
                    height += deltaHeight;
                }
                nodeHeights.setValue(v, 0.5f * (firstHeight + lastHeight));
                return nextLeafRank + numberOfChildren;
            } else {
                final float firstLeafRnak = nextLeafRank;
                float lastLeaf = 0;
                float firstHeight = Float.MIN_VALUE;
                float lastHeight = 0;

                for (Edge e : v.outEdges()) {
                    nextLeafRank = setNodeHeightsRec(e.getTarget(), nextLeafRank, nodeHeights);
                    final float eh = nodeHeights.getValue(e.getTarget());
                    if (firstHeight == Float.MIN_VALUE)
                        firstHeight = eh;
                    lastHeight = eh;
                    lastLeaf = nextLeafRank;
                }
                if (optionParentPlacementProperty().getValue() == ParentPlacement.ChildrenAverage)
                    nodeHeights.setValue(v, 0.5f * (firstHeight + lastHeight));
                else
                    nodeHeights.setValue(v, 0.5f * (firstLeafRnak + lastLeaf - 1));
                return nextLeafRank;
            }
        }
    }

    private boolean isAllChildrenAreLeaves(Node v) {
        for (Edge e : v.outEdges()) {
            if (e.getTarget().getOutDegree() > 0)
                return false;
        }
        return true;
    }

    /**
     * recursively set node coordinates for rectilinear view
     */
    private void computeNodeLocationsForRectilinearRec(Node v, final float x0, EdgeFloatArray edgeLengths, NodeFloatArray nodeHeights, NodeArray<Point2D> node2point) {
        node2point.setValue(v, new Point2D(x0, nodeHeights.getValue(v)));
        for (Edge e : v.outEdges()) {
            computeNodeLocationsForRectilinearRec(e.getTarget(), x0 + edgeLengths.getValue(e), edgeLengths, nodeHeights, node2point);
        }
    }

    /**
     * compute edge points
     */
    private void computeEdgePointsForRectilinearRec(Node v, NodeArray<Point2D> node2point, EdgeArray<EdgeControlPoints> edge2controlPoints) {
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
                final Point2D control1 = start.add(optionCubicCurveParentControl.get() / 100.0 * (support.getX() - start.getX()), 0);
                final Point2D control2 = support.add(-optionCubicCurveChildControl.get() / 100.0 * (support.getX() - start.getX()), 0);

                edge2controlPoints.put(e, new EdgeControlPoints(control1, mid, control2, support));

                computeEdgePointsForRectilinearRec(w, node2point, edge2controlPoints);
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
    private int computeEmbeddingForTriangularLayoutRec(Node v, Edge e, double hDistToRoot, int leafNumber, EdgeFloatArray edgeLengths, NodeArray<Point2D> node2point) {
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

