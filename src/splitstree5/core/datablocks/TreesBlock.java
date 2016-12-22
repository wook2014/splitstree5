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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import splitstree5.core.misc.ITree;

/**
 * A trees block
 * Created by huson on 12/21/16.
 */
public class TreesBlock extends DataBlock {
    private final ObservableList<ITree> trees;

    public TreesBlock() {
        trees = FXCollections.observableArrayList();
    }

    public TreesBlock(String name) {
        this();
        setName(name);
    }

    /**
     * access the trees
     *
     * @return trees
     */
    public ObservableList<ITree> getTrees() {
        return trees;
    }

    public String getInfo() {
        return "Number of trees: " + getTrees().size();
    }
}
