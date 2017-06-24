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

package splitstree5.core.algorithms;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import jloda.util.Basic;
import jloda.util.ProgressListener;
import splitstree5.core.datablocks.ADataBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.utils.Option;
import splitstree5.utils.OptionableBase;
import splitstree5.utils.OptionsAccessor;

/**
 * An algorithm
 * Created by huson on 12/21/16.
 */
abstract public class Algorithm<P extends ADataBlock, C extends ADataBlock> extends OptionableBase {
    private final BooleanProperty disabled = new SimpleBooleanProperty(true);

    /**
     * constructor
     */
    public Algorithm() {
        setName(Basic.getShortName(getClass()));
    }

    /**
     * named constructor
     *
     * @param name
     */
    public Algorithm(String name) {
        setName(name);
    }

    /**
     * named constructor
     *
     * @param name
     */
    public Algorithm(String name, String shortDescription) {
        super(name, shortDescription);
    }

    /**
     * perform the computation
     */
    abstract public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, P parent, C child) throws Exception;

    /**
     * perform the computation for datablocks of unknown type
     *
     * @param progressListener
     * @param taxaBlock
     * @param parent
     * @param child
     * @throws Exception
     */
    public void compute0(ProgressListener progressListener, TaxaBlock taxaBlock, ADataBlock parent, ADataBlock child) throws Exception {
        compute(progressListener, taxaBlock, (P) parent, (C) child);
    }

    /**
     * clear temporary data
     */
    public void clear() {
        super.clear();
    }

    public boolean isDisabled() {
        return disabled.get();
    }

    public BooleanProperty disabledProperty() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled.set(disabled);
    }

    /**
     * determines whether applicable
     *
     * @param taxaBlock
     * @param parent
     * @param child
     */
    public boolean isApplicable(TaxaBlock taxaBlock, P parent, C child) {
        return true;
    }

    /**
     * gets the name of this algorithm
     *
     * @return name
     */
    public String toString() {
        return getName();
    }

    /**
     * gets the citation for the algorithm
     *
     * @return
     */
    public String getCitation() {
        return "citation?";
    }

    /**
     * reports the parameters used by this algorithm
     *
     * @return parameters
     */
    final public String getParameters() {
        final StringBuilder buf = new StringBuilder();
        for (Option option : OptionsAccessor.getAllOptions(this)) {
            if (buf.length() > 0)
                buf.append(", ");
            buf.append(option.getName()).append(" = ").append(option.getValue().toString());
        }
        if (buf.length() == 0)
            return "none";
        else
            return buf.toString();
    }
}
