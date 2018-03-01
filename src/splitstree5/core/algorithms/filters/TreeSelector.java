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

package splitstree5.core.algorithms.filters;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;

/**
 * Tree selector
 * Daniel Huson, 1/2018
 */
public class TreeSelector extends Algorithm<TreesBlock, TreesBlock> implements IFromTrees, IToTrees, IFilter {
    private final IntegerProperty optionSelected = new SimpleIntegerProperty(1);
    private boolean active;

    @Override
    public void compute(ProgressListener progress, TaxaBlock ignored, TreesBlock parent, TreesBlock child) throws CanceledException {
        int which = Math.max(0, Math.min(parent.size() - 1, optionSelected.get() - 1));
        child.getTrees().add(parent.getTrees().get(which));
        child.setRooted(parent.isRooted());
        child.setPartial(parent.isPartial());
        active = parent.getNTrees() > 1;
        setShortDescription("using one of " + parent.size() + " trees");
    }

    @Override
    public void clear() {
    }

    @Override
    public String getShortDescription() {
        return "using tree " + optionSelected.get();
    }

    public int getOptionSelected() {
        return optionSelected.get();
    }

    public IntegerProperty optionSelectedProperty() {
        return optionSelected;
    }

    public void setOptionSelected(int optionSelected) {
        this.optionSelected.set(optionSelected);
    }

    /*
    public AlgorithmPane getAlgorithmPane() {
        try {
            return new TreeSelectorPane(this);
        } catch (IOException e) {
            Basic.caught(e);
            return null;
        }
    }
    */

    @Override
    public boolean isActive() {
        return active;
    }
}
