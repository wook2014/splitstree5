/*
 *  Copyright (C) 2019 Daniel H. Huson
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
import splitstree5.io.imports.interfaces.IImportNoAutoDetect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * text matrix input
 * Daniel Huson, 3.2018
 */
public class TextDistancesImporter implements IToDistances, IImportDistances, IImportNoAutoDetect {
    public static final List<String> extensions = new ArrayList<>(Collections.singletonList("txt"));

    @Override
    public void parse(ProgressListener progressListener, String inputFile, TaxaBlock taxa, DistancesBlock distances) throws CanceledException, IOException {
        taxa.clear();
        distances.clear();

        try (FileInputIterator it = new FileInputIterator(inputFile)) {
            boolean first = true;

            int ntax = 0;

            String[] labels = null;
            final ArrayList<String> taxonNames = new ArrayList<>();

            boolean similarities = false;

            int t1 = 1;

            while (it.hasNext()) {
                final String line = it.next();
                final String[] tokens = line.split("[\t,;]");
                if (tokens.length == 0)
                    continue;


                if (first) {
                    first = false;
                    if (tokens.length == 1 && Basic.isInteger(tokens[0])) {
                        // single number that indicates dataset size
                        ntax = Basic.parseInt((tokens[0]));
                        distances.setNtax(ntax);
                        continue;
                    } else {
                        if (!Basic.isDouble(tokens[1])) {
                            // first line are labels
                            labels = tokens;
                            ntax = tokens.length - 1;
                            distances.setNtax(ntax);
                            continue;
                        } else {
                            // first line contains distances
                            ntax = tokens.length - 1;
                            distances.setNtax(ntax);
                        }
                    }
                }
                final String name = tokens[0];
                if (labels != null && !labels[t1].equals(name))
                    throw new IOExceptionWithLineNumber("Expected label '" + labels[t1] + "'", (int) it.getLineNumber());
                taxonNames.add(name);
                for (int t2 = 1; t2 <= ntax; t2++) {
                    if (!Basic.isDouble(tokens[t2]))
                        throw new IOExceptionWithLineNumber("Number expected", (int) it.getLineNumber());
                    double value = Basic.parseDouble(tokens[t2]);
                    if (t1 == 1 && t2 == 1 && value == 1) {
                        NotificationManager.showInformation("First dialog value is 1, assuming input values are similarities, using -log(value)");
                        similarities = true;
                    }
                    if (!similarities)
                        distances.set(t1, t2, value);
                    else {
                        if (value == 0)
                            value = 0.00000001; // small number
                        distances.set(t1, t2, Math.max(0, -Math.log(value)));
                    }
                }
                t1++;
            }
            taxa.addTaxaByNames(taxonNames);
        } catch (Exception ex) {
            Basic.caught(ex);
        }
    }


    @Override
    public List<String> getExtensions() {
        return extensions;
    }

    @Override
    public boolean isApplicable(String fileName) throws IOException {
        return true;
    }

}
