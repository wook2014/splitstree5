/*
 *  Copyright (C) 2019 Daniel H. Huson
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

import jloda.fx.NotificationManager;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.filters.utils.ClosestTree;
import splitstree5.core.algorithms.filters.utils.GreedyCompatible;
import splitstree5.core.algorithms.filters.utils.GreedyWeaklyCompatible;
import splitstree5.core.algorithms.interfaces.IFromSplits;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Compatibility;
import splitstree5.utils.SplitsUtilities;

import java.util.*;

/**
 * splits filter
 * Daniel Huson 12/2016
 */
public class SplitsFilter extends Algorithm<SplitsBlock, SplitsBlock> implements IFromSplits, IToSplits, IFilter {
    public enum FilterAlgorithm {DimensionFilter, ClosestTree, GreedyCompatible, GreedyWeaklyCompatible, None}

    private boolean optionModifyWeightsUsingLeastSquares = false;
    private FilterAlgorithm optionFilterAlgorithm = FilterAlgorithm.DimensionFilter;

    private float optionWeightThreshold = 0;
    private float optionConfidenceThreshold = 0;
    private int optionMaximumDimension = 4;

    private boolean active = false;

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
        active = false;

        ArrayList<ASplit> splits = new ArrayList<>(parent.getSplits());

        if (optionModifyWeightsUsingLeastSquares) {
            NotificationManager.showWarning("optionModifyWeightsUsingLeastSquares: not implemented");
            // modify weights least squares
            //active = true;
        }

        final Map<ASplit, String> split2label = new HashMap<>();
        for (int s = 1; s <= parent.getSplitLabels().size(); s++) {
            split2label.put(parent.get(s), parent.getSplitLabels().get(s));
        }

        Compatibility compatibility = Compatibility.unknown;

        switch (optionFilterAlgorithm) {
            case GreedyCompatible: {
                final int oldSize = splits.size();
                splits = GreedyCompatible.apply(progress, splits);
                compatibility = Compatibility.compatible;
                if (splits.size() != oldSize)
                    active = true;
                break;
            }
            case ClosestTree: {
                final int oldSize = splits.size();
                splits = ClosestTree.apply(progress, taxaBlock.getNtax(), splits, parent.getCycle());
                compatibility = Compatibility.compatible;
                if (splits.size() != oldSize)
                    active = true;
                break;
            }
            case GreedyWeaklyCompatible: {
                final int oldSize = splits.size();
                splits = GreedyWeaklyCompatible.apply(progress, splits);
                if (splits.size() != oldSize)
                    active = true;
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
                active = true;
        }

        if (getOptionConfidenceThreshold() > 0) {
            final int oldSize = splits.size();
            final ArrayList<ASplit> tmp = new ArrayList<>(splits.size());
            for (ASplit split : splits) {
                if (split.getConfidence() >= optionConfidenceThreshold)
                    tmp.add(split);
            }
            splits = tmp;
            if (splits.size() != oldSize)
                active = true;
        }

        if (getOptionMaximumDimension() > 0 && optionFilterAlgorithm == FilterAlgorithm.GreedyCompatible && parent.getCompatibility() != Compatibility.compatible && parent.getCompatibility() != Compatibility.cyclic && parent.getCompatibility() != Compatibility.weaklyCompatible) {
            final int oldSize = splits.size();

            final DimensionFilter dimensionFilter = new DimensionFilter();
            ArrayList<ASplit> existing = new ArrayList<>(splits);
            splits.clear();
            dimensionFilter.apply(progress, optionMaximumDimension, existing, splits);
            if (splits.size() != oldSize)
                active = true;
        }

        child.getSplits().addAll(splits);
        if (split2label.size() > 0) {
            for (int s = 1; s <= child.getNsplits(); s++) {
                final String label = split2label.get(child.get(s));
                child.getSplitLabels().put(s, label);
            }
        }

        if (!active) {
            child.setCycle(parent.getCycle());
            child.setFit(parent.getFit());
            child.setCompatibility(parent.getCompatibility());
            child.setThreshold(parent.getThreshold());
            setShortDescription("using all " + parent.getNsplits() + " splits");
        } else {
            child.setCycle(SplitsUtilities.computeCycle(taxaBlock.getNtax(), child.getSplits()));

            child.setFit(-1);
            if (compatibility == Compatibility.unknown)
                compatibility = Compatibility.compute(taxaBlock.getNtax(), child.getSplits(), child.getCycle());
            child.setCompatibility(compatibility);
            child.setThreshold(parent.getThreshold());
            setShortDescription("using " + child.getNsplits() + " of " + parent.getNsplits() + " splits");
        }
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, SplitsBlock parent) {
        return !parent.isPartial();
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

    @Override
    public boolean isActive() {
        return active;
    }
}
