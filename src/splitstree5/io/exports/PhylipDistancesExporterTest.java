/*
 * PhylipDistancesExporterTest.java Copyright (C) 2020. Daniel H. Huson
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
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.DistancesNexusInput;

import java.io.*;

public class PhylipDistancesExporterTest {

    private PhylipDistancesExporter phylipDistancesExporter = new PhylipDistancesExporter();

    @Test
    public void export() throws Exception {


        File file1 = new File("test/exports/TEST_PhylipDist.aln");
        Writer writer1 = new BufferedWriter(new FileWriter(file1));
        File file2 = new File("test/exports/TEST_PhylipDistTri.aln");
        Writer writer2 = new BufferedWriter(new FileWriter(file2));

        TaxaBlock taxa = new TaxaBlock();
        DistancesBlock distances = new DistancesBlock();
        taxa.addTaxaByNames(new DistancesNexusInput().parse(new NexusStreamParser(
                        new FileReader("test/distances/algaeCod.nex")),
                taxa, distances));

        phylipDistancesExporter.export(writer1, taxa, distances);
        writer1.close();
        phylipDistancesExporter.setOptionTriangular(true);
        phylipDistancesExporter.export(writer2, taxa, distances);
        writer2.close();
    }

}