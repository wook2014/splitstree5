/*
 *  Copyright (C) 2019 Daniel H. Huson
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
 *  Copyright (C) 2019 Daniel H. Huson
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
 *  Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.gui.workflowtab;

import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import jloda.fx.ExtendedFXMLLoader;
import jloda.util.Basic;
import jloda.util.ProgramProperties;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.views.TreeEmbedder;
import splitstree5.core.algorithms.views.TreesGrid;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.ViewerBlock;
import splitstree5.core.workflow.Connector;
import splitstree5.core.workflow.DataNode;
import splitstree5.core.workflow.Workflow;
import splitstree5.undo.UndoableRedoableCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * displays the new node dialog and creates a new node, if desired
 * Daniel Huson, 1/31/17.
 */
public class NewNodeDialog {
    private final Parent root;
    private final NewNodeDialogController controller;
    private Stage stage;

    /**
     * run the new node dialog
     *
     * @param workflowView
     * @param sourceNodeView
     * @throws IOException
     */
    public NewNodeDialog(final WorkflowViewTab workflowView, WorkflowNodeView sourceNodeView, MouseEvent me) throws IOException {
        final Workflow workflow = workflowView.getDocument().getWorkflow();
        final DataNode sourceNode = (DataNode) sourceNodeView.getANode();

        final ExtendedFXMLLoader<NewNodeDialogController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        root = extendedFXMLLoader.getRoot();
        controller = extendedFXMLLoader.getController();

        controller.getSourceDataLabel().setText(sourceNode.getDataBlock().getName());

        controller.getTargetDataComboBox().getItems().setAll(DataBlock.getAllDataBlocks());
        controller.getTargetDataComboBox().getSelectionModel().select(0);

        controller.getTargetDataComboBox().valueProperty().addListener((observable, oldValue, newValue) -> {
            controller.getAlgorithmChoiceBox().getItems().setAll((new Connector(workflow.getWorkingTaxaBlock(), sourceNode, new DataNode(newValue), false)).getAllAlgorithms());
            if (newValue instanceof ViewerBlock.TreeViewerBlock)
                removeAlgorithm(controller.getAlgorithmChoiceBox().getItems(), TreesGrid.class);
            if (newValue instanceof ViewerBlock.TreesGridBlock)
                removeAlgorithm(controller.getAlgorithmChoiceBox().getItems(), TreeEmbedder.class);

            controller.getAlgorithmChoiceBox().getItems().remove(new TreesGrid());
            controller.getAlgorithmChoiceBox().getSelectionModel().select(0);
        });

        controller.getAlgorithmChoiceBox().getItems().setAll((new Connector(workflow.getWorkingTaxaBlock(), sourceNode, new DataNode(controller.getTargetDataComboBox().getValue()), false)).getAllAlgorithms());
        controller.getAlgorithmChoiceBox().getSelectionModel().select(0);

        controller.getCancelButton().setOnAction((e) -> stage.close());

        controller.getApplyButton().setOnAction((e -> {
            final Point2D point = workflowView.getScrollPane().getContentGroup().screenToLocal(me.getScreenX(), me.getScreenY());

            makeNewNodes(workflowView, sourceNodeView, controller.getTargetDataComboBox().getValue(), point.getX(), point.getY());
            stage.close();
        }));

        controller.getApplyButton().disableProperty().bind(controller.getTargetDataComboBox().valueProperty().isNull().or(controller.getAlgorithmChoiceBox().valueProperty().isNull()));

        stage = new Stage();
        stage.setTitle("New Node - " + ProgramProperties.getProgramName());
        stage.setScene(new Scene(root));

        stage.setX(me.getScreenX());
        stage.setY(me.getScreenY());

        stage.setAlwaysOnTop(true);

        stage.showAndWait();
    }

    private static void removeAlgorithm(List<? extends Algorithm> algorithms, Class clazz) {
        final String name = Basic.getShortName(clazz);
        for (Algorithm algorithm : algorithms) {
            if (Basic.getShortName(algorithm.getClass()).equals(name)) {
                algorithms.remove(algorithm);
                break;
            }
        }
    }

    /**
     * make the new nodes
     *
     * @param workflowView
     * @param sourceNodeView
     * @param childDataBlock
     * @param xTarget
     * @param yTarget
     */
    private void makeNewNodes(final WorkflowViewTab workflowView, WorkflowNodeView sourceNodeView, DataBlock childDataBlock, double xTarget, double yTarget) {
        final DataNode targetNode = new DataNode(childDataBlock);
        workflowView.getWorkflow().addDataNode(targetNode);
        final Connector connectorNode = workflowView.getWorkflow().createConnector((DataNode) sourceNodeView.getANode(), (DataNode) targetNode, (Algorithm) controller.getAlgorithmChoiceBox().getValue());
        final WorkflowNodeView targetNodeView = new WorkflowNodeView(workflowView, targetNode);
        final WorkflowNodeView connectorNodeView = new WorkflowNodeView(workflowView, connectorNode);

        targetNodeView.xProperty().set(xTarget);
        targetNodeView.yProperty().set(yTarget);

        connectorNodeView.xProperty().set((sourceNodeView.xProperty().get() + xTarget) / 2);
        connectorNodeView.yProperty().set((sourceNodeView.yProperty().get() + yTarget) / 2);

        final WorkflowEdgeView edgeView1 = new WorkflowEdgeView(sourceNodeView, connectorNodeView);
        final WorkflowEdgeView edgeView2 = new WorkflowEdgeView(connectorNodeView, targetNodeView);

        UndoableRedoableCommand undoableChange = new UndoableRedoableCommand("Create Node") {
            @Override
            public void undo() {
                targetNode.clear();
                workflowView.getWorkflow().delete(connectorNode, true, false);
                workflowView.getWorkflow().delete(targetNode, true, false);
                workflowView.getNodeViews().getChildren().removeAll(connectorNodeView, targetNodeView);
                workflowView.getEdgeViews().getChildren().removeAll(edgeView1, edgeView2);

                workflowView.getNode2NodeView().remove(connectorNode);
                workflowView.getNode2EdgeViews().remove(connectorNode);
                workflowView.getNode2NodeView().remove(targetNode);
            }

            @Override
            public void redo() {
                workflowView.getNode2NodeView().put(targetNode, targetNodeView);
                workflowView.getNode2NodeView().put(connectorNode, connectorNodeView);
                ArrayList<WorkflowEdgeView> list = new ArrayList<>();
                list.add(edgeView1);
                list.add(edgeView2);
                workflowView.getNode2EdgeViews().put(connectorNode, list);

                workflowView.getWorkflow().addConnector(connectorNode);
                workflowView.getWorkflow().addDataNode(targetNode);
                workflowView.getNodeViews().getChildren().addAll(connectorNodeView, targetNodeView);
                workflowView.getEdgeViews().getChildren().addAll(edgeView1, edgeView2);

                connectorNode.forceRecompute();
            }
        };
        undoableChange.redo();

        workflowView.getUndoManager().add(undoableChange);
    }
}
