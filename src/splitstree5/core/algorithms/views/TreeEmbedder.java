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

/*
 *  Copyright (C) 2017 Daniel H. Huson
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

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import jloda.graph.*;
import jloda.phylo.PhyloGraph;
import jloda.phylo.PhyloTree;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToTreeView;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreeViewBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.main.graphtab.TreeViewTab;
import splitstree5.main.graphtab.base.*;

import java.util.Arrays;
import java.util.List;

/**
 * compute a visualization of a tree
 * Daniel Huson, 11.2017
 * todo: add support for rooted networks (as in Dendroscope)
 */
public class TreeEmbedder extends Algorithm<TreesBlock, TreeViewBlock> implements IFromTrees, IToTreeView {

    public enum EdgeLengths {Weights, Uniform, Cladogram, CladogramEarlyBranching}

    public enum ParentPlacement {LeafAverage, ChildrenAverage}


    private final Property<GraphLayout> layout = new SimpleObjectProperty<>(GraphLayout.LeftToRight);
    private final Property<EdgeLengths> edgeLengths = new SimpleObjectProperty<>(EdgeLengths.Weights);
    private final Property<ParentPlacement> parentPlacement = new SimpleObjectProperty<>(ParentPlacement.ChildrenAverage);
    private final Property<AEdgeView.EdgeShape> edgeShape = new SimpleObjectProperty<>(AEdgeView.EdgeShape.Straight);

    private final IntegerProperty cubicCurveParentControl = new SimpleIntegerProperty(20);
    private final IntegerProperty cubicCurveChildControl = new SimpleIntegerProperty(50);

    private final IntegerProperty leafGroupGapProperty = new SimpleIntegerProperty(20);

    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, TreesBlock parent, TreeViewBlock child) throws Exception {
        progressListener.setTasks("Tree viewer", "Init.");

        final TreeViewTab view = child.getTreeViewTab();

        view.setLayout(getOptionLayout());

        if (parent.getNTrees() > 0) {
            final PhyloTree tree = parent.getTrees().get(0);
            view.init(tree);
            view.updateSelectionModels(tree);

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

                System.err.println("In compute");

                // compute edge lengths to reflect desired topology
                final EdgeFloatArray edgeLengths = EdgeLengthsCalculation.computeEdgeLengths(tree, getOptionEdgeLengths());

                // compute all coordinates:
                final NodeArray<Point2D> node2point = new NodeArray<>(tree);
                final EdgeArray<EdgeControlPoints> edge2controlPoints = new EdgeArray<>(tree);

                switch (getOptionLayout()) {
                    case Radial: {
                        final EdgeFloatArray edge2Angle = new EdgeFloatArray(tree); // angle of edge
                        setAnglesForCircularLayoutRec(root, null, 0, tree.getNumberOfLeaves(), edge2Angle);

                        if (getOptionEdgeShape() == AEdgeView.EdgeShape.Straight)
                            computeNodeLocationsForRadialRec(root, new Point2D(0, 0), edgeLengths, edge2Angle, node2point);
                        else
                            computeNodeLocationsForCircular(root, edgeLengths, edge2Angle, node2point);
                        scaleToFitTarget(getOptionLayout(), view.getTargetDimensions(), tree, node2point);
                        computeEdgePointsForCircularRec(root, 0, edge2Angle, node2point, edge2controlPoints);

                        break;
                    }
                    default:
                    case LeftToRight: {
                        if (getOptionEdgeShape() == AEdgeView.EdgeShape.Straight) {
                            setOptionEdgeLengths(EdgeLengths.Cladogram);
                            computeEmbeddingForTriangularLayoutRec(root, null, 0, 0, edgeLengths, node2point);
                            scaleToFitTarget(getOptionLayout(), view.getTargetDimensions(), tree, node2point);
                            computeEdgePointsForRectilinearRec(root, node2point, edge2controlPoints);
                        } else {
                            final NodeFloatArray nodeHeights = new NodeFloatArray(tree); // angle of edge
                            setNodeHeightsRec(root, 0, nodeHeights);
                            computeNodeLocationsForRectilinearRec(root, 0, edgeLengths, nodeHeights, node2point);
                            scaleToFitTarget(getOptionLayout(), view.getTargetDimensions(), tree, node2point);
                            computeEdgePointsForRectilinearRec(root, node2point, edge2controlPoints);
                        }
                        break;
                    }
                }

                // compute all views and put their parts into the appropriate groups
                for (Node v : tree.nodes()) {
                    String text = (tree.getLabel(v) != null ? tree.getLabel(v) : "Node " + v.getId());
                    final ANodeView nodeView = view.createNodeView(v, node2point.getValue(v), text);
                    view.getNode2view().put(v, nodeView);
                    if (nodeView.getShape() != null)
                        view.getNodesGroup().getChildren().addAll(nodeView.getShape());
                    if (nodeView.getLabel() != null)
                        view.getNodeLabelsGroup().getChildren().addAll(nodeView.getLabel());
                }
                for (Edge e : tree.edges()) {
                    final EdgeControlPoints controlPoints = edge2controlPoints.getValue(e);
                    final AEdgeView edgeView = view.createEdgeView(e, getOptionLayout(), getOptionEdgeShape(), tree.getWeight(e),
                            node2point.getValue(e.getSource()), controlPoints.getControl1(), controlPoints.getMid(),
                            controlPoints.getControl2(), controlPoints.getSupport(), node2point.getValue(e.getTarget()));
                    view.getEdge2view().put(e, edgeView);
                    if (edgeView.getShape() != null)
                        view.getEdgesGroup().getChildren().add(edgeView.getShape());
                    if (edgeView.getLabel() != null)
                        view.getEdgeLabelsGroup().getChildren().addAll(edgeView.getLabel());
                }

                if (false) {
                    System.err.println("Nodes:");
                    for (Node v : tree.nodes()) {
                        Point2D pt = node2point.getValue(v);
                        System.err.println(String.format("%d %.1f %.1f %s", v.getId(), pt.getX(), pt.getY(),
                                (tree.getLabel(v) != null ? tree.getLabel(v) : "")));
                    }
                    System.err.println("Edges:");
                    for (Edge e : tree.edges()) {
                        System.err.println(e.getSource().getId() + " " + e.getTarget().getId());
                    }
                }
            }
        }

        //progressListener.setMaximum(?);
        child.show();

        progressListener.close();
    }


    /**
     * scale all node coordinates so that they fit into the current scene
     *
     * @param optionLayout
     * @param target
     * @param phyloGraph
     * @param node2point
     */
    public static void scaleToFitTarget(GraphLayout optionLayout, Dimension2D target, PhyloGraph phyloGraph, NodeArray<Point2D> node2point) {
        // scale to target dimensions:
        final float factorX;
        final float factorY;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        {
            float minX = Float.MAX_VALUE;
            float minY = Float.MAX_VALUE;
            for (Node v : phyloGraph.nodes()) {
                final Point2D point = node2point.getValue(v);
                minX = Math.min(minX, (float) point.getX());
                maxX = Math.max(maxX, (float) point.getX());
                minY = Math.min(minY, (float) point.getY());
                maxY = Math.max(maxY, (float) point.getY());
            }

            if (optionLayout == GraphLayout.LeftToRight) {
                factorX = (float) (target.getWidth() - 50) / (maxX - minX);
                factorY = (float) (target.getHeight() - 50) / (maxY - minY);
            } else {
                factorX = factorY = (float) Math.min(((target.getWidth() - 50) / (maxX - minX)), ((target.getHeight() - 50) / (maxY - minY)));
            }
        }

        if (factorX != 1 || factorY != 1) {
            for (Node v : phyloGraph.nodes()) {
                final Point2D point = node2point.getValue(v);
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
                final float firstAngle = (float) ((360.0 / angleParts) * (nextLeafNum + leafGroupGapProperty.get() / 200f));
                final float deltaAngle = (float) ((360.0 / angleParts) * (nextLeafNum + numberOfChildren - 1.0 - leafGroupGapProperty.get() / 200f)) - firstAngle;
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
                    if (parentPlacement.getValue() == ParentPlacement.ChildrenAverage)
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
        for (Edge e = root.getFirstOutEdge(); e != null; e = root.getNextOutEdge(e)) {
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
        for (Edge f = v.getFirstOutEdge(); f != null; f = v.getNextOutEdge(f)) {
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
                final float firstHeight = (nextLeafRank + leafGroupGapProperty.get() / 200.0f);
                final float deltaHeight = (nextLeafRank + numberOfChildren - 1 - leafGroupGapProperty.get() / 200.0f) - firstHeight;
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
                if (parentPlacementProperty().getValue() == ParentPlacement.ChildrenAverage)
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
     *  @param v
     * @param x0
     * @param edgeLengths
     * @param nodeHeights
     */
    private void computeNodeLocationsForRectilinearRec(Node v, final float x0, EdgeFloatArray edgeLengths, NodeFloatArray nodeHeights, NodeArray<Point2D> node2point) {
        node2point.setValue(v, new Point2D(x0, nodeHeights.getValue(v)));
        for (Edge e : v.outEdges()) {
            computeNodeLocationsForRectilinearRec(e.getTarget(), x0 + edgeLengths.getValue(e), edgeLengths, nodeHeights, node2point);
        }
    }

    /**
     * compute edge points
     *
     * @param v
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
                final Point2D control1 = start.add(cubicCurveParentControl.get() / 100.0 * (support.getX() - start.getX()), 0);
                final Point2D control2 = support.add(-cubicCurveChildControl.get() / 100.0 * (support.getX() - start.getX()), 0);

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
        return layout.getValue();
    }

    public Property<GraphLayout> layoutProperty() {
        return layout;
    }

    public String getShortDescriptionOptionLayout() {
        return "Sets the tree layout to radial or fromLeft";
    }

    public void setOptionLayout(GraphLayout layoutProperty) {
        this.layout.setValue(layoutProperty);
    }

    public EdgeLengths getOptionEdgeLengths() {
        return edgeLengths.getValue();
    }

    public Property<EdgeLengths> edgeLengthsProperty() {
        return edgeLengths;
    }

    public void setOptionEdgeLengths(EdgeLengths edgeLengthsProperty) {
        this.edgeLengths.setValue(edgeLengthsProperty);
    }

    public ParentPlacement getOptionParentPlacement() {
        return parentPlacement.getValue();
    }

    public Property<ParentPlacement> parentPlacementProperty() {
        return parentPlacement;
    }

    public void setOptionParentPlacement(ParentPlacement parentPlacementProperty) {
        this.parentPlacement.setValue(parentPlacementProperty);
    }

    public AEdgeView.EdgeShape getOptionEdgeShape() {
        return edgeShape.getValue();
    }

    public Property<AEdgeView.EdgeShape> edgeShapeProperty() {
        return edgeShape;
    }

    public void setOptionEdgeShape(AEdgeView.EdgeShape edgeShapeProperty) {
        this.edgeShape.setValue(edgeShapeProperty);
    }


    public int getOptionCubicCurveParentControl() {
        return cubicCurveParentControl.get();
    }

    public IntegerProperty cubicCurveParentControlProperty() {
        return cubicCurveParentControl;
    }

    public void setOptionCubicCurveParentControl(int parentScale) {
        this.cubicCurveParentControl.set(parentScale);
    }

    public int getOptionCubicCurveChildControl() {
        return cubicCurveChildControl.get();
    }

    public IntegerProperty cubicCurveChildControlProperty() {
        return cubicCurveChildControl;
    }

    public void setOptionCubicCurveChildControl(int childScale) {
        this.cubicCurveChildControl.set(childScale);
    }

    public int getOptionLeafGroupGapProperty() {
        return leafGroupGapProperty.get();
    }

    public IntegerProperty leafGroupGapPropertyProperty() {
        return leafGroupGapProperty;
    }

    public void setOptionLeafGroupGapProperty(int leafGroupGapProperty) {
        this.leafGroupGapProperty.set(leafGroupGapProperty);
    }

    public List<String> listOptions() {
        return Arrays.asList("optionLayout", "optionEdgeLengths", "optionEdgeShape", "optionParentPlacement",
                "optionLeafGroupGapProperty", "optionCublicCurveChildControl", "optionCubicCurveParentControl");
    }
}

