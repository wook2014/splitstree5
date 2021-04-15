/*
 * WorkflowViewMouseHandler.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.gui.workflowtab;

import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import jloda.fx.control.ItemSelectionModel;
import jloda.fx.undo.UndoableChangeList;
import jloda.fx.undo.UndoableChangePropertyPair;
import jloda.util.Basic;
import splitstree5.core.workflow.DataNode;
import splitstree5.core.workflow.WorkflowNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * installs a mouse handler
 * Daniel Huson, 1/31/17.
 */
public class WorkflowViewMouseHandler {
    private double mouseDownX;
    private double mouseDownY;

    private final Map<WorkflowNode, UndoableChangePropertyPair> node2change = new HashMap<>();

    private boolean shiftDown;
    private boolean controlDown;
    private boolean dragged;

    private final Line line = new Line();
    private static WorkflowNodeView lock = null;


    /**
     * install a mouse handler
     *
     * @param nodeView
     */
    public static void install(WorkflowViewTab workflowView, Group group, WorkflowNodeView nodeView) {
        new WorkflowViewMouseHandler(workflowView, group, nodeView);
    }

    /**
     * setup the mouse handler
     *
     * @param nodeView
     */
    private WorkflowViewMouseHandler(WorkflowViewTab workflowView, Group group, WorkflowNodeView nodeView) {
        line.setStroke(Color.DARKGRAY);

        nodeView.setOnMousePressed((e) -> {
            if (lock == null) {
                lock = nodeView;
                dragged = false;
                group.getChildren().remove(line);

                final Point2D point = group.screenToLocal(e.getScreenX(), e.getScreenY());
                mouseDownX = point.getX();
                mouseDownY = point.getY();
                ;

                shiftDown = e.isShiftDown();
                controlDown = e.isControlDown();

                node2change.clear();

                if (controlDown && !shiftDown && nodeView.getANode() instanceof DataNode) {
                    line.setStartX(mouseDownX);
                    line.setStartY(mouseDownY);
                    line.setEndX(mouseDownX);
                    line.setEndY(mouseDownY);
                    group.getChildren().add(line);
                }
            }
        });

        nodeView.setOnMouseDragged((e) -> {
            if (nodeView == lock) {
                if (!controlDown) {
                    dragged = true;

                    final ItemSelectionModel<WorkflowNode> selectionModel = workflowView.getWorkflow().getNodeSelectionModel();

                    if (!selectionModel.getSelectedItems().contains(nodeView.getANode())) {
                        if (!e.isShiftDown())
                            selectionModel.clearSelection();
                        selectionModel.select(nodeView.getANode());
                    }

                    final boolean setupUndo = (node2change.size() == 0);

                    final Point2D point = group.screenToLocal(e.getScreenX(), e.getScreenY());
                    for (WorkflowNode node : selectionModel.getSelectedItems()) {
                        final WorkflowNodeView selectedNodeView = workflowView.getNodeView(node);
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
                if (controlDown && nodeView.getANode() instanceof DataNode) {
                    final Point2D point = group.screenToLocal(e.getScreenX(), e.getScreenY());
                    line.setEndX(point.getX());
                    line.setEndY(point.getY());
                }
            }
        });

        nodeView.setOnMouseReleased((e) -> {
                    if (nodeView == lock) {
                        if (dragged) {
                            if (!controlDown) {
                                final ItemSelectionModel<WorkflowNode> selectionModel = workflowView.getWorkflow().getNodeSelectionModel();
                                for (WorkflowNode node : selectionModel.getSelectedItems()) {
                                    final WorkflowNodeView selectedNodeView = workflowView.getNodeView(node);
                                    UndoableChangePropertyPair pair = node2change.get(node);
                                    if (pair != null) {
                                        pair.setNewValue1(selectedNodeView.getX());
                                        pair.setNewValue2(selectedNodeView.getY());
                                    }
                                    workflowView.getUndoManager().add(new UndoableChangeList("Move", node2change.values()));
                                }
                            }
                        } else {
                            final ItemSelectionModel<WorkflowNode> selectionModel = workflowView.getWorkflow().getNodeSelectionModel();
                            if (!e.isShiftDown()) {
                                selectionModel.clearSelection();
                            }
                            if (selectionModel.isSelected(nodeView.getANode())) {
                                selectionModel.clearSelection(nodeView.getANode());
                            } else
                                selectionModel.select(nodeView.getANode());

                            if (controlDown && nodeView.getANode() instanceof DataNode) {
                                try {
                                    new NewNodeDialog(workflowView, nodeView, e);
                                } catch (IOException ex) {
                                    Basic.caught(ex);
                                }
                            }
                            group.getChildren().remove(line);
                        }
                        lock = null;
                    }
                }
        );
        nodeView.setOnMouseClicked(Event::consume);
    }
}
