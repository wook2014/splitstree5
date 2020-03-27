/*
 * Similarities2Distances.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.tools;

import jloda.fx.util.ArgsOptions;
import jloda.util.*;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.imports.PhylipDistancesImporter;

import java.util.*;

/**
 * compares two distances
 */
public class CompareDistances {
    /**
     * sort last MAF alignments
     *
     * @param args
     * @throws UsageException
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) {
        try {
            ProgramProperties.setProgramName("CompareDistances");
            ProgramProperties.setProgramVersion(splitstree5.main.Version.SHORT_DESCRIPTION);

            PeakMemoryUsageMonitor.start();
            (new CompareDistances()).run(args);
            System.err.println("Total time:  " + PeakMemoryUsageMonitor.getSecondsSinceStartString());
            System.err.println("Peak memory: " + PeakMemoryUsageMonitor.getPeakUsageString());
            System.exit(0);
        } catch (Exception ex) {
            Basic.caught(ex);
            System.exit(1);
        }
    }

    /**
     * run the program
     *
     * @param args
     */
    public void run(String[] args) throws Exception {
        final ArgsOptions options = new ArgsOptions(args, this.getClass(), "Compares two distance matrices");
        options.setVersion(ProgramProperties.getProgramVersion());
        options.setLicense("This is free software, licensed under the terms of the GNU General Public License, Version 3.");
        options.setAuthors("Daniel H. Huson");

        final String[] inputFiles = options.getOptionMandatory("-i", "input", "Two nput files", new String[0]);
        //final String outputFile = options.getOption("-o", "output", "Output file (stdout for console output)", "stdout");

        final double delta = options.getOption("-d", "delta", "Delta for comparison", 0.001);
        options.done();

        if (inputFiles.length != 2)
            throw new UsageException("--input: two files");

        final TaxaBlock taxa1 = new TaxaBlock();
        final DistancesBlock distances1 = new DistancesBlock();
        (new PhylipDistancesImporter()).parse(new ProgressSilent(), inputFiles[0], taxa1, distances1);

        final TaxaBlock taxa2 = new TaxaBlock();
        final DistancesBlock distances2 = new DistancesBlock();
        (new PhylipDistancesImporter()).parse(new ProgressSilent(), inputFiles[1], taxa2, distances2);

        final Map<String, String> map1to2 = new HashMap<>();

        final Set<String> unused1 = new TreeSet<>(taxa1.getLabels());
        final Set<String> unused2 = new TreeSet<>(taxa2.getLabels());

        for (String name1 : taxa1.getLabels()) {
            if (unused2.contains(name1)) {
                map1to2.put(name1, name1);
                unused1.remove(name1);
                unused2.remove(name1);
            }
        }

        if (unused1.size() > 0) {
            for (String name1 : taxa1.getLabels()) {
                for (String name2 : unused2) {
                    if (name2.contains(name1) || name1.contains(name2)) {
                        map1to2.put(name1, name2);
                        unused1.remove(name1);
                        unused2.remove(name2);
                        break;
                    }
                }
            }
        }

        System.err.println("Comparing files " + Basic.getFileNameWithoutPath(inputFiles[0]) + " and " + Basic.getFileNameWithoutPath(inputFiles[1]) + ":");

        if (unused1.size() > 0)
            System.err.println("Unique to first file: " + Basic.toString(unused1, " ,"));

        if (unused2.size() > 0)
            System.err.println("Unique to second file: " + Basic.toString(unused2, " ,"));

        int bothZero = 0;
        int bothSame = 0;
        int bothDifferent = 0;

        int bothOne = 0;
        int firstOnlyOne = 0;
        int secondOnlyOne = 0;

        Map<String, Integer> badCount = new HashMap<>();

        final ArrayList<Pair<Double, String>> lines = new ArrayList<>();
        ArrayList<Double> differences = new ArrayList<>();
        for (String a1 : taxa1.getLabels()) {
            final String a2 = map1to2.get(a1);
            if (a2 != null) {
                for (String b1 : taxa1.getLabels()) {
                    if (a1.compareTo(b1) < 0) {
                        final String b2 = map1to2.get(b1);
                        if (b2 != null) {
                            final double dist1 = distances1.get(taxa1.indexOf(a1), taxa1.indexOf(b1));
                            final double dist2 = distances2.get(taxa2.indexOf(a2), taxa2.indexOf(b2));
                            final double diff = Math.abs(dist1 - dist2);
                            differences.add(diff);
                            if (diff > delta) {
                                lines.add(new Pair<>(diff, String.format("%s vs %s:    first: %.3f second: %.3f    (diff %.3f)", a1, b1, dist1, dist2, diff)));
                                bothDifferent++;

                                badCount.merge(a1, 1, Integer::sum);
                            } else
                                bothSame++;
                            if (dist1 == 0 && dist2 == 0)
                                bothZero++;
                            if (dist1 == 1.0 && dist2 == 1.0)
                                bothOne++;
                            else if (dist1 == 1.0)
                                firstOnlyOne++;
                            else if (dist2 == 1.0)
                                secondOnlyOne++;
                        }
                    }

                }
            }
        }

        lines.stream().sorted((x, y) -> -Double.compare(x.getFirst(), y.getFirst())).forEach(p -> System.err.println(p.getSecond()));

        System.err.println(String.format("Both same: %5d", bothSame));
        System.err.println(String.format("Both diff: %5d", bothDifferent));

        System.err.println(String.format("Both zero: %5d", bothZero));
        System.err.println(String.format("Both one : %5d", bothOne));
        System.err.println(String.format("First one: %5d", firstOnlyOne));
        System.err.println(String.format("Second one: %5d", secondOnlyOne));


        System.err.println(String.format("Bad taxa (%d):", badCount.size()));
        badCount.entrySet().stream().sorted((x, y) -> -Integer.compare(x.getValue(), y.getValue()))
                .forEach(p -> System.err.println(String.format("%s\t%d", p.getKey(), p.getValue())));

        System.err.println(new Statistics(differences).toString());
    }
}
