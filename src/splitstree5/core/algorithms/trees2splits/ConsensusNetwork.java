package splitstree5.core.algorithms.trees2splits;


import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;

public class ConsensusNetwork extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToTrees {

    public final static String MEDIAN = "median";
    public final static String MEAN = "mean";
    public final static String COUNT = "count";
    public final static String SUM = "sum";
    public final static String NONE = "none";
    private String optionEdgeWeights = MEAN;
    private double threshold = 0.33;
    public final static String DESCRIPTION = "Computes the consensus splits of trees (Holland and Moulton 2003)";

    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, TreesBlock parent, SplitsBlock child) throws Exception {

    }

    /*public ArrayList<ASplit> apply(TaxaBlock taxa, TreesBlock trees) throws CanceledException {

        //doc.notifySetMaximumProgress(100);
        //doc.notifySetProgress(0);

        TreeSelector trans;
        TreesBlock tempTree;
        //Splits splits = new Splits();
        ArrayList<ASplit> splits = new ArrayList<>();
        Map consensus = new HashMap();  // we will store all splits and the count in the Map

        if (trees.getNTrees() == 1) System.err.println("Consensus network: only one Tree specified");

        for (int which = 1; which <= trees.getNTrees(); which++) {
            //doc.notifySetProgress(50 * which / trees.getNtrees());

            //tempTree = new Trees(trees.getName(which), trees.getTree(which), taxa, trees.getTranslate());
            tempTree = new TreesBlock();
            tempTree.getTrees().add(trees.getTrees().get(which));

            trans = new TreeSelector();
            //splits = trans.apply(null, taxa, tempTree);  //DJB: pass null here so that progress bar doesn't appear
            splits = trans.apply(taxa,tempTree);
            for (int i = 1; i <= splits.size()/*splits.getNsplits()*/; /*i++) {
                Add(consensus, splits.get(i).getBits(), splits.getWeight(i));
            }
        }

        splits = new Splits(taxa.getNtax());
        Object[] keys = consensus.keySet().toArray();
        for (int t = 0; t < keys.length; t++) {
            doc.notifySetProgress(50 + 50 * t / keys.length);
            // check if the Split is in the consensus and if the appearance is high enough
            TaxaSet nTSet = new TaxaSet((BitSet) keys[t]);
            double wgt;
            WeightStats value = (WeightStats) consensus.get(keys[t]);
            if (value.getCount() / (double) trees.getNtrees() > threshold) {
                switch (getOptionEdgeWeights()) {
                    case "count":
                        wgt = value.getCount();
                        break;
                    case "mean":
                        wgt = value.getMean();
                        break;
                    case "median":
                        wgt = value.getMedian();
                        break;
                    case "sum":
                        wgt = value.getSum();
                        break;
                    default:
                        wgt = 1;
                        break;
                }

                float confidence = (float) value.getCount() / (float) trees.getNtrees();
                splits.add(nTSet, (float) wgt, confidence);
            }
        }
        splits.getFormat().setConfidences(true);

        System.runFinalization();
        //doc.notifySetProgress(100);

        return splits;
    }*/
}
