package splitstree5.core.algorithms.characters2distances;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.characters2distances.utils.SaturatedDistancesException;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.models.K3STmodel;

/**
 * Created by Daria on 21.02.2017.
 */
public class K3ST extends DNAdistance {

    private double[][] QMatrix; //Q Matrix provided by user for ML estimation.
    private double tratio = 2.0;
    private double ACvsAT = 2.0;
    public final static String DESCRIPTION = "Calculates distances using the Kimura3ST model";


    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock)
            throws Exception {

        progressListener.setTasks("K3ST Distance", "Init.");
        progressListener.setMaximum(taxaBlock.getNtax());

        K3STmodel model = new K3STmodel(this.tratio, this.ACvsAT);
        model.setPinv(getOptionPInvar());
        model.setGamma(getOptionGamma());

        distancesBlock.copy(fillDistanceMatrix(progressListener, charactersBlock, model));
    }

    @Override
    protected double exactDist(double[][] F) throws SaturatedDistancesException {
        double a, b, c, d;
        a = F[0][0] + F[1][1] + F[2][2] + F[3][3];
        b = F[0][1] + F[1][0] + F[2][3] + F[3][2];
        c = F[0][2] + F[2][0] + F[1][3] + F[3][1];
        d = 1.0 - a - b - c;
        return -1 / 4.0 * (Math.log(a + c - b - d) + Math.log(a + b - c - d) + Math.log(a + d - b - c));
    }

    // GETTER AND SETTER

    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * set ACvsAT (ACGT transversions vs ATGC transversions)
     *
     * @param value
     */
    public void setOptionAC_vs_ATRatio(double value) {
        this.ACvsAT = value;
    }

    /**
     * get ACvsAT parameter
     *
     * @return ACvsAT (ACGT transversions vs ATGC transversions)
     */
    public double getOptionAC_vs_ATRatio() {
        return this.ACvsAT;
    }
}