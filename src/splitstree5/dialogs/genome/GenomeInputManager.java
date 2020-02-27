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

package splitstree5.dialogs.genome;

import jloda.util.Basic;
import jloda.util.FastAFileIterator;
import jloda.util.FileLineIterator;
import jloda.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * manages the genome input
 * Daniel Huson, 2.2020
 */
public class GenomeInputManager {
    private final List<String> fileNames;
    private final boolean perFile;
    private final boolean useFileName;
    private final int minLength;

    private final Map<String, String> line2label;


    /**
     * constructor
     *
     * @param fileNames
     * @param line2label
     */
    public GenomeInputManager(List<String> fileNames, CompareGenomesDialog.TaxonIdentification taxonIdentification, Map<String, String> line2label, int minLength) {
        this.fileNames = fileNames;
        perFile = (taxonIdentification == CompareGenomesDialog.TaxonIdentification.PerFile || taxonIdentification == CompareGenomesDialog.TaxonIdentification.PerFileUsingFileName);
        useFileName = (taxonIdentification == CompareGenomesDialog.TaxonIdentification.PerFileUsingFileName);
        this.line2label = line2label;
        this.minLength = minLength;
    }


    /**
     * iterator over all input fastA records
     *
     * @return iterator
     */
    public Iterable<Pair<String, byte[]>> iterable() {
        return () -> new Iterator<>() {
            final Iterator<Pair<String, byte[]>> iterator = GenomeInputManager.this.iterator();
            Pair<String, byte[]> next = null;

            {
                while (next == null && iterator.hasNext()) {
                    final Pair<String, byte[]> pair = iterator.next();
                    if (pair.getSecond().length >= minLength)
                        next = pair;
                }
            }

            @Override
            public boolean hasNext() {
                return (next != null);

            }

            @Override
            public Pair<String, byte[]> next() {
                final Pair<String, byte[]> result = next;
                next = null;
                while (next == null && iterator.hasNext()) {
                    final Pair<String, byte[]> pair = iterator.next();
                    if (pair.getSecond().length >= minLength)
                        next = pair;
                }
                return result;
            }
        };
    }

    /**
     * iterator over all input
     *
     * @return iterator
     */
    private Iterator<Pair<String, byte[]>> iterator() {
        return new Iterator<>() {
            private FastAFileIterator fastaIterator;

            private int whichFile = 0;
            private Pair<String, byte[]> next;

            {
                try {
                    if (whichFile < fileNames.size()) {
                        if (perFile)
                            next = getDataFromAFile(fileNames.get(whichFile++), useFileName);
                        else {
                            do {
                                fastaIterator = new FastAFileIterator(fileNames.get(whichFile));
                                if (fastaIterator.hasNext()) {
                                    final Pair<String, String> pair = fastaIterator.next();
                                    next = new Pair<>(Basic.swallowLeadingGreaterSign(pair.getFirst()), pair.getSecond().getBytes());
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
            public Pair<String, byte[]> next() {
                final Pair<String, byte[]> result = next;
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
                            next = new Pair<>(Basic.swallowLeadingGreaterSign(pair.getFirst()), pair.getSecond().getBytes());
                        }
                    }
                } catch (IOException ex) {
                    next = null;
                }
                result.setFirst(line2label.getOrDefault(result.getFirst(), result.getFirst().replaceAll("'", "_")));
                return result;
            }
        };
    }

    private Pair<String, byte[]> getDataFromAFile(String fileName, boolean useFileName) {
        final String name;
        if (useFileName)
            name = Basic.replaceFileSuffix(Basic.getFileNameWithoutPath(fileName), "");
        else {
            final String line = Basic.getFirstLineFromFile(new File(fileName));
            if (line != null)
                name = Basic.swallowLeadingGreaterSign(line);
            else
                name = Basic.replaceFileSuffix(Basic.getFileNameWithoutPath(fileName), "");
        }

        try (FileLineIterator it = new FileLineIterator(fileName)) {
            return new Pair<>(name, it.stream().filter(s -> !s.startsWith(">")).map(s -> s.replaceAll("\\s+", "")).collect(Collectors.joining()).getBytes());
        } catch (IOException e) {
            Basic.caught(e);
            return null;
        }
    }
}
