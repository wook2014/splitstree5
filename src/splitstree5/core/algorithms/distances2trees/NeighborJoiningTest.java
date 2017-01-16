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

package splitstree5.core.algorithms.distances2trees;

import jloda.phylo.PhyloTree;
import org.junit.Test;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.Taxon;

/**
 * nj test
 * Created by huson on 1/12/17.
 */
public class NeighborJoiningTest {

    @Test
    public void runAlgorithm() {
        String[] names = {"a", "b", "c", "d", "e", "f"};
        Taxon[] taxons = new Taxon[6];
        TaxaBlock taxaBlock = new TaxaBlock();
        DistancesBlock distancesBlock = new DistancesBlock();

        for (int i = 0; i < names.length; i++) {
            taxons[i] = new Taxon();
            taxons[i].setName(names[i]);
            taxaBlock.getTaxa().add(taxons[i]);
        }

        double[][] dist =
                {{0, 5, 4, 7, 6, 8},
                        {5, 0, 7, 10, 9, 11},
                        {4, 7, 0, 7, 6, 8},
                        {7, 10, 7, 0, 5, 9},
                        {6, 9, 6, 5, 0, 8},
                        {8, 11, 8, 9, 8, 0}};

        distancesBlock.set(dist);

        // TESTING

        // todo: make this a proper test that compares the result against a known result

        PhyloTree tree = NeighborJoining.computeNJTree(taxaBlock, distancesBlock);
        //PhyloTree tree = new PhyloTree();
        System.out.println("output: " + tree.toString());

    }
}