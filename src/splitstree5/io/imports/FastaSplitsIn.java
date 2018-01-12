package splitstree5.io.imports;

import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ASplit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;

public class FastaSplitsIn implements IToSplits{

    public void parse(String inputFile, TaxaBlock taxa, SplitsBlock splits) throws IOException {

        ArrayList<String> taxonNamesFound = new ArrayList<>();
        ArrayList<String> binarySplits = new ArrayList<>();
        int ntax = 0;
        int nsplits = 0;
        int counter = 0;

        try (BufferedReader in = new BufferedReader(new FileReader(inputFile))) {
            counter++;
            String line;
            int sequenceLength = 0;
            StringBuilder sequence = new StringBuilder("");
            boolean startedNewSequence = false;

            while ((line = in.readLine()) != null) {
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
        //characters.setDimension(ntax, nchar);
        //readMatrix(matrix, characters);
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
