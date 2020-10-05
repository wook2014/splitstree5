/*
 *  Copyright (C) 2018. Daniel H. Huson
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

/*
 *  GenomeContext.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.tools;

import jloda.fx.util.ArgsOptions;
import jloda.fx.util.ProgramExecutorService;
import jloda.kmers.mash.MashDistance;
import jloda.util.*;
import splitstree5.dialogs.analyzegenomes.AccessReferenceDatabase;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * compute the genome context of a set of sequences
 * Daniel Huson, 9.2020
 */
public class GenomeContext {
    /**
     * main
     */
    public static void main(String[] args) {
        try {
            ProgramProperties.setProgramName("GenomeContext");
            ProgramProperties.setProgramVersion(splitstree5.main.Version.SHORT_DESCRIPTION);

            PeakMemoryUsageMonitor.start();
            (new GenomeContext()).run(args);
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
     */
    public void run(String[] args) throws Exception {
        final ArgsOptions options = new ArgsOptions(args, this.getClass(), "Compute the genome context for sequences");
        options.setVersion(ProgramProperties.getProgramVersion());
        options.setLicense("This is free software, licensed under the terms of the GNU General Public License, Version 3.");
        options.setAuthors("Daniel H. Huson");

        options.comment("Input and output:");
        final List<String> inputFiles = options.getOptionMandatory("-i", "input", "Input query FastA files (directory, stdin, .gz ok)", Collections.emptyList());
        final boolean perFastARecord = options.getOption("-p", "perFastaRecord", "Process each FastA record as a separate sequence", false);
        final String databaseFile = options.getOptionMandatory("-d", "database", "Database file", "");
        final String outputFile = options.getOption("-o", "output", "Output file (stdout, .gz ok)", "stdout");

        options.comment("Filtering");
        final double maxDistance = options.getOption("-md", "maxDistance", "Max mash distance to consider", 1d);
        final boolean best = options.getOption("-ub", "useBest", "Use best distance only", false);
        int minSketchIntersection = options.getOption("-ms", "minSketchIntersect", "Minimum sketch intersection size", 1);
        final int maxCount = options.getOption("-m", "max", "Max number of genomes to return", 25);

        options.comment("Reporting:");
        final boolean useFastAHeaders = options.getOption("-fh", "useFastaHeader", "Use FastA headers for query sequences", false);
        final boolean reportName = options.getOption("-rn", "reportNames", "Report reference names", true);
        final boolean reportId = options.getOption("-ri", "reportIds", "Report reference ids", false);
        final boolean reportFile = options.getOption("-rf", "reportFiles", "Report reference files", false);
        final boolean reportDistance = options.getOption("-rd", "reportMashDistances", "Report mash distances", true);
        final boolean reportLCA = options.getOption("-rlca", "reportLCA", "Report LCA of references", true);

        options.comment(ArgsOptions.OTHER);
        ProgramExecutorService.setNumberOfCoresToUse(options.getOption("-t", "threads", "Number of threads to use", Runtime.getRuntime().availableProcessors()));
        options.done();

        Basic.checkFileReadableNonEmpty(databaseFile);
        Basic.checkFileWritable(outputFile, true);

        if (inputFiles.size() == 1) {
            final String name = inputFiles.get(0);
            if (!name.equals("stdin") && !Basic.fileExistsAndIsNonEmpty(name)) {
                inputFiles.clear();
                inputFiles.addAll(Basic.getAllFilesInDirectory(name, true, ".fa", ".fna", ".fasta", ".fa.gz", ".fna.gz", ".fasta.gz"));
                if (inputFiles.size() == 0)
                    throw new IOException("No FastA files found in directory: " + name);
            }
        }

        try (Writer w = new OutputStreamWriter(Basic.getOutputStreamPossiblyZIPorGZIP(outputFile))) {
            try (AccessReferenceDatabase.MultiAccess multiAccess = new AccessReferenceDatabase.MultiAccess(ProgramExecutorService.getNumberOfCoresToUse(), databaseFile)) {
                final AccessReferenceDatabase database = multiAccess.next();
                final int mashK = database.getMashK();
                final int mashS = database.getMashS();

                try (final ProgressPercentage progress = new ProgressPercentage("Processing input files (" + inputFiles.size() + "):", inputFiles.size())) {
                    for (var fileName : inputFiles) {
                        final List<Pair<String, String>> pairs = new ArrayList<>();
                        try (var it = new FastAFileIterator(fileName)) {
                            while (it.hasNext()) {
                                pairs.add(it.next());
                            }
                        }

                        if (perFastARecord) {
                            if (!useFastAHeaders) {
                                final var name = Basic.replaceFileSuffix(Basic.getFileNameWithoutPath(fileName), "");
                                int count = 0;
                                for (var pair : pairs) {
                                    pair.setFirst(name + (++count));
                                }
                            }
                        } else { // per file
                            final List<String> sequences = pairs.stream().map(Pair::getSecond).collect(Collectors.toList());
                            final var name = (useFastAHeaders ? pairs.get(0).getFirst() : Basic.replaceFileSuffix(Basic.getFileNameWithoutPath(fileName), ""));
                            pairs.clear();
                            pairs.add(new Pair<>(name, Basic.toString(sequences, "").replaceAll("\\s", "")));
                        }

                        // todo: update minSketchIntersection from maxDistance
                        if (maxDistance < 1)
                            minSketchIntersection = Math.max(minSketchIntersection, computeMinSketchIntersection(maxDistance, mashK, mashS));

                        for (var pair : pairs) {
                            final Collection<Map.Entry<Integer, Double>> list = AccessReferenceDatabase.findSimilar(multiAccess, new ProgressSilent(), minSketchIntersection, Collections.singleton(pair.getSecond().getBytes()));

                            final Map<Integer, String> id2name = new HashMap<>();
                            if (reportName) {
                                id2name.putAll(database.getNames(list.stream().map(Map.Entry::getKey).collect(Collectors.toList())));
                            }
                            final Map<Integer, String> id2file = new HashMap<>();
                            if (reportFile) {
                                id2file.putAll(database.getFiles(list.stream().map(Map.Entry::getKey).collect(Collectors.toList())));
                            }


                            int count = 0;
                            final Set<Integer> taxa = new HashSet<>();
                            final StringBuilder buf = new StringBuilder();

                            buf.append("Query: ").append(pair.getFirst()).append("\n");

                            buf.append("Results: ").append(Math.min(list.size(), maxCount)).append("\n");

                            double smallestDistance = 1.0;

                            for (var result : list) {
                                if (++count >= maxCount)
                                    break;
                                if (count == 1)
                                    smallestDistance = result.getValue();
                                else if (best && result.getValue() > smallestDistance)
                                    break;

                                taxa.add(result.getKey());
                                buf.append(count);
                                if (reportName) {
                                    buf.append("\t").append(id2name.get(result.getKey()));
                                }
                                if (reportId) {
                                    buf.append("\t").append(result.getKey());
                                }
                                if (reportFile) {
                                    buf.append("\t").append(id2file.get(result.getKey()));
                                }
                                if (reportDistance) {
                                    buf.append("\t").append(result.getValue());
                                }
                                if (buf.length() > 0)
                                    buf.append("\n");
                            }

                            if (reportLCA && taxa.size() > 0) {
                                final int lca = computeLCA(database, taxa);
                                buf.append("LCA: ").append(lca).append(" ").append(database.getNames(Collections.singleton(lca)).get(lca));
                            }
                            w.write(buf.toString() + "\n\n");
                            w.flush();
                        }
                        progress.incrementProgress();
                    }
                }
            }
        }
    }

    public static int computeMinSketchIntersection(double maxDistance, int mashK, int mashS) {
        for (int i = mashS; i > 1; i--) {
            final double distance = MashDistance.compute((double) (i - 1) / (double) mashS, mashK);
            if (distance > maxDistance)
                return i;
        }
        return 1;
    }

    private static int computeLCA(AccessReferenceDatabase database, Collection<Integer> taxonIds) throws SQLException {
        final Collection<List<Integer>> list = database.getAncestors(taxonIds).values();
        if (list.size() == 0) {
            return 0;
        } else {
            int prev = 0;
            for (int depth = 0; ; depth++) {
                int current = 0;
                for (var ancestors : list) {
                    if (ancestors.size() <= depth)
                        return prev;
                    if (current == 0)
                        current = ancestors.get(depth);
                    else if (current != ancestors.get(depth))
                        return prev;
                }
                prev = current;
            }
        }
    }
}