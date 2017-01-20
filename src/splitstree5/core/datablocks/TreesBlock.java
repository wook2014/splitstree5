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

package splitstree5.core.datablocks;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import jloda.util.NotOwnerException;
import splitstree4.core.TaxaSet;

import java.util.Iterator;

/**
 * A trees block
 * Created by huson on 12/21/16.
 */
public class TreesBlock extends ADataBlock {
    private final ObservableList<PhyloTree> trees;
    private boolean partial = false; // are partial trees present?
    private boolean rooted = false; // are the trees explicitly rooted?

    public TreesBlock() {
        trees = FXCollections.observableArrayList();
    }

    public TreesBlock(String name) {
        this();
        setName(name);
    }

    @Override
    public int size() {
        return trees.size();
    }

    @Override
    public void clear() {
        trees.clear();
        partial = false;
        rooted = false;
        setShortDescription("");
    }

    /**
     * access the trees
     *
     * @return trees
     */
    public ObservableList<PhyloTree> getTrees() {
        return trees;
    }

    public int getNTrees() {
        return trees.size();
    }

    public String getShortDescription() {
        return "Number of trees: " + getTrees().size();
    }

    public boolean isPartial() {
        return partial;
    }

    public void setPartial(boolean partial) {
        this.partial = partial;
    }

    public boolean isRooted() {
        return rooted;
    }

    public void setRooted(boolean rooted) {
        this.rooted = rooted;
    }

    // todo: replace classes from splitstree4 (need it for the tree selector)

    /**
     * returns the set of taxa contained in this tree.
     *
     * @param taxa  original taxa
     * @param which tree
     * @return set of taxa not present in this tree. Uses original numbering
     */
    public TaxaSet getTaxaInTree(TaxaBlock taxa, int which) {
        PhyloTree tree = getTrees().get(which);

        TaxaSet seen = new TaxaSet();
        Iterator it = tree.nodeIterator();
        while (it.hasNext()) {
            try {
                String nodeLabel = tree.getLabel((Node) it.next());
                if (nodeLabel != null) {
                    //todo: need translate map in splitstree5?
                    String taxonLabel = tree.getLabel((Node) it.next());//translate.get(nodeLabel);
                    /*if (taxa.indexOf(taxonLabel) == -1) {
                        System.err.println("can't find " + nodeLabel + " in ");
                        Taxa.show("taxa", taxa);
                        if (taxa.getOriginalTaxa() != null)
                            Taxa.show("orig", taxa.getOriginalTaxa());
                    } else
                        seen.set(taxa.indexOf(taxonLabel));*/
                        seen.set(taxa.indexOf(taxa.get(taxonLabel)));
                }
            } catch (NotOwnerException ex) {
                Basic.caught(ex);
            }
        }
        return seen;
    }
}
