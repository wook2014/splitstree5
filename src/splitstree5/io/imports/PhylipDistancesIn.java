package splitstree5.io.imports;

import com.sun.istack.internal.Nullable;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.FileInputIterator;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.Taxon;
import splitstree5.io.imports.interfaces.IImportDistances;

import java.io.*;
import java.util.*;

/**
 * Daria Evseeva,02.10.2017.
 */
public class PhylipDistancesIn implements IToDistances, IImportDistances {

    public static final List<String> extensions = new ArrayList<>(Arrays.asList("dist", "dst"));

    /*
    todo :
    -----------------------------------------
    final ArrayList<String> taxonNamesFound = new ArrayList<>();
    final ArrayList<String> matrix = new ArrayList<>();
    -----------------------------------------
    try (FileInputIterator it = new FileInputIterator(inputFile))
    -----------------------------------------
    progressListener.setMaximum(it.getMaximumProgress());
    progressListener.setProgress(0);
            ...
    progressListener.setProgress(it.getProgress());
    -----------------------------------------
    while (it.hasNext()) {
                final String line = it.next();
    -----------------------------------------
     */

    @Override
    public void parse(ProgressListener progressListener, String inputFile, TaxaBlock taxa, DistancesBlock distances) throws CanceledException, IOException {
        taxa.clear();
        distances.clear();
        int ntax;

        // todo : try upper triangle; diagonal
        final Map<String, Vector<Double>> matrix = new LinkedHashMap<>();
        boolean square = true;
        //final double[][] matrix = new double[ntax][ntax];

        try (FileInputIterator it = new FileInputIterator(inputFile)) {

            progressListener.setMaximum(it.getMaximumProgress());
            progressListener.setProgress(0);
            int counter = 0;

            final String firstLine = it.next();
            counter++;
            //StringTokenizer st = new StringTokenizer(line);
            ntax = Integer.parseInt(firstLine.replaceAll(" ", ""));
            System.err.println(ntax);
            distances.setNtax(ntax);

            int tokensInPreviousRow = 0;
            int tokensInCurrentRow = 0;
            boolean newRow = false;
            Vector<Double> row;
            String currentLabel = "";
            boolean foundFirstLabel = false;

            while (it.hasNext()) {
                counter++;
                final String line = it.next();
                StringTokenizer st = new StringTokenizer(line);
                while (st.hasMoreTokens()) {

                    String token = st.nextToken();
                    if (!isNumeric(token)) {

                        if (tokensInCurrentRow > ntax || tokensInCurrentRow < tokensInPreviousRow)
                            throw  new IOException("line "+counter+": Wrong number of entries for Taxa "+currentLabel);
                        if (tokensInCurrentRow != tokensInPreviousRow && matrix.keySet().size() >= 2)
                            square = false;

                        foundFirstLabel = true;
                        System.err.println("curr " +tokensInCurrentRow + " pref "+tokensInPreviousRow);
                        tokensInPreviousRow = tokensInCurrentRow;
                        tokensInCurrentRow = 0;

                        row = new Vector<>();
                        matrix.put(token, row);
                        currentLabel = token;
                    } else {

                        if (!foundFirstLabel)
                            throw new IOException("line "+counter+": Taxa label expected");

                        tokensInCurrentRow++;
                        matrix.get(currentLabel).add(Double.parseDouble(token));
                    }
                }
                progressListener.setProgress(it.getProgress());
            }
        }
        System.err.println(square);
        for (String s : matrix.keySet()){
            System.err.println("Row " +s+" "+matrix.get(s));
        }

        taxa.addTaxaByNames(matrix.keySet());
        if (square)
            readSquareMatrix(matrix, distances);
        else
            readTriangularMatrix(matrix, distances);

            /*StreamTokenizer st = new StreamTokenizer(in);
            st.resetSyntax();
            st.eolIsSignificant(true);
            st.whitespaceChars(0, 32);
            st.wordChars(33, 126);
            st.nextToken();
            ntax = Integer.parseInt(st.sval);
            distances.setNtax(ntax);
            st.nextToken();
            for (int i = 1; i <= ntax; i++) {
                int shift = 0;
                int token = st.nextToken();
                if (token == StreamTokenizer.TT_EOL)
                    st.nextToken();
                Taxon taxon = new Taxon(st.sval);
                taxa.add(taxon);
                System.err.println(st.sval);
                for (int j = 1; j <= ntax; j++) {
                    token = st.nextToken();
                    if (token != StreamTokenizer.TT_EOL && token != StreamTokenizer.TT_EOF) {
                        distances.set(i, j - shift, Double.parseDouble(st.sval));
                        System.err.println(i + "--" + (j - shift) + "----------" + st.sval);
                    } else {
                        if (j >= i) {
                            st.nextToken();
                            if (isNumeric(st.sval)) {
                                st.pushBack();
                                shift++;
                                j--;
                            } else {
                                st.pushBack();
                                break;
                            }
                        }
                        if (j < i - 1) shift++;
                    }
                }
            }

            if (st.nextToken() != StreamTokenizer.TT_EOF) {
                throw new IOException("Unexpected symbol at line " + st.lineno());
            }
        }

        // todo make both or set triangular format?
        if (distances.get(1, ntax) != distances.get(ntax, 1))
            makeTriangularBoth(distances);*/
    }

    @Override
    public List<String> getExtensions() {
        return extensions;
    }

    @Override
    public boolean isApplicable(String fileName) throws IOException {
        String line = Basic.getFirstLineFromFile(new File(fileName));
        if (line == null) return false;

        StringTokenizer tokens = new StringTokenizer(line);
        return tokens.countTokens() == 1 && isNumeric(tokens.nextToken());
    }

    private static void readSquareMatrix(Map<String, Vector<Double>> matrix, DistancesBlock distancesBlock){
        int ntax = distancesBlock.getNtax();
        int taxaCounter = 0;
        for (String taxa : matrix.keySet()) {
            taxaCounter++;
            for (int j = 1; j <= ntax; j++) {
                distancesBlock.set(taxaCounter, j, matrix.get(taxa).get(j-1));
            }
        }
    }

    private static void readTriangularMatrix(Map<String, Vector<Double>> matrix, DistancesBlock distancesBlock){
        int ntax = distancesBlock.getNtax();
        int taxaCounter = 0;
        for (String taxa : matrix.keySet()) {
            taxaCounter++;
            for (int j = 1; j < taxaCounter; j++) {
                distancesBlock.set(taxaCounter, j, matrix.get(taxa).get(j-1));
                distancesBlock.set(j, taxaCounter, matrix.get(taxa).get(j-1));
            }
            distancesBlock.set(taxaCounter, taxaCounter, 0.0);
        }
    }

    private static void makeTriangularBoth(DistancesBlock distances) {
        int ntax = distances.getNtax();
        for (int i = 1; i <= ntax; i++) {
            for (int j = 1; j <= i; j++) {
                distances.set(j, i, distances.get(i, j));
            }
        }
    }

    private static boolean isNumeric(@Nullable String str) {
        if (str == null) return false;
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
