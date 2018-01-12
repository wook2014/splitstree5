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

package splitstree5.core.datablocks;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jloda.phylo.PhyloTree;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToTrees;

/**
 * A trees block
 * Daniel Huson, 12/21/16.
 */
public class TreesBlock extends ADataBlock {
    private final ObservableList<PhyloTree> trees;
    private boolean partial = false; // are partial trees present?
    private boolean rooted = false; // are the trees explicitly rooted?

    public TreesBlock() {
        trees = FXCollections.observableArrayList();
    }

    public TreesBlock(String name) {
        this();
        setName(name);
        trees.addListener((InvalidationListener) observable -> setShortDescription(getInfo()));
    }

    /**
     * shallow copy
     *
     * @param that
     */
    public void copy(TreesBlock that) {
        clear();
        trees.addAll(that.getTrees());
        partial = that.isPartial();
        rooted = that.isRooted();
    }

    @Override
    public void clear() {
        super.clear();
        trees.clear();
        partial = false;
        rooted = false;
    }

    @Override
    public int size() {
        return trees.size();
    }

    /**
     * access the trees
     *
     * @return trees
     */
    public ObservableList<PhyloTree> getTrees() {
        return trees;
    }

    public int getNTrees() {
        return trees.size();
    }

    public String getShortDescription() {
        return "Number of trees: " + getTrees().size();
    }

    public boolean isPartial() {
        return partial;
    }

    public void setPartial(boolean partial) {
        this.partial = partial;
    }

    public boolean isRooted() {
        return rooted;
    }

    public void setRooted(boolean rooted) {
        this.rooted = rooted;
    }

    @Override
    public Class getFromInterface() {
        return IFromTrees.class;
    }

    @Override
    public Class getToInterface() {
        return IToTrees.class;
    }

    @Override
    public String getInfo() {
        return (getNTrees() == 1 ? "a tree" : getNTrees() + " trees") + (isPartial() ? ", partial" : "") + (isRooted() ? ", rooted" : "");
    }
}
