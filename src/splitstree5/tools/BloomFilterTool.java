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
import jloda.fx.util.ProgramExecutorService;
import jloda.util.*;
import splitstree5.core.algorithms.genomes2distances.utils.bloomfilter.BloomFilter;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

/**
 * compute bloom filter for k-mers
 * Daniel Huson, 7.2020
 */
public class BloomFilterTool {
    /**
     * main
     */
    public static void main(String[] args) {
        try {
            ProgramProperties.setProgramName("ComputeBloomFilter");
            ProgramProperties.setProgramVersion(splitstree5.main.Version.SHORT_DESCRIPTION);

            PeakMemoryUsageMonitor.start();
            (new BloomFilterTool()).run(args);
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
        final ArgsOptions options = new ArgsOptions(args, this.getClass(), "Make a bloom filter or test for containment");
        options.setVersion(ProgramProperties.getProgramVersion());
        options.setLicense("This is free software, licensed under the terms of the GNU General Public License, Version 3.");
        options.setAuthors("Daniel H. Huson");
        options.setCommandMandatory(true);

        final String command = options.getCommand(
                new ArgsOptions.Command("make", "Create a Bloom filter for a collection of k-mers."),
                new ArgsOptions.Command("contains", "Determine containment of k-mers in Bloom filter(s)."),
                new ArgsOptions.Command("help", "Show program usage and quit."));


        options.comment("Input and output");
        final String[] kmerInput = options.getOptionMandatory("-i", "input", "Input files containing k-mers, one per line (directories or .gz ok, use suffix .kmers)", new String[0]);
        final String output;
        if (options.isDoHelp() || command.equals("make"))
            output = options.getOptionMandatory("-o", "output", "Output file (stdout ok)", "");
        else
            output = options.getOption("-o", "output", "Output file (stdout ok)", "stdout");

        options.comment("MAKE options");
        final double fpProbability;
        if (options.isDoHelp() || command.equals("make"))
            fpProbability = options.getOption("-fp", "fpProb", "Probability of false positive error in Bloom filter", 0.0001);
        else
            fpProbability = 0;

        options.comment("CONTAINS options");
        final String[] bloomFilterInput;
        if (options.isDoHelp() || command.equals("contains"))
            bloomFilterInput = options.getOptionMandatory("-ib", "bloomFilterInput", "Input files bloom filtres (directory ok, use suffix .bfilters)", new String[0]);
        else
            bloomFilterInput = null;

        options.comment(ArgsOptions.OTHER);
        // add number of cores option
        ProgramExecutorService.setNumberOfCoresToUse(options.getOption("-t", "threads", "Number of threads", 8));

        options.done();

        final ArrayList<String> inputFiles = getInputFiles(kmerInput, ".kmers", ".kmers.gz");

        if (command.equals("make")) {
            final long numberOfLines;
            {
                final Single<IOException> exception = new Single<>(null);
                final ForkJoinPool threadPool = new ForkJoinPool(ProgramExecutorService.getNumberOfCoresToUse());
                try (ProgressPercentage progress = new ProgressPercentage("Counting input lines", inputFiles.size())) {
                    // there is one job per core, so no need to setup threads:
                    numberOfLines = threadPool.submit(() ->
                            inputFiles.parallelStream().
                                    mapToLong(name -> {
                                        if (exception.get() == null) {
                                            try {
                                                return Files.lines((new File(name)).toPath()).count();
                                            } catch (IOException e) {
                                                exception.setIfCurrentValueIsNull(e);
                                            } finally {
                                                synchronized (progress) {
                                                    progress.incrementProgress();
                                                }
                                            }
                                        }
                                        return 0L;
                                    }).sum()).get();

                } catch (InterruptedException | ExecutionException e) {
                    throw e;
                } finally {
                    threadPool.shutdown();
                }
                if (exception.get() != null)
                    throw exception.get();
                System.err.println("Input lines: " + numberOfLines);
            }

            final BloomFilter allKMersBloomFilter = new BloomFilter((int) numberOfLines, fpProbability);
            {
                final Single<IOException> exception = new Single<>(null);
                final ForkJoinPool threadPool = new ForkJoinPool(ProgramExecutorService.getNumberOfCoresToUse());
                try (ProgressPercentage progress = new ProgressPercentage("Processing input lines", inputFiles.size())) {
                    final int dummy = threadPool.submit(() ->
                            inputFiles.parallelStream().
                                    map(name -> {
                                        if (exception.get() == null) {
                                            try {
                                                return Files.lines((new File(name)).toPath());
                                            } catch (IOException e) {
                                                exception.setIfCurrentValueIsNull(e);
                                            } finally {
                                                synchronized (progress) {
                                                    progress.incrementProgress();
                                                }
                                            }
                                        }
                                        return null;
                                    })
                                    .filter(Objects::nonNull)
                                    .mapToInt(list -> {
                                        list.forEach(s -> allKMersBloomFilter.add(s.getBytes()));
                                        return 1;
                                    }).sum()).get();  // forces wait until parallel threads completed

                } catch (InterruptedException | ExecutionException e) {
                    exception.setIfCurrentValueIsNull(new IOException(e));
                } finally {
                    threadPool.shutdown();
                }
                if (exception.get() != null)
                    throw exception.get();
            }

            System.err.println("Writing Bloom filter to file: " + output);
            try (OutputStream outs = Basic.getOutputStreamPossiblyZIPorGZIP(output)) {
                outs.write(allKMersBloomFilter.getBytes());
            }
            System.err.println("Size: " + Basic.getMemorySizeString((new File(output)).length()));
        } else if (command.equals("contains")) {
            final ArrayList<String> bloomFilterFiles = getInputFiles(bloomFilterInput, ".bfilter", ".bfilter.gz");
            final Map<String, BloomFilter> bloomFilters = new HashMap<>();

            final Single<IOException> exception = new Single<>(null);
            final ForkJoinPool threadPool = new ForkJoinPool(ProgramExecutorService.getNumberOfCoresToUse());
            try (ProgressPercentage progress = new ProgressPercentage("Reading bloom filters", bloomFilterFiles.size())) {
                final int dummy = threadPool.submit(() ->
                        bloomFilterFiles.parallelStream()
                                .mapToInt(name -> {
                                    try {
                                        final BloomFilter bloomFilter = BloomFilter.parseBytes(Files.readAllBytes((new File(name).toPath())));
                                        synchronized (bloomFilters) {
                                            bloomFilters.put(name, bloomFilter);
                                        }
                                        progress.incrementProgress();
                                    } catch (IOException e) {
                                        exception.setIfCurrentValueIsNull(e);
                                    }
                                    return 1;
                                }).sum()).get(); // forces wait until parallel threads completed
            } catch (InterruptedException | ExecutionException e) {
                exception.setIfCurrentValueIsNull(new IOException(e));
            } finally {
                threadPool.shutdown();
            }
            if (exception.get() != null)
                throw exception.get();

            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(Basic.getOutputStreamPossiblyZIPorGZIP(output)))) {
                if (bloomFilterFiles.size() > 1)
                    w.write("#Table\t" + Basic.toString(bloomFilterFiles, "\t") + "\n");
                for (String inputFile : inputFiles) {
                    final List<String> kmers = Files.lines((new File(inputFile)).toPath()).collect(Collectors.toList());
                    w.write(inputFile);
                    for (String bloomFilterFile : bloomFilterFiles) {
                        final BloomFilter bloomFilter = bloomFilters.get(bloomFilterFile);
                        w.write(" " + bloomFilter.countContainedProbably(kmers));
                    }
                    w.write("\n");
                }
            }
        }
    }

    public static ArrayList<String> getInputFiles(String[] input, String... suffixes) throws UsageException, IOException {
        final ArrayList<String> result = new ArrayList<>();
        for (String name : input) {
            if (Basic.fileExistsAndIsNonEmpty(name))
                result.add(name);
            else if (Basic.isDirectory(name)) {
                result.addAll(Basic.getAllFilesInDirectory(name, true, suffixes));
            }
        }
        if (result.size() == 0)
            throw new UsageException("No input files");

        for (String name : result) {
            Basic.checkFileReadableNonEmpty(name);
        }
        return result;
    }
}
