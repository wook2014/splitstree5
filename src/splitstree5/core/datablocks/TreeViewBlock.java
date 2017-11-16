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
package splitstree5.core.datablocks;


import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import jloda.fx.ASelectionModel;
import jloda.graph.Edge;
import jloda.phylo.PhyloTree;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToNone;
import splitstree5.core.algorithms.views.TreeEmbedder;
import splitstree5.core.datablocks.view.AEdgeView;
import splitstree5.core.datablocks.view.GeometryUtils;
import splitstree5.core.datablocks.view.NodeLabelLayouter;

import java.util.concurrent.CountDownLatch;

/**
 * This block represents the view of a tree
 * Daniel Huson, 11.2017
 */
public class TreeViewBlock extends ViewBlockBase {
    public enum Layout {LeftToRight, Radial}

    private ObjectProperty<Layout> layout = new SimpleObjectProperty<>(Layout.LeftToRight);

    /**
     * constructor
     */
    public TreeViewBlock() {
        super();
    }

    /**
     * show the phyloGraph or network
     */
    public void show() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                if (stage == null) {
                    stage = new Stage();
                    stage.setTitle("Tree view");
                    final Group world = new Group();
                    world.getChildren().add(group);
                    StackPane stackPane = new StackPane(world);
                    ScrollPane scrollPane = new ScrollPane(stackPane);
                    stackPane.minWidthProperty().bind(Bindings.createDoubleBinding(() ->
                            scrollPane.getViewportBounds().getWidth(), scrollPane.viewportBoundsProperty()).subtract(20));
                    stackPane.minHeightProperty().bind(Bindings.createDoubleBinding(() ->
                            scrollPane.getViewportBounds().getHeight(), scrollPane.viewportBoundsProperty()).subtract(20));

                    BorderPane borderPane = new BorderPane(scrollPane);
                    Button layoutLabels = new Button("Layout Labels");
                    layoutLabels.setOnAction((e) -> {
                        if (getPhyloTree() != null) {
                            if (getLayout() == Layout.Radial)
                                NodeLabelLayouter.radialLayout(getPhyloGraph(), getNode2view(), getEdge2view());
                            else if (getLayout() == Layout.LeftToRight)
                                NodeLabelLayouter.leftToRightLayout(getPhyloTree(), getPhyloTree().getRoot(), getNode2view(), getEdge2view());
                        }
                    });

                    Button zoomIn = new Button("Zoom In");
                    zoomIn.setOnAction((e) -> {
                                final double factor = 1.1;
                                group.setScaleX(factor * group.getScaleX());
                                group.setScaleY(factor * group.getScaleY());
                                rescaleNodesAndEdgesToKeepApparentSizes(factor);
                            }
                    );
                    Button zoomOut = new Button("Zoom Out");
                    zoomOut.setOnAction((e) -> {
                                final double factor = 1.0 / 1.1;
                                group.setScaleX(factor * group.getScaleX());
                                group.setScaleY(factor * group.getScaleY());
                                rescaleNodesAndEdgesToKeepApparentSizes(factor);
                            }
                    );

                    borderPane.setRight(new VBox(layoutLabels, zoomIn, zoomOut));
                    rootNode.setCenter(borderPane);
                    stage.setScene(scene);
                    stage.sizeToScene();
                    stage.show();
                }

                group.getChildren().clear();
                group.getChildren().addAll(edgesGroup.getChildren());
                group.getChildren().addAll(nodesGroup.getChildren());
                group.getChildren().addAll(edgeLabelsGroup.getChildren());
                group.getChildren().addAll(nodeLabelsGroup.getChildren());

                // empty all of these for the next computation
                edgesGroup.getChildren().clear();
                nodesGroup.getChildren().clear();
                edgeLabelsGroup.getChildren().clear();
                nodeLabelsGroup.getChildren().clear();
                nodeSelectionModel.clearSelection();
                edgeSelectionModel.clearSelection();


                stage.setIconified(false);
                stage.toFront();
            } finally {
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await(); // wait for the JavaFX update to take place
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * create a simple edge view
     *
     * @param layout
     * @param shape
     * @param weight
     * @param start
     * @param control1
     * @param mid
     * @param control2
     * @param support
     * @param end
     * @return
     */
    public static AEdgeView createEdgeView(Edge e, TreeViewBlock.Layout layout, TreeEmbedder.EdgeShape shape, Double weight,
                                           final Point2D start, final Point2D control1, final Point2D mid, final Point2D control2, final Point2D support, final Point2D end, ASelectionModel<Edge> selectionModel) {
        final AEdgeView edgeView = new AEdgeView(e);

        Shape edgeShape = null;

        switch (shape) {
            case CubicCurve: {
                boolean forJavaCourse = false;

                // todo: do we want to use the support node?
                if (true || layout == TreeViewBlock.Layout.Radial) {
                    edgeShape = new CubicCurve(start.getX(), start.getY(), control1.getX(), control1.getY(), control2.getX(), control2.getY(), end.getX(), end.getY());

                    if (forJavaCourse) {
                        Circle circle1 = new Circle(control1.getX(), control1.getY(), 2);
                        circle1.setFill(Color.RED);
                        edgeView.setLabel(circle1);
                        Circle circle2 = new Circle(control2.getX(), control2.getY(), 2);
                        circle2.setFill(Color.BLACK);
                        edgeView.setLabel(circle2);
                    }

                    break;
                } else {
                    if (support != null) {
                        final MoveTo moveTo = new MoveTo(start.getX(), start.getY());
                        final CubicCurveTo cubicCurveTo = new CubicCurveTo(control1.getX(), control1.getY(), control2.getX(), control2.getY(), support.getX(), support.getY());
                        final HLineTo hLineTo = new HLineTo(end.getX());
                        edgeShape = new Path(moveTo, cubicCurveTo, hLineTo);

                        break;
                    }
                } // else fall through to next
            }
            case QuadCurve: {
                if (layout == TreeViewBlock.Layout.Radial) {
                    edgeShape = new QuadCurve(start.getX(), start.getY(), mid.getX(), mid.getY(), end.getX(), end.getY());
                    break;
                } else {
                    if (support != null) {
                        final MoveTo moveTo = new MoveTo(start.getX(), start.getY());
                        final QuadCurveTo quadCurveTo = new QuadCurveTo(mid.getX(), mid.getY(), support.getX(), support.getY());
                        final LineTo lineTo = new LineTo(end.getX(), end.getY());
                        edgeShape = new Path(moveTo, quadCurveTo, lineTo);
                        break;
                    }
                }  // else fall through to next
            }
            case Angular: {
                if (layout == TreeViewBlock.Layout.Radial) {
                    double radius = mid.magnitude();
                    double startAngle = GeometryUtils.computeAngle(start);
                    double endAngle = GeometryUtils.computeAngle(mid);

                    final MoveTo moveTo = new MoveTo(start.getX(), start.getY());
                    final ArcTo arcTo = new ArcTo(radius, radius, 0, mid.getX(), mid.getY(), false, endAngle > startAngle);
                    final LineTo lineTo = new LineTo(end.getX(), end.getY());
                    edgeShape = new Path(moveTo, arcTo, lineTo);
                } else // rectilinear:
                {
                    edgeShape = new Polyline(start.getX(), start.getY(), mid.getX(), mid.getY(), end.getX(), end.getY());
                }
                break;
            }
            case Straight: {
                if (start != null && end != null) {
                    edgeShape = new Line(start.getX(), start.getY(), end.getX(), end.getY());
                }
                break;
            }
        }

        if (edgeShape != null) {
            edgeShape.setFill(Color.TRANSPARENT);
            edgeShape.setStroke(Color.BLACK);
            edgeShape.setStrokeLineCap(StrokeLineCap.ROUND);
            edgeShape.setStrokeWidth(3);
            edgeView.setShape(edgeShape);
            if (shape == TreeEmbedder.EdgeShape.Straight)
                edgeView.setReferencePoint(start.add(end).multiply(0.5));
            else
                edgeView.setReferencePoint(mid);
        }

        if (false && weight != null && start != null && end != null) {
            Label label = new Label("" + weight);
            final Point2D m = (mid != null ? mid : start.add(end).multiply(0.5));
            label.setLayoutX(m.getX());
            label.setLayoutY(m.getY());
            edgeView.setLabel(label);
        }

        if (edgeView.getShape() != null) {
            edgeView.getShape().setOnMouseClicked((x) -> {
                if (!x.isShiftDown())
                    selectionModel.clearSelection();
                if (selectionModel.getSelectedItems().contains(e))
                    selectionModel.clearSelection(e);
                else
                    selectionModel.select(e);
            });
        }

        if (edgeView.getLabel() != null) {
            edgeView.getLabel().setOnMouseClicked((x) -> {
                if (!x.isShiftDown())
                    selectionModel.clearSelection();
                if (selectionModel.getSelectedItems().contains(e))
                    selectionModel.clearSelection(e);
                else
                    selectionModel.select(e);
            });
        }
        return edgeView;
    }


    private PhyloTree getPhyloTree() {
        return (PhyloTree) getPhyloGraph();
    }

    public Layout getLayout() {
        return layout.get();
    }

    public ObjectProperty<Layout> layoutProperty() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout.set(layout);
    }

    @Override
    public Class getFromInterface() {
        return IFromTrees.class;
    }

    @Override
    public Class getToInterface() {
        return IToNone.class;
    }

    @Override
    public String getInfo() {
        return "Phylogenetic tree";
    }
}
