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
import javafx.beans.binding.Bindings;
import javafx.beans.property.LongProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import jloda.fx.ZoomableScrollPane;
import jloda.graph.EdgeArray;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloGraph;
import splitstree5.gui.graphtab.base.GraphTabBase;
import splitstree5.menu.MenuController;

import java.util.concurrent.Executors;

/**
 * tree and split network two-dimensional graph
 * Daniel Huson,. 12.2017
 */
public abstract class Graph3DTab<G extends PhyloGraph> extends GraphTabBase<G> {
    private final Camera camera;
    private final Group topGroup = new Group();
    private final Pane topPane = new Pane(topGroup);
    private final Pane bottomPane = new Pane();

    final Property<Transform> worldTransformProperty;

    public Graph3DTab() {
        super();
        camera = new PerspectiveCamera(true);
        camera.setFarClip(10000);
        camera.setNearClip(0.1);
        camera.setTranslateZ(-900);

        topPane.setPickOnBounds(false);


        worldTransformProperty = new SimpleObjectProperty<>(new Rotate());
        final LongProperty transformChangesProperty = new SimpleLongProperty(); // need this property so that selection rectangles update correctly
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
        Platform.runLater(() -> getUndoManager().clear());
    }

    /**
     * show the viewer
     */
    public void show() {
        Platform.runLater(() -> {
            try {
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

                topPane.getChildren().addAll(edgeLabelsGroup.getChildren());
                topPane.getChildren().addAll(nodeLabelsGroup.getChildren());

                // empty all of these for the next computation
                edgesGroup.getChildren().clear();
                nodesGroup.getChildren().clear();
                edgeLabelsGroup.getChildren().clear();
                nodeLabelsGroup.getChildren().clear();

                nodeSelectionModel.clearSelection();
                edgeSelectionModel.clearSelection();
            } finally {
                Executors.newSingleThreadExecutor().submit(() -> {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                    Platform.runLater(this::layoutLabels);
                });
            }
            if (!(rootNode.getCenter() instanceof ScrollPane)) {
                setContent(rootNode);
                scrollPane = new ZoomableScrollPane(centerPane);

                SubScene subScene = new SubScene(group, centerPane.getWidth(), centerPane.getHeight(), true, SceneAntialiasing.BALANCED);
                subScene.setCamera(camera);

                centerPane.getChildren().addAll(subScene, topPane);

                centerPane.minWidthProperty().bind(Bindings.createDoubleBinding(() ->
                        scrollPane.getViewportBounds().getWidth(), scrollPane.viewportBoundsProperty()).subtract(20));
                centerPane.minHeightProperty().bind(Bindings.createDoubleBinding(() ->
                        scrollPane.getViewportBounds().getHeight(), scrollPane.viewportBoundsProperty()).subtract(20));
                subScene.widthProperty().bind(centerPane.widthProperty());
                subScene.heightProperty().bind(centerPane.heightProperty());

                rootNode.setCenter(scrollPane);
                scrollPane.setLockAspectRatio(true);

                /* this works once window is open, but first time around...
                scrollPane.layout();
                System.err.print("Center: "+scrollPane.getVvalue());
                scrollPane.setVvalue(0.5);
                System.err.println(" -> "+scrollPane.getVvalue());

                scrollPane.setHvalue(0.5);
                */


            }
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

            if (!e.isShiftDown()) {
                final Point2D delta = new Point2D(e.getSceneX() - mousePosX, e.getSceneY() - mousePosY);

                //noinspection SuspiciousNameCombination
                final Point3D dragOrthogonalAxis = new Point3D(delta.getY(), -delta.getX(), 0);
                final Rotate rotate = new Rotate(0.25 * delta.magnitude(), dragOrthogonalAxis);
                worldTransformProperty.setValue(rotate.createConcatenation(worldTransformProperty.getValue()));

                mousePosX = e.getSceneX();
                mousePosY = e.getSceneY();
            }
            e.consume();
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
    }
}