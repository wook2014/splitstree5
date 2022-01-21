/*
 * MSFExporterTest.java Copyright (C) 2022 Daniel H. Huson
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

package splitstree5.io.exports;

import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusInput;

import java.io.*;
import java.util.List;

public class MSFExporterTest {

    private MSFExporter msfExporter = new MSFExporter();

    @Test
    public void export() throws IOException {

        File file = new File("test/exports/TEST_MSF.msf");
        Writer writer = new BufferedWriter(new FileWriter(file));

        TaxaBlock taxa = new TaxaBlock();
        CharactersBlock characters = new CharactersBlock();
        List<String> taxonNames = new CharactersNexusInput().parse(
                //new NexusStreamParser(new FileReader("test/characters/microsat1.nex")),
                new NexusStreamParser(new FileReader("test/characters/dolphins_binary.nex")),
                //new NexusStreamParser(new FileReader("test/characters/algae_rna_interleave.nex")),
                taxa, characters);
        taxa.addTaxaByNames(taxonNames);

        msfExporter.export(writer, taxa, characters);
        writer.close();
    }
}