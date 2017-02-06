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

import org.junit.Test;
import splitstree5.core.algorithms.filters.SplitsFilter;

/**
 * test option
 * Created by huson on 12/31/16.
 */
public class OptionTest {

    @Test
    public void testGetAllOptions() throws Exception {
        SplitsFilter splitsFilter = new SplitsFilter();

        for (Option option : OptionsAccessor.getAllOptions(splitsFilter)) {
            System.err.println(option);
            switch (option.getType().getTypeName()) {
                case "boolean": {
                    option.holdValue(!(boolean) option.getValue());
                    break;
                }
                case "int": {
                    option.holdValue(-(int) option.getValue());
                    break;
                }
                case "double": {
                    option.holdValue(-(double) option.getValue());
                    break;
                }
                case "float": {
                    option.holdValue(-(float) option.getValue());
                    break;
                }
                case "String": {
                    option.holdValue("changed " + (String) option.getValue());
                    break;
                }
            }
            option.setValue();
        }
    }
}