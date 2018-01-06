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

package splitstree5.gui.connectorview;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import jloda.fx.ExtendedFXMLLoader;
import jloda.util.Basic;
import jloda.util.ProgramProperties;
import jloda.util.ResourceManager;
import splitstree5.core.Document;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.ADataBlock;
import splitstree5.core.misc.Taxon;
import splitstree5.core.project.ProjectManager;
import splitstree5.core.workflow.UpdateState;
import splitstree5.gui.taxafilterview.TaxaFilterPane;
import splitstree5.info.MethodsTextGenerator;
import splitstree5.main.ISavesPreviousSelection;
import splitstree5.menu.MenuController;
import splitstree5.undo.UndoRedoManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * create a connector view
 * Daniel Huson , 12.2016
 */
public class ConnectorView<P extends ADataBlock, C extends ADataBlock> implements IShowable {
    private final Document document;
    private final Parent root;
    private final ConnectorViewController controller;
    private final MenuController menuController;
    private final UndoRedoManager undoManager;
    private AlgorithmPane algorithmPane;
    private Algorithm<P, C> currentAlgorithm;
    private Stage stage;

    private Map<Algorithm<P, C>, AlgorithmPane> algorithm2pane = new HashMap<>(); // we keep used panes around in case we want to undo back to one

    private final AConnector<P, C> connector;

    /**
     * constructor
     */
    public ConnectorView(Document document, AConnector<P, C> connector) throws IOException {
        this.document = document;
        this.connector = connector;
        this.currentAlgorithm = connector.getAlgorithm();

        {
            final ExtendedFXMLLoader<ConnectorViewController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
            root = extendedFXMLLoader.getRoot();
            controller = extendedFXMLLoader.getController();
        }
        {
            final ExtendedFXMLLoader<MenuController> extendedFXMLLoader = new ExtendedFXMLLoader<>(MenuController.class);
            menuController = extendedFXMLLoader.getController();
        }
        if (true || !ProgramProperties.isMacOS()) {
            menuController.getMenuBar().getMenus().remove(2, menuController.getMenuBar().getMenus().size() - 2);
            // keep File, Edit, Window and Help
        }
        controller.getBorderPane().setTop(menuController.getMenuBar());

        undoManager = new UndoRedoManager();

        connector.stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == UpdateState.VALID) {
                algorithmPane.syncModel2Controller();
            }
            // undoManager.clear();
        });

        connector.getParent().stateProperty().addListener((observable, oldValue, newValue) -> {
            undoManager.clear(); // if parent changes, have to forget history...
        });

        setupAlgorithmPane();

        final ComboBox<Algorithm> algorithmComboBox = controller.getAlgorithmComboBox();
        algorithmComboBox.setCellFactory(
                new Callback<ListView<Algorithm>, ListCell<Algorithm>>() {
                    @Override
                    public ListCell<Algorithm> call(ListView<Algorithm> param) {
                        return new ListCell<Algorithm>() {
                            @Override
                            public void updateItem(Algorithm item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null) {
                                    setText(item.getName());
                                    if (item.isApplicable(connector.getTaxaBlock(), connector.getParentDataBlock(), connector.getChildDataBlock()))
                                        setTextFill(Color.BLACK);
                                    else
                                        setTextFill(Color.LIGHTGRAY);
                                } else {
                                    setText(null);
                                }
                                //setPrefWidth(250);
                            }
                        };
                    }
                });

        final ArrayList<Algorithm<P, C>> algorithms = connector.getAllAlgorithms();
        if (currentAlgorithm != null) { // if algorithm already set, make sure we use the existing algorithm object
            for (int i = 0; i < algorithms.size(); i++) {
                final Algorithm<P, C> algorithm = algorithms.get(i);
                if (currentAlgorithm.getClass().equals(algorithm.getClass())) {
                    algorithms.set(i, currentAlgorithm);
                    break;
                }
            }
        }
        if (algorithms.size() <= 1 && currentAlgorithm != null) {
            algorithms.add(currentAlgorithm);
            algorithmComboBox.setDisable(true);
        }

        controller.getApplyButton().setOnAction((e) -> {
            if (connector.getAlgorithm() != currentAlgorithm)
                connector.setAlgorithm(currentAlgorithm);
            algorithmPane.syncController2Model();
            undoManager.addUndoableApply(algorithmPane::syncController2Model);
        });

        controller.getApplyButton().setDisable(!(currentAlgorithm.isApplicable(connector.getTaxaBlock(), connector.getParentDataBlock(), connector.getChildDataBlock())));

        connector.applicableProperty().addListener((c, o, n) -> {
            System.err.println(currentAlgorithm.getName() + " is applicable: " + n);
        });

        controller.getCancelButton().setOnAction((e) -> {
            if (connector.getAlgorithm() != currentAlgorithm) { // undo changes
                undoManager.clear();
                currentAlgorithm = connector.getAlgorithm();
                algorithmComboBox.setValue(currentAlgorithm);
                setupAlgorithmPane();
                controller.getCenterPane().getChildren().setAll(algorithmPane);
            }
            ConnectorView.this.stage.close();
        });

        algorithmComboBox.getItems().addAll(algorithms);
        algorithmComboBox.setValue(currentAlgorithm);
        algorithmComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            undoManager.add("Set Algorithm", algorithmComboBox.valueProperty(), oldValue, newValue);
            currentAlgorithm = ((Algorithm) newValue);
            setupAlgorithmPane();
            controller.getCenterPane().getChildren().setAll(algorithmPane);
            controller.getApplyButton().setDisable((!currentAlgorithm.isApplicable(connector.getTaxaBlock(), connector.getParentDataBlock(), connector.getChildDataBlock())));
        });

        algorithmPane.syncModel2Controller();

        controller.getCenterPane().getChildren().setAll(algorithmPane);

        menuController.unbindAndDisableAllMenuItems();

        menuController.getUndoMenuItem().setOnAction((e) -> {
            undoManager.undo();
        });

        menuController.getUndoMenuItem().disableProperty().bind(new SimpleBooleanProperty(false).isEqualTo(undoManager.canUndoProperty()));
        menuController.getUndoMenuItem().textProperty().bind(undoManager.undoNameProperty());

        menuController.getRedoMenuItem().setOnAction((e) -> undoManager.redo());
        menuController.getRedoMenuItem().disableProperty().bind(new SimpleBooleanProperty(false).isEqualTo(undoManager.canRedoProperty()));
        menuController.getRedoMenuItem().textProperty().bind(undoManager.redoNameProperty());

        menuController.getCloseMenuItem().setOnAction((e) -> ConnectorView.this.stage.close());

        if (algorithmPane instanceof TaxaFilterPane) {
            final TaxaFilterPane taxaFilterPane = (TaxaFilterPane) algorithmPane;
            menuController.getSelectNoneMenuItem().setOnAction((c) -> {
                taxaFilterPane.getController().getActiveList().getSelectionModel().clearSelection();
                taxaFilterPane.getController().getInactiveList().getSelectionModel().clearSelection();
            });
            menuController.getSelectAllMenuItem().setOnAction((c) -> {
                taxaFilterPane.getController().getActiveList().getSelectionModel().selectAll();
                taxaFilterPane.getController().getInactiveList().getSelectionModel().selectAll();
            });
            menuController.getSelectFromPreviousMenuItem().setOnAction((c) -> {
                for (Taxon taxon : taxaFilterPane.getController().getActiveList().getItems()) {
                    if (ProjectManager.getInstance().getPreviousSelection().contains(taxon.getName()))
                        taxaFilterPane.getController().getActiveList().getSelectionModel().select(taxon);
                }
                taxaFilterPane.getController().getActiveList().scrollTo(taxaFilterPane.getController().getActiveList().getSelectionModel().getSelectedIndex());
                for (Taxon taxon : taxaFilterPane.getController().getInactiveList().getItems()) {
                    if (ProjectManager.getInstance().getPreviousSelection().contains(taxon.getName()))
                        taxaFilterPane.getController().getInactiveList().getSelectionModel().select(taxon);
                }
                taxaFilterPane.getController().getInactiveList().scrollTo(taxaFilterPane.getController().getInactiveList().getSelectionModel().getSelectedIndex());

            });
            menuController.getSelectFromPreviousMenuItem().disableProperty().bind(Bindings.isEmpty(ProjectManager.getInstance().getPreviousSelection()));
        }

        menuController.enableAllUnboundActionMenuItems();
    }

    /**
     * setup the algorithm pane
     */
    private void setupAlgorithmPane() {
        algorithmPane = algorithm2pane.get(currentAlgorithm);
        if (algorithmPane == null) {
            algorithmPane = currentAlgorithm.getControl();
            if (algorithmPane == null)
                algorithmPane = new GenericAlgorithmPane<>(connector, currentAlgorithm);
            algorithm2pane.put(currentAlgorithm, algorithmPane);
            algorithmPane.setDocument(document);
            algorithmPane.setUndoManager(undoManager);
            algorithmPane.setConnector(connector);
            algorithmPane.setPrefWidth(controller.getCenterPane().getWidth());
            algorithmPane.setPrefHeight(controller.getCenterPane().getHeight());
            algorithmPane.prefHeightProperty().bind(controller.getCenterPane().heightProperty());
            algorithmPane.prefWidthProperty().bind(controller.getCenterPane().widthProperty());
            algorithmPane.setup();
        }

        if (MethodsTextGenerator.getCitation(currentAlgorithm).length() == 0)
            controller.getStatusBar().setText(Basic.fromCamelCase(currentAlgorithm.getName()));
        else
            controller.getStatusBar().setText(Basic.fromCamelCase(currentAlgorithm.getName()) + " (" + MethodsTextGenerator.getCitation(currentAlgorithm) + ")");
    }

    public static int windowCount = 0;

    /**
     * show this view
     */
    public void show() {
        show(-1, -1);
    }

    /**
     * show this view
     */
    public void show(double screenX, double screenY) {
        if (stage == null) {
            stage = new Stage();
            stage.titleProperty().bind(Bindings.concat("Algorithm - ").concat(connector.nameProperty()).concat(" - SplitsTree5"));
            stage.setScene(new Scene(root, 600, 400));

            if (screenX == -1) {
                screenX = 100 + ConnectorView.windowCount * 40;
                screenY = 200 + ConnectorView.windowCount * 40;
                ConnectorView.windowCount++;
            }
            stage.setX(screenX);
            stage.setY(screenY);

            if (algorithmPane instanceof ISavesPreviousSelection)
                stage.focusedProperty().addListener((c, o, n) -> {
                    if (!n)
                        ((ISavesPreviousSelection) algorithmPane).saveAsPreviousSelection();
                });
        }
        stage.getIcons().setAll(ResourceManager.getIcon("Algorithm16.gif"));
        stage.show();
        stage.sizeToScene();
        stage.toFront();
    }

}