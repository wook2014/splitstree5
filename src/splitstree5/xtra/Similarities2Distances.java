/*
 * Similarities2Distances.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.xtra;

import jloda.fx.util.ArgsOptions;
import jloda.util.*;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.DistancesNexusOutput;
import splitstree5.io.nexus.TaxaNexusOutput;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * convert list of pairwise similarities to matrix of distances
 */
public class Similarities2Distances {
    /**
     * sort last MAF alignments
     *
     * @param args
     * @throws UsageException
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws Exception {
        try {
            ProgramProperties.setProgramName("Similarities2Distances");
            ProgramProperties.setProgramVersion(splitstree5.main.Version.SHORT_DESCRIPTION);

            PeakMemoryUsageMonitor.start();
            (new Similarities2Distances()).run(args);
            System.err.println("Total time:  " + PeakMemoryUsageMonitor.getSecondsSinceStartString());
            System.err.println("Peak memory: " + PeakMemoryUsageMonitor.getPeakUsageString());
            System.exit(0);
        } catch (Exception ex) {
            Basic.caught(ex);
            System.exit(1);
        }
    }

    /**
     * run the program
     *
     * @param args
     */
    public void run(String[] args) throws Exception {
        final ArgsOptions options = new ArgsOptions(args, this.getClass(), "Converts a list of similarities to a matrix of distances");
        options.setVersion(ProgramProperties.getProgramVersion());
        options.setLicense("This is free software, licensed under the terms of the GNU General Public License, Version 3.");
        options.setAuthors("Daniel H. Huson");

        options.comment("Input output");
        final String inputFile = options.getOptionMandatory("-i", "input", "Input file", "");
        final String outputFile = options.getOption("-o", "output", "Output file (stdout for console output)", "stdout");
        options.done();

        final Map<String, Map<String, Double>> matrix = new HashMap<>();

        try (FileLineIterator it = new FileLineIterator(inputFile, true)) {
            while (it.hasNext()) {
                final String[] tokens = it.next().split("\t");
                if (tokens.length != 3)
                    throw new IOExceptionWithLineNumber(it.getLineNumber(), "Expected 3 tokens, got: " + StringUtils.toString(tokens, ","));
                final String a = tokens[0];
                final String b = tokens[1];
                final double value;
                if (tokens[2].contains("/")) {
                    final double p = Double.parseDouble(tokens[2].substring(0, tokens[2].indexOf('/')).trim());
                    final double q = Double.parseDouble(tokens[2].substring(tokens[2].indexOf('/') + 1, tokens[2].length()).trim());
                    value = p / q;
                } else
                    value = Double.parseDouble(tokens[2]);
                {
                    Map<String, Double> row = matrix.computeIfAbsent(a, k -> new HashMap<>());
                    row.put(b, value);
                }
                {
                    Map<String, Double> row = matrix.computeIfAbsent(b, k -> new HashMap<>());
                    row.put(a, value);
                }
            }
        }

        double maxValue = 0;
        for (String a : matrix.keySet()) {
            final Map<String, Double> row = matrix.get(a);
            for (String b : row.keySet()) {
                maxValue = Math.max(maxValue, row.get(b));
            }
        }

        final TaxaBlock taxaBlock = new TaxaBlock();

        taxaBlock.addTaxaByNames(matrix.keySet());

        final DistancesBlock distancesBlock = new DistancesBlock();
        distancesBlock.setNtax(taxaBlock.getNtax());
        for (String a : matrix.keySet()) {
            final Map<String, Double> row = matrix.get(a);
            for (String b : row.keySet()) {
                if (!a.equals(b))
                    distancesBlock.set(taxaBlock.indexOf(a), taxaBlock.indexOf(b), -Math.log(row.get(b) / maxValue));
            }
        }


        try (BufferedWriter w = new BufferedWriter(outputFile.equals("stdout") ? new BufferedWriter(new OutputStreamWriter(System.out)) : new FileWriter(outputFile))) {
            w.write("#nexus\n");
            (new TaxaNexusOutput()).write(w, taxaBlock);
            (new DistancesNexusOutput()).write(w, taxaBlock, distancesBlock);
        }

    }
}
