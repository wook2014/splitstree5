/*
 *  Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.io.exports;

import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.DistancesNexusInput;
import splitstree5.io.nexus.SplitsNexusInput;
import splitstree5.io.nexus.TaxaNexusInput;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class MatlabExporterTest {

    private MatlabExporter matlabExporter = new MatlabExporter();

    @Test
    public void export() throws Exception {

        File file = new File("test/exports/TEST_Matlab.txt");

        TaxaBlock taxa = new TaxaBlock();
        DistancesBlock distances = new DistancesBlock();
        SplitsBlock splits = new SplitsBlock();

        try (NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/algae.nex"))) {
            np.matchIgnoreCase("#nexus");
            new TaxaNexusInput().parse(np, taxa);
            new DistancesNexusInput().parse(np, taxa, distances);
            new SplitsNexusInput().parse(np, taxa, splits);
        }

        try (Writer writer = new BufferedWriter(new FileWriter(file))) {
            matlabExporter.export(writer, taxa);
            matlabExporter.export(writer, taxa, splits);
            matlabExporter.export(writer, taxa, distances);
        }

        byte[] encoded1 = Files.readAllBytes(Paths.get("test/notNexusFiles/algae.m"));
        String algae = new String(encoded1, StandardCharsets.UTF_8);

        byte[] encoded2 = Files.readAllBytes(Paths.get(file.getPath()));
        String export = new String(encoded2, StandardCharsets.UTF_8);

        System.err.println(algae);
        System.err.println(export);
        assertEquals(algae, export);
    }

}