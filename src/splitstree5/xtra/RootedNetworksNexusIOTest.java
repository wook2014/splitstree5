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

package splitstree5.xtra;

import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.TaxaNexusIO;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

/**
 * networks io test
 * Daniel Huson, 9/2017
 */
public class RootedNetworksNexusIOTest {
    @Test
    public void testRootedNetworksIO() throws IOException {
        TaxaBlock taxaBlock = new TaxaBlock();
        RootedNetworksBlock networksBlock = new RootedNetworksBlock();
        try (NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/rootednetworks.nex"))) {
            np.matchIgnoreCase("#nexus");
            TaxaNexusIO.parse(np, taxaBlock);
            RootedNetworksNexusIO.parse(np, taxaBlock, networksBlock, null);
            assertEquals(taxaBlock.getNtax(), 67);
            assertEquals(networksBlock.getNNetworks(), 2);
        }

        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        TaxaNexusIO.write(w, taxaBlock);
        RootedNetworksNexusIO.write(w, taxaBlock, networksBlock, null);

        System.err.println(w.toString());

        TaxaBlock taxaBlock2 = new TaxaBlock();
        RootedNetworksBlock networksBlock2 = new RootedNetworksBlock();
        try (NexusStreamParser np = new NexusStreamParser(new StringReader(w.toString()))) {
            np.matchIgnoreCase("#nexus");
            TaxaNexusIO.parse(np, taxaBlock2);
            RootedNetworksNexusIO.parse(np, taxaBlock2, networksBlock2, null);
            assertEquals(taxaBlock2.getNtax(), 67);
            assertEquals(networksBlock2.getNNetworks(), 2);
        }

        final StringWriter w2 = new StringWriter();
        w2.write("#nexus\n");
        TaxaNexusIO.write(w2, taxaBlock);
        RootedNetworksNexusIO.write(w2, taxaBlock, networksBlock, null);

        assertEquals(w.toString(), w2.toString());
    }

}