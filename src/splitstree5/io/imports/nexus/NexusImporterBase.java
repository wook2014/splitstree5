/*
 *  NexusImporterBase.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.io.imports.nexus;

import jloda.util.CanceledException;
import jloda.util.FileLineIterator;
import jloda.util.Pair;
import jloda.util.ProgressListener;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.IAdditionalBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TraitsBlock;
import splitstree5.core.misc.Taxon;
import splitstree5.io.nexus.TaxaNexusInput;
import splitstree5.io.nexus.TraitsNexusInput;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Base class for nexus importers
 *
 * @param <D> block to import
 *            Daniel HUson, 1.2018
 */
public abstract class NexusImporterBase<D extends DataBlock> {
    private String title;
    private Pair<String, String> link;
    public static final List<String> extensions = new ArrayList<>(Arrays.asList("nex", "nexus", "nxs"));

    public List<String> getExtensions() {
        return extensions;
    }

    /**
     * parse a nexus filer
     *
     * @param progressListener
     * @param fileName
     * @param taxaBlock
     * @param dataBlock
     * @throws CanceledException
     * @throws IOException
     */
    public void parse(ProgressListener progressListener, String fileName, TaxaBlock taxaBlock, D dataBlock) throws CanceledException, IOException {
        try (NexusStreamParser np = new NexusStreamParser(new FileReader(fileName))) {
            np.matchIgnoreCase("#nexus");

            if (np.peekMatchBeginBlock("splitstree5")) { // this is a complete file written by splitstree5
                System.err.println("Not implemented");
            } else { // this is a user input file

                boolean needToDetectTaxa = !(dataBlock instanceof IAdditionalBlock);

                if (np.peekMatchBeginBlock("taxa")) {
                    new TaxaNexusInput().parse(np, taxaBlock);
                    needToDetectTaxa = (taxaBlock.getTaxa().size() == 0);
                }

                if (np.peekMatchBeginBlock("traits") && !(dataBlock instanceof TraitsBlock)) {
                    new TraitsNexusInput().parse(np, taxaBlock, new TraitsBlock()); // parse and ignore, will reopen file and parse again later...
                }

                final List<String> namesFound = parseBlock(np, taxaBlock, dataBlock);
                if (needToDetectTaxa) {
                    if (namesFound.size() == 0)
                        throw new IOException("Failed to find taxon names in input file");
                    for (String name : namesFound) {
                        taxaBlock.add(new Taxon(name));
                    }
                }
            }

        }
    }

    /**
     * parse a block
     *
     * @param np
     * @param taxaBlock
     * @param dataBlock
     * @return taxon names found
     * @throws IOException
     */
    public abstract List<String> parseBlock(NexusStreamParser np, TaxaBlock taxaBlock, D dataBlock) throws IOException;

    /**
     * only allow import of the first block that is not the taxa block
     *
     * @param fileName
     * @param blockName
     * @return true if the first block (except the taxa block, if present) has the block name
     * @throws IOException
     */
    public boolean isApplicable(String fileName, String blockName) throws IOException {
        blockName = blockName.toLowerCase();

        try (FileLineIterator it = new FileLineIterator(fileName)) {
            boolean first = true;
            while (it.hasNext()) {
                final String aLine = it.next().toLowerCase();
                if (first) {
                    if (!aLine.startsWith("#nexus"))
                        return false;
                    first = false;
                }

                if (aLine.startsWith("begin")) {
                    final NexusStreamParser np = new NexusStreamParser(new StringReader(aLine));
                    if (!blockName.equals("traits") && np.peekMatchIgnoreCase("begin traits;"))
                        continue; // traits block: ignore
                    if (np.peekMatchIgnoreCase("begin " + blockName + ";"))
                        return true;
                    else if (blockName.equals("characters") && np.peekMatchIgnoreCase("begin data;"))
                        return true;
                    else if (!np.peekMatchIgnoreCase("begin taxa;") && !blockName.equals("traits"))
                        return false;
                }
            }
        }
        return false;
    }

    public String getTitle() {
        return title;
    }

    public Pair<String, String> getLink() {
        return link;
    }

    public void setTitleAndLink(String title, Pair<String, String> link) {
        this.title = title;
        this.link = link;
    }
}
