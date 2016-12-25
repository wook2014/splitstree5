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

import jloda.util.Basic;

/**
 * A undo-redoable actiom
 * Created by huson on 12/25/16.
 */

abstract public class UndoableChange {
    private String name;

    /**
     * default constructor
     */
    public UndoableChange() {
        name = Basic.fromCamelCase(Basic.getShortName(this.getClass()));
    }

    /**
     * named constructor
     *
     * @param name
     */
    public UndoableChange(String name) {
        this.name = name;
    }

    /**
     * get name to display
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * set name to display
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * undo action
     */
    abstract public void undo();

    /**
     * redo action
     */
    abstract public void redo();
}

