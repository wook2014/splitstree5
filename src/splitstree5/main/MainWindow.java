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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import jloda.fx.ExtendedFXMLLoader;
import jloda.util.Basic;
import jloda.util.ProgramProperties;
import splitstree5.core.Document;
import splitstree5.core.misc.ProgramExecutorService;
import splitstree5.gui.workflowtree.WorkFlowTreeViewSupport;
import splitstree5.main.methodstab.MethodsViewTab;
import splitstree5.main.workflowtab.WorkflowViewTab;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class MainWindow {
    public final static ObservableList<MainWindow> openWindows = FXCollections.observableArrayList();
    private Document document;

    private final Parent root;
    private final MainWindowController controller;

    private final WorkFlowTreeViewSupport workFlowTreeViewSupport;

    private final TabPane tabPane;

    private Stage stage;

    private final StringProperty titleProperty = new SimpleStringProperty("Untitled");

    private boolean allowClose = false;

    /**
     * constructor
     *
     * @throws IOException
     */
    public MainWindow() throws IOException {
        this.document = new Document();
        document.setMainWindow(this);

        Platform.setImplicitExit(false);

        final ExtendedFXMLLoader<MainWindowController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        root = extendedFXMLLoader.getRoot();
        controller = extendedFXMLLoader.getController();
        if (ProgramProperties.isMacOS()) {
            controller.getMenuBar().setUseSystemMenuBar(true);
        }

        tabPane = controller.getTabPane();

        tabPane.getSelectionModel().selectedItemProperty().addListener((c, o, n) -> {
            if (o instanceof ISavesPreviousSelection) {
                ((ISavesPreviousSelection) o).saveAsPreviousSelection();
            }
            updateMenus(n, controller);
        });

        final TreeItem<String> rootItem = new TreeItem<>();

        document.fileNameProperty().addListener((e) -> {
            final String name;
            if (document.getFileName() == null || document.getFileName().length() == 0)
                name = "Untitled";
            else
                name = (new File(document.getFileName())).getName();
            titleProperty.setValue("Main Window - " + name + " - SplitsTree5");
            rootItem.setValue(name);
        });

        controller.getTreeView().setRoot(rootItem);
        workFlowTreeViewSupport = new WorkFlowTreeViewSupport(controller.getTreeView(), document);
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
        }

        stage.titleProperty().bind(titleProperty);
        stage.setScene(new Scene(root, 800, 600));

        stage.setX(screenX);
        stage.setY(screenY);

        new TabPaneDragAndDropSupport(controller.getTabPane());

        stage.show();
        stage.sizeToScene();
        stage.toFront();

        if (document.getWorkflow() != null) {
            add(new MethodsViewTab(document));
            add(new WorkflowViewTab(document));
        }

        controller.getSplitPane().widthProperty().addListener((c, o, n) -> {
            if (n.doubleValue() > 0) {
                double oldPos = controller.getSplitPane().getDividerPositions()[0];
                double oldWidth = oldPos * o.doubleValue();
                double newPos = oldWidth / n.doubleValue();
                controller.getSplitPane().setDividerPositions(newPos);
            }
        });

        openWindows.add(this);
    }

    /**
     * get the document
     *
     * @return document
     */
    public Document getDocument() {
        return document;
    }

    public MainWindowController getController() {
        return controller;
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
        controller.getTabPane().getTabs().add(0, viewerTab);
        controller.getTabPane().getSelectionModel().select(0);
    }

    /**
     * remove a tab
     *
     * @param viewerTab
     */
    public void remove(Tab viewerTab) {
        controller.getTabPane().getTabs().remove(viewerTab);
    }

    /**
     * closes the current window. If it is the last to close, will ask for confirmation and then quit
     */
    public void close() {
        boolean openNewDocument = false;
        if (openWindows.size() == 1) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(this.getStage());
            alert.setTitle("SplitsTree5 - Confirm Quit");
            alert.setHeaderText("Closing the last open document");
            alert.setContentText("Do you really want to quit?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.CANCEL)
                openNewDocument = true;
        }
        openWindows.remove(this);
        if (openNewDocument) {
            try {
                MainWindow mainWindow = new MainWindow();
                mainWindow.show(null, this.getStage().getX(), this.getStage().getY());
            } catch (IOException e) {
                Basic.caught(e);
            }
        }
        this.getStage().close();
        if (openWindows.size() == 0) {
            ProgramExecutorService.getExecutorService().shutdownNow();
            Platform.exit();
        }
    }

    /**
     * update the menus
     *
     * @param tab
     * @param controller
     */
    private void updateMenus(Tab tab, MainWindowController controller) {
        controller.unbindAndDisableAllMenuItems();
        MainWindowMenuController.setupMainMenus(this);
        if (tab instanceof ViewerTab) {
            final ViewerTab viewer = (ViewerTab) tab;
            viewer.updateMenus(controller);
        }
        controller.enableAllUnboundActionMenuItems();
    }

}
