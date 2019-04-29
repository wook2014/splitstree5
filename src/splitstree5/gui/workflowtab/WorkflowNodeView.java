/*
 *  WorkflowNodeView.java Copyright (C) 2019 Daniel H. Huson
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

import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.transform.Shear;
import splitstree5.core.workflow.Connector;
import splitstree5.core.workflow.DataNode;
import splitstree5.core.workflow.UpdateState;
import splitstree5.core.workflow.WorkflowNode;

import java.util.ArrayList;

/**
 * a workflow node view
 * Daniel Huson, 1/27/17.
 */
public class WorkflowNodeView extends Group {
    private final Rectangle rectangle;
    private final WorkflowNode workflowNode;
    private final ArrayList<ChangeListener<UpdateState>> stateChangeListeners = new ArrayList<>();

    /**
     * constructor
     *
     * @param workflowView
     * @param workflowNode
     */
    public WorkflowNodeView(WorkflowViewTab workflowView, WorkflowNode workflowNode) {
        rectangle = new Rectangle(160, 60);
        this.workflowNode = workflowNode;

        if (workflowNode == null)
            return;

        {
            final ChangeListener<UpdateState> stateChangeListener = (c, o, n) -> {
                switch (n) {
                    case COMPUTING:
                        rectangle.setStyle("-fx-fill: LIGHTBLUE;");
                        break;
                    case FAILED:
                        rectangle.setStyle("-fx-fill: DARKRED;");
                        break;
                    case VALID:
                        rectangle.setStyle("-fx-fill: LIGHTGRAY;");
                        break;
                    default:
                        rectangle.setStyle("-fx-fill: DARKGRAY;");
                }
            };

            workflowNode.stateProperty().addListener(new WeakChangeListener<>(stateChangeListener));
            stateChangeListeners.add(stateChangeListener);
        }

        rectangle.setStyle("-fx-fill: LIGHTGRAY;");
        rectangle.setStroke(Color.DARKGRAY);
        getChildren().add(rectangle);

        final Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bind(workflowNode.shortDescriptionProperty());
        Tooltip.install(rectangle, tooltip);

        final Label label = new Label();
        label.setMouseTransparent(true);
        label.textProperty().bind(workflowNode.titleProperty());
        label.setFont(Font.font("Helvetica", 12));
        label.setLayoutX(10);
        label.setLayoutY(4);
        getChildren().add(label);

        if (workflowNode instanceof Connector) {
            final Label descriptionLabel = new Label();
            descriptionLabel.setFont(Font.font("Helvetica", 11));
            descriptionLabel.setMouseTransparent(true);

            // todo: uncomment the next line, if the description says something interesting...
            //descriptionLabel.textProperty().bind(workflowNode.shortDescriptionProperty());
            descriptionLabel.setLayoutX(10);
            descriptionLabel.setLayoutY(24);
            getChildren().add(descriptionLabel);

            rectangle.setOnMouseClicked((e) -> {
                if (e.getClickCount() == 2 && workflowNode.getState() == UpdateState.VALID
                        && !((Connector) workflowNode).getAlgorithm().getName().endsWith("TopFilter"))
                    workflowView.getDocument().getMainWindow().showAlgorithmView((Connector) workflowNode);
            });

            final ProgressBar progressBar = new ProgressBar();
            progressBar.setPrefWidth(70);
            progressBar.setPrefHeight(20);
            progressBar.setLayoutX(10);
            progressBar.setLayoutY(rectangle.getHeight() - 23);
            progressBar.visibleProperty().bind(((Connector) workflowNode).getService().runningProperty());
            progressBar.progressProperty().bind(((Connector) workflowNode).getService().progressProperty());
            getChildren().add(progressBar);
        } else if (workflowNode instanceof DataNode) {
            {
                Shear sh = new Shear(-0.2, 0, rectangle.getX() + rectangle.getWidth() / 2, rectangle.getY() + rectangle.getHeight() / 2);
                rectangle.getTransforms().add(sh);
            }
            final Label sizeLabel = new Label();
            sizeLabel.setMouseTransparent(true);

            sizeLabel.setFont(Font.font("Helvetica", 11));
            {
                final ChangeListener<UpdateState> stateChangeListener = (c, o, n) -> sizeLabel.setText("Size=" + ((DataNode) workflowNode).getDataBlock().size());
                workflowNode.stateProperty().addListener(new WeakChangeListener<>(stateChangeListener));
                stateChangeListeners.add(stateChangeListener);
            }
            sizeLabel.setText("Size=" + ((DataNode) workflowNode).getDataBlock().size());
            sizeLabel.setLayoutX(10);
            sizeLabel.setLayoutY(24);
            getChildren().add(sizeLabel);

            rectangle.setOnMouseClicked((e) -> {
                if (e.getClickCount() == 2 && workflowNode.getState() == UpdateState.VALID) {
                    workflowView.getDocument().getMainWindow().showDataView((DataNode) workflowNode);
                }
            });
        }

        WorkflowViewMouseHandler.install(workflowView, workflowView.getScrollPane().getContentGroup(), this);
    }

    public double getX() {
        return xProperty().get();
    }

    public void setX(double x) {
        xProperty().set(x);
    }

    public DoubleProperty xProperty() {
        return super.layoutXProperty();
    }

    public double getY() {
        return yProperty().get();
    }

    public void setY(double y) {
        yProperty().set(y);
    }

    public DoubleProperty yProperty() {
        return super.layoutYProperty();
    }

    public DoubleProperty widthProperty() {
        return rectangle.widthProperty();

    }

    public DoubleProperty heightProperty() {
        return rectangle.heightProperty();
    }

    public WorkflowNode getANode() {
        return workflowNode;
    }

    public void setXY(double x, double y) {
        xProperty().set(x);
        yProperty().set(y);
    }
}
