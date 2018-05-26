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

package splitstree5.io.nexus;

import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

/**
 * distances io test
 * Daniel Huson, 12/30/16.
 */
public class DistancesNexusIOTest {
    @Test
    public void testIO() throws IOException {
        TaxaBlock taxaBlock = new TaxaBlock();
        DistancesBlock distancesBlock = new DistancesBlock();
        try (NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/distances7-taxa.nex"))) {
            np.matchIgnoreCase("#nexus");
            new TaxaNexusInput().parse(np, taxaBlock);
            new DistancesNexusInput().parse(np, taxaBlock, distancesBlock);
            assertEquals(taxaBlock.getNtax(), 7);
            assertEquals(distancesBlock.getNtax(), 7);
        }

        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        new TaxaNexusOutput().write(w, taxaBlock);
        new DistancesNexusOutput().write(w, taxaBlock, distancesBlock);

        TaxaBlock taxaBlock2 = new TaxaBlock();
        DistancesBlock distancesBlock2 = new DistancesBlock();
        try (NexusStreamParser np = new NexusStreamParser(new StringReader(w.toString()))) {
            np.matchIgnoreCase("#nexus");
            new TaxaNexusInput().parse(np, taxaBlock2);
            new DistancesNexusInput().parse(np, taxaBlock2, distancesBlock2);
            assertEquals(taxaBlock2.getNtax(), 7);
            assertEquals(distancesBlock2.getNtax(), 7);
        }

        final StringWriter w2 = new StringWriter();
        w2.write("#nexus\n");
        new TaxaNexusOutput().write(w2, taxaBlock);
        new DistancesNexusOutput().write(w2, taxaBlock, distancesBlock);

        assertEquals(w.toString(), w2.toString());

    }

}