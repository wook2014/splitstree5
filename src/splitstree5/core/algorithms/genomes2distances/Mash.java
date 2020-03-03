/*
 * Mash.java Copyright (C) 2020. Daniel H. Huson
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
 *
 */

package splitstree5.core.algorithms.genomes2distances;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import jloda.util.Basic;
import jloda.util.Pair;
import jloda.util.ProgressListener;
import jloda.util.Triplet;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.genomes2distances.mash.MashDistance;
import splitstree5.core.algorithms.genomes2distances.mash.MashSketch;
import splitstree5.core.algorithms.interfaces.IFromGenomes;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.GenomesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.GenomesNexusFormat;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * implements the Mash algorithm
 * Daniel Huson, 3.2020
 */
public class Mash extends Algorithm<GenomesBlock, DistancesBlock> implements IFromGenomes, IToDistances {
    public enum Distance {
        Phylogenetic, Jaccard;

        public String toString() {
            if (this == Phylogenetic)
                return "Phylogenetic";
            else
                return "1-Jaccard";
        }
    }

    private final IntegerProperty optionKMerSize = new SimpleIntegerProperty(21);
    private final IntegerProperty optionSketchSize = new SimpleIntegerProperty(1000);
    private final ObjectProperty<Distance> optionDistances = new SimpleObjectProperty<>(Distance.Phylogenetic);

    @Override
    public List<String> listOptions() {
        return Arrays.asList("optionKMerSize", "optionSketchSize", "optionDistances");
    }

    @Override
    public String getCitation() {
        return "Ondov et al 2016; Brian D. Ondov, Todd J. Treangen, Páll Melsted, Adam B. Mallonee, Nicholas H. Bergman, Sergey Koren & Adam M. Phillippy. Mash: fast importgenomes and metagenome distance estimation using MinHash. Genome Biol 17, 132 (2016).";
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, GenomesBlock genomesBlock, DistancesBlock distancesBlock) throws Exception {

        final boolean isNucleotideData = ((GenomesNexusFormat) genomesBlock.getFormat()).getCharactersType().equals(GenomesNexusFormat.CharactersType.dna);
        final int alphabetSize = (isNucleotideData ? 5 : 21);

        final boolean use64Bits = (Math.pow(alphabetSize, getOptionKMerSize()) >= Integer.MAX_VALUE);

        progress.setSubtask("Sketching");
        progress.setMaximum(genomesBlock.getNGenomes());
        progress.setProgress(0);

        final ArrayList<MashSketch> sketches = genomesBlock.getGenomes().parallelStream().map(g -> new Pair<>(g.getName(), g.parts()))
                .map(pair -> MashSketch.compute(pair.getFirst(), Basic.asList(pair.getSecond()), isNucleotideData, getOptionSketchSize(), getOptionKMerSize(), use64Bits, progress))
                .collect(Collectors.toCollection(ArrayList::new));

        progress.checkForCancel();

        if (sketches.size() < 4) {
            throw new IOException("Too few genomes: " + sketches.size());
        }

        // todo: warn when files not found

        for (final MashSketch sketch : sketches) {
            if (sketch.getValues().length == 0)
                throw new IOException("Sketch '" + sketch.getName() + "': too few different k-mers");
        }

        final List<Triplet<MashSketch, MashSketch, Double>> triplets = new ArrayList<>();

        for (int i = 0; i < sketches.size(); i++) {
            for (int j = i + 1; j < sketches.size(); j++) {
                triplets.add(new Triplet<>(sketches.get(i), sketches.get(j), 0.0));
            }
        }

        triplets.parallelStream().forEach(t -> t.setThird(MashDistance.compute(t.get1(), t.get2(), getOptionDistances() == Distance.Phylogenetic)));

        progress.checkForCancel();

        final Map<String, Integer> name2rank = new HashMap<>();

        for (int i = 0; i < sketches.size(); i++) {
            final String name = sketches.get(i).getName();
            name2rank.put(name, i + 1);
        }

        distancesBlock.clear();

        distancesBlock.setNtax(taxaBlock.getNtax());
        for (Triplet<MashSketch, MashSketch, Double> triplet : triplets) {
            final int t1 = name2rank.get(triplet.get1().getName());
            final int t2 = name2rank.get(triplet.get2().getName());
            distancesBlock.set(t1, t2, triplet.getThird());
            distancesBlock.set(t2, t1, triplet.getThird());
        }
    }

    public int getOptionKMerSize() {
        return optionKMerSize.get();
    }

    public IntegerProperty optionKMerSizeProperty() {
        return optionKMerSize;
    }

    public void setOptionKMerSize(int optionKMerSize) {
        this.optionKMerSize.set(optionKMerSize);
    }

    public int getOptionSketchSize() {
        return optionSketchSize.get();
    }

    public IntegerProperty optionSketchSizeProperty() {
        return optionSketchSize;
    }

    public void setOptionSketchSize(int optionSketchSize) {
        this.optionSketchSize.set(optionSketchSize);
    }

    public Distance getOptionDistances() {
        return optionDistances.get();
    }

    public ObjectProperty<Distance> optionDistancesProperty() {
        return optionDistances;
    }

    public void setOptionDistances(Distance optionDistances) {
        this.optionDistances.set(optionDistances);
    }
}
