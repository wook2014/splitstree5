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

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import jloda.fx.ExtendedFXMLLoader;
import jloda.util.Basic;

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

    public FontSelector() {
        this(Font.getDefault());
    }

    public FontSelector(Font font) {
        setButtonCell(new ListCell<>());

        fontValueProperty().addListener((c, o, n) -> {
            getButtonCell().setFont(new Font(n.getName(), 12));
            setValue(n.getFamily() + String.format(" %.0fpx", n.getSize()));
        });

        setFontValue(font);

        final ExtendedFXMLLoader<FontSelectorController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        controller = extendedFXMLLoader.getController();

        controller.getFontFamilyComboBox().getItems().addAll(new TreeSet<>(Font.getFamilies()));
        controller.getFontFamilyComboBox().getSelectionModel().select(Font.getDefault().getFamily());

        controller.getFontStyleComboBox().getItems().addAll("Bold", "Italic", "Bold Italic", "Regular");
        controller.getFontStyleComboBox().getSelectionModel().select("Regular");

        controller.getFontSizeComboBox().getItems().addAll("4", "6", "8", "10", "12", "14", "16", "20", "24", "32", "38", "48", "60");
        controller.getFontSizeComboBox().getSelectionModel().select("12");

        controller.getFontSizeSlider().setMin(4);
        controller.getFontSizeSlider().setMax(60);
        controller.getFontSizeSlider().setValue(12);

        controller.getFontSizeComboBox().getSelectionModel().selectedItemProperty().addListener((c, o, n) -> {
            if (!sizeChanging) {
                if (Basic.isDouble(n)) {
                    sizeChanging = true;
                    controller.getFontSizeSlider().setValue(Double.parseDouble(n));
                    Platform.runLater(this::updateFontValue);
                    sizeChanging = false;
                }
            }
        });

        controller.getFontSizeSlider().valueProperty().addListener((c, o, n) -> {
            if (!sizeChanging) {
                sizeChanging = true;
                controller.getFontSizeComboBox().getSelectionModel().select(String.valueOf(n.intValue()));
                updateFontValue();
                sizeChanging = false;
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
        controller.getFontFamilyComboBox().getSelectionModel().selectedItemProperty().addListener((c, o, n) -> updateFontValue());

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
        controller.getFontStyleComboBox().getSelectionModel().selectedItemProperty().addListener((c, o, n) -> updateFontValue());

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
        final Font font = getFontValue();
        controller.getFontFamilyComboBox().getSelectionModel().select(font.getFamily());
        controller.getFontSizeComboBox().getSelectionModel().select(String.format("%.0f", font.getSize()));
        controller.getFontStyleComboBox().getSelectionModel().select(font.getStyle());

        final Point2D location = localToScreen(getLayoutX(), getLayoutY());
        popup.show(getScene().getWindow(), location.getX(), location.getY());
    }

    private void updateFontValue() {
        System.err.println("UP");
        final String family = controller.getFontFamilyComboBox().getSelectionModel().getSelectedItem();
        if (controller.getFontFamilyComboBox().getItems().contains(family) && Basic.isDouble(controller.getFontSizeComboBox().getSelectionModel().getSelectedItem())) {
            final Double size = Math.max(1, Basic.parseDouble(controller.getFontSizeComboBox().getSelectionModel().getSelectedItem()));
            final FontPosture fontPosture = controller.getFontStyleComboBox().getSelectionModel().getSelectedItem().contains("Italic") ? FontPosture.ITALIC : FontPosture.REGULAR;
            final FontWeight fontWeight = controller.getFontStyleComboBox().getSelectionModel().getSelectedItem().contains("Bold") ? FontWeight.BOLD : FontWeight.NORMAL;
            final Font font = Font.font(family, fontWeight, fontPosture, size);
            setFontValue(font);
        }
    }

    @Override
    public void hide() {
        updateFontValue();
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
