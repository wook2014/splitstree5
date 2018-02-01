package splitstree5.core.algorithms.characters2distances;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;

/**
 * Computes the gap distance from a set of sequences
 * <p>
 * created on Nov 2009
 *
 * @author bryant
 */
public class GapDist extends Algorithm<CharactersBlock, DistancesBlock> implements IFromChararacters, IToDistances {

    public final static String DESCRIPTION = "Calculates the gap distance from a set of sequences.";

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock)
            throws Exception {

        int nchar = charactersBlock.getNchar();
        int ntax = charactersBlock.getNtax();

        distancesBlock.setNtax(ntax);
        char missingchar = charactersBlock.getMissingCharacter();
        char gapchar = charactersBlock.getGapCharacter();
        int c, s, t;

        progress.setTasks("Gap distance", "Init.");
        progress.setMaximum(ntax);

        //todo getrow
        for (t = 0; t < ntax; t++) {
            char[] row_t = charactersBlock.getRow0(t);
            //char[] row_t = new char[charactersBlock.getMatrix()[t].length];
            //System.arraycopy(charactersBlock.getMatrix()[t], 0, row_t, 0, charactersBlock.getMatrix()[t].length);

            for (s = t + 1; s < ntax; s++) {
                char[] row_s = charactersBlock.getRow0(s);
                //char[] row_s = new char[charactersBlock.getMatrix()[s].length];
                //System.arraycopy(charactersBlock.getMatrix()[s], 0, row_s, 0, charactersBlock.getMatrix()[s].length);

                double sim = 0;
                double len = 0;
                char sc, tc;
                for (c = 0; c < nchar; c++) {
                    sc = row_s[c];
                    tc = row_t[c];

                    double weight = charactersBlock.getCharacterWeight(c);
                    len += weight;
                    if (((sc == gapchar && tc == gapchar) ||
                            (sc != gapchar && tc != gapchar)))
                        sim += weight;
                }
                double v = 1.0;
                if (sim != 0 && len != 0) v = (1.0 - sim / len);
                distancesBlock.set(s + 1, t + 1, v);
                distancesBlock.set(t + 1, s + 1, v);
            }
            progress.incrementProgress();
        }
        progress.close();
    }

    public String getDescription() {
        return DESCRIPTION;
    }
}
