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

package splitstree5.gui.connectorview;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;
import splitstree5.core.Document;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.dag.UpdateState;
import splitstree5.core.datablocks.ADataBlock;
import splitstree5.undo.UndoManager;
import splitstree5.utils.ExtendedFXMLLoader;
import splitstree5.utils.Option;

import java.io.IOException;
import java.util.ArrayList;

/**
 * create a connector view
 * Created by huson on 12/31/16.
 */
public class ConnectorView<P extends ADataBlock, C extends ADataBlock> {
    private final Document document;
    private final Parent root;
    private final ConnectorViewController controller;
    private final UndoManager undoManager;
    private final AlgorithmPane algorithmPane;
    private Stage stage;

    private final ArrayList<Option> options = new ArrayList<>();

    private final AConnector<P, C> connector;

    /**
     * constructor
     */
    public ConnectorView(Document document, AConnector<P, C> connector) throws IOException {
        this.document = document;
        this.connector = connector;
        AlgorithmPane control = connector.getAlgorithm().getControl();
        this.algorithmPane = (control != null ? control : new GenericAlgorithmPane<>(connector));

        final ExtendedFXMLLoader<ConnectorViewController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        root = extendedFXMLLoader.getRoot();
        controller = extendedFXMLLoader.getController();
        undoManager = new UndoManager();

        algorithmPane.syncModel2Controller();
        setupController();

        connector.stateProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != UpdateState.VALID && newValue == UpdateState.VALID) {
                algorithmPane.syncModel2Controller();
            }
            undoManager.clear();
        });

        algorithmPane.setUndoManager(undoManager);
        algorithmPane.prefHeightProperty().bind(controller.getCenterPane().heightProperty());
        algorithmPane.prefWidthProperty().bind(controller.getCenterPane().widthProperty());
        algorithmPane.setup();
    }

    /**
     * setup controller
     */
    private void setupController() {
        setupCenterPane();

        // todo: add other algorithm names here
        final ChoiceBox<String> algorithmChoiceBox = controller.getAlgorithmChoiceBox();
        algorithmChoiceBox.getItems().add(connector.getAlgorithm().getName());
        algorithmChoiceBox.setValue(connector.getAlgorithm().getName());
        algorithmChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            undoManager.addUndoableChange("Algorithm", algorithmChoiceBox.valueProperty(), oldValue, newValue);
            // todo: set algorithm by name
            setupCenterPane();
        });

        controller.getUndoMenuItem().setOnAction((e) -> {
            undoManager.undo();
        });
        controller.getUndoMenuItem().disableProperty().bind(new SimpleBooleanProperty(false).isEqualTo(undoManager.canUndoProperty()));
        controller.getUndoMenuItem().textProperty().bind(undoManager.undoNameProperty());

        controller.getRedoMenuItem().setOnAction((e) -> undoManager.redo());
        controller.getRedoMenuItem().disableProperty().bind(new SimpleBooleanProperty(false).isEqualTo(undoManager.canRedoProperty()));
        controller.getRedoMenuItem().textProperty().bind(undoManager.redoNameProperty());

        controller.getApplyButton().setOnAction((e) -> algorithmPane.syncController2Model());

        controller.getApplyButton().disableProperty().bind(algorithmPane.applicableProperty().not().or(document.updatingProperty()));

        controller.getCancelButton().setOnAction((e) -> {
            ConnectorView.this.stage.close();
        });

        controller.getResetButton().setOnAction((e) -> {
            algorithmPane.syncModel2Controller();
            undoManager.clear();
            setupCenterPane();
        });
    }

    private void setupCenterPane() {
        controller.getCenterPane().getChildren().setAll(algorithmPane);
    }

    private static int windowCount = 0;

    /**
     * show this view
     */
    public void show() {
        stage = new Stage();
        stage.setTitle("Algorithm - SplitsTree5");
        stage.setScene(new Scene(root, 500, 450));

        stage.setX(100 + windowCount * 40);
        stage.setY(200 + windowCount * 40);
        windowCount++;

        stage.show();
        algorithmPane.syncModel2Controller();
    }
}