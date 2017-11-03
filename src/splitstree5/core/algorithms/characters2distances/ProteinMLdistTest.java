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

import static org.junit.Assert.assertArrayEquals;

/**
 * Created on Feb 2017.
 * @author Daria
 */

public class ProteinMLdistTest {

    final ProteinMLdist pML = new ProteinMLdist();

    @Test
    public void testCompute() throws Exception {

        String inputFile = "test//characters//myosin_aa.nex";
        ProgressListener pl = new ProgressPercentage();
        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();

        CharactersNexusFormat format = new CharactersNexusFormat();
        List<String> taxonNames = CharactersNexusIO.parse(new NexusStreamParser(new FileReader(inputFile)), taxaBlock, charactersBlock, format);
        taxaBlock.addTaxaByNames(taxonNames);
        DistancesBlock distancesBlock = new DistancesBlock();

        pML.compute(pl, taxaBlock, charactersBlock, distancesBlock);

        final TaxaBlock taxaFromSplitsTree4 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree4 = new DistancesBlock();
        taxaFromSplitsTree4.addTaxaByNames
                (DistancesNexusIO.parse(new NexusStreamParser(new FileReader("test//distances//myosinProtML.nex")),
                        taxaFromSplitsTree4, distancesFromSplitsTree4, null));


        for(int i = 0; i<distancesBlock.getDistances().length; i++){
            assertArrayEquals(distancesFromSplitsTree4.getDistances()[i], distancesBlock.getDistances()[i], 0.000001);
        }

        // test 2

        List<String> taxonNames2 = CharactersNexusIO.parse(new NexusStreamParser(new FileReader(inputFile)), taxaBlock, charactersBlock, format);
        taxaBlock.addTaxaByNames(taxonNames2);
        DistancesBlock distancesBlock2 = new DistancesBlock();


        pML.setOptionModel(ProteinMLdist.Model.pmb);
        pML.compute(pl, taxaBlock, charactersBlock, distancesBlock2);

        final TaxaBlock taxaFromSplitsTree4_2 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree4_2 = new DistancesBlock();
        taxaFromSplitsTree4.addTaxaByNames
                (DistancesNexusIO.parse(new NexusStreamParser(new FileReader("test//distances//myosinProtML_pmb.nex")),
                        taxaFromSplitsTree4_2, distancesFromSplitsTree4_2, null));


        for(int i = 0; i<distancesBlock2.getDistances().length; i++){
            assertArrayEquals(distancesFromSplitsTree4_2.getDistances()[i], distancesBlock2.getDistances()[i], 0.000001);
        }
    }

}