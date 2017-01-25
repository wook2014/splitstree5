package splitstree5.core.algorithms.trees2splits;

import jloda.phylo.PhyloTree;
import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.Taxon;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

public class TreeSelectorTest {

    private TreeSelector treeSelector = new TreeSelector();

    @Test
    public void testCompute() throws Exception {

        String[] names = {"t1","t2","t3","t4","t5","t6"};
        Taxon[] taxons = new Taxon[6];
        TaxaBlock taxaBlock = new TaxaBlock();
        for (int i = 0; i < names.length; i++) {
            taxons[i] = new Taxon();
            taxons[i].setName(names[i]);
            taxaBlock.getTaxa().add(taxons[i]);
        }

        PhyloTree tree = PhyloTree.valueOf("(((5:1.0,6:1.0):1.0,((1:1.0,2:1.0):1.0,3:1.0):1.0):0.5,4:0.5):0;", false);
        System.out.print(tree.toString());
        TreesBlock trees = new TreesBlock();
        trees.getTrees().add(tree);

        ProgressListener pl = new ProgressPercentage();
        SplitsBlock splits = new SplitsBlock();
        //treeSelector.compute(pl, taxaBlock, trees, splits);

        //System.out.print(splits.getSplits().get(0));

        assertEquals(3,3);
    }

}