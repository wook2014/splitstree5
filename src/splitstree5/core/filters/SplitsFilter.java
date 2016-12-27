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
import splitstree5.core.Document;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ISplit;

import java.util.ArrayList;

/**
 * splits filter
 * Created by huson on 12/12/16.
 */
public class SplitsFilter extends AConnector<SplitsBlock, SplitsBlock> {
    private final ObservableList<ISplit> enabledData = FXCollections.observableArrayList();
    private final ObservableList<ISplit> disabledData = FXCollections.observableArrayList();

    /**
     * /**
     * constructor
     *
     * @param parent
     * @param child
     */
    public SplitsFilter(Document document, TaxaBlock taxaBlock, ADataNode<SplitsBlock> parent, ADataNode<SplitsBlock> child) {
        super(document, taxaBlock, parent, child);

        enabledData.addAll(parent.getDataBlock().getSplits());
        parent.getDataBlock().getSplits().addListener(new ListChangeListener<ISplit>() {
            @Override
            public void onChanged(Change<? extends ISplit> c) {
                while (c.next()) {
                    if (c.getRemovedSize() > 0)
                        enabledData.removeAll(c.getRemoved());
                    if (c.getAddedSize() > 0)
                        enabledData.addAll(c.getAddedSubList());
                }
            }
        });

        setAlgorithm(new Algorithm<SplitsBlock, SplitsBlock>() {
            public void compute(TaxaBlock taxaBlock, SplitsBlock original, SplitsBlock modified) {
                modified.getSplits().clear();

                final ArrayList<ISplit> list = new ArrayList<>();
                if (enabledData.size() == 0)
                    list.addAll(original.getSplits());
                else
                    list.addAll(enabledData);
                list.removeAll(disabledData);

                for (ISplit split : list) {
                    if (!getDisabledData().contains(split) && parent.getDataBlock().getSplits().contains(split)) {
                        modified.getSplits().add(split);
                    }
                }
            }
        });
    }

    /**
     * get the set of enabledData data.
     *
     * @return list of explicitly enabledData taxa
     */
    public ObservableList<ISplit> getEnabledData() {
        return enabledData;
    }

    /**
     * gets disabledData taxa
     *
     * @return disabledData
     */
    public ObservableList<ISplit> getDisabledData() {
        return disabledData;
    }
}
