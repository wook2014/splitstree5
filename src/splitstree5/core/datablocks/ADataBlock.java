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

package splitstree5.core.datablocks;

import jloda.util.Basic;
import splitstree5.core.misc.Named;

/**
 * A data block
 * Created by huson on 12/21/16.
 */
abstract public class ADataBlock extends Named {

    public ADataBlock newInstance() {
        try {
            return getClass().newInstance();
        } catch (Exception e) {
            Basic.caught(e);
            return null;
        }
    }

    /**
     * gets the size of this data block
     *
     * @return size
     */
    abstract public int size();

    /**
     * clear
     */
    abstract public void clear();
}
