/*
 *  Copyright (C) 2018 Daniel H. Huson
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

package splitstree5.core.algorithms.alt_characters2distances;

import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import jloda.fx.CallableService;
import jloda.fx.NotificationManager;
import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.analysis.CaptureRecapture;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.gui.utils.CharactersUtilities;

public abstract class Nucleotides2DistancesAlgorithm extends Algorithm<CharactersBlock, DistancesBlock> {
    public enum SetParameters {fromChars, defaultParameters}

    final double DEFAULT_PROP_INVARIABLE_SITES = 0.0;
    final double DEFAULT_GAMMA = -1;        //Negative gamma corresponds to equal rates
    final double[] DEFAULT_BASE_FREQ = {0.25, 0.25, 0.25, 0.25};  //Use the exact distance by default - transforms without exact distances should set useML = false
    final double DEFAULT_TSTV_RATIO = 2.0;  //default is no difference between transitions and transversions

    private final DoubleProperty optionPropInvariableSites = new SimpleDoubleProperty(DEFAULT_PROP_INVARIABLE_SITES);
    private final DoubleProperty optionGamma = new SimpleDoubleProperty(DEFAULT_GAMMA);
    private final BooleanProperty optionUseML = new SimpleBooleanProperty(false);
    private final DoubleProperty optionTsTvRatio = new SimpleDoubleProperty(DEFAULT_TSTV_RATIO);

    // Used in the panel to decide how to compute the above
    private final Property<SetParameters> optionSetParameters = new SimpleObjectProperty<>(SetParameters.defaultParameters);

    private final ObjectProperty<double[]> optionBaseFrequencies = new SimpleObjectProperty<>(new double[]{0.25, 0.25, 0.25, 0.25});

    private ChangeListener<SetParameters> listener = null;

    @Override
    public String getToolTip(String optionName) {
        switch (optionName) {
            case "PropInvariableSites":
                return "Proportion of invariable sites";
            case "Gamma":
                return "Alpha value for the Gamma distribution";
            case "UseML":
                return "Use maximum likelihood estimation of distances (rather than exact distances)";
            case "TsTvRatio":
                return "Ratio of transitions vs transversions";
            case "BaseFrequencies":
                return "Base frequencies (in order ATGC)";
            case "SetParameters":
                return "Set parameters to default values or to estimations from data (using Capture-recapture for invariable sites)";
        }
        return optionName;
    }

    /**
     * this is run after the node has been instantiated
     *
     * @param taxaBlock
     * @param parent
     * @throws Exception
     */
    public void setupBeforeDisplay(TaxaBlock taxaBlock, CharactersBlock parent) {
        if (listener != null)
            optionSetParametersProperty().removeListener(listener);
        // setup set Parameters control:
        listener = (c, o, n) -> {
            switch (n) {
                case defaultParameters: {
                    setOptionPropInvariableSites(DEFAULT_PROP_INVARIABLE_SITES);
                    setOptionBaseFrequencies(DEFAULT_BASE_FREQ);
                    setOptionGamma(DEFAULT_GAMMA);
                    break;
                }
                case fromChars: {
                    final CallableService<Double> service1 = new CallableService<>(() -> {
                        // todo: want this to run in foot pane
                        try (ProgressPercentage progress = new ProgressPercentage(CaptureRecapture.DESCRIPTION)) {
                            final CaptureRecapture captureRecapture = new CaptureRecapture();
                            return captureRecapture.estimatePropInvariableSites(progress, parent);
                        }
                    });
                    service1.setOnSucceeded((e) -> setOptionPropInvariableSites(service1.getValue()));
                    service1.setOnFailed((e) -> {
                        NotificationManager.showError("Calculation of proportion of invariable sites failed: " + service1.getException().getMessage());
                    });
                    service1.start();

                    final CallableService<double[]> service2 = new CallableService<>(() -> {
                        return CharactersUtilities.computeFreqs(parent, false);
                    });
                    service2.setOnSucceeded((e) -> setOptionBaseFrequencies(service2.getValue()));
                    service2.setOnFailed((e) -> {
                        NotificationManager.showError("Calculation of base frequencies failed: " + service2.getException().getMessage());
                    });
                    service2.start();
                    break;
                }
            }
        };
        optionSetParametersProperty().addListener(listener);
    }

    @Override
    abstract public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock parent, DistancesBlock child) throws Exception;

    public double getOptionPropInvariableSites() {
        return optionPropInvariableSites.get();
    }

    public DoubleProperty optionPropInvariableSitesProperty() {
        return optionPropInvariableSites;
    }

    public void setOptionPropInvariableSites(double optionPropInvariableSites) {
        this.optionPropInvariableSites.set(optionPropInvariableSites);
    }

    public double getOptionGamma() {
        return optionGamma.get();
    }

    public DoubleProperty optionGammaProperty() {
        return optionGamma;
    }

    public void setOptionGamma(double optionGamma) {
        this.optionGamma.set(optionGamma);
    }

    public boolean isOptionUseML() {
        return optionUseML.get();
    }

    public BooleanProperty optionUseMLProperty() {
        return optionUseML;
    }

    public void setOptionUseML(boolean optionUseML) {
        this.optionUseML.set(optionUseML);
    }

    public double getOptionTsTvRatio() {
        return optionTsTvRatio.get();
    }

    public DoubleProperty optionTsTvRatioProperty() {
        return optionTsTvRatio;
    }

    public void setOptionTsTvRatio(double optionTsTvRatio) {
        this.optionTsTvRatio.set(optionTsTvRatio);
    }

    public SetParameters getOptionSetParameters() {
        return optionSetParameters.getValue();
    }

    public Property<SetParameters> optionSetParametersProperty() {
        return optionSetParameters;
    }

    public void setOptionSetParameters(SetParameters optionSetParameters) {
        this.optionSetParameters.setValue(optionSetParameters);
    }

    public double[] getOptionBaseFrequencies() {
        return optionBaseFrequencies.get();
    }

    public ObjectProperty<double[]> optionBaseFrequenciesProperty() {
        return optionBaseFrequencies;
    }

    public void setOptionBaseFrequencies(double[] optionBaseFrequencies) {
        this.optionBaseFrequencies.set(optionBaseFrequencies);
    }
}
