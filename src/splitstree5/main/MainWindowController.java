/*
 *  Copyright (C) 2019 Daniel H. Huson
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

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import jloda.fx.control.SplittableTabPane;
import jloda.fx.util.MemoryUsage;
import splitstree5.gui.workflowtree.WorkflowTreeItem;

import java.util.LinkedList;
import java.util.Queue;

public class MainWindowController {
    @FXML
    private BorderPane borderPane;

    @FXML
    private VBox topVBox;

    @FXML
    private VBox rightVBox;

    @FXML
    private ToolBar topToolBar;

    @FXML
    private ToolBar leftToolBar;

    @FXML
    private Button openCloseRight;

    @FXML
    private SplitPane splitPane;

    @FXML
    private TreeView<String> treeView;

    @FXML
    private SplitPane algorithmSplitPane;

    @FXML
    private ToolBar algorithmToolBar;

    @FXML
    private FlowPane progressBarPane;

    @FXML
    private Button collapseAllButton;

    @FXML
    private Button expandAllButton;

    @FXML
    private Button showButton;

    @FXML
    private Label memoryUsageLabel;

    private Button openCloseLeft;

    public BorderPane getBorderPane() {
        return borderPane;
    }

    public VBox getTopVBox() {
        return topVBox;
    }


    public SplittableTabPane getMainTabPane() {
        return mainTabPane;
    }

    public SplittableTabPane getAlgorithmTabPane() {
        return algorithmTabPane;
    }

    public ToolBar getTopToolBar() {
        return topToolBar;
    }


    public SplitPane getSplitPane() {
        return splitPane;
    }

    public SplitPane getAlgorithmSplitPane() {
        return algorithmSplitPane;
    }

    public TreeView<String> getTreeView() {
        return treeView;
    }

    public FlowPane getBottomPane() {
        return progressBarPane;
    }

    public VBox getRightVBox() {
        return rightVBox;
    }

    // we override the tab panes provided by the FXML file:

    private final SplittableTabPane mainTabPane = new SplittableTabPane();

    private final SplittableTabPane algorithmTabPane = new SplittableTabPane();

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
                    ((WorkflowTreeItem) item).showView();
                }
            }
        });
        showButton.disableProperty().bind(Bindings.isEmpty(treeView.getSelectionModel().getSelectedItems()));

        openCloseRight.setOnAction((e) -> {
            ensureTreeViewIsOpen();
            if (algorithmSplitPane.getDividerPositions()[0] >= 0.99)
                animateSplitPane(algorithmSplitPane, (algorithmSplitPane.getHeight() - 300) / algorithmSplitPane.getHeight(), () -> openCloseRight.setText(("<")), true);
            else
                animateSplitPane(algorithmSplitPane, 1.0, () -> openCloseRight.setText((">")), true);
        });

        final MemoryUsage memoryUsage = MemoryUsage.getInstance();
        memoryUsageLabel.textProperty().bind(memoryUsage.memoryUsageStringProperty());

        getSplitPane().getItems().add(getMainTabPane());
        getRightVBox().getChildren().add(getAlgorithmTabPane());
        getAlgorithmTabPane().prefHeightProperty().bind(getRightVBox().heightProperty().subtract(30));
        getAlgorithmTabPane().prefWidthProperty().bind(getRightVBox().widthProperty());

    }


    public void openCloseLeft(boolean animate) {
        if (splitPane.getDividerPositions()[0] <= 0.01)
            animateSplitPane(splitPane, 300 / splitPane.getWidth(), () -> openCloseLeft.setText(("<")), animate);
        else
            animateSplitPane(splitPane, 0, () -> openCloseLeft.setText((">")), animate);
    }

    private void animateSplitPane(SplitPane splitPane, double target, Runnable runnable, boolean animate) {
        KeyValue keyValue = new KeyValue(splitPane.getDividers().get(0).positionProperty(), target);
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(animate ? 500 : 1), keyValue));
        timeline.play();
        timeline.setOnFinished((x) -> runnable.run());
    }

    public void ensureTreeViewIsOpen() {
        if (splitPane.getDividerPositions()[0] <= 0.01)
            animateSplitPane(splitPane, 300 / splitPane.getWidth(), () -> openCloseLeft.setText(("<")), true);

    }

    public void ensureAlgorithmsTabPaneIsOpen() {
        ensureTreeViewIsOpen();
        if (algorithmSplitPane.getDividerPositions()[0] >= (algorithmSplitPane.getHeight() - 300) / algorithmSplitPane.getHeight())
            animateSplitPane(algorithmSplitPane, (algorithmSplitPane.getHeight() - 300) / algorithmSplitPane.getHeight(), () -> openCloseRight.setText(("<")), true);
    }

    public void setupOpenCloseLeft(Button openCloseLeft) {
        this.openCloseLeft = openCloseLeft;

        openCloseLeft.setOnAction((e) -> {
            if (splitPane.getDividerPositions()[0] <= 0.01)
                animateSplitPane(splitPane, 300 / splitPane.getWidth(), () -> openCloseLeft.setText(("<")), true);
            else
                animateSplitPane(splitPane, 0, () -> openCloseLeft.setText((">")), true);
        });
    }
}
