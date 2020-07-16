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
import splitstree5.core.algorithms.genomes2distances.mash.MashSketch;
import splitstree5.core.algorithms.genomes2distances.utils.bloomfilter.BloomFilter;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * compute mash sketches
 * Daniel Huson, 7.2020
 */
public class ComputeMashSketches {
    /**
     * main
     */
    public static void main(String[] args) {
        try {
            ProgramProperties.setProgramName("ComputeMashSketches");
            ProgramProperties.setProgramVersion(splitstree5.main.Version.SHORT_DESCRIPTION);

            PeakMemoryUsageMonitor.start();
            (new ComputeMashSketches()).run(args);
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
        if (args.length == 0 && System.getProperty("user.name").equals("huson")) {
            args = new String[]{"-i", "/Users/huson/data/gtdb/release89/archaea",
                    "-o", "/Users/huson/data/gtdb/release89/sketches",
                    "-ok",
                    "-oak", "/Users/huson/data/gtdb/release89/archaea.kmers",
                    "-oabf", "/Users/huson/data/gtdb/release89/archaea.bfilter",
                    "-s", "1000",
                    "-v"
            };
        }

        final ArgsOptions options = new ArgsOptions(args, this.getClass(), "Computes mash sketches for FastA files");
        options.setVersion(ProgramProperties.getProgramVersion());
        options.setLicense("This is free software, licensed under the terms of the GNU General Public License, Version 3.");
        options.setAuthors("Daniel H. Huson");

        options.comment("Input and output");
        final String[] input = options.getOptionMandatory("-i", "input", "Input fastA files (directory or .gz ok)", new String[0]);
        final String[] output = options.getOptionMandatory("-o", "output", "Output mash sketch files (directory or .gz ok, use suffix .msketch)", new String[0]);
        final boolean createKMerFiles = options.getOption("-ok", "kMerFiles", "Create k-mer files, too", false);

        final String outputFormat = options.getOption("-f", "format", "Sketch output format ", new String[]{"binary", "hashes", "kmers"}, "binary");

        options.comment("Mash parameters");

        final int kParameter = options.getOption("-k", "kmerSize", "Word size k", 21);
        final int sParameter = options.getOption("-s", "sketchSize", "Sketch size", 1000);
        final int randomSeed = options.getOption("-rs", "randomSeed", "Hashing random seed", 42);
        final boolean filterUnique = options.getOption("-fu", "filterUnique", "Filter unique k-mers (use only for error-prone reads)", false);

        final boolean isNucleotideData = options.getOption("-st", "sequenceType", "Sequence type", new String[]{"dna", "protein"}, "dna").equalsIgnoreCase("dna");

        options.comment("All kmers output");
        final String allKMersOutputFile = options.getOption("-oak", "outputAllKMers", "Output file for all sketch k-mers (stdout or .gz ok, use suffix .kmers)", "");
        final String bloomFilterOutputFile = options.getOption("-oabf", "outputAllBloomFilter", "Output file for Bloom filter for all sketch kmers (use suffix .bfilter)", "");
        final double fpProbability = options.getOption("-fp", "fpProb", "Probability of false positive error in Bloom filter", 0.0001);
        // options.comment(ArgsOptions.OTHER);
        // add number of cores option

        options.done();

        final ArrayList<String> inputFiles = new ArrayList<>();
        for (String name : input) {
            if (Basic.fileExistsAndIsNonEmpty(name))
                inputFiles.add(name);
            else if (Basic.isDirectory(name)) {
                inputFiles.addAll(Basic.getAllFilesInDirectory(name, true, ".fasta", ".fna", ".faa", ".fasta.gz", ".fna.gz", ".faa.gz"));
            }
        }

        for (String name : inputFiles) {
            Basic.checkFileReadableNonEmpty(name);
        }

        final ArrayList<String> outputFiles = new ArrayList<>();
        if (output.length == 0) {
            for (String file : inputFiles) {
                outputFiles.add(Basic.replaceFileSuffix(file, ".msketch"));
            }
        } else if (output.length == 1) {
            if (output[0].equals("stdout")) {
                outputFiles.add("stdout");
            } else if (Basic.isDirectory(output[0])) {
                for (String file : inputFiles) {
                    outputFiles.add(new File(output[0], Basic.replaceFileSuffix(Basic.getFileNameWithoutPath(file), ".msketch")).getPath());
                }
            } else if (inputFiles.size() == 1) {
                outputFiles.add(output[0]);
            } else
                throw new UsageException("Input and output files don't match");
        } else if (output.length == inputFiles.size()) {
            outputFiles.addAll(Arrays.asList(output));
        } else
            throw new UsageException("Input and output files don't match");

        final BloomFilter allKMersBloomFilter;

        if (bloomFilterOutputFile.length() > 0) {
            final int estimatedSize = sParameter * inputFiles.size();
            allKMersBloomFilter = new BloomFilter(estimatedSize, fpProbability);
        } else
            allKMersBloomFilter = null;

        final Set<byte[]> allKMersSet;
        if (allKMersOutputFile.length() > 0)
            allKMersSet = new HashSet<>();
        else
            allKMersSet = null;

        final ArrayList<Pair<String, String>> inputOutputPairs = new ArrayList<>();

        for (int i = 0; i < inputFiles.size(); i++) {
            inputOutputPairs.add(new Pair<>(inputFiles.get(i), outputFiles.get(outputFiles.size() == 1 ? 0 : i)));
        }

        final Single<IOException> exception = new Single<>();
        try (final ProgressPercentage progress = new ProgressPercentage("Sketching...", inputOutputPairs.size())) {
            if (Basic.isDirectory(output[0]))
                System.err.println("Writing to directory: " + output[0]);

            inputOutputPairs.parallelStream().forEach(inputOutputPair -> {
                if (exception.get() == null) {
                    try {
                        final String inputFile = inputOutputPair.getFirst();
                        final byte[] sequence = readSequences(inputFile);
                        final MashSketch sketch = MashSketch.compute(inputFile, Collections.singleton(sequence), isNucleotideData, sParameter, kParameter, randomSeed, filterUnique, true, progress);
                        saveSketch(inputOutputPair.getSecond(), sketch, outputFormat);
                        if (allKMersBloomFilter != null) {
                            synchronized (allKMersBloomFilter) {
                                allKMersBloomFilter.addAll(sketch.getKmers());
                            }
                        }
                        if (allKMersSet != null) {
                            synchronized (allKMersSet) {
                                allKMersSet.addAll(Arrays.asList(sketch.getKmers()));
                            }
                        }
                        if (createKMerFiles) {
                            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(Basic.getOutputStreamPossiblyZIPorGZIP(Basic.replaceFileSuffixKeepGZ(inputOutputPair.getSecond(), ".kmers"))))) {
                                w.write(sketch.getKMersString());
                            }
                        }
                        progress.checkForCancel();
                    } catch (IOException ex) {
                        exception.setIfCurrentValueIsNull(ex);
                    }
                }
            });
        }
        if (exception.get() != null)
            throw exception.get();
        System.err.println(String.format("Wrote %,d files", inputOutputPairs.size()));

        if (allKMersBloomFilter != null) {
            try (OutputStream outs = Basic.getOutputStreamPossiblyZIPorGZIP(bloomFilterOutputFile)) {
                outs.write(allKMersBloomFilter.getBytes());
            }
        }
        if (allKMersSet != null) {
            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(Basic.getOutputStreamPossiblyZIPorGZIP(allKMersOutputFile)))) {
                for (byte[] kmer : allKMersSet) {
                    w.write(Basic.toString(kmer) + "\n");
                }
            }
        }
    }

    private byte[] readSequences(String fileName) throws IOException {
        try (FileLineIterator it = new FileLineIterator(fileName)) {
            return it.stream().filter(line -> !line.startsWith(">")).map(line -> line.replaceAll("\\s+", "")).collect(Collectors.joining()).getBytes();
        }
    }

    private void saveSketch(String outputFile, MashSketch sketch, String outputFormat) throws IOException {
        try (OutputStream outs = Basic.getOutputStreamPossiblyZIPorGZIP(outputFile)) {
            switch (outputFormat) {
                case "hashes": {
                    try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(outs))) {
                        w.write(sketch.getString() + "\n");
                    }
                    break;
                }
                case "binary": {
                    try (BufferedOutputStream w = new BufferedOutputStream(outs)) {
                        w.write(sketch.getBytes());
                    }
                    break;
                }

                case "kmers": {
                    try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(outs))) {
                        w.write(sketch.getKMersString());
                    }
                    break;
                }
            }
        }
    }
}
