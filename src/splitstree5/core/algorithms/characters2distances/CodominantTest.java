/*
 * CodominantTest.java Copyright (C) 2022 Daniel H. Huson
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

package splitstree5.core.algorithms.characters2distances;

import jloda.util.parse.NexusStreamParser;
import jloda.util.progress.ProgressListener;
import jloda.util.progress.ProgressPercentage;
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
import static org.junit.Assert.assertFalse;

/**
 * Created on 05.03.2017.
 *
 * @author Daria
 */
public class CodominantTest {

    final Codominant codominant = new Codominant();

    @Test
    public void isApplicable() throws Exception {
        //TODO shouldn't it be used only for nucleotide?
        String inputFile2 = "test//characters//algae_rna_interleave.nex";
        TaxaBlock taxaBlock2 = new TaxaBlock();
        CharactersBlock charactersBlock2 = new CharactersBlock();
        CharactersNexusFormat format2 = new CharactersNexusFormat();
        List<String> taxonNames2 = new CharactersNexusInput().parse(new NexusStreamParser(new FileReader(inputFile2)), taxaBlock2, charactersBlock2);
        taxaBlock2.addTaxaByNames(taxonNames2);
        DistancesBlock distancesBlock2 = new DistancesBlock();

		assertFalse(codominant.isApplicable(taxaBlock2, charactersBlock2));
    }

    @Test
    public void compute() throws Exception {

        // TEST 0
        String inputFile0 = "test//characters//miniDiploid.nex";
        ProgressListener pl0 = new ProgressPercentage();
        TaxaBlock taxaBlock0 = new TaxaBlock();
        CharactersBlock charactersBlock0 = new CharactersBlock();

        CharactersNexusFormat format0 = new CharactersNexusFormat();
        List<String> taxonNames0 = new CharactersNexusInput().parse(new NexusStreamParser(new FileReader(inputFile0)), taxaBlock0,
                charactersBlock0);
        taxaBlock0.addTaxaByNames(taxonNames0);
        DistancesBlock distancesBlock0 = new DistancesBlock();

        codominant.compute(pl0, taxaBlock0, charactersBlock0, distancesBlock0);

        final TaxaBlock taxaFromSplitsTree40 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree40 = new DistancesBlock();
        taxaFromSplitsTree40.addTaxaByNames
                (new DistancesNexusInput().parse(new NexusStreamParser(new FileReader("test//distances//miniDiploidCod.nex")),
                        taxaFromSplitsTree40, distancesFromSplitsTree40));


        for (int i = 0; i < distancesBlock0.getDistances().length; i++) {
            assertArrayEquals(distancesFromSplitsTree40.getDistances()[i], distancesBlock0.getDistances()[i], 0.000001);
        }

        // + Test
        String inputFile = "test//characters//mini_rightDiploid.nex";
        ProgressListener pl = new ProgressPercentage();
        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();

        CharactersNexusFormat format = new CharactersNexusFormat();
        List<String> taxonNames = new CharactersNexusInput().parse(new NexusStreamParser(new FileReader(inputFile)), taxaBlock,
                charactersBlock);
        taxaBlock.addTaxaByNames(taxonNames);
        DistancesBlock distancesBlock = new DistancesBlock();

        codominant.compute(pl, taxaBlock, charactersBlock, distancesBlock);

        final TaxaBlock taxaFromSplitsTree4 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree4 = new DistancesBlock();
        taxaFromSplitsTree4.addTaxaByNames
                (new DistancesNexusInput().parse(new NexusStreamParser(new FileReader("test//distances//mini_rightDiploidCod.nex")),
                        taxaFromSplitsTree4, distancesFromSplitsTree4));

        for (int i = 0; i < distancesBlock.getDistances().length; i++) {
            assertArrayEquals(distancesFromSplitsTree4.getDistances()[i], distancesBlock.getDistances()[i], 0.000001);
        }


        // test 1
        String inputFile1 = "test//characters//algae_rna_interleave.nex";
        ProgressListener pl1 = new ProgressPercentage();
        TaxaBlock taxaBlock1 = new TaxaBlock();
        CharactersBlock charactersBlock1 = new CharactersBlock();

        CharactersNexusFormat format1 = new CharactersNexusFormat();
        List<String> taxonNames1 = new CharactersNexusInput().parse(new NexusStreamParser(new FileReader(inputFile1)), taxaBlock1,
                charactersBlock1);
        taxaBlock1.addTaxaByNames(taxonNames1);
        DistancesBlock distancesBlock1 = new DistancesBlock();

        codominant.compute(pl1, taxaBlock1, charactersBlock1, distancesBlock1);

        final TaxaBlock taxaFromSplitsTree41 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree41 = new DistancesBlock();
        taxaFromSplitsTree41.addTaxaByNames
                (new DistancesNexusInput().parse(new NexusStreamParser(new FileReader("test//distances//algaeCod.nex")),
                        taxaFromSplitsTree41, distancesFromSplitsTree41));


        for (int i = 0; i < distancesBlock1.getDistances().length; i++) {
            assertArrayEquals(distancesFromSplitsTree41.getDistances()[i], distancesBlock1.getDistances()[i], 0.000001);
        }

        // test 2

        List<String> taxonNames2 = new CharactersNexusInput().parse(new NexusStreamParser(new FileReader(inputFile1)),
                taxaBlock1, charactersBlock1);
        taxaBlock1.addTaxaByNames(taxonNames2);
        DistancesBlock distancesBlock2 = new DistancesBlock();

        codominant.setOptionUseSquareRoot(true);
        codominant.compute(pl1, taxaBlock1, charactersBlock1, distancesBlock2);

        final TaxaBlock taxaFromSplitsTree4_2 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree4_2 = new DistancesBlock();
        taxaFromSplitsTree41.addTaxaByNames
                (new DistancesNexusInput().parse(new NexusStreamParser(new FileReader("test//distances//algaeCodSqrt.nex")),
                        taxaFromSplitsTree4_2, distancesFromSplitsTree4_2));


        for (int i = 0; i < distancesBlock2.getDistances().length; i++) {
            assertArrayEquals(distancesFromSplitsTree4_2.getDistances()[i], distancesBlock2.getDistances()[i], 0.000001);
        }

        // test 3
        String inputFile3 = "test//characters//diploid.nex";
        ProgressListener pl3 = new ProgressPercentage();
        TaxaBlock taxaBlock3 = new TaxaBlock();
        CharactersBlock charactersBlock3 = new CharactersBlock();

        CharactersNexusFormat format3 = new CharactersNexusFormat();
        List<String> taxonNames3 = new CharactersNexusInput().parse(new NexusStreamParser(new FileReader(inputFile3)), taxaBlock3,
                charactersBlock3);
        taxaBlock3.addTaxaByNames(taxonNames3);
        DistancesBlock distancesBlock3 = new DistancesBlock();

        codominant.setOptionUseSquareRoot(false);
        codominant.compute(pl3, taxaBlock3, charactersBlock3, distancesBlock3);

        final TaxaBlock taxaFromSplitsTree43 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree43 = new DistancesBlock();
        taxaFromSplitsTree43.addTaxaByNames
                (new DistancesNexusInput().parse(new NexusStreamParser(new FileReader("test//distances//diploidCod.nex")),
                        taxaFromSplitsTree43, distancesFromSplitsTree43));


        for (int i = 0; i < distancesBlock3.getDistances().length; i++) {
            assertArrayEquals(distancesFromSplitsTree43.getDistances()[i], distancesBlock3.getDistances()[i], 0.000001);
        }

        // test 4

        List<String> taxonNames4 = new CharactersNexusInput().parse(new NexusStreamParser(new FileReader(inputFile3)), taxaBlock3,
                charactersBlock3);
        taxaBlock3.addTaxaByNames(taxonNames4);
        DistancesBlock distancesBlock4 = new DistancesBlock();

        codominant.setOptionUseSquareRoot(true);
        codominant.compute(pl3, taxaBlock3, charactersBlock3, distancesBlock4);

        final TaxaBlock taxaFromSplitsTree44 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree44 = new DistancesBlock();
        taxaFromSplitsTree44.addTaxaByNames
                (new DistancesNexusInput().parse(new NexusStreamParser(new FileReader("test//distances//diploidCodSqrt.nex")),
                        taxaFromSplitsTree44, distancesFromSplitsTree44));


        for (int i = 0; i < distancesBlock4.getDistances().length; i++) {
            assertArrayEquals(distancesFromSplitsTree44.getDistances()[i], distancesBlock4.getDistances()[i], 0.000001);
        }
    }

}