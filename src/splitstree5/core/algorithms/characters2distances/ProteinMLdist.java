package splitstree5.core.algorithms.characters2distances;

import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.characters.CharactersType;
import splitstree5.gui.dialog.Alert;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.characters2distances.utils.PairwiseCompare;
import splitstree5.core.algorithms.characters2distances.utils.SaturatedDistancesException;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.models.*;
import splitstree5.utils.nexus.SplitsException;

import java.util.LinkedList;
import java.util.List;

public class ProteinMLdist extends SequenceBasedDistance implements IFromChararacters, IToDistances {

    private PairwiseCompare.HandleAmbiguous optionHandleAmbiguousStates = PairwiseCompare.HandleAmbiguous.Ignore;

    private String optionModel = "JTT";
    private double optionPInvar = 0.0;
    private double optionGamma = 0.0;
    private boolean usePinvar = false;
    private boolean useGamma = false;
    private boolean estimateVariance = true;
    private static final String STATES = "arndcqeghilkmfpstwyv";

    public final static String DESCRIPTION = "Calculates maximum likelihood protein distance estimates";

    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock)
            throws Exception {

        boolean hasSaturated = false;

        int ntax = charactersBlock.getNtax();
        int npairs = ntax * (ntax - 1) / 2;

        distancesBlock.setNtax(ntax);
        progressListener.setTasks("Protein ML distance", "Init.");
        progressListener.setMaximum(npairs);

        ProteinModel model = selectModel(getOptionModel());
        model.setPinv(this.getOptionPInvar());
        model.setGamma(this.getOptionGamma());

        if (model == null) {
            throw new SplitsException("Incorrect model name");
        }
        int k = 0;
        for (int s = 1; s <= ntax; s++) {
            for (int t = s + 1; t <= ntax; t++) {


                PairwiseCompare seqPair =
                        new PairwiseCompare(charactersBlock, STATES, s, t, optionHandleAmbiguousStates);
                double dist = 100.0;

                //Maximum likelihood distance. Note we want to ignore sites
                //with the stop codon.
                try {
                    dist = seqPair.mlDistance(model);
                } catch (SaturatedDistancesException e) {
                    hasSaturated = true;
                }

                distancesBlock.set(s, t, dist);
                distancesBlock.set(t, s, dist);

                double var = seqPair.bulmerVariance(dist, 0.93);
                distancesBlock.setVariance(s-1, t-1, var);
                distancesBlock.setVariance(t-1, s-1, var);

                k++;
                progressListener.incrementProgress();
            }

        }

        progressListener.close();
        if (hasSaturated) {
            new Alert("Warning: saturated or missing entries in the distance matrix - proceed with caution ");
        }
    }

    /**
     * Determine whether F84 corrected Hamming distances can be computed with given data.
     *
     * @param taxa the taxa
     * @param ch   the characters matrix
     * @return true, if method applies to given data
     */
    public boolean isApplicable(TaxaBlock taxa, CharactersBlock ch) {
        return taxa != null && ch != null
                && ch.getDataType().equals(CharactersType.protein);
                //&& (ch.getFormat().getDatatype().equalsIgnoreCase(Characters.Datatypes.PROTEIN));
    }



    /**
     * returns list of all known protein models
     * <p/>
     * In future, this list will be constructed using reflection
     *
     * @return methods
     */

    // todo not used?
    public enum Model {cpREV45, Dayhoff, JTT, mtMAM, mtREV24, pmb, Rhodopsin, WAG}

    public List selectionOptionModel() {
        //TODO: Get list directly from class list.
        List models = new LinkedList();
        models.add("cpREV45");
        models.add("Dayhoff");
        models.add("JTT");
        models.add("mtMAM");
        models.add("mtREV24");
        models.add("pmb");
        models.add("Rhodopsin");
        models.add("WAG");

        return models;
    }

/*
 * Given the names of the protein model (in the list), this
 * initialises and returns the appropriate ProteinModel object.
 * In future, this can be done using reflection.
 *
 * Returns null if no model identified.
 */

    public ProteinModel selectModel(String modelName) {
        ProteinModel themodel;

        System.err.println("Model name = " + modelName);
        //TODO: Add all models
        switch (modelName) {
            case "cpREV45":
                themodel = new cpREV45Model();
                break;
            case "Dayhoff":
                themodel = new DayhoffModel();
                break;
            case "JTT":
                themodel = new JTTmodel();
                break;
            case "mtMAM":
                themodel = new mtMAMModel();
                break;
            case "mtREV24":
                themodel = new mtREV24Model();
                break;
            case "pmb":
                themodel = new pmbModel();
                break;
            case "Rhodopsin":
                themodel = new RhodopsinModel();
                break;
            case "WAG":
                themodel = new WagModel();
                break;
            default:
                themodel = null;
                break;
        }


        return themodel;
    }

    // GETTER AND SETTER

    public PairwiseCompare.HandleAmbiguous getOptionHandleAmbiguousStates() {
        return optionHandleAmbiguousStates;
    }

    public void setOptionHandleAmbiguousStates(PairwiseCompare.HandleAmbiguous optionHandleAmbiguousStates) {
        this.optionHandleAmbiguousStates = optionHandleAmbiguousStates;
    }

    public boolean checkOptions(CharactersBlock characters) {
        return true;
    }

    public String getOptionModel() {
        return optionModel;
    }

    public void setOptionModel(String optionModel) {
        this.optionModel = optionModel;
    }

    public double getOptionPInvar() {
        return optionPInvar;
    }

    public void setOptionPInvar(double pinvar) {
        optionPInvar = pinvar;
    }

    public void setOptionUsePInvar(boolean val) {
        usePinvar = val;
    }

    public boolean getOptionUsePInvar() {
        return usePinvar;
    }

    public double getOptionGamma() {
        return optionGamma;
    }

    public void setOptionGamma(double gamma) {
        optionGamma = gamma;
    }

    public boolean getOptionUseGamma() {
        return useGamma;
    }

    public void setOptionUseGamma(boolean var) {
        useGamma = var;
    }

    public void setOptionEstimate_variances(boolean val) {
        this.estimateVariance = val;
    }

    public boolean getOptionEstimate_variances() {
        return this.estimateVariance;
    }
}
