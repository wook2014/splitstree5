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

import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTaxa;
import splitstree5.core.algorithms.interfaces.IToTaxa;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.Taxon;
import splitstree5.gui.algorithmtab.AlgorithmPane;
import splitstree5.gui.algorithmtab.taxafilterview.TaxaFilterPane;

import java.io.IOException;
import java.util.ArrayList;

/**
 * taxa filter
 * Created by huson on 12/31/16.
 */
public class TaxaFilter extends Algorithm<TaxaBlock, TaxaBlock> implements IFromTaxa, IToTaxa {

    private final ArrayList<Taxon> enabledTaxa = new ArrayList<>();
    private final ArrayList<Taxon> disabledTaxa = new ArrayList<>();

    @Override
    public void compute(ProgressListener progress, TaxaBlock ignored, TaxaBlock parent, TaxaBlock child) throws InterruptedException, CanceledException {
        child.getTaxa().clear();

        if (enabledTaxa.size() == 0 && disabledTaxa.size() == 0) // nothing has been explicitly set, copy everything
            child.getTaxa().setAll(parent.getTaxa());
        else {
            for (Taxon taxon : enabledTaxa) {
                if (!disabledTaxa.contains(taxon)) {
                    child.getTaxa().add(taxon);
                }
            }
        }
        if (enabledTaxa.size() == 0 && disabledTaxa.size() == 0)
            setShortDescription(Basic.fromCamelCase(Basic.getShortName(this.getClass())));
        else if (disabledTaxa.size() == 0)
            setShortDescription("Enabled: " + enabledTaxa.size());
        else
            setShortDescription("Enabled: " + enabledTaxa.size() + " (of " + (enabledTaxa.size() + disabledTaxa.size() + ")"));
    }

    @Override
    public void clear() {
        super.clear();
        enabledTaxa.clear();
        disabledTaxa.clear();
    }

    public ArrayList<Taxon> getEnabledTaxa() {
        return enabledTaxa;
    }

    public ArrayList<Taxon> getDisabledTaxa() {
        return disabledTaxa;
    }

    public AlgorithmPane getControl() {
        try {
            return new TaxaFilterPane(this);
        } catch (IOException e) {
            Basic.caught(e);
            return null;
        }
    }


}
