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

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.filters.utils.ClosestTree;
import splitstree5.core.filters.utils.GreedyCompatible;
import splitstree5.core.filters.utils.GreedyWeaklyCompatible;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Compatibility;

import java.util.List;

/**
 * splits filter
 * Created by huson on 12/12/16.
 */
public class SplitsFilter extends AConnector<SplitsBlock, SplitsBlock> {
    public enum Filter {GreedyCompatible, ClosestTree, GreedyWeaklyCompatible, WeightThreshold, ConfidenceThreshold, MaximumDimension, None}

    private final ObservableList<ASplit> enabledData = FXCollections.observableArrayList();
    private final ObservableList<ASplit> disabledData = FXCollections.observableArrayList();

    private boolean optionModifyWeightsUsingLeastSquares = false;
    private Filter optionSelectSplitsUsingFilter = Filter.MaximumDimension;

    private float optionWeightThreshold = 0;
    private float optionCondifidenceThreshold = 0;
    private int optionMaximumDimension = 4;

    /**
     * /**
     * constructor
     *
     * @param parent
     * @param child
     */
    public SplitsFilter(TaxaBlock taxaBlock, ADataNode<SplitsBlock> parent, ADataNode<SplitsBlock> child) {
        super(taxaBlock, parent, child);

        enabledData.addAll(parent.getDataBlock().getSplits());
        parent.getDataBlock().getSplits().addListener(new ListChangeListener<ASplit>() {
            @Override
            public void onChanged(Change<? extends ASplit> c) {
                while (c.next()) {
                    if (c.getRemovedSize() > 0)
                        enabledData.removeAll(c.getRemoved());
                    if (c.getAddedSize() > 0)
                        enabledData.addAll(c.getAddedSubList());
                }
            }
        });

        setAlgorithm(new Algorithm<SplitsBlock, SplitsBlock>() {
            public void compute(ProgressListener progress, TaxaBlock taxaBlock, SplitsBlock original, SplitsBlock modified) throws CanceledException {
                boolean changed = false;
                List<ASplit> splits = original.getSplits();
                if (optionModifyWeightsUsingLeastSquares) {
                    // modify weights least squares
                    // reset
                    changed = true;
                }
                Compatibility compatibility = Compatibility.unknown;

                switch (optionSelectSplitsUsingFilter) {
                    case GreedyCompatible:
                        splits = GreedyCompatible.apply(progress, splits);
                        compatibility = Compatibility.compatible;
                        changed = true;
                        break;
                    case ClosestTree:
                        changed = true;
                        splits = ClosestTree.apply(progress, taxaBlock.getNtax(), splits, original.getCycle());
                        compatibility = Compatibility.compatible;
                        break;
                    case GreedyWeaklyCompatible:
                        splits = GreedyWeaklyCompatible.apply(progress, splits);
                        changed = true;
                        break;
                    case WeightThreshold:
                        changed = true;
                        break;
                    case ConfidenceThreshold:
                        changed = true;
                        break;
                    case MaximumDimension:
                        changed = true;
                        break;
                    default:
                    case None:
                        break;
                }

                if (disabledData.size() > 0) {
                    splits.removeAll(disabledData);
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
        });
    }

    /**
     * get the set of enabledData data.
     *
     * @return list of explicitly enabledData taxa
     */
    public ObservableList<ASplit> getEnabledData() {
        return enabledData;
    }

    /**
     * gets disabledData taxa
     *
     * @return disabledData
     */
    public ObservableList<ASplit> getDisabledData() {
        return disabledData;
    }
}
