package splitstree5.core.algorithms.characters2distances;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import jloda.fx.Alert;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.characters2distances.utils.PairwiseCompare;
import splitstree5.core.algorithms.characters2distances.utils.SaturatedDistancesException;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.characters.CharactersType;
import splitstree5.core.models.NucleotideModel;
import splitstree5.gui.utils.CharactersUtilities;
import splitstree5.utils.SplitsException;

import java.util.Arrays;
import java.util.List;

public abstract class DNAdistance extends SequenceBasedDistance {

    /* These are the parameters used for distance calculation */

    //private final DoubleProperty[] baseFreq;  //Base frequences (unnormalised)
    private double[] baseFreq;

    private final DoubleProperty optionPInvar = new SimpleDoubleProperty(0.0);
    //Negative gamma corresponds to equal rates
    private final DoubleProperty optionGamma = new SimpleDoubleProperty(-1);
    //Use the exact distance by default - transforms without exact distances should set useML = false
    private final BooleanProperty optionUseML = new SimpleBooleanProperty(false);

    /* These are used in the panel to decide how to compute the above*/

    /* These constant used to decide where to get parameter estimates from */
    public static final int FROMCHARS = -1;
    public static final int FROMUSER = -2;
    public static final int DEFAULT = 0;

    //The symbols in the character matrix can come in any order, however
    //the order of states in the Q matrix is fixed. Here is the fixed order.
    public static final String DNASTATES = "acgt";
    public static final String RNASTATES = "acgu";

    private int whichPInvar;
    private int whichGamma;
    private int whichBaseFreq;

    public DNAdistance() {
        whichPInvar = DEFAULT;
        whichGamma = DEFAULT;
        baseFreq = new double[]{0.25, 0.25, 0.25, 0.25};    //default is equal frequencies
        /*this.baseFreq = new DoubleProperty[4];
        for(DoubleProperty dp : baseFreq)
            dp = new SimpleDoubleProperty(0.25); //.setValue(0.25);*/
        whichBaseFreq = DEFAULT;
    }

    public List<String> listOptions() {
        return Arrays.asList("PInvar", "Gamma", "UseML");
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
     * @param characters
     */
    public void updateSettings(CharactersBlock characters) {
        if (whichPInvar == FROMCHARS) {
            setOptionPInvar(characters.getpInvar());
        }
        if (whichGamma == FROMCHARS) {
            setOptionGamma(characters.getGammaParam());
        }
        if (whichBaseFreq == FROMCHARS) {
            setBaseFreq(CharactersUtilities.computeFreqs(characters, false));
        }
    }


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
        double p = getOptionPInvar();
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

                if (this.optionUseML.getValue()) {
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
            new Alert("Warning: " + numMissing + " saturated or missing entries in the distance matrix - proceed with caution ");
        }
        return distances;
    }


    public double getOptionPInvar() {
        return optionPInvar.getValue();
    }
    public DoubleProperty optionPInvarProperty() {
        return optionPInvar;
    }
    public void setOptionPInvar(double pinvar) {
        optionPInvar.setValue(pinvar);
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

    public boolean getOptionMaximumLikelihood() {
        return optionUseML.getValue();
    }
    public BooleanProperty optionUseMLProperty() {
        return optionUseML;
    }
    public void setOptionMaximumLikelihood(boolean useML) {
        this.optionUseML.setValue(useML);
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

    public int getWhichPInvar() {
        return whichPInvar;
    }

    public void setWhichPInvar(int whichPInvar) {
        this.whichPInvar = whichPInvar;
    }

    public int getWhichGamma() {
        return whichGamma;
    }

    public void setWhichGamma(int whichGamma) {
        this.whichGamma = whichGamma;
    }

    public int getWhichBaseFreq() {
        return whichBaseFreq;
    }

    public void setWhichBaseFreq(int whichBaseFreq) {
        this.whichBaseFreq = whichBaseFreq;
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
