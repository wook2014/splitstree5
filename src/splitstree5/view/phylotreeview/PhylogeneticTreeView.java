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

package splitstree5.view.phylotreeview;


import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import jloda.fx.ASelectionModel;
import splitstree5.core.algorithms.views.treeview.GeometryUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * rooted phylogenetic tree view
 */
public class PhylogeneticTreeView extends Group {
    public enum Layout {Rectangular, Circular}

    public enum EdgeLengths {Weights, Uniform, Cladogram, CladogramEarlyBranching}

    public enum ParentPlacement {LeafAverage, ChildrenAverage}

    public enum EdgeShape {Straight, Rectilinear, QuadCurve, CubicCurve}

    private final Group edgesGroup;
    private final Group nodesGroup;
    private final Group edgeLabelsGroup;
    private final Group nodeLabelsGroup;

    private final Array<PhylogeneticNodeView> node2view = new Array<>();
    private final Array<PhylogeneticEdgeView> edge2view = new Array<>();

    private final Property<Layout> layoutProperty;
    private final Property<EdgeLengths> edgeLengthsProperty;
    private final Property<EdgeShape> edgeShapeProperty;
    private final Property<ParentPlacement> parentPlacementProperty;

    private final ObservableList<String> nodeLabelChoices;
    private final ObservableList<String> edgeLabelChoices;

    private ASelectionModel<TreeNode> nodeSelectionModel;
    private ASelectionModel<TreeEdge> edgeSelectionModel;


    private final DoubleProperty scaleX;
    private final DoubleProperty scaleY;

    private final DoubleProperty parentScale;
    private final DoubleProperty childScale;

    private final FloatProperty leafGroupGapProperty;

    private TreeNode root;


    /**
     * constructor
     *
     * @param root
     */
    public PhylogeneticTreeView(TreeNode root) {
        this.root = root;

        edgesGroup = new Group();
        nodesGroup = new Group();
        edgeLabelsGroup = new Group();
        nodeLabelsGroup = new Group();
        getChildren().addAll(edgesGroup, nodesGroup, edgeLabelsGroup, nodeLabelsGroup);

        this.layoutProperty = new SimpleObjectProperty<>(Layout.Circular);
        this.edgeLengthsProperty = new SimpleObjectProperty<>(EdgeLengths.Cladogram);
        this.edgeShapeProperty = new SimpleObjectProperty<>(EdgeShape.CubicCurve);

        parentPlacementProperty = new SimpleObjectProperty<>(ParentPlacement.LeafAverage);

        this.nodeLabelChoices = FXCollections.observableArrayList("Names", "None");
        this.edgeLabelChoices = FXCollections.observableArrayList("Weights", "None");

        scaleX = new SimpleDoubleProperty(100);
        scaleY = new SimpleDoubleProperty(30);

        parentScale = new SimpleDoubleProperty(0.5);
        childScale = new SimpleDoubleProperty(0.5);

        leafGroupGapProperty = new SimpleFloatProperty(0);
    }

    public Layout getLayout() {
        return layoutProperty.getValue();
    }

    public Property<Layout> layoutProperty() {
        return layoutProperty;
    }

    public void setLayout(Layout layout) {
        this.layoutProperty.setValue(layout);
    }

    public EdgeLengths getEdgeLengths() {
        return edgeLengthsProperty.getValue();
    }

    public Property<EdgeLengths> edgeLengthsProperty() {
        return edgeLengthsProperty;
    }

    public void setEdgeLengths(EdgeLengths edgeLengths) {
        this.edgeLengthsProperty.setValue(edgeLengths);
    }

    public EdgeShape getEdgeShape() {
        return edgeShapeProperty.getValue();
    }

    public Property<EdgeShape> edgeShapeProperty() {
        return edgeShapeProperty;
    }

    public void setEdgeShape(EdgeShape edgeShape) {
        this.edgeShapeProperty.setValue(edgeShape);
    }

    public ObservableList<String> getNodeLabelChoices() {
        return nodeLabelChoices;
    }

    public ObservableList<String> getEdgeLabelChoices() {
        return edgeLabelChoices;
    }

    public ASelectionModel<TreeNode> getNodeSelectionModel() {
        if (nodeSelectionModel == null) {
            final Collection<TreeNode> nodes = root.collectAllNodesBelow();
            nodeSelectionModel = new ASelectionModel<>(nodes.toArray(new TreeNode[nodes.size()]));
        }
        return nodeSelectionModel;
    }

    public void setNodeSelectionModel(ASelectionModel<TreeNode> nodeSelectionModel) {
        this.nodeSelectionModel = nodeSelectionModel;
    }

    public ASelectionModel<TreeEdge> getEdgeSelectionModel() {
        if (edgeSelectionModel == null) {
            final Collection<TreeEdge> edges = root.collectAllEdgesBelow();
            edgeSelectionModel = new ASelectionModel<>(edges.toArray(new TreeEdge[edges.size()]));
        }
        return edgeSelectionModel;
    }

    public void setEdgeSelectionModel(ASelectionModel<TreeEdge> edgeSelectionModel) {
        this.edgeSelectionModel = edgeSelectionModel;
    }

    /**
     * update the view
     */
    public void update() {
        final FloatArray edgeLengths = EdgeLengthsCalculation.computeEdgeLengths(root, getEdgeLengths());


        switch (layoutProperty.getValue()) {
            case Circular: {
                final FloatArray edgeAngles = new FloatArray(root.numberOfEdges()); // angle of edge
                setAnglesRec(root, null, 0, root.countLeaves(), edgeAngles);
                computeNodeLocationAndViewForCircularLayout(root, edgeLengths, edgeAngles);
                computeEdgePointsAndViewsForCircularRec(root, 0, edgeAngles);
                break;
            }
            default:
            case Rectangular: {
                final FloatArray nodeHeights = new FloatArray(root.numberOfNodes());
                setNodeHeightsRec(root, 0, nodeHeights);
                computeNodeLocationAndViewForRectilinearRec(root, 0, edgeLengths, nodeHeights);
                computeEdgePointsAndViewsForRectilinearRec(root);
                break;
            }
        }

        for (PhylogeneticNodeView nodeView : node2view) {
            final Group parts = nodeView.getParts();
            if (parts != null)
                nodesGroup.getChildren().addAll(parts);
            final Group labels = nodeView.getLabels();
            if (labels != null)
                nodeLabelsGroup.getChildren().addAll(labels);
        }
        for (PhylogeneticEdgeView edgeView : edge2view) {
            final Group parts = edgeView.getParts();
            if (parts != null)
                edgesGroup.getChildren().addAll(parts);
            final Group labels = edgeView.getLabels();
            if (labels != null)
                edgeLabelsGroup.getChildren().addAll(labels);
        }

        if (true) {
            System.err.println("Nodes:");
            {
                Iterator<TreeNode> it = root.iteratorAllNodesBelow();
                while (it.hasNext()) {
                    final TreeNode v = it.next();
                    PhylogeneticNodeView nv = node2view.get(v.getId());

                    System.err.println(String.format("%d %.0f %.0f %s", v.getId(), nv.getLocation().getX(), nv.getLocation().getY(), v.getLabel() != null ? v.getLabel() : ""));
                }
            }
            System.err.println("Edges:");
            {
                LinkedList<TreeNode> stack = new LinkedList<>();
                stack.add(root);
                while (stack.size() > 0) {
                    TreeNode v = stack.pop();
                    for (TreeEdge e : v.getOutEdges()) {
                        final TreeNode w = e.getTarget();
                        System.err.println(v.getId() + " " + w.getId());
                        stack.add(w);
                    }
                }
            }

        }
    }

    /**
     * Recursively determines the angle of every tree edge.
     *
     * @param v
     * @param f
     * @param nextLeafNum
     * @param angleParts
     * @param edgeAngles
     * @return number of leaves visited
     */
    private int setAnglesRec(final TreeNode v, final TreeEdge f, int nextLeafNum, final int angleParts, final FloatArray edgeAngles) {
        if (v.isLeaf()) {
            if (f != null)
                edgeAngles.set(f.getId(), (float) (2 * Math.PI / angleParts * nextLeafNum));
            return nextLeafNum + 1;
        } else {
            if (v.isAllChildrenAreLeaves()) { // treat these separately because we want to place them all slightly closer together
                final int numberOfChildren = v.getChildren().size();
                final float firstAngle = (float) (2 * (Math.PI / angleParts) * (nextLeafNum + leafGroupGapProperty.get()));
                final float deltaAngle = (float) (2 * (Math.PI / angleParts) * (nextLeafNum + numberOfChildren - 1.0 - leafGroupGapProperty.get())) - firstAngle;
                float lastAngle = 0;
                float angle = firstAngle;
                for (TreeEdge e : v.getOutEdges()) {
                    edgeAngles.set(e.getId(), angle);
                    lastAngle = angle;
                    angle += deltaAngle;
                }
                nextLeafNum += numberOfChildren;
                edgeAngles.set(f.getId(), 0.5f * (firstAngle + lastAngle));
            } else {
                final float firstLeaf = nextLeafNum;
                float firstAngle = Float.MIN_VALUE;
                float lastAngle = Float.MIN_VALUE;

                for (TreeEdge e : v.getOutEdges()) {
                    nextLeafNum = setAnglesRec(e.getTarget(), e, nextLeafNum, angleParts, edgeAngles);
                    final float angle = edgeAngles.get(e.getId());
                    if (firstAngle == Float.MIN_VALUE)
                        firstAngle = angle;
                    lastAngle = angle;
                }

                if (f != null) {
                    if (parentPlacementProperty.getValue() == ParentPlacement.ChildrenAverage)
                        edgeAngles.set(f.getId(), 0.5f * (firstAngle + lastAngle));
                    else {
                        edgeAngles.set(f.getId(), (float) (Math.PI / angleParts * (firstLeaf + nextLeafNum - 1)));
                    }
                }
            }
            return nextLeafNum;
        }
    }


    /**
     * set the coordinates for all nodes and interior edge points
     *
     * @param root       root of tree
     * @param edgeAngles assignment of angles to edges
     */
    private void computeNodeLocationAndViewForCircularLayout(TreeNode root, FloatArray edgeLengths, FloatArray edgeAngles) {
        Point2D rootLocation = new Point2D(0.2, 0);
        node2view.set(root.getId(), PhylogeneticNodeView.createDefaultNodeView(rootLocation, root.getLabel()));
        for (TreeEdge e : root.getOutEdges()) {
            final TreeNode w = e.getTarget();
            final double distance = (scaleX.get() * edgeLengths.get(e.getId()));
            final Point2D wLocation = GeometryUtils.translateByAngle(rootLocation, edgeAngles.get(e.getId()), distance);
            node2view.set(w.getId(), PhylogeneticNodeView.createDefaultNodeView(wLocation, w.getLabel()));

            computeNodeLocationAndViewForCicularRec(rootLocation, w, wLocation, e, edgeLengths, edgeAngles);
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
    private void computeNodeLocationAndViewForCicularRec(Point2D origin, TreeNode v, Point2D vLocation, TreeEdge e, FloatArray edgeLengths, FloatArray edgeAngles) {
        for (TreeEdge f : v.getOutEdges()) {
            final TreeNode w = f.getTarget();
            final Point2D b = GeometryUtils.rotateAbout(vLocation, edgeAngles.get(f.getId()) - edgeAngles.get(e.getId()), origin);
            final double distance = scaleX.get() * edgeLengths.get(f.getId());
            final Point2D c = GeometryUtils.translateByAngle(b, edgeAngles.get(f.getId()), distance);
            node2view.set(w.getId(), PhylogeneticNodeView.createDefaultNodeView(c, w.getLabel()));
            computeNodeLocationAndViewForCicularRec(origin, w, c, f, edgeLengths, edgeAngles);
        }
    }

    /**
     * compute all edge points and setup edge views
     *
     * @param v
     * @param vAngle
     * @param angles
     */
    protected void computeEdgePointsAndViewsForCircularRec(TreeNode v, float vAngle, FloatArray angles) {
        final Point2D start = node2view.get(v.getId()).getLocation();

        for (TreeEdge e : v.getOutEdges()) {
            final TreeNode w = e.getTarget();
            final Point2D end = node2view.get(w.getId()).getLocation();

            float wAngle = angles.get(e.getId());
            final Point2D mid = GeometryUtils.rotate(start, wAngle - vAngle);

            final EdgePoint[] edgePoints = new EdgePoint[]{
                    new EdgePoint(EdgePoint.Type.PrePoint, start.multiply(1 + parentScale.get() * scaleX.get() / start.magnitude())),
                    new EdgePoint(EdgePoint.Type.StartPoint, start),
                    new EdgePoint(EdgePoint.Type.MidPoint, mid),
                    new EdgePoint(EdgePoint.Type.EndPoint, end),
                    new EdgePoint(EdgePoint.Type.PostPoint, end.multiply(1 - childScale.get() * scaleX.get() / end.magnitude()))};

            edge2view.set(e.getId(), PhylogeneticEdgeView.createDefaultEdgeView(layoutProperty.getValue(), edgeShapeProperty.getValue(), e.getWeight(), edgePoints));
            computeEdgePointsAndViewsForCircularRec(w, wAngle, angles);
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
    private float setNodeHeightsRec(final TreeNode v, float nextLeafRank, final FloatArray nodeHeights) {
        if (v.isLeaf()) {
            nodeHeights.set(v.getId(), (float) (scaleY.get() * nextLeafRank));
            return nextLeafRank + 1;
        } else {

            if (v.isAllChildrenAreLeaves()) { // treat these separately because we want to place them all slightly closer together
                final int numberOfChildren = v.getChildren().size();
                final float firstHeight = (float) (scaleY.get() * (nextLeafRank + leafGroupGapProperty.get()));
                final float deltaHeight = (float) (scaleY.get() * (nextLeafRank + numberOfChildren - 1 - leafGroupGapProperty.get())) - firstHeight;
                float lastHeight = 0;
                float height = firstHeight;
                for (TreeEdge e : v.getOutEdges()) {
                    nodeHeights.set(e.getTarget().getId(), height);
                    lastHeight = height;
                    height += deltaHeight;
                }
                nodeHeights.set(v.getId(), 0.5f * (firstHeight + lastHeight));
                return nextLeafRank + numberOfChildren;
            } else {
                final float firstLeafRnak = nextLeafRank;
                float lastLeaf = 0;
                float firstHeight = Float.MIN_VALUE;
                float lastHeight = 0;

                for (TreeEdge e : v.getOutEdges()) {
                    nextLeafRank = setNodeHeightsRec(e.getTarget(), nextLeafRank, nodeHeights);
                    final float eh = nodeHeights.get(e.getTarget().getId());
                    if (firstHeight == Float.MIN_VALUE)
                        firstHeight = eh;
                    lastHeight = eh;
                    lastLeaf = nextLeafRank;
                }
                if (parentPlacementProperty.getValue() == ParentPlacement.ChildrenAverage)
                    nodeHeights.set(v.getId(), 0.5f * (firstHeight + lastHeight));
                else
                    nodeHeights.set(v.getId(), (float) (scaleY.get() * 0.5f * (firstLeafRnak + lastLeaf - 1)));
                return nextLeafRank;
            }
        }
    }

    /**
     * recursively set node coordinates for rectilinear view
     *
     * @param v
     * @param x0
     * @param edgeLengths
     * @param nodeHeights
     */
    private void computeNodeLocationAndViewForRectilinearRec(TreeNode v, final float x0, FloatArray edgeLengths, FloatArray nodeHeights) {
        node2view.set(v.getId(), PhylogeneticNodeView.createDefaultNodeView(new Point2D(x0, nodeHeights.get(v.getId())), v.getLabel()));

        for (TreeEdge e : v.getOutEdges()) {
            computeNodeLocationAndViewForRectilinearRec(e.getTarget(), (float) (x0 + scaleX.get() * edgeLengths.get(e.getId())), edgeLengths, nodeHeights);
        }
    }

    /**
     * compute edge points
     *
     * @param v
     */
    private void computeEdgePointsAndViewsForRectilinearRec(TreeNode v) {
        Point2D closestChild = null;
        for (TreeEdge e : v.getOutEdges()) {
            final Point2D point = node2view.get(e.getTarget().getId()).getLocation();
            if (closestChild == null || point.getX() < closestChild.getX())
                closestChild = point;
        }

        if (closestChild != null) {
            final Point2D start = node2view.get(v.getId()).getLocation();
            for (TreeEdge e : v.getOutEdges()) {
                final TreeNode w = e.getTarget();
                final Point2D end = node2view.get(w.getId()).getLocation();
                final Point2D mid = new Point2D(start.getX(), end.getY());
                final Point2D support = new Point2D(closestChild.getX(), end.getY());

                final EdgePoint[] edgePoints = new EdgePoint[]{
                        new EdgePoint(EdgePoint.Type.PrePoint, start.add(1 + parentScale.get() * (support.getX() - start.getX()), 0)),
                        new EdgePoint(EdgePoint.Type.StartPoint, start),
                        new EdgePoint(EdgePoint.Type.MidPoint, mid),
                        new EdgePoint(EdgePoint.Type.SupportPoint, support),
                        new EdgePoint(EdgePoint.Type.EndPoint, end),
                        new EdgePoint(EdgePoint.Type.PostPoint, support.add(1 - childScale.get() * (support.getX() - start.getX()), 0))};

                edge2view.set(e.getId(), PhylogeneticEdgeView.createDefaultEdgeView(layoutProperty.getValue(), edgeShapeProperty.getValue(), e.getWeight(), edgePoints));

                if (false && e.getId() == 3) {
                    for (Node n : edge2view.get(e.getId()).getParts().getChildren()) {
                        if (n instanceof Shape) {
                            ((Shape) n).setStroke(Color.BLUE.deriveColor(1, 1, 1, 0.5));
                        }

                    }
                }


                computeEdgePointsAndViewsForRectilinearRec(w);
            }
        }
    }
}
