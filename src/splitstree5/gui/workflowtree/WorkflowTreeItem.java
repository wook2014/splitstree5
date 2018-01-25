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
import jloda.util.ResourceManager;
import splitstree5.core.Document;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.workflow.ANode;
import splitstree5.core.workflow.UpdateState;
import splitstree5.dialogs.exporter.ExportDialog;


/**
 * a workflow tree node
 * Daniel Huson, 1.2018
 */
public class WorkflowTreeItem extends TreeItem<String> {
    private final Document document;
    private final ANode aNode;
    private BooleanProperty disable = new SimpleBooleanProperty();
    private final ChangeListener<UpdateState> stateChangeListener;

    /**
     * constructor
     *
     * @param aNode
     */
    public WorkflowTreeItem(Document document, ANode aNode) {
        super("");
        this.document = document;
        this.aNode = aNode;

        final Label label = new Label();
        setGraphic(label);

        if (aNode != null) {
            label.textProperty().bind(aNode.nameProperty());
            final Tooltip tooltip = new Tooltip();
            final RotateTransition rotateTransition;

            if (aNode instanceof AConnector) {
                disable.bind(((AConnector) aNode).applicableProperty().not().and(aNode.stateProperty().isEqualTo(UpdateState.VALID).not()).or(new ReadOnlyBooleanWrapper(aNode.getName().endsWith("TopFilter"))));
                final Image icon = ResourceManager.getIcon(aNode.getName().endsWith("Filter") ? "Filter16.gif" : "Algorithm16.gif");
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
                disable.bind(aNode.stateProperty().isEqualTo(UpdateState.VALID).not());
                Image icon = ResourceManager.getIcon(aNode.getName().replaceAll("^Orig", "").replaceAll(".*]", "") + "16.gif");
                if (icon != null) {
                    label.setGraphic(new ImageView(icon));
                }
                rotateTransition = null;
            }
            tooltip.textProperty().bind(aNode.shortDescriptionProperty());
            Tooltip.install(getGraphic(), tooltip);

            stateChangeListener = (c, o, n) -> {
                // System.err.println("State change: " + aNode.getName() + ": " + n);
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
            aNode.stateProperty().addListener(new WeakChangeListener<>(stateChangeListener));
            {
                final MenuItem show = new MenuItem("Open...");
                show.setOnAction((x) -> {
                    showView();
                });
                show.disableProperty().bind(disable);
                MenuItem export = new MenuItem("Export...");
                export.setOnAction((x) -> {
                    if (aNode instanceof ADataNode)
                        ExportDialog.show(document.getMainWindow(), document.getWorkflow().getWorkingTaxaBlock(), ((ADataNode) aNode).getDataBlock());
                });
                export.setDisable(!(aNode instanceof ADataNode));
                label.setContextMenu(new ContextMenu(show, new SeparatorMenuItem(), export));
            }
            label.setOnMouseClicked((e) -> {
                if (e.getClickCount() == 2) {
                    showView();
                    e.consume();
                }
            });
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
     *
     */
    public void showView() {
        if (aNode instanceof ADataNode)
            document.getMainWindow().showDataView((ADataNode) aNode);
        else {
            document.getMainWindow().showAlgorithmView((AConnector) aNode);
        }
    }
}
