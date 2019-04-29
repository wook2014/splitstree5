/*
 *  SplitsNexusIOTest.java Copyright (C) 2019 Daniel H. Huson
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
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

/**
 * test splits test
 * Daniel Huson, 12/30/16.
 */
public class SplitsNexusIOTest {

    @Test
    public void testIO() throws IOException {
        TaxaBlock taxaBlock = new TaxaBlock();
        SplitsBlock splitsBlock = new SplitsBlock();
        try (NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/splits41.nex"))) {
            np.matchIgnoreCase("#nexus");
            new TaxaNexusInput().parse(np, taxaBlock);
            new SplitsNexusInput().parse(np, taxaBlock, splitsBlock);
            assertEquals(taxaBlock.getNtax(), 10);
            assertEquals(splitsBlock.getNsplits(), 41);
        }

        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        new TaxaNexusOutput().write(w, taxaBlock);
        new SplitsNexusOutput().write(w, taxaBlock, splitsBlock);

        TaxaBlock taxaBlock2 = new TaxaBlock();
        SplitsBlock splitsBlock2 = new SplitsBlock();
        try (NexusStreamParser np = new NexusStreamParser(new StringReader(w.toString()))) {
            np.matchIgnoreCase("#nexus");
            new TaxaNexusInput().parse(np, taxaBlock2);
            new SplitsNexusInput().parse(np, taxaBlock2, splitsBlock2);
            assertEquals(taxaBlock2.getNtax(), 10);
            assertEquals(splitsBlock2.getNsplits(), 41);
        }

        final StringWriter w2 = new StringWriter();
        w2.write("#nexus\n");
        new TaxaNexusOutput().write(w2, taxaBlock);
        new SplitsNexusOutput().write(w2, taxaBlock, splitsBlock);

        assertEquals(w.toString(), w2.toString());
    }
}