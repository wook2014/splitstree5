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

package splitstree5.gui.auxwindow;

import com.sun.javafx.scene.control.behavior.TabPaneBehavior;
import com.sun.javafx.scene.control.skin.TabPaneSkin;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import splitstree5.gui.ISavesPreviousSelection;

/**
 * drag and drop support
 * Daniel Huson, 12.2017
 */
public class TabPaneDragAndDropSupport {
    private final TabPane mainTabPane;
    private Image transferImage;
    private Tab currentDraggingTab;
    private final IStageSupplier stageSupplier;

    /**
     * implements drag and drop support
     */
    public TabPaneDragAndDropSupport(TabPane mainTabPane, IStageSupplier stageSupplier) {
        this.mainTabPane = mainTabPane;
        this.stageSupplier = stageSupplier;

        mainTabPane.getTabs().forEach(this::addDragHandlers);
        mainTabPane.getTabs().addListener((Change<? extends Tab> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(this::addDragHandlers);
                }
                if (c.wasRemoved()) {
                    c.getRemoved().forEach(this::removeDragHandlers);
                }
            }
        });

        // if we drag onto a tab pane (but not onto the tab graphic), add the tab to the end of the list of tabs:
        mainTabPane.setOnDragOver(e -> {
            if (matches(e) && currentDraggingTab != null && currentDraggingTab.getTabPane() != mainTabPane) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
        });
        mainTabPane.setOnDragDropped(e -> {
            if (matches(e) && currentDraggingTab != null && currentDraggingTab.getTabPane() != mainTabPane) {
                currentDraggingTab.getTabPane().getTabs().remove(currentDraggingTab);
                mainTabPane.getTabs().add(currentDraggingTab);
                currentDraggingTab.getTabPane().getSelectionModel().select(currentDraggingTab);
                currentDraggingTab = null;
            }
        });
    }

    /**
     * add drag handles to tab
     */
    private void addDragHandlers(Tab tab) {
        if (tab.getGraphic() == null) {
            Label label = new Label(tab.getText(), tab.getGraphic());
            tab.setText(null);
            tab.setGraphic(label);
        }
        final Node graphic = tab.getGraphic();

        if (graphic != null) {
            tab.getGraphic().setOnContextMenuRequested((e) -> {
                ContextMenu contextMenu = new ContextMenu();
                final MenuItem undockItem = new MenuItem("Undock");
                undockItem.setOnAction((x) -> {
                    mainTabPane.getTabs().remove(tab);
                    final Stage newStage = createAuxiliaryWindow(tab, mainTabPane.getWidth(), mainTabPane.getHeight());
                    newStage.setX(mainTabPane.localToScreen(0, 0).getX());
                    newStage.setY(mainTabPane.localToScreen(0, 0).getY());
                    newStage.sizeToScene();
                    newStage.show();
                });
                undockItem.setDisable(tab.getTabPane() != mainTabPane);
                contextMenu.getItems().add(undockItem);
                final MenuItem redockItem = new MenuItem("Redock");
                redockItem.setOnAction((x) -> {
                    tab.getTabPane().getTabs().remove(tab);
                    mainTabPane.getTabs().add(tab);
                    mainTabPane.getSelectionModel().select(mainTabPane.getTabs().size() - 1);

                });
                redockItem.setDisable(tab.getTabPane() == mainTabPane);
                contextMenu.getItems().add(redockItem);

                contextMenu.getItems().add(new SeparatorMenuItem());
                final MenuItem closeItem = new MenuItem("Close");
                closeItem.setOnAction((x) -> {
                    final TabPane theTabPane = tab.getTabPane();

                    final TabPaneBehavior behavior = ((TabPaneSkin) theTabPane.getSkin()).getBehavior();
                    if (behavior.canCloseTab(tab)) {
                        behavior.closeTab(tab);
                    } else
                        theTabPane.getTabs().remove(tab);

                    if (theTabPane != mainTabPane) { // is in auxiliary window
                        if (theTabPane.getTabs().size() == 0)
                            ((Stage) theTabPane.getScene().getWindow()).close();
                    }
                });
                closeItem.setDisable(!tab.isClosable());
                contextMenu.getItems().add(closeItem);

                if (contextMenu.getItems().size() > 0)
                    contextMenu.show(tab.getTabPane(), e.getScreenX(), e.getScreenY());
            });

            graphic.setOnDragDetected(e -> {
                final Dragboard dragboard = graphic.startDragAndDrop(TransferMode.MOVE);
                final ClipboardContent content = new ClipboardContent();
                transferImage = graphic.snapshot(null, null);
                content.putImage(transferImage);
                dragboard.setContent(content);
                dragboard.setDragView(transferImage);
                currentDraggingTab = tab;
            });

            graphic.setOnDragOver(e -> {
                if (matches(e) && currentDraggingTab != null && currentDraggingTab.getGraphic() != graphic) {
                    e.acceptTransferModes(TransferMode.MOVE);
                }
            });

            graphic.setOnDragDropped(e -> {
                if (matches(e) && currentDraggingTab != null && currentDraggingTab.getGraphic() != graphic) {
                    final int index = tab.getTabPane().getTabs().indexOf(tab);
                    currentDraggingTab.getTabPane().getTabs().remove(currentDraggingTab);
                    tab.getTabPane().getTabs().add(index, currentDraggingTab);
                    currentDraggingTab.getTabPane().getSelectionModel().select(currentDraggingTab);
                    currentDraggingTab = null;
                }
            });
        }
    }

    /**
     * remove drag-and-drop handlers
     */
    private void removeDragHandlers(Tab tab) {
        tab.getGraphic().setOnDragDetected(null);
        tab.getGraphic().setOnDragOver(null);
        tab.getGraphic().setOnDragDropped(null);
        tab.getGraphic().setOnDragDone(null);
    }

    /**
     * create an auxiliary window to hold undocked tab in
     */
    private Stage createAuxiliaryWindow(final Tab tab, double width, double height) {
        tab.setClosable(false);

        final Stage stage = stageSupplier.supplyStage(tab, width, height);

        stage.focusedProperty().addListener((c, o, n) -> {
            if (!n && tab instanceof ISavesPreviousSelection)
                ((ISavesPreviousSelection) tab).saveAsPreviousSelection();
        });

        final TabPane newTabPane = tab.getTabPane();
        addDragHandlers(tab);

        newTabPane.getTabs().addListener((InvalidationListener) (c) -> {
            if (newTabPane.getTabs().size() == 0) {
                stage.close();
                removeDragHandlers(tab);
            }
        });

        newTabPane.setOnDragOver(e -> {
            if (matches(e) && currentDraggingTab != null && currentDraggingTab != tab) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
        });

        newTabPane.setOnDragDropped(e -> {
            if (matches(e) && currentDraggingTab != null && currentDraggingTab != tab) {
                currentDraggingTab.getTabPane().getTabs().remove(currentDraggingTab);
                newTabPane.getTabs().add(currentDraggingTab);
                newTabPane.getSelectionModel().select(currentDraggingTab);
                addDragHandlers(currentDraggingTab);
                e.setDropCompleted(true);
                currentDraggingTab = null;
            }
        });

        if ((stage.getTitle() == null || stage.getTitle().length() == 0) && tab.getGraphic() instanceof Labeled)
            stage.setTitle(((Labeled) tab.getGraphic()).getText());

        stage.setOnCloseRequest((e) -> {
            while (newTabPane.getTabs().size() > 0) {
                final Tab a = newTabPane.getTabs().remove(0);
                mainTabPane.getTabs().add(a);
                if (newTabPane.getTabs().size() == 0) // select the last one
                {
                    mainTabPane.getSelectionModel().select(mainTabPane.getTabs().size() - 1);
                    stageSupplier.closedStage(stage, a);
                }
            }
        });
        stageSupplier.openedStage(stage, tab);
        return stage;
    }

    /**
     * does the dragged tab belong to us?
     */
    private boolean matches(DragEvent e) {
        Image image = e.getDragboard().getImage();
        return image != null && transferImage != null && image.getWidth() == transferImage.getWidth() && image.getHeight() == transferImage.getHeight();
    }
}
