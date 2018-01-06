package splitstree5.io.imports;

import splitstree5.core.datablocks.TaxaBlock;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;

public class MatlabIn {

    public void parseTaxa(String inputFile, TaxaBlock taxa) throws IOException {

        ArrayList<String> taxonNamesFound = new ArrayList<>();
        int ntax = 0;
        int counter = 1;

        try (BufferedReader in = new BufferedReader(new FileReader(inputFile))) {
            counter++;
            /*String line;
            int sequenceLength = 0;
            StringBuilder sequence = new StringBuilder("");
            boolean startedNewSequence = false;*/

            if (!in.readLine().equalsIgnoreCase("%%MATLAB%%"))
                throw new IOException("%%MATLAB%% header expected in the first line");

            /*while ((line = in.readLine()) != null) {
                counter++;

                if (line.startsWith("%"))
                    continue;
                if (line.equals(">"))
                    throw new IOException("No taxa label given at the sequence " + (ntax + 1) + " in line: " + counter);

            }*/
            StreamTokenizer st = new StreamTokenizer(in);
            st.resetSyntax();
            st.eolIsSignificant(false);
            st.whitespaceChars(0, 32);
            st.wordChars(33, 126);
            st.commentChar('%');

            st.nextToken();
            ntax = Integer.parseInt(st.sval);
            //ntax = (int) st.nval;
            for (int i = 0; i < ntax; i++) {
                st.nextToken();
                taxonNamesFound.add(st.sval);
            }
        }

        taxa.addTaxaByNames(taxonNamesFound);
    }
}
