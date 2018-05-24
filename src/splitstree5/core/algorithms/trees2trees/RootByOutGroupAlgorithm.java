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

package splitstree5.core.algorithms.trees2trees;


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

import java.util.ArrayList;
import java.util.BitSet;

/**
 * tree rerooting by outgroup
 * Daniel Huson, 5.2018
 */
public class RootByOutGroupAlgorithm extends Algorithm<TreesBlock, TreesBlock> implements IFromTrees, IToTrees, IFilter {

    private final ArrayList<Taxon> inGroupTaxa = new ArrayList<>();
    private final ArrayList<Taxon> outGroupTaxa = new ArrayList<>();

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxa, TreesBlock parent, TreesBlock child) throws InterruptedException, CanceledException {
        if (inGroupTaxa.size() == 0 || outGroupTaxa.size() == 0) // nothing has been explicitly set, copy everything
        {
            child.getTrees().setAll(parent.getTrees());
        } else { // reroot using outgroup
            // System.err.println("Outgroup taxa: "+Basic.toString(outGroupTaxa," "));

            final BitSet outGroupTaxonSet = new BitSet();
            for (Taxon taxon : outGroupTaxa) {
                outGroupTaxonSet.set(taxa.indexOf(taxon));
            }

            child.getTrees().clear();

            for (PhyloTree orig : parent.getTrees()) {
                final PhyloTree tree = new PhyloTree();
                tree.copy(orig);
                RerootingUtils.rerootByOutGroup(tree, outGroupTaxonSet);
                child.getTrees().add(tree);
            }
        }

        if (inGroupTaxa.size() == 0 || outGroupTaxa.size() == 0)
            setShortDescription(Basic.fromCamelCase(Basic.getShortName(this.getClass())));
        else
            setShortDescription("using " + outGroupTaxa.size() + " of " + (taxa.getNtax() + " taxa to root trees"));

    }

    @Override
    public void clear() {
        super.clear();
        inGroupTaxa.clear();
        outGroupTaxa.clear();
    }

    public ArrayList<Taxon> getInGroupTaxa() {
        return inGroupTaxa;
    }

    public ArrayList<Taxon> getOutGroupTaxa() {
        return outGroupTaxa;
    }

    public AlgorithmPane getAlgorithmPane() {
        return new RootByOutGroupPane(this);
    }

    @Override
    public boolean isActive() {
        return outGroupTaxa.size() > 0;
    }

}
