package splitstree5.core.algorithms.characters2distances;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.characters2distances.old_nucleotide.DNAdistance;
import splitstree5.core.algorithms.characters2distances.utils.SaturatedDistancesException;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.models.JCmodel;

import java.util.Arrays;
import java.util.List;

/**
 * Computes the Jukes Cantor distance for a set of characters
 * <p>
 * Created on 12-Jun-2004
 *
 * @author DJB
 */

public class JukesCantor extends DNAdistance implements IFromChararacters, IToDistances {
    @Override
    public String getCitation() {
        return "Jukes and Cantor 1969; Jukes TH & Cantor CR (1969). Evolution of Protein Molecules. New York: Academic Press. pp. 21–132";
    }

    @Override
    public List<String> listOptions() {
        return Arrays.asList("PropInvariableSites", "SetParameters", "UseML");
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock)
            throws Exception {

        progress.setTasks("Jukes-Cantor Distance", "Init.");
        progress.setMaximum(taxaBlock.getNtax());

        final JCmodel model = new JCmodel();
        model.setPropInvariableSites(getOptionPropInvariableSites());
        //model.setGamma(getOptionGamma());

        distancesBlock.copy(fillDistanceMatrix(progress, charactersBlock, model));
    }

    @Override
    protected double exactDist(double[][] F) throws SaturatedDistancesException {
        double D = 1 - (F[0][0] + F[1][1] + F[2][2] + F[3][3]);
        double B = 0.75;
        return -B * Minv(1 - D / B);
    }
}
