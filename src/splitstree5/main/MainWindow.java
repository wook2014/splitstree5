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

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import jloda.fx.ExtendedFXMLLoader;
import jloda.util.Basic;
import jloda.util.ResourceManager;
import splitstree5.core.Document;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.misc.ProgramExecutorService;
import splitstree5.core.project.ProjectManager;
import splitstree5.main.auxwindow.AuxWindow;
import splitstree5.main.auxwindow.TabPaneDragAndDropSupport;
import splitstree5.main.datatab.DataViewTab;
import splitstree5.main.methodstab.MethodsViewTab;
import splitstree5.main.workflowtab.WorkflowViewTab;
import splitstree5.main.workflowtree.WorkflowTreeSupport;
import splitstree5.menu.MenuController;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Optional;

public class MainWindow {
    private Document document;

    private final Parent root;
    private final MainWindowController mainWindowController;
    private final MenuController menuController;

    private final WorkflowTreeSupport workflowTreeSupport;

    private final TabPane tabPane;

    private Stage stage;

    private final StringProperty titleProperty = new SimpleStringProperty("Untitled");

    private boolean allowClose = false;

    private final ObservableMap<ADataNode, ViewerTab> dataNode2ViewerTab;

    /**
     * constructor
     *
     * @throws IOException
     */
    public MainWindow() throws IOException {
        this.document = new Document();
        document.setMainWindow(this);
        dataNode2ViewerTab = FXCollections.observableHashMap();

        Platform.setImplicitExit(false);

        {
            final ExtendedFXMLLoader<MainWindowController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
            root = extendedFXMLLoader.getRoot();
            root.getStylesheets().add("resources/css/styles.css");
            mainWindowController = extendedFXMLLoader.getController();
        }
        {
            final ExtendedFXMLLoader<MenuController> extendedFXMLLoader = new ExtendedFXMLLoader<>(MenuController.class);
            menuController = extendedFXMLLoader.getController();
        }
        mainWindowController.getTopVBox().getChildren().add(0, menuController.getMenuBar());

        tabPane = mainWindowController.getTabPane();

        tabPane.getSelectionModel().selectedItemProperty().addListener((c, o, n) -> {
            if (o instanceof ISavesPreviousSelection) {
                ((ISavesPreviousSelection) o).saveAsPreviousSelection();
            }
            updateMenus(n, menuController);
        });

        final TreeItem<String> rootItem = new TreeItem<>();

        titleProperty.bind(Bindings.concat("Main Window - ").concat(document.nameProperty()).concat(" - SplitsTree5"));

        mainWindowController.getTreeView().setRoot(rootItem);
        workflowTreeSupport = new WorkflowTreeSupport(mainWindowController.getTreeView(), document);
    }

    /**
     * show this view
     */
    public void show(Stage stage0, double screenX, double screenY) {
        if (stage != null)
            this.stage = stage0;
        else {
            this.stage = new Stage();
            stage.focusedProperty().addListener((c, o, n) -> {
                if (!n && tabPane.getSelectionModel().getSelectedItem() instanceof ISavesPreviousSelection)
                    ((ISavesPreviousSelection) tabPane.getSelectionModel().getSelectedItem()).saveAsPreviousSelection();
            });
            stage.getIcons().setAll(ResourceManager.getIcon("SplitsTree5-16.png"));
        }

        stage.titleProperty().bind(titleProperty);
        stage.setScene(new Scene(root, 800, 600));

        stage.setX(screenX);
        stage.setY(screenY);

        new TabPaneDragAndDropSupport(mainWindowController.getTabPane(), new AuxWindow());

        stage.show();
        stage.sizeToScene();
        stage.toFront();

        if (document.getWorkflow() != null) {
            add(new MethodsViewTab(document));
            add(new WorkflowViewTab(document));
        }

        mainWindowController.getSplitPane().widthProperty().addListener((c, o, n) -> {
            if (n.doubleValue() > 0) {
                double oldPos = mainWindowController.getSplitPane().getDividerPositions()[0];
                double oldWidth = oldPos * o.doubleValue();
                double newPos = oldWidth / n.doubleValue();
                mainWindowController.getSplitPane().setDividerPositions(newPos);
            }
        });

        ProjectManager.getInstance().addMainWindow(this);
    }

    /**
     * get the document
     *
     * @return document
     */
    public Document getDocument() {
        return document;
    }

    public MainWindowController getMainWindowController() {
        return mainWindowController;
    }

    public MenuController getMenuController() {
        return menuController;
    }

    public Stage getStage() {
        return stage;
    }

    boolean isAllowClose() {
        return allowClose;
    }

    void setAllowClose() {
        allowClose = true;
    }

    /**
     * add a viewer tab
     *
     * @param viewerTab
     */
    public void add(ViewerTab viewerTab) {
        mainWindowController.getTabPane().getTabs().add(0, viewerTab);
        mainWindowController.getTabPane().getSelectionModel().select(0);
        viewerTab.setMainWindow(this);
    }

    /**
     * remove a tab
     *
     * @param viewerTab
     */
    public void remove(Tab viewerTab) {
        mainWindowController.getTabPane().getTabs().remove(viewerTab);
    }

    /**
     * closes the current window. If it is the last to close, will ask for confirmation and then quit
     *
     * @return true if closed, false if canceled
     */
    public boolean close() {
        boolean openNewDocument = false;

        if (ProjectManager.getInstance().size() == 1) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(this.getStage());
            alert.setTitle("SplitsTree5 - Confirm Quit");
            alert.setHeaderText("Closing the last open document");
            alert.setContentText("Do you really want to quit?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.CANCEL)
                openNewDocument = true;
        }
        ProjectManager.getInstance().removeMainWindow(this);
        if (openNewDocument) {
            try {
                MainWindow mainWindow = new MainWindow();
                mainWindow.show(null, this.getStage().getX(), this.getStage().getY());
            } catch (IOException e) {
                Basic.caught(e);
            }
        }
        this.getStage().close();
        if (ProjectManager.getInstance().size() == 0) {
            ProgramExecutorService.getExecutorService().shutdownNow();
            Platform.exit();
        }
        return !openNewDocument;
    }

    /**
     * update the menus
     *
     * @param tab
     * @param controller
     */
    private void updateMenus(Tab tab, MenuController controller) {
        controller.unbindAndDisableAllMenuItems();
        MainWindowMenuController.setupMainMenus(this);
        if (tab instanceof ViewerTab) {
            final ViewerTab viewer = (ViewerTab) tab;
            viewer.updateMenus(controller);
        }
        controller.enableAllUnboundActionMenuItems();
    }

    /**
     * show a data view node
     *
     * @param aNode
     */
    public void showDataView(ADataNode aNode) {
        // if the data block as a getTab method, then assume that it is present and select it
        try {
            Method method = aNode.getDataBlock().getClass().getMethod("getTab");
            if (method != null) {
                Tab tab = (Tab) method.invoke(aNode.getDataBlock());
                if (tab != null)
                    tab.getTabPane().getSelectionModel().select(tab);
                return;
            }
        } catch (Exception ex) {
            // doesn't matter
        }

        ViewerTab viewerTab = dataNode2ViewerTab.get(aNode);
        if (viewerTab == null || viewerTab.getTabPane() == null) {
            viewerTab = new DataViewTab(document, aNode);
            dataNode2ViewerTab.put(aNode, viewerTab);
            tabPane.getTabs().add(0, viewerTab);
        }
        final Stage stage = (Stage) viewerTab.getTabPane().getScene().getWindow();
        if (stage != null) {
            stage.toFront();
        }
        tabPane.getSelectionModel().select(viewerTab);
    }
}
