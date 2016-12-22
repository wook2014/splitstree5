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
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.Named;

/**
 * A method
 * Created by huson on 12/21/16.
 */
abstract public class Algorithm<P extends DataBlock, C extends DataBlock> extends Named {
    private TaxaBlock taxa;
    private P parent;
    private C child;

    private final BooleanProperty disabled = new SimpleBooleanProperty(true);

    /**
     * constructor
     */
    public Algorithm() {
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
     * perform the computation
     */
    abstract public void compute(TaxaBlock taxaBlock, P parent, C child) throws InterruptedException;

    public TaxaBlock getTaxa() {
        return taxa;
    }

    public void setTaxa(TaxaBlock taxa) {
        this.taxa = taxa;
    }

    public void setParent(P parent) {
        this.parent = parent;
    }

    public P getParent() {
        return parent;
    }

    public C getChild() {
        return child;
    }

    public void setChild(C child) {
        this.child = child;
    }

    public boolean getDisabled() {
        return disabled.get();
    }

    public BooleanProperty disabledProperty() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled.set(disabled);
    }
}
