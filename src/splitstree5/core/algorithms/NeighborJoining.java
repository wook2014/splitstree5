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


import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.DummyTree;

/**
 * Neighbor joining algorithm
 * Created by huson on 12/11/16.
 */
public class NeighborJoining extends Algorithm<DistancesBlock, TreesBlock> {

    /**
     * compute the neighbor joining tree
     *
     * @throws InterruptedException
     */
    public void compute(TaxaBlock taxaBlock, DistancesBlock distances, TreesBlock trees) throws InterruptedException {
        System.err.println(getName() + ": not implemented, will wait for 4 seconds and then return a dummy tree...");

        System.err.println("Start waiting");
        Thread.sleep(4000);
        System.err.println("Done waiting");
        getChild().getTrees().setAll(new DummyTree(5, 6));
    }
}

