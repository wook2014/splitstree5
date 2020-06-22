/*
 * TaxaFilter.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.core.algorithms.filters;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import jloda.util.Basic;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTaxa;
import splitstree5.core.algorithms.interfaces.IToTaxa;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TraitsBlock;
import splitstree5.core.misc.Taxon;
import splitstree5.core.workflow.UpdateState;
import splitstree5.gui.algorithmtab.AlgorithmPane;
import splitstree5.gui.algorithmtab.taxafilterview.TaxaFilterPane;

import java.util.*;

/**
 * taxa filter
 * Daniel Huson, 12/31/16.
 */
public class TaxaFilter extends Algorithm<TaxaBlock, TaxaBlock> implements IFromTaxa, IToTaxa, IFilter {
    private final ObjectProperty<String[]> optionEnabledTaxa = new SimpleObjectProperty<>(new String[0]);
    private final ObjectProperty<String[]> optionDisabledTaxa = new SimpleObjectProperty<>(new String[0]);

    @Override
    public String getToolTip(String optionName) {
        switch (optionName) {
            case "EnabledTaxa":
                return "List of taxa currently enabled";
            case "DisabledTaxa":
                return "List of taxa currently disabled";
            default:
                return optionName;
        }
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock ignored, TaxaBlock parent, TaxaBlock child) {
        final Map<String, String> name2displayLabel = new HashMap<>();
        for (int t = 1; t <= child.getNtax(); t++) {
            final Taxon taxon = child.get(t);
            name2displayLabel.put(taxon.getName(), taxon.getDisplayLabel());
        }
        child.getTaxa().clear();

        if (numberEnabledTaxa() == 0 && numberDisabledTaxa() == 0) // nothing has been explicitly set, copy everything
        {
            child.getTaxa().setAll(parent.getTaxa());
        } else {
            final Set<String> disabled = new HashSet<>(Arrays.asList(getOptionDisabledTaxa()));
            for (String name : getOptionEnabledTaxa()) {
                if (!disabled.contains(name)) {
                    child.addTaxaByNames(Collections.singleton(name));
                    if (parent.get(name).getDisplayLabel() != null)
                        child.get(name).setDisplayLabel(parent.get(name).getDisplayLabel());
                    else
                        child.get(name).setDisplayLabel(name2displayLabel.get(name));
                }
            }
        }

        final TraitsBlock parentTraits = parent.getTraitsBlock();
        final TraitsBlock childTraits = child.getTraitsBlock();

        if (parentTraits != null && childTraits != null) {
            childTraits.copySubset(parent, parentTraits, child.getTaxa());
            if (childTraits.getDataNode() != null) {
                Platform.runLater(() ->
                {
                    childTraits.getDataNode().setState(UpdateState.INVALID); // need to set to invalid for change
                    childTraits.getDataNode().setState(UpdateState.VALID);
                });
            }
        }

        if (numberEnabledTaxa() == 0 && numberDisabledTaxa() == 0)
            setShortDescription(Basic.fromCamelCase(Basic.getShortName(this.getClass())));
        else if (numberDisabledTaxa() == 0)
            setShortDescription("using all " + numberEnabledTaxa() + " taxa");
        else
            setShortDescription("using " + numberEnabledTaxa() + " of " + (parent.getNtax() + " taxa"));

    }

    @Override
    public void clear() {
        super.clear();
        setOptionEnabledTaxa(new String[0]);
        setOptionDisabledTaxa(new String[0]);
    }

    public int numberEnabledTaxa() {
        return getOptionEnabledTaxa().length;
    }

    public int numberDisabledTaxa() {
        return getOptionDisabledTaxa().length;
    }

    public String[] getOptionEnabledTaxa() {
        return optionEnabledTaxa.get();
    }

    public ObjectProperty<String[]> optionEnabledTaxaProperty() {
        return optionEnabledTaxa;
    }

    public void setOptionEnabledTaxa(String[] optionEnabledTaxa) {
        this.optionEnabledTaxa.set(optionEnabledTaxa);
    }

    public String[] getOptionDisabledTaxa() {
        return optionDisabledTaxa.get();
    }

    public ObjectProperty<String[]> optionDisabledTaxaProperty() {
        return optionDisabledTaxa;
    }

    public void setOptionDisabledTaxa(String[] optionDisabledTaxa) {
        this.optionDisabledTaxa.set(optionDisabledTaxa);
    }

    public AlgorithmPane getAlgorithmPane() {
        return new TaxaFilterPane(this);
    }

    @Override
    public boolean isActive() {
        return numberDisabledTaxa() > 0;
    }
}
