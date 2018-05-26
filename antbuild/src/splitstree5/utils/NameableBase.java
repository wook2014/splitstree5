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

import splitstree5.core.misc.ANamed;
import splitstree5.gui.algorithmtab.AlgorithmPane;

import java.util.List;

/**
 * A base class to help implement INameable
 * Daniel Huson, 8/1/17.
 */
public class NameableBase extends ANamed implements INameable {

    public NameableBase() {
    }

    public NameableBase(String name) {
        if (name != null)
            setName(name);
    }

    public NameableBase(String name, String shortDescription) {
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
    public AlgorithmPane getAlgorithmPane() {
        return null;
    }
}
