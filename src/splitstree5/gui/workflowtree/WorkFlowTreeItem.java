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
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;
import splitstree5.core.Document;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.workflow.ANode;
import splitstree5.core.workflow.UpdateState;
import splitstree5.gui.connectorview.ANodeViewManager;


/**
 * a workflow tree node
 * Daniel Huson, 1.2018
 */
public class WorkFlowTreeItem extends TreeItem<String> {
    private final Document document;
    private final ANode aNode;
    private BooleanProperty disable = new SimpleBooleanProperty();

    /**
     * constructor
     *
     * @param aNode
     */
    public WorkFlowTreeItem(Document document, ANode aNode) {
        this.document = document;
        this.aNode = aNode;

        final Label label = new Label();
        setGraphic(label);

        if (aNode != null) {
            label.textProperty().bind(aNode.nameProperty());

            if (aNode instanceof AConnector) {
                disable.bind(((AConnector) aNode).applicableProperty().not().and(aNode.stateProperty().isEqualTo(UpdateState.VALID).not()));
            } else {
                disable.bind(aNode.stateProperty().isEqualTo(UpdateState.VALID).not());
            }
            aNode.stateColorProperty().addListener((c, o, n) -> getGraphic().setStyle(n));

            label.setOnContextMenuRequested((e) -> {
                if (!disable.get()) {
                    final MenuItem show = new MenuItem("Open...");
                    show.setOnAction((x) -> {
                        showView(e.getScreenX(), e.getScreenY());
                    });
                    final ContextMenu contextMenu = new ContextMenu(show);
                    contextMenu.show(label, e.getScreenX(), e.getScreenY());
                }
            });
            getGraphic().setOnMouseClicked((e) -> {
                if (e.getClickCount() == 2) {
                    showView(e.getScreenX(), e.getScreenY());
                }
            });
            setValue("");
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
        ANodeViewManager.getInstance().show(document, aNode, screenX, screenY);
    }
}
