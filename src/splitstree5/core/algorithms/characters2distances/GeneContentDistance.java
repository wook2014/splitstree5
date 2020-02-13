/*
 * GeneContentDistance.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.core.algorithms.characters2distances;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.characters.CharactersType;

import java.util.BitSet;
import java.util.Collections;
import java.util.List;

/**
 * Gene content distance
 * Daniel Huson, 2004
 */

public class GeneContentDistance extends Algorithm<CharactersBlock, DistancesBlock> implements IFromChararacters, IToDistances {
    public final static String DESCRIPTION = "Compute distances based on shared genes (Snel Bork et al 1999, Huson and Steel 2003)";

    private BooleanProperty optionUseML_DistancesDistance = new SimpleBooleanProperty(false);

    @Override
    public String getCitation() {
        return "Huson and Steel 2004; D.H. Huson  and  M. Steel. Phylogenetic  trees  based  on  gene  content. Bioinformatics, 20(13):2044–9, 2004.";
    }

    public List<String> listOptions() {
        return Collections.singletonList("UseMLDistance");
    }

    @Override
    public String getToolTip(String optionName) {
        if (optionName.equals("UseMLDistance"))
            return "Use maximum likelihood distance estimation";
        else
            return optionName;
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock) throws Exception {

        System.err.println("Not tested under construction");
        /*@todo: test this class

        todo: don't work for useMLDistance! also in ST4
         */
        final BitSet[] genes = computeGenes(charactersBlock);
        if (!optionUseML_DistancesDistance.getValue())
            computeSnelBorkDistance(distancesBlock, taxaBlock.getNtax(), genes);
        else
            computeMLDistance(distancesBlock, taxaBlock.getNtax(), genes);
    }

    /**
     * computes the SnelBork et al distance
     *
     * @param ntax
     * @param genes
     * @return the distance Object
     */
    private static void computeSnelBorkDistance(DistancesBlock dist, int ntax, BitSet[] genes) {

        dist.setNtax(ntax);
        for (int i = 1; i <= ntax; i++) {
            dist.set(i, i, 0.0);
            for (int j = i + 1; j <= ntax; j++) {
                BitSet intersection = ((BitSet) (genes[i]).clone());
                intersection.and(genes[j]);
                dist.set(j, i, (float) (1.0 - ((float) intersection.cardinality() / (float) Math.min(genes[i].cardinality(), genes[j].cardinality()))));
                dist.set(i, j, dist.get(j, i));
            }
        }
    }

    /**
     * computes the maximum likelihood estimator distance Huson and Steel 2003
     *
     * @param ntax
     * @param genes
     * @return the distance Object
     */
    private static void computeMLDistance(DistancesBlock dist, int ntax, BitSet[] genes) {
        dist.setNtax(ntax);
        // dtermine average genome size:
        double m = 0;
        for (int i = 1; i <= ntax; i++) {
            m += genes[i].cardinality();
        }
        m /= ntax;

        final double[] ai = new double[ntax + 1];
        final double[][] aij = new double[ntax + 1][ntax + 1];
        for (int i = 1; i <= ntax; i++) {
            ai[i] = ((double) genes[i].cardinality()) / m;
        }
        for (int i = 1; i <= ntax; i++) {
            for (int j = i + 1; j <= ntax; j++) {
                BitSet intersection = ((BitSet) (genes[i]).clone());
                intersection.and(genes[j]);
                aij[i][j] = aij[j][i] = ((double) intersection.cardinality()) / m;
            }
        }

        for (int i = 1; i <= ntax; i++) {
            dist.set(i, i, 0.0);
            for (int j = i + 1; j <= ntax; j++) {
                double b = 1.0 + aij[i][j] - ai[i] - ai[j];

                dist.set(j, i, (float) -Math.log(0.5 * (b + Math.sqrt(b * b + 4.0 * aij[i][j] * aij[i][j]))));
                if (dist.get(j, i) < 0)
                    dist.set(j, i, 0.0);
                dist.set(i, j, dist.get(j, i));
            }
        }
    }


    /**
     * computes gene sets from strings
     *
     * @param characters object wich holds the sequences
     * @return sets of genes
     */
    static private BitSet[] computeGenes(CharactersBlock characters) {
        final BitSet[] genes = new BitSet[characters.getNtax() + 1];

        for (int s = 1; s <= characters.getNtax(); s++) {
            genes[s] = new BitSet();
            for (int i = 1; i <= characters.getNchar(); i++) {
                if (characters.get(s, i) == '1')
                    genes[s].set(i);
            }
        }
        return genes;
    }

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, CharactersBlock parent) {
        return parent.getDataType() == CharactersType.Standard && parent.getSymbols().contains("1");
    }

    //GETTER AND SETTER

    public boolean getOptionUseML_DistancesDistance() {
        return optionUseML_DistancesDistance.getValue();
    }

    public BooleanProperty optionUseML_DistancesDistanceProperty() {
        return optionUseML_DistancesDistance;
    }

    public void setOptionUseML_DistancesDistance(boolean useMLDistance) {
        this.optionUseML_DistancesDistance.set(useMLDistance);
    }
}
