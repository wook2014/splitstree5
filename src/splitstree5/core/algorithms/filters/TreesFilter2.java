/*
 * TreesFilter2.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.core.algorithms.filters;

import javafx.beans.property.*;
import jloda.graph.Edge;
import jloda.phylo.PhyloTree;
import jloda.util.CanceledException;
import jloda.util.progress.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;

import java.util.Arrays;
import java.util.List;

/**
 * additional tree filtering
 * Daniel Huson, 1/2019
 */
public class TreesFilter2 extends Algorithm<TreesBlock, TreesBlock> implements IFromTrees, IToTrees, IFilter {
    private final BooleanProperty optionRequireAllTaxa = new SimpleBooleanProperty(false);
    private final IntegerProperty optionMinNumberOfTaxa = new SimpleIntegerProperty(1);
    private final DoubleProperty optionMinTotalTreeLength = new SimpleDoubleProperty(0);
    private final DoubleProperty optionMinEdgeLength = new SimpleDoubleProperty(0);
    private final BooleanProperty optionUniformEdgeLengths = new SimpleBooleanProperty(false);


    public List<String> listOptions() {
        return Arrays.asList("RequireAllTaxa", "MinNumberOfTaxa", "MinTotalTreeLength", "MinEdgeLength", "UniformEdgeLengths");
    }

    @Override
    public String getToolTip(String optionName) {
        switch (optionName) {
            case "RequireAllTaxa":
                return "Keep only trees that have the full set of taxa";
            case "MinNumberOfTaxa":
                return "Keep only trees that have at least this number of taxa";
            case "MinTotalTreeLength":
                return "Keep only trees that have at least this total length";
            case "MinEdgeLength":
                return "Keep only edges that have this minimum length";
            case "UniformEdgeLengths":
                return "Change all edge weights to 1";
            default:
                return optionName;
        }
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, TreesBlock parent, TreesBlock child) throws CanceledException {
        if (!isActive()) {
            child.getTrees().addAll(parent.getTrees());
            child.setPartial(parent.isPartial());
        } else {
            boolean isPartial = false;
            for (int t = 1; t <= parent.getNTrees(); t++) {
                PhyloTree tree = parent.getTree(t);

                if (isOptionRequireAllTaxa() && tree.getNumberOfTaxa() < taxaBlock.getNtax())
                    continue;
                if (tree.getNumberOfTaxa() < getOptionMinNumberOfTaxa())
                    continue;
                if (getOptionMinTotalTreeLength() > 0) {
                    final var treeLength = tree.computeTotalWeight();
                    if (treeLength < getOptionMinTotalTreeLength())
                        continue;
                }
                boolean isCopy = false;
                if (getOptionMinEdgeLength() > 0) {
                    tree = new PhyloTree(tree);
                    if (tree.contractShortEdges(getOptionMinEdgeLength()))
                        isCopy = true;
                    else
                        tree = parent.getTree(t); // nothing changed, use original
                }
                if (isOptionUniformEdgeLengths()) {
                    if (!isCopy)
                        tree = new PhyloTree(tree);

                    if (!makeEdgesUnitWeight(tree))
                        tree = parent.getTree(t); // nothing changed, use original
                }
                child.getTrees().add(tree);
                if (!isPartial && tree.getNumberOfTaxa() < taxaBlock.getNtax()) {
                    isPartial = true;
                }
            }
            child.setPartial(isPartial);
        }
        child.setRooted(parent.isRooted());

        if (child.getNTrees() == parent.getNTrees())
            setShortDescription("using all " + parent.size() + " trees");
        else
            setShortDescription("using " + child.size() + " of " + parent.size() + " trees");
    }

    @Override
    public void clear() {
    }

    @Override
    public boolean isActive() {
        return isOptionRequireAllTaxa() || getOptionMinNumberOfTaxa() > 1 || getOptionMinEdgeLength() > 0 || isOptionUniformEdgeLengths();
    }

    public boolean isOptionRequireAllTaxa() {
        return optionRequireAllTaxa.get();
    }

    public BooleanProperty optionRequireAllTaxaProperty() {
        return optionRequireAllTaxa;
    }

    public void setOptionRequireAllTaxa(boolean optionRequireAllTaxa) {
        this.optionRequireAllTaxa.set(optionRequireAllTaxa);
    }

    public int getOptionMinNumberOfTaxa() {
        return optionMinNumberOfTaxa.get();
    }

    public IntegerProperty optionMinNumberOfTaxaProperty() {
        return optionMinNumberOfTaxa;
    }

    public void setOptionMinNumberOfTaxa(int optionMinNumberOfTaxa) {
        this.optionMinNumberOfTaxa.set(optionMinNumberOfTaxa);
    }

    public double getOptionMinTotalTreeLength() {
        return optionMinTotalTreeLength.get();
    }

    public DoubleProperty optionMinTotalTreeLengthProperty() {
        return optionMinTotalTreeLength;
    }

    public void setOptionMinTotalTreeLength(double optionMinTotalTreeLength) {
        this.optionMinTotalTreeLength.set(optionMinTotalTreeLength);
    }


    public double getOptionMinEdgeLength() {
        return optionMinEdgeLength.get();
    }

    public DoubleProperty optionMinEdgeLengthProperty() {
        return optionMinEdgeLength;
    }

    public void setOptionMinEdgeLength(double optionMinEdgeLength) {
        this.optionMinEdgeLength.set(optionMinEdgeLength);
    }

    public boolean isOptionUniformEdgeLengths() {
        return optionUniformEdgeLengths.get();
    }

    public BooleanProperty optionUniformEdgeLengthsProperty() {
        return optionUniformEdgeLengths;
    }

    public void setOptionUniformEdgeLengths(boolean optionUniformEdgeLengths) {
        this.optionUniformEdgeLengths.set(optionUniformEdgeLengths);
    }

    /**
     * give all adjacentEdges unit weight
     */
    public static boolean makeEdgesUnitWeight(PhyloTree tree) {
        boolean changed = false;
        for (Edge e : tree.edges()) {
            if (tree.getWeight(e) != 1) {
                tree.setWeight(e, 1.0);
                changed = true;
            }
        }
        return changed;
    }


}
