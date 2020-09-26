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

import javafx.beans.property.*;
import jloda.fx.util.ProgramExecutorService;
import jloda.fx.window.NotificationManager;
import jloda.kmers.GenomeDistanceType;
import jloda.util.Basic;
import jloda.util.ExecuteInParallel;
import jloda.util.ProgressListener;
import jloda.util.Triplet;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromGenomes;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.GenomesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.GenomesNexusFormat;
import splitstree5.untested.dashing.DashingDistance;
import splitstree5.untested.dashing.DashingSketch;

import java.io.IOException;
import java.util.*;


/**
 * implements the Dashing algorithm
 * Daniel Huson, 3.2020
 */
public class DashingUntested extends Algorithm<GenomesBlock, DistancesBlock> implements IFromGenomes, IToDistances {
    private final IntegerProperty optionKMerSize = new SimpleIntegerProperty(31);
    private final IntegerProperty optionPrefixSize = new SimpleIntegerProperty(10);
    private final ObjectProperty<GenomeDistanceType> optionDistances = new SimpleObjectProperty<>(GenomeDistanceType.Mash);

    private final BooleanProperty optionIgnoreUniqueKMers = new SimpleBooleanProperty(false);

    private final IntegerProperty optionHashSeed = new SimpleIntegerProperty(42);


    @Override
    public List<String> listOptions() {
        return Arrays.asList("optionKMerSize", "optionPrefixSize", "optionDistances", "optionHashSeed", "optionIgnoreUniqueKMers");
    }

    @Override
    public String getCitation() {
        return "Baker and Langmead 2019;Baker DN and Langmead B, Genome Biology (2019) 20:265";
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, GenomesBlock genomesBlock, DistancesBlock distancesBlock) throws Exception {

        final boolean isNucleotideData = ((GenomesNexusFormat) genomesBlock.getFormat()).getCharactersType().equals(GenomesNexusFormat.CharactersType.dna);


        final ArrayList<DashingSketch> sketches = new ArrayList<>();

        progress.setSubtask("Sketching");
        ExecuteInParallel.apply(genomesBlock.getGenomes(), g ->
                        Collections.singleton(DashingSketch.compute(g.getName(), Basic.asList(g.parts()), getOptionKMerSize(), getOptionPrefixSize(), getOptionHashSeed(), isOptionIgnoreUniqueKMers(), progress)),
                sketches, ProgramExecutorService.getNumberOfCoresToUse(), progress);
        progress.reportTaskCompleted();

        if (sketches.size() < 4) {
            throw new IOException("Too few genomes: " + sketches.size());
        }

        // todo: warn when files not found

        for (final DashingSketch sketch : sketches) {
            if (sketch.getHarmonicMean() == Double.NEGATIVE_INFINITY)
                throw new IOException("Sketch '" + sketch.getName() + "': too few different k-mers");
            if (false)
                System.err.println(sketch.toStringComplete());
        }

        final List<Triplet<DashingSketch, DashingSketch, Double>> triplets = new ArrayList<>();

        for (int i = 0; i < sketches.size(); i++) {
            for (int j = i + 1; j < sketches.size(); j++) {
                triplets.add(new Triplet<>(sketches.get(i), sketches.get(j), 0.0));
            }
        }

        progress.setSubtask("distances");
        ExecuteInParallel.apply(triplets, t -> t.setThird(DashingDistance.compute(t.get1(), t.get2(), getOptionDistances())), ProgramExecutorService.getNumberOfCoresToUse(), progress);
        progress.reportTaskCompleted();

        final Map<String, Integer> name2rank = new HashMap<>();

        for (int i = 0; i < sketches.size(); i++) {
            final String name = sketches.get(i).getName();
            name2rank.put(name, i + 1);
        }

        distancesBlock.clear();

        int countOnes = 0;

        distancesBlock.setNtax(taxaBlock.getNtax());
        for (Triplet<DashingSketch, DashingSketch, Double> triplet : triplets) {
            final int t1 = name2rank.get(triplet.get1().getName());
            final int t2 = name2rank.get(triplet.get2().getName());
            final double dist = triplet.getThird();
            distancesBlock.set(t1, t2, dist);
            distancesBlock.set(t2, t1, dist);
            if (dist == 1.0)
                countOnes++;
        }

        if (countOnes > 0)
            NotificationManager.showWarning(String.format("Failed to determine distance for %d pairs (distances set to 1)", countOnes));

    }

    @Override
    public String getToolTip(String optionName) {
        if (optionName.endsWith("IgnoreUniqueKMers"))
            return "Use this only when input data consists of unassembled sequencing reads";
        return super.getToolTip(optionName);
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

    public int getOptionPrefixSize() {
        return optionPrefixSize.get();
    }

    public IntegerProperty optionPrefixSizeProperty() {
        return optionPrefixSize;
    }

    public void setOptionPrefixSize(int optionPrefixSize) {
        this.optionPrefixSize.set(optionPrefixSize);
    }

    public GenomeDistanceType getOptionDistances() {
        return optionDistances.get();
    }

    public ObjectProperty<GenomeDistanceType> optionDistancesProperty() {
        return optionDistances;
    }

    public void setOptionDistances(GenomeDistanceType optionDistances) {
        this.optionDistances.set(optionDistances);
    }

    public boolean isOptionIgnoreUniqueKMers() {
        return optionIgnoreUniqueKMers.get();
    }

    public BooleanProperty optionIgnoreUniqueKMersProperty() {
        return optionIgnoreUniqueKMers;
    }

    public void setOptionIgnoreUniqueKMers(boolean optionIgnoreUniqueKMers) {
        this.optionIgnoreUniqueKMers.set(optionIgnoreUniqueKMers);
    }

    public int getOptionHashSeed() {
        return optionHashSeed.get();
    }

    public IntegerProperty optionHashSeedProperty() {
        return optionHashSeed;
    }

    public void setOptionHashSeed(int optionHashSeed) {
        this.optionHashSeed.set(optionHashSeed);
    }
}
