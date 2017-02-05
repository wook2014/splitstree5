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

package splitstree5.core.misc;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import jloda.util.Basic;

/**
 * a class with name and short description
 * Created by huson on 1/16/17.
 */
public class ANamed {
    protected final StringProperty name = new SimpleStringProperty();
    protected String shortDescription;

    public ANamed() {
        name.set(Basic.getShortName(this.getClass()));
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    /**
     * gets a short description e.g. for a tooltip
     *
     * @return short description
     */
    public String getShortDescription() {
        return shortDescription;
    }

    /**
     * sets a short description
     *
     * @param shortDescription
     */
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }
}
