package splitstree5.io.imports;

import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.FileInputIterator;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.io.imports.interfaces.IImportSplits;
import splitstree5.utils.SplitsUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public class FastaSplitsIn extends CharactersFormat implements IToSplits, IImportSplits {

    public static final List<String> extensions = new ArrayList<>(Arrays.asList("fasta", "fas", "fa", "seq", "fsa", "fna"));
    private static int numberOfLinesToCheckInApplicable = 10;

    @Override
    public void parse(ProgressListener progressListener, String fileName, TaxaBlock taxa, SplitsBlock splits) throws CanceledException, IOException {

        final ArrayList<String> taxonNamesFound = new ArrayList<>();
        final ArrayList<String> binarySplits = new ArrayList<>();
        int ntax = 0;
        int nsplits = 0;
        int counter = 0;

        try (FileInputIterator it = new FileInputIterator(fileName)) {
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
                    throw new IOExceptionWithLineNumber("No taxa label given at line: " + counter, counter);

                if (line.startsWith(">")) {
                    addTaxaName(line, taxonNamesFound, counter);
                    ntax++;

                    if (ntax > 1 && currentSequence.toString().isEmpty())
                        throw new IOExceptionWithLineNumber("No sequence is given at line " + counter, counter);

                    if (nsplits != 0 && nsplits != currentSequenceLength)
                        throw new IOExceptionWithLineNumber("Sequences must be the same length. " +
                                "Wrong number of chars at the line: " + (counter - 1) + ". Length " + nsplits + " expected", counter - 1);

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
                throw new IOExceptionWithLineNumber("Line: "+counter+": Sequence " + ntax + " is zero", counter);
            binarySplits.add(currentSequence.toString());
            if (nsplits != currentSequenceLength)
                throw new IOExceptionWithLineNumber("Wrong number of chars at the line: " + counter + ". Length " + nsplits + " expected", counter);

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
            counter ++;

            // count all non-comment and not empty lines
            if (line.equals("") || line.startsWith(";"))
                continue;
            else
                seqCounter ++;
            // even lines = taxa labels
            if (seqCounter % 2 == 1 && !line.startsWith(">"))
                return false;
            // odd lines = sequences
            if (seqCounter % 2 == 0){
                // check if the same length
                if (lineLength == 0)
                    lineLength = line.length();
                else if (lineLength != line.length())
                    return false;
                // check if only 1/0
                for (char c : line.toCharArray()) {
                    if (c != '0' && c!='1') return false;
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
