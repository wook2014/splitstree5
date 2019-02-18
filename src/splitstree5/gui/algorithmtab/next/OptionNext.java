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

import javafx.beans.property.Property;

import java.util.ArrayList;

/**
 * A property-based option
 * Daniel Husobn, 2.2019
 */
public class OptionNext<T> {
    private final Property<T> property;
    private final String name;
    private String toolTipText;


    private final ArrayList<String> legalValues;


    /**
     * constructs an option
     *
     * @param property
     * @param name
     * @param toolTipText
     */
    OptionNext(Property<T> property, String name, String toolTipText) {
        this.property = property;
        this.name = name;
        this.toolTipText = toolTipText;

        if (property.getValue().getClass().isEnum()) {
            legalValues = new ArrayList<>();
            for (Object value : ((Enum) property.getValue()).getClass().getEnumConstants()) {
                legalValues.add(value.toString());
            }
        } else
            legalValues = null;
    }

    public Property<T> getProperty() {
        return property;
    }

    public String getName() {
        return name;
    }

    public String getToolTipText() {
        return toolTipText;
    }

    public void setToolTipText(String toolTipText) {
        this.toolTipText = toolTipText;
    }

    public ArrayList<String> getLegalValues() {
        return legalValues;
    }

    public Object getEnumValueForName(String name) {
        for (Object value : ((Enum) property.getValue()).getClass().getEnumConstants()) {
            if (value.toString().equalsIgnoreCase(name))
                return value;
        }
        return null;
    }
}
