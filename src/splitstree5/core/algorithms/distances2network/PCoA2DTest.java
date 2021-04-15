/*
 * PCoA2DTest.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.core.algorithms.distances2network;

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.NetworkBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.DistancesNexusInput;
import splitstree5.io.nexus.NetworkNexusOutput;
import splitstree5.io.nexus.TaxaNexusInput;
import splitstree5.io.nexus.TaxaNexusOutput;

import java.io.FileReader;
import java.io.StringWriter;

public class PCoA2DTest {

    private PCoA2D pCoA2D = new PCoA2D();

    @Test
    public void compute() throws Exception {

        TaxaBlock taxa = new TaxaBlock();
        DistancesBlock distancesBlock = new DistancesBlock();
        NetworkBlock networkBlock = new NetworkBlock();
        ProgressListener pl = new ProgressPercentage();

        NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/algae.nex"));
        np.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np, taxa);
        new DistancesNexusInput().parse(np, taxa, distancesBlock);

        pCoA2D.compute(pl, taxa, distancesBlock, networkBlock);

        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        new TaxaNexusOutput().write(w1, taxa);
        new NetworkNexusOutput().write(w1, taxa, networkBlock);
        System.err.println(w1.toString());
    }
}