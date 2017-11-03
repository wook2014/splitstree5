/*
 *  Copyright (C) 2017 Daniel H. Huson
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

package splitstree5.core.connectors;

import org.junit.Test;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;

/**
 * test
 * Created by huson on 1/31/17.
 */
public class AConnectorTest {
    @Test
    public void getAllAlgorithms() throws Exception {
        AConnector<DistancesBlock, SplitsBlock> aConnector = new AConnector<>(new TaxaBlock(), new ADataNode<>(new DistancesBlock()), new ADataNode<>(new SplitsBlock()));

        for (Algorithm<DistancesBlock, SplitsBlock> algorithm : aConnector.getAllAlgorithms()) {
            System.err.println(algorithm.getName());
        }
    }
}