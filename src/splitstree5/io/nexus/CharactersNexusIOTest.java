/*
 *  Copyright (C) 2017 Daniel H. Huson
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

package splitstree5.io.nexus;

import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * test cases for CharacterBlock io
 * Created by huson on 1/19/17.
 */
public class CharactersNexusIOTest {

    // not #nexus, ntaxa in character block, characters first

    @Test
    public void testIO() throws IOException {

        final ArrayList<String> inputFiles = new ArrayList<>();

        inputFiles.add("test//characters//ferment4-diploid.nex");

        inputFiles.add("test//characters//tokens.nex");

        inputFiles.add("test//nexus//characters-simple.nex");

        inputFiles.add("test//characters//algae_rna_interleave.nex");
        inputFiles.add("test//characters//bees_dna_interleave.nex");
        inputFiles.add("test//characters//dolphins_binary.nex");
        inputFiles.add("test//characters//mammals_aa.nex");
        inputFiles.add("test//characters//mtDNA_interleave.nex");
        inputFiles.add("test//characters//myosin_aa.nex");
        inputFiles.add("test//characters//rubber_dna_interleave.nex");

        inputFiles.add("test//characters//microsat1.nex");
        inputFiles.add("test//characters//microsat2.nex");

        // transposed matrix
        inputFiles.add("test//characters//transpose//algae_rna_interleave.nex");
        inputFiles.add("test//characters//transpose//bees_dna_interleave.nex");
        inputFiles.add("test//characters//transpose//dolphins_binary.nex");
        inputFiles.add("test//characters//transpose//mtDNA_interleave.nex");
        inputFiles.add("test//characters//transpose//rubber_dna_interleave.nex");

        inputFiles.add("test//characters//transpose//microsat1.nex");
        inputFiles.add("test//characters//transpose//microsat2.nex");

        //new
        inputFiles.add("test//characters//pf02171.nexus");
        inputFiles.add("test//characters//pf01569.nexus");


        for (String inputFile : inputFiles) {
            System.err.println("Input file: " + inputFile);

            TaxaBlock taxaBlock = new TaxaBlock();
            CharactersBlock charactersBlock1 = new CharactersBlock();

            CharactersNexusFormat format = new CharactersNexusFormat();
            List<String> taxonNames = CharactersNexusIO.parse(new NexusStreamParser(new FileReader(inputFile)), taxaBlock, charactersBlock1, format);
            taxaBlock.addTaxaByNames(taxonNames);

            for (int test = 0; test < 4; test++) {
                System.err.println("test: " + test);
                switch (test) {
                    // first: original interleave and transpose
                    // transpose must be set false before setting interleave true
                    case 0:
                        break;
                    case 1:
                        format.setInterleave(false);
                        format.setTranspose(false);
                        break;
                    case 2:
                        format.setInterleave(true);
                        format.setTranspose(false);
                        break;
                    case 3:
                        format.setInterleave(false);
                        format.setTranspose(true);
                        break;

                    //need list of tokens
                    /*case 3:
                        format.setTranspose(false);
                        format.setTokens(true);
                        break;*/

                }
                StringWriter sw1 = new StringWriter();
                CharactersNexusIO.write(sw1, taxaBlock, charactersBlock1, format);
                System.err.println(sw1.toString());

                CharactersBlock charactersBlock2 = new CharactersBlock();
                CharactersNexusIO.parse(new NexusStreamParser(new StringReader(sw1.toString())), taxaBlock, charactersBlock2, format);

                // for (int t = 1; t <= charactersBlock1.getNtax(); t++)
                //     assertArrayEquals("t=" + t, charactersBlock1.getMatrix()[t - 1], charactersBlock2.getMatrix()[t - 1]);

                StringWriter sw2 = new StringWriter();
                CharactersNexusIO.write(sw2, taxaBlock, charactersBlock2, format);

                System.err.println(sw2.toString());

                assertEquals(sw1.toString(), sw2.toString());

            }
        }
    }
}