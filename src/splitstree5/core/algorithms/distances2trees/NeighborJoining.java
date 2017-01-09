/*
 *  Copyright (C) 2016 Daniel H. Huson
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


import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;

import java.util.Vector;

/**
 * Neighbor joining algorithm
 * Created by huson on 12/11/16.
 */
public class NeighborJoining extends Algorithm<DistancesBlock, TreesBlock> {

    /**
     * compute the neighbor joining tree
     *
     * @throws InterruptedException
     */
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, DistancesBlock distances, TreesBlock trees) throws InterruptedException, CanceledException {
        progressListener.setDebug(true);
        progressListener.setTasks("Simulating NJ", "Waiting...");
        progressListener.setMaximum(10);
        for (int i = 0; i < 10; i++) {
            Thread.sleep(400);
            progressListener.incrementProgress();
        }
        progressListener.close();
        trees.getTrees().setAll(new PhyloTree());

        //start implemetation
        int nTax = distances.getNtax();
        double[][] distanceMatrix = fillDistanceMatrix(nTax, distances);
        PhyloTree NJTree = setInitialTree(taxaBlock);

    }

    // Functions

    private double[][] fillDistanceMatrix(int nTax, DistancesBlock distances){
        double[][] distanceMatrix = new double[nTax+1][nTax+1];
        for(int i = 0; i<nTax+1; i++){
            distanceMatrix[nTax][i] = 1.0; // with 1.0 marked columns indicate columns/rows
            distanceMatrix[i][nTax] = 1.0;// that haven't been deleted after merging
            for(int j = 0; j<nTax; j++){
                if (i < j)
                    distanceMatrix[i][j] = distances.get(i, j);
                else
                    distanceMatrix[i][j] = distances.get(j, i);
            }
        }
        //System.arraycopy(distances.getDistances(), 0, distanceMatrix, 0, nTax);
        return distanceMatrix;
    }

    /**
     * Setting of the initial star tree
     *
     * @param taxaBlock
     * @return
     */
    private PhyloTree setInitialTree(TaxaBlock taxaBlock){
        PhyloTree tree = new PhyloTree();
        for(int i = 0; i<taxaBlock.getNtax(); i++){
            Node init = tree.newNode();
            tree.setLabel(init, taxaBlock.getLabel(i));
        }
        return tree;
    }

    private void computeQ(double[][] distances, double[] divergences){
        int n = distances.length;
        double[][] Qmatrix = new double[n][n];

        for(int i = 0; i<n; i++){
            for(int j = 0; j<n; j++){
                Qmatrix[i][j] = (n-2)*(distances[i][j]-divergences[i]-divergences[j]);
            }
        }
    }

    //divergences
    private double[] computeDivergences(double[][] distances){
        double[] divergences = new double[distances.length];
        for(int i = 0; i<divergences.length; i++){
            for (int j = 0; j < distances.length-1; j++) {
                divergences[i] += distances[i][j];
            }
        }
        return  divergences;
    }


    // TESTING
    public static void main(String[] args) {
        int[][] b = {{1,2},{3,4}};
        int[][] a = new int[2][2];
        System.arraycopy(b, 0, a,0, b.length);
        for(int i = 0; i<a.length; i++){
            for(int j = 0; j<a[i].length; j++){
                System.out.println(a[i][j]);
            }
            System.out.println("---");
        }

    }
}

