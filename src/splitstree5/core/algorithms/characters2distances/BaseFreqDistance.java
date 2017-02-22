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
public class BaseFreqDistance extends Algorithm<CharactersBlock, DistancesBlock> implements IFromChararacters, IToDistances {

    private boolean optionIgnoreGaps = true;

    public final static String DESCRIPTION = "Calculates distances from differences in the base composition";
    protected String TASK = "Base Frequency Distance";

    protected String getTask() {
        return TASK;
    }

    /**
     * Determine whether Hamming distances can be computed with given data.
     *
     * @param taxa the taxa
     * @param c    the characters matrix
     * @return true, if method applies to given data
     */
    public boolean isApplicable(TaxaBlock taxa, CharactersBlock c) {
        //return taxa != null && c != null;
        return true;
    }


    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock)
            throws Exception {

        String symbols = charactersBlock.getSymbols();
        int nstates = symbols.length();

        int ntax = taxaBlock.getNtax();
        distancesBlock.setNtax(ntax);

        progressListener.setTasks(TASK, "Init.");
        progressListener.setMaximum(ntax);

        double[][] baseFreqs = new double[ntax + 1][nstates];
        System.err.println("Base Frequencies");

        for (int s = 1; s <= ntax; s++) {
            System.err.print(taxaBlock.getLabel(s) + "\t");
            double count = 0;
            for (int i = 0; i < charactersBlock.getNchar(); i++) {
                int x = symbols.indexOf(charactersBlock.get(s, i));
                if (x >= 0) {
                    double weight = charactersBlock.getCharacterWeight(i);
                    baseFreqs[s][x] += weight;
                    count += weight;
                }
            }

            for (int x = 0; x < nstates; x++) {
                baseFreqs[s][x] /= count;
                System.err.print("" + baseFreqs[s][x] + "\t");
            }
            System.err.println("");
        }


        for (int s = 1; s <= ntax; s++) {
            for (int t = s + 1; t <= ntax; t++) {
                double p = 0.0;
                for (int i = 0; i < nstates; i++) {
                    double pi_i = baseFreqs[s][i];
                    double pihat_i = baseFreqs[t][i];
                    //  if (pi_i>0.0 && pihat_i>0.0)
                    //    p+=pi_i * Math.log(pi_i/pihat_i) + pihat_i * Math.log(pihat_i/pi_i);
                    p += Math.abs(pi_i - pihat_i);
                }

                distancesBlock.set(s, t, p);
                distancesBlock.set(t, s, p);
            }
            progressListener.incrementProgress();
        }
        progressListener.close();
    }

    // GETTER AND SETTER

    /**
     * ignore gaps?
     *
     * @return true if gaps are ignored
     */
    public boolean getOptionignoregaps() {
        return optionIgnoreGaps;
    }

    /**
     * set option ignore gaps
     *
     * @param ignore
     */
    public void setOptionignoregaps(boolean ignore) {
        optionIgnoreGaps = ignore;
    }

    /**
     * Gets a short description of the algorithm
     *
     * @return description
     */
    public String getDescription() {
        return DESCRIPTION;
    }
}
