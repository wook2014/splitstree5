/*
 * UPGMA.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.core.algorithms.distances2trees;

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.CanceledException;
import jloda.util.progress.ProgressListener;
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
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, DistancesBlock distances, TreesBlock trees) throws CanceledException {
        progress.setTasks("UPGMA", "Creating nodes...");
        progress.setMaximum(taxaBlock.getNtax());

        trees.setRooted(true);

        final PhyloTree tree = new PhyloTree();
        final int ntax = distances.getNtax();

        final Node[] subtrees = new Node[ntax + 1];
        final int[] sizes = new int[ntax + 1];
        double[] heights = new double[ntax + 1];

        for (int t = 1; t <= ntax; t++) {
            final Node v = tree.newNode();
            subtrees[t] = v;
            tree.setLabel(v, taxaBlock.getLabel(t));
            tree.addTaxon(v, t);
            sizes[t] = 1;
        }

        final double[][] d = new double[ntax + 1][ntax + 1];// distance matix

        //Initialise d
        //Compute the closest values for each taxa.
        for (int i = 1; i <= ntax; i++) {
            for (int j = i + 1; j <= ntax; j++) {
                //d[i][j] = d[j][i] = (distances.get(i, j) + distances.get(j, i)) / 2.0;
                final double sum = distances.get(i, j) + distances.get(j, i);
                if (sum == distances.get(i, j) || sum == distances.get(j, i)) {
                    d[i][j] = d[j][i] = sum;
                } else {
                    d[i][j] = d[j][i] = sum / 2.0;
                }
            }
        }

        for (int clusters = ntax; clusters > 2; clusters--) {
            int i_min = 0, j_min = 0;
            //Find closest pair.
            double d_min = Double.POSITIVE_INFINITY;
            for (int i = 1; i <= clusters; i++) {
                for (int j = i + 1; j <= clusters; j++) {
                    double dij = d[i][j];
                    if (i_min == 0 || dij < d_min) {
                        i_min = i;
                        j_min = j;
                        d_min = dij;
                    }
                }
            }

            final double height = d_min / 2.0;

            final Node v = tree.newNode();
            final Edge e = tree.newEdge(v, subtrees[i_min]);
            tree.setWeight(e, Math.max(height - heights[i_min], 0.0));
            final Edge f = tree.newEdge(v, subtrees[j_min]);
            tree.setWeight(f, Math.max(height - heights[j_min], 0.0));

            subtrees[i_min] = v;
            subtrees[j_min] = null;
            heights[i_min] = height;


            final int size_i = sizes[i_min];
            final int size_j = sizes[j_min];
            sizes[i_min] = size_i + size_j;

            for (int k = 1; k <= ntax; k++) {
                if ((k == i_min) || k == j_min) continue;
                final double dki = (d[k][i_min] * size_i + d[k][j_min] * size_j) / ((double) (size_i + size_j));
                d[k][i_min] = d[i_min][k] = dki;
            }

            //Copy the top row of the matrix and arrays into the empty j_min row/column.
            if (j_min < clusters) {
                for (int k = 1; k <= clusters; k++) {
                    d[j_min][k] = d[k][j_min] = d[clusters][k];
                }
                d[j_min][j_min] = 0.0;
                subtrees[j_min] = subtrees[clusters];
                sizes[j_min] = sizes[clusters];
                heights[j_min] = heights[clusters];
            }

            progress.incrementProgress();
        }

        final int brother = 1;

        int sister = brother + 1;
        while (subtrees[sister] == null)
            sister++;

        final Node root = tree.newNode();
        final Edge left = tree.newEdge(root, subtrees[brother]);
        final Edge right = tree.newEdge(root, subtrees[sister]);

        final double halfTotal = 0.5 * (d[brother][sister] + heights[brother] + heights[sister]);
        tree.setWeight(left, halfTotal - heights[brother]);
        tree.setWeight(right, halfTotal - heights[sister]);

        tree.setRoot(root);

        trees.getTrees().addAll(tree);
        progress.close();
    }


    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, DistancesBlock parent) {
        return parent.getNtax() > 0;
    }
}
