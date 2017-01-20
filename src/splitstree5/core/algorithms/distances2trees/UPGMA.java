package splitstree5.core.algorithms.distances2trees;

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.Taxon;

public class UPGMA extends Algorithm<DistancesBlock, TreesBlock> {
    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, DistancesBlock distances, TreesBlock trees)
            throws InterruptedException, CanceledException {

        progressListener.setDebug(true);
        progressListener.setTasks("UPGMA", "Creating nodes...");
        progressListener.setMaximum(taxaBlock.getNtax());

        //PhyloTree tree = makeUPGMATree(progressListener, taxaBlock, distances);
        //trees.getTrees().addAll(tree);

        progressListener.close();
    }

    public static/*private*/ PhyloTree makeUPGMATree(/*ProgressListener progressListener*/ TaxaBlock taxaBlock, DistancesBlock distances) /*throws CanceledException*/ {

        PhyloTree tree = new PhyloTree();
        int ntax = distances.getNtax();

        Node[] subtrees = new Node[ntax + 1];
        int[] sizes = new int[ntax + 1];
        double[] heights = new double[ntax + 1];

        for (int i = 1; i <= ntax; i++) {
            subtrees[i] = tree.newNode();
            tree.setLabel(subtrees[i], taxaBlock.getLabel(i));
            sizes[i] = 1;
        }

        double d[][] = new double[ntax + 1][ntax + 1];// distance matix

        //Initialise d
        //Compute the closest values for each taxa.
        for (int i = 1; i <= ntax; i++) {
            for (int j = i + 1; j <= ntax; j++) {
                //d[i][j] = d[j][i] = (distances.get(i, j) + distances.get(j, i)) / 2.0;
                double sum = distances.get(i, j) + distances.get(j, i);
                if (sum == distances.get(i, j) || sum == distances.get(j, i)) {
                    d[i][j] = d[j][i] = sum;
                } else {
                    d[i][j] = d[j][i] = sum / 2.0;
                }
            }
        }

        int steps = 0;
        for (int actual = ntax; actual > 2; actual--) {

            int i_min = 0, j_min = 0;
            //Find closest pair.
            double d_min = Double.MAX_VALUE;
            for (int i = 1; i <= actual; i++) {
                for (int j = i + 1; j <= actual; j++) {
                    double dij = d[i][j];
                    if (i_min == 0 || dij < d_min) {
                        i_min = i;
                        j_min = j;
                        d_min = dij;
                    }
                }

            }


            double height = d_min / 2.0;

            Node v = tree.newNode();
            Edge e = tree.newEdge(subtrees[i_min], v);
            tree.setWeight(e, Math.max(height - heights[i_min], 0.0));
            Edge f = tree.newEdge(subtrees[j_min], v);
            tree.setWeight(f, Math.max(height - heights[j_min], 0.0));

            subtrees[i_min] = v;
            subtrees[j_min] = null;
            heights[i_min] = height;


            int size_i = sizes[i_min];
            int size_j = sizes[j_min];
            sizes[i_min] = size_i + size_j;

            for (int k = 1; k <= ntax; k++) {
                if ((k == i_min) || k == j_min) continue;
                double dki = (d[k][i_min] * size_i + d[k][j_min] * size_j) / ((double) (size_i + size_j));
                d[k][i_min] = d[i_min][k] = dki;
            }

            //Copy the top row of the matrix and arrays into the empty j_min row/column.
            if (j_min < actual) {
                for (int k = 1; k <= actual; k++) {
                    d[j_min][k] = d[k][j_min] = d[actual][k];
                }
                d[j_min][j_min] = 0.0;
                subtrees[j_min] = subtrees[actual];
                sizes[j_min] = sizes[actual];
                heights[j_min] = heights[actual];
            }

            steps += actual;
            //progressListener.incrementProgress();
        }

        int sister = 2;
        while (subtrees[sister] == null)
            sister++;

        final Node root = tree.newNode();
        final Edge left = tree.newEdge(root, subtrees[1]);
        final Edge right = tree.newEdge(root, subtrees[sister]);
        tree.setWeight(left, d[1][sister] / 2.0);
        tree.setWeight(right, d[1][sister] / 2.0);
        tree.setRoot(root);
        return tree;
    }

    // TESTING
    public static void testApply(TaxaBlock taxaBlock, DistancesBlock distances, TreesBlock trees){
        PhyloTree tree = makeUPGMATree(taxaBlock, distances);
        //PhyloTree tree = new PhyloTree();
        System.out.println("output: "+tree.toString());
    }

    public static void main(String[] args) {
        String[] names = {"a","b","c","d","e"};
        Taxon[] taxons = new Taxon[5];
        TaxaBlock taxaBlock = new TaxaBlock();
        DistancesBlock distancesBlock = new DistancesBlock();
        TreesBlock treesBlock = new TreesBlock();

        for(int i=0; i<names.length; i++){
            taxons[i] = new Taxon();
            taxons[i].setName(names[i]);
            taxaBlock.getTaxa().add(taxons[i]);
        }

        double[][] dist = {{0, 17, 21, 31, 23},
                {17, 0,	30,	34,	21},
                {21, 30, 0, 28, 39},
                {31, 34, 28, 0, 43},
                { 23, 21, 39, 43, 0}};

        /*double[][] dist = {{0, 0, 0, 0, 0},
                {17, 0,	0,	0,	0},
                {21, 30, 0, 0, 0},
                {31, 34, 28, 0, 0},
                { 23, 21, 39, 43, 0}};*/

        distancesBlock.set(dist);

        testApply(taxaBlock,distancesBlock,treesBlock);

        /*PhyloTree tree = new PhyloTree();
        Node a = tree.newNode();
        Node b = tree.newNode();
        Node c = tree.newNode();
        Node d = tree.newNode();
        Node Ncd = tree.newNode();

        Edge ab = tree.newEdge(a, b);
        Edge cN = tree.newEdge(Ncd, c);
        Edge dN = tree.newEdge(Ncd, d);
        Edge acd = tree.newEdge(a, Ncd);

        System.out.println("output: "+tree.toString());*/

        //output: (((a:8.5,b:8.5):2.5,e:11.0):16.5,(d:14.0,c:14.0):16.5)root
    }
}
