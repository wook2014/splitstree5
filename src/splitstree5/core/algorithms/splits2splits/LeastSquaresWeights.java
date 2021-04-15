/*
 * LeastSquaresWeights.java Copyright (C) 2021. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
