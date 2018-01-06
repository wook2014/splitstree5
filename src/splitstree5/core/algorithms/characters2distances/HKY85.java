package splitstree5.core.algorithms.characters2distances;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.characters2distances.utils.SaturatedDistancesException;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.models.HKY85model;

/**
 * Computes the Hasegawa, Kishino and Yano distance for a set of characters.
 * <p>
 * Created on 12-Jun-2004
 *
 * @author Mig
 */

public class HKY85 extends DNAdistance implements IFromChararacters, IToDistances {

    private double tratio = 2.0;
    public final static String DESCRIPTION = "Calculates distances using the Hasegawa, Kishino and Yano model";

    @Override
    public String getCitation() {
        return "HKY85; Hasegawa, Kishino, Yano 1985; " +
                "Hasegawa M, Kishino H, Yano T. \"Dating of human-ape splitting by a molecular clock of mitochondrial DNA\". " +
                "Journal of Molecular Evolution. 22 (2): 160–174. PMID 3934395. doi:10.1007/BF02101694, 1985.";
    }

    public HKY85() {
        super();
        setOptionMaximum_Likelihood(true);
    }

    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock)
            throws Exception {

        progressListener.setTasks("HKY85 Distance", "Init.");

        HKY85model model = new HKY85model(getNormedBaseFreq(), this.tratio);
        model.setPinv(getOptionPInvar());
        model.setGamma(getOptionGamma());

        setOptionMaximum_Likelihood(true);
        distancesBlock.copy(fillDistanceMatrix(progressListener, charactersBlock, model));
    }

    @Override
    protected double exactDist(double[][] F) throws SaturatedDistancesException {
        return 0.0;//We will never get here!
    }

    /**
     * gets a short description of the algorithm
     *
     * @return a description
     */
    public String getDescription() {
        return DESCRIPTION;
    }
}
