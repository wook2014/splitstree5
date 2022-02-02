/*
 * FastaImporterTest.java Copyright (C) 2022 Daniel H. Huson
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

package splitstree5.io.imports;

import jloda.util.FileUtils;
import jloda.util.progress.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusOutput;
import splitstree5.io.nexus.TaxaNexusOutput;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Daria Evseeva,01.07.2017.
 */
public class FastaImporterTest {

    private final FastaImporter fastaImporter = new FastaImporter();

    @Test
    public void parseI() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();
        try (ProgressPercentage progress = new ProgressPercentage("Test")) {
            fastaImporter.parse(progress, "test/notNexusFiles/smallTest.fasta", taxaBlock, charactersBlock);
        }

        // printing
        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        new TaxaNexusOutput().write(w, taxaBlock);
		new CharactersNexusOutput().write(w, taxaBlock, charactersBlock);
		System.err.println(w);
    }

    @Test
    public void parseII() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();
        try (ProgressPercentage progress = new ProgressPercentage("Test")) {
            fastaImporter.parse(progress, "test/notNexusFiles/algae.fasta", taxaBlock, charactersBlock);
        }

        // printing
        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        new TaxaNexusOutput().write(w, taxaBlock);
		new CharactersNexusOutput().write(w, taxaBlock, charactersBlock);
		System.err.println(w);
    }

    @Test
    public void parseIII() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();
        try (ProgressPercentage progress = new ProgressPercentage("Test")) {
            fastaImporter.parse(progress, "test/notNexusFiles/ncbi.fasta", taxaBlock, charactersBlock);
        }

        // printing
        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        new TaxaNexusOutput().write(w, taxaBlock);
		new CharactersNexusOutput().write(w, taxaBlock, charactersBlock);
		System.err.println(w);
    }

    @Test
    public void parseIV() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();
        try (ProgressPercentage progress = new ProgressPercentage("Test")) {
            fastaImporter.setOptionPIRFormat(true);
            fastaImporter.parse(progress, "test/notNexusFiles/pir.fasta", taxaBlock, charactersBlock);
        }

        // printing
        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        new TaxaNexusOutput().write(w, taxaBlock);
		new CharactersNexusOutput().write(w, taxaBlock, charactersBlock);
		System.err.println(w);
    }

    @Test
    public void isApplicable() throws IOException {
        Set<String> applicableFiles = new HashSet<>();

        File directory = new File("test/notNexusFiles");
        File[] directoryListing = directory.listFiles();
        if (directoryListing != null) {
            for (File file : directoryListing) {
                if (fastaImporter.isApplicable(file.getPath()))
					applicableFiles.add(FileUtils.getFileNameWithoutPath(file.getName()));
            }
        }
        System.err.println(applicableFiles);
        assertEquals(applicableFiles, new HashSet<>(Arrays.asList("algae.fasta", "algae_splits.fasta", "pir.fasta",
                "ncbi.fasta", "smallTest.fasta", "trees49_splits.fasta")));
    }

}