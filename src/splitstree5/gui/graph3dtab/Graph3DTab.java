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
package splitstree5.gui.graph3dtab;

import javafx.application.Platform;
import javafx.beans.binding.LongBinding;
import javafx.beans.property.LongProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import jloda.graph.EdgeArray;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloGraph;
import splitstree5.gui.graphtab.base.GraphTabBase;
import splitstree5.gui.graphtab.base.NodeViewBase;
import splitstree5.menu.MenuController;
import splitstree5.utils.Print;

/**
 * tree and split network two-dimensional graph
 * Daniel Huson,. 12.2017
 */
public abstract class Graph3DTab<G extends PhyloGraph> extends GraphTabBase<G> {
    private boolean sceneIsSetup = false;
    private final Camera camera;
    private final Pane topPane = new Pane();
    private final Pane bottomPane = new Pane();

    private LongBinding viewChanged;
    private LongProperty viewNumber = new SimpleLongProperty(0);

    final Property<Transform> worldTransformProperty;
    final LongProperty transformChangesProperty;

    public Graph3DTab() {
        super();
        camera = new PerspectiveCamera(true);
        camera.setFarClip(10000);
        camera.setNearClip(0.1);
        camera.setTranslateZ(-1000);

        topPane.setPickOnBounds(false);
        bottomPane.setPickOnBounds(false);

        worldTransformProperty = new SimpleObjectProperty<>(new Rotate());
        transformChangesProperty = new SimpleLongProperty(); // need this property so that selection rectangles update correctly
        worldTransformProperty.addListener((observable, oldValue, newValue) -> {
            group.getTransforms().setAll(newValue);
            transformChangesProperty.set(transformChangesProperty.get() + 1);
        });

        setupHandlers(centerPane);
    }

    /**
     * initialize data structures
     *
     * @param phyloGraph
     */
    public void init(G phyloGraph) {
        this.graph = phyloGraph;
        node2view = new NodeArray<>(phyloGraph);
        edge2view = new EdgeArray<>(phyloGraph);
        nodeLabelSearcher.setGraph(graph);
        edgeLabelSearcher.setGraph(graph);
        Platform.runLater(() -> {
            worldTransformProperty.setValue(new Rotate());
            getUndoManager().clear();
        });
    }

    /**
     * show the viewer
     */
    public void show() {
        Platform.runLater(() -> {

            if (centerPane.getChildren().size() == 0) {
                centerPane.setOnMouseClicked((e) -> {
                    if (!e.isShiftDown()) {
                        nodeSelectionModel.clearSelection();
                        edgeSelectionModel.clearSelection();
                    }
                });
            }

            group.getChildren().clear();
            group.getChildren().addAll(edgesGroup.getChildren());
            group.getChildren().addAll(nodesGroup.getChildren());

            topPane.getChildren().clear();
            // topPane.getChildren().addAll(edgeLabelsGroup.getChildren());
            topPane.getChildren().setAll(nodeLabelsGroup.getChildren());

            // empty all of these for the next computation
            edgesGroup.getChildren().clear();
            nodesGroup.getChildren().clear();

            edgeLabelsGroup.getChildren().clear();
            nodeLabelsGroup.getChildren().clear();

            nodeSelectionModel.clearSelection();
            edgeSelectionModel.clearSelection();

            if (!sceneIsSetup) {
                sceneIsSetup = true;

                setContent(borderPane);
                SubScene subScene = new SubScene(group, centerPane.getWidth(), centerPane.getHeight(), true, SceneAntialiasing.BALANCED);
                subScene.setCamera(camera);

                viewChanged = new LongBinding() {
                    long value = 0;

                    {
                        super.bind(transformChangesProperty, subScene.widthProperty(), subScene.heightProperty(), camera.translateXProperty(), camera.translateYProperty(), viewNumber);
                    }

                    @Override
                    protected long computeValue() {
                        return value++;
                    }
                };

                bottomPane.getChildren().setAll(subScene);

                centerPane.getChildren().addAll(bottomPane, topPane);

                subScene.widthProperty().bind(centerPane.widthProperty());
                subScene.heightProperty().bind(centerPane.heightProperty());
                borderPane.setCenter(centerPane);
                borderPane.setTop(findToolBar);
                findToolBar.visibleProperty().addListener((c, o, n) -> {
                    borderPane.setTop(n ? findToolBar : null);
                });
            }

            for (NodeViewBase nv : node2view.values()) {
                ((NodeView3D) nv).setupSelectionRectangle(bottomPane, viewChanged);
            }
            viewNumber.set(viewNumber.get() + 1);
        });
    }

    public void layoutLabels() {
    }

    private double mousePosX;
    private double mousePosY;

    public void setupHandlers(Pane bottomPane) {

        bottomPane.setOnMousePressed((e) -> {

            mousePosX = e.getSceneX();
            mousePosY = e.getSceneY();

            if (nodeSelectionModel != null && (e.isShiftDown() || e.getClickCount() == 2)) {
                nodeSelectionModel.clearSelection();
            }
        });

        bottomPane.setOnMouseDragged((e) -> {
            e.consume();
            if (!e.isShiftDown()) {
                final Point2D delta = new Point2D(e.getSceneX() - mousePosX, e.getSceneY() - mousePosY);

                //noinspection SuspiciousNameCombination
                final Point3D dragOrthogonalAxis = new Point3D(delta.getY(), -delta.getX(), 0);
                final Rotate rotate = new Rotate(0.25 * delta.magnitude(), dragOrthogonalAxis);
                worldTransformProperty.setValue(rotate.createConcatenation(worldTransformProperty.getValue()));

            } else {
                double deltaX = e.getSceneX() - mousePosX;
                double deltaY = e.getSceneY() - mousePosY;
                if (deltaX != 0)
                    camera.setTranslateX(camera.getTranslateX() - deltaX);
                if (deltaY != 0)
                    camera.setTranslateY(camera.getTranslateY() - deltaY);

            }
            mousePosX = e.getSceneX();
            mousePosY = e.getSceneY();
        });

        bottomPane.setOnScroll((e) -> {
            double amount = e.getDeltaY();

            if (amount != 0) {
                double factor = (amount > 0 ? 1.1 : 1.0 / 1.1);
                worldTransformProperty.setValue(worldTransformProperty.getValue().createConcatenation(new Scale(factor, factor, factor)));
            }
        });
    }

    @Override
    public void updateMenus(MenuController controller) {
        super.updateMenus(controller);

        controller.getPrintMenuitem().setOnAction((e) -> {
            final SnapshotParameters snapshotParameters = new SnapshotParameters();
            snapshotParameters.setDepthBuffer(true);
            WritableImage bottomImage = bottomPane.snapshot(snapshotParameters, null);
            // todo: need to overlay top pane or place labels into bottom pane
            Print.print(getMainWindow().getStage(), new ImageView(bottomImage));
        });

        controller.getResetMenuItem().setOnAction((e -> {
            worldTransformProperty.setValue(new Rotate());
            camera.setTranslateX(0);
            camera.setTranslateY(0);
        }));

        controller.getZoomInMenuItem().setOnAction((e) -> camera.setTranslateY(camera.getTranslateY() + 10));
        controller.getZoomInMenuItem().setText("Move Up");

        controller.getZoomOutMenuItem().setOnAction((e) -> camera.setTranslateY(camera.getTranslateY() - 10));
        controller.getZoomInMenuItem().setText("Move Down");

        controller.getRotateRightMenuItem().setOnAction((e) -> camera.setTranslateX(camera.getTranslateX() - 10));
        controller.getRotateRightMenuItem().setText("Move Right");

        controller.getRotateLeftMenuItem().setOnAction((e) -> camera.setTranslateX(camera.getTranslateX() + 10));
        controller.getRotateLeftMenuItem().setText("Move Left");
    }
}