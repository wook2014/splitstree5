package splitstree5.core.algorithms.distances2trees;

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromDistances;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;

/**
 * UPGMA classic n³ version
 * <p>
 * Created on 2010-02-04
 *
 * @author Christian Rausch, David Bryant, Daniel Huson
 */

public class UPGMA extends Algorithm<DistancesBlock, TreesBlock> implements IFromDistances, IToTrees {

    @Override
    public String getCitation() {
        return "Sokal and Michener 1958; " +
                "R.R. Sokal and C.D.Michener. A statistical method for evaluating systematic relationships. " +
                "University of Kansas Scientific Bulletin, 28:1409-1438, 1958.";
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, DistancesBlock distances, TreesBlock trees)
            throws InterruptedException, CanceledException {

        progress.setTasks("UPGMA", "Creating nodes...");
        progress.setMaximum(taxaBlock.getNtax());

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
            Edge e = tree.newEdge(v, subtrees[i_min]);
            tree.setWeight(e, Math.max(height - heights[i_min], 0.0));
            Edge f = tree.newEdge(v, subtrees[j_min]);
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

            progress.incrementProgress();
        }

        int brother = 1;

        int sister = brother + 1;
        while (subtrees[sister] == null)
            sister++;

        final Node root = tree.newNode();
        final Edge left = tree.newEdge(root, subtrees[brother]);
        final Edge right = tree.newEdge(root, subtrees[sister]);

        final double diff = (heights[brother] - heights[sister]);
        if (diff >= 0) {
            tree.setWeight(left, (d[brother][sister] - diff) / 2.0);
            tree.setWeight(right, (d[brother][sister] + diff) / 2.0);
        } else {
            tree.setWeight(left, (d[brother][sister] + diff) / 2.0);
            tree.setWeight(right, (d[brother][sister] - diff) / 2.0);
        }

        tree.setRoot(root);

        trees.getTrees().addAll(tree);
        progress.close();
    }
}
