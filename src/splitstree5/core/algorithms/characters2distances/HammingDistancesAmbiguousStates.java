package splitstree5.core.algorithms.characters2distances;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import jloda.fx.NotificationManager;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.characters2distances.utils.PairwiseCompare;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.characters.AmbiguityCodes;

import java.util.Arrays;
import java.util.List;

public class HammingDistancesAmbiguousStates extends Algorithm<CharactersBlock, DistancesBlock> implements IFromChararacters, IToDistances {

    public enum AmbiguousOptions {Ignore, AverageStates, MatchStates};

    private BooleanProperty optionNormalize = new SimpleBooleanProperty(true);
    private Property<AmbiguousOptions> optionHandleAmbiguousStates = new SimpleObjectProperty<>(AmbiguousOptions.Ignore);

    public List<String> listOptions() {
        return Arrays.asList("Normalize", "HandleAmbiguousStates");
    }

    @Override
    public String getToolTip(String optionName) {
        switch (optionName) {
            case "Normalize":
                return "Normalize distances";
            case "HandleAmbiguousStates":
                return "Choose way to handle ambiguous nucleotides";
        }
        return null;
    }


    @Override
    public String getCitation() {
        return "Hamming 1950; Hamming, Richard W. Error detecting and error correcting codes. Bell System Technical Journal. 29 (2): 147–160. MR 0035935, 1950.";
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxa, CharactersBlock characters, DistancesBlock distances) throws Exception {
        progress.setMaximum(taxa.getNtax());

        distances.setNtax(characters.getNtax());

        if (optionHandleAmbiguousStates.getValue().equals(AmbiguousOptions.MatchStates)
                && characters.getDataType().isNucleotides() && characters.isHasAmbiguityCodes())
            computeMatchStatesHamming(progress, taxa, characters, distances);
        else {
            // all the same here
            int numMissing = 0;
            final int ntax = taxa.getNtax();
            for (int s = 1; s <= ntax; s++) {
                for (int t = s + 1; t <= ntax; t++) {

                    final PairwiseCompare seqPair;
                    if (optionHandleAmbiguousStates.getValue().equals(AmbiguousOptions.Ignore))
                        seqPair = new PairwiseCompare(characters, s, t, true);
                    else
                        seqPair = new PairwiseCompare(characters, s, t, false);

                    double p = 1.0;

                    final double[][] F = seqPair.getF();

                    if (F == null) {
                        numMissing++;
                    } else {
                        for (int x = 0; x < seqPair.getNumStates(); x++) {
                            p = p - F[x][x];
                        }

                        if (!isOptionNormalize())
                            p = Math.round(p * seqPair.getNumNotMissing());
                    }
                    distances.set(s, t, p);
                    distances.set(t, s, p);
                }
                progress.incrementProgress();
            }
            if (numMissing > 0)
                NotificationManager.showWarning("Proceed with caution: " + numMissing + " saturated or missing entries in the distance matrix");
        }
    }


    /**
     * Computes 'Best match' Hamming distances with a given characters block.
     *
     * @param taxa       the taxa
     * @param characters the input characters
     */
    private void computeMatchStatesHamming(ProgressListener progressListener, TaxaBlock taxa, CharactersBlock characters, DistancesBlock distances) throws Exception {

        final String ALLSTATES = "acgt" + AmbiguityCodes.CODES;
        int ntax = taxa.getNtax();
        int nstates = ALLSTATES.length();

        /* Fill in the costs ascribed to comparing different allele combinations */
        double[][] weights = new double[nstates][nstates];
        for (int s1 = 0; s1 < nstates; s1++)
            for (int s2 = 0; s2 < nstates; s2++)
                weights[s1][s2] = stringDiff(AmbiguityCodes.getNucleotides(ALLSTATES.charAt(s1)),
                        AmbiguityCodes.getNucleotides(ALLSTATES.charAt(s2)));

        /*for (char s1 : ALLSTATES.toCharArray())
            for (char s2 : ALLSTATES.toCharArray())
                weights[s1][s2] = stringDiff(AmbiguityCodes.getNucleotides(s1), AmbiguityCodes.getNucleotides(s2));*/

        /*Fill in the distance matrix */
        for (int s = 1; s <= ntax; s++) {
            for (int t = s + 1; t <= ntax; t++) {

                double[][] F = getFmatrix(ALLSTATES, characters, s, t);
                double diff = 0.0;
                for (int s1 = 0; s1 < F.length; s1++)
                    for (int s2 = 0; s2 < F.length; s2++)
                        diff += F[s1][s2] * weights[s1][s2];

                distances.set(s, t, (float) diff);
                distances.set(t, s, (float) diff);
            }
        }
    }

    private double stringDiff(String s1, String s2) {
        int matchCount = 0;
        for (int i = 0; i < s1.length(); i++) {
            char ch = s1.charAt(i);
            if (s2.indexOf(ch) >= 0) {
                matchCount++;
            }
        }
        for (int i = 0; i < s2.length(); i++) {
            char ch = s2.charAt(i);
            if (s1.indexOf(ch) >= 0) {
                matchCount++;
            }
        }

        return 1.0 - (double) matchCount / ((double) s1.length() + s2.length());
        //SAME IN INVERSE.
    }

    private double[][] getFmatrix(String ALLSTATES, CharactersBlock characters, int i, int j) {
        int nstates = ALLSTATES.length();
        double[][] F = new double[nstates][nstates];
        double fsum = 0.0;
        for (int k = 1; k <= characters.getNchar(); k++) {
            char ch1 = characters.get(i, k);
            char ch2 = characters.get(j, k);
            int state1 = ALLSTATES.indexOf(ch1);
            int state2 = ALLSTATES.indexOf(ch2);
            if (state1 >= 0 && state2 >= 0) {
                F[state1][state2] += 1.0;
                fsum += 1.0;
            }
        }
        if (fsum > 0.0) {
            for (int x = 0; x < nstates; x++)
                for (int y = 0; y < nstates; y++)
                    F[x][y] = F[x][y] / fsum;

        }

        return F;
    }

    // GETTERS AND SETTERS

    public boolean isOptionNormalize() {
        return optionNormalize.getValue();
    }
    public BooleanProperty optionNormalizeProperty() {
        return optionNormalize;
    }
    public void setOptionNormalize(boolean optionNormalize) {
        this.optionNormalize.setValue(optionNormalize);
    }

    public AmbiguousOptions getOptionHandleAmbiguousStates(){
        return this.optionHandleAmbiguousStates.getValue();
    }
    public Property<AmbiguousOptions> optionHandleAmbiguousStatesProperty(){
        return this.optionHandleAmbiguousStates;
    }
    public void setOptionHandleAmbiguousStates(AmbiguousOptions optionHandleAmbiguousStates) {
        this.optionHandleAmbiguousStates.setValue(optionHandleAmbiguousStates);
    }
}
