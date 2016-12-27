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

package splitstree5.core.algorithms;

import javafx.beans.property.SimpleObjectProperty;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;

/**
 * implements consensus splits
 * Created by huson on 12/11/16.
 */
public class ConsensusSplits extends Algorithm<TreesBlock, SplitsBlock> {
    public enum Consensus {STRICT, LOOSE, MAJORITY, NETWORK, ALL}

    private final SimpleObjectProperty<Consensus> consensus = new SimpleObjectProperty<>(Consensus.ALL);

    public Consensus getConsensus() {
        return consensus.get();
    }

    public SimpleObjectProperty<Consensus> consensusProperty() {
        return consensus;
    }

    public void setConsensus(Consensus consensus) {
        this.consensus.set(consensus);
    }

    public void compute(TaxaBlock taxaBlock, TreesBlock treesBlock, SplitsBlock splitsBlock) {
        System.err.println(getName() + ": not implemented");
    }
}