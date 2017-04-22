package splitstree5.core.algorithms.characters2distances;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.utils.nexus.SplitsException;

public class Codominant extends Algorithm<CharactersBlock, DistancesBlock> implements IFromChararacters, IToDistances {

    protected String TASK = "Codominant Genetic Distance";
    protected String DESCRIPTION = "Codominant Genetic Distance for diploid characters (Smouse & Peakall 1999)";

    /**
     * In Smouse and Peakall, the final distance is the square root of the contribution of the
     * individual loci. This flag sets whether to use this square root, or just the averages
     * over the loci.
     */
    protected boolean useSquareRoot;

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock){
        return !charactersBlock.isUseCharacterWeights() && charactersBlock.isDiploid();
    }

    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock)
            throws Exception {

        distancesBlock.setNtax(taxaBlock.getNtax());

        char missingchar = charactersBlock.getMissingCharacter();
        char gapchar = charactersBlock.getGapCharacter();


        int ntax = taxaBlock.getNtax();

        for (int i = 0; i < ntax; i++) {
            char[] seqi = charactersBlock.getRow0(i);

            for (int j = i + 1; j < ntax; j++) {

                char[] seqj = charactersBlock.getRow0(j);
                double distSquared = 0.0;


                int nchar = charactersBlock.getNchar();
                int nLoci = nchar / 2;
                int nValidLoci = 0;

                for (int k = 0; k < nLoci; k++) {

                    char ci1 = seqi[2 * k];System.out.println("ci1 "+(2*k)+" "+ci1);
                    char ci2 = seqi[2 * k+1];System.out.println("ci2 "+(2*k+1)+" "+ci2);
                    char cj1 = seqj[2 * k];System.out.println("cj1 "+(2*k)+" "+cj1);
                    char cj2 = seqj[2 * k+1];System.out.println("cj2 "+(2*k+1)+" "+cj2);

                    if (ci1 == missingchar || ci2 == missingchar || cj1 == missingchar || cj2 == missingchar)
                        continue;
                    if (ci1 == gapchar || ci2 == gapchar || cj1 == gapchar || cj2 == gapchar)
                        continue;

                    nValidLoci++;

                    int diff;

                    if (ci1 == ci2) { //AA vs ...
                        if (cj1 == cj2) {
                            if (ci1 != cj1)
                                diff = 4;   //AA vs BB
                            else
                                diff = 0;  //AA vs AA
                        } else {  //AA vs XY
                            if (ci1 == cj1 || ci1 == cj2)
                                diff = 1; //AA vs AY
                            else
                                diff = 3; //AA vs BC
                        }
                    } else {     //AB vs ...
                        if (cj1 == cj2) {  //AB vs XX
                            if (ci1 == cj1 && ci2 == cj1)
                                diff = 1;   //AB vs AA
                            else
                                diff = 3;   //AB vs CC
                        } else {  //AB vs XY
                            if ((ci1 == cj1 && ci2 == cj2) || (ci1 == cj2 && ci2 == cj1))
                                diff = 0; //AB vs BA or AB vs AB
                            else if (ci1 == cj1 || ci2 == cj2 || ci1 == cj2 || ci2 == cj1)
                                diff = 1;   //AB vs AC
                            else
                                diff = 2;   //AB vs CD
                        }
                    }

                    distSquared += (double) diff;
                }

                double dij = nchar / 2.0 * distSquared / (double) nValidLoci;
                if (getOptionUseSquareRoot())
                    dij = Math.sqrt(dij);

                distancesBlock.set(i+1, j+1, Math.sqrt(dij));
                distancesBlock.set(j+1, i+1, Math.sqrt(dij));
            }
        }
        /*char missingchar = charactersBlock.getMissingCharacter();
        char gapchar = charactersBlock.getGapCharacter();


        int ntax = taxaBlock.getNtax();
        distancesBlock.setNtax(ntax);

        progressListener.setTasks(TASK, "Init.");
        progressListener.setMaximum(ntax);

        for (int i = 0; i < ntax; i++) {
            char[] seqi = charactersBlock.getRow0(i);

            //char[] seqi = new char[charactersBlock.getMatrix()[i].length];
            //System.arraycopy(charactersBlock.getMatrix()[i], 0, seqi, 0, charactersBlock.getMatrix()[i].length);

            for (int j = i + 1; j < ntax; j++) {

                char[] seqj = charactersBlock.getRow0(j);
                //char[] seqj = new char[charactersBlock.getMatrix()[j].length];
                //System.arraycopy(charactersBlock.getMatrix()[j], 0, seqj, 0, charactersBlock.getMatrix()[j].length);

                double distSquared = 0.0;


                int nchar = charactersBlock.getNchar();
                int nLoci = nchar / 2;
                int nValidLoci = 0;


                for (int k = 1; k < nLoci; k++) {
                    char ci1 = seqi[2 * k - 1];
                    char ci2 = seqi[2 * k];
                    char cj1 = seqj[2 * k - 1];
                    char cj2 = seqj[2 * k];

                    if (ci1 == missingchar || ci2 == missingchar || cj1 == missingchar || cj2 == missingchar)
                        continue;
                    if (ci1 == gapchar || ci2 == gapchar || cj1 == gapchar || cj2 == gapchar)
                        continue;

                    nValidLoci++;

                    int diff;

                    if (ci1 == ci2) { //AA vs ...
                        if (cj1 == cj2) {
                            if (ci1 != cj1)
                                diff = 4;   //AA vs BB
                            else
                                diff = 0;  //AA vs AA
                        } else {  //AA vs XY
                            if (ci1 == cj1 || ci1 == cj2)
                                diff = 1; //AA vs AY
                            else
                                diff = 3; //AA vs BC
                        }
                    } else {     //AB vs ...
                        if (cj1 == cj2) {  //AB vs XX
                            if (ci1 == cj1 && ci2 == cj1)
                                diff = 1;   //AB vs AA
                            else
                                diff = 3;   //AB vs CC
                        } else {  //AB vs XY
                            if ((ci1 == cj1 && ci2 == cj2) || (ci1 == cj2 && ci2 == cj1))
                                diff = 0; //AB vs BA or AB vs AB
                            else if (ci1 == cj1 || ci2 == cj2 || ci1 == cj2 || ci2 == cj1)
                                diff = 1;   //AB vs AC
                            else
                                diff = 2;   //AB vs CD
                        }
                    }

                    distSquared += (double) diff;
                }

                double dij = nchar / 2.0 * distSquared / (double) nValidLoci;
                if (getOptionUseSquareRoot())
                    dij = Math.sqrt(dij);

                distancesBlock.set(i+1, j+1, Math.sqrt(dij));
                distancesBlock.set(j+1, i+1, Math.sqrt(dij));
            }
            progressListener.incrementProgress();
        }
        progressListener.close();*/
    }

    // GETTER AND SETTER

    /**
     * gets a short description of the algorithm
     *
     * @return a description
     */
    public String getDescription() {
        return DESCRIPTION;
    }

    protected String getTask() {
        return TASK;
    }

    /**
     * Get the flag indicating if the distance computed is the square root of the contributions
     * of the loci (as in (Smouse and Peakall 99).
     *
     * @return boolean flag that is true if we use the square root in the final calculation.
     */
    public boolean getOptionUseSquareRoot() {
        return useSquareRoot;
    }

    /**
     * Set the flag indicating if the distance computed is the square root of the contributions
     * of the loci (as in (Smouse and Peakall 99).
     *
     * @param useSquareRoot flag that is true if we use the square root in the final calculation.
     */
    public void setOptionUseSquareRoot(boolean useSquareRoot) {
        this.useSquareRoot = useSquareRoot;
    }
}
