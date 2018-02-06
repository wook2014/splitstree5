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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.collections.SetChangeListener;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import jloda.fx.ExtendedFXMLLoader;
import jloda.util.Basic;
import jloda.util.Pair;
import jloda.util.ProgramProperties;
import jloda.util.ResourceManager;
import splitstree5.core.Document;
import splitstree5.core.datablocks.ViewDataBlock;
import splitstree5.core.workflow.Connector;
import splitstree5.core.workflow.DataNode;
import splitstree5.core.workflow.Workflow;
import splitstree5.core.workflow.WorkflowNode;
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
import splitstree5.toolbar.MainToolBarController;
import splitstree5.undo.UndoManager;

import java.io.IOException;

public class MainWindow {
    private final Document document;
    private final Workflow workflow;

    private final Parent root;
    private final MainWindowController mainWindowController;
    private final MenuController menuController;
    private final MainToolBarController toolBarController;

    private final WorkflowTreeSupport workflowTreeSupport;

    private final TabPane mainTabPane;
    private final TabPane algorithmsTabPane;

    private FormatTab formatTab;

    private Stage stage;

    private final StringProperty titleProperty = new SimpleStringProperty("Untitled");

    private boolean allowClose = false;

    private final ObservableMap<WorkflowNode, ViewerTab> aNode2ViewerTab;

    private final StringProperty dirtyStar = new SimpleStringProperty("");

    private final ObjectProperty<UndoManager> undoRedoManager = new SimpleObjectProperty<>();

    /**
     * constructor
     *
     * @throws IOException
     */
    public MainWindow() {
        this.document = new Document();
        this.workflow = document.getWorkflow();
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
            menuController.setMainWindow(this);
        }

        {
            final ExtendedFXMLLoader<MainToolBarController> extendedFXMLLoader = new ExtendedFXMLLoader<>(MainToolBarController.class);
            toolBarController = extendedFXMLLoader.getController();
            mainWindowController.setupOpenCloseLeft(toolBarController.getOpenCloseLeft());

            toolBarController.getOpenButton().setOnAction((e) -> menuController.getOpenMenuItem().fire());
            toolBarController.getOpenButton().disableProperty().bind(menuController.getOpenMenuItem().disableProperty());

            toolBarController.getFindButton().setOnAction((e) -> menuController.getFindMenuItem().fire());
            toolBarController.getFindButton().disableProperty().bind(menuController.getFindMenuItem().disableProperty());
            toolBarController.getPrintButton().setOnAction((e) -> menuController.getPrintMenuitem().fire());
            toolBarController.getPrintButton().disableProperty().bind(menuController.getPrintMenuitem().disableProperty());
            toolBarController.getSaveButton().setOnAction((e) -> menuController.getSaveMenuItem().fire());
            toolBarController.getSaveButton().disableProperty().bind(menuController.getSaveMenuItem().disableProperty());

            toolBarController.getZoomButton().setOnAction((e -> menuController.getResetMenuItem().fire()));
            toolBarController.getZoomButton().disableProperty().bind(menuController.getResetMenuItem().disableProperty());

            toolBarController.getZoomInButton().setOnAction((e) -> menuController.getZoomInMenuItem().fire());
            toolBarController.getZoomInButton().disableProperty().bind(menuController.getZoomInMenuItem().disableProperty());

            toolBarController.getZoomOutButton().setOnAction((e) -> menuController.getZoomOutMenuItem().fire());
            toolBarController.getZoomOutButton().disableProperty().bind(menuController.getZoomOutMenuItem().disableProperty());

            toolBarController.getRotateLeftButton().setOnAction((e) -> menuController.getRotateLeftMenuItem().fire());
            toolBarController.getRotateLeftButton().disableProperty().bind(menuController.getRotateLeftMenuItem().disableProperty().or(menuController.getRotateLeftMenuItem().textProperty().isNotEqualTo("Rotate Left")));
            toolBarController.getRotateRightButton().setOnAction((e) -> menuController.getRotateRightMenuItem().fire());
            toolBarController.getRotateRightButton().disableProperty().bind(menuController.getRotateRightMenuItem().disableProperty().or(menuController.getRotateRightMenuItem().textProperty().isNotEqualTo("Rotate Right")));
        }
        mainWindowController.getTopVBox().getChildren().setAll(menuController.getMenuBar());
        mainWindowController.getTopVBox().getChildren().add(toolBarController.getToolBar());

        mainTabPane = mainWindowController.getMainTabPane();

        algorithmsTabPane = mainWindowController.getAlgorithmTabPane();

        document.dirtyProperty().addListener((c, o, n) -> {
            dirtyStar.set(n ? "*" : "");
        });

        titleProperty.bind(Bindings.concat("Main Window - ").concat(document.nameProperty()).concat(dirtyStar).concat(" - " + ProgramProperties.getProgramName()));

        {
            final TreeItem<String> rootItem = new TreeItem<>("");
            Label label = new Label();
            label.textProperty().bind(document.nameProperty());
            label.setGraphic(new ImageView(ResourceManager.getIcon("Document16.gif")));
            rootItem.setGraphic(label);
            final ContextMenu contextMenu = new ContextMenu();
            MenuItem openItem = new MenuItem("Open...");
            openItem.setOnAction((e) -> menuController.getOpenMenuItem().fire());
            openItem.disableProperty().bind(Bindings.isNotNull(workflow.topTaxaNodeProperty()));
            MenuItem importItem = new MenuItem("Import...");
            importItem.setOnAction((e) -> menuController.getImportMenuItem().fire());
            importItem.disableProperty().bind(Bindings.isNotNull(workflow.topTaxaNodeProperty()));
            MenuItem closeItem = new MenuItem("Close");
            closeItem.setOnAction((e) -> menuController.getCloseMenuItem().fire());

            contextMenu.getItems().addAll(openItem, importItem, new SeparatorMenuItem(), closeItem);
            label.setContextMenu(contextMenu);
            mainWindowController.getTreeView().setRoot(rootItem);
            workflowTreeSupport = new WorkflowTreeSupport(mainWindowController.getTreeView(), document);
        }

        mainTabPane.getSelectionModel().selectedItemProperty().addListener((c, o, n) -> {
            if (o instanceof ISavesPreviousSelection) {
                ((ISavesPreviousSelection) o).saveAsPreviousSelection();
            }
            setUndoRedoManager(((ViewerTab) n).getUndoManager());
            if (getStage() != null)
                updateMenus(n, menuController);
        });

        workflow.dataNodes().addListener((SetChangeListener<DataNode>) (c) -> {
            if (c.wasRemoved() && aNode2ViewerTab.get(c.getElementRemoved()) != null) {
                Platform.runLater(() -> getMainWindowController().getMainTabPane().getTabs().remove(aNode2ViewerTab.get(c.getElementRemoved())));
            }
            if (c.wasAdded() && c.getElementAdded().getDataBlock() instanceof ViewDataBlock) {
                Platform.runLater(() -> showDataView(c.getElementAdded()));
            }
        });

        workflow.connectors().addListener((SetChangeListener<Connector>) (c) -> {
            if (c.wasRemoved() && aNode2ViewerTab.get(c.getElementRemoved()) != null) {
                Platform.runLater(() -> getMainWindowController().getAlgorithmTabPane().getTabs().remove(aNode2ViewerTab.get(c.getElementRemoved())));
            }
        });
    }

    /**
     * show this view
     */
    public void show(Stage stage0, double screenX, double screenY) {
        if (stage != null)
            this.stage = stage0;
        else {
            this.stage = new Stage();
            stage.getIcons().setAll(ProgramProperties.getProgramIcons());

            stage.focusedProperty().addListener((c, o, n) -> {
                if (!n && mainTabPane.getSelectionModel().getSelectedItem() instanceof ISavesPreviousSelection)
                    ((ISavesPreviousSelection) mainTabPane.getSelectionModel().getSelectedItem()).saveAsPreviousSelection();
            });

            Platform.runLater(() ->
            {
                stage.getScene().setOnKeyTyped((e) -> {
                    if (e.getCharacter().equals("+") && e.isShortcutDown() && !menuController.getIncreaseFontSizeMenuItem().isDisable())
                        menuController.getIncreaseFontSizeMenuItem().fire();
                    e.consume();
                });
                stage.getScene().setOnKeyTyped((e) -> {
                    if (e.getCharacter().equals("-") && e.isShortcutDown() && !menuController.getDecreaseFontSizeMenuItem().isDisable())
                        menuController.getDecreaseFontSizeMenuItem().fire();
                    e.consume();
                });

                formatTab = new FormatTab(this);

                getMenuController().setupFullScreenMenuSupport(stage);
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

        add(new MethodsViewTab(document));
        add(new WorkflowViewTab(document));

        mainWindowController.getSplitPane().widthProperty().addListener((c, o, n) -> {
            if (n.doubleValue() > 0) {
                double[] dividerPositions = mainWindowController.getSplitPane().getDividerPositions();
                {
                    double oldWidth = dividerPositions[0] * o.doubleValue();
                    dividerPositions[0] = oldWidth / n.doubleValue();
                }
                mainWindowController.getSplitPane().setDividerPositions(dividerPositions);
            }
        });
        mainWindowController.getSplitPane().heightProperty().addListener((c, o, n) -> {
            if (n.doubleValue() > 0) {
                double[] dividerPositions = mainWindowController.getAlgorithmSplitPane().getDividerPositions();
                {
                    double oldHeight = (1 - dividerPositions[0]) * o.doubleValue();
                    dividerPositions[0] = 1 - oldHeight / n.doubleValue();
                }
                mainWindowController.getAlgorithmSplitPane().setDividerPositions(dividerPositions);
            }
        });

        MainWindowManager.getInstance().addMainWindow(this);
        getMainWindowController().openCloseLeft(false);
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
    private void add(ViewerTab viewerTab) {
        mainWindowController.getMainTabPane().getTabs().add(0, viewerTab);
        mainWindowController.getMainTabPane().getSelectionModel().select(0);
        viewerTab.setMainWindow(this);
    }

    /**
     * update the menus
     *
     * @param selectedTab
     * @param controller
     */
    private void updateMenus(Tab selectedTab, MenuController controller) {
        controller.unbindAndDisableAllMenuItems();
        MainWindowMenuController.setupMainMenus(this);
        if (selectedTab instanceof ViewerTab) {
            final ViewerTab viewer = (ViewerTab) selectedTab;
            viewer.updateMenus(controller);
        }
        MainWindowMenuController.updateConstructionMenuItems(this, selectedTab, controller);

        controller.enableAllUnboundActionMenuItems();
    }

    /**
     * show a data view node
     *
     * @param workflowNode
     */
    public void showDataView(DataNode workflowNode) {
        // if the data block has a getTab method, then assume that it is present and select it
        try {
            if (workflowNode.getDataBlock() instanceof ViewDataBlock) {
                final ViewerTab viewerTab = ((ViewDataBlock) workflowNode.getDataBlock()).getTab();
                if (!mainTabPane.getTabs().contains(viewerTab)) {
                    mainTabPane.getTabs().add(viewerTab);
                    aNode2ViewerTab.put(workflowNode, viewerTab);
                }
                TabPane tabPane = viewerTab.getTabPane(); // might be in an auxilary window
                if (tabPane == null)
                    tabPane = mainTabPane;
                tabPane.getSelectionModel().select(viewerTab);
                if (tabPane.getScene() != null && tabPane.getScene().getWindow() != null) {
                    final Stage stage = (Stage) tabPane.getScene().getWindow();
                    if (stage != null) {
                        stage.toFront();
                    }
                }
            } else {
                ViewerTab viewerTab = aNode2ViewerTab.get(workflowNode);
                if (viewerTab == null || viewerTab.getTabPane() == null) {
                    viewerTab = new DataViewTab(document, workflowNode);
                    aNode2ViewerTab.put(workflowNode, viewerTab);
                    mainTabPane.getTabs().add(0, viewerTab);
                }
                mainTabPane.getSelectionModel().select(viewerTab);
                final Stage stage = (Stage) viewerTab.getTabPane().getScene().getWindow();
                if (stage != null) {
                    stage.toFront();
                }
            }
        } catch (Exception ex) {
            Basic.caught(ex);
            // doesn't matter
        }
    }

    /**
     * show a data view node
     *
     * @param workflowNode
     */
    public void showAlgorithmView(Connector workflowNode) {
        ViewerTab viewerTab = aNode2ViewerTab.get(workflowNode);
        if (viewerTab == null) {
            viewerTab = new AlgorithmTab<>(document, workflowNode);
            aNode2ViewerTab.put(workflowNode, viewerTab);
            algorithmsTabPane.getTabs().add(0, viewerTab);
        }
        TabPane tabPane = viewerTab.getTabPane(); // might be in an auxilary window
        if (tabPane == null)
            tabPane = mainTabPane;
        tabPane.getSelectionModel().select(viewerTab);

        final Stage stage = (Stage) viewerTab.getTabPane().getScene().getWindow();
        if (stage != null) {
            stage.toFront();
        }
        getMainWindowController().ensureAlgorithmsTabPaneIsOpen();
    }

    /**
     * closes the current window.
     *
     * @return true if closed, false if canceled
     */
    public boolean close() {
        boolean result = !SaveBeforeClose.apply(this) || MainWindowManager.getInstance().closeMainWindow(this);
        if (result) {
            workflow.cancelAll();
            for (Tab tab : mainTabPane.getTabs()) {
                if (tab instanceof ViewerTab && ((ViewerTab) tab).getFindToolBar() != null)
                    ((ViewerTab) tab).getFindToolBar().cancel(); // cancel all find jobs
            }
        }
        return result;
    }

    public void showFormatTab() {
        if (formatTab != null) {
            if (!getMainWindowController().getAlgorithmTabPane().getTabs().contains(formatTab))
                getMainWindowController().getAlgorithmTabPane().getTabs().add(formatTab);
            getMainWindowController().ensureAlgorithmsTabPaneIsOpen();
            getMainWindowController().getAlgorithmTabPane().getSelectionModel().select(formatTab);
        }
    }

    public UndoManager getUndoRedoManager() {
        return undoRedoManager.get();
    }

    public ObjectProperty<UndoManager> undoRedoManagerProperty() {
        return undoRedoManager;
    }

    public void setUndoRedoManager(UndoManager undoManager) {
        this.undoRedoManager.set(undoManager);
    }

    public WorkflowViewTab getWorkflowTab() {
        for (Tab tab : mainWindowController.getMainTabPane().getTabs()) {
            if (tab instanceof WorkflowViewTab)
                return (WorkflowViewTab) tab;
        }
        return null;
    }

    /**
     * show a pair of algorithm and data
     *
     * @param pair
     */
    public void show(Pair<Connector, DataNode> pair) {
        showAlgorithmView(pair.getFirst());
        showDataView(pair.getSecond());
    }

    public Workflow getWorkflow() {
        return workflow;
    }
}
