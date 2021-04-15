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
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import jloda.util.FileLineIterator;
import jloda.util.PeakMemoryUsageMonitor;
import jloda.util.ProgramProperties;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.Queue;

/**
 * a taxonomy tool
 * Daniel Huson, 8.2020
 */
public class TaxonomyTool {
    /**
     * main
     */
    public static void main(String[] args) {
        try {
            ProgramProperties.setProgramName("TaxonomyTool");
            ProgramProperties.setProgramVersion(splitstree5.main.Version.SHORT_DESCRIPTION);

            PeakMemoryUsageMonitor.start();
            (new TaxonomyTool()).run(args);
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
        final ArgsOptions options = new ArgsOptions(args, this.getClass(), "Simple tools for taxonomy processing");
        options.setVersion(ProgramProperties.getProgramVersion());
        options.setLicense("This is free software, licensed under the terms of the GNU General Public License, Version 3.");
        options.setAuthors("Daniel H. Huson");

        final String command = options.getCommand("node-parent");

        final String treeFile = options.getOptionMandatory("-i", "input", "File containing a newick tree  (stdin or .gz ok)", "");
        final String outputFile = options.getOption("-o", "output", "Output file (stdout or .gz ok)", "stdout");
        options.done();

        final String newickStr;
        try (final FileLineIterator it = new FileLineIterator(treeFile, true)) {
            final StringBuilder buf = new StringBuilder();
            while (it.hasNext()) {
                final String next = it.next().trim();
                buf.append(next);
                if (next.endsWith(";"))
                    break;
            }
            newickStr = buf.toString();
        }
        if (command.equalsIgnoreCase("node-parent")) {
            final PhyloTree tree = new PhyloTree();
            tree.parseBracketNotation(newickStr, true);
            if (tree.getRoot() == null)
                throw new IOException("No tree found");
            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(Basic.getOutputStreamPossiblyZIPorGZIP(outputFile)))) {
                System.err.println("Writing to: " + outputFile);
                int count = 0;
                final Queue<Node> queue = new LinkedList<>();
                queue.add(tree.getRoot());
                while (queue.size() > 0) {
                    final Node v = queue.poll();
                    final Node p = v.getParent();
                    w.write(tree.getLabel(v) + "\t" + (p == null ? 0 : tree.getLabel(p)) + "\n");
                    queue.addAll(Basic.asList(v.children()));
                    count++;
                }
                System.err.printf("Lines: %,d%n", count);
            }
        }
    }
}
