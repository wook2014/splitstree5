/*
 * ClustalImporterTest.java Copyright (C) 2021. Daniel H. Huson
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
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusFormat;
import splitstree5.io.nexus.CharactersNexusOutput;
import splitstree5.io.nexus.TaxaNexusOutput;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Daria Evseeva,05.08.2017.
 */
public class ClustalImporterTest {

    private ClustalImporter clustalImporter = new ClustalImporter();

    @Test
    public void parse() throws Exception {

        {
            TaxaBlock taxaBlock = new TaxaBlock();
            CharactersBlock charactersBlock = new CharactersBlock();
            CharactersNexusFormat format = new CharactersNexusFormat();
            try (ProgressListener progress = new ProgressPercentage()) {
                clustalImporter.parse(progress, "test/notNexusFiles/prot1.aln", taxaBlock, charactersBlock);
            }
            // printing
            final StringWriter w1 = new StringWriter();
            w1.write("#nexus\n");
            new TaxaNexusOutput().write(w1, taxaBlock);
            new CharactersNexusOutput().write(w1, taxaBlock, charactersBlock);
            System.err.println(w1.toString());
            System.err.println(format.isOptionInterleave());
        }
        //System.err.println("Ambiguous : " + charactersBlock.isHasAmbiguousStates());

        {
            TaxaBlock taxaBlock = new TaxaBlock();
            CharactersBlock charactersBlock = new CharactersBlock();
            CharactersNexusFormat format = new CharactersNexusFormat();
            try (ProgressListener progress = new ProgressPercentage()) {
                clustalImporter.parse(progress, "test/notNexusFiles/protein.aln", taxaBlock, charactersBlock);
            }
            // printing
            final StringWriter w2 = new StringWriter();
            w2.write("#nexus\n");
            new TaxaNexusOutput().write(w2, taxaBlock);
            new CharactersNexusOutput().write(w2, taxaBlock, charactersBlock);
            System.err.println(w2.toString());
            //System.err.println("Ambiguous : " + charactersBlock.isHasAmbiguousStates());
        }

        {
            TaxaBlock taxaBlock = new TaxaBlock();
            CharactersBlock charactersBlock = new CharactersBlock();
            CharactersNexusFormat format = new CharactersNexusFormat();
            try (ProgressListener progress = new ProgressPercentage()) {
                clustalImporter.parse(progress, "test/notNexusFiles/conservation.aln", taxaBlock, charactersBlock);
            }
            // printing
            final StringWriter w3 = new StringWriter();
            w3.write("#nexus\n");
            new TaxaNexusOutput().write(w3, taxaBlock);
            new CharactersNexusOutput().write(w3, taxaBlock, charactersBlock);
            System.err.println(w3.toString());
            //System.err.println("Ambiguous : " + charactersBlock.isHasAmbiguousStates());
        }

        {
            TaxaBlock taxaBlock = new TaxaBlock();
            CharactersBlock charactersBlock = new CharactersBlock();
            try (ProgressListener progress = new ProgressPercentage()) {
                clustalImporter.parse(progress, "test/notNexusFiles/dna-ncbi.aln", taxaBlock, charactersBlock);
            }// printing
            final StringWriter w4 = new StringWriter();
            w4.write("#nexus\n");
            new TaxaNexusOutput().write(w4, taxaBlock);
            new CharactersNexusOutput().write(w4, taxaBlock, charactersBlock);
            System.err.println(w4.toString());
            //System.err.println("Ambiguous : " + charactersBlock.isHasAmbiguousStates());
        }

        {
            TaxaBlock taxaBlock = new TaxaBlock();
            CharactersBlock charactersBlock = new CharactersBlock();
            CharactersNexusFormat format = new CharactersNexusFormat();
            try (ProgressListener progress = new ProgressPercentage()) {
                clustalImporter.parse(progress, "test/notNexusFiles/dna-ncbi-num.aln", taxaBlock, charactersBlock);
            }
            // printing
            final StringWriter w5 = new StringWriter();
            w5.write("#nexus\n");
            new TaxaNexusOutput().write(w5, taxaBlock);
            new CharactersNexusOutput().write(w5, taxaBlock, charactersBlock);
            System.err.println(w5.toString());
            //System.err.println("Ambiguous : " + charactersBlock.isHasAmbiguousStates());
        }
    }

    @Test
    public void isApplicable() throws IOException {
        ArrayList<String> applicableFiles = new ArrayList<>();

        File directory = new File("test/notNexusFiles");
        File[] directoryListing = directory.listFiles();
        if (directoryListing != null) {
            for (File file : directoryListing) {
                if (clustalImporter.isApplicable(file.getPath()))
                    applicableFiles.add(Basic.getFileNameWithoutPath(file.getName()));
            }
        }
        System.err.println(applicableFiles);
        assertEquals(applicableFiles, Arrays.asList("conservation.aln", "dna-ncbi-num.aln", "dna-ncbi.aln", "prot1.aln"));
    }

}