/*
 *  PhylipDistancesImporter.java Copyright (C) 2019 Daniel H. Huson
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

import jloda.fx.util.NotificationManager;
import jloda.util.*;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.imports.interfaces.IImportDistances;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Phylip matrix input
 * Daria Evseeva,02.10.2017.
 */
public class PhylipDistancesImporter implements IToDistances, IImportDistances {

    public static final List<String> extensions = new ArrayList<>(Arrays.asList("dist", "dst"));

    public enum Triangle {Both, Lower, Upper}

    @Override
    public void parse(ProgressListener progressListener, String inputFile, TaxaBlock taxa, DistancesBlock distances) throws CanceledException, IOException {
        taxa.clear();
        distances.clear();

        final Map<String, Vector<Double>> matrix = new LinkedHashMap<>();
        Triangle triangleForCurrentRow = null;
        Triangle triangleForPreviousRow = null;

        try (FileInputIterator it = new FileInputIterator(inputFile)) {

            progressListener.setMaximum(it.getMaximumProgress());
            progressListener.setProgress(0);
            int counter = 0;

            final String firstLine = it.next();
            counter++;
            final int ntax = Integer.parseInt(firstLine.replaceAll("\\s+", ""));
            //System.err.println(ntax);
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
                    if (!Basic.isDouble(token)) {
                        foundFirstLabel = true;

                        if (tokensInCurrentRow > ntax)
                            throw new IOExceptionWithLineNumber("Wrong number of entries for Taxa " + currentLabel, counter);

                        // when 2 lines are read
                        if (matrix.keySet().size() >= 2) {
                            int differenceOfLines = tokensInCurrentRow - tokensInPreviousRow;
                            switch (differenceOfLines) {
                                case 0:
                                    triangleForCurrentRow = Triangle.Both;
                                    break;
                                case 1:
                                    triangleForCurrentRow = Triangle.Lower;
                                    break;
                                case -1:
                                    triangleForCurrentRow = Triangle.Upper;
                                    break;
                                default:
                                    throw new IOExceptionWithLineNumber("Wrong number of entries for Taxa " + currentLabel, counter);
                            }
                        }

                        if (triangleForPreviousRow != null && !triangleForCurrentRow.equals(triangleForPreviousRow)) {
                            throw new IOExceptionWithLineNumber("Wrong number of entries for Taxa " + currentLabel, counter);
                        } else {
                            triangleForPreviousRow = triangleForCurrentRow;
                        }

                        // System.err.println("curr " + tokensInCurrentRow + " pref " + tokensInPreviousRow);
                        tokensInPreviousRow = tokensInCurrentRow;
                        tokensInCurrentRow = 0;
                        currentLabel = addTaxaName(matrix, token, counter);
                    } else {

                        if (!foundFirstLabel)
                            throw new IOExceptionWithLineNumber("Taxa label expected", counter);

                        tokensInCurrentRow++;
                        matrix.get(currentLabel).add(Double.parseDouble(token));
                    }
                    progressListener.setProgress(it.getProgress());
                }
            }
        }

        if (false) {
            for (String s : matrix.keySet()) {
                System.err.println("Row " + s + " " + Basic.toString(matrix.get(s), " "));
            }
        }
        taxa.addTaxaByNames(matrix.keySet());
        if (triangleForCurrentRow != null) {
            if (triangleForCurrentRow.equals(Triangle.Both))
                readSquareMatrix(matrix, distances);
            if (triangleForCurrentRow.equals(Triangle.Lower))
                readTriangularMatrix(matrix, distances);
            if (triangleForCurrentRow.equals(Triangle.Upper))
                readUpperTriangularMatrix(matrix, distances);
        } else {
            throw new IOException("Error: Cannot detect shape of matrix (square, triangular or upper-triangular?)");
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

        final StringTokenizer tokens = new StringTokenizer(line);
        return tokens.countTokens() == 1 && Basic.isInteger(tokens.nextToken());
    }

    /**
     * read a square matrix
     *
     * @param matrix
     * @param distancesBlock
     */
    private static void readSquareMatrix(Map<String, Vector<Double>> matrix, DistancesBlock distancesBlock) {
        boolean similarities = false;

        int t1 = 0;
        for (String taxa : matrix.keySet()) {
            t1++;
            for (int t2 = 1; t2 <= distancesBlock.getNtax(); t2++) {
                double value = matrix.get(taxa).get(t2 - 1);
                if (t1 == 1 && t2 == 1 && value == 1) {
                    NotificationManager.showInformation("First dialog value is 1, assuming input values are similarities, using -log(value)");
                    similarities = true;
                }
                if (!similarities)
                    distancesBlock.set(t1, t2, value);
                else {
                    if (value == 0)
                        value = 0.00000001; // small number
                    distancesBlock.set(t1, t2, Math.max(0, -Math.log(value)));
                }
            }
        }
    }

    /**
     * read a lower triangular matrix
     *
     * @param matrix
     * @param distancesBlock
     */
    private static void readTriangularMatrix(Map<String, Vector<Double>> matrix, DistancesBlock distancesBlock) {
        int t1 = 0;
        for (String taxa : matrix.keySet()) {
            t1++;
            for (int t2 = 1; t2 < t1; t2++) {
                distancesBlock.set(t1, t2, matrix.get(taxa).get(t2 - 1));
                distancesBlock.set(t2, t1, matrix.get(taxa).get(t2 - 1));
            }
            distancesBlock.set(t1, t1, 0.0);
        }
    }

    /**
     * read an upper triangular matrix
     *
     * @param matrix
     * @param distancesBlock
     */
    private static void readUpperTriangularMatrix(Map<String, Vector<Double>> matrix, DistancesBlock distancesBlock) {
        int t1 = 0;
        // length of the first line - ntax
        int diff = matrix.entrySet().iterator().next().getValue().size() - distancesBlock.getNtax() + 1;
        for (String taxa : matrix.keySet()) {
            t1++;
            for (int t2 = matrix.get(taxa).size(); t2 > 0; t2--) {
                int positionInSquareMatrix = t2 + t1 - diff;
                distancesBlock.set(t1, positionInSquareMatrix, matrix.get(taxa).get(t2 - 1));
                distancesBlock.set(positionInSquareMatrix, t1, matrix.get(taxa).get(t2 - 1));
            }
            distancesBlock.set(t1, t1, 0.0);
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
}
