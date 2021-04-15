/*
 * FastaExporterTest.java Copyright (C) 2021. Daniel H. Huson
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
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusFormat;
import splitstree5.io.nexus.CharactersNexusInput;
import splitstree5.io.nexus.SplitsNexusInput;
import splitstree5.io.nexus.TaxaNexusInput;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FastaExporterTest {

    private FastaExporter fastaExporter = new FastaExporter();

    @Test
    public void exportCharacters() throws Exception {

        File file = new File("test/exports/TEST_FASTA.fasta");
        Writer writer = new BufferedWriter(new FileWriter(file));


        TaxaBlock taxa = new TaxaBlock();
        CharactersBlock characters = new CharactersBlock();
        CharactersNexusFormat format = new CharactersNexusFormat();

        List<String> taxonNames = new CharactersNexusInput().parse(
                new NexusStreamParser(new FileReader("test/characters/microsat1.nex")),
                taxa, characters);
        taxa.addTaxaByNames(taxonNames);

        fastaExporter.export(writer, taxa, characters);
        writer.close();

    }

    @Test
    public void exportSplitsI() throws Exception {

        File file = new File("test/exports/TEST_FASTA_splits.fasta");
        Writer writer = new BufferedWriter(new FileWriter(file));

        TaxaBlock taxa = new TaxaBlock();
        SplitsBlock splits = new SplitsBlock();

        NexusStreamParser np = new NexusStreamParser(new FileReader("test/splits/algae.txt"));
        np.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np, taxa);
        new SplitsNexusInput().parse(np, taxa, splits);

        fastaExporter.export(writer, taxa, splits);
        writer.close();

        byte[] encoded1 = Files.readAllBytes(Paths.get("test/notNexusFiles/algae_splits.fasta"));
        String fromST4 = new String(encoded1, StandardCharsets.UTF_8);
        byte[] encoded2 = Files.readAllBytes(Paths.get("test/exports/TEST_FASTA_splits.fasta"));
        String export = new String(encoded2, StandardCharsets.UTF_8);

        System.err.println(fromST4);
        System.err.println(export);
        assertEquals(fromST4, export);

    }

    @Test
    public void exportSplitsII() throws Exception {

        File file = new File("test/exports/TEST_FASTA_splits_trees49.fasta");
        Writer writer = new BufferedWriter(new FileWriter(file));

        TaxaBlock taxa = new TaxaBlock();
        SplitsBlock splits = new SplitsBlock();

        NexusStreamParser np = new NexusStreamParser(new FileReader("test/splits/trees49-SuperNet.nex"));
        np.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np, taxa);
        new SplitsNexusInput().parse(np, taxa, splits);

        fastaExporter.export(writer, taxa, splits);
        writer.close();

        // zeilenwise einlesen, stringbuilder, zeilenunbruch in str-buffer reinschreiben, replase all
        //todo splits A-B, traits
        byte[] encoded1 = Files.readAllBytes(Paths.get("test/notNexusFiles/trees49_splits.fasta"));
        String fromST4 = new String(encoded1, StandardCharsets.UTF_8);
        byte[] encoded2 = Files.readAllBytes(Paths.get("test/exports/TEST_FASTA_splits_trees49.fasta"));
        String export = new String(encoded2, StandardCharsets.UTF_8);

        assertEquals(fromST4, export);

    }
}