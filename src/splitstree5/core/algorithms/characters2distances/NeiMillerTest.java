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
import splitstree5.io.nexus.TaxaNexusIO;

import java.io.FileReader;
import java.io.StringWriter;
import java.util.List;

/**
 * Created on 22.02.2017.
 *
 * @author Daria
 */
public class NeiMillerTest {

    final NeiMiller neiMiller = new NeiMiller();

    @Test
    public void compute() throws Exception {

        String inputFile = "test//characters//dolphins_weighted.nex";
        ProgressListener pl = new ProgressPercentage();
        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();

        CharactersNexusFormat format = new CharactersNexusFormat();
        List<String> taxonNames = CharactersNexusIO.parse(new NexusStreamParser(new FileReader(inputFile)), taxaBlock, charactersBlock, format);
        taxaBlock.addTaxaByNames(taxonNames);
        DistancesBlock distancesBlock = new DistancesBlock();

        neiMiller.compute(pl, taxaBlock, charactersBlock, distancesBlock);

        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        TaxaNexusIO.write(w, taxaBlock);
        DistancesNexusIO.write(w, taxaBlock, distancesBlock, null);
        System.err.println(w.toString());

        throw new Exception("NO TEST DATA FROM ST4");

        /*final TaxaBlock taxaFromSplitsTree4 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree4 = new DistancesBlock();
        taxaFromSplitsTree4.addTaxaByNames
                (DistancesNexusIO.parse(new NexusStreamParser(new FileReader("test//distances//....nex")),
                        taxaFromSplitsTree4, distancesFromSplitsTree4, null));


        for(int i = 0; i<distancesBlock.getDistances().length; i++){
            assertArrayEquals(distancesFromSplitsTree4.getDistances()[i], distancesBlock.getDistances()[i], 0.000001);
        }*/

    }

}