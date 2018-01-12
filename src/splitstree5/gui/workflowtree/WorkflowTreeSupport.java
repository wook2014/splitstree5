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

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import splitstree5.core.Document;
import splitstree5.core.workflow.ANode;
import splitstree5.core.workflow.Workflow;

import java.util.LinkedList;
import java.util.Queue;


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
            treeView.getRoot().getChildren().clear();
            WorkflowTreeItem topTaxaItem = new WorkflowTreeItem(document, workflow.getTopTaxaNode());
            treeView.getRoot().getChildren().add(topTaxaItem);
            WorkflowTreeItem taxaFilterItem = new WorkflowTreeItem(document, workflow.getTaxaFilter());
            topTaxaItem.getChildren().add(taxaFilterItem);
            WorkflowTreeItem workingTaxaItem = new WorkflowTreeItem(document, workflow.getWorkingTaxaNode());
            topTaxaItem.getChildren().add(workingTaxaItem);

            if (workflow.getTopDataNode() != null) {
                WorkflowTreeItem topDataItem = new WorkflowTreeItem(document, workflow.getTopDataNode());
                treeView.getRoot().getChildren().add(topDataItem);
                addToTreeRec(document, topDataItem, workflow.getTopDataNode());
            }
            if (treeView.getRoot() != null) {
                final Queue<TreeItem> queue = new LinkedList<>();
                queue.add(treeView.getRoot());
                while (queue.size() > 0) {
                    final TreeItem item = queue.poll();
                    item.setExpanded(true);
                    queue.addAll(item.getChildren());
                }
            }
        });

    }

    /**
     * recursively set up the tree
     *
     * @param document
     * @param treeItem
     * @param aNode
     */
    private void addToTreeRec(final Document document, final WorkflowTreeItem treeItem, final ANode aNode) {
        for (ANode child : aNode.getChildren()) {
            WorkflowTreeItem childItem = new WorkflowTreeItem(document, child);
            treeItem.getChildren().add(childItem);
            if (child.getChildren().size() == 1 && aNode.getChildren().size() == 1)
                addToTreeRec(document, treeItem, child);
            else
                addToTreeRec(document, childItem, child);

        }
    }
}
