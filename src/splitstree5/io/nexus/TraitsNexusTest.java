/*
 * TraitsNexusTest.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.io.nexus;

import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TraitsBlock;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class TraitsNexusTest {
    @Test
    public void testIO() throws IOException {
        final String fileName = "test/nexus/popart-sequences-with-traits.nex";

        final TaxaBlock taxaBlock = new TaxaBlock();
        final TraitsBlock traitsBlock = new TraitsBlock();

        try (NexusStreamParser np = new NexusStreamParser(new FileReader(fileName))) {
            new TaxaNexusInput().parse(np, taxaBlock);
            new CharactersNexusInput().parse(np, taxaBlock, new CharactersBlock());
            new TraitsNexusInput().parse(np, taxaBlock, traitsBlock);
        }

        final String output1;
        try (StringWriter writer = new StringWriter()) {
            new TaxaNexusOutput().write(writer, taxaBlock);
            new TraitsNexusOutput().write(writer, taxaBlock, traitsBlock);
            output1 = writer.toString();
        }

        final TaxaBlock taxaBlock2 = new TaxaBlock();
        final TraitsBlock traitsBlock2 = new TraitsBlock();

        try (NexusStreamParser np = new NexusStreamParser(new StringReader(output1))) {
            new TaxaNexusInput().parse(np, taxaBlock2);
            new TraitsNexusInput().parse(np, taxaBlock2, traitsBlock2);
        }

        final String output2;
        try (StringWriter writer = new StringWriter()) {
            new TaxaNexusOutput().write(writer, taxaBlock2);
            new TraitsNexusOutput().write(writer, taxaBlock2, traitsBlock2);
            output2 = writer.toString();
        }

        assertEquals(output1, output2);
    }
}
