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
import jloda.util.Basic;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.gui.connectorview.AlgorithmPane;
import splitstree5.gui.treefilterview.TreeFilterPane;

import java.io.IOException;
import java.util.ArrayList;

/**
 * trees filter
 * Created by huson on 12/12/16.
 */
public class TreeFilter extends AConnector<TreesBlock, TreesBlock> {
    private final ArrayList<PhyloTree> enabledTrees = new ArrayList<>(); // these should be placed inside the algorithm?
    private final ArrayList<PhyloTree> disabledTrees = new ArrayList<>();

    /**
     * /**
     * constructor
     *
     * @param parent
     * @param child
     */
    public TreeFilter(TaxaBlock taxaBlock, ADataNode<TreesBlock> parent, ADataNode<TreesBlock> child) {
        super(taxaBlock, parent, child);
        enabledTrees.addAll(parent.getDataBlock().getTrees());
        parent.getDataBlock().getTrees().addListener((ListChangeListener<PhyloTree>) c -> {
            while (c.next()) {
                if (c.getRemovedSize() > 0)
                    enabledTrees.removeAll(c.getRemoved());
                if (c.getAddedSize() > 0)
                    enabledTrees.addAll(c.getAddedSubList());
            }
        });

        setAlgorithm(new Algorithm<TreesBlock, TreesBlock>("TreeFilter") {
            public void compute(ProgressListener progressListener, TaxaBlock ignored, TreesBlock originalTrees, TreesBlock modifiedTrees) {
                modifiedTrees.getTrees().clear();

                final ArrayList<PhyloTree> list = new ArrayList<>();
                if (enabledTrees.size() == 0)
                    list.addAll(originalTrees.getTrees());
                else
                    list.addAll(enabledTrees);
                list.removeAll(disabledTrees);

                for (PhyloTree tree : list) {
                    if (!TreeFilter.this.getDisabledTrees().contains(tree) && originalTrees.getTrees().contains(tree)) {
                        modifiedTrees.getTrees().add(tree);
                    }
                }
            }

            @Override
            public AlgorithmPane getControl() {
                try {
                    return new TreeFilterPane(TreeFilter.this);
                } catch (IOException e) {
                    Basic.caught(e);
                    return null;
                }
            }
        });
    }


    public ArrayList<PhyloTree> getEnabledTrees() {
        return enabledTrees;
    }

    public ArrayList<PhyloTree> getDisabledTrees() {
        return disabledTrees;
    }
}
