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

package splitstree5.gui.formattab.fontselector;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import javafx.util.converter.IntegerStringConverter;
import jloda.fx.ExtendedFXMLLoader;
import jloda.util.Basic;
import jloda.util.ProgramProperties;

import java.util.TreeSet;

/**
 * font selection popup
 * Daniel Huson, 1.2018
 */
public class FontSelector extends ComboBox<String> {
    private final FontSelectorController controller;
    private final Popup popup;

    private final ObjectProperty<Font> fontValue = new SimpleObjectProperty<>();

    private boolean sizeChanging = false;
    private boolean fontChanging = false;

    public FontSelector() {
        this(ProgramProperties.getDefaultFont());
    }

    public FontSelector(Font font) {
        fontValueProperty().addListener((c, o, n) -> {
            setValue(n.getFamily() + String.format(" %.0fpx", n.getSize()));
        });

        final ExtendedFXMLLoader<FontSelectorController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        controller = extendedFXMLLoader.getController();

        controller.getFontFamilyComboBox().getItems().addAll(new TreeSet<>(Font.getFamilies()));

        controller.getFontStyleComboBox().getItems().addAll("Bold", "Italic", "Bold Italic", "Regular");

        controller.getFontSizeComboBox().getItems().addAll(4, 6, 8, 10, 12, 14, 16, 20, 24, 32, 38, 48, 60);

        controller.getFontSizeSlider().setMin(4);
        controller.getFontSizeSlider().setMax(60);

        controller.getFontSizeComboBox().setConverter(new IntegerStringConverter());
        controller.getFontSizeComboBox().valueProperty().addListener((c, o, n) -> {
            if (!sizeChanging) {
                try {
                    sizeChanging = true;
                    controller.getFontSizeSlider().setValue(n);
                } finally {
                    sizeChanging = false;
                }
            }
        });

        controller.getFontSizeSlider().valueProperty().addListener((c, o, n) -> {
            if (!sizeChanging) {
                try {
                    sizeChanging = true;
                    controller.getFontSizeComboBox().setValue(Math.round(n.floatValue()));
                } finally {
                    sizeChanging = false;
                }
            }
        });

        controller.getFontFamilyComboBox().getEditor().setOnKeyReleased((e) -> {
            if (e.getCode().isLetterKey() || e.getCode() == KeyCode.SPACE) {
                final String currentText = controller.getFontFamilyComboBox().getEditor().getText().toLowerCase();
                if (currentText.length() > 0) {
                    for (String item : controller.getFontFamilyComboBox().getItems()) {
                        if (item.toLowerCase().startsWith(currentText)) {
                            controller.getFontFamilyComboBox().getEditor().setText(item);
                            controller.getFontFamilyComboBox().getEditor().selectRange(currentText.length(), item.length());
                            break;
                        }
                    }
                }
            }
        });

        controller.getFontStyleComboBox().getEditor().setOnKeyReleased((e) -> {
            if (e.getCode().isLetterKey() || e.getCode() == KeyCode.SPACE) {
                final String currentText = controller.getFontStyleComboBox().getEditor().getText().toLowerCase();
                if (currentText.length() > 0) {
                    for (String item : controller.getFontStyleComboBox().getItems()) {
                        if (item.toLowerCase().startsWith(currentText)) {
                            controller.getFontStyleComboBox().getEditor().setText(item);
                            controller.getFontStyleComboBox().getEditor().selectRange(currentText.length(), item.length());
                            break;
                        }
                    }
                }
            }
        });

        setDefaultFont(font);

        controller.getFontFamilyComboBox().getSelectionModel().selectedItemProperty().addListener((c, o, n) -> updateFontValue());
        controller.getFontStyleComboBox().getSelectionModel().selectedItemProperty().addListener((c, o, n) -> updateFontValue());
        controller.getFontSizeComboBox().getSelectionModel().selectedItemProperty().addListener((c, o, n) -> updateFontValue());

        popup = new Popup();
        popup.getContent().add(extendedFXMLLoader.getRoot());
        final DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.LIGHTGRAY);
        dropShadow.setOffsetX(2);
        dropShadow.setOffsetY(2);
        extendedFXMLLoader.getRoot().setEffect(dropShadow);
    }

    @Override
    public void show() {
        Event.fireEvent(this, new Event(ComboBoxBase.ON_SHOWING));
        final Point2D location = localToScreen(getLayoutX(), getLayoutY());
        popup.show(getScene().getWindow(), location.getX(), location.getY());
    }

    public void setDefaultFont(Font font) {
        if (!fontChanging) {
            try {
                fontChanging = true;
                setFontValue(font);
                controller.getFontFamilyComboBox().setValue(font.getFamily());
                controller.getFontSizeComboBox().setValue((int) Math.round(font.getSize()));
                controller.getFontStyleComboBox().setValue(font.getStyle());
            } finally {
                fontChanging = false;
            }
        }
    }

    private void updateFontValue() {
        if (!fontChanging) {
            try {
                fontChanging = true;

                final String family = controller.getFontFamilyComboBox().getSelectionModel().getSelectedItem();
                if (controller.getFontFamilyComboBox().getItems().contains(family)) {
                    final int size = Math.max(1, controller.getFontSizeComboBox().getValue());
                    final String style = controller.getFontStyleComboBox().getSelectionModel().getSelectedItem();
                    final FontPosture fontPosture = style.toLowerCase().contains("italic") || style.toLowerCase().contains("oblique") ? FontPosture.ITALIC : FontPosture.REGULAR;

                    FontWeight fontWeight = null; // one of the words should define the weight, find it
                    for (String word : style.split("\\s+")) {
                        fontWeight = FontWeight.findByName(Basic.capitalize(word, true));
                        if (fontWeight != null)
                            break;
                    }
                    final Font font = Font.font(family, fontWeight, fontPosture, size);
                    setFontValue(font);
                }
            } finally {
                fontChanging = false;
            }
        }
    }

    @Override
    public void hide() {
        Event.fireEvent(this, new Event(ComboBoxBase.ON_HIDING));
        popup.hide();
    }

    public Font getFontValue() {
        return fontValue.get();
    }

    public ObjectProperty<Font> fontValueProperty() {
        return fontValue;
    }

    public void setFontValue(Font fontValue) {
        this.fontValue.set(fontValue);
    }
}
