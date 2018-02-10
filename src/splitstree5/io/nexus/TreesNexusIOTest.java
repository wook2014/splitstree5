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

package splitstree5.io.nexus;

import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * test io
 * Daniel Huson, 12/30/16.
 */
public class TreesNexusIOTest {

    @Test
    public void testInputNoTaxaBlock() throws IOException {
        TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();
        try (NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/trees49-notaxa.nex"))) {
            np.matchIgnoreCase("#nexus");
            final List<String> taxaLabelsFound = new TreesNexusInput().parse(np, taxaBlock, treesBlock);
            assertEquals(taxaLabelsFound.size(), 91);
            assertEquals(treesBlock.getNTrees(), 49);
        }
    }

    @Test
    public void testInputWithTaxaBlock() throws IOException {
        TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();
        try (NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/trees49-taxa.nex"))) {
            np.matchIgnoreCase("#nexus");
            new TaxaNexusInput().parse(np, taxaBlock);
            final List<String> taxaLabelsFound = new TreesNexusInput().parse(np, taxaBlock, treesBlock);
            assertEquals(taxaLabelsFound.size(), taxaBlock.getNtax());
            assertEquals(taxaLabelsFound.size(), 91);
            assertEquals(treesBlock.getNTrees(), 49);
        }
    }

    @Test
    public void testInputWithTranslate() throws IOException {
        TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();
        try (NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/trees6-translate.nex"))) {
            np.matchIgnoreCase("#nexus");
            new TaxaNexusInput().parse(np, taxaBlock);
            final List<String> taxaLabelsFound = new TreesNexusInput().parse(np, taxaBlock, treesBlock);
            assertEquals(taxaLabelsFound.size(), taxaBlock.getNtax());
            assertEquals(taxaLabelsFound.size(), 6);
            assertEquals(treesBlock.getNTrees(), 6);
        }
    }

    @Test
    public void testInputOutputWithTranslate() throws IOException {
        TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();
        try (NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/trees6-translate.nex"))) {
            np.matchIgnoreCase("#nexus");
            new TaxaNexusInput().parse(np, taxaBlock);
            final List<String> taxaLabelsFound = new TreesNexusInput().parse(np, taxaBlock, treesBlock);
            assertEquals(taxaLabelsFound.size(), taxaBlock.getNtax());
            assertEquals(taxaLabelsFound.size(), 6);
            assertEquals(treesBlock.getNTrees(), 6);
        }

        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        new TaxaNexusOutput().write(w, taxaBlock);
        new TreesNexusOutput().write(w, taxaBlock, treesBlock);

        // System.err.println(w.toString());

        TaxaBlock taxaBlock2 = new TaxaBlock();
        TreesBlock treesBlock2 = new TreesBlock();
        try (NexusStreamParser np = new NexusStreamParser(new StringReader(w.toString()))) {
            np.matchIgnoreCase("#nexus");
            new TaxaNexusInput().parse(np, taxaBlock2);
            new TreesNexusInput().parse(np, taxaBlock2, treesBlock2);
            assertEquals(taxaBlock.getTaxa(), taxaBlock2.getTaxa());
        }

        final StringWriter w2 = new StringWriter();
        w2.write("#nexus\n");
        new TaxaNexusOutput().write(w2, taxaBlock2);
        new TreesNexusOutput().write(w2, taxaBlock2, treesBlock2);
        assertEquals(w.toString(), w2.toString());
    }

}