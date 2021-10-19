/*
 * GenericAlgorithmPane.java Copyright (C) 2021. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package splitstree5.gui.algorithmtab;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;
import jloda.fx.undo.UndoManager;
import jloda.util.Basic;
import jloda.util.NumberUtils;
import jloda.util.StringUtils;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.workflow.Connector;
import splitstree5.core.workflow.UpdateState;
import splitstree5.utils.Option;
import splitstree5.utils.OptionsAccessor;

import java.util.ArrayList;

/**
 * generates a generic algorithm pane
 * <p>
 * Daniel Huson, 1/8/17.
 */
public class GenericAlgorithmPane<P extends DataBlock, C extends DataBlock> extends AlgorithmPane {
    private UndoManager undoManager;

    private final Connector<P, C> connector;
    private final ArrayList<Option> options = new ArrayList<>();

    private final BooleanProperty applicable = new SimpleBooleanProperty(true);

    /**
     * constructor
     *
     * @param connector
     */
    public GenericAlgorithmPane(Connector<P, C> connector) {
        this.connector = connector;
        options.addAll(OptionsAccessor.getAllOptions(connector.getAlgorithm()));
    }

    /**
     * constructor
     *
     * @param algorithm
     */
    public GenericAlgorithmPane(Connector<P, C> connector, Algorithm<P, C> algorithm) {
        this.connector = connector;
        options.addAll(OptionsAccessor.getAllOptions(algorithm));
    }

    @Override
    public void setUndoManager(UndoManager undoManager) {
        this.undoManager = undoManager;
    }

    public boolean hasOptions() {
        return options.size() > 0;
    }

    /**
     * setup controller
     */
    public void setup() {
        final GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 5, 10, 5));
        grid.setHgap(10);
        grid.setVgap(4);

        getChildren().setAll(grid);

        int row = 1;
        try {
            for (final Option option : options) {
				final String text = StringUtils.fromCamelCase(option.getName());
				final Label label = new Label(text);
                label.setTooltip(new Tooltip(text));
                grid.add(label, 0, row);
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
                        control.setPrefColumnCount(6);
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
							if (NumberUtils.isInteger(newValue)) {
								undoManager.add(text, control.textProperty(), oldValue, newValue);
								option.holdValue(NumberUtils.parseInt(newValue));
							}
                        });
                        if (option.getInfo() != null)
                            control.setTooltip(new Tooltip(option.getInfo()));
                        grid.add(control, 1, row);
                        break;
                    }
                    case "double": {
                        javafx.scene.control.TextField control = new TextField();
                        control.setPrefColumnCount(8);
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
							if (NumberUtils.isDouble(newValue)) {
								undoManager.add(text, control.textProperty(), oldValue, newValue);
								option.holdValue(NumberUtils.parseDouble(newValue));
							}
                        });
                        if (option.getInfo() != null)
                            control.setTooltip(new Tooltip(option.getInfo()));
                        grid.add(control, 1, row);
                        break;
                    }
                    case "float": {
                        javafx.scene.control.TextField control = new TextField();
                        control.setPrefColumnCount(6);
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
							if (NumberUtils.isFloat(newValue)) {
								undoManager.add(text, control.textProperty(), oldValue, newValue);
								option.holdValue(NumberUtils.parseFloat(newValue));
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
                            choiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                                undoManager.add(text, choiceBox.valueProperty(), oldValue, newValue);
                                option.holdValue(newValue);
                            });
                            control = choiceBox;
                        } else {
                            final TextField textField = new TextField();
                            textField.setPrefColumnCount(6);
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

    @Override
    public BooleanProperty applicableProperty() {
        return applicable;
    }

    /**
     * sync model to controller
     */
    public void syncModel2Controller() {
        // can't sync back
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
