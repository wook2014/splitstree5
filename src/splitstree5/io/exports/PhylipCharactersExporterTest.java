/*
 * PhylipCharactersExporterTest.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.io.exports;

import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusFormat;
import splitstree5.io.nexus.CharactersNexusInput;

import java.io.*;
import java.util.List;

public class PhylipCharactersExporterTest {

    private PhylipCharactersExporter phylipCharactersExporter = new PhylipCharactersExporter();

    @Test
    public void export() throws Exception {

        // interleaved
        File fileI = new File("test/exports/TEST_PHYL_INTER.phy");
        Writer writerI = new BufferedWriter(new FileWriter(fileI));


        TaxaBlock taxaI = new TaxaBlock();
        CharactersBlock charactersI = new CharactersBlock();
        CharactersNexusFormat formatI = new CharactersNexusFormat();

        List<String> taxonNamesI = new CharactersNexusInput().parse(
                new NexusStreamParser(new FileReader("test/characters/microsat1.nex")),
                taxaI, charactersI);
        taxaI.addTaxaByNames(taxonNamesI);

        phylipCharactersExporter.export(writerI, taxaI, charactersI);
        writerI.close();


        // standard
        phylipCharactersExporter.setOptionInterleaved(false);
        File file = new File("test/exports/TEST_PHYL.phy");
        Writer writer = new BufferedWriter(new FileWriter(file));


        TaxaBlock taxa = new TaxaBlock();
        CharactersBlock characters = new CharactersBlock();
        CharactersNexusFormat format = new CharactersNexusFormat();

        List<String> taxonNames = new CharactersNexusInput().parse(
                new NexusStreamParser(new FileReader("test/characters/microsat1.nex")),
                taxa, characters);
        taxa.addTaxaByNames(taxonNames);

        phylipCharactersExporter.export(writer, taxa, characters);
        writer.close();

    }

}