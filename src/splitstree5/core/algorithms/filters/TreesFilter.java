/*
 *  Copyright (C) 2019 Daniel H. Huson
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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.gui.algorithmtab.AlgorithmPane;
import splitstree5.gui.algorithmtab.treefilterview.TreeFilterPane;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Trees filter
 * Daniel Huson, 12/31/16.
 */
public class TreesFilter extends Algorithm<TreesBlock, TreesBlock> implements IFromTrees, IToTrees, IFilter {
    private final ObservableList<String> optionEnabledTrees = FXCollections.observableArrayList();
    private final ObservableList<String> OptionDisabledTrees = FXCollections.observableArrayList();

    @Override
    public String getToolTip(String optionName) {
        switch (optionName) {
            case "EnabledTrees":
                return "List of trees currently enabled";
            case "DisabledTrees":
                return "List of trees currently disabled";
            default:
                return optionName;
        }
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, TreesBlock parent, TreesBlock child) throws CanceledException {
        if (optionEnabledTrees.size() == 0 && OptionDisabledTrees.size() == 0) // nothing has been explicitly set, copy everything
        {
            progress.setMaximum(1);
            child.getTrees().setAll(parent.getTrees());
            child.setPartial(parent.isPartial()); // if trees subselected, recompute this!
            progress.incrementProgress();
        } else {
            final int totalTaxa = taxaBlock.getNtax();
            boolean partial = false;

            progress.setMaximum(optionEnabledTrees.size());
            final Map<String, PhyloTree> name2tree = new HashMap<>();
            for (PhyloTree tree : parent.getTrees()) {
                name2tree.put(tree.getName(), tree);
            }
            for (String name : optionEnabledTrees) {
                if (!OptionDisabledTrees.contains(name)) {
                    final PhyloTree tree = name2tree.get(name);
                    child.getTrees().add(tree);
                    if (tree.getNumberOfTaxa() != totalTaxa)
                        partial = true;
                    progress.incrementProgress();
                }
            }
            child.setPartial(partial);
        }
        if (OptionDisabledTrees.size() == 0)
            setShortDescription("using all " + parent.size() + " trees");
        else
            setShortDescription("using " + optionEnabledTrees.size() + " of " + parent.size() + " trees");

        child.setRooted(parent.isRooted());
    }

    @Override
    public void clear() {
        optionEnabledTrees.clear();
        OptionDisabledTrees.clear();
    }

    public ObservableList<String> getOptionEnabledTrees() {
        return optionEnabledTrees;
    }

    public ObservableList<String> getOptionDisabledTrees() {
        return OptionDisabledTrees;
    }

    public AlgorithmPane getAlgorithmPane() {
        try {
            return new TreeFilterPane(this);
        } catch (IOException e) {
            Basic.caught(e);
            return null;
        }
    }

    @Override
    public boolean isActive() {
        return OptionDisabledTrees.size() > 0;
    }
}
