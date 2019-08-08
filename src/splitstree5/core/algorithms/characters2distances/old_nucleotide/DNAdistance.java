/*
 *  DNAdistance.java Copyright (C) 2019 Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.core.algorithms.characters2distances.old_nucleotide;

import javafx.beans.property.*;
import jloda.fx.window.NotificationManager;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.characters2distances.utils.PairwiseCompare;
import splitstree5.core.algorithms.characters2distances.utils.SaturatedDistancesException;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.characters.CharactersType;
import splitstree5.core.models.nucleotideModels.NucleotideModel;
import splitstree5.core.workflow.Connector;
import splitstree5.utils.SplitsException;

import java.util.Arrays;
import java.util.List;

/**
 * @deprecated
 */
public abstract class DNAdistance extends Algorithm<CharactersBlock, DistancesBlock> {

    public enum SetParameters {fromChars, defaultParameters}

    final double DEFAULT_PROP_INVARIABLE_SITES = 0.0;
    final double DEFAULT_GAMMA = -1;        //Negative gamma corresponds to equal rates
    final double DEFAULT_BASE_FREQ = 0.25;  //Use the exact distance by default - transforms without exact distances should set useML = false
    final double DEFAULT_TSTV_RATIO = 2.0;  //default is no difference between transitions and transversions

    /* These are the parameters used for distance calculation */

    //private final DoubleProperty[] baseFreq;  //Base frequencies (unnormalised)
    private double[] baseFreq;
    private final DoubleProperty optionPropInvariableSites = new SimpleDoubleProperty(DEFAULT_PROP_INVARIABLE_SITES);
    private final DoubleProperty optionGamma = new SimpleDoubleProperty(DEFAULT_GAMMA);
    private final BooleanProperty optionUseML_Distances = new SimpleBooleanProperty(false);
    private final DoubleProperty optionTsTvRatio = new SimpleDoubleProperty(DEFAULT_TSTV_RATIO);

    // Used in the panel to decide how to compute the above
    private final Property<SetParameters> optionSetParameters = new SimpleObjectProperty<>(SetParameters.defaultParameters);


    public DNAdistance() {
        baseFreq = new double[]{0.25, 0.25, 0.25, 0.25};    //default is equal frequencies
        /*this.baseFreq = new DoubleProperty[4];
        for(DoubleProperty dp : baseFreq)
            dp = new SimpleDoubleProperty(0.25); //.setValue(0.25);*/

        //todo: connector = null, need access to charatersBlock for updateSetting function!

        connectorProperty().addListener((c) -> {
            System.err.println("Connector Listener");
            final Connector<CharactersBlock, ? extends DataBlock> connector = connectorProperty().get();
            optionSetParameters.addListener((observable, oldValue, newValue) -> updateSettings(connector != null ? connector.getParentDataBlock() : null, newValue));
        });
    }

    public List<String> listOptions() {
        return Arrays.asList("Gamma", "PropInvariableSites", "UseML_Distances", "SetParameters", "TsTvRatio");
    }

    @Override
    public String getToolTip(String optionName) {
        switch (optionName) {
            case "PropInvariableSites":
                return "Proportion of invariable sites";
            case "Gamma":
                return "Alpha parameter for gamma distribution. Negative gamma = Equal rates";
            case "UseML_Distances":
                return "Use maximum likelihood distances estimation";
            case "SetParameters":
                return "Choose between default or in character block defined parameters ";
            case "TsTvRatio":
                return "Ratio between transitions and transversions";
            case "ACvsAT":
                return "Ratio between ACGT transversions and ATGC transversions";
        }
        return null;
    }

    /**
     * Update properties from the characters block.
     * <p/>
     * Affects only those parameters which are set to be read from characters.
     * The base frequencies are computed from scratch - but the others
     * are read from what is in Characters.properties (and assumed correct)
     *
     * @param
     */
    public void updateSettings(CharactersBlock characters, SetParameters value) {
        if (value.equals(SetParameters.fromChars)) {
            setBaseFreq(NucleotideModel.computeFreqs(characters, false));
        } else if (value.equals(SetParameters.defaultParameters)) {
            setOptionPropInvariableSites(DEFAULT_PROP_INVARIABLE_SITES);
            setOptionGamma(DEFAULT_GAMMA);
            setOptionTsTvRatio(DEFAULT_TSTV_RATIO);
        }
    }

    /**
     * only applicable to nucleotide data
     *
     * @param taxa
     * @param charactersBlock
     * @return
     */
    public boolean isApplicable(TaxaBlock taxa, CharactersBlock charactersBlock) {
        return charactersBlock.getDataType() == CharactersType.DNA || charactersBlock.getDataType() == CharactersType.RNA;
    }

    /**
     * REturn the inverse of the moment generating function corresponding to the current settings
     *
     * @param x
     * @return double
     */
    protected double Minv(double x) throws SaturatedDistancesException {
        if (x <= 0.0)
            throw new SaturatedDistancesException();
        double p = getOptionPropInvariableSites();
        if (p < 0.0 || p > 1.0)
            p = 0.0;
        if (x - p <= 0.0)
            throw new SaturatedDistancesException();
        double alpha = getOptionGamma();
        if (alpha > 0.0) {
            return alpha * (1.0 - Math.pow((x - p) / (1.0 - p), -1.0 / alpha));
        } else
            return Math.log((x - p) / (1 - p));
    }

    /**
     * exact Distance - use an exact distance formula (if available) SHould never be called
     * if the transform does not have an exact dist formula.
     *
     * @param F
     * @return
     * @throws SaturatedDistancesException
     */
    abstract protected double exactDist(double[][] F) throws SaturatedDistancesException;

    /**
     * Fill in the distance matrix
     *
     * @param progressListener used to display the progress
     * @param characters
     * @param model
     * @return
     * @throws SplitsException
     * @throws CanceledException
     */
    protected DistancesBlock fillDistanceMatrix(ProgressListener progressListener, CharactersBlock characters, NucleotideModel model) throws SplitsException, CanceledException {
        final int ntax = characters.getNtax();
        final DistancesBlock distances = new DistancesBlock();
        distances.setNtax(ntax);
        //distances.setTriangle("both"); // todo always so?

        int numMissing = 0;

        for (int s = 1; s <= ntax; s++) {
            for (int t = s + 1; t <= ntax; t++) {
                final PairwiseCompare seqPair = new PairwiseCompare(characters, s, t);
                double dist = 100.0;

                if (this.optionUseML_Distances.getValue()) {
                    //Maximum likelihood distance
                    try {
                        dist = seqPair.mlDistance(model);
                    } catch (SaturatedDistancesException e) {
                        numMissing++;
                    }
                } else {
                    //Exact distance
                    double[][] F = seqPair.getF();
                    if (F == null)
                        numMissing++;
                    else {
                        try {
                            dist = exactDist(F);
                        } catch (SaturatedDistancesException e) {
                            numMissing++;
                        }
                    }

                }

                distances.set(s, t, dist);
                distances.set(t, s, dist);

                double var = seqPair.bulmerVariance(dist, 0.75);
                distances.setVariance(s, t, var);
                distances.setVariance(t, s, var);
            }
            progressListener.incrementProgress();
        }
        progressListener.close();

        if (numMissing > 0) {
            NotificationManager.showWarning("Proceed with caution: " + numMissing + " saturated or missing entries in the distance matrix");
        }
        return distances;
    }


    // GETTERS AND SETTERS

    public double getOptionPropInvariableSites() {
        return optionPropInvariableSites.getValue();
    }

    public DoubleProperty optionPropInvariableSitesProperty() {
        return optionPropInvariableSites;
    }

    public void setOptionPropInvariableSites(double pinvar) {
        optionPropInvariableSites.setValue(pinvar);
    }

    public double getOptionGamma() {
        return optionGamma.getValue();
    }

    public DoubleProperty optionGammaProperty() {
        return optionGamma;
    }

    public void setOptionGamma(double gamma) {
        optionGamma.setValue(gamma);
    }

    public boolean getOptionUseML_Distances() {
        return optionUseML_Distances.getValue();
    }

    public BooleanProperty optionUseML_DistancesProperty() {
        return optionUseML_Distances;
    }

    public void setOptionUseML_Distances(boolean useML) {
        this.optionUseML_Distances.setValue(useML);
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

    public double[] getBaseFreq() {
        return baseFreq;
    }

    public void setBaseFreq(double [] baseFreq){
        this.baseFreq = baseFreq;
    }
    /*public DoubleProperty[] optionBaseFreqProperty() {
        return this.baseFreq;
    }
    public void setOptionBaseFreq(double[] baseFreq) {
        for (int i = 0; i < baseFreq.length; i++)
            this.baseFreq[i].setValue(baseFreq[i]);
    }*/

    public Property<SetParameters> optionSetParametersProperty() {
        return this.optionSetParameters;
    }

    public void setOptionSetParameters(SetParameters setParameters) {
        this.optionSetParameters.setValue(setParameters);
    }

    public double[] getNormedBaseFreq() {
        double[] freqs = new double[4];
        double sum = 0.0;
        for (int i = 0; i < 4; i++) {
            sum += getBaseFreq()[i];
        }
        for (int i = 0; i < 4; i++) {
            freqs[i] = getBaseFreq()[i] / sum;
        }
        return freqs;
    }

    public boolean freqsOK(double[] freqs) {
        if (freqs == null || freqs.length != 4)
            return false;
        for (int i = 0; i < 4; i++) {
            if (freqs[i] < 0.0)
                return false;
        }
        return true;
    }
}
