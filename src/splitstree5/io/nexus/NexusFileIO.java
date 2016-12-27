/*
 *  Copyright (C) 2016 Daniel H. Huson
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

package splitstree5.io.nexus;

import jloda.util.parse.NexusStreamParser;
import splitstree5.core.Document;
import splitstree5.core.algorithms.Report;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.filters.TaxaFilter;
import splitstree5.core.misc.Taxon;
import splitstree5.core.topfilters.DistancesTopFilter;
import splitstree5.gui.TaxaFilterView;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Read/write a file in nexus format
 * Created by huson on 12/27/16.
 */
public class NexusFileIO {
    /**
     * parses a document in nexus format
     *
     * @param document
     * @throws IOException
     */
    public static void parse(Document document) throws IOException {
        try (NexusStreamParser np = new NexusStreamParser(new FileReader(document.getFileName()))) {
            System.err.println("Loading file: " + document.getFileName());
            parse(document, np);
            System.err.println("done");
        }
    }

    /**
     * parses nexus and loads it into the document
     *
     * @param np
     * @return
     * @throws IOException
     */
    public static void parse(Document document, NexusStreamParser np) throws IOException {
        np.matchIgnoreCase("#nexus");

        if (np.peekMatchIgnoreCase("begin splitstree5;")) { // this is a complete file written by splitstree5
            System.err.println("Not implemented");
        } else { // this is a user input file
            document.setOriginalDataNode(null);

            final TaxaBlock originalTaxaBlock = new TaxaBlock("OriginalTaxa");
            document.setOriginalTaxaNode(new ADataNode<>(document, originalTaxaBlock));

            boolean needToDetectTaxa = true;

            if (np.peekMatchIgnoreCase("begin taxa;")) {
                final TaxaNexusIO io = new TaxaNexusIO(originalTaxaBlock);
                io.parse(np, null);
                needToDetectTaxa = (io.getTaxonNamesFound().size() == 0);
            }
            if (np.peekMatchIgnoreCase("begin distances;")) {
                final DistancesBlock originalDistancesBlock = new DistancesBlock("OriginalDistances");
                final DistancesNexusIO io = new DistancesNexusIO(originalDistancesBlock);
                io.parse(np, originalTaxaBlock);
                if (needToDetectTaxa) {
                    final List<String> namesFound = io.getTaxonNamesFound();
                    if (namesFound.size() == 0)
                        throw new IOException("Couldn't detect taxon names in input file");
                    for (String name : namesFound) {
                        originalTaxaBlock.add(new Taxon(name));
                    }
                }
                final ADataNode<DistancesBlock> originalDistancesNode = new ADataNode<>(document, originalDistancesBlock);
                document.setOriginalDataNode(originalDistancesNode);

                final ADataNode<DistancesBlock> workingDistancesNode = new ADataNode<>(document, new DistancesBlock("Distances"));


                ADataNode<TaxaBlock> workingTaxaNode = new ADataNode<>(document, new TaxaBlock("Taxa"));
                TaxaFilter taxaFilter = new TaxaFilter(document, document.getOriginalTaxaNode(), workingTaxaNode);

                // todo: just for debugging, report on changes of working taxa and changes of distances:
                new Report<>(document, workingTaxaNode.getDataBlock(), workingTaxaNode);
                new Report<>(document, workingTaxaNode.getDataBlock(), workingDistancesNode);

                // todo: just for debugging, open taxa filter view here:
                {
                    final TaxaFilterView taxaFilterView = new TaxaFilterView(document, taxaFilter);
                    taxaFilterView.show();
                }

                new DistancesTopFilter(document, document.getOriginalTaxaNode(), workingTaxaNode, originalDistancesNode, workingDistancesNode);
            }
        }
    }

    /**
     * write a document in nexus format
     *
     * @param document
     */
    public static void write(Document document) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(document.getFileName()))) {
            writer.write("#nexus\n");

            // taxa block:
            (new TaxaNexusIO(document.getOriginalTaxaNode().getDataBlock())).write(writer, null);

            // taxa filter and second block, if necessary:
            final TaxaFilter taxaFilter = (TaxaFilter) document.getOriginalTaxaNode().getChildren().get(0);
            if (taxaFilter.getDisabledData().size() > 0) { // some taxa have been filtered
                writer.write("[NEED to report taxa filter]\n");
            }

            if (document.getOriginalDataNode().getDataBlock() instanceof DistancesBlock) {
                (new DistancesNexusIO((DistancesBlock) document.getOriginalDataNode().getDataBlock())).write(writer, null);

            }
        }

    }
}
