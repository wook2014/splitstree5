package splitstree5.core.algorithms.characters2distances;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.characters2distances.utils.FixUndefinedDistances;
import splitstree5.core.algorithms.characters2distances.utils.PairwiseCompare;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.characters.CharactersType;

/**
 * Simple implementation of hamming distances
 * <p>
 * Created on 2009-01-25
 *
 * @author bryant
 */
public class Jaccard extends Algorithm<CharactersBlock, DistancesBlock> implements IFromChararacters, IToDistances {

    private PairwiseCompare.HandleAmbiguous optionHandleAmbiguousStates = PairwiseCompare.HandleAmbiguous.Ignore;

    public final static String DESCRIPTION = "Calculates distances using the Jaccard coefficient distance.";
    protected String TASK = "Jaccard Coefficient Distance";


    @Override
    public boolean isApplicable(TaxaBlock taxa, CharactersBlock c) {
        return c.getDataType().equals(CharactersType.standard);
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock)
            throws Exception {

        char gapchar = charactersBlock.getGapCharacter();
        char missingchar = charactersBlock.getMissingCharacter();

        int ntax = taxaBlock.getNtax();
        distancesBlock.setNtax(ntax);

        progress.setTasks("Jaccard distance", "Init.");
        progress.setMaximum(ntax);

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
            progress.incrementProgress();
        }
        if (numUndefined > 0)
            FixUndefinedDistances.apply(ntax, maxDist, distancesBlock);

        progress.close();
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

    public String getTASK() {
        return TASK;
    }
}
