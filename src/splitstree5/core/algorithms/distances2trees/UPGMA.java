package splitstree5.core.algorithms.distances2trees;

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;

public class UPGMA extends Algorithm<DistancesBlock, TreesBlock> {
    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, DistancesBlock distances, TreesBlock trees)
            throws InterruptedException, CanceledException {

        //PhyloTree tree = makeUPGMATree(taxaBlock, distances);

    }

    private PhyloTree makeUPGMATree(TaxaBlock taxaBlock, DistancesBlock distances) throws CanceledException {

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
                d[i][j] = d[j][i] = (distances.get(i, j) + distances.get(j, i)) / 2.0;
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
        }

        int sister = 2;
        while (subtrees[sister] == null)
            sister++;

        //ToDo: fix phyloTree and get this to return a rooted tree.
        //Edge e = tree.newEdge(subtrees[1], subtrees[sister]);
        //tree.setWeight(e, d[1][sister]);

        // root ???
        Node root = tree.newNode();
        Edge left = tree.newEdge(root, subtrees[1]);
        Edge right = tree.newEdge(root, subtrees[sister]);
        tree.setWeight(left,  d[1][sister]/2.0);
        tree.setWeight(right,  d[1][sister]/2.0);
        tree.setRoot(root);

        return tree;
    }
}
