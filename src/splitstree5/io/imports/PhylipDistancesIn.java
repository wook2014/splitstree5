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

    public enum MatrixType {square, triangular, upperTriangular}

    @Override
    public void parse(ProgressListener progressListener, String inputFile, TaxaBlock taxa, DistancesBlock distances) throws CanceledException, IOException {
        taxa.clear();
        distances.clear();
        int ntax;

        final Map<String, Vector<Double>> matrix = new LinkedHashMap<>();
        MatrixType matrixTypeForCurrentRow = null;
        MatrixType matrixTypeForPreviousRow = null;

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
            String currentLabel = "";
            boolean foundFirstLabel = false;

            while (it.hasNext()) {
                counter++;
                final String line = it.next();
                StringTokenizer st = new StringTokenizer(line);
                while (st.hasMoreTokens()) {

                    String token = st.nextToken();
                    if (!isNumeric(token)) {
                        foundFirstLabel = true;

                        if (tokensInCurrentRow > ntax)
                            throw new IOExceptionWithLineNumber("line " + counter +
                                    ": Wrong number of entries for Taxa " + currentLabel, counter);

                        // when 2 lines are read
                        if (matrix.keySet().size() >= 2) {
                            int differenceOfLines = tokensInCurrentRow - tokensInPreviousRow;
                            switch (differenceOfLines) {
                                case 0 :
                                    matrixTypeForCurrentRow = MatrixType.square;
                                    break;
                                case 1 :
                                    matrixTypeForCurrentRow = MatrixType.triangular;
                                    break;
                                case -1 :
                                    matrixTypeForCurrentRow = MatrixType.upperTriangular;
                                    break;
                                default:
                                    throw new IOExceptionWithLineNumber("line " + counter +
                                        ": Wrong number of entries for Taxa " + currentLabel, counter);
                            }
                        }

                        if (matrixTypeForPreviousRow != null && !matrixTypeForCurrentRow.equals(matrixTypeForPreviousRow)) {
                            throw new IOExceptionWithLineNumber("line " + counter +
                                    ": Wrong number of entries for Taxa " + currentLabel, counter);
                        } else {
                            matrixTypeForPreviousRow = matrixTypeForCurrentRow;
                        }

                        System.err.println("curr " + tokensInCurrentRow + " pref " + tokensInPreviousRow);
                        tokensInPreviousRow = tokensInCurrentRow;
                        tokensInCurrentRow = 0;

                        currentLabel = addTaxaName(matrix, token, counter);
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

        for (String s : matrix.keySet()) {
            System.err.println("Row " + s + " " + matrix.get(s));
        }
        taxa.addTaxaByNames(matrix.keySet());
        if (matrixTypeForCurrentRow != null) {
            if (matrixTypeForCurrentRow.equals(MatrixType.square))
                readSquareMatrix(matrix, distances);
            if (matrixTypeForCurrentRow.equals(MatrixType.triangular))
                readTriangularMatrix(matrix, distances);
            if (matrixTypeForCurrentRow.equals(MatrixType.upperTriangular))
                readUpperTriangularMatrix(matrix, distances);
        } else {
            throw new IOException("Error: Cannot estimate matrix form! (square, triangular or upper-triangular)");
        }

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

    private static void readUpperTriangularMatrix(Map<String, Vector<Double>> matrix, DistancesBlock distancesBlock) {
        int taxaCounter = 0;
        // length of the first line - ntax
        int diff = matrix.entrySet().iterator().next().getValue().size() - distancesBlock.getNtax() + 1;
        for (String taxa : matrix.keySet()) {
            taxaCounter++;
            for (int j = matrix.get(taxa).size(); j > 0; j--) {
                int positionInSquareMatrix = j + taxaCounter - diff;
                distancesBlock.set(taxaCounter, positionInSquareMatrix, matrix.get(taxa).get(j - 1));
                distancesBlock.set(positionInSquareMatrix, taxaCounter, matrix.get(taxa).get(j - 1));
            }
            distancesBlock.set(taxaCounter, taxaCounter, 0.0);
        }
    }

    private static String addTaxaName(Map<String, Vector<Double>> matrix, String label, int linesCounter) {
        int sameNamesCounter = 0;
        if (matrix.keySet().contains(label)) {
            System.err.println("Repeating taxon name in line " + linesCounter);
            sameNamesCounter++;
        }
        while (matrix.keySet().contains(label + "(" + sameNamesCounter + ")")) {
            sameNamesCounter++;
        }

        if (sameNamesCounter == 0) {
            matrix.put(label, new Vector<>());
            return label;
        } else {
            matrix.put(label + "(" + sameNamesCounter + ")", new Vector<>());
            return label + "(" + sameNamesCounter + ")";
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
