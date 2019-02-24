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
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.util.converter.DoubleStringConverter;
import jloda.util.Basic;
import jloda.util.Single;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.TaxaBlock;
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
     * @param algorithm
     */
    public GenericAlgorithmPaneNext(TaxaBlock taxaBlock, Connector<P, C> connector, Algorithm<P, C> algorithm) {
        this.connector = connector;
        algorithm.setupBeforeDisplay(taxaBlock, connector.getParent().getDataBlock());
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

                final OptionValueType type = option.getOptionValueType();

                switch (type) {
                    case Boolean: {
                        final Single<Boolean> inUpdate = new Single<>(false);
                        final CheckBox control = new CheckBox("");

                        if (option.getToolTipText() != null)
                            control.setTooltip(new Tooltip(option.getToolTipText()));
                        control.setSelected(((Property<Boolean>) option.getProperty()).getValue());

                        control.selectedProperty().addListener((observable, oldValue, newValue) -> {
                            if (!inUpdate.get()) {
                                inUpdate.set(true);
                                option.getProperty().setValue(newValue);
                                inUpdate.set(false);
                            }
                        });
                        InvalidationListener invalidationListener = observable -> {
                            if (!inUpdate.get()) {
                                inUpdate.set(true);
                                control.setSelected(((Property<Boolean>) option.getProperty()).getValue());
                                inUpdate.set(false);
                            }
                        };
                        listeners.add(invalidationListener);
                        option.getProperty().addListener(new WeakInvalidationListener(invalidationListener));

                        grid.add(control, 1, row);
                        break;
                    }
                    case Double:
                    case Integer:
                    case Float: {
                        final Single<Boolean> inUpdate = new Single<>(false);
                        final TextField control = new TextField();
                        control.setPrefColumnCount(6);
                        control.addEventFilter(KeyEvent.ANY, e -> {
                            if (e.getCode() == KeyCode.Z && e.isShortcutDown()) {
                                e.consume();
                                control.getParent().fireEvent(e);
                            }
                        });
                        control.setText(OptionValueType.toStringType(type, option.getProperty().getValue()));

                        if (option.getToolTipText() != null)
                            control.setTooltip(new Tooltip(option.getToolTipText()));

                        control.textProperty().addListener((observable, oldValue, newValue) -> {
                            if (newValue.length() == 0)
                                newValue = "0";
                            if (OptionValueType.isType(type, newValue)) {
                                if (!inUpdate.get()) {
                                    inUpdate.set(true);
                                    option.getProperty().setValue(OptionValueType.parseType(type, newValue));
                                    inUpdate.set(false);
                                }
                            }
                        });

                        InvalidationListener invalidationListener = observable -> {
                            if (!inUpdate.get()) {
                                inUpdate.set(true);
                                control.setText(OptionValueType.toStringType(type, option.getProperty().getValue()));
                                inUpdate.set(false);
                            }
                        };
                        listeners.add(invalidationListener);
                        option.getProperty().addListener(new WeakInvalidationListener(invalidationListener));

                        grid.add(control, 1, row);
                        break;
                    }
                    case doubleArray: {
                        final Single<Boolean> inUpdate = new Single<>(false);

                        final double[] array = ((double[]) option.getProperty().getValue());
                        final int length = array.length;

                        final TextField[] controls = new TextField[length];
                        for (int i = 0; i < length; i++) {
                            final TextField control = new TextField();
                            controls[i] = control;
                            control.setPrefColumnCount(6);
                            control.setPrefWidth(60);
                            control.setTextFormatter(new TextFormatter<>(new DoubleStringConverter()));
                            control.setText(String.format("%.6f", array[i]));
                            if (option.getToolTipText() != null)
                                control.setTooltip(new Tooltip(option.getToolTipText()));

                            control.textProperty().addListener((observable, oldValue, newValue) -> {
                                if (newValue.length() == 0)
                                    newValue = "0";
                                if (OptionValueType.isType(type, newValue)) {
                                    if (!inUpdate.get()) {
                                        inUpdate.set(true);
                                        for (int j = 0; j < length; j++) {
                                            array[j] = Basic.parseDouble(controls[j].getText());
                                        }
                                        option.getProperty().setValue(array);
                                        inUpdate.set(false);
                                    }
                                }
                            });

                            final InvalidationListener invalidationListener = observable -> {
                                if (!inUpdate.get()) {
                                    inUpdate.set(true);
                                    final double[] values = ((double[]) option.getProperty().getValue());
                                    for (int j = 0; j < length; j++) {
                                        controls[j].setText(String.format("%.6f", values[j]));
                                    }
                                    inUpdate.set(false);
                                }
                            };
                            listeners.add(invalidationListener);
                            option.getProperty().addListener(new WeakInvalidationListener(invalidationListener));
                        }

                        final FlowPane flowPane = new FlowPane(controls);
                        flowPane.setMaxWidth(250);

                        grid.add(flowPane, 1, row);
                        break;
                    }
                    case String: {
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

                        InvalidationListener invalidationListener = observable -> {
                            if (!inUpdate.get()) {
                                inUpdate.set(true);
                                control.setText(option.getProperty().getValue().toString());
                                inUpdate.set(false);
                            }
                        };
                        listeners.add(invalidationListener);
                        option.getProperty().addListener(new WeakInvalidationListener(invalidationListener));

                        grid.add(control, 1, row);
                        break;
                    }
                    case Enum: {
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

                        InvalidationListener invalidationListener = observable -> {
                            if (!inUpdate.get()) {
                                inUpdate.set(true);
                                control.setValue(option.getProperty().getValue().toString());
                                inUpdate.set(false);
                            }
                        };
                        listeners.add(invalidationListener);
                        option.getProperty().addListener(new WeakInvalidationListener(invalidationListener));

                        grid.add(control, 1, row);
                        break;
                    }
                    default:
                        continue;
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
