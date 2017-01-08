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

import splitstree5.gui.connectorview.CustomizedControl;

import java.util.List;

/**
 * A base class to help implement IOptionable
 * Created by huson on 8/1/17.
 */
public class OptionableBase implements IOptionable {
    private String name;
    private String shortDescription;

    public OptionableBase() {
    }

    public OptionableBase(String name) {
        this(name, null);
    }

    public OptionableBase(String name, String shortDescription) {
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
     * gets customized control pane to get and set options
     *
     * @return pane or null
     */
    public CustomizedControl getControl() {
        return null;
    }
}
