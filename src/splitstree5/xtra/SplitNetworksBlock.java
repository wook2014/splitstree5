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

/*
 *  Copyright (C) 2017 Daniel H. Huson
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

package splitstree5.xtra;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jloda.phylo.PhyloTree;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.ADataBlock;

/**
 * A networks block
 * Daniel Huson, 7.2017
 */
public class SplitNetworksBlock extends ADataBlock {
    private final ObservableList<PhyloTree> networks;
    private boolean partial = false; // are partial networks present?

    public SplitNetworksBlock() {
        networks = FXCollections.observableArrayList();
    }

    public SplitNetworksBlock(String name) {
        this();
        setName(name);
    }

    /**
     * shallow copy
     *
     * @param that
     */
    public void copy(SplitNetworksBlock that) {
        clear();
        networks.addAll(that.getNetworks());
        partial = that.isPartial();
    }

    @Override
    public void clear() {
        super.clear();
        networks.clear();
        partial = false;
        setShortDescription("");
    }

    @Override
    public int size() {
        return networks.size();
    }

    /**
     * access the trees
     *
     * @return trees
     */
    public ObservableList<PhyloTree> getNetworks() {
        return networks;
    }

    public int getNNetworks() {
        return networks.size();
    }

    public String getShortDescription() {
        return "Number of networks: " + getNetworks().size();
    }

    public boolean isPartial() {
        return partial;
    }

    public void setPartial(boolean partial) {
        this.partial = partial;
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
        return (getNNetworks() == 1 ? "a split network" : getNNetworks() + " split networks") + (isPartial() ? ", partial" : "");
    }
}
