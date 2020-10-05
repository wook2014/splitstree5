/*
 *  GenomeInputManager.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.dialogs.analyzegenomes;

import javafx.collections.ObservableList;
import javafx.scene.layout.FlowPane;
import jloda.fx.control.RichTextLabel;
import jloda.fx.util.AService;
import jloda.fx.window.MainWindowManager;
import jloda.fx.window.NotificationManager;
import jloda.util.*;
import splitstree5.core.data.Genome;
import splitstree5.core.datablocks.GenomesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.dialogs.importer.FileOpener;
import splitstree5.io.nexus.GenomesNexusOutput;
import splitstree5.io.nexus.TaxaNexusOutput;
import splitstree5.main.MainWindow;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * performs genome import
 * Daniel Huson, 2.2020
 */
public class GenomesAnalyzer {
    private final List<String> fileNames;
    private final boolean perFile;
    private final boolean useFileName;
    private final int minLength;
    private final boolean storeFileLocations;

    private final Map<String, String> referenceFile2Names = new HashMap<>();

    private final Map<String, String> line2label;

    /**
     * constructor
     *
     * @param fileNames
     * @param line2label
     */
    public GenomesAnalyzer(List<String> fileNames, AnalyzeGenomesDialog.TaxonIdentification taxonIdentification, Map<String, String> line2label, int minLength, boolean storeFileLocations) {
        this.fileNames = new ArrayList<>(fileNames);
        perFile = (taxonIdentification == AnalyzeGenomesDialog.TaxonIdentification.PerFile || taxonIdentification == AnalyzeGenomesDialog.TaxonIdentification.PerFileUsingFileName);
        useFileName = (taxonIdentification == AnalyzeGenomesDialog.TaxonIdentification.PerFastARecordUsingFileName || taxonIdentification == AnalyzeGenomesDialog.TaxonIdentification.PerFileUsingFileName);
        this.line2label = line2label;
        this.minLength = minLength;
        this.storeFileLocations = storeFileLocations;
    }

    /**
     * iterator over all input fastA records
     *
     * @return iterator
     */
    public Iterable<InputRecord> iterable(ProgressListener progressListener) {
        return () -> new Iterator<>() {
            final Iterator<InputRecord> iterator = GenomesAnalyzer.this.iterator(progressListener);
            InputRecord next = null;

            {
                while (next == null && iterator.hasNext()) {
                    final InputRecord pair = iterator.next();
                    if (pair.getSequence().length >= minLength)
                        next = pair;
                }
            }

            @Override
            public boolean hasNext() {
                return (next != null);
            }

            @Override
            public InputRecord next() {
                final InputRecord result = next;
                next = null;
                while (next == null && iterator.hasNext()) {
                    final InputRecord pair = iterator.next();
                    if (pair.getSequence().length >= minLength)
                        next = pair;
                }
                return result;
            }
        };
    }

    /**
     * iterator over all input records
     *
     * @return iterator
     */
    private Iterator<InputRecord> iterator(ProgressListener progressListener) {
        return new Iterator<>() {
            private IFastAIterator fastaIterator;

            private int whichFile = 0;
            private int countInFile = 0;
            private InputRecord next;

            {
                next(); // this will set next to first result
            }

            @Override
            public boolean hasNext() {
                if (next == null) {
                    try {
                        if (fastaIterator != null)
                            fastaIterator.close();
                    } catch (IOException ignored) {
                    }
                }
                return next != null;
            }

            @Override
            public InputRecord next() {
                final InputRecord result = next;
                try {
                    next = null;

                    if (whichFile < fileNames.size()) {
                        final String fileName = fileNames.get(whichFile);
                        if (referenceFile2Names.containsKey(fileName)) {
                            next = getDataFromAFile(fileName, true);
                            if (next != null)
                                next.setName(referenceFile2Names.get(fileName));
                            whichFile++;
                        } else if (perFile) {
                            next = getDataFromAFile(fileNames.get(whichFile++), useFileName);
                        } else { // perFastA
                            if (fastaIterator == null)
                                fastaIterator = new FastAFileIterator(fileNames.get(whichFile));

                            while (fastaIterator != null && !fastaIterator.hasNext()) {
                                fastaIterator.close();
                                whichFile++;
                                countInFile = 0;
                                if (whichFile < fileNames.size()) {
                                    fastaIterator = new FastAFileIterator(fileNames.get(whichFile));
                                } else {
                                    fastaIterator = null;
                                }
                            }
                            if (fastaIterator != null) {
                                final Pair<String, String> pair = fastaIterator.next();
                                countInFile++;
                                next = new InputRecord(Basic.swallowLeadingGreaterSign(pair.getFirst()), pair.getSecond().toUpperCase().getBytes(), fileNames.get(whichFile), fastaIterator.getPosition());
                                if (useFileName)
                                    next.setName(Basic.replaceFileSuffix(Basic.getFileNameWithoutPath(fileName), "") + ":" + countInFile);
                            }
                        }
                    }
                } catch (IOException ex) {
                    next = null;
                }
                if (result != null)
                    result.setName(line2label.getOrDefault(result.getName(), result.getName().replaceAll("'", "_")));

                if (progressListener != null) {
                    try {
                        progressListener.setProgress(whichFile);
                    } catch (CanceledException ignored) {
                    }
                }
                return result;
            }
        };
    }

    /**
     * get a single input record from a single file
     *
     * @param fileName
     * @param useFileName
     * @return input record
     */
    private InputRecord getDataFromAFile(String fileName, boolean useFileName) {
        final String name;
        if (useFileName)
            name = Basic.replaceFileSuffix(Basic.getFileNameWithoutPath(fileName), "");
        else {
            final String line = Basic.getFirstLineFromFile(new File(fileName));
            if (line != null) {
                if (line.startsWith(">") || line.startsWith("@"))
                    name = line.substring(1).trim();
                else
                    name = line;
            } else
                name = Basic.replaceFileSuffix(Basic.getFileNameWithoutPath(fileName), "");
        }

        try (IFastAIterator it = FastAFileIterator.getFastAOrFastQAsFastAIterator(fileName)) {
            return new InputRecord(name, it.stream().map(Pair::getSecond).collect(Collectors.joining()).getBytes(), fileName, 0L);
        } catch (IOException e) {
            NotificationManager.showError("File " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * save the data to a file
     *
     * @param fileName
     * @param statusFlowPane
     * @throws IOException
     */
    public void saveData(AccessReferenceDatabase referenceDatabase, ObservableList<Integer> referenceIds, String fileName, FlowPane statusFlowPane, Consumer<Boolean> running) {
        AService<Integer> aService = new AService<>(statusFlowPane);

        aService.setCallable(() -> {
            aService.getProgressListener().setTasks("Find similar", "");

            if (referenceIds.size() > 0 && referenceDatabase != null) {
                try {
                    addReferenceFile2Names(referenceDatabase.getReferenceFile2Name(referenceIds, aService.getProgressListener()));
                } catch (SQLException | IOException e) {
                    NotificationManager.showError("Failed to load reference sequences: " + e.getMessage());
                }
            }

            final GenomesBlock genomesBlock = new GenomesBlock();

            final Single<Exception> exception = new Single<>();

            StreamSupport.stream(iterable(aService.getProgressListener()).spliterator(), true).forEach(inputRecord -> {
                try {
                    final Genome genome = new Genome();
                    genome.setName(inputRecord.getName());
                    final Genome.GenomePart genomePart = new Genome.GenomePart();
                    genomePart.setName("part");
                    if (storeFileLocations) {
                        genomePart.setFile(inputRecord.getFile(), inputRecord.getOffset(), inputRecord.getSequence().length);
                    } else {
                        genomePart.setSequence(inputRecord.getSequence(), inputRecord.getSequence().length);
                    }
                    genome.getParts().add(genomePart);
                    genome.setLength(genome.computeLength());
                    synchronized (genomesBlock) {
                        genomesBlock.getGenomes().add(genome);
                        aService.getProgressListener().checkForCancel();
                    }
                } catch (CanceledException ex) {
                    synchronized (exception) {
                        if (exception.get() == null)
                            exception.set(ex);
                    }
                }
            });

            if (exception.get() != null)
                throw exception.get();

            final TaxaBlock taxaBlock = new TaxaBlock();

            if (genomesBlock.size() < 4)
                throw new IOException("Too few genomes: " + genomesBlock.size());

            for (Genome genome : genomesBlock.getGenomes()) {
                final String name = RichTextLabel.getRawText(genome.getName());
                final String uniqueName = taxaBlock.addTaxonByName(name);
                if (!uniqueName.equals(genome.getName()))
                    taxaBlock.get(uniqueName).setDisplayLabel(genome.getName());
            }

            try (BufferedWriter w = new BufferedWriter(new FileWriter(fileName))) {
                // ((GenomesNexusFormat)genomesBlock.getFormat()).setOptionLabels(true);
                w.write("#nexus\n");
                (new TaxaNexusOutput()).write(w, taxaBlock);
                (new GenomesNexusOutput()).write(w, taxaBlock, genomesBlock);
            }
            return genomesBlock.size();
        });

        aService.runningProperty().addListener((c, o, n) -> running.accept(n));

        aService.setOnFailed(c -> NotificationManager.showError("Genome import failed: " + aService.getException()));
        aService.setOnSucceeded(c -> {
            NotificationManager.showInformation("Imported " + aService.getValue() + " genomes");
            FileOpener.open(false, (MainWindow) MainWindowManager.getInstance().getLastFocusedMainWindow(), statusFlowPane, fileName,
                    e -> NotificationManager.showError("Genome import failed: " + e));
        });
        aService.start();
    }

    public void addReferenceFile2Names(Map<String, String> referenceFile2Names) {
        this.referenceFile2Names.putAll(referenceFile2Names);
        this.fileNames.addAll(referenceFile2Names.keySet());
    }

    public static class InputRecord {
        private String name;
        private final byte[] sequence;
        private final String file;
        private final long offset;

        public InputRecord(String name, byte[] sequence, String file, long offset) {
            this.name = name;
            this.sequence = sequence;
            this.file = file;
            this.offset = offset;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public byte[] getSequence() {
            return sequence;
        }

        public String getFile() {
            return file;
        }

        public long getOffset() {
            return offset;
        }
    }
}