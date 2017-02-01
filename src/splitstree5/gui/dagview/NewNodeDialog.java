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

package splitstree5.gui.dagview;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.dag.DAG;
import splitstree5.core.datablocks.ADataBlock;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.utils.ExtendedFXMLLoader;

import java.io.IOException;

/**
 * displays the new node dialog and creates a new node, if desired
 * Created by huson on 1/31/17.
 */
public class NewNodeDialog {
    private final Parent root;
    private final NewNodeDialogController controller;
    private Stage stage;

    /**
     * run the new node dialog
     *
     * @param dagView
     * @param sourceNodeView
     * @throws IOException
     */
    public NewNodeDialog(final DAGView dagView, DagNodeView sourceNodeView, MouseEvent me) throws IOException {
        final DAG dag = dagView.getDocument().getDag();
        final ADataNode sourceNode = (ADataNode) sourceNodeView.getANode();

        final ExtendedFXMLLoader<NewNodeDialogController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        root = extendedFXMLLoader.getRoot();
        controller = extendedFXMLLoader.getController();

        controller.getSourceDataLabel().setText(sourceNode.getDataBlock().getName());

        controller.getTargetDataChoiceBox().getItems().setAll(ADataBlock.getAllDataBlocks());
        controller.getTargetDataChoiceBox().getSelectionModel().select(0);

        controller.getTargetDataChoiceBox().valueProperty().addListener((observable, oldValue, newValue) -> {
            controller.getAlgorithmChoiceBox().getItems().setAll((new AConnector(dag.getWorkingTaxaBlock(), sourceNode, new ADataNode(newValue))).getAllAlgorithms());
            controller.getAlgorithmChoiceBox().getSelectionModel().select(0);
        });

        controller.getAlgorithmChoiceBox().getItems().setAll((new AConnector(dag.getWorkingTaxaBlock(), sourceNode, new ADataNode(controller.getTargetDataChoiceBox().getValue()))).getAllAlgorithms());
        controller.getAlgorithmChoiceBox().getSelectionModel().select(0);


        controller.getCancelButton().setOnAction((e) -> stage.close());

        controller.getApplyButton().setOnAction((e -> {
            final ADataNode childNode = new ADataNode(controller.getTargetDataChoiceBox().getValue());
            final AConnector connectorNode = dag.createConnector((ADataNode) sourceNode, (ADataNode) childNode, (Algorithm) controller.getAlgorithmChoiceBox().getValue());

            DagNodeView targetNodeView = new DagNodeView(dagView, childNode);
            DagNodeView connectorNodeView = new DagNodeView(dagView, connectorNode);

            double xTarget = me.getSceneX();
            double yTarget = me.getSceneY();

            targetNodeView.xProperty().set(xTarget);
            targetNodeView.yProperty().set(yTarget);

            connectorNodeView.xProperty().set((sourceNodeView.xProperty().get() + xTarget) / 2);
            connectorNodeView.yProperty().set((sourceNodeView.yProperty().get() + yTarget) / 2);

            dagView.getNodeViews().getChildren().addAll(targetNodeView, connectorNodeView);

            dagView.getEdgeViews().getChildren().addAll(new DagEdgeView(sourceNodeView, connectorNodeView), new DagEdgeView(connectorNodeView, targetNodeView));

            stage.close();

            connectorNode.forceRecompute();
        }));

        controller.getApplyButton().disableProperty().bind(controller.getTargetDataChoiceBox().valueProperty().isNull().or(controller.getAlgorithmChoiceBox().valueProperty().isNull()));

        stage = new Stage();
        stage.setTitle("New Node - SplitsTree5");
        stage.setScene(new Scene(root));

        stage.setX(me.getScreenX());
        stage.setY(me.getScreenY());

        stage.setAlwaysOnTop(true);

        stage.showAndWait();
    }
}
