/*
 * HammingDistancesAmbigStatesTest.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.core.algorithms.characters2distances;

import jloda.util.progress.ProgressListener;
import jloda.util.progress.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.imports.FastaImporter;
import splitstree5.io.nexus.DistancesNexusInput;
import splitstree5.io.nexus.DistancesNexusOutput;

import java.io.FileReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class HammingDistancesAmbigStatesTest {

    @Test
    public void compute() throws Exception {


        // todo no ambig -> different results(must be the same!): MAtch case!

        final TaxaBlock taxa = new TaxaBlock();
        final CharactersBlock characters = new CharactersBlock();
        final DistancesBlock distances = new DistancesBlock();

        final HammingDistancesAmbigStates hammingDistances = new HammingDistancesAmbigStates();
        final FastaImporter fastaImporter = new FastaImporter();
        final ProgressListener progressListener = new ProgressPercentage();

        fastaImporter.parse(progressListener,
                "test/PRLR pyramidum complex final haplotypes edited labels.fas",
                taxa, characters);

        hammingDistances.setOptionHandleAmbiguousStates(HammingDistancesAmbigStates.AmbiguousOptions.MatchStates);
        hammingDistances.compute(new ProgressPercentage(), taxa, characters, distances);

        StringWriter w = new StringWriter();
        new DistancesNexusOutput().write(w, taxa, distances);
        System.err.println(w.toString());

        final TaxaBlock taxaFromSplitsTree4 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree4 = new DistancesBlock();
        taxaFromSplitsTree4.addTaxaByNames(
                new DistancesNexusInput().parse(new NexusStreamParser(new FileReader("test/distances/PRLP_hamming_Match")), taxaFromSplitsTree4, distancesFromSplitsTree4));

        StringWriter wFromSplitsTree4 = new StringWriter();
        new DistancesNexusOutput().write(wFromSplitsTree4, taxaFromSplitsTree4, distancesFromSplitsTree4);
        //System.err.println(wFromSplitsTree4.toString());


        assertEquals(w.toString().toUpperCase(), wFromSplitsTree4.toString().toUpperCase());

        w.close();
        wFromSplitsTree4.close();
    }

    @Test
    public void computeIgnore() throws Exception {


        // todo no ambig -> different results(must be the same!): MAtch case!

        final TaxaBlock taxa = new TaxaBlock();
        final CharactersBlock characters = new CharactersBlock();
        final DistancesBlock distances = new DistancesBlock();

        final HammingDistancesAmbigStates hammingDistances = new HammingDistancesAmbigStates();
        final FastaImporter fastaImporter = new FastaImporter();
        final ProgressListener progressListener = new ProgressPercentage();

        fastaImporter.parse(progressListener,
                "test/PRLR pyramidum complex final haplotypes edited labels.fas",
                taxa, characters);

        hammingDistances.setOptionHandleAmbiguousStates(HammingDistancesAmbigStates.AmbiguousOptions.Ignore);
        hammingDistances.compute(new ProgressPercentage(), taxa, characters, distances);

        StringWriter w = new StringWriter();
        new DistancesNexusOutput().write(w, taxa, distances);
        System.err.println(w.toString());

        final TaxaBlock taxaFromSplitsTree4 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree4 = new DistancesBlock();
        taxaFromSplitsTree4.addTaxaByNames(
                new DistancesNexusInput().parse(new NexusStreamParser(new FileReader("test/distances/PRLP_hamming_Ignore")), taxaFromSplitsTree4, distancesFromSplitsTree4));

        StringWriter wFromSplitsTree4 = new StringWriter();
        new DistancesNexusOutput().write(wFromSplitsTree4, taxaFromSplitsTree4, distancesFromSplitsTree4);
        //System.err.println(wFromSplitsTree4.toString());


        assertEquals(w.toString().toUpperCase(), wFromSplitsTree4.toString().toUpperCase());

        w.close();
        wFromSplitsTree4.close();
    }
}