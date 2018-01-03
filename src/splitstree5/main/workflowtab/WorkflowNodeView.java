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

package splitstree5.main.workflowtab;

import com.sun.istack.internal.NotNull;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.transform.Shear;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.workflow.ANode;
import splitstree5.gui.connectorview.ANodeViewManager;

/**
 * a workflow node view
 * Created by huson on 1/27/17.
 */
public class WorkflowNodeView extends Group {
    private final Rectangle rectangle;
    private final ANode aNode;

    /**
     * constructor
     *
     * @param workflowView
     * @param aNode
     */
    public WorkflowNodeView(WorkflowViewTab workflowView, @NotNull ANode aNode) {
        rectangle = new Rectangle(160, 60);
        this.aNode = aNode;
        aNode.stateProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case VALID:
                    rectangle.setFill(Color.WHITE);
                    break;
                case COMPUTING:
                    rectangle.setFill(Color.LIGHTYELLOW.darker());
                    break;
                case FAILED:
                    rectangle.setFill(Color.PINK);
                    break;
                case INVALID:
                    rectangle.setFill(Color.LIGHTYELLOW.brighter());
                    break;
                case UPDATE_PENDING:
                    rectangle.setFill(Color.SANDYBROWN.brighter());
                    break;
                default:
                    rectangle.setFill(Color.LIGHTGRAY);
            }
        });
        rectangle.setFill(Color.LIGHTGRAY);
        rectangle.setStroke(Color.DARKGRAY);
        getChildren().add(rectangle);

        final Label label = new Label();
        label.textProperty().bind(aNode.nameProperty());
        label.setLayoutX(10);
        label.setLayoutY(4);
        getChildren().add(label);

        if (aNode instanceof AConnector) {
            final Label descriptionLabel = new Label();
            aNode.stateProperty().addListener(observable -> descriptionLabel.setText(aNode.getShortDescription()));
            descriptionLabel.setText(aNode.getShortDescription());
            descriptionLabel.setLayoutX(10);
            descriptionLabel.setLayoutY(24);
            getChildren().add(descriptionLabel);

            final Button openButton = new Button("Open...");
            openButton.setFont(new Font(openButton.getFont().getName(), 10));
            openButton.setOnAction((e) -> {
                ANodeViewManager.getInstance().show(workflowView.getDocument(), (AConnector) aNode, this.getX(), this.getY());
            });
            openButton.setPrefWidth(50);
            openButton.setPrefHeight(20);
            openButton.setLayoutX(rectangle.getWidth() - 53);
            openButton.setLayoutY(rectangle.getHeight() - 23);
            getChildren().add(openButton);

            final ProgressBar progressBar = new ProgressBar();
            progressBar.setPrefWidth(70);
            progressBar.setPrefHeight(20);
            progressBar.setLayoutX(10);
            progressBar.setLayoutY(rectangle.getHeight() - 23);
            progressBar.visibleProperty().bind(((AConnector) aNode).getService().runningProperty());
            progressBar.progressProperty().bind(((AConnector) aNode).getService().progressProperty());
            getChildren().add(progressBar);
        } else if (aNode instanceof ADataNode) {
            {
                Shear sh = new Shear(-0.2, 0, rectangle.getX() + rectangle.getWidth() / 2, rectangle.getY() + rectangle.getHeight() / 2);
                rectangle.getTransforms().add(sh);
            }
            final Label sizeLabel = new Label();
            sizeLabel.setFont(new Font(sizeLabel.getFont().getName(), 10));
            aNode.stateProperty().addListener(observable -> sizeLabel.setText("Size=" + ((ADataNode) aNode).getDataBlock().size()));
            sizeLabel.setText("Size=" + ((ADataNode) aNode).getDataBlock().size());
            sizeLabel.setLayoutX(10);
            sizeLabel.setLayoutY(24);
            getChildren().add(sizeLabel);

            Button openButton = new Button("Open...");
            openButton.setFont(new Font(openButton.getFont().getName(), 10));
            openButton.setOnAction((e) -> ANodeViewManager.getInstance().show(workflowView.getDocument(), aNode, this.getX(), this.getY()));

            openButton.setPrefWidth(50);
            openButton.setPrefHeight(20);
            openButton.setLayoutX(rectangle.getWidth() - 58);
            openButton.setLayoutY(rectangle.getHeight() - 23);
            getChildren().add(openButton);
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

    public ANode getANode() {
        return aNode;
    }

    public void setXY(double x, double y) {
        xProperty().set(x);
        yProperty().set(y);
    }
}
