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
 * Calculates distances using the Jaccard coefficient distance
 *
 * @author Dave Bryant, 2009
 */
public class Jaccard extends Algorithm<CharactersBlock, DistancesBlock> implements IFromChararacters, IToDistances {
    @Override
    public String getCitation() {
        return "Jaccard 1901; Jaccard, Paul (1901). Étude comparative de la distribution florale dans une portion des Alpes et des Jura, Bulletin de la Société Vaudoise des Sciences Naturelles, 37: 547–579.";
    }

    @Override
    public boolean isApplicable(TaxaBlock taxa, CharactersBlock c) {
        return c.getDataType().equals(CharactersType.Standard);
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock) throws Exception {

        final int ntax = taxaBlock.getNtax();
        distancesBlock.setNtax(ntax);

        progress.setTasks("Jaccard distance", "Init.");
        progress.setMaximum(ntax);

        double maxDist = 0.0;
        int numUndefined = 0;

        for (int s = 1; s <= ntax; s++) {
            for (int t = s + 1; t <= ntax; t++) {
                //System.err.println(s+","+t);
                final PairwiseCompare seqPair = new PairwiseCompare(charactersBlock, s, t);
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
}
