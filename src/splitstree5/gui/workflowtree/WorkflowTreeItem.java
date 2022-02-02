/*
 * WorkflowTreeItem.java Copyright (C) 2022 Daniel H. Huson
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

package splitstree5.gui.workflowtree;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import jloda.fx.util.ResourceManagerFX;
import splitstree5.core.Document;
import splitstree5.core.workflow.Connector;
import splitstree5.core.workflow.DataNode;
import splitstree5.core.workflow.UpdateState;
import splitstree5.core.workflow.WorkflowNode;
import splitstree5.dialogs.exporter.ExportDialog;

import java.util.Collections;


/**
 * a workflow tree node
 * Daniel Huson, 1.2018
 */
public class WorkflowTreeItem extends TreeItem<String> {
    private final Document document;
    private final WorkflowNode workflowNode;
	private final BooleanProperty disable = new SimpleBooleanProperty();
	private final ChangeListener<UpdateState> stateChangeListener;

    /**
     * constructor
     *
	 */
    public WorkflowTreeItem(Document document, WorkflowNode workflowNode) {
        super("");
        this.document = document;
        this.workflowNode = workflowNode;

        final Label label = new Label();
        setGraphic(label);

        if (workflowNode != null) {
            label.textProperty().bind(workflowNode.nameProperty());
            final Tooltip tooltip = new Tooltip();
            final RotateTransition rotateTransition;

            if (workflowNode instanceof Connector) {
                disable.bind(((Connector) workflowNode).applicableProperty().not().and(workflowNode.stateProperty().isEqualTo(UpdateState.VALID).not()).or(new ReadOnlyBooleanWrapper(workflowNode.getName().endsWith("TopFilter"))));
                final Image icon = ResourceManagerFX.getIcon(workflowNode.getName().endsWith("Filter") ? "Filter16.gif" : "Algorithm16.gif");
                if (icon != null) {
                    final ImageView imageView = new ImageView(icon);
                    rotateTransition = new RotateTransition(Duration.millis(1000), imageView);
                    rotateTransition.setByAngle(360);
                    rotateTransition.setCycleCount(Animation.INDEFINITE);
                    rotateTransition.setInterpolator(Interpolator.LINEAR);
                    label.setGraphic(imageView);
                } else
                    rotateTransition = null;
            } else { // a data node
                disable.bind(workflowNode.stateProperty().isEqualTo(UpdateState.VALID).not());
                Image icon = ResourceManagerFX.getIcon(workflowNode.getName().replaceAll("Input", "").replaceAll(".*]", "") + "16.gif");
                if (icon != null) {
                    label.setGraphic(new ImageView(icon));
                }
                rotateTransition = null;
            }
            tooltip.textProperty().bind(workflowNode.shortDescriptionProperty());
            Tooltip.install(getGraphic(), tooltip);

            stateChangeListener = (c, o, n) -> {
                // System.err.println("State change: " + workflowNode.getName() + ": " + n);
                switch (n) {
                    case COMPUTING:
                        label.setTextFill(Color.BLACK);
                        label.setStyle("-fx-background-color: LIGHTBLUE;");
                        if (rotateTransition != null)
                            rotateTransition.play();
                        break;
                    case FAILED:
                        label.setStyle("");
                        label.setTextFill(Color.DARKRED);
                        if (rotateTransition != null) {
                            rotateTransition.stop();
                            rotateTransition.getNode().setRotate(0);
                        }
                        break;
                    default:
                        label.setTextFill(Color.BLACK);
                        label.setStyle("");
                        if (rotateTransition != null) {
                            rotateTransition.stop();
                            rotateTransition.getNode().setRotate(0);
                        }
                }
            };
            workflowNode.stateProperty().addListener(new WeakChangeListener<>(stateChangeListener));
            {
                final MenuItem show = new MenuItem("Open...");
				show.setOnAction((x) -> showView());
				show.disableProperty().bind(disable);
                final MenuItem export = new MenuItem("Export...");
                export.setOnAction((x) -> {
                    if (workflowNode instanceof DataNode)
                        ExportDialog.show(document.getMainWindow().getStage(), document.getWorkflow().getWorkingTaxaBlock(), ((DataNode) workflowNode).getDataBlock());
                });
                export.setDisable(!(workflowNode instanceof DataNode));

                final MenuItem duplicate = new MenuItem("Duplicate");
                duplicate.setOnAction((x) -> {
                    if (workflowNode instanceof Connector) {
                        document.getWorkflow().selectAllBelow(Collections.singletonList(workflowNode), false);
                        document.getMainWindow().getWorkflowTab().callDuplicateCommand();
                    }
                });
                duplicate.setDisable(!(workflowNode instanceof Connector));

                label.setContextMenu(new ContextMenu(show, new SeparatorMenuItem(), export, new SeparatorMenuItem(), duplicate));
            }
            label.setOnMouseClicked((e) -> {
                if (e.getClickCount() == 2) {
                    showView();
                    e.consume();
                }
            });
        } else
            stateChangeListener = null;

        disable.addListener((c, o, n) -> {
            if (n)
                label.setTextFill(Color.LIGHTGRAY);
            else
                label.setTextFill(Color.BLACK);
        });
    }

    /**
     * show the view for this node
     */
    public void showView() {
        if (workflowNode instanceof DataNode)
            document.getMainWindow().showDataView((DataNode) workflowNode);
        else {
            document.getMainWindow().showAlgorithmView((Connector) workflowNode);
        }
    }
}
