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
import splitstree5.gui.utils.CharactersUtilities;

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

    // ACGT transversions vs ATGC transversions
    private final double DEFAULT_AC_VS_AT = 2.0;
    private final DoubleProperty optionACvsAT = new SimpleDoubleProperty(DEFAULT_AC_VS_AT);

    public final static String DESCRIPTION = "Calculates distances using the Kimura3ST model";

    @Override
    public String getCitation() {
        return "Kimura 1981; M. Kimura, Estimation of evolutionary sequences between homologous nucleotide sequences, " +
                "Proc. Natl. Acad. Sci. USA 78 (1981) 454–45";
    }

    public List<String> listOptions() {
        return Arrays.asList("PropInvariableSites", "Gamma", "UseML", "SetParameters", "TsTvRatio", "ACvsAT");
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock) throws Exception {
        progress.setTasks("K3ST Distance", "Init.");
        progress.setMaximum(taxaBlock.getNtax());

        K3STmodel model = new K3STmodel(getOptionTsTvRatio(), this.optionACvsAT.getValue());
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

    @Override
    public void updateSettings(CharactersBlock characters, SetParameters value) {
        if (value.equals(SetParameters.fromChars)) {
            setOptionPropInvariableSites(characters.hasPropInvariableSites() ? characters.getPropInvariableSites() : DEFAULT_PROP_INVARIABLE_SITES);
            setOptionGamma(characters.hasGamma() ? characters.getGammaParam() : DEFAULT_GAMMA);
            setBaseFreq(CharactersUtilities.computeFreqs(characters, false));
        } else if (value.equals(SetParameters.defaultParameters)) {
            setOptionPropInvariableSites(DEFAULT_PROP_INVARIABLE_SITES);
            setOptionGamma(DEFAULT_GAMMA);
            setOptionTsTvRatio(DEFAULT_TSTV_RATIO);
            setOptionAC_vs_ATRatio(DEFAULT_AC_VS_AT);
        }
    }

    // GETTER AND SETTER

    public String getDescription() {
        return DESCRIPTION;
    }

    public void setOptionAC_vs_ATRatio(double value) {
        this.optionACvsAT.setValue(value);
    }
    public double getOptionAC_vs_ATRatio() {
        return this.optionACvsAT.getValue();
    }
    public DoubleProperty optionACvsATProperty() {
        return this.optionACvsAT;
    }
}
