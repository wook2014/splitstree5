/*
 *  Copyright (C) 2019 Daniel H. Huson
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
package splitstree5.gui.algorithmtab.next;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;
import jloda.util.Basic;
import jloda.util.Single;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.workflow.Connector;
import splitstree5.core.workflow.UpdateState;
import splitstree5.gui.algorithmtab.AlgorithmPane;
import splitstree5.undo.UndoManager;

import java.util.ArrayList;

/**
 * generates a generic algorithm pane
 * <p>
 * Daniel Huson, 2/2019
 */
public class GenericAlgorithmPaneNext<P extends DataBlock, C extends DataBlock> extends AlgorithmPane {
    private final ArrayList<InvalidationListener> listeners = new ArrayList<>();

    private final Connector<P, C> connector;
    private final ArrayList<OptionNext> options = new ArrayList<>();

    private final BooleanProperty applicable = new SimpleBooleanProperty(true);

    /**
     * constructor
     *
     * @param connector
     */
    public GenericAlgorithmPaneNext(Connector<P, C> connector) {
        this.connector = connector;
        options.addAll(OptionNext.getAllOptions(connector.getAlgorithm()));
    }

    /**
     * constructor
     *
     * @param algorithm
     */
    public GenericAlgorithmPaneNext(Connector<P, C> connector, Algorithm<P, C> algorithm) {
        this.connector = connector;
        options.addAll(OptionNext.getAllOptions(algorithm));
    }

    @Override
    public void setUndoManager(UndoManager undoManager) {
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
            for (final OptionNext option : options) {
                final String text = Basic.fromCamelCase(option.getName());
                final Label label = new Label(text);
                label.setTooltip(new Tooltip(option.getToolTipText()));
                grid.add(label, 0, row);

                if (option.getProperty().getValue() instanceof Boolean) {
                    final Single<Boolean> inUpdate = new Single<>(false);
                    final CheckBox control = new CheckBox("");
                    grid.add(control, 1, row);
                    if (option.getToolTipText() != null)
                        control.setTooltip(new Tooltip(option.getToolTipText()));

                    control.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        if (!inUpdate.get()) {
                            inUpdate.set(true);
                            option.getProperty().setValue(newValue);
                            inUpdate.set(false);
                        }
                    });
                    InvalidationListener invalidationListener = new InvalidationListener() {
                        @Override
                        public void invalidated(Observable observable) {
                            if (!inUpdate.get()) {
                                inUpdate.set(true);
                                control.setSelected(((Property<Boolean>) option.getProperty()).getValue());
                                inUpdate.set(false);
                            }
                        }
                    };
                    listeners.add(invalidationListener);
                    option.getProperty().addListener(new WeakInvalidationListener(invalidationListener));
                } else if (option.getProperty().getValue() instanceof Integer) {
                    final Single<Boolean> inUpdate = new Single<>(false);
                    TextField control = new TextField();
                    control.setPrefColumnCount(6);
                    control.addEventFilter(KeyEvent.ANY, e -> {
                        if (e.getCode() == KeyCode.Z && e.isShortcutDown()) {
                            e.consume();
                            control.getParent().fireEvent(e);
                        }
                    });
                    grid.add(control, 1, row);
                    if (option.getToolTipText() != null)
                        control.setTooltip(new Tooltip(option.getToolTipText()));

                    control.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
                    control.setText(option.getProperty().getValue().toString());

                    control.textProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue.length() == 0)
                            newValue = "0";
                        if (Basic.isInteger(newValue)) {
                            if (!inUpdate.get()) {
                                inUpdate.set(true);
                                option.getProperty().setValue(Basic.parseInt(newValue));
                                inUpdate.set(false);
                            }
                        }
                    });

                    InvalidationListener invalidationListener = new InvalidationListener() {
                        @Override
                        public void invalidated(Observable observable) {
                            if (!inUpdate.get()) {
                                inUpdate.set(true);
                                control.setText(option.getProperty().getValue().toString());
                                inUpdate.set(false);
                            }
                        }
                    };
                    listeners.add(invalidationListener);
                    option.getProperty().addListener(new WeakInvalidationListener(invalidationListener));
                } else if (option.getProperty().getValue() instanceof Double) {
                    final Single<Boolean> inUpdate = new Single<>(false);
                    TextField control = new TextField();
                    control.setPrefColumnCount(6);
                    control.addEventFilter(KeyEvent.ANY, e -> {
                        if (e.getCode() == KeyCode.Z && e.isShortcutDown()) {
                            e.consume();
                            control.getParent().fireEvent(e);
                        }
                    });
                    grid.add(control, 1, row);
                    if (option.getToolTipText() != null)
                        control.setTooltip(new Tooltip(option.getToolTipText()));

                    control.setTextFormatter(new TextFormatter<>(new DoubleStringConverter()));
                    control.setText(option.getProperty().getValue().toString());

                    control.textProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue.length() == 0)
                            newValue = "0";
                        if (Basic.isDouble(newValue)) {
                            if (!inUpdate.get()) {
                                inUpdate.set(true);
                                option.getProperty().setValue(Basic.parseDouble(newValue));
                                inUpdate.set(false);
                            }
                        }
                    });

                    InvalidationListener invalidationListener = new InvalidationListener() {
                        @Override
                        public void invalidated(Observable observable) {
                            if (!inUpdate.get()) {
                                inUpdate.set(true);
                                control.setText(option.getProperty().getValue().toString());
                                inUpdate.set(false);
                            }
                        }
                    };
                    listeners.add(invalidationListener);
                    option.getProperty().addListener(new WeakInvalidationListener(invalidationListener));
                } else if (option.getProperty().getValue() instanceof Float) {
                    final Single<Boolean> inUpdate = new Single<>(false);
                    TextField control = new TextField();
                    control.setPrefColumnCount(6);
                    control.addEventFilter(KeyEvent.ANY, e -> {
                        if (e.getCode() == KeyCode.Z && e.isShortcutDown()) {
                            e.consume();
                            control.getParent().fireEvent(e);
                        }
                    });
                    grid.add(control, 1, row);
                    if (option.getToolTipText() != null)
                        control.setTooltip(new Tooltip(option.getToolTipText()));

                    control.setTextFormatter(new TextFormatter<>(new FloatStringConverter()));
                    control.setText(option.getProperty().getValue().toString());

                    control.textProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue.length() == 0)
                            newValue = "0";
                        if (Basic.isFloat(newValue)) {
                            if (!inUpdate.get()) {
                                inUpdate.set(true);
                                option.getProperty().setValue(Basic.parseFloat(newValue));
                                inUpdate.set(false);
                            }
                        }
                    });

                    InvalidationListener invalidationListener = new InvalidationListener() {
                        @Override
                        public void invalidated(Observable observable) {
                            if (!inUpdate.get()) {
                                inUpdate.set(true);
                                control.setText(option.getProperty().getValue().toString());
                                inUpdate.set(false);
                            }
                        }
                    };
                    listeners.add(invalidationListener);
                    option.getProperty().addListener(new WeakInvalidationListener(invalidationListener));
                } else if (option.getProperty().getValue() instanceof String) {
                    final Single<Boolean> inUpdate = new Single<>(false);
                    TextField control = new TextField();
                    control.setPrefColumnCount(6);
                    control.addEventFilter(KeyEvent.ANY, e -> {
                        if (e.getCode() == KeyCode.Z && e.isShortcutDown()) {
                            e.consume();
                            control.getParent().fireEvent(e);
                        }
                    });
                    if (option.getToolTipText() != null)
                        control.setTooltip(new Tooltip(option.getToolTipText()));

                    control.setText(option.getProperty().getValue().toString());

                    control.textProperty().addListener((observable, oldValue, newValue) -> {
                        if (!inUpdate.get()) {
                            inUpdate.set(true);
                            option.getProperty().setValue(newValue);
                            inUpdate.set(false);
                        }
                    });

                    InvalidationListener invalidationListener = new InvalidationListener() {
                        @Override
                        public void invalidated(Observable observable) {
                            if (!inUpdate.get()) {
                                inUpdate.set(true);
                                control.setText(option.getProperty().getValue().toString());
                                inUpdate.set(false);
                            }
                        }
                    };
                    listeners.add(invalidationListener);
                    option.getProperty().addListener(new WeakInvalidationListener(invalidationListener));
                    grid.add(control, 1, row);
                } else if (option.getProperty().getValue() instanceof Enum) {
                    final Single<Boolean> inUpdate = new Single<>(false);

                    final ChoiceBox<String> control = new ChoiceBox<>();
                    control.getItems().addAll(option.getLegalValues());
                    control.setValue(option.getProperty().getValue().toString());
                    if (option.getToolTipText() != null)
                        control.setTooltip(new Tooltip(option.getToolTipText()));

                    control.valueProperty().addListener((observable, oldValue, newValue) -> {
                        if (!inUpdate.get()) {
                            inUpdate.set(true);
                            option.getProperty().setValue(option.getEnumValueForName(newValue)); // need to convert to enum
                            inUpdate.set(false);
                        }
                    });
                    grid.add(control, 1, row);

                    InvalidationListener invalidationListener = new InvalidationListener() {
                        @Override
                        public void invalidated(Observable observable) {
                            if (!inUpdate.get()) {
                                inUpdate.set(true);
                                control.setValue(option.getProperty().getValue().toString());
                                inUpdate.set(false);
                            }
                        }
                    };
                    listeners.add(invalidationListener);
                    option.getProperty().addListener(new WeakInvalidationListener(invalidationListener));
                }
                row++;
            }
        } catch (Exception ex) {
            Basic.caught(ex);
        }
    }

    public boolean hasOptions() {
        return options.size() > 0;
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
        connector.setState(UpdateState.INVALID);
    }
}
