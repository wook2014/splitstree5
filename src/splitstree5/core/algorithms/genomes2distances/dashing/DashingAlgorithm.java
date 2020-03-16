/*
 *  DashingAlgorithm.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.core.algorithms.genomes2distances.dashing;

import javafx.beans.property.BooleanProperty;
import javafx.scene.layout.FlowPane;
import jloda.fx.util.AService;
import jloda.fx.window.NotificationManager;
import jloda.util.Basic;
import jloda.util.Pair;
import jloda.util.Triplet;
import splitstree5.core.algorithms.genomes2distances.utils.GenomeDistanceType;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.dialogs.importer.FileOpener;
import splitstree5.io.nexus.DistancesNexusOutput;
import splitstree5.io.nexus.TaxaNexusOutput;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * implements the dashing algorithm
 * Daniel Huson, 3.2020
 */
public class DashingAlgorithm {
    /**
     * apply the dashing algorithm
     *
     * @param outputFile
     * @param input
     * @param kMerSize
     * @param prefixSize
     * @param genomeDistanceType
     * @param statusFlowPane
     * @param isRunning
     */
    public static void apply(String outputFile, Iterable<Pair<String, byte[]>> input, int kMerSize, int prefixSize, GenomeDistanceType genomeDistanceType, boolean filterUniqueKMers, FlowPane statusFlowPane, BooleanProperty isRunning) {
        if (true) {
            for (Pair<String, byte[]> pair : input) {
                System.err.println(pair.getFirst() + " " + pair.getSecond().length);
            }
        }


        AService<Pair<TaxaBlock, DistancesBlock>> aService = new AService<>(statusFlowPane);

        aService.setCallable(() -> {

            final ArrayList<DashingSketch> sketches = StreamSupport.stream(input.spliterator(), true)
                    .map(pair -> DashingSketch.compute(pair.getFirst(), Collections.singletonList(pair.getSecond()), kMerSize, prefixSize, filterUniqueKMers, aService.getProgressListener())).collect(Collectors.toCollection(ArrayList::new));

            aService.getProgressListener().checkForCancel();

            if (sketches.size() < 4) {
                throw new IOException("Too few genomes: " + sketches.size());
            }

            for (final DashingSketch sketch : sketches) {
                if (sketch.getHarmonicMean() == Double.NEGATIVE_INFINITY)
                    throw new IOException("Sketch '" + sketch.getName() + "': too few different k-mers");
            }

            final List<Triplet<DashingSketch, DashingSketch, Double>> triplets = new ArrayList<>();

            for (int i = 0; i < sketches.size(); i++) {
                for (int j = i + 1; j < sketches.size(); j++) {
                    triplets.add(new Triplet<>(sketches.get(i), sketches.get(j), 0.0));
                }
            }

            triplets.parallelStream().forEach(t -> t.setThird(DashingDistance.compute(t.get1(), t.get2(), genomeDistanceType)));
            aService.getProgressListener().checkForCancel();

            final Map<String, Integer> name2rank = new HashMap<>();

            final TaxaBlock taxaBlock = new TaxaBlock();
            for (int i = 0; i < sketches.size(); i++) {
                final String name = sketches.get(i).getName();
                name2rank.put(name, i + 1);
                taxaBlock.addTaxaByNames(Collections.singleton(name));
            }

            final DistancesBlock distancesBlock = new DistancesBlock();
            distancesBlock.setNtax(taxaBlock.getNtax());
            for (Triplet<DashingSketch, DashingSketch, Double> triplet : triplets) {
                final int t1 = name2rank.get(triplet.get1().getName());
                final int t2 = name2rank.get(triplet.get2().getName());
                distancesBlock.set(t1, t2, triplet.getThird());
                distancesBlock.set(t2, t1, triplet.getThird());
            }
            return new Pair<>(taxaBlock, distancesBlock);
        });
        aService.runningProperty().addListener((c, o, n) -> {
            isRunning.set(n);
        });

        aService.setOnFailed(c -> NotificationManager.showError("Dashing failed: " + aService.getException()));

        aService.setOnSucceeded(c -> {
            try (BufferedWriter w = new BufferedWriter(new FileWriter(outputFile))) {
                final Pair<TaxaBlock, DistancesBlock> pair = aService.getValue();
                w.write("#nexus\n");
                (new TaxaNexusOutput()).write(w, pair.getFirst());
                (new DistancesNexusOutput()).write(w, pair.getFirst(), pair.getSecond());
            } catch (IOException ex) {
                NotificationManager.showError("Dashing failed: " + ex);
            }
            if (Basic.fileExistsAndIsNonEmpty(outputFile)) {
                FileOpener.open(false, null, statusFlowPane, outputFile, e -> NotificationManager.showError("Dashing failed: " + e));
            }
        });

        aService.start();
    }


}
