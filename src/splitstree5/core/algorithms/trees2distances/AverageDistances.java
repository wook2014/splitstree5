package splitstree5.core.algorithms.trees2distances;

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.algorithms.trees2splits.TreeSelector;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;

import java.util.BitSet;

/**
 * Created by Daria on 07.06.2017.
 */
public class AverageDistances extends Algorithm<TreesBlock, DistancesBlock> implements IFromTrees, IToDistances {
    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, TreesBlock treesBlock, DistancesBlock distancesBlock)
            throws Exception {

        distancesBlock.setNtax(taxaBlock.getNtax());

        int[][] count = new int[taxaBlock.getNtax() + 1][taxaBlock.getNtax() + 1];
        // number of trees that contain two given taxa

        TreeSelector selector = new TreeSelector();

        for (int which = 1; which <= treesBlock.getNTrees(); which++) {
            TaxaBlock tmpTaxa = (TaxaBlock) taxaBlock.clone();
            selector.setOptionWhich(which);

            ProgressListener pl = new ProgressPercentage();
            SplitsBlock splits = new SplitsBlock();
            selector.compute(pl, tmpTaxa, treesBlock, splits);
            selector.compute(new ProgressPercentage(), tmpTaxa, treesBlock, splits); // modifies tmpTaxa, too!
            for (int a = 1; a <= tmpTaxa.getNtax(); a++)
                for (int b = 1; b <= tmpTaxa.getNtax(); b++) {
                    int i = taxaBlock.indexOf(tmpTaxa.getLabel(a)); // translate numbering
                    int j = taxaBlock.indexOf(tmpTaxa.getLabel(b));
                    count[i][j]++;
                    count[j][i]++;
                }

            for (int s = 0; s < splits.getNsplits(); s++) {
                BitSet A = splits.getSplits().get(s).getA();
                //splits.get(s).getBits();
                BitSet B = splits.getSplits().get(s).getB();
                //splits.get(s).getComplement(tmpTaxa.getNtax()).getBits();
                for (int a = A.nextSetBit(1); a > 0; a = A.nextSetBit(a + 1)) {
                    for (int b = B.nextSetBit(1); b > 0; b = B.nextSetBit(b + 1)) {
                        int i = taxaBlock.indexOf(tmpTaxa.getLabel(a)); // translate numbering
                        int j = taxaBlock.indexOf(tmpTaxa.getLabel(b));
                        distancesBlock.set(i, j, distancesBlock.get(i, j) +  splits.getSplits().get(s).getWeight());//splits.getWeight(s));
                        distancesBlock.set(j, i, distancesBlock.get(i, j));
                    }
                }
            }
        }
        // divide by count
        for (int i = 1; i <= taxaBlock.getNtax(); i++) {
            for (int j = 1; j <= taxaBlock.getNtax(); j++) {
                if (count[i][j] > 0)
                    distancesBlock.set(i, j, distancesBlock.get(i, j) / count[i][j]);
                else
                    distancesBlock.set(i, j, 100); // shouldn't ever happen!
            }
        }
        print(distancesBlock);
    }

    public void print(DistancesBlock distancesBlock){
        System.out.println("DISTANCES");
        for(int i = 0; i < distancesBlock.getDistances().length; i++){
            for(int j = 0; j < distancesBlock.getDistances()[i].length; j++){
                System.out.print(distancesBlock.get(i,j)+" ");
            }
            System.out.println();
        }
    }
}
