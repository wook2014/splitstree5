package splitstree5.core.algorithms.splits2splits;

import jloda.util.Basic;
import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromSplits;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.utils.SplitsUtilities;

/**
 * wrapper for the least squares computations
 *
 * @author huson
 * Date: 17-Feb-2004
 */
public class LeastSquaresWeights extends Algorithm<SplitsBlock, SplitsBlock> implements IFromSplits, IToSplits {

    // todo make dist2splits

    public final static String DESCRIPTION = "Compute least squares weights";
    private boolean optionConstrain = true;
    private DistancesBlock distancesBlock = new DistancesBlock();

    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, SplitsBlock parent, SplitsBlock child)
            throws Exception {

        System.err.println("Computing least squares...");
        progressListener.setMaximum(3);
        /*
        try {
            Writer w = new StringWriter();
            splits.write(w, doc.getTaxa());
            System.err.println(w.toString());
        } catch (Exception ex) {
            Basic.caught(ex);
        }
        */
        progressListener.incrementProgress();
        try {
            LeastSquares.optimizeLS(parent, distancesBlock, getOptionConstrain());
            SplitsUtilities.computeFits(true, parent, distancesBlock, new ProgressPercentage());
        } catch (Exception ex) {
            Basic.caught(ex);
        }
        progressListener.incrementProgress();
        progressListener.close();
        /*
        try {
            Writer w = new StringWriter();
            splits.write(w, doc.getTaxa());
            System.err.println(w.toString());
        } catch (Exception ex) {
            Basic.caught(ex);
        }
        */

    }

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, SplitsBlock parent, SplitsBlock child) {
        return !parent.isPartial();
    }

    /**
     * Gets a short description of the algorithm
     *
     * @return description
     */
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * get constrained optimization?
     *
     * @return flag indicating whether to use constrained least squares.(true = constrained)
     */
    public boolean getOptionConstrain() {
        return optionConstrain;
    }

    /**
     * set constrained optimization
     *
     * @param optionConstrain, flag indicating whether to use constrained least squares (true = constrained)
     */
    public void setOptionConstrain(boolean optionConstrain) {
        this.optionConstrain = optionConstrain;
    }

    public void setDistancesBlock(DistancesBlock dist) {
        this.distancesBlock.copy(dist);
    }
}
