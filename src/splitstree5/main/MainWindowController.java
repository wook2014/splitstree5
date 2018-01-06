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
package splitstree5.main;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import splitstree5.main.workflowtree.WorkflowTreeItem;

import java.util.LinkedList;
import java.util.Queue;

public class MainWindowController {
    @FXML
    private BorderPane borderPane;

    @FXML
    private VBox topVBox;

    @FXML
    private ToolBar topToolBar;

    @FXML
    private Button openCloseTreeView;

    @FXML
    private SplitPane splitPane;

    @FXML
    private TreeView<String> treeView;

    @FXML
    private TabPane tabPane;

    @FXML
    private ToolBar bottomToolBar;

    @FXML
    private Button collapseAllButton;

    @FXML
    private Button expandAllButton;

    @FXML
    private Button showButton;

    public BorderPane getBorderPane() {
        return borderPane;
    }

    public VBox getTopVBox() {
        return topVBox;
    }


    public TabPane getTabPane() {
        return tabPane;
    }

    public ToolBar getTopToolBar() {
        return topToolBar;
    }

    public SplitPane getSplitPane() {
        return splitPane;
    }

    public TreeView<String> getTreeView() {
        return treeView;
    }

    public ToolBar getBottomToolBar() {
        return bottomToolBar;
    }


    @FXML
    void initialize() {
        // some specific toolbar buttons:
        collapseAllButton.setOnAction((e) -> treeView.getRoot().setExpanded(false));
        expandAllButton.setOnAction((e) -> {
            final Queue<TreeItem> queue = new LinkedList<>();
            queue.add(treeView.getRoot());
            while (queue.size() > 0) {
                final TreeItem item = queue.poll();
                item.setExpanded(true);
                queue.addAll(item.getChildren());
            }
        });

        showButton.setOnAction((e) -> {
            for (TreeItem item : treeView.getSelectionModel().getSelectedItems()) {
                if (item instanceof WorkflowTreeItem) {
                    final Point2D point2D = item.getGraphic().localToScreen(item.getGraphic().getLayoutX(), item.getGraphic().getLayoutY());
                    ((WorkflowTreeItem) item).showView(point2D.getX(), point2D.getY());
                }
            }
        });
        showButton.disableProperty().bind(Bindings.isEmpty(treeView.getSelectionModel().getSelectedItems()));

        openCloseTreeView.setOnAction((e) -> {
            if (splitPane.getDividerPositions()[0] <= 0.01) {
                System.err.println(treeView.getPrefWidth());
                splitPane.setDividerPositions(300 / splitPane.getWidth());
                openCloseTreeView.setText(("<"));
            } else {
                splitPane.setDividerPositions(0);
                openCloseTreeView.setText((">"));
            }
        });
    }
}
