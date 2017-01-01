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

package splitstree5.utils;

import javafx.scene.layout.Pane;

import java.util.List;

/**
 * A class that supports setting and getting of options by reflection (like a bean)
 * <p>
 * Provide getOptionNAME and setOptionNAME (or isOptionNAME) to provide access to option NAME
 * <p>
 * Optionally, provide a method getInfoNAME to provide a tooltip description for the option NAME
 * <p>
 * Optionally, provide a method getLegalValuesNAME to provide an array of all legal values (as strings)
 * <p>
 * Optionally, provide a method called List\<String\> listOptions () to list the option names in the order that they should be displayed
 * <p>
 * Optionally, provide a method called getPane() that returns a JavaFX pane that can be used to get and set the options
 * <p>
 * Created by huson on 1/1/17.
 */
public class Optionable {
    private String name;
    private String shortDescription;

    public Optionable() {
    }

    public Optionable(String name) {
        this(name, null);
    }

    public Optionable(String name, String shortDescription) {
        this.name = name;
        this.shortDescription = shortDescription;
    }

    /**
     * gets the name
     *
     * @return name or null
     */
    public String getName() {
        return name;
    }

    /**
     * sets the name
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
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

    /**
     * list options in desired order of appearance
     *
     * @return list of options or null
     */
    public List<String> listOptions() {
        return null;
    }

    /**
     * gets JavaFX pane to get and set options
     *
     * @return pane or null
     */
    public Pane getPane() {
        return null;
    }
}
