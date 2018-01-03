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

package splitstree5.main.graphtab;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import splitstree5.core.Document;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.workflow.ANode;
import splitstree5.core.workflow.UpdateState;
import splitstree5.core.workflow.Workflow;
import splitstree5.gui.connectorview.ANodeViewManager;

import java.util.ArrayList;

/**
 * algorithms bread crumbs for viewer toolbar
 * Daniel Huson, 1.2018
 */
public class AlgorithmBreadCrumbsToolBar extends ToolBar {
    private final Document document;
    private final ANode aNode;

    /**
     * constructor
     *
     * @param document
     * @param aNode
     */
    public AlgorithmBreadCrumbsToolBar(Document document, ANode aNode) {
        this.document = document;
        this.aNode = aNode;

        document.getWorkflow().getTopologyChanged().addListener((c, o, n) -> update());
    }

    /**
     * update the bread crumbs
     */
    public void update() {
        final Workflow workflow = document.getWorkflow();
        getItems().clear();
        if (workflow.getTaxaFilter() != null) {
            getItems().add(makeBreadCrumb(document, workflow.getTaxaFilter()));
        }
        if (workflow.getTopFilter() != null && workflow.getTopFilter().getChild() != null) {
            final ArrayList<AConnector> connectors = new ArrayList<>();
            findConnectorsAlongPathRec(workflow.getTopFilter().getChild(), aNode, connectors);
            for (AConnector connector : connectors) {
                getItems().add(makeBreadCrumb(document, connector));
            }
        }
    }

    /**
     * collects all connectors on the path to the target node
     *
     * @param aNode
     * @param targetNode
     * @param connectors
     * @return path of connectors
     */
    private boolean findConnectorsAlongPathRec(ANode aNode, ANode targetNode, ArrayList<AConnector> connectors) {
        if (aNode instanceof AConnector) {
            connectors.add((AConnector) aNode);
        }
        if (aNode == targetNode)
            return true;
        for (ANode child : aNode.getChildren()) {
            if (findConnectorsAlongPathRec(child, targetNode, connectors))
                return true;
        }
        if (aNode instanceof AConnector) {
            connectors.remove(aNode);
        }
        return false;
    }

    private Node makeBreadCrumb(Document document, AConnector aConnector) {
        final Button button = new Button();
        button.setStyle("-fx-shape: \"M 0 0 L 0 18 L 100 18 L 110 9 L 100 0 z\";"); // arrow shape
        button.textProperty().bind(aConnector.nameProperty());
        button.disableProperty().bind(aConnector.applicableProperty().not().and(aConnector.stateProperty().isEqualTo(UpdateState.VALID).not()));
        button.setOnAction((e) -> {
            final Point2D location = button.localToScreen(button.getLayoutX(), button.getLayoutY());
            ANodeViewManager.getInstance().show(document, aConnector, location.getX(), location.getY());
        });
        return button;
    }
}
