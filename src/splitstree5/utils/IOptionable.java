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

package splitstree5.utils;

import splitstree5.gui.algorithmtab.AlgorithmPane;

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
 * Optionally, provide a method called getControl() that returns a JavaFX pane that can be used to get and set the options
 * <p>
 * Created by huson on 8/1/17.
 */
public interface IOptionable {
    /**
     * gets the name
     *
     * @return name or null
     */
    String getName();

    /**
     * sets the name
     *
     * @param name or null
     */
    void setName(String name);


    /**
     * gets a short description e.g. for a tooltip
     *
     * @return short description
     */
    String getShortDescription();

    /**
     * sets a short description e.g. for a tooltip
     *
     * @param shortDescription
     */
    void setShortDescription(String shortDescription);

    /**
     * list options in desired order of appearance
     *
     * @return list of options or null
     */
    List<String> listOptions();

    /**
     * gets JavaFX pane to get and set options. This is optional, if it is not given, will use reflection to buold view
     *
     * @return pane or null
     */
    AlgorithmPane getControl();
}
