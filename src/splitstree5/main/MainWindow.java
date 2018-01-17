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
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import jloda.fx.ExtendedFXMLLoader;
import jloda.util.ResourceManager;
import splitstree5.core.Document;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.workflow.ANode;
import splitstree5.dialogs.SaveBeforeClose;
import splitstree5.gui.ISavesPreviousSelection;
import splitstree5.gui.ViewerTab;
import splitstree5.gui.algorithmtab.AlgorithmTab;
import splitstree5.gui.auxwindow.AuxWindow;
import splitstree5.gui.auxwindow.TabPaneDragAndDropSupport;
import splitstree5.gui.datatab.DataViewTab;
import splitstree5.gui.formattab.FormatTab;
import splitstree5.gui.methodstab.MethodsViewTab;
import splitstree5.gui.workflowtab.WorkflowViewTab;
import splitstree5.gui.workflowtree.WorkflowTreeSupport;
import splitstree5.menu.MenuController;

import java.io.IOException;
import java.lang.reflect.Method;

public class MainWindow {
    private Document document;

    private final Parent root;
    private final MainWindowController mainWindowController;
    private final MenuController menuController;

    private final WorkflowTreeSupport workflowTreeSupport;

    private final TabPane mainTabPane;
    private final TabPane algorithmsTabPane;

    private Stage stage;

    private final StringProperty titleProperty = new SimpleStringProperty("Untitled");

    private boolean allowClose = false;

    private final ObservableMap<ANode, ViewerTab> aNode2ViewerTab;

    private final StringProperty dirtyStar = new SimpleStringProperty("");


    /**
     * constructor
     *
     * @throws IOException
     */
    public MainWindow() {
        this.document = new Document();
        document.setMainWindow(this);
        aNode2ViewerTab = FXCollections.observableHashMap();

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

        mainTabPane = mainWindowController.getMainTabPane();

        mainTabPane.getSelectionModel().selectedItemProperty().addListener((c, o, n) -> {
            if (o instanceof ISavesPreviousSelection) {
                ((ISavesPreviousSelection) o).saveAsPreviousSelection();
            }
            updateMenus(n, menuController);
        });

        algorithmsTabPane = mainWindowController.getAlgorithmTabPane();

        document.dirtyProperty().addListener((c, o, n) -> {
            dirtyStar.set(n ? "*" : "");
        });

        titleProperty.bind(Bindings.concat("Main Window - ").concat(document.nameProperty()).concat(dirtyStar).concat(" - SplitsTree5"));

        {
            final TreeItem<String> rootItem = new TreeItem<>("");
            Label label = new Label();
            label.textProperty().bind(document.nameProperty());
            label.setGraphic(new ImageView(ResourceManager.getIcon("Document16.gif")));
            rootItem.setGraphic(label);
            mainWindowController.getTreeView().setRoot(rootItem);
            workflowTreeSupport = new WorkflowTreeSupport(mainWindowController.getTreeView(), document);
        }

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
                if (!n && mainTabPane.getSelectionModel().getSelectedItem() instanceof ISavesPreviousSelection)
                    ((ISavesPreviousSelection) mainTabPane.getSelectionModel().getSelectedItem()).saveAsPreviousSelection();
            });

            Platform.runLater(() ->
            {
                stage.getScene().setOnKeyTyped((e) -> {
                    if (e.getCharacter().equals("+") && e.isShortcutDown())
                        menuController.getIncreaseFontSizeMenuItem().fire();
                    e.consume();
                });
                stage.getScene().setOnKeyTyped((e) -> {
                    if (e.getCharacter().equals("-") && e.isShortcutDown())
                        menuController.getDecreaseFontSizeMenuItem().fire();
                    e.consume();
                });
                stage.getIcons().setAll(ResourceManager.getIcon("SplitsTree5-16.png"), ResourceManager.getIcon("SplitsTree5-32.png"),
                        ResourceManager.getIcon("SplitsTree5-64.png"), ResourceManager.getIcon("SplitsTree5-128.png"));


                final FormatTab formatTab = new FormatTab(this);
                getMainWindowController().getAlgorithmTabPane().getTabs().add(formatTab);

            });
        }

        stage.titleProperty().bind(titleProperty);
        stage.setScene(new Scene(root, 800, 600));

        stage.setX(screenX);
        stage.setY(screenY);

        new TabPaneDragAndDropSupport(mainWindowController.getMainTabPane(), new AuxWindow());

        new TabPaneDragAndDropSupport(mainWindowController.getAlgorithmTabPane(), new AuxWindow());


        stage.show();
        stage.sizeToScene();
        stage.toFront();

        if (document.getWorkflow() != null) {
            add(new MethodsViewTab(document));
            add(new WorkflowViewTab(document));
        }

        mainWindowController.getSplitPane().widthProperty().addListener((c, o, n) -> {
            if (n.doubleValue() > 0) {
                double[] dividerPositions = mainWindowController.getSplitPane().getDividerPositions();
                {
                    double oldWidth = dividerPositions[0] * o.doubleValue();
                    dividerPositions[0] = oldWidth / n.doubleValue();
                }
                if (false) {
                    double oldWidth = (1.0 - dividerPositions[1]) * o.doubleValue();
                    dividerPositions[1] = 1.0 - oldWidth / n.doubleValue();
                }
                mainWindowController.getSplitPane().setDividerPositions(dividerPositions);
            }
        });

        MainWindowManager.getInstance().addMainWindow(this);
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
        mainWindowController.getMainTabPane().getTabs().add(0, viewerTab);
        mainWindowController.getMainTabPane().getSelectionModel().select(0);
        viewerTab.setMainWindow(this);
    }

    /**
     * remove a tab
     *
     * @param viewerTab
     */
    public void remove(Tab viewerTab) {
        mainWindowController.getMainTabPane().getTabs().remove(viewerTab);
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

        ViewerTab viewerTab = aNode2ViewerTab.get(aNode);
        if (viewerTab == null || viewerTab.getTabPane() == null) {
            viewerTab = new DataViewTab(document, aNode);
            aNode2ViewerTab.put(aNode, viewerTab);
            mainTabPane.getTabs().add(0, viewerTab);
        }
        final Stage stage = (Stage) viewerTab.getTabPane().getScene().getWindow();
        if (stage != null) {
            stage.toFront();
        }
        mainTabPane.getSelectionModel().select(viewerTab);
    }

    /**
     * show a data view node
     *
     * @param aNode
     */
    public void showAlgorithmView(AConnector aNode) {
        ViewerTab viewerTab = aNode2ViewerTab.get(aNode);
        if (viewerTab == null || viewerTab.getTabPane() == null) {
            viewerTab = new AlgorithmTab<>(document, aNode);
            aNode2ViewerTab.put(aNode, viewerTab);
            algorithmsTabPane.getTabs().add(0, viewerTab);
        }
        final Stage stage = (Stage) viewerTab.getTabPane().getScene().getWindow();
        if (stage != null) {
            stage.toFront();
        }
        getMainWindowController().ensureAlgorithmsTabPaneIsOpen();
        algorithmsTabPane.getSelectionModel().select(viewerTab);
    }

    /**
     * closes the current window. If it is the last to close, will ask for confirmation and then quit
     *
     * @return true if closed, false if canceled
     */
    public boolean close() {
        return !SaveBeforeClose.apply(this) || MainWindowManager.getInstance().closeMainWindow(this);
    }

}
