package splitstree5.core.algorithms.characters2distances;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.characters2distances.utils.SaturatedDistancesException;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.models.K3STmodel;

import java.util.Arrays;
import java.util.List;

/**
 * Calculates distances using the Kimura-3ST model
 * <p>
 * Created on 12-Jun-2004
 *
 * @author DJB
 */

public class K3ST extends DNAdistance implements IFromChararacters, IToDistances {

    //private double[][] QMatrix; //Q Matrix provided by user for ML estimation. //todo not used?

    //default is no difference between transitions and transversions
    private final DoubleProperty optionTsTvRatio = new SimpleDoubleProperty(2.0);
    private double ACvsAT = 2.0;
    public final static String DESCRIPTION = "Calculates distances using the Kimura3ST model";

    @Override
    public String getCitation() {
        return "Swofford et al 1996; " +
                "D.L. Swofford, G.J. Olsen, P.J. Waddell, and  D.M. Hillis. Chapter  11:  Phylogenetic inference. " +
                "In D. M. Hillis, C. Moritz, and B. K. Mable, editors, Molecular Systematics, pages 407–514. " +
                "Sinauer Associates, Inc., 2nd edition, 1996.";
    }

    public List<String> listOptions() {
        return Arrays.asList("PInvar", "Gamma", "UseML", "SetParameters", "TsTvRatio");
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock)
            throws Exception {

        progress.setTasks("K3ST Distance", "Init.");
        progress.setMaximum(taxaBlock.getNtax());

        K3STmodel model = new K3STmodel(optionTsTvRatio.getValue(), this.ACvsAT);
        model.setPropInvariableSites(getOptionPropInvariableSites());
        model.setGamma(getOptionGamma());

        distancesBlock.copy(fillDistanceMatrix(progress, charactersBlock, model));
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

    public double getOptionTsTvRatio() {
        return optionTsTvRatio.getValue();
    }
    public DoubleProperty optionTsTvRatioProperty() {
        return optionTsTvRatio;
    }
    public void setOptionTsTvRatio(double optionTsTvRatio) {
        this.optionTsTvRatio.setValue(optionTsTvRatio);
    }
}
