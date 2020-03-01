/*
 * TreesBlock.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package splitstree5.core.datablocks;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.dialogs.genome.Genome;

/**
 * A genomes block
 * Daniel Huson, 2.2020
 */
public class GenomesBlock extends DataBlock {
    public static final String BLOCK_NAME = "GENOMES";

    private final ObservableList<Genome> genomes;

    public GenomesBlock() {
        genomes = FXCollections.observableArrayList();
    }

    /**
     * shallow copy
     *
     * @param that
     */
    public void copy(GenomesBlock that) {
        clear();
        genomes.addAll(that.getGenomes());
    }

    @Override
    public void clear() {
        super.clear();
        genomes.clear();
    }

    @Override
    public int size() {
        return genomes.size();
    }

    /**
     * access the trees
     *
     * @return trees
     */
    public ObservableList<Genome> getGenomes() {
        return genomes;
    }

    public int getNGenomes() {
        return genomes.size();
    }

    public String getShortDescription() {
        return "Number of genomes: " + size();
    }


    @Override
    public Class getFromInterface() {
        return IFromTrees.class;
    }

    @Override
    public Class getToInterface() {
        return IToTrees.class;
    }

    @Override
    public String getInfo() {
        return (size() == 1 ? "one genome" : size() + " trees");
    }

    @Override
    public String getDisplayText() {
        return super.getDisplayText();
    }

    /**
     * get t-th genomes
     *
     * @param t 1-based
     * @return tree
     */
    public Genome getGenome(int t) {
        return genomes.get(t - 1);
    }

    @Override
    public String getBlockName() {
        return BLOCK_NAME;
    }

}
