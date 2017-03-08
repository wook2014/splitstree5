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
public class BaseFreqDistanceTest {

    final BaseFreqDistance baseFreqDistance = new BaseFreqDistance();

    @Test
    public void compute() throws Exception {

        // TEST 1
        String inputFile = "test//characters//algae_rna_interleave.nex";
        ProgressListener pl = new ProgressPercentage();
        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();

        CharactersNexusFormat format = new CharactersNexusFormat();
        List<String> taxonNames = CharactersNexusIO.parse(new NexusStreamParser(new FileReader(inputFile)), taxaBlock, charactersBlock, format);
        taxaBlock.addTaxaByNames(taxonNames);
        DistancesBlock distancesBlock = new DistancesBlock();

        baseFreqDistance.compute(pl, taxaBlock, charactersBlock, distancesBlock);

        final TaxaBlock taxaFromSplitsTree4 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree4 = new DistancesBlock();
        taxaFromSplitsTree4.addTaxaByNames
                (DistancesNexusIO.parse(new NexusStreamParser(new FileReader("test//distances//algaeBaseFreq.nex")),
                        taxaFromSplitsTree4, distancesFromSplitsTree4, null));


        for(int i = 0; i<distancesBlock.getDistances().length; i++){
            assertArrayEquals(distancesFromSplitsTree4.getDistances()[i], distancesBlock.getDistances()[i], 0.001);
        }

        // TEST 2
        String inputFile2 = "test//characters//myosin_aa.nex";
        ProgressListener pl2 = new ProgressPercentage();
        TaxaBlock taxaBlock2 = new TaxaBlock();
        CharactersBlock charactersBlock2 = new CharactersBlock();

        CharactersNexusFormat format2 = new CharactersNexusFormat();
        List<String> taxonNames2 = CharactersNexusIO.parse(new NexusStreamParser(new FileReader(inputFile2)), taxaBlock2, charactersBlock2, format2);
        taxaBlock2.addTaxaByNames(taxonNames2);
        DistancesBlock distancesBlock2 = new DistancesBlock();

        baseFreqDistance.compute(pl2, taxaBlock2, charactersBlock2, distancesBlock2);

        final TaxaBlock taxaFromSplitsTree4_2 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree4_2 = new DistancesBlock();
        taxaFromSplitsTree4_2.addTaxaByNames
                (DistancesNexusIO.parse(new NexusStreamParser(new FileReader("test//distances//myosinBaseFreq.nex")),
                        taxaFromSplitsTree4_2, distancesFromSplitsTree4_2, null));


        for(int i = 0; i<distancesBlock2.getDistances().length; i++){
            assertArrayEquals(distancesFromSplitsTree4_2.getDistances()[i], distancesBlock2.getDistances()[i], 0.01);
        }

    }

}