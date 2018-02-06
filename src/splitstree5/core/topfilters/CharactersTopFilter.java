/*
 *  Copyright (C) 2018 Daniel H. Huson
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

package splitstree5.core.topfilters;


import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.Taxon;
import splitstree5.core.workflow.DataNode;
import splitstree5.io.nexus.CharactersNexusFormat;
import splitstree5.io.nexus.CharactersNexusInput;
import splitstree5.io.nexus.CharactersNexusOutput;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * characters top taxon filter
 * Daniel Huson, 12/12/16.
 */
public class CharactersTopFilter extends ATopFilter<CharactersBlock> {
    /**
     * constructor
     *
     * @param originalTaxaNode
     * @param modifiedTaxaNode
     * @param parentNode
     * @param childNode
     */
    public CharactersTopFilter(DataNode<TaxaBlock> originalTaxaNode, DataNode<TaxaBlock> modifiedTaxaNode, DataNode<CharactersBlock> parentNode, DataNode<CharactersBlock> childNode) {
        super(originalTaxaNode.getDataBlock(), modifiedTaxaNode, parentNode, childNode);

        setAlgorithm(new Algorithm<CharactersBlock, CharactersBlock>("TopFilter") {
            public void compute(ProgressListener progress, TaxaBlock modifiedTaxaBlock, CharactersBlock parent, CharactersBlock child) throws CanceledException {
                // todo: implement direct copy?
                {
                    progress.setMaximum(modifiedTaxaBlock.size());
                    final StringWriter w = new StringWriter();
                    try {
                        CharactersNexusFormat charactersNexusFormat = new CharactersNexusFormat();
                        final CharactersNexusOutput charactersNexusOutput = new CharactersNexusOutput();
                        charactersNexusOutput.setIgnoreMatrix(true);
                        charactersNexusOutput.write(w, originalTaxaNode.getDataBlock(), parent, charactersNexusFormat);
                        final CharactersNexusInput charactersNexusInput = new CharactersNexusInput();
                        charactersNexusInput.setIgnoreMatrix(true);
                        charactersNexusInput.parse(new NexusStreamParser(new StringReader(w.toString())), originalTaxaNode.getDataBlock(), child, charactersNexusFormat);
                    } catch (IOException e) {
                        Basic.caught(e);
                    }
                }
                child.setDimension(modifiedTaxaBlock.getNtax(), 0);

                for (Taxon a : modifiedTaxaBlock.getTaxa()) {
                    final int originalI = getOriginalTaxaBlock().indexOf(a);
                    final int modifiedI = modifiedTaxaBlock.indexOf(a);
                    child.copyRow(parent, originalI, modifiedI);
                    progress.incrementProgress();
                }
                if (modifiedTaxaBlock.size() == getOriginalTaxaBlock().size())
                    setShortDescription("using all " + modifiedTaxaBlock.size() + " sequences");
                else
                    setShortDescription("using " + modifiedTaxaBlock.size() + " of " + getOriginalTaxaBlock().size() + " sequences");
            }
        });
    }
}
