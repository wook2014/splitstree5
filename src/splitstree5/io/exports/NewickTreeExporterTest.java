/*
 * NewickTreeExporterTest.java Copyright (C) 2022 Daniel H. Huson
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
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.nexus.TaxaNexusInput;
import splitstree5.io.nexus.TreesNexusInput;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class NewickTreeExporterTest {

    private NewickTreeExporter newickTreeExporter = new NewickTreeExporter();

    @Test
    public void export() throws Exception {

        File file = new File("test/exports/TEST_Newick.txt");
        Writer writer = new BufferedWriter(new FileWriter(file));

        TaxaBlock taxa = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();

        NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/trees49-taxa.nex"));
        np.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np, taxa);
        new TreesNexusInput().parse(np, taxa, treesBlock);

        newickTreeExporter.export(writer, taxa, treesBlock);
        writer.close();

        byte[] encoded1 = Files.readAllBytes(Paths.get("test/notNexusFiles/trees49.tre"));
        String fromST4 = new String(encoded1, StandardCharsets.UTF_8);

        byte[] encoded2 = Files.readAllBytes(Paths.get("test/exports/TEST_Newick.txt"));
        String export = new String(encoded2, StandardCharsets.UTF_8);

        System.err.println(fromST4);
        System.err.println(export);
        assertEquals(fromST4, export);

    }

}