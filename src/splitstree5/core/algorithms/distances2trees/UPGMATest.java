package splitstree5.core.algorithms.distances2trees;

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.Taxon;

import static org.junit.Assert.assertEquals;

/**
 * Created on 21.01.2017.
 *
 * @author Daria
 */

public class UPGMATest {

    private UPGMA upgma = new UPGMA();

    @Test
    public void testCompute() throws Exception {

        String output = "(((a:8.5,b:8.5):2.5,e:11.0):16.5,(d:14.0,c:14.0):16.5)";

        String[] names = {"a", "b", "c", "d", "e"};
        Taxon[] taxons = new Taxon[5];
        TaxaBlock taxaBlock = new TaxaBlock();
        for (int i = 0; i < names.length; i++) {
            taxons[i] = new Taxon();
            taxons[i].setName(names[i]);
            taxaBlock.getTaxa().add(taxons[i]);
        }
        DistancesBlock distancesBlock = new DistancesBlock();
        double[][] dist = {{0, 17, 21, 31, 23},
                {17, 0, 30, 34, 21},
                {21, 30, 0, 28, 39},
                {31, 34, 28, 0, 43},
                {23, 21, 39, 43, 0}};
        distancesBlock.set(dist);

        ProgressListener pl = new ProgressPercentage();
        TreesBlock treesBlock = new TreesBlock();

        upgma.compute(pl, taxaBlock, distancesBlock, treesBlock);
        System.out.println("output: " + treesBlock.getTrees().get(0).toString());
        assertEquals(output, treesBlock.getTrees().get(0).toString());
    }
}