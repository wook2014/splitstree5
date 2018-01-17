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

package splitstree5.xtra.fontselector;

import javafx.scene.control.ComboBoxBase;
import javafx.scene.text.Font;
import jloda.fx.ExtendedFXMLLoader;

import java.util.ArrayList;

public class FontSelector extends ComboBoxBase<Font> {
    private static final ArrayList<String> recentFamilies = new ArrayList<>();

    private final FontSelectorController controller = null;

    public FontSelector() {
        this(Font.getDefault());
    }

    public FontSelector(Font font) {
        {
            final ExtendedFXMLLoader<FontSelectorController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
            //controller = extendedFXMLLoader.getController();
            //setContent(extendedFXMLLoader.getRoot());
        }

    }
}
