/*
 * GraphTabBase.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package splitstree5.gui.graphtab.base;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import jloda.fx.control.ItemSelectionModel;
import jloda.fx.control.RichTextLabel;
import jloda.fx.control.ZoomableScrollPane;
import jloda.fx.find.EdgeLabelSearcher;
import jloda.fx.find.FindToolBar;
import jloda.fx.find.NodeLabelSearcher;
import jloda.fx.undo.ChangeValueCommand;
import jloda.fx.util.Print;
import jloda.fx.window.MainWindowManager;
import jloda.graph.Edge;
import jloda.graph.EdgeArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloGraph;
import jloda.util.Basic;
import jloda.util.BitSetUtils;
import jloda.util.Single;
import splitstree5.core.Document;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.Taxon;
import splitstree5.core.workflow.DataNode;
import splitstree5.dialogs.nodelabel.NodeLabelDialog;
import splitstree5.gui.ISavesPreviousSelection;
import splitstree5.gui.ViewerTab;
import splitstree5.gui.formattab.FontSizeIncrementCommand;
import splitstree5.gui.formattab.FormatItem;
import splitstree5.gui.graphtab.commands.ChangeEdgeLabelCommand;
import splitstree5.gui.graphtab.commands.ChangeNodeLabelCommand;
import splitstree5.gui.graphtab.commands.MoveEdgeLabelCommand;
import splitstree5.gui.graphtab.commands.MoveNodeLabelCommand;
import splitstree5.main.MainWindow;
import splitstree5.menu.MenuController;

import java.util.*;
import java.util.function.Function;

/**
 * base class for graph Tab, can support both 2D and 3D visualizationm
 *
 * @param <G>
 */
abstract public class GraphTabBase<G extends PhyloGraph> extends ViewerTab implements ISavesPreviousSelection {
    protected ZoomableScrollPane scrollPane;

    protected final Group group = new Group();
    protected final Group edgesGroup = new Group();
    protected final Group nodesGroup = new Group();
    protected final Group edgeLabelsGroup = new Group();
    protected final Group nodeLabelsGroup = new Group();
    protected final ItemSelectionModel<Node> nodeSelectionModel = new ItemSelectionModel<>();
    protected final ItemSelectionModel<Edge> edgeSelectionModel = new ItemSelectionModel<>();

    protected final BorderPane borderPane = new BorderPane();
    protected G graph;

    protected final NodeLabelSearcher nodeLabelSearcher;
    private NodeLabelSearcher.LabelChangedListener labelChangedListener;
    protected final EdgeLabelSearcher edgeLabelSearcher;

    protected NodeArray<NodeViewBase> node2view;
    protected EdgeArray<EdgeViewBase> edge2view;

    protected ArrayList<PolygonView2D> polygons = new ArrayList<>();

    protected final StackPane centerPane = new StackPane();

    protected final BooleanProperty sparseLabels = new SimpleBooleanProperty(false);

    protected Map<String, FormatItem> nodeLabel2Style;

    protected ListChangeListener<Node> nodeSelectionChangeListener;
    protected ListChangeListener<Taxon> documentTaxonSelectionChangeListener;

    protected ListChangeListener<Node> weakNodeSelectionChangeListener;
    protected ListChangeListener<Taxon> weakDocumentTaxonSelectionChangeListener;

    private ObservableList<Taxon> documentTaxonSelectedItems;

    private boolean skipNextLabelLayout = false;

    private Function<NodeViewBase, EventHandler<? super ContextMenuEvent>> nodeViewContextMenu = (nv) -> (EventHandler<ContextMenuEvent>) z -> {
    };

    /**
     * constructor
     */
    public GraphTabBase() {
        borderPane.setBorder(Border.EMPTY);

        setContent(centerPane); // first need to set this here and then later set to center of root node...
        centerPane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        centerPane.setBorder(Border.EMPTY);

        // setup find / replace tool bar:
        {
            nodeLabelSearcher = new NodeLabelSearcher("Nodes", graph, nodeSelectionModel);
            //nodeLabelSearcher.addLabelChangedListener(v -> Platform.runLater(() -> getUndoManager().doAndAdd(new ChangeNodeLabelCommand(v, node2view.get(v), graph))));

            edgeLabelSearcher = new EdgeLabelSearcher("Edges", graph, edgeSelectionModel);
            edgeLabelSearcher.addLabelChangedListener(e -> Platform.runLater(() -> getUndoManager().doAndAdd(new ChangeEdgeLabelCommand(e, edge2view.get(e), graph))));

            findToolBar = new FindToolBar(null, nodeLabelSearcher, edgeLabelSearcher);

            nodeLabelSearcher.foundProperty().addListener((c, o, n) -> {
                if (n != null && scrollPane != null) {
                    final NodeViewBase nv = getNode2view().get(n);
                    if (nv.getLabel() != null)
                        scrollPane.ensureVisible(nv.getLabel());
                }
            });
            edgeLabelSearcher.foundProperty().addListener((c, o, n) -> {
                if (n != null && scrollPane != null) {
                    final EdgeViewBase ev = getEdge2view().get(n);
                    if (ev.getLabel() != null)
                        scrollPane.ensureVisible(ev.getLabel());
                }
            });
        }

        // centerPane.setStyle("-fx-border-color: red");

        nodeSelectionModel.getSelectedItems().addListener((ListChangeListener<Node>) c -> {
            boolean verbose = false;
            if (verbose) {
                System.err.println("Node selection for " + Basic.getShortName(this.getClass()) + "(" + this + "):");
                for (Node v : graph.nodes()) {
                    System.err.println("node " + v + " id: " + v.getId() + " label: " + graph.getLabel(v) + " taxa: " + Basic.toString(graph.getTaxa(v), " "));
                }
            }
            while (c.next()) {
                if (verbose) {
                    System.err.println("ADDED:");
                    for (Node v : c.getAddedSubList()) {
                        System.err.println("node " + v + " id: " + v.getId());
                    }
                }
                for (Node v : c.getAddedSubList()) {
                    if (v.getOwner() == getGraph()) {
                        final NodeViewBase nv = getNode2view().get(v);
                        if (nv != null) {
                            nv.showAsSelected(true);
                        }
                    }
                }
                for (Node v : c.getRemoved()) {
                    if (v.getOwner() == getGraph()) {
                        final NodeViewBase nv = getNode2view().get(v);
                        if (nv != null) {
                            nv.showAsSelected(false);
                        }
                    }
                }
            }
            if (verbose) {
                System.err.println("Selected:");
                for (Node v : graph.nodes()) {
                    System.err.println("node " + v + " id: " + v.getId() + " selected: " + nodeSelectionModel.getSelectedItems().contains(v) + " shown: " + getNode2view().get(v).isShownAsSelected());
                }
            }
        });
        edgeSelectionModel.getSelectedItems().addListener((ListChangeListener<Edge>) c -> {
            while (c.next()) {
                for (Edge e : c.getAddedSubList()) {
                    if (e.getOwner() == getGraph()) {
                        final EdgeViewBase ev = getEdge2view().getValue(e);
                        if (ev != null) {
                            ev.showAsSelected(true);
                        }
                    }
                }
                for (Edge e : c.getRemoved()) {
                    if (e.getOwner() == getGraph()) {
                        final EdgeViewBase ev = getEdge2view().getValue(e);
                        if (ev != null) {
                            ev.showAsSelected(false);
                        }
                    }
                }
            }
        });
    }

    public G getGraph() {
        return graph;
    }

    public int size() {
        return edgesGroup.getChildren().size() + nodesGroup.getChildren().size();
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

    public ItemSelectionModel<Node> getNodeSelectionModel() {
        return nodeSelectionModel;
    }

    public ItemSelectionModel<Edge> getEdgeSelectionModel() {
        return edgeSelectionModel;
    }

    public NodeArray<NodeViewBase> getNode2view() {
        return node2view;
    }

    public EdgeArray<EdgeViewBase> getEdge2view() {
        return edge2view;
    }

    public Dimension2D getTargetDimensions() {
        return new Dimension2D(0.6 * centerPane.getWidth(), 0.9 * centerPane.getHeight());
    }

    private boolean inSelection = false;

    public void updateSelectionModels(PhyloGraph graph, TaxaBlock taxaBlock, Document document) {
        if (weakNodeSelectionChangeListener != null)
            nodeSelectionModel.getSelectedItems().removeListener(weakNodeSelectionChangeListener);
        if (weakDocumentTaxonSelectionChangeListener != null)
            document.getTaxaSelectionModel().getSelectedItems().removeListener(weakDocumentTaxonSelectionChangeListener);


        if (true) {
            if (labelChangedListener != null)
                nodeLabelSearcher.removeLabelChangedListener(labelChangedListener);
            labelChangedListener = v -> {
                final MainWindow mainWindow = document.getMainWindow();
                final TaxaBlock workingTaxaBlock = mainWindow.getWorkflow().getWorkingTaxaBlock();
                final NodeViewBase nv = node2view.get(v);
                if (nv.getNumberOfWorkingTaxonIds() > 0) {
                    final int workingTaxonId = node2view.get(v).getWorkingTaxa().nextSetBit(1);
                    Platform.runLater(() ->
                            mainWindow.getUndoRedoManager().doAndAdd(new ChangeValueCommand<>("Change Label", workingTaxaBlock.get(workingTaxonId).getDisplayLabel(), graph.getLabel(v),
                                    (label) -> {
                                        final Taxon workingTaxon = workingTaxaBlock.get(workingTaxonId);
                                        workingTaxon.setDisplayLabel(label);
                                        mainWindow.updateDataView(mainWindow.getWorkflow().getWorkingTaxaNode());
                                        nv.setLabel(workingTaxon.getDisplayLabelOrName());
                                    })));
                } else {
                    Platform.runLater(() -> getUndoManager().doAndAdd(new ChangeNodeLabelCommand(v, node2view.get(v), graph)));
                }
            };
            nodeLabelSearcher.addLabelChangedListener(labelChangedListener);

        }


        Platform.runLater(() -> {
            nodeSelectionModel.clearSelection();
            edgeSelectionModel.clearSelection();
        });

        nodeSelectionChangeListener = (c -> {
            try {
                inSelection = true;
                while (c.next()) {
                    if (c.getAddedSize() > 0) {
                        for (Node v : c.getAddedSubList()) {
                            if (v.getOwner() == graph) {
                                String name = graph.getLabel(v);
                                if (name != null) {
                                    Taxon taxon = taxaBlock.get(name);
                                    if (taxon != null)
                                        document.getTaxaSelectionModel().select(taxon);
                                    for (Integer taxId : graph.getTaxa(v)) {
                                        if (taxId <= taxaBlock.getNtax())
                                            document.getTaxaSelectionModel().select(taxaBlock.get(taxId));
                                    }
                                }
                            }
                        }
                    }
                    if (c.getRemovedSize() > 0) {
                        for (Node v : c.getRemoved()) {
                            if (v.getOwner() == graph) {
                                String name = graph.getLabel(v);
                                if (name != null) {
                                    Taxon taxon = taxaBlock.get(name);
                                    if (taxon != null)
                                        document.getTaxaSelectionModel().clearSelection(taxon);
                                    for (Integer taxId : graph.getTaxa(v)) {
                                        if (taxId <= taxaBlock.getNtax())
                                            document.getTaxaSelectionModel().clearSelection(taxaBlock.get(taxId));
                                    }
                                }
                            }
                        }
                    }
                }
            } finally {
                inSelection = false;
            }
        });
        weakNodeSelectionChangeListener = new WeakListChangeListener<>(nodeSelectionChangeListener);
        nodeSelectionModel.getSelectedItems().addListener(weakNodeSelectionChangeListener);

        documentTaxonSelectionChangeListener = (c -> {
            if (!inSelection) {
                while (c.next()) {
                    if (c.getAddedSize() > 0) {
                        for (Taxon taxon : c.getAddedSubList()) {
                            final Node v = graph.getTaxon2Node(taxaBlock.indexOf(taxon));
                            if (v != null) {
                                nodeSelectionModel.select(v);
                            } else {
                                final String label = taxon.getName();
                                for (Node w : graph.nodes()) {
                                    if (graph.getLabel(w) != null) {
                                        if (label.equals(graph.getLabel(w))) {
                                            nodeSelectionModel.select(w);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (c.getRemovedSize() > 0) {
                        for (Taxon taxon : c.getRemoved()) {
                            final Node v = graph.getTaxon2Node(taxaBlock.indexOf(taxon));
                            if (v != null) {
                                nodeSelectionModel.clearSelection(v);
                            } else {
                                String label = taxon.getName();
                                for (Node w : graph.nodes()) {
                                    if (graph.getLabel(w) != null) {
                                        if (label.equals(graph.getLabel(w))) {
                                            nodeSelectionModel.clearSelection(w);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
        weakDocumentTaxonSelectionChangeListener = new WeakListChangeListener<>(documentTaxonSelectionChangeListener);
        documentTaxonSelectedItems = document.getTaxaSelectionModel().getSelectedItems();
        documentTaxonSelectedItems.addListener(weakDocumentTaxonSelectionChangeListener);

        if (!document.getTaxaSelectionModel().isEmpty()) {
            final Set<String> selectedNames = new HashSet<>();
            for (Taxon taxon : document.getTaxaSelectionModel().getSelectedItems()) {
                selectedNames.add(taxon.getName());
            }
            for (Node v : graph.nodes()) {
                if (graph.getLabel(v) != null && selectedNames.contains(graph.getLabel(v))) {
                    nodeSelectionModel.select(v);
                }
            }
        }
    }


    /**
     * select nodes  labels
     *
     * @param set
     */
    public void selectNodesByLabel(Collection<String> set, boolean select) {
        for (Node node : getGraph().nodes()) {
            String label = getGraph().getLabel(node);
            if (label != null && set.contains(label))
                if (select)
                    nodeSelectionModel.select(node);
                else
                    nodeSelectionModel.clearSelection(node);
        }
    }

    /**
     * select nodes and edges by labels
     *
     * @param set
     */
    public void selectByLabel(Collection<String> set) {
        System.err.println("Selecting by label...");
        for (Node node : getGraph().nodes()) {
            final String label = getGraph().getLabel(node);
            //System.err.println(label);
            if (label != null && set.contains(label))
                nodeSelectionModel.select(node);
        }
        for (Edge edge : getGraph().edges()) {
            final String label = getGraph().getLabel(edge);
            if (label != null && set.contains(label))
                edgeSelectionModel.select(edge);
        }
    }

    public void saveAsPreviousSelection() {
        MainWindowManager.getPreviousSelection().clear();
        if (nodeSelectionModel.getSelectedItems().size() > 0) {
            final PhyloGraph graph = getGraph();
            for (Node v : nodeSelectionModel.getSelectedItems()) {
                if (v.getOwner() == graph) {
                    final String label = graph.getLabel(v);
                    if (label != null) {
                        for (String one : label.split(",")) {
                            one = one.trim();
                            if (one.length() > 0)
                                MainWindowManager.getPreviousSelection().add(one);
                        }
                    }
                }
            }
        }
        if (edgeSelectionModel.getSelectedItems().size() > 0) {
            for (Edge edge : edgeSelectionModel.getSelectedItems()) {
                if (edge.getOwner() == getGraph()) {
                    final String label = getGraph().getLabel(edge);
                    if (label != null)
                        MainWindowManager.getPreviousSelection().add(label);
                }
            }
        }
    }

    public void addNodeLabelMovementSupport(NodeView2D nodeView) {
        final RichTextLabel label = nodeView.getLabel();
        if (label != null) {
            final Single<Point2D> oldLocation = new Single<>();
            final Single<Point2D> point = new Single<>();
            label.setOnMousePressed((e) -> {
                if (nodeSelectionModel.getSelectedItems().contains(nodeView.getNode())) {
                    point.set(new Point2D(e.getScreenX(), e.getScreenY()));
                    oldLocation.set(new Point2D(label.getTranslateX(), label.getTranslateY()));
                }
                e.consume();
            });
            label.setOnMouseDragged((e) -> {
                if (point.get() != null) {
                    double deltaX = e.getScreenX() - point.get().getX();
                    double deltaY = e.getScreenY() - point.get().getY();
                    point.set(new Point2D(e.getScreenX(), e.getScreenY()));
                    if (deltaX != 0)
                        label.setTranslateX(label.getTranslateX() + deltaX);
                    if (deltaY != 0)
                        label.setTranslateY(label.getTranslateY() + deltaY);
                    e.consume();
                }
            });
            label.setOnMouseReleased((e) -> {
                if (oldLocation.get() != null) {
                    final Point2D newLocation = new Point2D(label.getTranslateX(), label.getTranslateY());
                    if (!newLocation.equals(oldLocation.get())) {
                        getUndoManager().doAndAdd(new MoveNodeLabelCommand(label, oldLocation.get(), newLocation, nodeView));
                    }
                }
                point.set(null);
                e.consume();
            });
        }
    }

    public void addEdgeLabelMovementSupport(EdgeView2D edgeView) {
        final Labeled label = edgeView.getLabel();
        if (label != null) {
            final Single<Point2D> oldLocation = new Single<>();
            final Single<Point2D> point = new Single<>();
            label.setOnMousePressed((e) -> {
                if (edgeSelectionModel.getSelectedItems().contains(edgeView.getEdge())) {
                    point.set(new Point2D(e.getScreenX(), e.getScreenY()));
                    oldLocation.set(new Point2D(label.getTranslateX(), label.getTranslateY()));
                }
                e.consume();
            });
            label.setOnMouseDragged((e) -> {
                if (point.get() != null) {
                    double deltaX = e.getScreenX() - point.get().getX();
                    double deltaY = e.getScreenY() - point.get().getY();
                    point.set(new Point2D(e.getScreenX(), e.getScreenY()));
                    if (deltaX != 0)
                        label.setTranslateX(label.getTranslateX() + deltaX);
                    if (deltaY != 0)
                        label.setTranslateY(label.getTranslateY() + deltaY);
                    e.consume();
                }
            });
            label.setOnMouseReleased((e) -> {
                if (oldLocation.get() != null) {
                    final Point2D newLocation = new Point2D(label.getTranslateX(), label.getTranslateY());
                    if (!newLocation.equals(oldLocation.get())) {
                        getUndoManager().doAndAdd(new MoveEdgeLabelCommand(label, oldLocation.get(), newLocation, edgeView));
                    }
                }
                point.set(null);
                e.consume();
            });
        }
    }

    @Override
    public void updateMenus(MenuController controller) {
        setOnClosed((e) -> {
            getMainWindow().getWorkflow().deleteNodeAndPathAndDescendants(getDataNode());
        });
        setClosable(true);

        controller.getPageSetupMenuItem().setOnAction((e) -> Print.showPageLayout(getMainWindow().getStage()));
        controller.getPrintMenuitem().setOnAction((e) -> Print.print(getMainWindow().getStage(), centerPane));

        if (getUndoManager() != null) {
            controller.getUndoMenuItem().setOnAction((e) -> {
                getUndoManager().undo();
            });
            controller.getUndoMenuItem().disableProperty().bind(getUndoManager().undoableProperty().not());
            controller.getUndoMenuItem().textProperty().bind(getUndoManager().undoNameProperty());

            controller.getRedoMenuItem().setOnAction((e) -> {
                getUndoManager().redo();
            });
            controller.getRedoMenuItem().disableProperty().bind(getUndoManager().redoableProperty().not());
            controller.getRedoMenuItem().textProperty().bind(getUndoManager().redoNameProperty());
        }

        controller.getSelectAllMenuItem().setOnAction((e) -> {
            nodeSelectionModel.selectItems(graph.getNodesAsSet());
            edgeSelectionModel.selectItems(graph.getEdgesAsSet());
        });
        // controller.getSelectAllMenuItem().disableProperty().bind(Bindings.size(nodeSelectionModel.getSelectedItems()).isEqualTo(graph.getNumberOfNodes())
        // .and(Bindings.size(edgeSelectionModel.getSelectedItems()).isEqualTo(graph.getNumberOfEdges()))); // todo: breaks if number of nodes or edges changes...

        controller.getSelectNoneMenuItem().setOnAction((e) -> {
            nodeSelectionModel.clearSelection();
            edgeSelectionModel.clearSelection();
        });

        if (graph != null) {
            controller.getSelectAllNodesMenuItem().setOnAction((e) -> nodeSelectionModel.selectItems(graph.getNodesAsSet()));
            controller.getSelectAllNodesMenuItem().disableProperty().bind(Bindings.size(nodeSelectionModel.getSelectedItems()).isEqualTo(graph.getNumberOfNodes()));

            controller.getSelectAllEdgesMenuItem().setOnAction((e) -> edgeSelectionModel.selectItems(graph.getEdgesAsSet()));
            controller.getSelectAllEdgesMenuItem().disableProperty().bind(Bindings.size(edgeSelectionModel.getSelectedItems()).isEqualTo(graph.getNumberOfEdges()));
        }

        controller.getCopyMenuItem().setOnAction((x) -> {
            final Set<String> set = new TreeSet<>();
            for (Node v : nodeSelectionModel.getSelectedItems()) {
                final NodeViewBase nv = node2view.get(v);
                if (nv.getLabel() != null && nv.getLabel().getText().length() > 0)
                    set.add(nv.getLabel().getText());
            }
            for (Edge e : edgeSelectionModel.getSelectedItems()) {
                final EdgeViewBase ev = edge2view.get(e);
                if (ev.getLabel() != null && ev.getLabel().getText().length() > 0)
                    set.add(ev.getLabel().getText());
            }
            if (set.size() > 0) {
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(Basic.toString(set, "\n"));
                clipboard.setContent(content);
            }
        });
        controller.getCopyMenuItem().disableProperty().bind(Bindings.isEmpty(nodeSelectionModel.getSelectedItems()).and(Bindings.isEmpty(edgeSelectionModel.getSelectedItems())));

        controller.getCopyImageMenuItem().setOnAction((x) -> {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            WritableImage writableImage = new WritableImage((int) centerPane.getWidth() + 1, (int) centerPane.getHeight() + 1);
            centerPane.snapshot(null, writableImage);
            content.putImage(writableImage);
            clipboard.setContent(content);
        });
        //controller.getCopyImageMenuItem().disableProperty().bind(Bindings.isNotEmpty(nodesGroup.getChildren()));

        controller.getFindMenuItem().setOnAction((e) -> findToolBar.setShowFindToolBar(true));
        controller.getFindAgainMenuItem().setOnAction((e) -> findToolBar.findAgain());
        controller.getFindAgainMenuItem().disableProperty().bind(findToolBar.canFindAgainProperty().not());

        controller.getReplaceMenuItem().setOnAction((e) -> findToolBar.setShowReplaceToolBar(true));

        controller.getSelectAllLabeledNodesMenuItem().setOnAction((e) -> {
            for (Node v : getGraph().nodes()) {
                if (getGraph().getLabel(v) != null && getGraph().getLabel(v).length() > 0)
                    nodeSelectionModel.select(v);
            }
        });

        controller.getInvertNodeSelectionMenuItem().setOnAction((e) -> nodeSelectionModel.invertSelection(graph.getNodesAsSet()));
        controller.getInvertEdgeSelectionMenuItem().setOnAction((e) -> edgeSelectionModel.invertSelection(graph.getEdgesAsSet()));

        controller.getDeselectAllNodesMenuItem().setOnAction((e) -> nodeSelectionModel.clearSelection());
        controller.getDeselectAllNodesMenuItem().disableProperty().bind(nodeSelectionModel.emptyProperty());
        controller.getDeselectEdgesMenuItem().setOnAction((e) -> edgeSelectionModel.clearSelection());
        controller.getDeselectEdgesMenuItem().disableProperty().bind(edgeSelectionModel.emptyProperty());

        controller.getSelectFromPreviousMenuItem().setOnAction((e) -> {
            selectByLabel(MainWindowManager.getPreviousSelection());

        });
        // todo: do we need to unbind this when this tab disappears?
        controller.getSelectFromPreviousMenuItem().disableProperty().bind(Bindings.isEmpty(MainWindowManager.getPreviousSelection()));

        controller.getZoomInMenuItem().setOnAction((e) -> scrollPane.zoomBy(1.1, 1.1));
        controller.getZoomOutMenuItem().setOnAction((e) -> scrollPane.zoomBy(1 / 1.1, 1 / 1.1));

        controller.getIncreaseFontSizeMenuItem().setOnAction((x) -> {
            if (nodeSelectionModel.getSelectedItems().size() > 0 || edgeSelectionModel.getSelectedItems().size() > 0) {
                getUndoManager().doAndAdd(new FontSizeIncrementCommand(2, nodeSelectionModel.getSelectedItems(),
                        node2view, edgeSelectionModel.getSelectedItems(), edge2view));
            } else {
                getUndoManager().doAndAdd(new FontSizeIncrementCommand(2, graph.getNodesAsSet(),
                        node2view, graph.getEdgesAsSet(), edge2view));
            }
        });

        controller.getDecreaseFontSizeMenuItem().setOnAction((x) -> {
            if (nodeSelectionModel.getSelectedItems().size() > 0 || edgeSelectionModel.getSelectedItems().size() > 0) {
                getUndoManager().doAndAdd(new FontSizeIncrementCommand(-2, nodeSelectionModel.getSelectedItems(),
                        node2view, edgeSelectionModel.getSelectedItems(), edge2view));
            } else {
                getUndoManager().doAndAdd(new FontSizeIncrementCommand(-2, graph.getNodesAsSet(),
                        node2view, graph.getEdgesAsSet(), edge2view));
            }
        });


        controller.getFormatNodesMenuItem().setOnAction((e) -> getMainWindow().showFormatTab());
        controller.getFormatNodesMenuItem().setDisable(false);

    }

    public void setNodeLabel2Style(Map<String, FormatItem> nodeLabel2Style) {
        this.nodeLabel2Style = nodeLabel2Style;
    }

    public Map<String, FormatItem> getNodeLabel2Style() {
        return nodeLabel2Style;
    }

    public FindToolBar getFindToolBar() {
        return findToolBar;
    }


    public void setDataNode(DataNode dataNode) {
        super.setDataNode(dataNode);
        if (dataNode != null && dataNode.getDataBlock() != null && dataNode.getDataBlock().getDocument() != null)
            setMainWindow(dataNode.getDataBlock().getDocument().getMainWindow());
    }

    public void close() {
        if (documentTaxonSelectedItems != null)
            documentTaxonSelectedItems.removeListener(weakDocumentTaxonSelectionChangeListener);
    }

    abstract public String getInfo();

    abstract public void show();

    public boolean isSkipNextLabelLayout() {
        return skipNextLabelLayout;
    }

    public void setSkipNextLabelLayout(boolean skipNextLabelLayout) {
        this.skipNextLabelLayout = skipNextLabelLayout;
    }

    public ArrayList<PolygonView2D> getPolygons() {
        return polygons;
    }

    public Function<NodeViewBase, EventHandler<? super ContextMenuEvent>> getNodeViewContextMenu() {
        return nodeViewContextMenu;
    }


    public void setupNodeViewContextMenu(Document document) {
        nodeViewContextMenu = nv -> {
            if (nv.getNumberOfWorkingTaxonIds() > 0) {
                final int taxonId = BitSetUtils.members(nv.getWorkingTaxa()).iterator().next();
                final TaxaBlock taxaBlock = document.getWorkflow().getWorkingTaxaBlock();

                if (taxonId > 0) {
                    return x -> {
                        final MenuItem menuItem = new MenuItem("Change label");
                        menuItem.setOnAction(z -> NodeLabelDialog.apply(taxaBlock.getDocument().getMainWindow(), taxonId, nv));
                        (new ContextMenu(menuItem)).show(nv.getShapeGroup(), x.getScreenX(), x.getScreenY());
                    };
                }
            }
            return null;
        };
    }
}

