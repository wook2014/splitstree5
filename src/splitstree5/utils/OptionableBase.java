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

import splitstree5.core.misc.ANamed;
import splitstree5.gui.connectorview.AlgorithmPane;

import java.util.List;

/**
 * A base class to help implement IOptionable
 * Created by huson on 8/1/17.
 */
public class OptionableBase extends ANamed implements IOptionable {

    public OptionableBase() {
    }

    public OptionableBase(String name) {
        if (name != null)
            setName(name);
    }

    public OptionableBase(String name, String shortDescription) {
        if (name != null)
            setName(name);
        if (shortDescription != null)
            setShortDescription(shortDescription);
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
    public AlgorithmPane getControl() {
        return null;
    }
}
