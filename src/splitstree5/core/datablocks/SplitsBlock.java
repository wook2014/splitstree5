/*
 *  Copyright (C) 2017 Daniel H. Huson
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

package splitstree5.core.datablocks;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jloda.util.Basic;
import splitstree5.core.algorithms.interfaces.IFromSplits;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Compatibility;
import splitstree5.core.misc.SplitsUtilities;

import java.util.BitSet;

/**
 * A splits block
 * Created by huson on 12/21/16.
 */
public class SplitsBlock extends ADataBlock {
    private final ObservableList<ASplit> splits;

    private Compatibility compatibility = Compatibility.unknown;
    private float fit = -1;

    private float threshold = 0; // todo: this belongs in SplitsFilter?

    private boolean partial = false;

    private int[] cycle;

    /**
     * default constructor
     */
    public SplitsBlock() {
        splits = FXCollections.observableArrayList();
    }

    /**
     * named constructor
     *
     * @param name
     */
    public SplitsBlock(String name) {
        this();
        setName(name);
    }

    /**
     * shallow copy
     *
     * @param that
     */
    public void copy(SplitsBlock that) {
        clear();
        splits.addAll(that.getSplits());
        compatibility = that.getCompatibility();
        fit = that.getFit();
        threshold = that.getThreshold();
        partial = that.isPartial();
        cycle = that.getCycle();
    }

    @Override
    public void clear() {
        super.clear();
        splits.clear();
        cycle = null;
        compatibility = Compatibility.unknown;
        fit = -1;
        threshold = 0;
        setShortDescription("");
    }

    @Override
    public int size() {
        return splits.size();
    }

    public int getNsplits() {
        return splits.size();
    }

    /**
     * access the splits
     *
     * @return splits
     */
    public ObservableList<ASplit> getSplits() {
        return splits;
    }

    public String getShortDescription() {
        return "Number of splits: " + getSplits().size();
    }

    public Compatibility getCompatibility() {
        return compatibility;
    }

    public void setCompatibility(Compatibility compatibility) {
        this.compatibility = compatibility;
    }

    public float getFit() {
        return fit;
    }

    public void setFit(float fit) {
        this.fit = fit;
    }

    public float getThreshold() {
        return threshold;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    public boolean isPartial() {
        return partial;
    }

    public void setPartial(boolean partial) {
        this.partial = partial;
    }

    public int[] getCycle() {
        return cycle;
    }

    /**
     * set the cycle (and normalize it)
     *
     * @param cycle
     */
    public void setCycle(int[] cycle) {
        if (cycle != null) {
            BitSet set = new BitSet();
            for (int i : cycle) {
                set.set(i);
            }
            if (set.cardinality() != cycle.length) {
                System.err.println("Internal error: setCycle() failed: wrong cardinality");
                cycle = null;
            } else {
                cycle = SplitsUtilities.normalizeCycle(cycle);
            }
        }
        this.cycle = cycle;
    }

    @Override
    public Class getFromInterface() {
        return IFromSplits.class;
    }

    @Override
    public Class getToInterface() {
        return IToSplits.class;
    }

    @Override
    public String getInfo() {
        return getNsplits() + " splits" + (compatibility != Compatibility.unknown ? ", " + Basic.fromCamelCase(compatibility.toString()) : "");
    }
}
