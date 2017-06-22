package splitstree5.core.algorithms.trees2splits;

import javafx.beans.property.SimpleObjectProperty;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.*;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.algorithms.splits2splits.LeastSquaresWeights;
import splitstree5.core.algorithms.trees2distances.AverageDistances;
import splitstree5.core.algorithms.trees2splits.utils.PartialSplit;
import splitstree5.core.datablocks.DistancesBlock;
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
    private boolean optionLeastSquare = false; // todo ???
    private boolean optionSuperTree = false;
    private int optionNumberOfRuns = 1;
    private boolean optionApplyRefineHeuristic = false;
    private int optionSeed = 0;

    // edge weight options:
    public enum EdgeWeights {AverageRelative, Mean, TreeSizeWeightedMean, Sum, Min, None}
    private final SimpleObjectProperty<EdgeWeights> optionEdgeWeights = new SimpleObjectProperty<>(EdgeWeights.TreeSizeWeightedMean);

    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, TreesBlock treesBlock, SplitsBlock splitsBlock)
            throws Exception {

        /**
         * Determine the set of taxa for partial trees.
         * If the block contains partial trees, then the translate statement must mention all
         * taxa. We use this info to build a taxa block
         */
        // todo move to trees block?
        /*if (treesBlock.isPartial()){

            // trees.setTaxaFromPartialTrees(taxa);

            // contains partial trees, most determine
            // full set of taxa
            Set<String> taxaLabels = new HashSet<>();
            for (int i = 0; i < treesBlock.getNTrees(); i++) {
                PhyloTree tree = treesBlock.getTrees().get(i);
                Set<String> nodeLabels = tree.getNodeLabels();

                for (String nodeLabel : nodeLabels) {
                    //taxaLabels.add(translate.get(nodeLabel));
                    taxaLabels.add(nodeLabel);
                }
            }

            //are these taxa equal taxa, if so, do nothing:
            boolean areLabelsInTaxa = true;
            for(String label : taxaLabels){
                if(!taxaBlock.getLabels().contains(label)) {
                    areLabelsInTaxa = false;
                    break;
                }
            }
            if (taxaBlock.getNtax() == taxaLabels.size() && areLabelsInTaxa)
                return;

            // if they are contained in the original taxa, unhide them:
            if (taxaBlock.getTaxa() != null && areLabelsInTaxa) {
                BitSet toHide = new BitSet();
                for (int t = 1; t <= taxa.getOriginalTaxa().getNtax(); t++)
                    if (!taxaLabels.contains(taxa.getOriginalTaxa().getLabel(t)))
                        toHide.set(t);
                taxa.hideTaxa(toHide);
            } else {
                taxa.setNtax(taxaLabels.size());
                Iterator it = taxaLabels.iterator();
                int t = 0;
                while (it.hasNext()) {
                    taxa.setLabel(++t, (String) it.next());
                }
            }

        }*/


        progressListener.setTasks("Z-closure", "init");

        Map[] pSplitsOfTrees = new Map[treesBlock.getNTrees() + 1];
        // for each tree, identity map on set of splits
        BitSet[] supportSet = new BitSet[treesBlock.getNTrees() + 1];
        Set allPSplits = new HashSet();

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
                        progressListener.incrementProgress();
                    }
                }
            } catch (NotOwnerException e) {
                Basic.caught(e);
            }
        }
        SplitsBlock splits = new SplitsBlock();

        if (getOptionZRule()) {
            computeClosureOuterLoop(progressListener, taxaBlock, allPSplits);
        }

        if (getOptionApplyRefineHeuristic()) {
            progressListener.setSubtask("Refinement heuristic");
            applyRefineHeuristic(allPSplits);
        }

        ////doc.notifySubtask("collecting full splits");
        ////doc.notifySetMaximumProgress(allPSplits.size());
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
            BitSet ts1 = new BitSet();
            ts1.set(1, taxaBlock.getNtax()+1);
            ps.setComplement(ts1);
            if (!allPSplits.contains(ps)){
                ASplit split = new ASplit(ps.getA(), taxaBlock.getNtax());
                splits.getSplits().add(split);
            }

        }

        if (getOptionEdgeWeights().equals(EdgeWeights.AverageRelative)) {
            setWeightAverageReleativeLength(pSplitsOfTrees, supportSet, taxaBlock, splits);
        } else if (!getOptionEdgeWeights().equals(EdgeWeights.None)) {
            setWeightsConfidences(pSplitsOfTrees, supportSet, taxaBlock, splits);
        }

        // todo how do we get here ?
        if (getNoOptionLeastSquare()) {
            if (!TreesUtilities.hasAllPairs(taxaBlock, treesBlock)) {
                new Alert("Partial trees don't have the 'All Pairs' property,\n" +
                        "can't apply Least Squares");
                setNoOptionLeastSquare(false);
            } else {
                DistancesBlock distances = new DistancesBlock();
                AverageDistances ad = new AverageDistances();
                ad.compute(new ProgressPercentage(), taxaBlock, treesBlock, distances);

                LeastSquaresWeights leastSquares = new LeastSquaresWeights();
                leastSquares.setDistancesBlock(distances);

                leastSquares.compute(new ProgressPercentage(), taxaBlock, splits, splitsBlock);
            }
        }

        splitsBlock.copy(splits);
        progressListener.close();
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
                case Min:
                    value = min;
                    break;
                case Mean:
                    value = weighted / total;
                    break;
                case TreeSizeWeightedMean:
                    value = sum / total;
                    break;
                case Sum:
                    value = sum;
                    break;
            }
            splits.getSplits().get(s-1).setWeight(value);
            splits.getSplits().get(s-1).setConfidence(total);
        }
    }

    /**
     * sets the weight of a split in the network as the average relative length of the edge
     * in the input trees
     *
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
        PhyloTree tree = trees.getTrees().get(which-1);
        BitSet e_taxa = new BitSet();
        if (taxa.indexOf(tree.getLabel(v)) != -1)
            e_taxa.set(taxa.indexOf(tree.getLabel(v)));

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
     * @param partialSplits
     * @throws CanceledException
     */
    private void applyRefineHeuristic(Set partialSplits) throws CanceledException {


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

    public EdgeWeights getOptionEdgeWeights() {
        return this.optionEdgeWeights.get();
    }

    public SimpleObjectProperty<EdgeWeights> optionEdgeWeightsProperty() {
        return this.optionEdgeWeights;
    }

    public void setOptionEdgeWeights(EdgeWeights optionEdgeWeights) {
        this.optionEdgeWeights.set(optionEdgeWeights);
    }

    public boolean getOptionApplyRefineHeuristic() {
        return optionApplyRefineHeuristic;
    }

    public void setOptionApplyRefineHeuristic(boolean optionApplyRefineHeuristic) {
        this.optionApplyRefineHeuristic = optionApplyRefineHeuristic;
    }
}
