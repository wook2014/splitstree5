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

package splitstree5.gui.dagview;

import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import jloda.fx.ASelectionModel;
import jloda.util.Basic;
import splitstree5.core.dag.ANode;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.undo.UndoableChangeList;
import splitstree5.undo.UndoableChangePropertyPair;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * installs a mouse handler
 * Created by huson on 1/31/17.
 */
public class DagNodeViewMouseHandler {
    private double mouseDownX;
    private double mouseDownY;

    private final Map<ANode, UndoableChangePropertyPair> node2change = new HashMap<>();

    private boolean shiftDown;
    private boolean controlDown;
    private boolean dragged;

    private final Line line = new Line();
    private static DagNodeView lock = null;

    /**
     * setup the mouse handler
     *
     * @param nodeView
     */
    private DagNodeViewMouseHandler(DAGView dagView, Pane world, DagNodeView nodeView) {
        line.setStroke(Color.DARKGRAY);

        nodeView.setOnMousePressed((e) -> {
            if (lock == null) {
                lock = nodeView;
                dragged = false;
                world.getChildren().remove(line);

                final Point2D point = world.screenToLocal(e.getScreenX(), e.getScreenY());
                mouseDownX = point.getX();
                mouseDownY = point.getY();
                ;

                shiftDown = e.isShiftDown();
                controlDown = e.isControlDown();

                node2change.clear();

                if (controlDown && !shiftDown && nodeView.getANode() instanceof ADataNode) {
                    line.setStartX(mouseDownX);
                    line.setStartY(mouseDownY);
                    line.setEndX(mouseDownX);
                    line.setEndY(mouseDownY);
                    world.getChildren().add(line);
                }
            }
        });

        nodeView.setOnMouseDragged((e) -> {
            if (nodeView == lock) {
                if (!controlDown) {
                    dragged = true;

                    final ASelectionModel<ANode> selectionModel = dagView.getDag().getNodeSelectionModel();

                    if (!selectionModel.getSelectedItems().contains(nodeView.getANode())) {
                        if (!e.isShiftDown())
                            selectionModel.clearSelection();
                        selectionModel.select(nodeView.getANode());
                    }

                    final boolean setupUndo = (node2change.size() == 0);

                    final Point2D point = world.screenToLocal(e.getScreenX(), e.getScreenY());
                    for (ANode node : selectionModel.getSelectedItems()) {
                        final DagNodeView selectedNodeView = dagView.getNodeView(node);
                        if (selectedNodeView != null) {
                            if (setupUndo) {
                                node2change.put(node,
                                        new UndoableChangePropertyPair("", selectedNodeView.xProperty(), selectedNodeView.getX(), null, selectedNodeView.yProperty(), selectedNodeView.getY(), null));
                            }
                            selectedNodeView.xProperty().set(selectedNodeView.xProperty().get() + (point.getX() - mouseDownX));
                            selectedNodeView.yProperty().set(selectedNodeView.yProperty().get() + (point.getY() - mouseDownY));
                        }
                    }

                    mouseDownX = point.getX();
                    mouseDownY = point.getY();

                }
                if (controlDown && nodeView.getANode() instanceof ADataNode) {
                    final Point2D point = world.screenToLocal(e.getScreenX(), e.getScreenY());
                    line.setEndX(point.getX());
                    line.setEndY(point.getY());
                }
            }
        });

        nodeView.setOnMouseReleased((e) -> {
            if (nodeView == lock) {
                if (dragged) {
                    if (!controlDown) {
                        final ASelectionModel<ANode> selectionModel = dagView.getDag().getNodeSelectionModel();
                        for (ANode node : selectionModel.getSelectedItems()) {
                            final DagNodeView selectedNodeView = dagView.getNodeView(node);
                            UndoableChangePropertyPair pair = node2change.get(node);
                            if (pair != null) {
                                pair.setNewValue1(selectedNodeView.getX());
                                pair.setNewValue2(selectedNodeView.getY());
                            }
                            dagView.getUndoManager().addUndoableChange(new UndoableChangeList("Move", node2change.values()));
                        }
                    }
                } else {
                    final ASelectionModel<ANode> selectionModel = dagView.getDag().getNodeSelectionModel();
                    if (!e.isShiftDown()) {
                        selectionModel.clearSelection();
                    }
                    if (selectionModel.getSelectedItems().contains(nodeView.getANode())) {
                        selectionModel.clearSelection(nodeView.getANode());
                    } else
                        selectionModel.select(nodeView.getANode());

                    if (controlDown && nodeView.getANode() instanceof ADataNode) {
                        try {
                            new NewNodeDialog(dagView, nodeView, e);
                        } catch (IOException ex) {
                            Basic.caught(ex);
                        }
                    }
                    world.getChildren().remove(line);
                }
                lock = null;
            }
                }
        );
        nodeView.setOnMouseClicked(Event::consume);
    }

    /**
     * install a mouse handler
     *
     * @param nodeView
     */
    public static void install(DAGView dagView, Pane world, DagNodeView nodeView) {
        new DagNodeViewMouseHandler(dagView, world, nodeView);
    }
}
