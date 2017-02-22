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

import java.io.FileReader;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Daria on 22.02.2017.
 */
public class LogDetTest {

    final LogDet logDet = new LogDet();

    @Test
    public void testCompute() throws Exception {

        String inputFile = "test//characters//bees_dna_interleave.nex";
        ProgressListener pl = new ProgressPercentage();
        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();

        CharactersNexusFormat format = new CharactersNexusFormat();
        List<String> taxonNames = CharactersNexusIO.parse(new NexusStreamParser(new FileReader(inputFile)), taxaBlock, charactersBlock, format);
        taxaBlock.addTaxaByNames(taxonNames);
        DistancesBlock distancesBlock = new DistancesBlock();

        logDet.compute(pl, taxaBlock, charactersBlock, distancesBlock);

        double[][] must_dist = {
                {0.0,	0.08840697494340972,	0.10929642036356128,	0.09666305856063594,	0.006158003731885308,	0.07668734238595977},
                {0.08840697494340972,	0.0,	0.08704838233386236,	0.08912904045753299,	0.09185514250183825,	0.1025178572437495},
                {0.10929642036356128,	0.08704838233386236,	0.0,	0.11406122758106056,	0.11283547273859765,	0.1083728721443404},
                {0.09666305856063594,	0.08912904045753299,	0.11406122758106056,	0.0,	0.09997871489180964,	0.09546887605217587},
                {0.006158003731885308,	0.09185514250183825,	0.11283547273859765,	0.09997871489180964,	0.0,	0.08003562509457929},
                {0.07668734238595977,	0.1025178572437495,	0.1083728721443404,	0.09546887605217587,	0.08003562509457929,	0.0}
        };

        for(int i = 0; i<distancesBlock.getDistances().length; i++){
            assertArrayEquals(must_dist[i], distancesBlock.getDistances()[i], 0);
        }

    }

}