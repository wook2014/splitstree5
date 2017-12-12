/*
 *  Copyright (C) 2017 Daniel H. Huson
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

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;
import jloda.fx.ExtendedFXMLLoader;
import jloda.util.Basic;
import splitstree5.core.Document;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.ADataBlock;
import splitstree5.core.workflow.UpdateState;
import splitstree5.info.MethodsTextGenerator;
import splitstree5.undo.UndoRedoManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * create a connector view
 * Created by huson on 12/31/16.
 */
public class ConnectorView<P extends ADataBlock, C extends ADataBlock> {
    private final Document document;
    private final Parent root;
    private final ConnectorViewController controller;
    private final UndoRedoManager undoManager;
    private AlgorithmPane algorithmPane;
    private Stage stage;

    private Map<Algorithm<P, C>, AlgorithmPane> algorithm2pane = new HashMap<>(); // we keep used panes around in case we want to undo back to one

    private final AConnector<P, C> connector;

    /**
     * constructor
     */
    public ConnectorView(Document document, AConnector<P, C> connector) throws IOException {
        this.document = document;
        this.connector = connector;

        final ExtendedFXMLLoader<ConnectorViewController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        root = extendedFXMLLoader.getRoot();
        controller = extendedFXMLLoader.getController();
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

        final ChoiceBox<Algorithm> algorithmChoiceBox = controller.getAlgorithmChoiceBox();
        final ArrayList<Algorithm<P, C>> algorithms = connector.getAllAlgorithms();
        if (connector.getAlgorithm() != null) { // if algorithm already set, make sure we use the existing algorithm object
            for (int i = 0; i < algorithms.size(); i++) {
                final Algorithm<P, C> algorithm = algorithms.get(i);
                if (connector.getAlgorithm().getClass().equals(algorithm.getClass())) {
                    algorithms.set(i, connector.getAlgorithm());
                    break;
                }
            }
        }
        if (algorithms.size() <= 1 && connector.getAlgorithm() != null) {
            algorithms.add(connector.getAlgorithm());
            algorithmChoiceBox.setDisable(true);
        }

        controller.getUndoMenuItem().setOnAction((e) -> {
            undoManager.undo();
        });
        controller.getUndoMenuItem().disableProperty().bind(new SimpleBooleanProperty(false).isEqualTo(undoManager.canUndoProperty()));
        controller.getUndoMenuItem().textProperty().bind(undoManager.undoNameProperty());

        controller.getRedoMenuItem().setOnAction((e) -> undoManager.redo());
        controller.getRedoMenuItem().disableProperty().bind(new SimpleBooleanProperty(false).isEqualTo(undoManager.canRedoProperty()));
        controller.getRedoMenuItem().textProperty().bind(undoManager.redoNameProperty());

        controller.getApplyButton().setOnAction((e) -> {
            algorithmPane.syncController2Model();
            undoManager.addUndoableApply(algorithmPane::syncController2Model);
        });

        controller.getApplyButton().disableProperty().bind(algorithmPane.applicableProperty().not());
        // controller.getApplyButton().disableProperty().bind(document.updatingProperty().or(algorithmPane.applicableProperty().not()));
        // todo: need to bind to something that lets us know that something has changed

        connector.applicableProperty().addListener((c, o, n) -> {
            System.err.println(connector.getAlgorithm().getName() + " is applicable: " + n);
        });

        controller.getCancelButton().setOnAction((e) -> ConnectorView.this.stage.close());
        controller.getCloseMenuItem().setOnAction((e) -> ConnectorView.this.stage.close());

        controller.getResetButton().setOnAction((e) -> {
            algorithmPane.syncModel2Controller();
            undoManager.clear();
            controller.getCenterPane().getChildren().setAll(algorithmPane);
        });

        algorithmChoiceBox.getItems().addAll(algorithms);
        algorithmChoiceBox.setValue(connector.getAlgorithm());
        algorithmChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            undoManager.add("Set Algorithm", algorithmChoiceBox.valueProperty(), oldValue, newValue);
            connector.setAlgorithm((Algorithm) newValue);
            setupAlgorithmPane();
            controller.getCenterPane().getChildren().setAll(algorithmPane);
        });

        algorithmPane.syncModel2Controller();

        controller.getCenterPane().getChildren().setAll(algorithmPane);
    }

    /**
     * setup the algorithm pane
     */
    private void setupAlgorithmPane() {
        algorithmPane = algorithm2pane.get(connector.getAlgorithm());
        if (algorithmPane == null) {
            algorithmPane = connector.getAlgorithm().getControl();
            if (algorithmPane == null)
                algorithmPane = new GenericAlgorithmPane<>(connector);
            algorithm2pane.put(connector.getAlgorithm(), algorithmPane);
            algorithmPane.setDocument(document);
            algorithmPane.setUndoManager(undoManager);
            algorithmPane.setConnector(connector);
            algorithmPane.setPrefWidth(controller.getCenterPane().getWidth());
            algorithmPane.setPrefHeight(controller.getCenterPane().getHeight());
            algorithmPane.prefHeightProperty().bind(controller.getCenterPane().heightProperty());
            algorithmPane.prefWidthProperty().bind(controller.getCenterPane().widthProperty());
            algorithmPane.setup();
        }

        if (MethodsTextGenerator.getCitation(connector.getAlgorithm()).length() == 0)
            controller.getStatusBar().setText(Basic.fromCamelCase(connector.getAlgorithm().getName()));
        else
            controller.getStatusBar().setText(Basic.fromCamelCase(connector.getAlgorithm().getName()) + " (" + MethodsTextGenerator.getCitation(connector.getAlgorithm()) + ")");
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
            stage.setTitle("Algorithm - SplitsTree5");
            stage.setScene(new Scene(root, 600, 400));

            if (screenX == -1) {
                screenX = 100 + ConnectorView.windowCount * 40;
                screenY = 200 + ConnectorView.windowCount * 40;
                ConnectorView.windowCount++;
            }
            stage.setX(screenX);
            stage.setY(screenY);
        }
        stage.show();
        stage.sizeToScene();
        stage.toFront();
    }
}