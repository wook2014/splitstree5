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
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.ITree;

import java.util.ArrayList;

/**
 * trees filter
 * Created by huson on 12/12/16.
 */
public class TreesFilter extends AConnector<TreesBlock, TreesBlock> {
    private final ObservableList<ITree> enabledData = FXCollections.observableArrayList();
    private final ObservableList<ITree> disabledData = FXCollections.observableArrayList();

    /**
     * /**
     * constructor
     *
     * @param document
     * @param parent
     * @param child
     */
    public TreesFilter(Document document, TaxaBlock taxaBlock, ADataNode<TreesBlock> parent, ADataNode<TreesBlock> child) {
        super(document, taxaBlock, parent, child);

        enabledData.addAll(parent.getDataBlock().getTrees());
        parent.getDataBlock().getTrees().addListener(new ListChangeListener<ITree>() {
            @Override
            public void onChanged(Change<? extends ITree> c) {
                while (c.next()) {
                    if (c.getRemovedSize() > 0)
                        enabledData.removeAll(c.getRemoved());
                    if (c.getAddedSize() > 0)
                        enabledData.addAll(c.getAddedSubList());
                }
            }
        });

        setAlgorithm(new Algorithm<TreesBlock, TreesBlock>() {
            public void compute(TaxaBlock taxaBlock, TreesBlock original, TreesBlock modified) {
                modified.getTrees().clear();

                final ArrayList<ITree> list = new ArrayList<>();
                if (enabledData.size() == 0)
                    list.addAll(original.getTrees());
                else
                    list.addAll(enabledData);
                list.removeAll(disabledData);

                for (ITree tree : list) {
                    if (!getDisabledData().contains(tree) && original.getTrees().contains(tree)) {
                        modified.getTrees().add(tree);
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
    public ObservableList<ITree> getEnabledData() {
        return enabledData;
    }

    /**
     * gets disabledData taxa
     *
     * @return disabledData
     */
    public ObservableList<ITree> getDisabledData() {
        return disabledData;
    }
}
