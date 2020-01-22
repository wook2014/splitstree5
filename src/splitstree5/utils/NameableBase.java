/*
 *  NameableBase.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.utils;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import jloda.util.Basic;

import java.util.List;

/**
 * A base class for names and descriptions
 * Daniel Huson, 8/1/17.
 */
public class NameableBase {
    private StringProperty name;
    private StringProperty shortDescription;

    private StringProperty title;

    /**
     * constructor
     */
    public NameableBase() {
    }

    /**
     * constructor
     *
     * @param name
     * @param shortDescription
     */
    public NameableBase(String name, String shortDescription) {
        if (name != null)
            setName(name);
        if (shortDescription != null)
            setShortDescription(shortDescription);
    }


    /**
     * gets the name. E.g., the name associated with the Neighbor-joining Algorithm class is NeighborJoining
     *
     * @return name
     */
    public String getName() {
        return nameProperty().get();
    }

    public StringProperty nameProperty() {
        if (name == null)
            name = new SimpleStringProperty(Basic.getShortName(this.getClass()));
        return name;
    }

    public void setName(String name) {
        nameProperty().set(name);
    }

    public StringProperty shortDescriptionProperty() {
        if (shortDescription == null)
            shortDescription = new SimpleStringProperty(Basic.fromCamelCase(Basic.getShortName(this.getClass())));
        return shortDescription;
    }

    /**
     * gets a short description e.g. for a tooltip
     *
     * @return short description
     */
    public String getShortDescription() {
        return shortDescriptionProperty().get();
    }

    /**
     * sets a short description
     *
     * @param shortDescription
     */
    public void setShortDescription(String shortDescription) {
        if (shortDescription == null)
            shortDescription = Basic.fromCamelCase(Basic.getShortName(this.getClass()));
        shortDescriptionProperty().set(shortDescription);
    }

    public StringProperty titleProperty() {
        if (title == null)
            title = new SimpleStringProperty();
        return title;
    }

    /**
     * gets the title of a block. This is used during IO
     *
     * @return title
     */
    public String getTitle() {
        return titleProperty().get();
    }

    /**
     * sets the title of a block. This is used during IO
     *
     * @param title
     */
    public void setTitle(String title) {
        titleProperty().set(title);
    }

    /**
     * clear title and links
     */
    public void clear() {
        title = null;
        setShortDescription(Basic.fromCamelCase(Basic.getShortName(this.getClass())));
    }

    /**
     * list options in desired order of appearance
     *
     * @return list of options or null
     */
    public List<String> listOptions() {
        return null;
    }

    /**
     * gets a tool tip for the named option
     *
     * @param optionName
     * @return tool tip
     */
    public String getToolTip(String optionName) {
        return optionName;
    }

}
