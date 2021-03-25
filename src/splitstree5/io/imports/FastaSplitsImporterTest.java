/*
 * FastaSplitsImporterTest.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.io.imports;

import jloda.util.Basic;
import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.SplitsNexusOutput;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class FastaSplitsImporterTest {

    private FastaSplitsImporter fastaSplitsImporter = new FastaSplitsImporter();

    @Test
    public void parse() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        SplitsBlock splitsBlock = new SplitsBlock();
        ProgressListener pl = new ProgressPercentage();
        fastaSplitsImporter.parse(pl, "test/notNexusFiles/algae_splits.fasta", taxaBlock, splitsBlock);

        // printing
        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        //new TaxaNexusOutput().write(w, taxaBlock);
        new SplitsNexusOutput().write(w, taxaBlock, splitsBlock);
        System.err.println(w.toString());

        String expectedOutput = "#nexus\n" +
                "\n" +
                "BEGIN SPLITS;\n" +
                "DIMENSIONS ntax=8 nsplits=22;\n" +
                "FORMAT labels=no weights=yes confidences=no;\n" +
                "PROPERTIES fit=-1.00;\n" +
                "CYCLE 1 2 7 8 6 4 5 3;\n" +
                "MATRIX\n" +
                "[1, size=1] \t 1.0 \t 1 3 4 5 6 7 8,\n" +
                "[2, size=3] \t 1.0 \t 1 3 4 5 6,\n" +
                "[3, size=3] \t 1.0 \t 1 3 5,\n" +
                "[4, size=2] \t 1.0 \t 1 3,\n" +
                "[5, size=1] \t 1.0 \t 1,\n" +
                "[6, size=1] \t 1.0 \t 1 2 3 4 5 6 8,\n" +
                "[7, size=2] \t 1.0 \t 1 2 3 4 5 6,\n" +
                "[8, size=3] \t 1.0 \t 1 2 3 4 5,\n" +
                "[9, size=4] \t 1.0 \t 1 2 3 5,\n" +
                "[10, size=3] \t 1.0 \t 1 2 3,\n" +
                "[11, size=2] \t 1.0 \t 1 2,\n" +
                "[12, size=1] \t 1.0 \t 1 2 3 4 5 6 7,\n" +
                "[13, size=2] \t 1.0 \t 1 2 3 4 5 7,\n" +
                "[14, size=4] \t 1.0 \t 1 2 3 7,\n" +
                "[15, size=3] \t 1.0 \t 1 2 7,\n" +
                "[16, size=1] \t 1.0 \t 1 2 3 4 5 7 8,\n" +
                "[17, size=2] \t 1.0 \t 1 2 3 5 7 8,\n" +
                "[18, size=4] \t 1.0 \t 1 2 7 8,\n" +
                "[19, size=1] \t 1.0 \t 1 2 3 5 6 7 8,\n" +
                "[20, size=1] \t 1.0 \t 1 2 3 4 6 7 8,\n" +
                "[21, size=2] \t 1.0 \t 1 2 4 6 7 8,\n" +
                "[22, size=1] \t 1.0 \t 1 2 4 5 6 7 8,\n" +
                ";\n" +
                "END; [SPLITS]\n";

        assertEquals(w.toString(), expectedOutput);

    }

    @Test
    public void isApplicable() throws IOException {
        Set<String> applicableFiles = new HashSet<>();

        File directory = new File("test/notNexusFiles");
        File[] directoryListing = directory.listFiles();
        if (directoryListing != null) {
            for (File file : directoryListing) {
                if (fastaSplitsImporter.isApplicable(file.getPath()))
                    applicableFiles.add(Basic.getFileNameWithoutPath(file.getName()));
            }
        }
        System.err.println(applicableFiles);
        assertEquals(applicableFiles, new HashSet<>(Arrays.asList("algae_splits.fasta", "trees49_splits.fasta")));
    }

}