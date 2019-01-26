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
package splitstree5.gui.datatab;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;
import jloda.util.Basic;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.undo.UndoManager;
import splitstree5.utils.Option;
import splitstree5.utils.OptionsAccessor;

import java.util.ArrayList;

/**
 * generates a generic data block format toolbar
 * <p>
 * Daniel Huson, 2/2018
 */
public class GenericDatablockFormatToolBar extends ToolBar {
    private final TaxaBlock taxaBlock;
    private final DataBlock dataBlock;
    private UndoManager undoManager;
    private final StringProperty text = new SimpleStringProperty();

    private final ArrayList<Option> options = new ArrayList<>();

    private final BooleanProperty applicable = new SimpleBooleanProperty(true);

    /**
     * constructor
     *
     * @param dataBlock
     */
    public GenericDatablockFormatToolBar(TaxaBlock taxaBlock, DataBlock dataBlock) {
        this.taxaBlock = taxaBlock;
        this.dataBlock = dataBlock;
        undoManager = new UndoManager();
        if (dataBlock.getFormat() != null)
            options.addAll(OptionsAccessor.getAllOptions(dataBlock.getFormat()));
        setup();
        updateText();
    }

    /**
     * setup controller
     */
    public void setup() {
        try {
            for (final Option option : options) {
                final String text = Basic.fromCamelCase(option.getName());
                final Label label = new Label(text);
                label.setTooltip(new Tooltip(text));
                getItems().add(label);
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
                        getItems().add(control);
                        break;
                    }
                    case "int": {
                        TextField control = new TextField();
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
                            if (Basic.isInteger(newValue)) {
                                undoManager.add(text, control.textProperty(), oldValue, newValue);
                                option.holdValue(Basic.parseInt(newValue));
                            }
                        });
                        if (option.getInfo() != null)
                            control.setTooltip(new Tooltip(option.getInfo()));
                        getItems().add(control);
                        break;
                    }
                    case "double": {
                        TextField control = new TextField();
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
                            if (Basic.isDouble(newValue)) {
                                undoManager.add(text, control.textProperty(), oldValue, newValue);
                                option.holdValue(Basic.parseDouble(newValue));
                            }
                        });
                        if (option.getInfo() != null)
                            control.setTooltip(new Tooltip(option.getInfo()));
                        getItems().add(control);
                        break;
                    }
                    case "float": {
                        TextField control = new TextField();
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
                            if (Basic.isFloat(newValue)) {
                                undoManager.add(text, control.textProperty(), oldValue, newValue);
                                option.holdValue(Basic.parseFloat(newValue));
                            }
                        });
                        if (option.getInfo() != null)
                            control.setTooltip(new Tooltip(option.getInfo()));
                        getItems().add(control);
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
                        getItems().add(control);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            Basic.caught(ex);
        }

        if (getItems().size() > 0) {
            final Button applyButton = new Button("Apply");
            applyButton.setOnAction((e) -> syncController2Model());
            applyButton.disableProperty().bind(undoManager.canUndoProperty().not());
            getItems().add(applyButton);
        }
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
        updateText();
    }


    public BooleanProperty applicableProperty() {
        return applicable;
    }

    public StringProperty textProperty() {
        return text;
    }

    public void updateText() {
        text.set(dataBlock.getDisplayText());
    }
}
