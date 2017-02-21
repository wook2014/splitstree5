package splitstree5.core.algorithms.characters2distances;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.characters2distances.utils.SaturatedDistancesException;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.models.F84Model;

public class F84 extends DNAdistance {

    private double tratio = 2.0;
    private double A, B, C;

    public final static String DESCRIPTION = "Calculates distances using the Felsenstein84 model";


    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock)
            throws Exception {

        progressListener.setTasks("F84 Distance", "Init.");
        progressListener.setMaximum(taxaBlock.getNtax());

        F84Model model = new F84Model(this.getNormedBaseFreq(), this.tratio);
        model.setPinv(getOptionPInvar());
        model.setGamma(getOptionGamma());

        double[] baseFreq = getNormedBaseFreq();
        double piA = baseFreq[0],
                piC = baseFreq[1],
                piG = baseFreq[2],
                piT = baseFreq[3];
        double piR = piA + piG; //frequency of purines
        double piY = piC + piT; //frequency of pyrimidines
        A = piC * piT / piY + piA * piG / piR;
        B = piC * piT + piA * piG;
        C = piR * piY;

        distancesBlock.copy(fillDistanceMatrix(progressListener, charactersBlock, model));
    }

    @Override
    protected double exactDist(double[][] F) throws SaturatedDistancesException {
        double P = F[0][2] + F[1][3] + F[2][0] + F[3][1];
        double Q = F[0][1] + F[0][3] + F[1][0] + F[1][2];
        Q += F[2][1] + F[2][3] + F[3][0] + F[3][2];
        double dist = -2.0 * A * Minv(1.0 - P / (2.0 * A) - (A - B) * Q / (2.0 * A * C));
        dist += 2.0 * (A - B - C) * Minv(1.0 - Q / (2.0 * C));
        return dist;
    }

    // GETTER AND SETTER

    public String getDescription() {
        return DESCRIPTION;
    }

    public void setOptionTRatio(double value) {
        this.tratio = value;
    }

    public double getOptionTRatio() {
        return this.tratio;
    }
}
