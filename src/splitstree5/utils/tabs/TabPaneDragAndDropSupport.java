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

package splitstree5.utils.tabs;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener.Change;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import jloda.util.MouseLocator;

/**
 * drag and drop support
 * Daniel Huson, 12.2017
 */
public class TabPaneDragAndDropSupport {
    private final TabPane tabPane;
    private final BooleanProperty allowDragTearOff = new SimpleBooleanProperty();
    private Image transferImage;
    private Tab currentDraggingTab;

    /**
     * adds drag and drop support
     *
     * @param tabPane
     */
    public TabPaneDragAndDropSupport(Stage stage, TabPane tabPane) {
        this.tabPane = tabPane;

        tabPane.getTabs().forEach(this::addDragHandlers);
        tabPane.getTabs().addListener((Change<? extends Tab> c) -> {
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
        tabPane.setOnDragOver(e -> {
            if (matches(e) && currentDraggingTab != null && currentDraggingTab.getTabPane() != tabPane) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
        });
        tabPane.setOnDragDropped(e -> {
            if (matches(e) && currentDraggingTab != null && currentDraggingTab.getTabPane() != tabPane) {
                currentDraggingTab.getTabPane().getTabs().remove(currentDraggingTab);
                tabPane.getTabs().add(currentDraggingTab);
                currentDraggingTab.getTabPane().getSelectionModel().select(currentDraggingTab);
                currentDraggingTab = null;
            }
        });


        tabPane.setOnDragDone((e) -> {
            if (allowDragTearOff.get() && matches(e) && currentDraggingTab != null && currentDraggingTab.getTabPane() == tabPane) {

                // if mouse is still inside stage, don't create new window
                Point2D mouseOnScreen = MouseLocator.getMouseOnScreen();
                Rectangle rectangle = new Rectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
                if (!rectangle.contains(mouseOnScreen)) {
                    currentDraggingTab.getTabPane().getTabs().remove(currentDraggingTab);
                    final Stage newStage = tearOff(currentDraggingTab);
                    newStage.setX(mouseOnScreen.getX());
                    newStage.setY(mouseOnScreen.getX());
                    newStage.show();
                }
            }
        });
    }

    public boolean getAllowDragTearOff() {
        return allowDragTearOff.get();
    }

    public BooleanProperty allowDragTearOffProperty() {
        return allowDragTearOff;
    }

    public void setAllowDragTearOff(boolean allowDragTearOff) {
        this.allowDragTearOff.set(allowDragTearOff);
    }

    private void addDragHandlers(Tab tab) {
        if (tab.getText() != null && !tab.getText().isEmpty()) {
            Label label = new Label(tab.getText(), tab.getGraphic());
            tab.setText(null);
            tab.setGraphic(label);
        }

        final Node graphic = tab.getGraphic();

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

    private void removeDragHandlers(Tab tab) {
        tab.getGraphic().setOnDragDetected(null);
        tab.getGraphic().setOnDragOver(null);
        tab.getGraphic().setOnDragDropped(null);
        tab.getGraphic().setOnDragDone(null);
    }

    /**
     * Tear off a tab into a new window
     *
     * @param tab
     * @return open in new window
     */
    public Stage tearOff(final Tab tab) {
        final Stage stage = new Stage();

        final TabPane newTabPane = new TabPane(tab);
        addDragHandlers(tab);

        Bindings.isEmpty(newTabPane.getTabs()).addListener((c) -> {
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
                {
                    final Tab tab1 = currentDraggingTab;
                    tab1.getTabPane().getTabs().remove(tab1);
                    newTabPane.getTabs().add(tab1);
                    newTabPane.getSelectionModel().select(tab1);
                    e.setDropCompleted(true);
                    currentDraggingTab = null;
                }
            }
        });

        stage.setScene(new Scene(newTabPane, 300, 300));
        if (tab.getGraphic() instanceof Labeled)
            stage.setTitle(((Labeled) tab.getGraphic()).getText());

        stage.sizeToScene();

        stage.setOnCloseRequest((e) -> {
            tabPane.getTabs().add(tab);
        });

        tab.setClosable(false);

        return stage;

    }

    private boolean matches(DragEvent e) {
        Image image = e.getDragboard().getImage();
        return image != null && transferImage != null && image.getWidth() == transferImage.getWidth() && image.getHeight() == transferImage.getHeight();
    }

}
