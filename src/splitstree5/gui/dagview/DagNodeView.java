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

import com.sun.istack.internal.NotNull;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Shear;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.dag.ANode;
import splitstree5.core.dag.UpdateState;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.gui.connectorview.ConnectorView;
import splitstree5.gui.textview.TextView;
import splitstree5.io.nexus.NexusFileWriter;

import java.io.IOException;

/**
 * a Dag node view
 * Created by huson on 1/27/17.
 */
public class DagNodeView extends Group {
    private final Rectangle rectangle;
    private final ANode aNode;
    private TextView textView;
    private ConnectorView connectorView;
    private ChangeListener<UpdateState> stateChangeListener;


    /**
     * constructor
     *
     * @param dagView
     * @param aNode
     */
    public DagNodeView(DAGView dagView, @NotNull ANode aNode) {
        rectangle = new Rectangle(200, 100);
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
                openButton.setOnAction((e) -> {
                    try {
                        if (connectorView == null)
                            connectorView = new ConnectorView(dagView.getDocument(), (AConnector) aNode);
                        connectorView.show(this.getX(), this.getY());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                });
                openButton.setPrefWidth(70);
                openButton.setPrefHeight(30);
                openButton.setLayoutX(rectangle.getWidth() - 83);
                openButton.setLayoutY(rectangle.getHeight() - 33);
                getChildren().add(openButton);

                final ProgressBar progressBar = new ProgressBar();
                progressBar.setPrefWidth(70);
                progressBar.setPrefHeight(30);
                progressBar.setLayoutX(10);
                progressBar.setLayoutY(rectangle.getHeight() - 33);
                progressBar.visibleProperty().bind(((AConnector) aNode).getService().runningProperty());
                progressBar.progressProperty().bind(((AConnector) aNode).getService().progressProperty());
                getChildren().add(progressBar);
        } else if (aNode instanceof ADataNode) {
            {
                Shear sh = new Shear(-0.2, 0, rectangle.getX() + rectangle.getWidth() / 2, rectangle.getY() + rectangle.getHeight() / 2);
                rectangle.getTransforms().add(sh);
            }
            final Label sizeLabel = new Label();
            aNode.stateProperty().addListener(observable -> sizeLabel.setText("Size=" + ((ADataNode) aNode).getDataBlock().size()));
            sizeLabel.setText("Size=" + ((ADataNode) aNode).getDataBlock().size());
            sizeLabel.setLayoutX(10);
            sizeLabel.setLayoutY(24);
            getChildren().add(sizeLabel);

            Button openButton = new Button("Open...");
            openButton.setOnAction((e) -> {

                try {
                    if (textView == null) {
                        final StringProperty textProperty = new SimpleStringProperty(NexusFileWriter.toString(dagView.getDag().getWorkingTaxaNode().getDataBlock(), ((ADataNode) aNode).getDataBlock()));
                        stateChangeListener = (observable, oldValue, newValue) -> textProperty.set(NexusFileWriter.toString(dagView.getDag().getWorkingTaxaNode().getDataBlock(), ((ADataNode) aNode).getDataBlock()));
                        aNode.stateProperty().addListener(new WeakChangeListener<>(stateChangeListener));
                        textView = new TextView(textProperty);
                    }
                    textView.show(this.getX(), this.getY());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
            openButton.setPrefWidth(70);
            openButton.setPrefHeight(30);
            openButton.setLayoutX(rectangle.getWidth() - 83);
            openButton.setLayoutY(rectangle.getHeight() - 33);
            getChildren().add(openButton);
        }

        DagNodeViewMouseHandler.install(dagView, dagView.getCenterPane(), this);
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
