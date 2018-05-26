package splitstree5.core.algorithms.characters2distances;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.characters2distances.utils.FixUndefinedDistances;
import splitstree5.core.algorithms.characters2distances.utils.PairwiseCompare;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.characters.CharactersType;

/**
 * Implements the Upholt (1979) distance for restriction site data.
 *
 * @author bryant
 */

public class Upholt extends Algorithm<CharactersBlock, DistancesBlock> implements IFromChararacters, IToDistances {
    private double optionRestrictionSiteLength = 6.0;

    public final boolean EXPERT = false;
    public final static String DESCRIPTION = "Calculates the Upholt (1979) distance for restriction site data";

    @Override
    public String getCitation() {
        return "Upholt 1979;";
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock)
            throws Exception {

        int ntax = taxaBlock.getNtax();
        distancesBlock.setNtax(ntax);

        progress.setTasks("Upholt distance", "Init.");
        progress.setMaximum(ntax);

        double maxDist = 0.0;
        int numUndefined = 0;

        for (int s = 1; s <= ntax; s++) {
            for (int t = s + 1; t <= ntax; t++) {
                //System.err.println(s+","+t);
                PairwiseCompare seqPair = new PairwiseCompare(charactersBlock, s, t);
                double[][] F = seqPair.getF();
                double dist = -1.0;

                if (F == null)
                    numUndefined++;
                else {

                    double ns = F[1][0] + F[1][1];
                    double nt = F[0][1] + F[1][1];
                    double nst = F[1][1];

                    if (nst == 0) {
                        numUndefined++;
                        dist = -1;
                    } else {
                        double s_hat = 2.0 * nst / (ns + nt);
                        dist = -Math.log(s_hat) / optionRestrictionSiteLength;
                    }

                }

                distancesBlock.set(s, t, dist);
                distancesBlock.set(t, s, dist);
                if (dist > maxDist)
                    maxDist = dist;
            }
            progress.incrementProgress();
        }
        if (numUndefined > 0)
            FixUndefinedDistances.apply(ntax, maxDist, distancesBlock);

        progress.close();
    }

    /**
     * Determine whether Upholt distances can be computed with given data.
     *
     * @param taxa the taxa
     * @param c    the characters matrix
     * @return true, character block exists and has standard datatype.
     */
    public boolean isApplicable(TaxaBlock taxa, CharactersBlock c) {
        /*if (taxa == null || c == null || !(c.getFormat().getDatatype()).equalsIgnoreCase(Characters.Datatypes.STANDARD))
            return false;
        return c.getFormat().getSymbols().equalsIgnoreCase(Characters.Datatypes.STANDARDSYMBOLS);
        */
        return c.getDataType() == CharactersType.Standard;
    }

    final public String getDescription() {
        return DESCRIPTION;
    }

    public double getOptionRestrictionSiteLength() {
        return this.optionRestrictionSiteLength;
    }

    public void setOptionRestrictionSiteLength(double restrictionSiteLength) {
        this.optionRestrictionSiteLength = restrictionSiteLength;
    }

}
