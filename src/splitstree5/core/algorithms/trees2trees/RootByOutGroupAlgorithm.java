/*
 *  RootByOutGroupAlgorithm.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.core.algorithms.trees2trees;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.filters.IFilter;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.Taxon;
import splitstree5.gui.algorithmtab.AlgorithmPane;
import splitstree5.gui.algorithmtab.rootbyoutgroup.RootByOutGroupPane;
import splitstree5.utils.RerootingUtils;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * tree rerooting by outgroup
 * Daniel Huson, 5.2018
 */
public class RootByOutGroupAlgorithm extends Algorithm<TreesBlock, TreesBlock> implements IFromTrees, IToTrees, IFilter {

    private final ObservableList<Taxon> optionInGroupTaxa = FXCollections.observableArrayList();
    private final ObservableList<Taxon> optionOutGroupTaxa = FXCollections.observableArrayList();

    @Override
    public List<String> listOptions() {
        return Arrays.asList("InGroupTaxa", "OutGroupTaxa");
    }

    @Override
    public String getToolTip(String optionName) {
        switch (optionName) {
            case "InGroupTaxa":
                return "List of taxa belonging to the in-group";
            case "GroupTaxa":
                return "List of taxa belonging to the out-group";
            default:
                return optionName;
        }
    }


    @Override
    public void compute(ProgressListener progress, TaxaBlock taxa, TreesBlock parent, TreesBlock child) throws InterruptedException, CanceledException {
        if (optionInGroupTaxa.size() == 0 || optionOutGroupTaxa.size() == 0) // nothing has been explicitly set, copy everything
        {
            child.getTrees().setAll(parent.getTrees());
        } else { // reroot using outgroup
            // System.err.println("Outgroup taxa: "+Basic.toString(outGroupTaxa," "));

            final BitSet outGroupTaxonSet = new BitSet();
            for (Taxon taxon : optionOutGroupTaxa) {
                outGroupTaxonSet.set(taxa.indexOf(taxon));
            }

            child.getTrees().clear();

            for (PhyloTree orig : parent.getTrees()) {
                if (orig.getNumberOfNodes() > 0) {
                    final PhyloTree tree = new PhyloTree();
                    tree.copy(orig);
                    if (tree.getRoot() == null) {
                        tree.setRoot(tree.getFirstNode());
                        tree.redirectEdgesAwayFromRoot();
                    }
                    RerootingUtils.rerootByOutGroup(tree, outGroupTaxonSet);
                    child.getTrees().add(tree);
                }
            }
            child.setRooted(true);
        }

        if (optionInGroupTaxa.size() == 0 || optionOutGroupTaxa.size() == 0)
            setShortDescription(Basic.fromCamelCase(Basic.getShortName(this.getClass())));
        else
            setShortDescription("using " + optionOutGroupTaxa.size() + " of " + (taxa.getNtax() + " for tree rooting"));

    }

    @Override
    public void clear() {
        super.clear();
        optionInGroupTaxa.clear();
        optionOutGroupTaxa.clear();
    }

    public ObservableList<Taxon> getOptionInGroupTaxa() {
        return optionInGroupTaxa;
    }

    public ObservableList<Taxon> getOptionOutGroupTaxa() {
        return optionOutGroupTaxa;
    }

    public AlgorithmPane getAlgorithmPane() {
        return new RootByOutGroupPane(this);
    }

    @Override
    public boolean isActive() {
        return optionOutGroupTaxa.size() > 0;
    }

}
