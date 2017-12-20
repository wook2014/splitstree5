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
package splitstree5.main.graphtab.base;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import jloda.fx.ASelectionModel;
import jloda.graph.Edge;
import jloda.graph.EdgeArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloGraph;
import jloda.phylo.PhyloTree;
import splitstree5.main.MainWindowController;
import splitstree5.main.ViewerTab;
import splitstree5.utils.SelectionEffect;

import java.util.concurrent.CountDownLatch;

/**
 * tree and split network tab base class
 * Daniel Huson,. 12.2017
 */
public abstract class GraphTab extends ViewerTab {
    protected final Group group = new Group();
    protected final Group edgesGroup = new Group();
    protected final Group nodesGroup = new Group();
    protected final Group edgeLabelsGroup = new Group();
    protected final Group nodeLabelsGroup = new Group();
    protected final ASelectionModel<Node> nodeSelectionModel = new ASelectionModel<>();
    protected final ASelectionModel<Edge> edgeSelectionModel = new ASelectionModel<>();
    protected final BorderPane rootNode = new BorderPane();
    private PhyloGraph phyloGraph;
    protected NodeArray<ANodeView> node2view;
    protected EdgeArray<AEdgeView> edge2view;

    private final StackPane pane = new StackPane();

    protected final Presenter presenter;

    private double scaleChangeX = 1; // keep track of scale changes, used for reset
    private double scaleChangeY = 1;

    private final StringProperty title = new SimpleStringProperty("");
    private ObjectProperty<GraphLayout> layout = new SimpleObjectProperty<>(GraphLayout.LeftToRight);

    /**
     * constructor
     */
    public GraphTab() {
        setContent(pane);
        // pane.setStyle("-fx-border-color: red");

        nodeSelectionModel.getSelectedItems().addListener((ListChangeListener<Node>) c -> {
            if (c.next()) {
                for (Node v : c.getAddedSubList()) {
                    if (v.getOwner() != null) {
                        final ANodeView nv = getNode2view().get(v);
                        if (nv != null) {
                            if (nv.getLabel() != null)
                                nv.getLabel().setEffect(SelectionEffect.getInstance());
                            if (nv.getShape() != null)
                                nv.getShape().setEffect(SelectionEffect.getInstance());
                        }
                    }
                }
                for (Node v : c.getRemoved()) {
                    if (v.getOwner() != null) {
                        final ANodeView nv = getNode2view().get(v);
                        if (nv != null) {
                            if (nv.getLabel() != null)
                                nv.getLabel().setEffect(null);
                            if (nv.getShape() != null)
                                nv.getShape().setEffect(null);
                        }
                    }
                }
            }
        });
        edgeSelectionModel.getSelectedItems().addListener((ListChangeListener<Edge>) c -> {
            while (c.next()) {
                for (Edge e : c.getAddedSubList()) {
                    if (e.getOwner() != null) {
                        final AEdgeView ev = getEdge2view().getValue(e);
                        if (ev != null) {
                            if (ev.getLabel() != null)
                                ev.getLabel().setEffect(SelectionEffect.getInstance());
                            if (ev.getShape() != null)
                                ev.getShape().setEffect(SelectionEffect.getInstance());
                        }
                    }
                }
                for (Edge e : c.getRemoved()) {
                    if (e.getOwner() != null) {
                        final AEdgeView ev = getEdge2view().getValue(e);
                        if (ev != null) {
                            if (ev.getLabel() != null)
                                ev.getLabel().setEffect(null);
                            if (ev.getShape() != null)
                                ev.getShape().setEffect(null);
                        }
                    }
                }
            }
        });

        presenter = new Presenter(this);
    }

    public int size() {
        return edgesGroup.getChildren().size() + nodesGroup.getChildren().size();
    }

    public Group getGroup() {
        return group;
    }

    public Group getEdgesGroup() {
        return edgesGroup;
    }

    public Group getNodesGroup() {
        return nodesGroup;
    }

    public Group getEdgeLabelsGroup() {
        return edgeLabelsGroup;
    }

    public Group getNodeLabelsGroup() {
        return nodeLabelsGroup;
    }

    public ASelectionModel<Node> getNodeSelectionModel() {
        return nodeSelectionModel;
    }

    public ASelectionModel<Edge> getEdgeSelectionModel() {
        return edgeSelectionModel;
    }

    public void updateSelectionModels(PhyloGraph graph) {
        nodeSelectionModel.setItems(graph.getNodesAsSet().toArray(new Node[graph.getNumberOfNodes()]));
        edgeSelectionModel.setItems(graph.getEdgesAsSet().toArray(new Edge[graph.getNumberOfEdges()]));
    }

    public NodeArray<ANodeView> getNode2view() {
        return node2view;
    }

    public EdgeArray<AEdgeView> getEdge2view() {
        return edge2view;
    }

    public Dimension2D getTargetDimensions() {
        return new Dimension2D(0.6 * pane.getWidth(), 0.9 * pane.getHeight());
    }

    public PhyloGraph getPhyloGraph() {
        return phyloGraph;
    }

    /**
     * initialize datastructures
     *
     * @param phyloGraph
     */
    public void init(PhyloGraph phyloGraph) {
        this.phyloGraph = phyloGraph;
        node2view = new NodeArray<>(phyloGraph);
        edge2view = new EdgeArray<>(phyloGraph);
        group.setScaleX(1);
        group.setScaleY(1);
        scaleChangeX = 1;
        scaleChangeY = 1;
    }

    /**
     * show the viewer
     */
    public void show() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                if (pane.getChildren().size() == 0) {
                    final Group world = new Group();
                    world.getChildren().add(group);
                    pane.getChildren().add(world);

                    presenter.setup(pane);
                    final ScrollPane scrollPane = new ScrollPane(pane);
                    pane.minWidthProperty().bind(Bindings.createDoubleBinding(() ->
                            scrollPane.getViewportBounds().getWidth(), scrollPane.viewportBoundsProperty()).subtract(20));
                    pane.minHeightProperty().bind(Bindings.createDoubleBinding(() ->
                            scrollPane.getViewportBounds().getHeight(), scrollPane.viewportBoundsProperty()).subtract(20));

                    pane.setOnMouseClicked((e) -> {
                        if (!e.isShiftDown()) {
                            nodeSelectionModel.clearSelection();
                            edgeSelectionModel.clearSelection();
                        }
                    });


                    final BorderPane borderPane = new BorderPane(scrollPane);
                    Button layoutLabels = new Button("Layout Labels");
                    layoutLabels.setOnAction((e) -> {
                        layoutLabels();
                    });

                    final Button zoomIn = new Button("Zoom In");
                    zoomIn.setOnAction((e) -> {
                                final double factor = 1.1;
                                scale(factor, factor);
                            }
                    );
                    final Button zoomOut = new Button("Zoom Out");
                    zoomOut.setOnAction((e) -> {
                                final double factor = 1.0 / 1.1;
                                scale(factor, factor);
                            }
                    );

                    borderPane.setRight(new VBox(layoutLabels, zoomIn, zoomOut));
                    rootNode.setCenter(borderPane);
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
            } finally {
                countDownLatch.countDown();
                (new Thread(() -> {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                    Platform.runLater(this::layoutLabels);

                })).start();
            }
            if (!(getContent() instanceof ScrollPane)) {
                ScrollPane scrollPane = new ScrollPane(pane);
                setContent(scrollPane);

                pane.minWidthProperty().bind(Bindings.createDoubleBinding(() ->
                        scrollPane.getViewportBounds().getWidth(), scrollPane.viewportBoundsProperty()));

                pane.minHeightProperty().bind(Bindings.createDoubleBinding(() ->
                        scrollPane.getViewportBounds().getHeight(), scrollPane.viewportBoundsProperty()));
            }
        });
    }

    public GraphLayout getLayout() {
        return layout.get();
    }

    public void setLayout(GraphLayout layout) {
        this.layout.set(layout);
    }

    public void layoutLabels() {
        System.err.println("layout labels");
        // todo: use a service to update label layout
        if (getPhyloGraph() != null) {
            if (getLayout() == GraphLayout.Radial)
                NodeLabelLayouter.radialLayout(getPhyloGraph(), getNode2view(), getEdge2view());
            else {
                if (getPhyloGraph() instanceof PhyloTree) {
                    NodeLabelLayouter.leftToRightLayout(getPhyloGraph(), ((PhyloTree) getPhyloGraph()).getRoot(), getNode2view(), getEdge2view());
                }
            }
        }
    }

    /**
     * change scale by the given factors
     *
     * @param xFactor
     * @param yFactor
     */
    public void scale(double xFactor, double yFactor) {
        scaleChangeX *= xFactor;
        scaleChangeY *= yFactor;

        for (ANodeView nodeView : getNode2view()) {
            nodeView.scaleCoordinates(xFactor, yFactor);
        }
        for (AEdgeView edgeView : getEdge2view()) {
            edgeView.scaleCoordinates(xFactor, yFactor);
        }
    }

    @Override
    public void updateMenus(MainWindowController controller) {
        controller.getSelectAllMenuItem().setOnAction((e) -> {
            nodeSelectionModel.selectAll();
            edgeSelectionModel.selectAll();
        });
        controller.getSelectNoneMenuItem().setOnAction((e) -> {
            nodeSelectionModel.clearSelection();
            edgeSelectionModel.clearSelection();
        });
        controller.getSelectAllNodesMenuItem().setOnAction((e) -> nodeSelectionModel.selectAll());
        controller.getSelectAllEdgeMenuItem().setOnAction((e) -> edgeSelectionModel.selectAll());

        controller.getSelectAllLabeledNodesMenuItem().setOnAction((e) -> {
            for (Node v : getPhyloGraph().nodes()) {
                if (getPhyloGraph().getLabel(v) != null && getPhyloGraph().getLabel(v).length() > 0)
                    nodeSelectionModel.select(v);
            }
        });

        controller.getZoomInMenuItem().setOnAction((e) -> scale(1.1, 1.1));
        controller.getZoomOutMenuItem().setOnAction((e) -> scale(1 / 1.1, 1 / 1.1));

        controller.getResetMenuItem().setOnAction((e) -> scale(1 / scaleChangeX, 1 / scaleChangeY));

        controller.getLayoutLabelsMenuItem().setOnAction((e) -> layoutLabels());
    }
}
