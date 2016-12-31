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
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.Taxon;

import java.util.ArrayList;

/**
 * taxon filter
 * Created by huson on 12/12/16.
 */
public class TaxaFilter extends AConnector<TaxaBlock, TaxaBlock> {
    private final ArrayList<Taxon> enabled = new ArrayList<>(); // these should be placed inside the algorithm?
    private final ArrayList<Taxon> disabled = new ArrayList<>();

    /**
     * /**
     * constructor
     *
     * @param parent
     * @param child
     */
    public TaxaFilter(ADataNode<TaxaBlock> parent, ADataNode<TaxaBlock> child) {
        super(parent.getDataBlock(), parent, child);

        enabled.addAll(parent.getDataBlock().getTaxa());
        parent.getDataBlock().getTaxa().addListener(new ListChangeListener<Taxon>() {
            @Override
            public void onChanged(Change<? extends Taxon> c) {
                while (c.next()) {
                    if (c.getRemovedSize() > 0)
                        enabled.removeAll(c.getRemoved());
                    if (c.getAddedSize() > 0)
                        enabled.addAll(c.getAddedSubList());
                }
            }
        });

        setAlgorithm(new Algorithm<TaxaBlock, TaxaBlock>("TaxaFilter") {
            public void compute(ProgressListener progressListener, TaxaBlock ignored, TaxaBlock originalTaxa, TaxaBlock modifiedTaxa) {
                modifiedTaxa.getTaxa().clear();

                final ArrayList<Taxon> list = new ArrayList<>();
                if (enabled.size() == 0)
                    list.addAll(originalTaxa.getTaxa());
                else
                    list.addAll(enabled);
                list.removeAll(disabled);

                for (Taxon taxon : list) {
                    if (!TaxaFilter.this.getDisabled().contains(taxon) && originalTaxa.getTaxa().contains(taxon)) {
                        modifiedTaxa.getTaxa().add(taxon);
                    }
                }
            }
        });
    }

    /**
     * get the set of enabled data.
     *
     * @return list of explicitly enabled taxa
     */
    public ArrayList<Taxon> getEnabled() {
        return enabled;
    }

    /**
     * gets disabled taxa
     *
     * @return disabled
     */
    public ArrayList<Taxon> getDisabled() {
        return disabled;
    }
}
