/*
 *  Copyright (C) 2018 Daniel H. Huson
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

package jloda.fx;

import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

/**
 * some font utilities
 * Daniel Huson, 3.2018
 */
public class FontUtils {
    /**
     * guess the weight of a font
     *
     * @param font
     * @return the weight
     */
    public static FontWeight getWeight(Font font) {
        return getWeight(font.getStyle());
    }

    /**
     * get the posture
     *
     * @param font
     * @return posture
     */
    public static FontPosture getPosture(Font font) {
        return getPosture(font.getStyle());
    }

    /**
     * guess the weight
     *
     * @param style
     * @return weight
     */
    public static FontWeight getWeight(String style) {
        for (String word : style.split("\\s+")) {
            FontWeight weight = FontWeight.findByName(word.toUpperCase());
            if (weight != null)
                return weight;
        }
        return FontWeight.NORMAL;
    }

    /**
     * get the posture
     *
     * @param style
     * @return posture
     */
    public static FontPosture getPosture(String style) {
        for (String word : style.split("\\s+")) {
            FontPosture posture = FontPosture.findByName(word.toUpperCase());
            if (posture != null)
                return posture;
        }
        return FontPosture.REGULAR;
    }

    /**
     * creates a font for the given family, style and size
     *
     * @param family
     * @param style
     * @param size
     * @return new font
     */
    public static Font font(String family, String style, double size) {
        return Font.font(family, getWeight(style), getPosture(style), size);
    }

    /**
     * creates the same font with a different size
     *
     * @param font
     * @param size
     * @return new font of given size
     */
    public static Font font(Font font, double size) {
        if (size > 0 && size != font.getSize())
            return Font.font(font.getFamily(), getWeight(font), getPosture(font), size);
        else
            return font;
    }
}
