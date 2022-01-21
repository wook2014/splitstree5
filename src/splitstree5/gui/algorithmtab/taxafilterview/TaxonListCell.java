/*
 * TaxonListCell.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.gui.algorithmtab.taxafilterview;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import splitstree5.core.misc.Taxon;

/**
 * taxon list cell
 * Daniel Huson 12/2016
 */
public class TaxonListCell {
    /**
     * forListView a taxon list view cell
     *
     * @return
     */
    public static Callback<ListView<Taxon>, ListCell<Taxon>> forListView() {
        return TextFieldListCell.forListView(new TaxonConverter());
    }

    private static class TaxonConverter extends StringConverter<Taxon> {
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString(Taxon value) {
            return (value != null) ? value.getName() : "";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Taxon fromString(String value) {
            return new Taxon(value);
        }
    }

}
