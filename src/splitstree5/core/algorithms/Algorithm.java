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
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.datablocks.ADataBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.Named;

/**
 * A method
 * Created by huson on 12/21/16.
 */
abstract public class Algorithm<P extends ADataBlock, C extends ADataBlock> extends Named {
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
    abstract public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, P parent, C child) throws InterruptedException, CanceledException;

    public boolean isDisabled() {
        return disabled.get();
    }

    public BooleanProperty disabledProperty() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled.set(disabled);
    }
}
