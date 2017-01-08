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
import jloda.util.Basic;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.Taxon;
import splitstree5.gui.connectorview.CustomizedControl;
import splitstree5.gui.customized.taxaview.TaxaFilterPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * taxon filter
 * Created by huson on 12/12/16.
 */
public class TaxaFilter extends AConnector<TaxaBlock, TaxaBlock> {
    private final ArrayList<Taxon> enabledTaxa = new ArrayList<>(); // these should be placed inside the algorithm?
    private final ArrayList<Taxon> disabledTaxa = new ArrayList<>();

    /**
     * /**
     * constructor
     *
     * @param parent
     * @param child
     */
    public TaxaFilter(ADataNode<TaxaBlock> parent, ADataNode<TaxaBlock> child) {
        super(parent.getDataBlock(), parent, child);

        enabledTaxa.addAll(parent.getDataBlock().getTaxa());
        parent.getDataBlock().getTaxa().addListener(new ListChangeListener<Taxon>() {
            @Override
            public void onChanged(Change<? extends Taxon> c) {
                while (c.next()) {
                    if (c.getRemovedSize() > 0)
                        enabledTaxa.removeAll(c.getRemoved());
                    if (c.getAddedSize() > 0)
                        enabledTaxa.addAll(c.getAddedSubList());
                }
            }
        });

        setAlgorithm(new Algorithm<TaxaBlock, TaxaBlock>("TaxaFilter") {
            public void compute(ProgressListener progressListener, TaxaBlock ignored, TaxaBlock originalTaxa, TaxaBlock modifiedTaxa) {
                modifiedTaxa.getTaxa().clear();

                final ArrayList<Taxon> list = new ArrayList<>();
                if (enabledTaxa.size() == 0)
                    list.addAll(originalTaxa.getTaxa());
                else
                    list.addAll(enabledTaxa);
                list.removeAll(disabledTaxa);

                for (Taxon taxon : list) {
                    if (!TaxaFilter.this.getDisabledTaxa().contains(taxon) && originalTaxa.getTaxa().contains(taxon)) {
                        modifiedTaxa.getTaxa().add(taxon);
                    }
                }
            }

            @Override
            public CustomizedControl getControl() {
                try {
                    return new TaxaFilterPane(TaxaFilter.this);
                } catch (IOException e) {
                    Basic.caught(e);
                    return null;
                }
            }
        });
    }


    /**
     * get the set of enabled data.
     *
     * @return list of explicitly enabled taxa
     */
    public List<Taxon> getEnabledTaxa() {
        return enabledTaxa;
    }

    /**
     * gets disabled taxa
     *
     * @return disabled
     */
    public List<Taxon> getDisabledTaxa() {
        return disabledTaxa;
    }
}
