/*
 * BioNJ.java Copyright (C) 2021. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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

import java.util.BitSet;
import java.util.HashMap;


/**
 * Implementation of the Bio-Neighbor-Joining algorithm (Gascuel 1997)
 * <p>
 * Created on 2008-02-26
 *
 * @author David Bryant and Daniel Huson
 */

public class BioNJ extends Algorithm<DistancesBlock, TreesBlock> implements IFromDistances, IToTrees {

    @Override
    public String getCitation() {
        return "Gascuel 1997; " +
                "O. Gascuel, BIONJ: an improved version of the NJ algorithm based on a simple model of sequence data. " +
                "Molecular Biology and Evolution. 1997 14:685-695.";
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, DistancesBlock distances, TreesBlock trees) throws InterruptedException, CanceledException {
         progress.setTasks("BioNJ", "Creating nodes...");
        progress.setMaximum(distances.getNtax());

        PhyloTree tree = computeBioNJTree(progress, taxaBlock, distances);
        trees.getTrees().setAll(tree);

        progress.close();
    }

    /**
     * compute the BIO nj tree
     *
     * @param progressListener
     * @param taxaBlock
     * @param distances
     * @return tree
     * @throws InterruptedException
     * @throws CanceledException
     */
    private PhyloTree computeBioNJTree(ProgressListener progressListener, TaxaBlock taxaBlock, DistancesBlock distances) throws InterruptedException, CanceledException {

        final PhyloTree tree = new PhyloTree();
        final HashMap<String, Node> taxaHashMap = new HashMap<>();
        final int nTax = distances.getNtax();

        final StringBuffer[] tax = new StringBuffer[nTax + 1];

        for (int t = 1; t <= nTax; t++) {
            tax[t] = new StringBuffer();
            tax[t].append(t);
            final Node v = tree.newNode(); // create newNode for each Taxon
            tree.setLabel(v, taxaBlock.getLabel(t));
            taxaHashMap.put(tax[t].toString(), v);
            tree.addTaxon(v, t);
        }

        final double[][] h = new double[nTax + 1][nTax + 1];// distance matrix

        final BitSet active = new BitSet();

        final double[][] var = new double[nTax + 1][nTax + 1]; // variances matrix. This really should be upper diag of h.
        final double[] b = new double[nTax + 1];// the b variable in Neighbor Joining
        int i_min = 0, j_min = 0; // needed for manipulation of h and b
        double temp, dist_e, dist_f;//new edge weights
        StringBuffer tax_old_i; //labels of taxa that are being merged
        StringBuffer tax_old_j;
        StringBuffer tax_old_k;
        Node v;
        Edge e, f; //from tax_old to new=merged edge
        double lambda; //lambda value in BioNJ

        active.set(1, nTax + 1);

        for (int i = 1; i <= nTax; i++) {
            h[i][i] = 0.0;
            for (int j = 1; j <= nTax; j++) { //fill up the distance matix h
                if (i < j)
                    h[i][j] = distances.get(i, j);//
                else
                    h[i][j] = distances.get(j, i);
                var[i][j] = h[i][j];
            }
        }

        // calculate b:
        for (int i = 1; i <= nTax; i++) {
            for (int j = 1; j <= nTax; j++) {
                b[i] += h[i][j];
            }
        }
        // recall: int i_min=0, j_min=0;

        // actual for (finding all nearest Neighbors)
        for (int actual = nTax; actual > 3; actual--) {
            // find: min D (h, b, b)
            double d_min = Double.POSITIVE_INFINITY, d_ij;
            for (int i = 1; i < nTax; i++) {
                if (!active.get(i)) continue;
                for (int j = i + 1; j <= nTax; j++) {
                    if (!active.get(j))
                        continue;
                    d_ij = ((double) actual - 2.0) * h[i][j] - b[i] - b[j];
                    if (d_ij < d_min) {
                        d_min = d_ij;
                        i_min = i;
                        j_min = j;
                    }
                }
            }
            dist_e = 0.5 * (h[i_min][j_min] + b[i_min] / ((double) actual - 2.0)
                    - b[j_min] / ((double) actual - 2.0));
            dist_f = h[i_min][j_min] - dist_e;
            //dist_f=0.5*(h[i_min][j_min] + b[j_min]/((double)actual-2.0)
            //	- b[i_min]/((double)actual-2.0) );

            active.set(j_min, false);

            // tax taxa update:
            tax_old_i = new StringBuffer(tax[i_min].toString());
            tax_old_j = new StringBuffer(tax[j_min].toString());
            tax[i_min].insert(0, "(");
            tax[i_min].append(",");
            tax[i_min].append(tax[j_min]);
            tax[i_min].append(")");
            tax[j_min].delete(0, tax[j_min].length());

            // b update:

            b[i_min] = 0.0;
            b[j_min] = 0.0;

            // fusion of h
            // double h_min = h[i_min][j_min];
            double var_min = var[i_min][j_min]; //Variance of the distance between i_min and j_min

            //compute lambda to minimize the variances of the new distances
            lambda = 0.0;
            if ((var_min == 0.0) || (actual == 3))
                lambda = 0.5;
            else {
                for (int i = 1; i <= nTax; i++) {
                    if ((i_min != i) && (j_min != i) && (h[0][i] != 0.0))
                        lambda += var[i_min][i] - var[j_min][i];
                }
                lambda = 0.5 + lambda / (2.0 * (actual - 2) * var_min);
                if (lambda < 0.0)
                    lambda = 0.0;
                if (lambda > 1.0)
                    lambda = 1.0;
            }


            for (int i = 1; i <= nTax; i++) {
                if ((i == i_min) || (!active.get(i)))
                    continue;
                //temp=(h[i][i_min] + h[i][j_min] - h_min)/2; NJ                                        //temp=(h[i][i_min] + h[i][j_min] - dist_e - dist_f)/2; NJ
                temp = (1.0 - lambda) * (h[i][i_min] - dist_e) + (lambda) * (h[i][j_min] - dist_f); //BioNJ

                if (i != i_min) {
                    b[i] = b[i] - h[i][i_min] - h[i][j_min] + temp;
                }
                b[i_min] += temp;
                h[i_min][i] = h[i][i_min] = temp; //WARNING... this can affect updating of b[i]
                //Update variances
                var[i_min][i] = (1.0 - lambda) * var[i_min][i] + (lambda) * var[j_min][i] - lambda * (1.0 - lambda) * var_min;
                var[i][i_min] = var[i_min][i];
            }

            for (int i = 1; i <= nTax; i++) {
                h[i_min][i] = h[i][i_min];
                h[i][j_min] = 0.0;
                h[j_min][i] = 0.0;
            }

            // generate new Node for merged Taxa:
            v = tree.newNode();
            taxaHashMap.put(tax[i_min].toString(), v);

            // generate Edges from two Taxa that are merged to one:
            e = tree.newEdge((Node) taxaHashMap.get(tax_old_i.toString()), v);
            tree.setWeight(e, dist_e);
            f = tree.newEdge((Node) taxaHashMap.get(tax_old_j.toString()), v);
            tree.setWeight(f, dist_f);
            progressListener.incrementProgress();
        }

        // evaluating last three nodes:
        int k_min, i;
        i = 1;
        while (!active.get(i))
            i++;
        i_min = i;
        i++;
        while (!active.get(i))
            i++;
        j_min = i;
        i++;
        while (!active.get(i))
            i++;
        k_min = i;

        tax_old_i = new StringBuffer(tax[i_min].toString());
        tax_old_j = new StringBuffer(tax[j_min].toString());
        tax_old_k = new StringBuffer(tax[k_min].toString());

        tax[i_min].insert(0, "(");
        tax[i_min].append(",");
        tax[i_min].append(tax[j_min]);
        tax[i_min].append(",");
        tax[i_min].append(tax[k_min]);
        tax[i_min].append(")");
        tax[j_min].delete(0, tax[j_min].length()); //not neces. but sets content to NULL
        tax[k_min].delete(0, tax[k_min].length()); //not neces. but sets content to NULL

        // System.err.println(tax[i_min].toString());

        // generate new Node for the root of the tree.
        v = tree.newNode();
        taxaHashMap.put(tax[i_min].toString(), v);
        e = tree.newEdge(taxaHashMap.get(tax_old_i.toString()), v);
        tree.setWeight(e, 0.5 * (h[i_min][j_min] + h[i_min][k_min] - h[j_min][k_min]));
        e = tree.newEdge(taxaHashMap.get(tax_old_j.toString()), v);
        tree.setWeight(e, 0.5 * (h[i_min][j_min] + h[j_min][k_min] - h[i_min][k_min]));
        e = tree.newEdge(taxaHashMap.get(tax_old_k.toString()), v);
        tree.setWeight(e, 0.5 * (h[i_min][k_min] + h[j_min][k_min] - h[i_min][j_min]));
        return tree;
    }

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, DistancesBlock parent) {
        return parent.getNtax() > 0;
    }
}
