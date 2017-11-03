package splitstree5.core.algorithms.characters2distances;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.characters2distances.utils.SaturatedDistancesException;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.models.K2Pmodel;

/**
 * Computes the Kimura two parameter distance for a set of characters
 *
 * Created on 12-Jun-2004
 * @author DJB
 */

public class K2P extends DNAdistance implements IFromChararacters, IToDistances {

    private double tratio = 2.0;
    public final static String DESCRIPTION = "Calculates distances using the Kimura2P model";

    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock) throws Exception {

        progressListener.setTasks("K2P Distance", "Init.");
        progressListener.setMaximum(taxaBlock.getNtax());

        K2Pmodel model = new K2Pmodel(this.tratio);
        model.setPinv(getOptionPInvar());
        model.setGamma(getOptionGamma());

        distancesBlock.copy(fillDistanceMatrix(progressListener, charactersBlock, model));
    }

    @Override
    protected double exactDist(double[][] F) throws SaturatedDistancesException {
        double P = F[0][2] + F[1][3] + F[2][0] + F[3][1];
        double Q = F[0][1] + F[0][3] + F[1][0] + F[1][2];
        Q += F[2][1] + F[2][3] + F[3][0] + F[3][2];
        double dist = 0.5 * Minv(1 / (1 - (2 * P) - Q));
        dist += 0.25 * Minv(1 / (1 - (2 * Q)));
        return dist;
    }

    public String getDescription(){
        return DESCRIPTION;
    }
}
