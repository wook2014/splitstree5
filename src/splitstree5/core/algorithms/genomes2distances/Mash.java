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
import jloda.kmers.mash.MashDistance;
import jloda.kmers.mash.MashSketch;
import jloda.util.*;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromGenomes;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.GenomesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.GenomesNexusFormat;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * implements the Mash algorithm
 * Daniel Huson, 3.2020
 */
public class Mash extends Algorithm<GenomesBlock, DistancesBlock> implements IFromGenomes, IToDistances {

    private final IntegerProperty optionKMerSize = new SimpleIntegerProperty(15);
    private final IntegerProperty optionSketchSize = new SimpleIntegerProperty(10000);
    private final ObjectProperty<GenomeDistanceType> optionDistances = new SimpleObjectProperty<>(GenomeDistanceType.Mash);

    private final BooleanProperty optionIgnoreUniqueKMers = new SimpleBooleanProperty(false);

    private final IntegerProperty optionHashSeed = new SimpleIntegerProperty(42);

    @Override
    public List<String> listOptions() {
        return Arrays.asList("optionKMerSize", "optionSketchSize", "optionDistances", "optionHashSeed", "optionIgnoreUniqueKMers");
    }

    @Override
    public String getCitation() {
        return "Ondov et al 2016; Brian D. Ondov, Todd J. Treangen, Páll Melsted, Adam B. Mallonee, Nicholas H. Bergman, Sergey Koren & Adam M. Phillippy. Mash: fast genome and metagenome distance estimation using MinHash. Genome Biol 17, 132 (2016).";
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, GenomesBlock genomesBlock, DistancesBlock distancesBlock) throws Exception {

        final boolean isNucleotideData = ((GenomesNexusFormat) genomesBlock.getFormat()).getCharactersType().equals(GenomesNexusFormat.CharactersType.dna);

        progress.setSubtask("Sketching");
        progress.setMaximum(genomesBlock.getNGenomes());
        progress.setProgress(0);

        genomesBlock.checkGenomesPresent();

        final ArrayList<MashSketch> sketches = new ArrayList<>(genomesBlock.size());
        {
            final ExecutorService service = Executors.newFixedThreadPool(ProgramExecutorService.getNumberOfCoresToUse());
            final Single<Exception> exception = new Single<>(null);
            try {
                genomesBlock.getGenomes().forEach(genome -> {
                    service.submit(() -> {
                        if (exception.get() == null) {
                            try {
                                final MashSketch sketch = MashSketch.compute(genome.getName(), Basic.asList(genome.parts()), isNucleotideData, getOptionSketchSize(), getOptionKMerSize(), getOptionHashSeed(), isOptionIgnoreUniqueKMers(), progress);
                                synchronized (sketches) {
                                    progress.checkForCancel();
                                    sketches.add(sketch);
                                }
                            } catch (Exception ex) {
                                exception.setIfCurrentValueIsNull(ex);
                            }
                        }
                    });
                });
            } finally {
                service.shutdown();
                service.awaitTermination(1000, TimeUnit.DAYS);
            }
            if (exception.get() != null)
                throw exception.get();
        }

        if (sketches.size() < 4) {
            throw new IOException("Too few genomes: " + sketches.size());
        }

        // todo: warn when files not found

        int countTooSmall = 0;
        for (final MashSketch sketch : sketches) {
            if (sketch.getValues().length < optionSketchSize.get())
                countTooSmall++;

        }
        if (countTooSmall > 0)
            NotificationManager.showWarning(String.format("Too few k-mers for %,d genomes- rerun with smaller sketch size", countTooSmall));

        final List<Triplet<MashSketch, MashSketch, Double>> triplets = new ArrayList<>();

        for (int i = 0; i < sketches.size(); i++) {
            for (int j = i + 1; j < sketches.size(); j++) {
                triplets.add(new Triplet<>(sketches.get(i), sketches.get(j), 0.0));
            }
        }

        progress.setSubtask("distances");
        ExecuteInParallel.apply(triplets, t -> t.setThird(MashDistance.compute(t.get1(), t.get2(), getOptionDistances())), ProgramExecutorService.getNumberOfCoresToUse(), progress);
        progress.reportTaskCompleted();
        
        final Map<String, Integer> name2rank = new HashMap<>();

        for (int i = 0; i < sketches.size(); i++) {
            final String name = sketches.get(i).getName();
            name2rank.put(name, i + 1);
        }

        distancesBlock.clear();

        int countOnes = 0;

        distancesBlock.setNtax(taxaBlock.getNtax());
        for (Triplet<MashSketch, MashSketch, Double> triplet : triplets) {
            final int t1 = name2rank.get(triplet.get1().getName());
            final int t2 = name2rank.get(triplet.get2().getName());
            final double dist = triplet.getThird();
            distancesBlock.set(t1, t2, dist);
            distancesBlock.set(t2, t1, dist);
            if (dist == 1.0)
                countOnes++;
        }
        if (countOnes > 0)
            NotificationManager.showWarning(String.format("Failed to estimate distance for %d pairs (distances set to 1) - increase sketch size or decrease k", countOnes));
    }

    @Override
    public String getToolTip(String optionName) {
        if (optionName.endsWith("IgnoreUniqueKMers"))
            return "Use this only when input data consists of unassembled reads";
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

    public int getOptionSketchSize() {
        return optionSketchSize.get();
    }

    public IntegerProperty optionSketchSizeProperty() {
        return optionSketchSize;
    }

    public void setOptionSketchSize(int optionSketchSize) {
        this.optionSketchSize.set(optionSketchSize);
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

    public static void main(String[] args) throws IOException {
        final String file1 = "/Users/huson/data/mash/all/EF065509.fasta";
        final String file2 = "/Users/huson/data/mash/all/AY278489.fasta";

        final String name1 = Basic.replaceFileSuffix(Basic.getFileNameWithoutPath(file1), "");
        final String seq1;
        try (FileLineIterator it = new FileLineIterator(file1)) {
            final StringBuilder buf = new StringBuilder();
            it.stream().filter(s -> !s.startsWith(">")).collect(Collectors.toList()).forEach(s ->
                    buf.append(s.replaceAll("\\s+", "")));
            seq1 = buf.toString();
        }

        final String name2 = Basic.replaceFileSuffix(Basic.getFileNameWithoutPath(file2), "");
        final String seq2;
        try (FileLineIterator it = new FileLineIterator(file2)) {
            final StringBuilder buf = new StringBuilder();
            it.stream().filter(s -> !s.startsWith(">")).collect(Collectors.toList()).forEach(s ->
                    buf.append(s.replaceAll("\\s+", "")));
            seq2 = buf.toString();
        }

        final MashSketch sketch1 = MashSketch.compute(name1, Collections.singleton(seq1.getBytes()), true, 1000, 21, 666, false, false, new ProgressSilent());
        final MashSketch sketch2 = MashSketch.compute(name2, Collections.singleton(seq2.getBytes()), true, 1000, 21, 666, false, false, new ProgressSilent());

        System.err.println(String.format("Jaccard: %.5f", MashDistance.compute(sketch1, sketch2, GenomeDistanceType.JaccardIndex)));
        System.err.println(String.format("Mash:    %.5f", MashDistance.compute(sketch1, sketch2, GenomeDistanceType.Mash)));
    }
}

