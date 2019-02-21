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
import splitstree5.core.models.K2Pmodel;

import java.util.Arrays;
import java.util.List;

/**
 * Computes the Kimura two parameter distance for a set of characters
 * <p>
 * Created on 12-Jun-2004
 *
 * @author DJB
 */

public class K2P extends DNAdistance implements IFromChararacters, IToDistances {

    //default is no difference between transitions and transversions
    private final DoubleProperty optionTsTvRatio = new SimpleDoubleProperty(2.0);
    public final static String DESCRIPTION = "Calculates distances using the Kimura2P model";

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
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock) throws Exception {

        progress.setTasks("K2P Distance", "Init.");
        progress.setMaximum(taxaBlock.getNtax());

        K2Pmodel model = new K2Pmodel(this.optionTsTvRatio.getValue());
        model.setPropInvariableSites(getOptionPropInvariableSites());
        model.setGamma(getOptionGamma());

        distancesBlock.copy(fillDistanceMatrix(progress, charactersBlock, model));
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

    public String getDescription() {
        return DESCRIPTION;
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
