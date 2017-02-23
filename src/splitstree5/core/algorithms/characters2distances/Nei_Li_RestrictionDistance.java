package splitstree5.core.algorithms.characters2distances;

import jloda.util.Alert;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.characters2distances.utils.PairwiseCompare;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;

public class Nei_Li_RestrictionDistance extends Algorithm<CharactersBlock, DistancesBlock> implements IFromChararacters, IToDistances {

    private PairwiseCompare.HandleAmbiguous optionHandleAmbiguousStates = PairwiseCompare.HandleAmbiguous.Ignore;
    public final boolean EXPERT = false;
    private final String DESCRIPTION = "Calculates the Nei and Li (1979) distance for restriction site data";

    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock)
            throws Exception {

        int ntax = taxaBlock.getNtax();
        distancesBlock.setNtax(ntax);

        progressListener.setTasks("Nei Li (1979) Restriction Site Distance", "Init.");
        progressListener.setMaximum(ntax);

        double maxDist = 0.0;
        int numUndefined = 0;

        for (int s = 1; s <= ntax; s++) {
            for (int t = s + 1; t <= ntax; t++) {

                PairwiseCompare seqPair = new PairwiseCompare(charactersBlock, "01", s, t,optionHandleAmbiguousStates);
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
                        double a = (4.0 * Math.pow(s_hat, 1.0 / (2 * 6)) - 1.0) / 3.0;
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
            progressListener.incrementProgress();
        }
        progressListener.close();
        if (numUndefined > 0) {
            for (int s = 1; s <= ntax; s++)
                for (int t = s + 1; t <= ntax; t++) {
                    if (distancesBlock.get(s, t) < 0) {
                        distancesBlock.set(s, t, 2.0 * maxDist);
                        distancesBlock.set(t, s, 2.0 * maxDist);
                    }
                }
            String message = "Distance matrix contains " + numUndefined + " undefined ";
            message += "distances. These have been arbitrarily set to 2 times the maximum";
            message += " defined distance (= " + (2.0 * maxDist) + ").";
            new Alert(message);
        }
    }


    /**
     * Determine whether Nei and Lirestriction distances can be computed with given data.
     *
     * @param taxa the taxa
     * @param c    the characters matrix
     * @return true, character block exists and has standard datatype.
     */
    public boolean isApplicable(TaxaBlock taxa, CharactersBlock c) {
        /*if (taxa == null || c == null || !(c.getFormat().getDatatype()).equalsIgnoreCase(Characters.Datatypes.STANDARD))
            return false;
        return c.getFormat().getSymbols().equalsIgnoreCase(Characters.Datatypes.STANDARDSYMBOLS);*/
        return true;
    }

    // GETTER AND SETTER

    public String getDESCRIPTION(){
        return DESCRIPTION;
    }

    public PairwiseCompare.HandleAmbiguous getOptionHandleAmbiguousStates() {
        return optionHandleAmbiguousStates;
    }

    public void setOptionHandleAmbiguousStates(PairwiseCompare.HandleAmbiguous optionHandleAmbiguousStates) {
        this.optionHandleAmbiguousStates = optionHandleAmbiguousStates;
    }
}