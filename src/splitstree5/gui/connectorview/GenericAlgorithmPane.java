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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;
import jloda.util.Basic;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.ADataBlock;
import splitstree5.core.workflow.UpdateState;
import splitstree5.undo.UndoRedoManager;
import splitstree5.utils.Option;
import splitstree5.utils.OptionsAccessor;

import java.util.ArrayList;

/**
 * generates a generic algorithm pane
 * <p>
 * Created by huson on 1/8/17.
 */
public class GenericAlgorithmPane<P extends ADataBlock, C extends ADataBlock> extends AlgorithmPane {
    private UndoRedoManager undoManager;

    private final AConnector<P, C> connector;
    private final ArrayList<Option> options = new ArrayList<>();

    final SimpleBooleanProperty applicableProperty = new SimpleBooleanProperty();

    /**
     * constructor
     *
     * @param connector
     */
    public GenericAlgorithmPane(AConnector<P, C> connector) {
        this.connector = connector;
        options.addAll(OptionsAccessor.getAllOptions(connector.getAlgorithm()));
    }

    @Override
    public void setUndoManager(UndoRedoManager undoManager) {
        this.undoManager = undoManager;
    }

    /**
     * setup controller
     */
    public void setup() {
        final GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 5, 10, 5));
        grid.setHgap(20);
        getChildren().setAll(grid);

        int row = 1;
        try {
            for (final Option option : options) {
                final String text = Basic.fromCamelCase(option.getName());
                grid.add(new Label(text), 0, row);
                switch (option.getType().getTypeName()) {
                    case "boolean": {
                        final CheckBox control = new CheckBox("");
                        control.setSelected((boolean) option.getValue());
                        control.selectedProperty().addListener((observable, oldValue, newValue) -> {
                            undoManager.add(text, control.selectedProperty(), oldValue, newValue);
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
                                undoManager.add(text, control.textProperty(), oldValue, newValue);
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
                                undoManager.add(text, control.textProperty(), oldValue, newValue);
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
                                undoManager.add(text, control.textProperty(), oldValue, newValue);
                                option.holdValue(Basic.parseFloat(newValue));
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
                                    undoManager.add(text, choiceBox.valueProperty(), oldValue, newValue);
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
        applicableProperty.bind(undoManager.canUndoProperty());
    }

    /**
     * is algorithm applicable
     *
     * @return true, if parameters changed
     */
    public BooleanProperty applicableProperty() {
        return applicableProperty;
    }

    /**
     * sync model to controller
     */
    public void syncModel2Controller() {
        // not sure what we must do here...
    }

    /**
     * sync controller to model
     */
    public void syncController2Model() {
        for (Option option : options) {
            try {
                option.setValue();
            } catch (Exception e) {
                Basic.caught(e);
            }
        }
        connector.setState(UpdateState.INVALID);
    }
}
