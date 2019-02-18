/*
 *  Copyright (C) 2019 Daniel H. Huson
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
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import jloda.util.Basic;
import splitstree5.core.algorithms.interfaces.IFromTaxa;
import splitstree5.core.algorithms.interfaces.IToTaxa;
import splitstree5.core.misc.Taxon;

import java.io.IOException;
import java.util.*;

/**
 * A taxa block
 * Daniel Huson, 12/21/16.
 */
public class TaxaBlock extends DataBlock {
    public static final String BLOCK_NAME = "TAXA";

    private int ntax;
    private final ObservableList<Taxon> taxa;
    private final ObservableMap<Taxon, Integer> taxon2index;
    private final ObservableMap<String, Taxon> name2taxon;

    private final BooleanProperty hasTaxa = new SimpleBooleanProperty();

    private final ObjectProperty<TraitsBlock> traitsBlock = new SimpleObjectProperty<>();

    /**
     * constructor
     */
    public TaxaBlock() {
        super(BLOCK_NAME);
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
        taxa.addListener((InvalidationListener) observable -> {
            setShortDescription(getInfo());
            ntax = size();
        });

        hasTaxa.bind(Bindings.isNotEmpty(taxa));
    }

    /**
     * named constructor
     *
     * @param name
     */
    public TaxaBlock(String name) {
        this();
        setName(name);
    }

    @Override
    public void clear() {
        super.clear();
        taxa.clear();
        taxon2index.clear();
        name2taxon.clear();
    }

    /**
     * get size
     *
     * @return number of taxa
     */
    @Override
    public int size() {
        return taxa.size();
    }

    public ReadOnlyBooleanProperty hasTaxaProperty() {
        return hasTaxa;
    }

    /**
     * get number of taxa
     *
     * @return number of taxa
     */
    public int getNtax() {
        return ntax;
    }

    /**
     * Set the number of taxa. Note that any change to the list of taxa will reset this
     *
     * @param ntax
     */
    public void setNtax(int ntax) {
        this.ntax = ntax;
    }

    public Taxon get(String taxonName) {
        return name2taxon.get(taxonName);
    }

    /**
     * get taxon
     *
     * @param t range 1 to nTax
     * @return taxon
     */
    public Taxon get(int t) {
        if (t == 0)
            throw new IndexOutOfBoundsException("0");
        return taxa.get(t - 1);
    }

    /**
     * get taxon label
     *
     * @param t range 1 to nTax
     * @return taxon
     */
    public String getLabel(int t) {
        if (t == 0)
            throw new IndexOutOfBoundsException("0");
        return taxa.get(t - 1).getName();
    }

    /**
     * get index of taxon
     *
     * @param taxon
     * @return number between 1 and ntax, or -1 if not found
     */
    public int indexOf(Taxon taxon) {
        if (taxon2index.containsKey(taxon))
            return taxon2index.get(taxon) + 1;
        else
            return -1;
    }

    /**
     * get index of taxon by label
     *
     * @param label
     * @return number between 1 and ntax, or -1 if not found
     */
    public int indexOf(String label) {
        final int t = getLabels().indexOf(label);
        if (t == -1)
            return -1;
        else
            return t + 1;
    }

    /**
     * get list of all taxa
     *
     * @return taxa
     */
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
        final HashMap<Integer, Integer> map = new HashMap<>();
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
        return getLabels(taxa);
    }

    /**
     * get all taxon labels in the given collection
     *
     * @return labels
     */
    public static ArrayList<String> getLabels(Collection<Taxon> taxa) {
        final ArrayList<String> labels = new ArrayList<>();
        for (Taxon taxon : taxa) {
            labels.add(taxon.getName());
        }
        return labels;
    }

    /**
     * get all taxon labels in the given collection
     *
     * @return labels
     */
    public ArrayList<String> getLabels(Iterable<Integer> taxa) {
        final ArrayList<String> labels = new ArrayList<>();
        for (Integer t : taxa) {
            labels.add(getLabel(t));
        }
        return labels;
    }

    public Object clone() {
        TaxaBlock result = new TaxaBlock();

        try {
            for (int t = 1; t <= getNtax(); t++) {
                result.add(get(t));
            }
        } catch (Exception ex) {
            Basic.caught(ex);
        }
        result.traitsBlock.set(traitsBlock.get());
        return result;
    }

    /**
     * get the current set of taxa as a bit set
     *
     * @return
     */
    public BitSet getTaxaSet() {
        final BitSet taxa = new BitSet();
        taxa.set(1, getNtax() + 1);
        return taxa;
    }

    @Override
    public Class getFromInterface() {
        return IFromTaxa.class;
    }

    @Override
    public Class getToInterface() {
        return IToTaxa.class;
    }

    @Override
    public String getInfo() {
        return getNtax() + " taxa";
    }

    public TraitsBlock getTraitsBlock() {
        return traitsBlock.get();
    }

    public ObjectProperty<TraitsBlock> traitsBlockProperty() {
        return traitsBlock;
    }

    public void setTraitsBlock(TraitsBlock traitsBlock) {
        this.traitsBlock.set(traitsBlock);
    }


    /**
     * copy a taxon block
     *
     * @param src
     */
    public void copy(TaxaBlock src) {
        taxa.clear();
        taxa.addAll(src.taxa);
        taxon2index.clear();
        taxon2index.clear();
        taxon2index.putAll(src.taxon2index);
        name2taxon.clear();
        name2taxon.putAll(src.name2taxon);
        traitsBlock.set(src.traitsBlock.get());
    }
}
