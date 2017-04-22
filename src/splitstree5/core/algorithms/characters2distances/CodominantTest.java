package splitstree5.core.algorithms.characters2distances;

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusFormat;
import splitstree5.io.nexus.CharactersNexusIO;
import splitstree5.io.nexus.DistancesNexusIO;

import java.io.FileReader;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Daria on 05.03.2017.
 */
public class CodominantTest {

    final Codominant codominant = new Codominant();

    @Test
    public void isApplicable() throws Exception {
        //TODO
    }

    @Test
    public void compute() throws Exception {

        String inputFile0 = "test//characters//miniDiploid.nex";
        ProgressListener pl0 = new ProgressPercentage();
        TaxaBlock taxaBlock0 = new TaxaBlock();
        CharactersBlock charactersBlock0 = new CharactersBlock();

        CharactersNexusFormat format0 = new CharactersNexusFormat();
        List<String> taxonNames0 = CharactersNexusIO.parse(new NexusStreamParser(new FileReader(inputFile0)), taxaBlock0,
                charactersBlock0, format0);
        taxaBlock0.addTaxaByNames(taxonNames0);
        DistancesBlock distancesBlock0 = new DistancesBlock();

        codominant.compute(pl0, taxaBlock0, charactersBlock0, distancesBlock0);

        for(int i = 0; i < distancesBlock0.getDistances().length; i++){
            for(int j = 0; j< distancesBlock0.getDistances()[i].length; j++){
                System.out.print(distancesBlock0.getDistances()[i][j]+" ");
            }
            System.out.println();
        }

        final TaxaBlock taxaFromSplitsTree40 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree40 = new DistancesBlock();
        taxaFromSplitsTree40.addTaxaByNames
                (DistancesNexusIO.parse(new NexusStreamParser(new FileReader("test//distances//miniDiploidCod.nex")),
                        taxaFromSplitsTree40, distancesFromSplitsTree40, null));


        for(int i = 0; i<distancesBlock0.getDistances().length; i++){
            assertArrayEquals(distancesFromSplitsTree40.getDistances()[i], distancesBlock0.getDistances()[i], 0.000001);
        }

        // test 1
        String inputFile = "test//characters//algae_rna_interleave.nex";
        ProgressListener pl = new ProgressPercentage();
        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();

        CharactersNexusFormat format = new CharactersNexusFormat();
        List<String> taxonNames = CharactersNexusIO.parse(new NexusStreamParser(new FileReader(inputFile)), taxaBlock, charactersBlock, format);
        taxaBlock.addTaxaByNames(taxonNames);
        DistancesBlock distancesBlock = new DistancesBlock();

        codominant.compute(pl, taxaBlock, charactersBlock, distancesBlock);

        final TaxaBlock taxaFromSplitsTree4 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree4 = new DistancesBlock();
        taxaFromSplitsTree4.addTaxaByNames
                (DistancesNexusIO.parse(new NexusStreamParser(new FileReader("test//distances//algaeCod.nex")),
                        taxaFromSplitsTree4, distancesFromSplitsTree4, null));


        for(int i = 0; i<distancesBlock.getDistances().length; i++){
            assertArrayEquals(distancesFromSplitsTree4.getDistances()[i], distancesBlock.getDistances()[i], 0.0001);
        }

        // test 2

        List<String> taxonNames2 = CharactersNexusIO.parse(new NexusStreamParser(new FileReader(inputFile)), taxaBlock, charactersBlock, format);
        taxaBlock.addTaxaByNames(taxonNames2);
        DistancesBlock distancesBlock2 = new DistancesBlock();

        codominant.setOptionUseSquareRoot(true);
        codominant.compute(pl, taxaBlock, charactersBlock, distancesBlock2);

        final TaxaBlock taxaFromSplitsTree4_2 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree4_2 = new DistancesBlock();
        taxaFromSplitsTree4.addTaxaByNames
                (DistancesNexusIO.parse(new NexusStreamParser(new FileReader("test//distances//algaeCodSqrt.nex")),
                        taxaFromSplitsTree4_2, distancesFromSplitsTree4_2, null));


        for(int i = 0; i<distancesBlock2.getDistances().length; i++){
            assertArrayEquals(distancesFromSplitsTree4_2.getDistances()[i], distancesBlock2.getDistances()[i], 1.0);
        }

        // test 3
        String inputFile3 = "test//characters//diploid.nex";
        ProgressListener pl3 = new ProgressPercentage();
        TaxaBlock taxaBlock3 = new TaxaBlock();
        CharactersBlock charactersBlock3 = new CharactersBlock();

        CharactersNexusFormat format3 = new CharactersNexusFormat();
        List<String> taxonNames3 = CharactersNexusIO.parse(new NexusStreamParser(new FileReader(inputFile3)), taxaBlock3, charactersBlock3, format3);
        taxaBlock3.addTaxaByNames(taxonNames3);
        DistancesBlock distancesBlock3 = new DistancesBlock();

        codominant.setOptionUseSquareRoot(false);
        codominant.compute(pl3, taxaBlock3, charactersBlock3, distancesBlock3);

        final TaxaBlock taxaFromSplitsTree43 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree43 = new DistancesBlock();
        taxaFromSplitsTree43.addTaxaByNames
                (DistancesNexusIO.parse(new NexusStreamParser(new FileReader("test//distances//diploidCod.nex")),
                        taxaFromSplitsTree43, distancesFromSplitsTree43, null));


        for(int i = 0; i<distancesBlock3.getDistances().length; i++){
            assertArrayEquals(distancesFromSplitsTree43.getDistances()[i], distancesBlock3.getDistances()[i], 1.0);
        }

        // test 4

        List<String> taxonNames4 = CharactersNexusIO.parse(new NexusStreamParser(new FileReader(inputFile3)), taxaBlock3, charactersBlock3, format3);
        taxaBlock3.addTaxaByNames(taxonNames4);
        DistancesBlock distancesBlock4 = new DistancesBlock();

        codominant.setOptionUseSquareRoot(true);
        codominant.compute(pl3, taxaBlock3, charactersBlock3, distancesBlock4);

        final TaxaBlock taxaFromSplitsTree44 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree44 = new DistancesBlock();
        taxaFromSplitsTree44.addTaxaByNames
                (DistancesNexusIO.parse(new NexusStreamParser(new FileReader("test//distances//diploidCodSqrt.nex")),
                        taxaFromSplitsTree44, distancesFromSplitsTree44, null));


        for(int i = 0; i<distancesBlock4.getDistances().length; i++){
            assertArrayEquals(distancesFromSplitsTree44.getDistances()[i], distancesBlock4.getDistances()[i], 1.0);
        }
    }

}