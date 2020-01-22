/*
 *  BaseFreqDistanceTest.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.core.algorithms.characters2distances;

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusFormat;
import splitstree5.io.nexus.CharactersNexusInput;
import splitstree5.io.nexus.DistancesNexusInput;

import java.io.FileReader;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created on 05.03.2017.
 *
 * @author Daria
 */
public class BaseFreqDistanceTest {

    final private BaseFreqDistance baseFreqDistance = new BaseFreqDistance();

    @Test
    public void compute() throws Exception {

        // todo try with weights

        String inputFile = "test//characters//mini.nex";
        ProgressListener pl = new ProgressPercentage();
        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();

        CharactersNexusFormat format = new CharactersNexusFormat();
        List<String> taxonNames = new CharactersNexusInput().parse(new NexusStreamParser(new FileReader(inputFile)), taxaBlock, charactersBlock);
        taxaBlock.addTaxaByNames(taxonNames);
        DistancesBlock distancesBlock = new DistancesBlock();

        baseFreqDistance.compute(pl, taxaBlock, charactersBlock, distancesBlock);

        final TaxaBlock taxaFromSplitsTree4 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree4 = new DistancesBlock();
        taxaFromSplitsTree4.addTaxaByNames
                (new DistancesNexusInput().parse(new NexusStreamParser(new FileReader("test//distances//miniBaseFreq.nex")),
                        taxaFromSplitsTree4, distancesFromSplitsTree4));


        System.out.println("MATRIX");
        for (int i = 0; i < distancesBlock.getDistances().length; i++) {
            for (int j = 0; j < distancesBlock.getDistances()[i].length; j++) {
                System.out.print(distancesBlock.get(i + 1, j + 1) + " ");
            }
            System.out.println();
            assertArrayEquals(distancesFromSplitsTree4.getDistances()[i], distancesBlock.getDistances()[i], 0.0000001);
        }

        // TEST 1
        String inputFile1 = "test//characters//algae_rna_interleave.nex";
        ProgressListener pl1 = new ProgressPercentage();
        TaxaBlock taxaBlock1 = new TaxaBlock();
        CharactersBlock charactersBlock1 = new CharactersBlock();

        CharactersNexusFormat format1 = new CharactersNexusFormat();
        List<String> taxonNames1 = new CharactersNexusInput().parse(new NexusStreamParser(new FileReader(inputFile1)),
                taxaBlock1, charactersBlock1);
        taxaBlock1.addTaxaByNames(taxonNames1);
        DistancesBlock distancesBlock1 = new DistancesBlock();

        baseFreqDistance.compute(pl1, taxaBlock1, charactersBlock1, distancesBlock1);

        final TaxaBlock taxaFromSplitsTree41 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree41 = new DistancesBlock();
        taxaFromSplitsTree41.addTaxaByNames
                (new DistancesNexusInput().parse(new NexusStreamParser(new FileReader("test//distances//algaeBaseFreq.nex")),
                        taxaFromSplitsTree41, distancesFromSplitsTree41));


        System.out.println("MATRIX");
        for (int i = 0; i < distancesBlock1.getDistances().length; i++) {
            //for(int j = 0; j < distancesBlock1.getDistances()[i].length; j++){
            //  System.out.print(distancesBlock1.get(i+1,j+1)+" ");
            //}
            //System.out.println();
            assertArrayEquals(distancesFromSplitsTree41.getDistances()[i], distancesBlock1.getDistances()[i], 0.0000001);
        }

        // TEST 2
        String inputFile2 = "test//characters//myosin_aa.nex";
        ProgressListener pl2 = new ProgressPercentage();
        TaxaBlock taxaBlock2 = new TaxaBlock();
        CharactersBlock charactersBlock2 = new CharactersBlock();

        CharactersNexusFormat format2 = new CharactersNexusFormat();
        List<String> taxonNames2 = new CharactersNexusInput().parse(new NexusStreamParser(new FileReader(inputFile2)), taxaBlock2, charactersBlock2);
        taxaBlock2.addTaxaByNames(taxonNames2);
        DistancesBlock distancesBlock2 = new DistancesBlock();

        baseFreqDistance.compute(pl2, taxaBlock2, charactersBlock2, distancesBlock2);

        final TaxaBlock taxaFromSplitsTree4_2 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree4_2 = new DistancesBlock();
        taxaFromSplitsTree4_2.addTaxaByNames
                (new DistancesNexusInput().parse(new NexusStreamParser(new FileReader("test//distances//myosinBaseFreq.nex")),
                        taxaFromSplitsTree4_2, distancesFromSplitsTree4_2));


        for (int i = 0; i < distancesBlock2.getDistances().length; i++) {
            assertArrayEquals(distancesFromSplitsTree4_2.getDistances()[i], distancesBlock2.getDistances()[i], 0.0000001);
        }

    }

}