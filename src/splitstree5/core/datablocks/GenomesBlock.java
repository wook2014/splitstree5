/*
 * TreesBlock.java Copyright (C) 2021. Daniel H. Huson
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
import splitstree5.core.algorithms.interfaces.IFromGenomes;
import splitstree5.core.algorithms.interfaces.IToGenomes;
import splitstree5.core.data.Genome;
import splitstree5.io.nexus.GenomesNexusFormat;

import java.io.IOException;

/**
 * A genomes block
 * Daniel Huson, 2.2020
 */
public class GenomesBlock extends DataBlock {
    public static final String BLOCK_NAME = "GENOMES";

    private final ObservableList<Genome> genomes;

    public GenomesBlock() {
        genomes = FXCollections.observableArrayList();
        format = new GenomesNexusFormat();
    }

    /**
     * shallow copy
     *
     * @param that
     */
    public void copy(GenomesBlock that) {
        clear();
        genomes.addAll(that.getGenomes());
        format = that.getFormat();
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
     * next the trees
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
    public Class<IFromGenomes> getFromInterface() {
        return IFromGenomes.class;
    }

    @Override
    public Class<IToGenomes> getToInterface() {
        return IToGenomes.class;
    }

    @Override
    public String getInfo() {
        return (size() == 1 ? "one importgenomes" : size() + " genomes");
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

    public void checkGenomesPresent() throws IOException {
        for (int t = 1; t <= getNGenomes(); t++) {
            if (getGenome(t).getLength() == 0)
                throw new IOException("Genome(" + t + "): not present or length 0");
        }
    }
}
