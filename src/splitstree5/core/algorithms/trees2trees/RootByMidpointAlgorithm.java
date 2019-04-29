/*
 *  RootByMidpointAlgorithm.java Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.core.algorithms.trees2trees;


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import jloda.phylo.PhyloTree;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.filters.IFilter;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.utils.RerootingUtils;

import java.util.Collections;
import java.util.List;

/**
 * tree rerooting by midpoint
 * Daniel Huson, 5.2018
 */
public class RootByMidpointAlgorithm extends Algorithm<TreesBlock, TreesBlock> implements IFromTrees, IToTrees, IFilter {
    private final BooleanProperty optionUseMidpoint = new SimpleBooleanProperty(true);

    @Override
    public List<String> listOptions() {
        return Collections.singletonList("UseMidpoint");
    }

    @Override
    public String getToolTip(String optionName) {
        if ("UseMidpoint".equals(optionName)) {
            return "Determine whether to apply midpoint rooting";
        }
        return optionName;
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxa, TreesBlock parent, TreesBlock child) throws InterruptedException, CanceledException {
        if (!isOptionUseMidpoint()) // nothing has been explicitly set, copy everything
        {
            child.getTrees().setAll(parent.getTrees());
        } else { // reroot using outgroup
            child.getTrees().clear();

            for (PhyloTree orig : parent.getTrees()) {
                final PhyloTree tree = new PhyloTree();
                tree.copy(orig);
                if (tree.getRoot() == null) {
                    tree.setRoot(tree.getFirstNode());
                    tree.redirectEdgesAwayFromRoot();
                }
                RerootingUtils.rerootByMidpoint(tree);
                child.getTrees().add(tree);
            }
        }

        setShortDescription(isOptionUseMidpoint() ? "using midpoint rooting" : "not using midpoint rooting");
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public boolean isActive() {
        return isOptionUseMidpoint();
    }

    public boolean isOptionUseMidpoint() {
        return optionUseMidpoint.get();
    }

    public BooleanProperty optionUseMidpointProperty() {
        return optionUseMidpoint;
    }

    public void setOptionUseMidpoint(boolean optionUseMidpoint) {
        this.optionUseMidpoint.set(optionUseMidpoint);
    }
}
