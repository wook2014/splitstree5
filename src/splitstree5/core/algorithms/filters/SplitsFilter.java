/*
 *  Copyright (C) 2018 Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
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
 */

package splitstree5.core.algorithms.filters;

import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.filters.utils.ClosestTree;
import splitstree5.core.algorithms.filters.utils.DimensionFilter;
import splitstree5.core.algorithms.filters.utils.GreedyCompatible;
import splitstree5.core.algorithms.filters.utils.GreedyWeaklyCompatible;
import splitstree5.core.algorithms.interfaces.IFromSplits;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Compatibility;
import splitstree5.core.misc.SplitsUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * splits filter
 * Daniel Huson 12/2016
 */
public class SplitsFilter extends Algorithm<SplitsBlock, SplitsBlock> implements IFromSplits, IToSplits {
    public enum FilterAlgorithm {None, GreedyCompatible, ClosestTree, GreedyWeaklyCompatible}

    private final ArrayList<ASplit> enabledSplits = new ArrayList<>();
    private final ArrayList<ASplit> disabledSplits = new ArrayList<>();

    private boolean optionModifyWeightsUsingLeastSquares = false;
    private FilterAlgorithm optionFilterAlgorithm = FilterAlgorithm.None;

    private float optionWeightThreshold = 0;
    private float optionConfidenceThreshold = 0;
    private int optionMaximumDimension = 4;

    public List<String> listOptions() {
        return Arrays.asList("optionFilterAlgorithm", "optionWeightThreshold", "optionConfidenceThreshold", "optionMaximumDimension");
    }

    /**
     * do the computation
     *
     * @param progress
     * @param taxaBlock
     * @param parent
     * @param child
     * @throws CanceledException
     */
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, SplitsBlock parent, SplitsBlock child) throws CanceledException {
        boolean changed = false;
        List<ASplit> splits = new ArrayList<>(parent.size());

        if (enabledSplits.size() == 0 && disabledSplits.size() == 0) // nothing has been explicitly set, copy everything
            child.getSplits().setAll(parent.getSplits());
        else {
            for (ASplit split : enabledSplits) {
                if (!disabledSplits.contains(split)) {
                    child.getSplits().add(split);
                }
            }
        }

        if (optionModifyWeightsUsingLeastSquares) {
            System.err.println("optionModifyWeightsUsingLeastSquares: not implemented");
            // modify weights least squares
            //changed = true;
        }
        Compatibility compatibility = Compatibility.unknown;

        switch (optionFilterAlgorithm) {
            case GreedyCompatible: {
                final int oldSize = splits.size();
                splits = GreedyCompatible.apply(progress, splits);
                compatibility = Compatibility.compatible;
                if (splits.size() != oldSize)
                    changed = true;
                break;
            }
            case ClosestTree: {
                final int oldSize = splits.size();
                splits = ClosestTree.apply(progress, taxaBlock.getNtax(), splits, parent.getCycle());
                compatibility = Compatibility.compatible;
                if (splits.size() != oldSize)
                    changed = true;
                break;
            }
            case GreedyWeaklyCompatible: {
                final int oldSize = splits.size();
                splits = GreedyWeaklyCompatible.apply(progress, splits);
                if (splits.size() != oldSize)
                    changed = true;
                break;
            }
        }
        if (getOptionWeightThreshold() > 0) {
            final int oldSize = splits.size();
            ArrayList<ASplit> tmp = new ArrayList<>(splits.size());
            for (ASplit split : splits) {
                if (split.getWeight() >= optionWeightThreshold)
                    tmp.add(split);
            }
            splits = tmp;
            if (splits.size() != oldSize)
                changed = true;
        }
        if (getOptionConfidenceThreshold() > 0) {
            final int oldSize = splits.size();
            ArrayList<ASplit> tmp = new ArrayList<>(splits.size());
            for (ASplit split : splits) {
                if (split.getConfidence() >= optionConfidenceThreshold)
                    tmp.add(split);
            }
            splits = tmp;
            if (splits.size() != oldSize)
                changed = true;
        }
        if (getOptionMaximumDimension() > 0) {
            final int oldSize = splits.size();
            splits = DimensionFilter.apply(progress, optionMaximumDimension, splits);
            if (splits.size() != oldSize)
                changed = true;
        }

        child.getSplits().addAll(splits);
        if (!changed) {
            child.setCycle(parent.getCycle());
            child.setFit(parent.getFit());
            child.setCompatibility(parent.getCompatibility());
            child.setThreshold(parent.getThreshold());
        } else {
            child.setCycle(SplitsUtilities.computeCycle(taxaBlock.getNtax(), child.getSplits()));

            child.setFit(-1);
            if (compatibility == Compatibility.unknown)
                compatibility = Compatibility.compute(taxaBlock.getNtax(), child.getSplits(), child.getCycle());
            child.setCompatibility(compatibility);
            child.setThreshold(parent.getThreshold());
        }

        if (enabledSplits.size() == 0 && disabledSplits.size() == 0)
            setShortDescription(null);
        else if (disabledSplits.size() == 0)
            setShortDescription("Enabled: " + enabledSplits.size());
        else
            setShortDescription("Enabled: " + enabledSplits.size() + " (of " + (enabledSplits.size() + disabledSplits.size() + ")"));
    }

    @Override
    public void clear() {
        super.clear();
        enabledSplits.clear();
        disabledSplits.clear();
    }

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, SplitsBlock parent, SplitsBlock child) {
        return !parent.isPartial();
    }

    /**
     * get the set of enabledSplits data.
     *
     * @return list of explicitly enabledSplits taxa
     */
    public ArrayList<ASplit> getEnabledSplits() {
        return enabledSplits;
    }

    /**
     * gets disabledSplits taxa
     *
     * @return disabledSplits
     */
    public ArrayList<ASplit> getDisabledSplits() {
        return disabledSplits;
    }

    public boolean isOptionModifyWeightsUsingLeastSquares() {
        return optionModifyWeightsUsingLeastSquares;
    }

    public void setOptionModifyWeightsUsingLeastSquares(boolean optionModifyWeightsUsingLeastSquares) {
        this.optionModifyWeightsUsingLeastSquares = optionModifyWeightsUsingLeastSquares;
    }

    public FilterAlgorithm getOptionFilterAlgorithm() {
        return optionFilterAlgorithm;
    }

    public void setOptionFilterAlgorithm(FilterAlgorithm optionFilterAlgorithm) {
        this.optionFilterAlgorithm = optionFilterAlgorithm;
    }

    public float getOptionWeightThreshold() {
        return optionWeightThreshold;
    }

    public void setOptionWeightThreshold(float optionWeightThreshold) {
        this.optionWeightThreshold = optionWeightThreshold;
    }

    public float getOptionConfidenceThreshold() {
        return optionConfidenceThreshold;
    }

    public void setOptionConfidenceThreshold(float optionConfidenceThreshold) {
        this.optionConfidenceThreshold = optionConfidenceThreshold;
    }

    public int getOptionMaximumDimension() {
        return optionMaximumDimension;
    }

    public void setOptionMaximumDimension(int optionMaximumDimension) {
        this.optionMaximumDimension = optionMaximumDimension;
    }
}
