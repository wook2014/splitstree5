package splitstree5.core.algorithms.splits2splits;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

import java.util.Collections;
import java.util.List;

/**
 * wrapper for the least squares computations
 *
 * @author huson
 * Date: 17-Feb-2004
 */
public class LeastSquaresWeights extends Algorithm<SplitsBlock, SplitsBlock> implements IFromSplits, IToSplits {
    final DistancesBlock distancesBlock = new DistancesBlock();

    // todo make dist2splits

    private final BooleanProperty optionConstrain = new SimpleBooleanProperty(true);

    @Override
    public List<String> listOptions() {
        return Collections.singletonList("Constrain");
    }

    @Override
    public String getToolTip(String optionName) {
        if ("Constrain".equals(optionName)) {
            return "Use constrained least squares";
        }
        return optionName;
    }


    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, SplitsBlock parent, SplitsBlock child) throws Exception {

        System.err.println("Computing least squares...");
        progress.setMaximum(3);
        /*
        try {
            Writer w = new StringWriter();
            splits.write(w, doc.getTaxa());
            System.err.println(w.toString());
        } catch (Exception ex) {
            Basic.caught(ex);
        }
        */
        progress.incrementProgress();
        try {
            child.copy(parent);
            LeastSquares.optimizeLS(child, distancesBlock, isOptionConstrain());
            SplitsUtilities.computeFits(true, parent, distancesBlock, new ProgressPercentage());
        } catch (Exception ex) {
            Basic.caught(ex);
        }
        progress.incrementProgress();
        progress.close();
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
    public boolean isApplicable(TaxaBlock taxaBlock, SplitsBlock parent) {
        return !parent.isPartial();
    }

    public boolean isOptionConstrain() {
        return optionConstrain.get();
    }

    public BooleanProperty optionConstrainProperty() {
        return optionConstrain;
    }

    public void setOptionConstrain(boolean optionConstrain) {
        this.optionConstrain.set(optionConstrain);
    }

    public void setDistancesBlock(DistancesBlock dist) {
        this.distancesBlock.copy(dist);
    }
}
