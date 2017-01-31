/*
 *  Copyright (C) 2016 Daniel H. Huson
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

package splitstree5.core.filters;

import javafx.collections.ListChangeListener;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.filters.utils.ClosestTree;
import splitstree5.core.filters.utils.DimensionFilter;
import splitstree5.core.filters.utils.GreedyCompatible;
import splitstree5.core.filters.utils.GreedyWeaklyCompatible;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Compatibility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * splits filter
 * Created by huson on 12/12/16.
 */
public class SplitsFilterAlgorithm extends Algorithm<SplitsBlock, SplitsBlock> {
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
     * constructor
     *
     * @param parent
     */
    public SplitsFilterAlgorithm(SplitsBlock parent) {
        super("Splits Filter");
        enabledSplits.addAll(parent.getSplits());
        parent.getSplits().addListener((ListChangeListener<ASplit>) c -> {
            while (c.next()) {
                if (c.getRemovedSize() > 0)
                    enabledSplits.removeAll(c.getRemoved());
                if (c.getAddedSize() > 0)
                    enabledSplits.addAll(c.getAddedSubList());
            }
        });
    }

    /**
     * do the computation
     *
     * @param progress
     * @param taxaBlock
     * @param original
     * @param modified
     * @throws CanceledException
     */
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, SplitsBlock original, SplitsBlock modified) throws CanceledException {
        boolean changed = false;
        List<ASplit> splits = new ArrayList<>(original.size());

        if (enabledSplits.size() == 0 || enabledSplits.size() == original.size()) {
            splits.addAll(original.getSplits());
        } else {
            splits.addAll(enabledSplits);
            changed = true;
        }
        if (disabledSplits.size() > 0) {
            splits.removeAll(disabledSplits);
            changed = true;
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
                splits = ClosestTree.apply(progress, taxaBlock.getNtax(), splits, original.getCycle());
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
        if (getOptionWeightThreshold()>0) {
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
        if (getOptionConfidenceThreshold()>0) {
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
        if (getOptionMaximumDimension()>0) {
            final int oldSize = splits.size();
            splits = DimensionFilter.apply(progress, optionMaximumDimension, splits);
            if (splits.size() != oldSize)
                changed = true;
        }

        modified.getSplits().addAll(splits);
        if (!changed) {
            modified.setCycle(original.getCycle());
            modified.setFit(original.getFit());
            modified.setCompatibility(original.getCompatibility());
            modified.setThreshold(original.getThreshold());
        } else {
            modified.setCycle(original.getCycle());
            modified.setFit(-1);
            if (compatibility == Compatibility.unknown)
                compatibility = Compatibility.compute(taxaBlock.getNtax(), modified.getSplits(), modified.getCycle());
            modified.setCompatibility(compatibility);
            modified.setThreshold(original.getThreshold());
        }
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
