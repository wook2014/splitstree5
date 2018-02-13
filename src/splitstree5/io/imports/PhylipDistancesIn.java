package splitstree5.io.imports;

import com.sun.istack.internal.Nullable;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.FileInputIterator;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.imports.interfaces.IImportDistances;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Daria Evseeva,02.10.2017.
 */
public class PhylipDistancesIn implements IToDistances, IImportDistances {

    public static final List<String> extensions = new ArrayList<>(Arrays.asList("dist", "dst"));

    @Override
    public void parse(ProgressListener progressListener, String inputFile, TaxaBlock taxa, DistancesBlock distances) throws CanceledException, IOException {
        taxa.clear();
        distances.clear();
        int ntax;

        // todo : try upper triangle; diagonal
        final Map<String, Vector<Double>> matrix = new LinkedHashMap<>();
        boolean square = false;
        boolean triangular = false;
        boolean upperTriangular = false;
        boolean onlyOneMatrixFormIsFound = false;

        try (FileInputIterator it = new FileInputIterator(inputFile)) {

            progressListener.setMaximum(it.getMaximumProgress());
            progressListener.setProgress(0);
            int counter = 0;

            final String firstLine = it.next();
            counter++;
            ntax = Integer.parseInt(firstLine.replaceAll("\\s+", ""));
            System.err.println(ntax);
            distances.setNtax(ntax);

            int tokensInPreviousRow = 0;
            int tokensInCurrentRow = 0;
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

                        if (tokensInCurrentRow > ntax || !(square || triangular || upperTriangular))
                            throw new IOExceptionWithLineNumber("line " + counter +
                                    ": Wrong number of entries for Taxa " + currentLabel, counter);

                        if (tokensInCurrentRow == tokensInPreviousRow && matrix.keySet().size() >= 2) // when 2 lines are read
                            square = true;
                        if (tokensInCurrentRow == tokensInPreviousRow + 1 && matrix.keySet().size() >= 2) // when 2 lines are read
                            triangular = true;
                        if (tokensInCurrentRow == tokensInPreviousRow - 1 && matrix.keySet().size() >= 2) // when 2 lines are read
                            upperTriangular = true;

                        // only one variable is set to 1
                        onlyOneMatrixFormIsFound = !(square & triangular & upperTriangular) &
                                (square ^ triangular ^ upperTriangular);

                        foundFirstLabel = true;
                        System.err.println("curr " + tokensInCurrentRow + " pref " + tokensInPreviousRow);
                        tokensInPreviousRow = tokensInCurrentRow;
                        tokensInCurrentRow = 0;

                        row = new Vector<>();
                        matrix.put(token, row);
                        currentLabel = token;
                    } else {

                        if (!foundFirstLabel)
                            throw new IOExceptionWithLineNumber("line " + counter + ": Taxa label expected", counter);

                        tokensInCurrentRow++;
                        matrix.get(currentLabel).add(Double.parseDouble(token));
                    }
                    progressListener.setProgress(it.getProgress());
                }
            }
        }
        System.err.println(square);
        for (String s : matrix.keySet()) {
            System.err.println("Row " + s + " " + matrix.get(s));
        }

        System.err.println(square+" "+triangular+" "+upperTriangular);
        taxa.addTaxaByNames(matrix.keySet());
        if (square)
            readSquareMatrix(matrix, distances);
        else
            readTriangularMatrix(matrix, distances);
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

    private static void readSquareMatrix(Map<String, Vector<Double>> matrix, DistancesBlock distancesBlock) {
        int ntax = distancesBlock.getNtax();
        int taxaCounter = 0;
        for (String taxa : matrix.keySet()) {
            taxaCounter++;
            for (int j = 1; j <= ntax; j++) {
                distancesBlock.set(taxaCounter, j, matrix.get(taxa).get(j - 1));
            }
        }
    }

    private static void readTriangularMatrix(Map<String, Vector<Double>> matrix, DistancesBlock distancesBlock) {
        int ntax = distancesBlock.getNtax();
        int taxaCounter = 0;
        for (String taxa : matrix.keySet()) {
            taxaCounter++;
            for (int j = 1; j < taxaCounter; j++) {
                distancesBlock.set(taxaCounter, j, matrix.get(taxa).get(j - 1));
                distancesBlock.set(j, taxaCounter, matrix.get(taxa).get(j - 1));
            }
            distancesBlock.set(taxaCounter, taxaCounter, 0.0);
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
