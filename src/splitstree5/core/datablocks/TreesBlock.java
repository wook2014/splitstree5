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
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import jloda.util.NotOwnerException;
import splitstree4.core.TaxaSet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A trees block
 * Created by huson on 12/21/16.
 */
public class TreesBlock extends ADataBlock {
    private final ObservableList<PhyloTree> trees;
    private boolean partial = false; // are partial trees present?
    private boolean rooted = false; // are the trees explicitly rooted?

    // todo need for treeSelector, use od delete
    final private Map<String, String> translate = new HashMap<>();

    public TreesBlock() {
        trees = FXCollections.observableArrayList();
        // for translate update
        /*trees.addListener(new ListChangeListener<PhyloTree>() {
            @Override
            public void onChanged(Change<? extends PhyloTree> c) {
                if(c.wasAdded()) updateTranslate((List<PhyloTree>) c.getAddedSubList());
            }
        });*/
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
        translate.clear();
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

    private void updateTranslate(List<PhyloTree> addedTress){

        //todo update translate field according to addTree function from st4
        //todo get parent taxa, st4 : taxa is got as an input of addTree function

        /*for(PhyloTree tree : addedTress){

            /*if (translate.size() == 0) // need to setup translation table
            {
                for (Node v = tree.getFirstNode(); v != null; v = tree.getNextNode(v)) {
                    String nodelabel = tree.getLabel(v);
                    if (nodelabel != null) {
                        int t = taxa.indexOf(nodelabel);
                        if (t > 0)
                            // translate.put(taxa.getLabel(t), nodelabel);
                            translate.put(nodelabel, taxa.getLabel(t));
                        else if (!getPartial())
                            throw new SplitsException("Invalid node label: " + nodelabel);
                    }
                }
            }
            checkTranslation(tree, this.translate);

            ntrees++;
            trees.setSize(ntrees);
            trees.add(ntrees - 1, tree);

            // make sure tree gets unique name
            names.setSize(ntrees);
            if (name.length() == 0)
                name = "tree";
            if (names.indexOf(name) != -1) {
                int count = 1;
                while (names.indexOf(name + "_" + count) != -1)
                    count++;
                name += "_" + count;
            }
            names.add(ntrees - 1, name);
            taxasets.add(ntrees - 1, (this.getTaxaInTree(taxa, ntrees)));
        }*/
    }

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

    /**
     * Returns the set of taxa associated with a given node-label
     *
     * @param nlab the node label
     * @return the set of taxa mapped to the given node label
     */
    public TaxaSet getTaxaForLabel(TaxaBlock taxa, String nlab) {
        TaxaSet result = new TaxaSet();
        try {
            if (nlab != null)
                for (int t = 1; t <= taxa.getNtax(); t++) {
                    if (translate.get(nlab).equals(taxa.getLabel(t))) {
                        result.set(t);
                    }
                }
        } catch (Exception ex) {
            Basic.caught(ex);
        }
        return result;
    }

    public Map<String, String> getTranslate(){
        return this.translate;
    }
}
