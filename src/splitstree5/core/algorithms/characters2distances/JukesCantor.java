package splitstree5.core.algorithms.characters2distances;

import jloda.util.ProgressListener;
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

    public final static String DESCRIPTION = "Calculates distances using the Jukes Cantor model";

    @Override
    public String getCitation() {
        return "Swofford et al 1996; " +
                "D.L. Swofford, G.J. Olsen, P.J. Waddell, and  D.M. Hillis. Chapter  11:  Phylogenetic inference. " +
                "In D. M. Hillis, C. Moritz, and B. K. Mable, editors, Molecular Systematics, pages 407–514. " +
                "Sinauer Associates, Inc., 2nd edition, 1996.";
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

    // GETTER AND SETTER
    public String getDescription() {
        return DESCRIPTION;
    }
}
