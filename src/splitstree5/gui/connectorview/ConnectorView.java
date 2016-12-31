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
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;
import jloda.util.Basic;
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
    private Stage stage;

    private final ArrayList<Option> options = new ArrayList<>();

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
        undoManager = new UndoManager();

        syncModel2Controller();
        setupController();

        connector.stateProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != UpdateState.VALID && newValue == UpdateState.VALID) {
                syncModel2Controller();
            }
            undoManager.clear();
        });
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

        controller.getApplyButton().setOnAction((e) -> syncController2Model());
        controller.getApplyButton().disableProperty().bind(document.updatingProperty());

        controller.getCancelButton().setOnAction((e) -> {
            ConnectorView.this.stage.close();
        });

        controller.getResetButton().setOnAction((e) -> {
            syncModel2Controller();
            undoManager.clear();
        });
    }

    private void setupCenterPane() {
        final Pane centerPane = controller.getCenterPane();
        final GridPane grid = new GridPane();
        centerPane.getChildren().setAll(grid);

        int row = 0;
        try {
            for (Option option : options) {
                final String text = Basic.fromCamelCase(option.getName());
                grid.add(new Label(text), 0, row);
                switch (option.getType().getTypeName()) {
                    case "boolean": {
                        final CheckBox control = new CheckBox("");
                        control.setSelected((boolean) option.getValue());
                        control.selectedProperty().addListener((observable, oldValue, newValue) -> {
                            undoManager.addUndoableChange(text, control.selectedProperty(), oldValue, newValue);
                            option.holdValue(newValue);
                        });
                        grid.add(control, 1, row);
                        break;
                    }
                    case "int": {
                        javafx.scene.control.TextField control = new TextField();
                        control.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
                        control.setText(option.getValue().toString());
                        control.textProperty().addListener((observable, oldValue, newValue) -> {
                            if (newValue.length() > 0 && Basic.isInteger(newValue)) {
                                undoManager.addUndoableChange(text, control.textProperty(), oldValue, newValue);
                                option.holdValue(Basic.parseInt(newValue));
                            }
                        });
                        grid.add(control, 1, row);
                        break;
                    }
                    case "double": {
                        javafx.scene.control.TextField control = new TextField();
                        control.setTextFormatter(new TextFormatter<>(new DoubleStringConverter()));
                        control.setText(option.getValue().toString());
                        control.textProperty().addListener((observable, oldValue, newValue) -> {
                            if (newValue.length() > 0 && Basic.isDouble(newValue)) {
                                undoManager.addUndoableChange(text, control.textProperty(), oldValue, newValue);
                                option.holdValue(Basic.parseDouble(newValue));
                            }
                        });
                        grid.add(control, 1, row);
                        break;
                    }
                    case "float": {
                        javafx.scene.control.TextField control = new TextField();
                        control.setTextFormatter(new TextFormatter<>(new FloatStringConverter()));
                        control.setText(option.getValue().toString());
                        control.textProperty().addListener((observable, oldValue, newValue) -> {
                            if (newValue.length() > 0 && Basic.isFloat(newValue)) {
                                undoManager.addUndoableChange(text, control.textProperty(), oldValue, newValue);
                                option.holdValue(Basic.isFloat(newValue));
                            }
                        });
                        grid.add(control, 1, row);
                        break;
                    }
                    case "String": {
                        javafx.scene.control.TextField control = new TextField();
                        control.setText(option.getValue().toString());
                        control.textProperty().addListener((observable, oldValue, newValue) -> {
                            if (newValue.length() > 0 && newValue.length() > 0)
                                undoManager.addUndoableChange(text, control.textProperty(), oldValue, newValue);
                            option.holdValue(newValue);
                        });
                        grid.add(control, 1, row);
                        break;
                    }
                }
                row++;
            }
        } catch (Exception ex) {
            Basic.caught(ex);
        }
    }

    /**
     * show this view
     */
    public void show() {
        stage = new Stage();
        stage.setTitle("Algorithm - SplitsTree5");
        stage.setScene(new Scene(root, 450, 450));
        stage.show();
        syncModel2Controller();
    }

    /**
     * sync model to controller
     */
    private void syncModel2Controller() {
        options.clear();
        options.addAll(Option.getAllOptions(connector.getAlgorithm()));
    }

    /**
     * sync controller to model
     */
    private void syncController2Model() {
        for (Option option : options) {
            try {
                option.setValue();
            } catch (Exception e) {
                Basic.caught(e);
            }
        }
    }
}