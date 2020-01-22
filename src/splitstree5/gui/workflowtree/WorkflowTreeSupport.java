/*
 *  WorkflowTreeSupport.java Copyright (C) 2020 Daniel H. Huson
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

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import splitstree5.core.Document;
import splitstree5.core.workflow.Workflow;
import splitstree5.core.workflow.WorkflowNode;


public class WorkflowTreeSupport {
    /**
     * constructor sets up tree view support
     *
     * @param treeView
     * @param document
     */
    public WorkflowTreeSupport(TreeView<String> treeView, Document document) {
        final Workflow workflow = document.getWorkflow();

        treeView.setOnMouseEntered((e) -> treeView.requestFocus());
        treeView.setOnMouseExited((e) -> document.getMainWindow().getMainWindowController().getMainTabPane().requestFocus());

        workflow.getTopologyChanged().addListener((c, o, n) -> {
            Platform.runLater(() -> {
                treeView.getRoot().getChildren().clear();
                treeView.getRoot().setExpanded(true);

                if (workflow.getTopTaxaNode() != null) {
                    WorkflowTreeItem topTaxaItem = new WorkflowTreeItem(document, workflow.getTopTaxaNode());
                    treeView.getRoot().getChildren().add(topTaxaItem);
                    if (workflow.getTopTraitsNode() != null)
                        topTaxaItem.getChildren().add(new WorkflowTreeItem(document, workflow.getTopTraitsNode()));

                    if (workflow.getTopDataNode() != null) {
                        final WorkflowTreeItem topDataItem = new WorkflowTreeItem(document, workflow.getTopDataNode());
                        topTaxaItem.getChildren().add(topDataItem);
                    }

                    WorkflowTreeItem taxaFilterItem = new WorkflowTreeItem(document, workflow.getTaxaFilter());
                    topTaxaItem.getChildren().add(taxaFilterItem);

                    WorkflowTreeItem workingTaxaItem = new WorkflowTreeItem(document, workflow.getWorkingTaxaNode());
                    topTaxaItem.getChildren().add(workingTaxaItem);

                    if (workflow.getWorkingTraitsNode() != null)
                        topTaxaItem.getChildren().add(new WorkflowTreeItem(document, workflow.getWorkingTraitsNode()));

                    if (workflow.getWorkingDataNode() != null) {
                        final WorkflowTreeItem workingDataItem = new WorkflowTreeItem(document, workflow.getWorkingDataNode());
                        treeView.getRoot().getChildren().add(workingDataItem);
                        workingDataItem.setExpanded(true);
                        addToTreeRec(document, workingDataItem, workflow.getWorkingDataNode());
                    }
                }
            });
        });

    }

    /**
     * recursively set up the tree
     *
     * @param document
     * @param treeItem
     * @param workflowNode
     */
    private void addToTreeRec(final Document document, final TreeItem<String> treeItem, final WorkflowNode workflowNode) {
        for (WorkflowNode child : workflowNode.getChildren()) {
            TreeItem<String> childItem = new WorkflowTreeItem(document, child);
            childItem.setExpanded(true);
            treeItem.getChildren().add(childItem);
            if (child.getChildren().size() == 1 && workflowNode.getChildren().size() == 1)
                addToTreeRec(document, treeItem, child);
            else
                addToTreeRec(document, childItem, child);
        }
    }
}
