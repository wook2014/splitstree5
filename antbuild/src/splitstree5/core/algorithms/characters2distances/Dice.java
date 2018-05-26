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
 * Created on Nov 2007
 *
 * @author bryant
 */

public class Dice extends Algorithm<CharactersBlock, DistancesBlock> implements IFromChararacters, IToDistances {
    public final static String DESCRIPTION = "Calculates distances using the Dice coefficient distance.";

    @Override
    public String getCitation() { // is this the correct citation?
        return "Hamming 1950; " +
                "Hamming, Richard W. \"Error detecting and error correcting codes\". " +
                "Bell System Technical Journal. 29 (2): 147–160. MR 0035935, 1950.";
    }

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, CharactersBlock parent) {
        return parent.getDataType().equals(CharactersType.Standard);
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock) throws Exception {

        int ntax = taxaBlock.getNtax();
        distancesBlock.setNtax(ntax);

        progress.setTasks("Dice distance", "Init.");
        progress.setMaximum(ntax);

        double maxDist = 0.0;
        int numUndefined = 0;

        for (int s = 1; s <= ntax; s++) {
            for (int t = s + 1; t <= ntax; t++) {
                PairwiseCompare seqPair = new PairwiseCompare(charactersBlock, s, t);
                double dist;

                double[][] F = seqPair.getF();
                if (F == null) {
                    numUndefined++;
                    dist = -1;
                } else {

                    double b = F[1][0];
                    double c = F[0][1];
                    double a = F[1][1];

                    if (2 * a + b + c <= 0.0) {
                        numUndefined++;
                        dist = -1;
                    } else {
                        dist = 1.0 - 2.0 * a / (2.0 * a + b + c);
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
}

