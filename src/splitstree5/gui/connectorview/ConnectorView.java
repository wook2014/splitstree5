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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
import splitstree5.utils.OptionsAccessor;

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
    private final CustomizedControl customizedControl;
    private Stage stage;

    private final ArrayList<Option> options = new ArrayList<>();

    private final AConnector<P, C> connector;

    /**
     * constructor
     */
    public ConnectorView(Document document, AConnector<P, C> connector) throws IOException {
        this.document = document;
        this.connector = connector;
        this.customizedControl = connector.getAlgorithm().getControl();

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

        if (customizedControl != null) {
            customizedControl.setUndoManager(undoManager);
            customizedControl.prefHeightProperty().bind(controller.getCenterPane().heightProperty());
            customizedControl.prefWidthProperty().bind(controller.getCenterPane().widthProperty());
            customizedControl.setup();
        }
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

        if (customizedControl == null)
            controller.getApplyButton().disableProperty().bind(document.updatingProperty());
        else
            controller.getApplyButton().disableProperty().bind(customizedControl.applicableProperty().not().or(document.updatingProperty()));

        controller.getCancelButton().setOnAction((e) -> {
            ConnectorView.this.stage.close();
        });

        controller.getResetButton().setOnAction((e) -> {
            syncModel2Controller();
            undoManager.clear();
            setupCenterPane();
        });
    }

    private void setupCenterPane() {
        final Pane centerPane = controller.getCenterPane();

        // check whether connector has own pane and if so, use it
        {
            if (customizedControl != null) {
                centerPane.getChildren().setAll(customizedControl);
                return;
            }
        }
        final GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 5, 10, 5));
        grid.setHgap(20);
        centerPane.getChildren().setAll(grid);

        int row = 1;
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
                        if (option.getInfo() != null)
                            control.setTooltip(new Tooltip(option.getInfo()));
                        grid.add(control, 1, row);
                        break;
                    }
                    case "int": {
                        javafx.scene.control.TextField control = new TextField();
                        control.addEventFilter(KeyEvent.ANY, e -> {
                            if (e.getCode() == KeyCode.Z && e.isShortcutDown()) {
                                e.consume();
                                control.getParent().fireEvent(e);
                            }
                        });

                        control.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
                        control.setText(option.getValue().toString());
                        control.textProperty().addListener((observable, oldValue, newValue) -> {
                            if (newValue.length() == 0)
                                newValue = "0";
                            if (Basic.isInteger(newValue)) {
                                undoManager.addUndoableChange(text, control.textProperty(), oldValue, newValue);
                                option.holdValue(Basic.parseInt(newValue));
                            }
                        });
                        if (option.getInfo() != null)
                            control.setTooltip(new Tooltip(option.getInfo()));
                        grid.add(control, 1, row);
                        break;
                    }
                    case "double": {
                        javafx.scene.control.TextField control = new TextField();
                        control.addEventFilter(KeyEvent.ANY, e -> {
                            if (e.getCode() == KeyCode.Z && e.isShortcutDown()) {
                                e.consume();
                                control.getParent().fireEvent(e);
                            }
                        });

                        control.setTextFormatter(new TextFormatter<>(new DoubleStringConverter()));
                        control.setText(option.getValue().toString());
                        control.textProperty().addListener((observable, oldValue, newValue) -> {
                            if (newValue.length() == 0)
                                newValue = "0";
                            if (Basic.isDouble(newValue)) {
                                undoManager.addUndoableChange(text, control.textProperty(), oldValue, newValue);
                                option.holdValue(Basic.parseDouble(newValue));
                            }
                        });
                        if (option.getInfo() != null)
                            control.setTooltip(new Tooltip(option.getInfo()));
                        grid.add(control, 1, row);
                        break;
                    }
                    case "float": {
                        javafx.scene.control.TextField control = new TextField();
                        control.addEventFilter(KeyEvent.ANY, e -> {
                            if (e.getCode() == KeyCode.Z && e.isShortcutDown()) {
                                e.consume();
                                control.getParent().fireEvent(e);
                            }
                        });

                        control.setTextFormatter(new TextFormatter<>(new FloatStringConverter()));
                        control.setText(option.getValue().toString());
                        control.textProperty().addListener((observable, oldValue, newValue) -> {
                            if (newValue.length() == 0)
                                newValue = "0";
                            if (Basic.isFloat(newValue)) {
                                undoManager.addUndoableChange(text, control.textProperty(), oldValue, newValue);
                                option.holdValue(Basic.isFloat(newValue));
                            }
                        });
                        if (option.getInfo() != null)
                            control.setTooltip(new Tooltip(option.getInfo()));
                        grid.add(control, 1, row);
                        break;
                    }
                    case "java.lang.String": {
                        final Control control;
                        if (option.getLegalValues() != null) {
                            final ChoiceBox<String> choiceBox = new ChoiceBox<>();
                            choiceBox.getItems().addAll(option.getLegalValues());
                            choiceBox.setValue(option.getValue().toString());
                            choiceBox.valueProperty().addListener(new ChangeListener<String>() {
                                @Override
                                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                                    undoManager.addUndoableChange(text, choiceBox.valueProperty(), oldValue, newValue);
                                    option.holdValue(newValue);
                                }
                            });
                            control = choiceBox;
                        } else {
                            final TextField textField = new TextField();
                            textField.addEventFilter(KeyEvent.ANY, e -> {
                                if (e.getCode() == KeyCode.Z && e.isShortcutDown()) {
                                    e.consume();
                                    textField.getParent().fireEvent(e);
                                }
                            });

                            textField.setText(option.getValue().toString());
                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                option.holdValue(newValue);
                            });
                            control = textField;
                        }
                        if (option.getInfo() != null)
                            control.setTooltip(new Tooltip(option.getInfo()));
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

    private static int windowCount = 0;

    /**
     * show this view
     */
    public void show() {
        stage = new Stage();
        stage.setTitle("Algorithm - SplitsTree5");
        stage.setScene(new Scene(root, 450, 450));

        stage.setX(100 + windowCount * 40);
        stage.setY(200 + windowCount * 40);
        windowCount++;

        stage.show();
        syncModel2Controller();
    }

    /**
     * sync model to controller
     */
    private void syncModel2Controller() {
        if (customizedControl != null)
            customizedControl.syncModel2Controller();
        options.clear();
        options.addAll(OptionsAccessor.getAllOptions(connector.getAlgorithm()));
    }

    /**
     * sync controller to model
     */
    private void syncController2Model() {
        if (customizedControl != null)
            customizedControl.syncController2Model();
        for (Option option : options) {
            try {
                option.setValue();
            } catch (Exception e) {
                Basic.caught(e);
            }
        }
    }
}