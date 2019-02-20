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
import splitstree5.core.models.HKY85model;

import java.util.Arrays;
import java.util.List;

/**
 * Computes the Hasegawa, Kishino and Yano distance for a set of characters.
 * <p>
 * Created on 12-Jun-2004
 *
 * @author Mig
 */

public class HKY85 extends DNAdistance implements IFromChararacters, IToDistances {

    //default is no difference between transitions and transversions
    private final DoubleProperty optionTsTvRatio = new SimpleDoubleProperty(2.0);
    public final static String DESCRIPTION = "Calculates distances using the Hasegawa, Kishino and Yano model";

    @Override
    public String getCitation() {
        return "Hasegawa, Kishino, Yano 1985; " +
                "Hasegawa M, Kishino H, Yano T. \"Dating of human-ape splitting by a molecular clock of mitochondrial DNA\". " +
                "Journal of Molecular Evolution. 22 (2): 160–174. PMID 3934395. doi:10.1007/BF02101694, 1985.";
    }

    public HKY85() {
        super();
        setOptionMaximumLikelihood(true);
    }

    public List<String> listOptions() {
        return Arrays.asList("PInvar", "Gamma", "UseML", "TsTvRatio");
    }

    @Override
    public String getToolTip(String optionName) {
        switch (optionName) {
            case "PInvar":
                return "Proportion of invariable sites";
            case "Gamma":
                return "Alpha parameter for gamma distribution. Negative gamma = Equal rates";
            case "UseML":
                return "Use maximum likelihood distances estimation";
            case "TsTvRatio":
                return "Ratio between transitions and transversions";
        }
        return null;
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock)
            throws Exception {

        progress.setTasks("HKY85 Distance", "Init.");

        HKY85model model = new HKY85model(getNormedBaseFreq(), this.optionTsTvRatio.getValue());
        model.setPinv(getOptionPInvar());
        model.setGamma(getOptionGamma());

        setOptionMaximumLikelihood(true);
        distancesBlock.copy(fillDistanceMatrix(progress, charactersBlock, model));
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
