package splitstree5.core.algorithms.characters2distances;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.characters2distances.utils.SaturatedDistancesException;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.models.JCmodel;

/**
 * Computes the Jukes Cantor distance for a set of characters
 *
 * Created on 12-Jun-2004
 * @author DJB
 */

public class JukesCantor extends DNAdistance implements IFromChararacters, IToDistances {

    public final static String DESCRIPTION = "Calculates distances using the Jukes Cantor model";

    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock)
            throws Exception {

        progressListener.setTasks("Jukes-Cantor Distance", "Init.");
        progressListener.setMaximum(taxaBlock.getNtax());

        JCmodel model = new JCmodel();
        model.setPinv(getOptionPInvar());
        model.setGamma(getOptionGamma());

        distancesBlock.copy(fillDistanceMatrix(progressListener, charactersBlock, model));
    }

    @Override
    protected double exactDist(double[][] F) throws SaturatedDistancesException {
        double D = 1 - (F[0][0] + F[1][1] + F[2][2] + F[3][3]);
        double B = 0.75;
        return -B * Minv(1 - D / B);
    }

    // GETTER AND SETTER
    public String getDescription() {
        return DESCRIPTION;
    }
}
