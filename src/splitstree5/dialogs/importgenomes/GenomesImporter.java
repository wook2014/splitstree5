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

package splitstree5.dialogs.importgenomes;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * performs genome import
 * Daniel Huson, 2.2020
 */
public class GenomesImporter {
    private final List<String> fileNames;
    private final boolean perFile;
    private final boolean useFileName;
    private final int minLength;
    private final boolean storeFileReferences;

    private final Map<String, String> line2label;

    /**
     * constructor
     *
     * @param fileNames
     * @param line2label
     */
    public GenomesImporter(List<String> fileNames, ImportGenomesDialog.TaxonIdentification taxonIdentification, Map<String, String> line2label, int minLength, boolean storeFileReferences) {
        this.fileNames = fileNames;
        perFile = (taxonIdentification == ImportGenomesDialog.TaxonIdentification.PerFile || taxonIdentification == ImportGenomesDialog.TaxonIdentification.PerFileUsingFileName);
        useFileName = (taxonIdentification == ImportGenomesDialog.TaxonIdentification.PerFileUsingFileName);
        this.line2label = line2label;
        this.minLength = minLength;
        this.storeFileReferences = storeFileReferences;
    }

    /**
     * iterator over all input fastA records
     *
     * @return iterator
     */
    public Iterable<InputRecord> iterable(ProgressListener progressListener) {
        return () -> new Iterator<>() {
            final Iterator<InputRecord> iterator = GenomesImporter.this.iterator(progressListener);
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
            private InputRecord next;

            {
                if (progressListener != null) {
                    progressListener.setMaximum(fileNames.size());
                    try {
                        progressListener.setProgress(0);
                    } catch (CanceledException ignored) {
                    }
                }
                try {
                    if (whichFile < fileNames.size()) {
                        if (perFile)
                            next = getDataFromAFile(fileNames.get(whichFile++), useFileName);
                        else {
                            do {
                                fastaIterator = FastAFileIterator.getFastAOrFastQAsFastAIterator(fileNames.get(whichFile));
                                if (fastaIterator.hasNext()) {
                                    final Pair<String, String> pair = fastaIterator.next();
                                    next = new InputRecord(Basic.swallowLeadingGreaterSign(pair.getFirst()), pair.getSecond().getBytes(), fileNames.get(whichFile), fastaIterator.getPosition());
                                } else {
                                    fastaIterator.close();
                                    whichFile++;
                                }
                            }
                            while (next == null && whichFile < fileNames.size());
                        }
                    }
                } catch (IOException ex) {
                    next = null;
                }
            }

            @Override
            public boolean hasNext() {
                if (next == null) {
                    try {
                        if (fastaIterator != null)
                            fastaIterator.close();
                        ;
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

                    if (perFile) {
                        if (whichFile < fileNames.size())
                            next = getDataFromAFile(fileNames.get(whichFile++), useFileName);
                    } else {
                        while (fastaIterator != null && !fastaIterator.hasNext()) {
                            fastaIterator.close();
                            whichFile++;
                            if (whichFile < fileNames.size()) {
                                fastaIterator = new FastAFileIterator(fileNames.get(whichFile));
                            } else {
                                fastaIterator = null;
                            }
                        }
                        if (fastaIterator != null) {
                            final Pair<String, String> pair = fastaIterator.next();
                            next = new InputRecord(Basic.swallowLeadingGreaterSign(pair.getFirst()), pair.getSecond().toUpperCase().getBytes(), fileNames.get(whichFile), fastaIterator.getPosition());
                        }
                    }
                } catch (IOException ex) {
                    next = null;
                }
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
            return new InputRecord(name, it.stream().map(Pair::getSecond).collect(Collectors.joining()).getBytes(),
                    fileName, 0L);

        } catch (IOException e) {
            Basic.caught(e);
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
    public void saveData(String fileName, FlowPane statusFlowPane, Consumer<Boolean> running) {
        AService<Integer> aService = new AService<>(statusFlowPane);

        aService.setCallable(() -> {
            aService.getProgressListener().setTasks("Processing files", "");

            final GenomesBlock genomesBlock = new GenomesBlock();

            final Single<Exception> exception = new Single<>();

            StreamSupport.stream(iterable(aService.getProgressListener()).spliterator(), true).forEach(inputRecord -> {
                try {
                    final Genome genome = new Genome();
                    genome.setName(inputRecord.getName());
                    final Genome.GenomePart genomePart = new Genome.GenomePart();
                    genomePart.setName("part");
                    if (storeFileReferences) {
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
                System.err.println("Name: " + name);
                final String uniqueName = taxaBlock.addTaxonByName(name);
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
