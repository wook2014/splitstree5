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

package splitstree5.core.misc;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import jloda.util.Basic;
import jloda.util.Pair;

/**
 * a class with name and short description
 * Daniel Huson, 1/16/17.
 */
public class ANamed {
    private StringProperty name;
    private StringProperty shortDescription;

    private String title;
    private final ObjectProperty<Pair<String, String>> link = new SimpleObjectProperty<>();

    public ANamed() {
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

    /**
     * gets the title of a block. This is used during IO
     *
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * sets the title of a block. This is used during IO
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * link used in parsing a complete SplitsTree5 download
     *
     * @return link property
     */
    public ObjectProperty<Pair<String, String>> linkProperty() {
        return link;
    }

    /**
     * clear title and links
     */
    public void clear() {
        title = null;
        link.set(null);
        setShortDescription(Basic.fromCamelCase(Basic.getShortName(this.getClass())));
    }
}
