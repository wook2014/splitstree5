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

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.Taxon;

import static junit.framework.Assert.assertEquals;

/**
 * nj test
 * Created by huson on 1/12/17.
 */
public class NeighborJoiningTest {

    private NeighborJoining nj = new NeighborJoining();

    @Test
    public void testCompute() throws Exception{

        String output = "(a:1.0,b:4.0,(c:2.0,(f:5.0,(d:3.0,e:2.0):1.0):1.0):1.0)";

        String[] names = {"a", "b", "c", "d", "e", "f"};
        Taxon[] taxons = new Taxon[6];
        TaxaBlock taxaBlock = new TaxaBlock();
        for (int i = 0; i < names.length; i++) {
            taxons[i] = new Taxon();
            taxons[i].setName(names[i]);
            taxaBlock.getTaxa().add(taxons[i]);
        }
        DistancesBlock distancesBlock = new DistancesBlock();
        double[][] dist = {{0, 5, 4, 7, 6, 8},
                {5, 0, 7, 10, 9, 11},
                {4, 7, 0, 7, 6, 8},
                {7, 10, 7, 0, 5, 9},
                {6, 9, 6, 5, 0, 8},
                {8, 11, 8, 9, 8, 0}};
        distancesBlock.set(dist);

        ProgressListener pl = new ProgressPercentage();
        TreesBlock treesBlock = new TreesBlock();

        nj.compute(pl, taxaBlock, distancesBlock, treesBlock);
        System.out.println("output: " + treesBlock.getTrees().get(0).toString());
        assertEquals(output, treesBlock.getTrees().get(0).toString());
    }
}