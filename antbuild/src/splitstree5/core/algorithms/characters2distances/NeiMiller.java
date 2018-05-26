package splitstree5.core.algorithms.characters2distances;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.characters.CharactersType;

/**
 * Computes the Nei and Miller (1990) distance from a set of characters
 * <p>
 * Created on 2008-03-17
 *
 * @author bryant
 */
public class NeiMiller extends Algorithm<CharactersBlock, DistancesBlock> implements IFromChararacters, IToDistances {

    public final static String DESCRIPTION = "Calculate distances from restriction-sites using Nei and Miller (1990).";

    @Override
    public String getCitation() {
        return "Nei and Miller 1990; " +
                "M. Nei and J.C. Miller. A simple method for estimating average number of nucleotide substitutions within and between populations from restriction data. " +
                "Genetics, 125:873–879, 1990.";
    }

    @Override
    public boolean isApplicable(TaxaBlock taxa, CharactersBlock chars) {
        /*return taxa != null && chars != null
                && chars.getFormat().getDatatype().equalsIgnoreCase(Characters.Datatypes.STANDARD)
                && chars.hasCharweights();*/
        return taxa != null && chars != null
                && chars.getDataType().equals(CharactersType.Standard)
                && chars.getCharacterWeights() != null;
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock)
            throws Exception {

        int nchar = charactersBlock.getNchar();
        int ntax = charactersBlock.getNtax();

        distancesBlock.setNtax(ntax);

        int c, i, j, k;

        //distancesBlock.setNtax(ntax);

        boolean warned_sij = false, warned_dhij = false, warned_dist = false;

        // Determine enzyme classes etc:

        double[] class_value = new double[nchar + 1];     // Value for given enzyme class
        int[] class_size = new int[nchar + 1];                  // number of positions for class
        int[] char2class = new int[nchar + 1];        // Maps characters to enzyme classes
        int num_classes = 0;                    // Number of different classes


        int maxProgress = 5 * taxaBlock.getNtax() + charactersBlock.getNchar();

        progress.setTasks("NeiMiller distance", "Init.");
        progress.setMaximum(maxProgress);

        for (c = 1; c <= nchar; c++) {
            //if (!characters.isMasked(c)) {
            boolean found = false;
            for (k = 1; k <= num_classes; k++) {
                if (class_value[k] == charactersBlock.getCharacterWeight(c)) {// belongs to class already encountered
                    char2class[c] = k;
                    class_size[k]++;
                    found = true;
                    break;
                }
            }
            if (!found) // new class
            {
                ++num_classes;
                char2class[c] = num_classes;
                class_value[num_classes] = charactersBlock.getCharacterWeight(c);
                class_size[num_classes] = 1;
            }
            //}

            //doc.notifySetProgress(100 * c / maxProgress);
            progress.incrementProgress();
        }

        // Compute mij_k:

        int[][][] mij_k = new int[ntax + 1][ntax + 1][num_classes + 1];

        for (i = 1; i <= ntax; i++) {
            for (j = i; j <= ntax; j++) {
                for (c = 1; c <= nchar; c++) {
                    //if (!characters.isMasked(c)) {
                    if (charactersBlock.get(i, c) == '1' && charactersBlock.get(j, c) == '1') {
                        mij_k[i][j][char2class[c]]++;
                    }
                    //}
                }
            }

            //doc.notifySetProgress((characters.getNchar() + i) * 100 / maxProgress);
            progress.incrementProgress();
        }

        // Compute sij_k  (equation 2):

        double[][][] sij_k = new double[ntax + 1][ntax + 1][num_classes + 1];
        for (i = 1; i <= ntax; i++) {
            for (j = i + 1; j <= ntax; j++) {
                for (k = 1; k <= num_classes; k++) {
                    double bot = mij_k[i][i][k] + mij_k[j][j][k];

                    if (bot != 0)
                        sij_k[i][j][k] = (2 * mij_k[i][j][k]) / bot;
                    else {
                        if (!warned_sij) {
                            System.err.println("nei_miller: denominator zero in equation (2)");
                            warned_sij = true;
                        }
                        sij_k[i][j][k] = 100000;
                    }
                }
            }

            //doc.notifySetProgress((characters.getNchar() + ntax + i) * 100 / maxProgress);
            progress.incrementProgress();
        }

        // Compute dhij_k (i.e. dij_k_hat in equation (3)):

        double[][][] dhij_k = new double[ntax + 1][ntax + 1][num_classes + 1];

        for (i = 1; i <= ntax; i++) {
            for (j = i + 1; j <= ntax; j++) {
                for (k = 1; k <= num_classes; k++) {
                    if (class_value[k] == 0) {
                        dhij_k[i][j][k] = 100000;
                        if (!warned_dhij) {
                            System.err.println("nei_miller: denominator zero in equation (3)");
                            warned_dhij = true;
                        }
                    } else
                        dhij_k[i][j][k]
                                = (-Math.log(sij_k[i][j][k])) / class_value[k]; // equation (3)
                }
            }

            //doc.notifySetProgress(100 * (characters.getNchar() + 2 * ntax + i) / maxProgress);
            progress.incrementProgress();
        }

        // Compute mk_k (mk_bar=(mii_k+mjj_k)/2):

        double[][][] mk_k = new double[ntax + 1][ntax + 1][num_classes + 1];

        for (i = 1; i <= ntax; i++) {
            for (j = i; j <= ntax; j++) {
                for (k = 1; k <= num_classes; k++) {
                    mk_k[i][j][k] = (mij_k[i][i][k] + mij_k[j][j][k]) / 2.0;
                }
            }

            //doc.notifySetProgress((100 * characters.getNchar() + 3 * ntax + i) / maxProgress);
            progress.incrementProgress();
        }

        // Computes the distances as described in equation (4):

        for (i = 1; i <= ntax; i++) {
            for (j = i + 1; j <= ntax; j++) {
                // Computes the bottom of equation 4:
                double bottom = 0;
                for (k = 1; k <= num_classes; k++)
                    bottom += mk_k[i][j][k] * class_value[k];

                // Computes the top of equation 4:
                double top = 0;
                for (k = 1; k <= num_classes; k++)
                    top += mk_k[i][j][k] * class_value[k] * dhij_k[i][j][k];

                if (bottom != 0)
                    distancesBlock.set(i, j, top / bottom);
                else {
                    if (!warned_dist) {
                        System.err.println("nei_miller: denominator zero in equation (4)");
                        warned_dist = true;
                    }
                    distancesBlock.set(i, j, 1);
                }
            }

            //doc.notifySetProgress(100 * (characters.getNchar() + 4 * ntax + i) / maxProgress);
            progress.incrementProgress();
        }
        progress.close();
    }

    // GETTER AND SETTER
    public String getDescription() {
        return DESCRIPTION;
    }
}
