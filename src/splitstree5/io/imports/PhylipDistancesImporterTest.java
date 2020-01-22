/*
 *  PhylipDistancesImporterTest.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.io.imports;

import jloda.util.Basic;
import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.DistancesNexusOutput;
import splitstree5.io.nexus.TaxaNexusOutput;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Daria Evseeva,03.10.2017.
 */
public class PhylipDistancesImporterTest {

    private PhylipDistancesImporter phylipDistancesImporter = new PhylipDistancesImporter();

    @org.junit.Test
    public void parse() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        DistancesBlock distancesBlock = new DistancesBlock();
        ProgressListener pl = new ProgressPercentage();

        phylipDistancesImporter.parse(pl, "test/notNexusFiles/square.dist", taxaBlock, distancesBlock);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        new TaxaNexusOutput().write(w1, taxaBlock);
        new DistancesNexusOutput().write(w1, taxaBlock, distancesBlock);
        System.err.println(w1.toString());
        String s1 = w1.toString();

        phylipDistancesImporter.parse(pl, "test/notNexusFiles/triangular.dist", taxaBlock, distancesBlock);
        // printing
        final StringWriter w2 = new StringWriter();
        w2.write("#nexus\n");
        new TaxaNexusOutput().write(w2, taxaBlock);
        new DistancesNexusOutput().write(w2, taxaBlock, distancesBlock);
        System.err.println(w2.toString());
        String s2 = w2.toString();

        phylipDistancesImporter.parse(pl, "test/notNexusFiles/triangularEOL.dist", taxaBlock, distancesBlock);
        // printing
        final StringWriter w3 = new StringWriter();
        w3.write("#nexus\n");
        new TaxaNexusOutput().write(w3, taxaBlock);
        new DistancesNexusOutput().write(w3, taxaBlock, distancesBlock);
        System.err.println(w3.toString());

        phylipDistancesImporter.parse(pl, "test/notNexusFiles/squareEOL-bf.dist", taxaBlock, distancesBlock);
        // printing
        final StringWriter w4 = new StringWriter();
        w4.write("#nexus\n");
        new TaxaNexusOutput().write(w4, taxaBlock);
        new DistancesNexusOutput().write(w4, taxaBlock, distancesBlock);
        System.err.println(w4.toString());

        phylipDistancesImporter.parse(pl, "test/notNexusFiles/triangular_upper.dist", taxaBlock, distancesBlock);
        // printing
        final StringWriter w5 = new StringWriter();
        w5.write("#nexus\n");
        new TaxaNexusOutput().write(w5, taxaBlock);
        new DistancesNexusOutput().write(w5, taxaBlock, distancesBlock);
        System.err.println(w5.toString());
    }

    @Test
    public void isApplicable() throws IOException {
        ArrayList<String> applicableFiles = new ArrayList<>();

        File directory = new File("test/notNexusFiles");
        File[] directoryListing = directory.listFiles();
        if (directoryListing != null) {
            for (File file : directoryListing) {
                if (phylipDistancesImporter.isApplicable(file.getPath()))
                    applicableFiles.add(Basic.getFileNameWithoutPath(file.getName()));
            }
        }
        System.err.println(applicableFiles);
        assertEquals(applicableFiles, Arrays.asList("mash.dist", "square.dist", "squareEOL-bf.dist", "triangular.dist",
                "triangularEOL.dist", "triangular_upper.dist"));
    }

}