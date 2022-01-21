/*
 * TreeSelector.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.core.algorithms.filters;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.progress.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.gui.algorithmtab.AlgorithmPane;
import splitstree5.gui.algorithmtab.treeselector.TreeSelectorPane;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Tree selector
 * Daniel Huson, 1/2018
 */
public class TreeSelector extends Algorithm<TreesBlock, TreesBlock> implements IFromTrees, IToTrees, IFilter {
    private final IntegerProperty optionWhich = new SimpleIntegerProperty(1); // 1-based

    @Override
    public List<String> listOptions() {
        return Collections.singletonList("Which");
    }

    @Override
    public String getToolTip(String optionName) {
        if ("Which".equals(optionName)) {
            return "Which tree to use";
        }
        return optionName;
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock ignored, TreesBlock parent, TreesBlock child) throws CanceledException {
        setOptionWhich(Math.max(1, Math.min(parent.size(), optionWhich.get())));
        child.getTrees().add(parent.getTree(getOptionWhich()));
        child.setRooted(parent.isRooted());
        child.setPartial(parent.isPartial());
        setShortDescription("using tree " + getConnector() + " of " + parent.size() + " trees");
    }

    @Override
    public void clear() {
    }

    public int getOptionWhich() {
        return optionWhich.get();
    }

    public IntegerProperty optionWhichProperty() {
        return optionWhich;
    }

    public void setOptionWhich(int optionWhich) {
        this.optionWhich.set(optionWhich);
    }

    public AlgorithmPane getAlgorithmPane() {
        try {
            return new TreeSelectorPane(this);
        } catch (IOException e) {
            Basic.caught(e);
            return null;
        }
    }

    @Override
    public boolean isActive() {
        return optionWhich.get() > 0;
    }
}
