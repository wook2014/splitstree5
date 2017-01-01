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

package splitstree5.core.algorithms.distances2splits;

import jloda.util.CanceledException;
import jloda.util.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.DistancesNexusIO;
import splitstree5.io.nexus.SplitsNexusIO;
import splitstree5.io.nexus.TaxaNexusIO;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

import static junit.framework.Assert.assertEquals;

/**
 * test neighbor net
 * Created by huson on 12/30/16.
 */
public class NeighborNetTest {
    @Test
    public void testCompute() throws IOException, CanceledException, InterruptedException {
        TaxaBlock taxaBlock = new TaxaBlock();
        DistancesBlock distancesBlock = new DistancesBlock();
        try (NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/distances7-taxa.nex"))) {
            np.matchIgnoreCase("#nexus");
            TaxaNexusIO.parse(np, taxaBlock);
            DistancesNexusIO.parse(np, taxaBlock, distancesBlock, null);
            assertEquals(taxaBlock.getNtax(), 7);
            assertEquals(distancesBlock.getNtax(), 7);
        }

        final SplitsBlock splitsBlock = new SplitsBlock();

        final NeighborNet algorithm = new NeighborNet();
        algorithm.compute(new ProgressPercentage(), taxaBlock, distancesBlock, splitsBlock);

        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        TaxaNexusIO.write(w, taxaBlock);
        DistancesNexusIO.write(w, taxaBlock, distancesBlock, null);
        SplitsNexusIO.write(w, taxaBlock, splitsBlock, null);
        System.err.println(w.toString());
    }

}