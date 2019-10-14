/*
 *  FastaSplitsImporter.java Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.io.imports;

import jloda.util.CanceledException;
import jloda.util.FileLineIterator;
import jloda.util.IOExceptionWithLineNumber;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.io.imports.interfaces.IImportNoAutoDetect;
import splitstree5.io.imports.interfaces.IImportSplits;
import splitstree5.utils.SplitsUtilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public class FastaSplitsImporter extends CharactersFormat implements IToSplits, IImportSplits, IImportNoAutoDetect {

    public static final List<String> extensions = new ArrayList<>(Arrays.asList("fasta", "fas", "fa", "seq", "fsa", "fna"));
    private static int numberOfLinesToCheckInApplicable = 10;

    @Override
    public void parse(ProgressListener progressListener, String fileName, TaxaBlock taxa, SplitsBlock splits) throws CanceledException, IOException {

        final ArrayList<String> taxonNamesFound = new ArrayList<>();
        final ArrayList<String> binarySplits = new ArrayList<>();
        int ntax = 0;
        int nsplits = 0;
        int counter = 0;

        try (FileLineIterator it = new FileLineIterator(fileName)) {
            progressListener.setMaximum(it.getMaximumProgress());
            progressListener.setProgress(0);
            int currentSequenceLength = 0;
            StringBuilder currentSequence = new StringBuilder("");

            while (it.hasNext()) {
                final String line = it.next();

                counter++;
                if (line.startsWith(";") || line.isEmpty())
                    continue;
                if (line.equals(">"))
                    throw new IOExceptionWithLineNumber("No taxa label", counter);

                if (line.startsWith(">")) {
                    addTaxaName(line, taxonNamesFound, counter);
                    ntax++;

                    if (ntax > 1 && currentSequence.toString().isEmpty())
                        throw new IOExceptionWithLineNumber("No sequence is found", counter);

                    if (nsplits != 0 && nsplits != currentSequenceLength)
                        throw new IOExceptionWithLineNumber("Sequences must be the same length. " +
                                "Wrong number of chars. Length " + nsplits + " expected", counter - 1);

                    if (!currentSequence.toString().equals("")) binarySplits.add(currentSequence.toString());
                    nsplits = currentSequenceLength;
                    currentSequenceLength = 0;
                    currentSequence = new StringBuilder("");
                } else {
                    String allowedChars = "" + getMissing() + getMatchChar() + getGap();
                    checkIfCharactersValid(line, counter, allowedChars);
                    String add = line.replaceAll("\\s+", "");
                    currentSequenceLength += add.length();
                    currentSequence.append(add);
                }
                progressListener.setProgress(it.getProgress());
            }

            if (currentSequence.length() == 0)
                throw new IOExceptionWithLineNumber("Sequence " + ntax + " is zero", counter);
            binarySplits.add(currentSequence.toString());
            if (nsplits != currentSequenceLength)
                throw new IOExceptionWithLineNumber("Wrong number of chars at the line. Length " + nsplits + " expected", counter);

        }
        System.err.println("ntax: " + ntax + " nsplits: " + nsplits);
        for (String s : binarySplits) {
            System.err.println(s);
        }
        for (String s : taxonNamesFound) {
            System.err.println(s);
        }

        taxa.addTaxaByNames(taxonNamesFound);
        splits.clear();
        readSplits(ntax, nsplits, binarySplits, splits);
        splits.setCycle(SplitsUtilities.computeCycle(ntax, splits.getSplits()));
    }

    @Override
    public List<String> getExtensions() {
        return extensions;
    }

    @Override
    public boolean isApplicable(String fileName) throws IOException {

        int lineLength = 0;
        BufferedReader input = new BufferedReader(new FileReader(fileName));
        String line;
        int counter = 0;
        int seqCounter = 0;

        while ((line = input.readLine()) != null && counter <= numberOfLinesToCheckInApplicable) {
            counter++;

            // count all non-comment and not empty lines
            if (line.equals("") || line.startsWith(";"))
                continue;
            else
                seqCounter++;
            // even lines = taxa labels
            if (seqCounter % 2 == 1 && !line.startsWith(">"))
                return false;
            // odd lines = sequences
            if (seqCounter % 2 == 0) {
                // check if the same length
                if (lineLength == 0)
                    lineLength = line.length();
                else if (lineLength != line.length())
                    return false;
                // check if only 1/0
                for (char c : line.toCharArray()) {
                    if (c != '0' && c != '1') return false;
                }
            }
        }
        return counter != 0;
    }

    private static void readSplits(int ntax, int nsplits, ArrayList<String> binarySplits, SplitsBlock splitsBlock) {

        for (int s = 0; s < nsplits; s++) {
            BitSet A = new BitSet(ntax + 1);
            for (int t = 0; t < ntax; t++) {
                if (binarySplits.get(t).charAt(s) == '1') A.set(t + 1);
            }
            System.err.println("bitset: " + A);
            ASplit aSplit = new ASplit(A, ntax);
            splitsBlock.getSplits().add(aSplit);
        }
    }
}
