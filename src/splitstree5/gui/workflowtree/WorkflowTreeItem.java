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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import jloda.util.ResourceManager;
import splitstree5.core.Document;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.workflow.ANode;
import splitstree5.core.workflow.UpdateState;


/**
 * a workflow tree node
 * Daniel Huson, 1.2018
 */
public class WorkflowTreeItem extends TreeItem<String> {
    private final Document document;
    private final ANode aNode;
    private BooleanProperty disable = new SimpleBooleanProperty();

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

            if (aNode instanceof AConnector) {
                disable.bind(((AConnector) aNode).applicableProperty().not().and(aNode.stateProperty().isEqualTo(UpdateState.VALID).not()).or(new ReadOnlyBooleanWrapper(aNode.getName().endsWith("TopFilter"))));
                final Image icon = ResourceManager.getIcon(aNode.getName().endsWith("Filter") ? "Filter16.gif" : "Algorithm16.gif");
                if (icon != null) {
                    label.setGraphic(new ImageView(icon));
                }
            } else {
                disable.bind(aNode.stateProperty().isEqualTo(UpdateState.VALID).not());
                Image icon = ResourceManager.getIcon(aNode.getName().replaceAll("^Orig", "").replaceAll(".*]", "") + "16.gif");
                if (icon != null) {
                    label.setGraphic(new ImageView(icon));
                }
            }
            tooltip.textProperty().bind(aNode.shortDescriptionProperty());
            Tooltip.install(getGraphic(), tooltip);

            aNode.stateProperty().addListener((c, o, n) -> {
                        switch (n) {
                            case COMPUTING:
                                label.setStyle("-fx-background-color: LIGHTBLUE;");
                                break;
                            case FAILED:
                                label.setStyle("-fx-background-color: PINK;");
                                break;
                            default:
                                label.setStyle("");
                        }
                    }
            );

            label.setOnContextMenuRequested((e) -> {
                final MenuItem show = new MenuItem("Open...");
                show.setOnAction((x) -> {
                    showView(e.getScreenX(), e.getScreenY());
                });
                show.disableProperty().bind(disable);
                final ContextMenu contextMenu = new ContextMenu(show);
                contextMenu.show(label, e.getScreenX(), e.getScreenY());
            });
            getGraphic().setOnMouseClicked((e) -> {
                if (e.getClickCount() == 2) {
                    if (!disable.get())
                        showView(e.getScreenX(), e.getScreenY());
                    e.consume();
                }
            });
        }

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
     * @param screenX
     * @param screenY
     */
    public void showView(double screenX, double screenY) {
        if (aNode instanceof ADataNode)
            document.getMainWindow().showDataView((ADataNode) aNode);
        else {
            document.getMainWindow().showAlgorithmView((AConnector) aNode);
        }
    }
}
