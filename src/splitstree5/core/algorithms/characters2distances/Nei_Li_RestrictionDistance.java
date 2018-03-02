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
 * Implements the NeiLi (1979) distance for restriction site data.
 *
 * @author bryant
 */

public class Nei_Li_RestrictionDistance extends Algorithm<CharactersBlock, DistancesBlock> implements IFromChararacters, IToDistances {

    private PairwiseCompare.HandleAmbiguous optionHandleAmbiguousStates = PairwiseCompare.HandleAmbiguous.Ignore;
    public final boolean EXPERT = false;
    private final String DESCRIPTION = "Calculates the Nei and Li (1979) distance for restriction site data";
    private double optionRestrictionSiteLength = 6.0;

    @Override
    public String getCitation() {
        return "Nei and Li 1979;M Nei and W H Li. Mathematical model for studying genetic variation in terms of restriction endonucleases, PNAS 79(1):5269-5273, 1979.";
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock)
            throws Exception {

        int ntax = taxaBlock.getNtax();
        distancesBlock.setNtax(ntax);

        progress.setTasks("Nei Li (1979) Restriction Site Distance", "Init.");
        progress.setMaximum(ntax);

        double maxDist = 0.0;
        int numUndefined = 0;

        for (int s = 1; s <= ntax; s++) {
            for (int t = s + 1; t <= ntax; t++) {

                PairwiseCompare seqPair = new PairwiseCompare(charactersBlock, "01", s, t, optionHandleAmbiguousStates);
                double[][] F = seqPair.getF();
                double dist = -1.0;
                if (F == null)
                    numUndefined++;
                else {

                    double ns = F[1][0] + F[1][1];
                    double nt = F[0][1] + F[1][1];
                    double nst = F[1][1];

                    if (nst == 0) {
                        dist = -1;
                        numUndefined++;
                    } else {
                        double s_hat = 2.0 * nst / (ns + nt);
                        double a = (4.0 * Math.pow(s_hat, 1.0 / (2 * optionRestrictionSiteLength)) - 1.0) / 3.0;
                        if (a <= 0.0) {
                            dist = -1;
                            numUndefined++;
                        } else
                            dist = -1.5 * Math.log(a);
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


    @Override
    public boolean isApplicable(TaxaBlock taxa, CharactersBlock c) {
        return c.getDataType().equals(CharactersType.standard);
    }

    // GETTER AND SETTER

    public String getDESCRIPTION() {
        return DESCRIPTION;
    }

    public PairwiseCompare.HandleAmbiguous getOptionHandleAmbiguousStates() {
        return optionHandleAmbiguousStates;
    }

    public void setOptionHandleAmbiguousStates(PairwiseCompare.HandleAmbiguous optionHandleAmbiguousStates) {
        this.optionHandleAmbiguousStates = optionHandleAmbiguousStates;
    }

    public double getOptionRestrictionSiteLength() {
        return this.optionRestrictionSiteLength;
    }

    public void setOptionRestrictionSiteLength(double restrictionSiteLength) {
        this.optionRestrictionSiteLength = restrictionSiteLength;
    }
}
