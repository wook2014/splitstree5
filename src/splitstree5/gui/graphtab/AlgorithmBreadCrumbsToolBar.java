/*
 * AlgorithmBreadCrumbsToolBar.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.gui.graphtab;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import splitstree5.core.Document;
import splitstree5.core.datablocks.ViewerBlock;
import splitstree5.core.workflow.*;

import java.util.ArrayList;

/**
 * algorithms bread crumbs for viewer toolbar
 * Daniel Huson, 1.2018
 */
public class AlgorithmBreadCrumbsToolBar extends ToolBar {
    private final String shape = "-fx-shape: \"M 0 0 L 5 9 L 0 18 L 100 18 L 105 9 L 100 0 z\";"; // arrow shape
    private final String computingColor = "-fx-background-color: LIGHTBLUE;";

    private final Document document;
    private final WorkflowNode workflowNode;
    private final ArrayList<ChangeListener<UpdateState>> stateChangeListeners = new ArrayList<>();

    /**
     * constructor
     *
     * @param document
     * @param workflowNode
     */
    public AlgorithmBreadCrumbsToolBar(Document document, WorkflowNode workflowNode) {
        this.document = document;
        this.workflowNode = workflowNode;
        document.getWorkflow().getTopologyChanged().addListener((c, o, n) -> Platform.runLater(this::update));
    }

    /**
     * update the bread crumbs
     */
    public void update() {
        final Workflow workflow = document.getWorkflow();
        getItems().clear();
        if (document.getMainWindow().getInputTab() != null) {
            getItems().add(makeInputTabBreadCrumb());

        }

        if (workflow.getTaxaFilter() != null) {
            getItems().add(makeBreadCrumb(document, workflow.getTaxaFilter()));
        }
        if (workflow.getTopFilter() != null && workflow.getTopFilter().getChild() != null) {
            final ArrayList<Connector> connectors = new ArrayList<>();
            findConnectorsAlongPathRec(workflow.getTopFilter().getChild(), workflowNode, connectors);
            for (Connector connector : connectors) {
                getItems().add(makeBreadCrumb(document, connector));
            }
        }
        if (workflowNode instanceof DataNode && (((DataNode) workflowNode).getDataBlock() instanceof ViewerBlock))
            getItems().add(makeFormatBreadCrumb((DataNode) workflowNode, document));
    }

    /**
     * collects all connectors on the path to the target node
     *
     * @param workflowNode
     * @param targetNode
     * @param connectors
     * @return path of connectors
     */
    private boolean findConnectorsAlongPathRec(WorkflowNode workflowNode, WorkflowNode targetNode, ArrayList<Connector> connectors) {
        if (workflowNode instanceof Connector) {
            connectors.add((Connector) workflowNode);
        }
        if (workflowNode == targetNode)
            return true;
        for (WorkflowNode child : workflowNode.getChildren()) {
            if (findConnectorsAlongPathRec(child, targetNode, connectors))
                return true;
        }
        if (workflowNode instanceof Connector) {
            connectors.remove(workflowNode);
        }
        return false;
    }

    private Node makeBreadCrumb(Document document, Connector connector) {
        final Button button = new Button();
        button.setStyle(shape);
        button.textProperty().bind(connector.nameProperty());
        button.disableProperty().bind(connector.applicableProperty().not().and(connector.stateProperty().isEqualTo(UpdateState.VALID).not()));
        final Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bind(connector.shortDescriptionProperty());
        button.setTooltip(tooltip);

        final ChangeListener<UpdateState> stateChangeListener = (c, o, n) -> {
            switch (n) {
                case COMPUTING:
                    button.setTextFill(Color.BLACK);
                    button.setStyle(shape + computingColor);
                    break;
                case FAILED:
                    button.setTextFill(Color.DARKRED);
                    button.setStyle(shape);
                    break;
                default:
                    button.setTextFill(Color.BLACK);
                    button.setStyle(shape);
            }
        };
        connector.stateProperty().addListener(new WeakChangeListener<>(stateChangeListener));
        stateChangeListeners.add(stateChangeListener);

        button.setOnAction((e) -> {
            document.getMainWindow().showAlgorithmView(connector);
        });
        return button;
    }

    private Node makeFormatBreadCrumb(DataNode dataNode, Document document) {
        final Button button = new Button();
        button.setStyle(shape);
        button.setText("Format");
        button.disableProperty().bind(dataNode.stateProperty().isEqualTo(UpdateState.VALID).not());
        final Tooltip tooltip = new Tooltip("Format nodes and edges");
        button.setTooltip(tooltip);
        final ChangeListener<UpdateState> stateChangeListener = (c, o, n) -> {
            switch (n) {
                case COMPUTING:
                    button.setStyle(shape + computingColor);
                    break;
                case FAILED:
                    button.setStyle(shape); // can't fail
                    break;
                default:
                    button.setStyle(shape);
            }
        };
        dataNode.stateProperty().addListener(stateChangeListener);
        stateChangeListeners.add(stateChangeListener);
        button.setOnAction((e) -> {
            document.getMainWindow().showFormatTab();
        });
        return button;
    }

    private Node makeInputTabBreadCrumb() {
        final Button button = new Button();
        button.setStyle(shape);
        button.setText("Input");
        button.disableProperty().bind(document.getWorkflow().updatingProperty());
        final Tooltip tooltip = new Tooltip("Input data");
        button.setTooltip(tooltip);
        button.setOnAction((e) -> {
            document.getMainWindow().showInputTab();
        });
        return button;
    }

}
