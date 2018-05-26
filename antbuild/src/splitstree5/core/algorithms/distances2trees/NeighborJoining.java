/*
 *  Copyright (C) 2018 Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
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

import java.util.HashMap;

/**
 * Neighbor joining algorithm
 * <p>
 * Created on 12/11/16. Original version : 2010-05-09.
 *
 * @author Christian Rausch, Daniel Huson and Daria Evseeva
 */
public class NeighborJoining extends Algorithm<DistancesBlock, TreesBlock> implements IFromDistances, IToTrees {

    @Override
    public String getCitation() {
        return "Saitou and Nei 1987; " +
                "N. Saitou and M. Nei. The Neighbor-Joining method: a new method for reconstructing phylogenetic trees. " +
                "Molecular Biology and Evolution, 4:406-425, 1987.";
    }

    /**
     * compute the neighbor joining tree
     *
     * @throws InterruptedException
     */
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, DistancesBlock distances, TreesBlock trees)
            throws InterruptedException, CanceledException {
        progress.setTasks("Neighbor Joining", "Init.");
        progress.setMaximum(distances.getNtax());

        PhyloTree tree = computeNJTree(progress, taxaBlock, distances);
        trees.getTrees().setAll(tree);

        progress.close();
    }

    private PhyloTree computeNJTree(ProgressListener progressListener, TaxaBlock taxaBlock, DistancesBlock distances)
            throws InterruptedException, CanceledException {
        final int nTax = distances.getNtax();
        final PhyloTree tree = new PhyloTree();
        final HashMap<String, Node> taxa2Nodes = new HashMap<>();
        final StringBuilder taxaLabels[] = new StringBuilder[nTax + 1];

        initialize(nTax, taxaBlock, tree, taxa2Nodes, taxaLabels);

        int i_min = 0, j_min = 0;
        double temp, dist2i, dist2j;  //new edge weights
        StringBuilder mergedTaxa_i; //labels of taxa that are being merged
        StringBuilder mergedTaxa_j;
        Node newParentNode;
        Edge edge2i, edge2j; //from tax_old to new=merged edge

        double[][] distanceMatrix = fillDistanceMatrix(nTax, distances);

        double[] divergences = new double[nTax + 1];
        // calculate divergences:
        for (int i = 1; i <= nTax; i++) {
            for (int j = 1; j <= nTax; j++) {
                divergences[i] += distanceMatrix[i][j];
            }
        }

        // actual for (finding all nearest Neighbors)
        for (int actual = nTax; actual > 2; actual--) {
            // find: min D (h, b, b)
            double minDist = Double.MAX_VALUE;
            for (int i = 1; i < nTax; i++) {
                if (distanceMatrix[0][i] == 0.0) continue;
                for (int j = i + 1; j <= nTax; j++) {
                    if (distanceMatrix[0][j] == 0.0)
                        continue;
                    if (distanceMatrix[i][j] - ((divergences[i] + divergences[j]) / (actual - 2)) < minDist) {
                        minDist = distanceMatrix[i][j] - ((divergences[i] + divergences[j]) / (actual - 2));
                        i_min = i;
                        j_min = j;
                    }
                }
            }
            dist2i = 0.5 * (distanceMatrix[i_min][j_min] + divergences[i_min] / (actual - 2)
                    - divergences[j_min] / (actual - 2));
            dist2j = 0.5 * (distanceMatrix[i_min][j_min] + divergences[j_min] / (actual - 2)
                    - divergences[i_min] / (actual - 2));

            distanceMatrix[j_min][0] = 0.0;// marking
            distanceMatrix[0][j_min] = 0.0;

            // tax taxa update:
            mergedTaxa_i = new StringBuilder(taxaLabels[i_min].toString());
            mergedTaxa_j = new StringBuilder(taxaLabels[j_min].toString());
            taxaLabels[i_min].insert(0, "(");
            taxaLabels[i_min].append(",");
            taxaLabels[i_min].append(taxaLabels[j_min]);
            taxaLabels[i_min].append(")");
            taxaLabels[j_min].delete(0, taxaLabels[j_min].length());

            // divergences update:

            divergences[i_min] = 0.0;
            divergences[j_min] = 0.0;

            // fusion of distance
            // double distance_min = distance[i_min][j_min];

            for (int i = 1; i <= nTax; i++) {
                if (distanceMatrix[0][i] == 0.0)
                    continue;
                temp = (distanceMatrix[i][i_min] + distanceMatrix[i][j_min] - dist2i - dist2j) / 2; // correct NJ
                if (i != i_min) {
                    divergences[i] = divergences[i] - distanceMatrix[i][i_min] - distanceMatrix[i][j_min] + temp;
                }
                divergences[i_min] += temp;
                distanceMatrix[i][i_min] = temp;
                divergences[j_min] = 0.0;
            }

            for (int i = 0; i <= nTax; i++) {
                distanceMatrix[i_min][i] = distanceMatrix[i][i_min];
                distanceMatrix[i][j_min] = 0.0;
                distanceMatrix[j_min][i] = 0.0;
            }

            // generate new Node for merged Taxa:
            newParentNode = tree.newNode();
            taxa2Nodes.put(taxaLabels[i_min].toString(), newParentNode);

            // generate Edges from two Taxa that are merged to one:
            edge2i = tree.newEdge(taxa2Nodes.get(mergedTaxa_i.toString()), newParentNode);
            tree.setWeight(edge2i, Math.max(dist2i, 0.0));
            edge2j = tree.newEdge(taxa2Nodes.get(mergedTaxa_j.toString()), newParentNode);
            tree.setWeight(edge2j, Math.max(dist2j, 0.0));

            progressListener.incrementProgress();
        }

        // evaluating last two nodes:
        for (int i = 1; i <= nTax; i++) {
            if (distanceMatrix[0][i] == 1.0) {
                i_min = i;
                i++;

                for (; i <= nTax; i++) {
                    if (distanceMatrix[0][i] == 1.0) {
                        j_min = i;
                    }
                }
            }
        }
        mergedTaxa_i = new StringBuilder(taxaLabels[i_min].toString());
        mergedTaxa_j = new StringBuilder(taxaLabels[j_min].toString());

        taxaLabels[i_min].insert(0, "(");
        taxaLabels[i_min].append(",");
        taxaLabels[i_min].append(taxaLabels[j_min]);
        taxaLabels[i_min].append(")");
        taxaLabels[j_min].delete(0, taxaLabels[j_min].length()); //not neces. but sets content to NULL

        // generate new Node for merged Taxa:
        // generate Edges from two Taxa that are merged to one:
        edge2i = tree.newEdge(taxa2Nodes.get(mergedTaxa_i.toString()), taxa2Nodes.get(mergedTaxa_j.toString()));
        tree.setWeight(edge2i, Math.max(distanceMatrix[i_min][j_min], 0.0));
        return tree;
    }

    private static void initialize(int nTax, TaxaBlock taxa, PhyloTree tree, HashMap<String, Node> taxa2nodes, StringBuilder[] labels) {
        for (int i = 1; i <= nTax; i++) {
            labels[i] = new StringBuilder();
            labels[i].append(taxa.getLabel(i));
            final Node v = tree.newNode(); // create newNode for each Taxon
            tree.setLabel(v, labels[i].toString());
            taxa2nodes.put(labels[i].toString(), v);
        }
    }

    private static double[][] fillDistanceMatrix(int nTax, DistancesBlock distances) {
        double[][] distanceMatrix = new double[nTax + 1][nTax + 1];
        for (int i = 1; i <= nTax; i++) {
            distanceMatrix[0][i] = 1.0; // with 1.0 marked columns indicate columns/rows
            distanceMatrix[i][0] = 1.0;// that haven't been deleted after merging
            for (int j = 1; j <= nTax; j++) {
                if (i < j)
                    distanceMatrix[i][j] = distances.get(i, j);
                else
                    distanceMatrix[i][j] = distances.get(j, i);
            }
        }
        distanceMatrix[0][0] = 1.0;
        return distanceMatrix;
    }

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, DistancesBlock parent) {
        return parent.getNtax() > 0;
    }
}
