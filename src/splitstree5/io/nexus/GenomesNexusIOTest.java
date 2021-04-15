/*
 * DistancesNexusIOTest.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.io.nexus;

import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.GenomesBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * distances io test
 * Daniel Huson, 12/30/16.
 */
public class GenomesNexusIOTest {
    @Test
    public void testIO() throws IOException {
        TaxaBlock taxaBlock = new TaxaBlock();
        GenomesBlock genomesBlock = new GenomesBlock();

        final String input =
                "begin genomes;\n" +
                        "dimensions ntax=4;\n" +
                        "format labels=yes accessions=yes multiPart=yes files=no;\n" +
                        "matrix\n" +
                        "\tfirst AA00001 85 2 80 ACTTAAAAAGATTTTCTATCTACGGATAGTTAGCTCTTTTTCTAGACCTTGTCTACTCAATTCAACTAAACAGAAATTTT 5 ACGTA,\n" +
                        "\tsecond AA00002 80 1 80 GTGCTGTGCTGTCCTCTAGTTCCTGGTTGGCGTTCCGTCGCCTTCTACATACTAGACAAACAGCCTTCCTCCGGTTCCGT,\n" +
                        "\tthird AA00003 90 3 80 GCAGTGCACTCGTTGACATTGTTGACGATGCACTGGGACAACCTTGGTTCATACGTAAGCTTGGTGACCTTGCAAGTGCA 5 ACGTA 5 ACGTA,\n" +
                        "\tfour AA00004 80 1 80 TGTTGCTTAAAAATTACAACACTCCTTATAAAACTTACAGCTGCGTAGTGAGAGGTGATAAGTGTTGCATCACTTGCACC\n" +
                        ";\n" +
                        "end;\n";

        System.err.println("Input:\n" + input);


        final List<String> taxa = (new GenomesNexusInput()).parse(new NexusStreamParser(new StringReader(input)), taxaBlock, genomesBlock);
        taxaBlock.addTaxaByNames(taxa);

        NexusParser.parse(new NexusStreamParser(new StringReader(input)), taxaBlock, genomesBlock);

        final String string1;
        {
            final StringWriter writer = new StringWriter();
            (new GenomesNexusOutput()).write(writer, taxaBlock, genomesBlock);
            string1 = writer.toString();
            System.err.println("String1:\n" + string1);
        }

        final String string2;
        {
            NexusParser.parse(new NexusStreamParser(new StringReader(string1)), taxaBlock, genomesBlock);
            final StringWriter writer = new StringWriter();
            (new GenomesNexusOutput()).write(writer, taxaBlock, genomesBlock);
            string2 = writer.toString();
            System.err.println("String2:\n" + string2);

        }

        assertEquals(string1, string2);

    }

}