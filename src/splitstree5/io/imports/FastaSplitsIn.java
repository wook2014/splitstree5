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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public class FastaSplitsIn implements IToSplits, IImportSplits{

    public static final List<String> extensions = new ArrayList<>(Arrays.asList("fasta", "fas", "fa", "seq", "fsa", "fna"));

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
            counter++;
            int sequenceLength = 0;
            StringBuilder sequence = new StringBuilder("");
            boolean startedNewSequence = false;

            while (it.hasNext()) {
                final String line = it.next();
                counter++;

                if (line.startsWith(";"))
                    continue;
                if (line.equals(">"))
                    throw new IOException("No taxa label given at the sequence " + (ntax + 1) + " in line: " + counter);

                if (line.startsWith(">")) {
                    startedNewSequence = true;
                    addTaxaName(line, taxonNamesFound, counter);
                    if (taxonNamesFound.contains(line.substring(1))) {
                        System.err.println("");
                    }
                    ntax++;
                } else {
                    if (startedNewSequence) {
                        if (!sequence.toString().equals("")) binarySplits.add(sequence.toString());
                        if (nsplits != 0 && nsplits != sequenceLength) {
                            throw new IOException("Sequences must be the same length. Wrong number of chars at the sequence "
                                    + (ntax - 1) + " in line: " + counter);
                        }
                        nsplits = sequenceLength;
                        sequenceLength = 0;
                        sequence = new StringBuilder("");
                        startedNewSequence = false;
                    }
                    String add = line.replaceAll("\\s+", "");
                    sequenceLength += add.length();
                    sequence.append(line.replaceAll("\\s+", ""));
                }
                progressListener.setProgress(it.getProgress());
            }

            if (sequence.length() == 0)
                throw new IOException("Sequence " + ntax + " is zero");
            binarySplits.add(sequence.toString());
            if (nsplits != sequenceLength)
                throw new IOException("Sequences must be the same length. Wrong number of chars at the sequence " + ntax);

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
        String line = Basic.getFirstLineFromFileIgnoreEmptyLines(new File(fileName), ";", 1000);
        return line != null && line.startsWith(">");
    }

    private static void readSplits( int ntax, int nsplits, ArrayList<String> binarySplits, SplitsBlock splitsBlock){

        for (int s = 0; s < nsplits; s++){
            BitSet A = new BitSet(ntax+1);
            for (int t = 0; t < ntax; t++){
                if (binarySplits.get(t).charAt(s) == '1') A.set(t+1);
            }
            System.err.println("bitset: "+A);
            ASplit aSplit = new ASplit(A, ntax);
            splitsBlock.getSplits().add(aSplit);
        }
    }

    private static void addTaxaName(String line, ArrayList<String> taxonNames, int linesCounter){

        int sameNamesCounter = 0;
        if (taxonNames.contains(line.substring(1))) {
            System.err.println("Repeating taxon name in line "+linesCounter);
            sameNamesCounter++;
        }
        while (taxonNames.contains(line.substring(1)))
            sameNamesCounter++;

        if (sameNamesCounter == 0)
            taxonNames.add(line.substring(1));
        else
            taxonNames.add(line.substring(1)+sameNamesCounter);
    }
}
