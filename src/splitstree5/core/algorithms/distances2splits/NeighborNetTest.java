/*
 * NeighborNetTest.java Copyright (C) 2021. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package splitstree5.core.algorithms.distances2splits;

import jloda.util.CanceledException;
import jloda.util.progress.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.*;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

/**
 * test neighbor net
 * Created on 12/30/16.
 *
 * @author huson
 */
public class NeighborNetTest {
    @Test
    public void testCompute() throws IOException, CanceledException, InterruptedException {
        TaxaBlock taxaBlock = new TaxaBlock();
        DistancesBlock distancesBlock = new DistancesBlock();
        try (NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/distances7-taxa.nex"))) {
            np.matchIgnoreCase("#nexus");
            new TaxaNexusInput().parse(np, taxaBlock);
            new DistancesNexusInput().parse(np, taxaBlock, distancesBlock);
            assertEquals(taxaBlock.getNtax(), 7);
            assertEquals(distancesBlock.getNtax(), 7);
        }

        final SplitsBlock splitsBlock = new SplitsBlock();

        final NeighborNet algorithm = new NeighborNet();
        algorithm.compute(new ProgressPercentage(), taxaBlock, distancesBlock, splitsBlock);

        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        new TaxaNexusOutput().write(w, taxaBlock);
        new DistancesNexusOutput().write(w, taxaBlock, distancesBlock);
        new SplitsNexusOutput().write(w, taxaBlock, splitsBlock);
        System.err.println(w.toString());
    }

    @Test
    public void testComputeII() throws IOException, CanceledException, InterruptedException {
        TaxaBlock taxaBlock = new TaxaBlock();
        DistancesBlock distancesBlock = new DistancesBlock();
        try (NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/distances7-taxa.nex"))) {
            np.matchIgnoreCase("#nexus");
            new TaxaNexusInput().parse(np, taxaBlock);
            new DistancesNexusInput().parse(np, taxaBlock, distancesBlock);
            assertEquals(taxaBlock.getNtax(), 7);
            assertEquals(distancesBlock.getNtax(), 7);
        }

        final SplitsBlock splitsBlock = new SplitsBlock();

        final NeighborNet algorithm = new NeighborNet();
        algorithm.compute(new ProgressPercentage(), taxaBlock, distancesBlock, splitsBlock);

        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        new TaxaNexusOutput().write(w, taxaBlock);
        new DistancesNexusOutput().write(w, taxaBlock, distancesBlock);
        new SplitsNexusOutput().write(w, taxaBlock, splitsBlock);
        System.err.println(w.toString());
    }

}