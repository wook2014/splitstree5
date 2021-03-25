/*
 * PhylipDistancesImporter.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package splitstree5.io.imports;

import jloda.util.*;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.imports.interfaces.IImportDistances;
import splitstree5.io.imports.utils.DistanceSimilarityCalculator;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Phylip matrix input
 * Daria Evseeva, 02.10.2017, Daniel Huson, 3.2020
 */
public class PhylipDistancesImporter implements IToDistances, IImportDistances {

    private boolean similarities = false;
    private String similarities_calculation = "";

    public static final List<String> extensions = new ArrayList<>(Arrays.asList("dist", "dst"));

    public enum Triangle {Both, Lower, Upper}

    @Override
    public void parse(ProgressListener progressListener, String inputFile, TaxaBlock taxa, DistancesBlock distances) throws CanceledException, IOException {
         Triangle triangle = null;
        int row = 0;
        int numberOfTaxa = 0;

        try (FileLineIterator it = new FileLineIterator(inputFile)) {
            while (it.hasNext()) {
                final String line = it.next().trim();

                if (line.startsWith("#") || line.length() == 0)
                    continue;
                if (row == 0) {
                    numberOfTaxa = Integer.parseInt(line);
                    distances.setNtax(numberOfTaxa);
                } else {
                    final String[] tokens = line.split("\\s+");
                    if (row == 1) {
                        if (tokens.length == 1)
                            triangle = Triangle.Lower;
                        else if (tokens.length == numberOfTaxa)
                            triangle = Triangle.Upper;
                        else if (tokens.length == numberOfTaxa + 1)
                            triangle = Triangle.Both;
                        else
                            throw new IOExceptionWithLineNumber(it.getLineNumber(), "Matrix has wrong shape");
                    }

                    if (row > numberOfTaxa)
                        throw new IOExceptionWithLineNumber(it.getLineNumber(), "Matrix has wrong shape");

                    if (triangle == Triangle.Both) {
                        if (tokens.length != numberOfTaxa + 1)
                            throw new IOExceptionWithLineNumber(it.getLineNumber(), "Matrix has wrong shape");
                        taxa.addTaxaByNames(Collections.singleton(tokens[0]));
                        for (int col = 1; col < tokens.length; col++) {
                            final double value = Basic.parseDouble(tokens[col]);
                            distances.set(row, col, value);
                        }
                    } else if (triangle == Triangle.Upper) {
                        if (tokens.length != numberOfTaxa + 1 - row)
                            throw new IOExceptionWithLineNumber(it.getLineNumber(), "Matrix has wrong shape");
                        taxa.addTaxaByNames(Collections.singleton(tokens[0]));
                        for (int i = 1; i < tokens.length; i++) {
                            final int col = row + i;
                            final double value = Basic.parseDouble(tokens[i]);
                            distances.set(row, col, value);
                            distances.set(col, row, value);
                        }
                    } else if (triangle == Triangle.Lower) {
                        if (tokens.length != row)
                            throw new IOExceptionWithLineNumber(it.getLineNumber(), "Matrix has wrong shape");
                        taxa.addTaxaByNames(Collections.singleton(tokens[0]));
                        for (int col = 1; col < tokens.length; col++) {
                            final double value = Basic.parseDouble(tokens[col]);
                            distances.set(row, col, value);
                            distances.set(col, row, value);
                        }
                    }
                }
                row++;
            }
        }

        if (similarities) {
            invertDistMatrixValues(distances);
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

    private void invertDistMatrixValues(DistancesBlock distances) {

        EnumSet<DistanceSimilarityCalculator> distanceSimilarityCalculators =
                EnumSet.allOf(DistanceSimilarityCalculator.class);
        DistanceSimilarityCalculator dsc = DistanceSimilarityCalculator.log;
        for (DistanceSimilarityCalculator d : distanceSimilarityCalculators)
            if (d.getLabel().equals(this.similarities_calculation)) {
                dsc = d;
                break;
            }

        for (int i = 1; i <= distances.getNtax(); i++) {
            for (int j = 1; j <= distances.getNtax(); j++) {
                // set 0 to the main diagonal
                if (i == j)
                    distances.set(i, j, 0);
                else
                    distances.set(i, j, dsc.applyAsDouble(distances.get(i, j)));
            }
        }
    }

    public void setSimilarities(boolean similarities) {
        this.similarities = similarities;
    }

    @Override
    public void setSimilaritiesCalculation(String similaritiesCalculation) {
        this.similarities_calculation = similaritiesCalculation;
    }
}
