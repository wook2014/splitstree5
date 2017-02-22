package splitstree5.core.algorithms.characters2distances;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;

/**
 * Created by Daria on 22.02.2017.
 */
public class GapDist extends Algorithm<CharactersBlock, DistancesBlock> implements IFromChararacters, IToDistances {

    public final static String DESCRIPTION = "Calculates the gap distance from a set of sequences.";

    /**
     * gets a short description of the algorithm
     *
     * @return a description
     */
    public String getDescription() {
        return DESCRIPTION;
    }


    /**
     * Determine whether gap-distances can be computed with given data.
     *
     * @param taxa the taxa
     * @param c    the characters matrix
     * @return true, if method applies to given data
     */
    public boolean isApplicable(TaxaBlock taxa, CharactersBlock c) {
        //return c.isValid() && taxa.isValid();
        return true;
    }

    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock)
            throws Exception {

        int nchar = charactersBlock.getNchar();
        int ntax = charactersBlock.getNtax();

        distancesBlock.setNtax(ntax);
        char missingchar = charactersBlock.getMissingCharacter();
        char gapchar = charactersBlock.getGapCharacter();
        int c, s, t;

        progressListener.setTasks("Gap distance", "Init.");
        progressListener.setMaximum(ntax);


        for (t = 1; t <= ntax; t++) {
            //char[] row_t = charactersBlock.getRow(t);
            char[] row_t = new char[charactersBlock.getMatrix()[t].length];
            System.arraycopy(charactersBlock.getMatrix()[t], 0, row_t, 0, charactersBlock.getMatrix()[t].length);

            for (s = t + 1; s <= ntax; s++) {
                //char[] row_s = characters.getRow(s);
                char[] row_s = new char[charactersBlock.getMatrix()[s].length];
                System.arraycopy(charactersBlock.getMatrix()[s], 0, row_s, 0, charactersBlock.getMatrix()[s].length);

                double sim = 0;
                double len = 0;
                char sc, tc;
                for (c = 1; c <= nchar; c++) {

                    //if (!characters.isMasked(c)) {
                        sc = row_s[c];
                        tc = row_t[c];

                        double weight = charactersBlock.getCharacterWeight(c);
                        len += weight;
                        if (((sc == gapchar && tc == gapchar) ||
                                (sc != gapchar && tc != gapchar)))
                            sim += weight;
                    //}
                }
                double v = 1.0;
                if (sim != 0 && len != 0) v = (1.0 - sim / len);
                distancesBlock.set(s, t, v);
            }
            progressListener.incrementProgress();
        }
        progressListener.close();
    }
}
