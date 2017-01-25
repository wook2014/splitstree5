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
import javafx.collections.ObservableMap;
import jloda.util.Basic;
import splitstree4.core.SplitsException;
import splitstree4.core.TaxaSet;
import splitstree5.core.misc.Taxon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A taxa block
 * Created by huson on 12/21/16.
 */
public class TaxaBlock extends ADataBlock {
    private final ObservableList<Taxon> taxa;
    private final ObservableMap<Taxon, Integer> taxon2index;
    private final ObservableMap<String, Taxon> name2taxon;

    /**
     * constructor
     */
    public TaxaBlock() {
        taxa = FXCollections.observableArrayList();
        taxon2index = FXCollections.observableHashMap();
        name2taxon = FXCollections.observableHashMap();
        taxa.addListener((ListChangeListener<Taxon>) c -> {
            taxon2index.clear();
            name2taxon.clear();
            for (int t = 1; t <= getNtax(); t++) {
                final Taxon taxon = taxa.get(t - 1);
                taxon2index.put(taxon, t - 1);
                name2taxon.put(taxon.getName(), taxon);
            }
        });
    }

    public TaxaBlock(String name) {
        this();
        setName(name);
    }

    @Override
    public int size() {
        return taxa.size();
    }

    @Override
    public void clear() {
        taxa.clear();
        taxon2index.clear();
        name2taxon.clear();
        setShortDescription("");
    }

    public int getNtax() {
        return taxa.size();
    }

    public Taxon get(String taxonName) {
        return name2taxon.get(taxonName);
    }

    public Taxon get(int t) {
        return taxa.get(t - 1);
    }

    public String getLabel(int t) {
        return taxa.get(t - 1).getName();
    }

    public int indexOf(Taxon taxon) {
        if (taxon2index.containsKey(taxon))
            return taxon2index.get(taxon) + 1;
        else
            return -1;
    }

    public ObservableList<Taxon> getTaxa() {
        return taxa;
    }

    public void addTaxaByNames(Collection<String> taxonNames) {
        for (String name : taxonNames) {
            if (!name2taxon.keySet().contains(name))
                getTaxa().add(new Taxon(name));
        }
    }

    /**
     * computes index map for modified block
     *
     * @param modifiedTaxaBlock
     * @return modified map
     */
    public Map<Integer, Integer> computeIndexMap(TaxaBlock modifiedTaxaBlock) {
        HashMap<Integer, Integer> map = new HashMap<>();

        for (int t = 1; t <= getNtax(); t++) {
            final Taxon taxon = get(t);
            if (modifiedTaxaBlock.getTaxa().contains(taxon)) {
                map.put(t, modifiedTaxaBlock.indexOf(taxon));
            }
        }
        return map;
    }

    /**
     * adds a taxon. Throws an exception if name already present
     *
     * @param taxon
     * @throws IOException taxon name already present
     */
    public void add(Taxon taxon) throws IOException {
        if (taxa.contains(taxon))
            throw new IOException("Duplicate taxon name: " + taxon.getName());
        taxa.add(taxon);
    }

    /**
     * get all taxon labels
     *
     * @return labels
     */
    public ArrayList<String> getLabels() {
        final ArrayList<String> labels = new ArrayList<>(taxa.size());
        for (Taxon taxon : taxa) {
            labels.add(taxon.getName());
        }
        return labels;
    }

    // todo: replace classes from splitstree4 (need it for the tree selector)
    TaxaBlock originalTaxa;
    TaxaSet hiddenTaxa;

    /**
     * Returns the set of all taxa
     *
     * @return the set 1,2..,ntax
     */
    public TaxaSet getTaxaSet() {
        TaxaSet all = new TaxaSet();
        all.set(1, getNtax());
        return all;
    }


    /**
     * additionally hide more taxa. This is used in the presence of partial trees.
     * Note that these numbers are given with respect to the current taxa set,
     * not the original one!
     *
     * @param additionalHidden
     */
    public void hideAdditionalTaxa(TaxaSet additionalHidden) throws SplitsException {
        if (originalTaxa == null)
            originalTaxa = (TaxaBlock) this.clone(); // make a copy

        TaxaSet additionalO = new TaxaSet();

        int ntax = getOriginalTaxa().getNtax();
        if (additionalHidden != null) {
            for (int oID = 1; oID <= ntax; oID++) {
                int nID = indexOf(originalTaxa.get(oID));
                if (nID != -1 && additionalHidden.get(nID)) {
                    additionalO.set(oID);
                }
            }
        }
        if (hiddenTaxa != null && hiddenTaxa.intersects(additionalO))
            throw new SplitsException("hidden <" + hiddenTaxa + "> and additional <"
                    + additionalO + "> intersect");

        int count = 0;
        for (int oID = 1; oID <= ntax; oID++) {
            if ((hiddenTaxa == null || !hiddenTaxa.get(oID))
                    && (additionalHidden == null || !additionalO.get(oID))) {
                ++count;
            }
        }
        //setNtax(count);
        count = 0;
        for (int oID = 1; oID <= ntax; oID++) {
            {
                if ((hiddenTaxa == null || !hiddenTaxa.get(oID))
                        && (additionalHidden == null || !additionalO.get(oID))) {
                    if (getOriginalTaxa() != null) {
                        //???
                        //setLabel(++count, getOriginalTaxa().getLabel(oID));
                        //setInfo(count, getOriginalTaxa().getInfo(oID));

                    }
                }
            }
        }
    }

    public TaxaBlock getOriginalTaxa() {
        return originalTaxa;
    }

    public Object clone()
    {
        TaxaBlock result = new TaxaBlock();

        try {
            //result.setNTax(getNtax());
            for (int t = 1; t <= getNtax(); t++) {
                //result.setLabel(t, getLabel(t));
                //result.setInfo(t, getInfo(t));
                result.add(get(t));
            }
        } catch (Exception ex) {
            Basic.caught(ex);
        }
        return result;
    }
}
