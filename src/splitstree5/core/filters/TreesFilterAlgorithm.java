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

package splitstree5.core.filters;

import javafx.collections.ListChangeListener;
import jloda.phylo.PhyloTree;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;

import java.util.ArrayList;

/**
 * Trees filter
 * Created by huson on 12/31/16.
 */
public class TreesFilterAlgorithm extends Algorithm<TreesBlock, TreesBlock> {
    public enum Consensus {Strict, Majority, Loose, Network, None}

    private Consensus optionConsensusMethod = Consensus.None;
    private int optionNetworkDimension = 2;

    private final ArrayList<PhyloTree> enabledTrees = new ArrayList<>();
    private final ArrayList<PhyloTree> disabledTrees = new ArrayList<>();

    public TreesFilterAlgorithm(TreesBlock parent) {
        enabledTrees.addAll(parent.getTrees());
        parent.getTrees().addListener((ListChangeListener<PhyloTree>) c -> {
            while (c.next()) {
                if (c.getRemovedSize() > 0)
                    enabledTrees.removeAll(c.getRemoved());
                if (c.getAddedSize() > 0)
                    disabledTrees.addAll(c.getAddedSubList());
            }
        });
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, TreesBlock parent, TreesBlock child) throws InterruptedException, CanceledException {
        System.err.println("TreesFilter: notImplemented");
        // todo: implement
        child.getTrees().addAll(parent.getTrees());
    }
}
