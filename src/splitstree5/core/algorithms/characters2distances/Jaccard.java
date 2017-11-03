package splitstree5.core.algorithms.characters2distances;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.characters2distances.utils.PairwiseCompare;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.characters.CharactersType;
import splitstree5.gui.dialog.Alert;

/**
 * Simple implementation of hamming distances
 *
 * Created on 2009-01-25
 * @author bryant
 */
public class Jaccard extends Algorithm<CharactersBlock, DistancesBlock> implements IFromChararacters, IToDistances {

    private PairwiseCompare.HandleAmbiguous optionHandleAmbiguousStates = PairwiseCompare.HandleAmbiguous.Ignore;

    public final static String DESCRIPTION = "Calculates distances using the Jaccard coefficient distance.";
    protected String TASK = "Jaccard Coefficient Distance";


    @Override
    public boolean isApplicable(TaxaBlock taxa, CharactersBlock c, DistancesBlock d) {
        return c.getDataType().equals(CharactersType.standard);
    }

    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock)
            throws Exception {

        char gapchar = charactersBlock.getGapCharacter();
        char missingchar = charactersBlock.getMissingCharacter();

        int ntax = taxaBlock.getNtax();
        distancesBlock.setNtax(ntax);

        progressListener.setTasks("Jaccard distance", "Init.");
        progressListener.setMaximum(ntax);

        double maxDist = 0.0;
        int numUndefined = 0;

        for (int s = 1; s <= ntax; s++) {
            for (int t = s + 1; t <= ntax; t++) {
                //System.err.println(s+","+t);
                PairwiseCompare seqPair = new PairwiseCompare(charactersBlock, "01", s, t, optionHandleAmbiguousStates);
                double[][] F = seqPair.getF();

                double dist = -1.0;

                if (F == null) {
                    numUndefined++;
                } else {
                    double b = F[1][0];
                    double c = F[0][1];
                    double a = F[1][1];

                    if (a + b + c <= 0.0) {
                        numUndefined++;
                        dist = -1;
                    } else {
                        dist = 1.0 - 2 * a / (2 * a + b + c);
                    }

                }

                distancesBlock.set(s, t, dist);
                distancesBlock.set(t, s, dist);
                if (dist > maxDist)
                    maxDist = dist;
            }
            progressListener.incrementProgress();
        }
        progressListener.close();
        if (numUndefined > 0) {
            for (int s = 1; s <= ntax; s++)
                for (int t = s + 1; t <= ntax; t++) {
                    if (distancesBlock.get(s, t) < 0) {
                        distancesBlock.set(s, t, 2.0 * maxDist);
                        distancesBlock.set(t, s, 2.0 * maxDist);
                    }
                }
            String message = "Distance matrix contains " + numUndefined + " undefined ";
            message += "distances. These have been arbitrarily set to 2 times the maximum";
            message += " defined distance (= " + (2.0 * maxDist) + ").";
            new Alert(message);
        }
    }

    final public String getDescription() {
        return DESCRIPTION;
    }

    public PairwiseCompare.HandleAmbiguous getOptionHandleAmbiguousStates() {
        return optionHandleAmbiguousStates;
    }

    public void setOptionHandleAmbiguousStates(PairwiseCompare.HandleAmbiguous optionHandleAmbiguousStates) {
        this.optionHandleAmbiguousStates = optionHandleAmbiguousStates;
    }

    public String getTASK(){
        return TASK;
    }
}
