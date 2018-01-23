package splitstree5.core.algorithms.characters2distances;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.characters.CharactersType;

import java.util.BitSet;

/**
 * Gene content distance
 * Daniel Huson, 2004
 */

public class GeneContentDistance extends Algorithm<CharactersBlock, DistancesBlock> implements IFromChararacters, IToDistances {

    public final static String DESCRIPTION = "Compute distances based on shared genes (Snel Bork et al 1999, Huson and Steel 2003)";
    private boolean useMLDistance = false;

    @Override
    public String getCitation() {
        return "Huson and Steel 2004; " +
                "D.H. Huson  and  M. Steel. Phylogenetic  trees  based  on  gene  content. Bioinformatics, 20(13):2044–9, 2004.";
    }

    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock)
            throws Exception {

        System.err.println("Not tested under construction");
        /*@todo: test this class
         */
        BitSet genes[] = computeGenes(charactersBlock);
        if (!useMLDistance)
            computeSnelBorkDistance(distancesBlock, taxaBlock.getNtax(), genes);
        else
            computeMLDistance(distancesBlock, taxaBlock.getNtax(), genes);
    }

    /**
     * Determine whether the gene content distance can be computed with given data.
     *
     * @param taxa the taxa
     * @param ch   the characters matrix
     * @return true, if method applies to given data
     */
    public boolean isApplicable(TaxaBlock taxa, CharactersBlock ch) {
        //return taxa != null && ch != null && ch.getFormat().getDatatype().equalsIgnoreCase(Characters.Datatypes.STANDARD);
        return taxa != null && ch != null && ch.getDataType().equals(CharactersType.standard);
    }

    /**
     * comnputes the SnelBork et al distance
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
     * comnputes the maximum likelihood estimator distance Huson and Steel 2003
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

        double ai[] = new double[ntax + 1];
        double aij[][] = new double[ntax + 1][ntax + 1];
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
        BitSet genes[] = new BitSet[characters.getNtax() + 1];

        for (int s = 1; s <= characters.getNtax(); s++) {
            genes[s] = new BitSet();
            for (int i = 1; i <= characters.getNchar(); i++) {
                if (characters.get(s, i) == '1')
                    genes[s].set(i);
            }
        }
        return genes;
    }

    //GETTER AND SETTER

    public boolean getOptionUseMLDistance() {
        return useMLDistance;
    }

    public void setOptionUseMLDistance(boolean useMLDistance) {
        this.useMLDistance = useMLDistance;
    }

    public String getDescription() {
        return DESCRIPTION;
    }
}
