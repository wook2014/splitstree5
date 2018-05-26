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

package splitstree5.core.datablocks;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jloda.util.Basic;
import splitstree5.core.algorithms.interfaces.IFromSplits;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Compatibility;
import splitstree5.io.nexus.SplitsNexusFormat;
import splitstree5.utils.SplitsUtilities;

import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A splits block
 * Daniel Huson, 12/21/16.
 */
public class SplitsBlock extends DataBlock {
    public static final String BLOCK_NAME = "SPLITS";


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
        super(BLOCK_NAME);
        splits = FXCollections.observableArrayList();
        format = new SplitsNexusFormat();
    }

    /**
     * named constructor
     *
     * @param name
     */
    public SplitsBlock(String name) {
        this();
        setName(name);
        getSplits().addListener((InvalidationListener) observable -> setShortDescription(getInfo()));
    }

    /**
     * shallow copy
     *
     * @param that
     */
    public void copy(SplitsBlock that) {
        clear();
        for (ASplit split : that.getSplits())
            splits.add(split.clone());
        compatibility = that.getCompatibility();
        fit = that.getFit();
        threshold = that.getThreshold();
        partial = that.isPartial();
        cycle = that.getCycle().clone();
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

    public ObservableList<ASplit> getSplits() {
        return splits;
    }

    @Override
    public int size() {
        return splits.size();
    }

    public int getNsplits() {
        return splits.size();
    }

    public Iterable<ASplit> splits() {
        return () -> new Iterator<ASplit>() {
            int i = 0; // 0-based because we do not use get(i) but rather splits.get(i)

            @Override
            public boolean hasNext() {
                return i < splits.size();
            }

            @Override
            public ASplit next() {
                if (i >= splits.size())
                    throw new NoSuchElementException();
                return splits.get(i++);
            }
        };
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
        if (cycle == null && getNsplits() > 0) {
            final BitSet taxa = new BitSet();
            for (ASplit split : splits) {
                taxa.or(split.getA());
                taxa.or(split.getB());
            }
            cycle = SplitsUtilities.computeCycle(taxa.cardinality(), splits);
        }
        return cycle;
    }

    /**
     * get the i-th split, 1-based
     *
     * @param i
     * @return i-th split
     */
    public ASplit get(int i) {
        return splits.get(i - 1);
    }

    /**
     * gets  all taxa that are included in one specified side of one split and also one specified side of the other split.
     *
     * @param splitP the index of split "P", 1-based
     * @param sideP  the "side" of the split P that should be considered
     * @param splitQ the index of the other split "Q", 1-based
     * @param sideQ  the "side" of the split Q that should be considered
     */
    public BitSet intersect2(int splitP, boolean sideP, int splitQ, boolean sideQ) {
        final BitSet result = new BitSet();
        result.or(sideP ? get(splitP).getA() : get(splitP).getB());
        result.and(sideQ ? get(splitQ).getA() : get(splitQ).getB());
        return result;
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
        return getNsplits() + " splits" + (compatibility != Compatibility.unknown ? ", " + Basic.fromCamelCase(compatibility.toString()).toLowerCase() : "");
    }
}