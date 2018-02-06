package splitstree5.core.algorithms.characters2distances;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.characters2distances.utils.SaturatedDistancesException;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.characters.CharactersType;
import splitstree5.core.models.F81model;

/**
 * Implements the Felsenstein81 DNA distance model
 * <p>
 * Created on Jun 2004
 * <p>
 * todo : no authors
 */

public class F81 extends DNAdistance implements IFromChararacters, IToDistances {

    public final static String DESCRIPTION = "Calculates distances using the Felsenstein81 model";
    private double B;

    @Override
    public String getCitation() {
        return "Swofford et al 1996; " +
                "D.L. Swofford, G.J. Olsen, P.J. Waddell, and  D.M. Hillis. Chapter 11: Phylogenetic inference. " +
                "In D. M. Hillis, C. Moritz, and B. K. Mable, editors, Molecular Systematics, pages 407–514. " +
                "Sinauer Associates, Inc., 2nd edition, 1996.";
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock)
            throws Exception {

        progress.setTasks("F81 Distance", "computing...");
        F81model model = new F81model(this.getNormedBaseFreq());
        model.setPinv(getOptionPInvar());
        model.setGamma(getOptionGamma());

        //System.out.println("A is: " + baseFreq[0]);
        double[] freqs = getNormedBaseFreq();

        double piA = freqs[0],
                piC = freqs[1],
                piG = freqs[2],
                piT = freqs[3];

        B = 1.0 - ((piA * piA) + (piC * piC) + (piG * piG) + (piT * piT));

        distancesBlock.copy(fillDistanceMatrix(progress, charactersBlock, model));
    }

    /**
     * return the exact distance
     *
     * @param F
     * @return
     * @throws SaturatedDistancesException
     */
    protected double exactDist(double[][] F) throws SaturatedDistancesException {
        double D = 1 - (F[0][0] + F[1][1] + F[2][2] + F[3][3]);
        return -B * Minv(1 - D / B);
    }

    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public boolean isApplicable(TaxaBlock taxa, CharactersBlock ch) {
        return ch.getDataType() == CharactersType.DNA || ch.getDataType() == CharactersType.RNA;
    }
}
