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

package splitstree5.core.algorithms.filters;

import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.gui.connectorview.AlgorithmPane;
import splitstree5.gui.treefilterview.TreeFilterPane;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Trees filter
 * Created by huson on 12/31/16.
 */
public class TreeFilter extends Algorithm<TreesBlock, TreesBlock> implements IFromTrees, IToTrees {
    public enum Consensus {Strict, Majority, Loose, Network, None}

    private Consensus optionConsensusMethod = Consensus.None;
    private int optionNetworkDimension = 2;

    private final ArrayList<PhyloTree> enabledTrees = new ArrayList<>();
    private final ArrayList<PhyloTree> disabledTrees = new ArrayList<>();

    public TreeFilter() {
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock ignored, TreesBlock originalTrees, TreesBlock modifiedTrees) throws InterruptedException, CanceledException {
        modifiedTrees.getTrees().clear();

        final ArrayList<PhyloTree> list = new ArrayList<>();
        if (enabledTrees.size() == 0)
            list.addAll(originalTrees.getTrees());
        else
            list.addAll(enabledTrees);
        list.removeAll(disabledTrees);

        for (PhyloTree tree : list) {
            if (!getDisabledTrees().contains(tree) && originalTrees.getTrees().contains(tree)) {
                modifiedTrees.getTrees().add(tree);
            }
        }
    }

    public ArrayList<PhyloTree> getEnabledTrees() {
        return enabledTrees;
    }

    public ArrayList<PhyloTree> getDisabledTrees() {
        return disabledTrees;
    }

    public AlgorithmPane getControl() {
        try {
            return new TreeFilterPane(this);
        } catch (IOException e) {
            Basic.caught(e);
            return null;
        }
    }
}
