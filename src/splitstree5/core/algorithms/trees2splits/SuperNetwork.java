package splitstree5.core.algorithms.trees2splits;

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.NotOwnerException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.gui.dialog.Alert;
import splitstree5.utils.nexus.TreesUtilities;

import java.util.*;

public class SuperNetwork  extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToSplits {

    public final static String DESCRIPTION = "Z-closure super-network from partial trees (Huson, Dezulian, Kloepper and Steel 2004)";
    private boolean optionZRule = true;
    private boolean optionLeastSquare = false;
    private boolean optionSuperTree = false;
    private int optionNumberOfRuns = 1;
    private boolean optionApplyRefineHeuristic = false;
    private int optionSeed = 0;
    private String optionEdgeWeights = TREESIZEWEIGHTEDMEAN;

    // edge weight options:
    static final String AVERAGERELATIVE = "AverageRelative";
    static final String MEAN = "Mean";
    static final String TREESIZEWEIGHTEDMEAN = "TreeSizeWeightedMean";
    static final String SUM = "Sum";
    static final String MIN = "Min";
    static final String NONE = "None";

    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, TreesBlock treesBlock, SplitsBlock splitsBlock)
            throws Exception {

        // todo
        /*if (treesBlock.getNTrees() == 0)
            return new Splits(taxa.getNtax());*/

        /*if (treesBlock.getPartial()) // contains partial trees, most determine
            // full set of taxa
            trees.setTaxaFromPartialTrees(taxa);*/

        progressListener.setTasks("Z-closure", "init");

        Map[] pSplitsOfTrees = new Map[treesBlock.getNTrees() + 1];
        // for each tree, identity map on set of splits
        BitSet[] supportSet = new BitSet[treesBlock.getNTrees() + 1];
        Set allPSplits = new HashSet();

        ////doc.notifySubtask("extracting partial splits from trees");
        progressListener.setSubtask("extracting partial splits from trees");
        progressListener.setMaximum(treesBlock.getNTrees());

        for (int which = 1; which <= treesBlock.getNTrees(); which++) {
            try {
                progressListener.incrementProgress();
                pSplitsOfTrees[which] = new HashMap();
                supportSet[which] = new BitSet();
                computePartialSplits(taxaBlock, treesBlock, which, pSplitsOfTrees[which], supportSet[which]);
                for (Object o : pSplitsOfTrees[which].keySet()) {
                    PartialSplit ps = (PartialSplit) o;
                    if (ps.isNonTrivial()) {
                        allPSplits.add(ps.clone());
                        ////doc.notifySetProgress(which);
                        progressListener.incrementProgress();
                    }
                }
            } catch (NotOwnerException e) {
                Basic.caught(e);
            }
        }
        SplitsBlock splits = new SplitsBlock();

        if (getOptionZRule()) {
            computeClosureOuterLoop(/*doc,*/ progressListener, taxaBlock, allPSplits);
        }

        if (getOptionApplyRefineHeuristic()) {
            ////doc.notifySubtask("Refinement heuristic");
            applyRefineHeuristic(/*doc,*/ allPSplits);
        }

        ////doc.notifySubtask("collecting full splits");
        ////doc.notifySetMaximumProgress(allPSplits.size());
        int count = 0;
        for (Object allPSplit : allPSplits) {
            //doc.notifySetProgress(++count);
            PartialSplit ps = (PartialSplit) allPSplit;
            int size = ps.getXsize();

            // for now, keep all splits of correct size
            if (size == taxaBlock.getNtax()) {
                boolean ok = true;
                if (getOptionSuperTree()) {
                    for (int t = 1; ok && t <= treesBlock.getNTrees(); t++) {
                        Map pSplits = (pSplitsOfTrees[t]);
                        BitSet support = supportSet[t];
                        PartialSplit induced = ps.getInduced(support);
                        if (induced != null && !pSplits.containsKey(induced))
                            ok = false;     // found a tree that doesn't contain the induced split
                    }
                }
                if (ok){
                    ASplit split = new ASplit(ps.getA(), taxaBlock.getNtax());
                    splits.getSplits().add(split);
                }
            }
        }

        // add all missing trivial splits
        for (int t = 1; t <= taxaBlock.getNtax(); t++) {
            BitSet ts = new BitSet();
            ts.set(t);
            PartialSplit ps = new PartialSplit(ts);
            ps.setComplement(taxaBlock.getTaxaSet());
            if (!allPSplits.contains(ps)){
                //splits.getSplitsSet().add(ps.getA());
                ASplit split = new ASplit(ps.getA(), taxaBlock.getNtax());
                splits.getSplits().add(split);
            }

        }

        if (getOptionEdgeWeights().equals(AVERAGERELATIVE)) {
            setWeightAverageReleativeLength(pSplitsOfTrees, supportSet, taxaBlock, splits);
        } else if (!getOptionEdgeWeights().equals(NONE)) {
            setWeightsConfidences(pSplitsOfTrees, supportSet, taxaBlock, splits);
        }

        if (getNoOptionLeastSquare()) {
            if (!TreesUtilities.hasAllPairs(taxaBlock, treesBlock)) {
                new Alert("Partial trees don't have the 'All Pairs' property,\n" +
                        "can't apply Least Squares");
                setNoOptionLeastSquare(false);
            } else {
               /* Distances distances = TreesUtilities.getAveragePairwiseDistances(taxa, trees);
                LeastSquaresWeights leastSquares = new LeastSquaresWeights();

                //document tmp//doc = new //document();
                tmp//doc.setTaxa(taxa);
                tmp//doc.setDistances(distances);
                tmp//doc.setSplits(splits);
                tmp//doc.setProgressListener(//doc.getProgressListener());
                if (!leastSquares.isApplicable(tmp//doc, taxa, splits))
                    new Alert("Least Squares not applicable");
                else
                    leastSquares.apply(tmp//doc, taxa, splits);*/
            }
        }

        //doc.notifySetProgress(100);   //set progress to 100%
        // pd.close();								//get rid of the progress listener
        // //doc.setProgressListener(null);
        //return splits;

    }


    /**
     * set the weight to the mean weight of all projections of this split and confidence to
     * the count of trees containing a projection of the split
     *
     * @param pSplits
     * @param supportSet
     * @param taxa
     * @param splits
     */
    private void setWeightsConfidences(/*document doc*/ Map[] pSplits,
                                       BitSet[] supportSet, TaxaBlock taxa, SplitsBlock splits) throws CanceledException {
        for (int s = 1; s <= splits.getNsplits(); s++) {
            //doc.notifySetProgress(-1);
            PartialSplit current = new PartialSplit(splits.getSplits().get(s-1).getA(),
                    splits.getSplits().get(s-1).getB());
                    //new PartialSplit(splits.get(s),
                    //splits.get(s).getComplement(taxa.getNtax()));

            float min = 1000000;
            float sum = 0;
            float weighted = 0;
            float confidence = 0;
            int total = 0;
            for (int t = 1; t < pSplits.length; t++) {
                PartialSplit projection = current.getInduced(supportSet[t]);
                if (projection != null)  // split cuts support set of tree t
                {
                    if (pSplits[t].containsKey(projection)) {
                        float cur = ((PartialSplit) pSplits[t].get(projection)).getWeight();
                        weighted += supportSet[t].cardinality() * cur;
                        if (cur < min)
                            min = cur;
                        sum += cur;
                        confidence += supportSet[t].cardinality() *
                                ((PartialSplit) pSplits[t].get(projection)).getConfidence();
                    }
                    total += supportSet[t].cardinality();
                }
            }

            float value = 1;
            switch (getOptionEdgeWeights()) {
                case MIN:
                    value = min;
                    break;
                case MEAN:
                    value = weighted / total;
                    break;
                case TREESIZEWEIGHTEDMEAN:
                    value = sum / total;
                    break;
                case SUM:
                    value = sum;
                    break;
            }
            //splits.setWeight(s, value);
            //splits.setConfidence(s, total);
            splits.getSplits().get(s-1).setWeight(value);
            splits.getSplits().get(s-1).setConfidence(total);
        }
    }

    /**
     * sets the weight of a split in the network as the average relative length of the edge
     * in the input trees
     *
     * @param //doc
     * @param pSplits
     * @param supportSet
     * @param taxa
     * @param splits
     * @throws CanceledException
     */
    private void setWeightAverageReleativeLength( Map[] pSplits,
                                                 BitSet[] supportSet, TaxaBlock taxa, SplitsBlock splits) throws
            CanceledException {
        // compute average of weights and num of edges for each input tree
        float[] averageWeight = new float[pSplits.length];
        int[] numEdges = new int[pSplits.length];

        for (int t = 1; t < pSplits.length; t++) {
            numEdges[t] = pSplits[t].size();
            float sum = 0;
            for (Object o : pSplits[t].keySet()) {
                PartialSplit ps = (PartialSplit) o;
                sum += ps.getWeight();
            }
            averageWeight[t] = sum / numEdges[t];
        }

        // consider each network split in turn:
        for (int s = 1; s <= splits.getNsplits(); s++) {
            //doc.notifySetProgress(-1);
            PartialSplit current =
                    new PartialSplit(splits.getSplits().get(s-1).getA(),
                            splits.getSplits().get(s-1).getB());
                    //new PartialSplit(splits.get(s),
                    //splits.get(s).getComplement(taxa.getNtax()));

            BitSet activeTrees = new BitSet(); // trees that contain projection of
            // current split

            for (int t = 1; t < pSplits.length; t++) {
                PartialSplit projection = current.getInduced(supportSet[t]);
                if (projection != null && pSplits[t].containsKey(projection)) {
                    activeTrees.set(t);
                }
            }

            float weight = 0;
            for (int t = activeTrees.nextSetBit(1); t >= 0; t = activeTrees.nextSetBit(t + 1)) {
                PartialSplit projection = current.getInduced(supportSet[t]);

                weight += ((PartialSplit) pSplits[t].get(projection)).getWeight()
                        / averageWeight[t];
            }
            weight /= activeTrees.cardinality();
            splits.getSplits().get(s-1).setWeight(weight); //setWeight(s, weight);
        }
    }

    /**
     * returns the set of all partial splits in the given tree
     *
     * @param trees
     * @param which
     * @param pSplitsOfTree partial splits are returned here
     * @param support       supporting taxa are returned here
     */
    private void computePartialSplits(TaxaBlock taxa, TreesBlock trees, int which,
                                      Map pSplitsOfTree, BitSet support) throws NotOwnerException {
        List list = new LinkedList(); // list of (onesided) partial splits
        Node v = trees.getTrees().get(which-1).getFirstNode();
        computePSplitsFromTreeRecursively(v, null, trees, taxa, list, which, support);

        for (Object aList : list) {
            PartialSplit ps = (PartialSplit) aList;
            ps.setComplement(support);
            pSplitsOfTree.put(ps, ps);
        }
    }

    // recursively compute the splits:

    private BitSet computePSplitsFromTreeRecursively(Node v, Edge e, TreesBlock trees,
                                                      TaxaBlock taxa, List list, int which, BitSet seen) throws NotOwnerException {
        PhyloTree tree = trees.getTrees().get(which);// getTree(which);
        //BitSet e_taxa = trees.getTaxaForLabel(taxa, tree.getLabel(v));
        // todo implement getTaxaForLabel in Taxa Block?
        BitSet e_taxa = new BitSet(); // todo delete
        seen.or(e_taxa);

        Iterator edges = tree.getAdjacentEdges(v);
        while (edges.hasNext()) {
            Edge f = (Edge) edges.next();
            if (f != e) {
                BitSet f_taxa = computePSplitsFromTreeRecursively(tree.getOpposite(v, f), f, trees,
                        taxa, list, which, seen);
                PartialSplit ps = new PartialSplit(f_taxa);
                ps.setWeight((float) tree.getWeight(f));
                list.add(ps);
                //e_taxa.set(f_taxa);
                for (int t = 1; t < f_taxa.length(); t++) {
                    if (f_taxa.get(t))
                        e_taxa.set(t);
                }
            }
        }
        return e_taxa;
    }

    Random rand = null;

    /**
     * runs the closure method. Does this multiple times, if desired
     *
     * @param taxa
     * @param partialSplits
     * @throws CanceledException
     */
    private void computeClosureOuterLoop(ProgressListener pl, TaxaBlock taxa, Set partialSplits) {
        this.rand = new Random(this.optionSeed);

        try {
            Set allEverComputed = new HashSet(partialSplits);

            for (int i = 0; i < this.optionNumberOfRuns; i++) {
                ////doc.notifySubtask("compute closure" + (i == 0 ? "" : "(" + (i + 1) + ")"));

                Set clone = new LinkedHashSet(partialSplits);

                {
                    Vector tmp = new Vector(clone);
                    Collections.shuffle(tmp, rand);
                    clone = new LinkedHashSet(tmp);
                    computeClosure(/*doc,*/pl, clone);
                }

                allEverComputed.addAll(clone);

            }
            partialSplits.clear();
            partialSplits.addAll(allEverComputed);
        } catch (Exception ex) {
            Basic.caught(ex);
        }
    }

    /**
     * gets the number of full splits
     *
     * @param numAllTaxa
     * @param partialSplits
     * @return number of full splits
     */
    public int getNumberOfFullSplits(int numAllTaxa, Set partialSplits) {
        int nfs = 0;
        for (Object partialSplit1 : partialSplits) {
            PartialSplit partialSplit = (PartialSplit) partialSplit1;
            if (partialSplit.getXsize() == numAllTaxa) nfs++;
        }
        return nfs;
    }


    /**
     * computes the split closure obtained using the zig-zap rule
     *
     * @param //doc           the //document
     * @param partialSplits
     */
    private void computeClosure(ProgressListener pl, Set partialSplits) throws CanceledException {

        PartialSplit[] splits;
        Set seniorSplits = new LinkedHashSet();
        Set activeSplits = new LinkedHashSet();
        Set newSplits = new LinkedHashSet();
        {
            splits = new PartialSplit[partialSplits.size()];
            Iterator it = partialSplits.iterator();
            int pos = 0;
            while (it.hasNext()) {
                splits[pos] = (PartialSplit) it.next();
                seniorSplits.add(pos);
                ////doc.notifySetProgress(-1);
                pos++;
            }
        }

        // init:
        {
            for (int pos1 = 0; pos1 < splits.length; pos1++) {

                for (int pos2 = pos1 + 1; pos2 < splits.length; pos2++) {
                    PartialSplit ps1 = splits[pos1];
                    PartialSplit ps2 = splits[pos2];
                    PartialSplit qs1 = new PartialSplit();
                    PartialSplit qs2 = new PartialSplit();
                    if (PartialSplit.applyZigZagRule(ps1, ps2, qs1, qs2)) {
                        splits[pos1] = qs1;
                        splits[pos2] = qs2;
                        newSplits.add(pos1);
                        newSplits.add(pos2);
                    }
                    ////doc.notifySetProgress(-1);
                }
            }
        }

        // main loop:
        {
            while (newSplits.size() != 0) {
                seniorSplits.addAll(activeSplits);
                activeSplits = newSplits;
                newSplits = new HashSet();

                Iterator it1 = seniorSplits.iterator();
                while (it1.hasNext()) {
                    Integer pos1 = ((Integer) it1.next());

                    for (Object activeSplit : activeSplits) {
                        Integer pos2 = ((Integer) activeSplit);
                        PartialSplit ps1 = splits[pos1];
                        PartialSplit ps2 = splits[pos2];
                        PartialSplit qs1 = new PartialSplit();
                        PartialSplit qs2 = new PartialSplit();
                        if (PartialSplit.applyZigZagRule(ps1, ps2, qs1, qs2)) {
                            splits[pos1] = qs1;
                            splits[pos2] = qs2;
                            newSplits.add(pos1);
                            newSplits.add(pos2);
                        }
                        //doc.notifySetProgress(-1);
                    }
                }
                it1 = activeSplits.iterator();
                while (it1.hasNext()) {
                    Integer pos1 = ((Integer) it1.next());

                    for (Object activeSplit : activeSplits) {
                        Integer pos2 = ((Integer) activeSplit);
                        PartialSplit ps1 = splits[pos1];
                        PartialSplit ps2 = splits[pos2];
                        PartialSplit qs1 = new PartialSplit();
                        PartialSplit qs2 = new PartialSplit();
                        if (PartialSplit.applyZigZagRule(ps1, ps2, qs1, qs2)) {
                            splits[pos1] = qs1;
                            splits[pos2] = qs2;
                            newSplits.add(pos1);
                            newSplits.add(pos2);
                        }
                        //doc.notifySetProgress(-1);
                    }
                }
            }
        }

        partialSplits.clear();
        Iterator it = seniorSplits.iterator();
        while (it.hasNext()) {
            Integer pos1 = (Integer) it.next();
            partialSplits.add(splits[pos1]);
        }
        it = activeSplits.iterator();
        while (it.hasNext()) {
            Integer pos1 = (Integer) it.next();
            partialSplits.add(splits[pos1]);
        }
    }

    /**
     * applies a simple refinement heuristic
     *
     * @param //doc
     * @param partialSplits
     * @throws CanceledException
     */
    private void applyRefineHeuristic(/*document doc,*/ Set partialSplits) throws CanceledException {


        for (int i = 1; i <= 10; i++) {
            int count = 0;
            //doc.notifySetMaximumProgress(partialSplits.size());

            PartialSplit[] splits = new PartialSplit[partialSplits.size()];
            splits = (PartialSplit[]) partialSplits.toArray(splits);

            for (int a = 0; a < splits.length; a++) {
                //doc.notifySetMaximumProgress(a);
                final PartialSplit psa = splits[a];
                for (int p = 1; p <= 2; p++) {
                    final BitSet Aa, Ba;
                    if (p == 1) {
                        Aa = psa.getA();
                        Ba = psa.getB();
                    } else {
                        Aa = psa.getB();
                        Ba = psa.getA();
                    }
                    for (int b = a + 1; b < splits.length; b++) {
                        final PartialSplit psb = splits[b];
                        for (int q = 1; q <= 2; q++) {
                            final BitSet Ab, Bb;
                            if (q == 1) {
                                Ab = psb.getA();
                                Bb = psb.getB();
                            } else {
                                Ab = psb.getB();
                                Bb = psb.getA();
                            }
                            if (Aa.intersects(Ab)
                                    && !Ba.intersects(Ab) && !Bb.intersects(Aa)
                                    && Ba.intersects(Bb)) {
                                PartialSplit ps = new PartialSplit(PartialSplit.union(Aa, Ab), PartialSplit.union(Ba, Bb));
                                if (!partialSplits.contains(ps)) {
                                    partialSplits.add(ps);
                                    count++;
                                }
                            }
                        }
                    }
                    /*
                    if (psa.getA().cardinality() == 2) {
                        Aa = psa.getA();
                        Ba = psa.getB();
                    } else if (psa.getB().cardinality() == 2) {
                        Aa = psa.getB();
                        Ba = psa.getA();
                    } else
                        continue;
                    for (int b = a + 1; b < splits.length; b++) {
                        final PartialSplit psb = splits[b];
                        final TaxaSet Ab, Bb;
                        if (psb.getA().cardinality() == 2) {
                            Ab = psb.getA();
                            Bb = psb.getB();
                        } else if (psb.getB().cardinality() == 2) {
                            Ab = psb.getB();
                            Bb = psb.getA();
                        } else
                            continue;
                        if (TaxaSet.intersection(Aa, Ab).cardinality() == 1
                                && Ba.intersects(Ab) == false && Bb.intersects(Aa) == false
                                && Ba.intersects(Bb) == true) {
                            PartialSplit ps=new PartialSplit(
                                    TaxaSet.union(Aa, Ab), TaxaSet.union(Ba, Bb));
                            if(partialSplits.contains(ps)==false)
                            {
                                partialSplits.add(ps);
                                count++;
                            }
                        */

                }
            }
            System.err.println("# Refinement heuristic [" + i + "] added " + count + " partial splits");
            if (count == 0)
                break;
        }
    }


    public boolean getOptionZRule() {
        return optionZRule;
    }

    public void setOptionZRule(boolean optionZRule) {
        this.optionZRule = optionZRule;
    }

    public boolean getNoOptionLeastSquare() {
        return optionLeastSquare;
    }

    public void setNoOptionLeastSquare(boolean optionLeastSquare) {
        this.optionLeastSquare = optionLeastSquare;
    }

    /**
     * which seed it to be used for the random runs ?
     *
     * @return optionRandomRunSeed
     */
    public int getNoOptionSeed() {
        return this.optionSeed;
    }

    public void setNoOptionSeed(int optionSeed) {
        this.optionSeed = optionSeed;
    }


    /**
     * how many runs with random permutations of the input splits shall be done ?
     *
     * @return number of runs to be done
     */
    public int getOptionNumberOfRuns() {
        return this.optionNumberOfRuns;
    }

    public void setOptionNumberOfRuns(int optionNumberOfRuns) {
        this.optionNumberOfRuns = Math.max(1, optionNumberOfRuns);
    }


    /**
     * do we want to force the resulting split system to have the strong nduction proerty?
     * The strong induction property is that if an output split induces a proper split on some input
     * taxon set, then that induced split is contained in the input tree
     *
     * @return true, if option is set
     */
    public boolean getOptionSuperTree() {
        return optionSuperTree;
    }

    public void setOptionSuperTree(boolean optionSuperTree) {
        this.optionSuperTree = optionSuperTree;
    }

    public String getOptionEdgeWeights() {
        return optionEdgeWeights;
    }

    public void setOptionEdgeWeights(String optionEdgeWeights) {
        this.optionEdgeWeights = optionEdgeWeights;
    }


    /**
     * return the possible chocies for optionEdgeWeights
     *
     * @return list of choices
     */
    public List selectionOptionEdgeWeights() {
        List list = new LinkedList();
        list.add(AVERAGERELATIVE);
        list.add(MEAN);
        list.add(TREESIZEWEIGHTEDMEAN);
        list.add(SUM);
        list.add(MIN);
        list.add(NONE);
        return list;
    }

    public boolean getOptionApplyRefineHeuristic() {
        return optionApplyRefineHeuristic;
    }

    public void setOptionApplyRefineHeuristic(boolean optionApplyRefineHeuristic) {
        this.optionApplyRefineHeuristic = optionApplyRefineHeuristic;
    }
}
