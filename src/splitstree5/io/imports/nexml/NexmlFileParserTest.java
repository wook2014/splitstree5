/*
 * NexmlFileParserTest.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.io.imports.nexml;

import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.nexus.CharactersNexusOutput;
import splitstree5.io.nexus.TaxaNexusOutput;
import splitstree5.io.nexus.TreesNexusFormat;
import splitstree5.io.nexus.TreesNexusOutput;

import java.io.IOException;
import java.io.StringWriter;

public class NexmlFileParserTest {
    @Test
    public void parseTaxa() throws Exception {

        NexmlFileParser nexmlFileParser = new NexmlFileParser();
        TaxaBlock taxaBlock = new TaxaBlock();
        nexmlFileParser.parse("test/neXML/taxa.xml", taxaBlock);

        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
		new TaxaNexusOutput().write(w1, taxaBlock);
		System.err.println(w1);

    }

    @Test
    public void parseCharacters() throws IOException {

        NexmlFileParser nexmlFileParser = new NexmlFileParser();
        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();

        nexmlFileParser.parse("test/neXML/M4097_seq.xml", taxaBlock, charactersBlock);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        //new TaxaNexusOutput().write(w1, taxaBlock);
		new CharactersNexusOutput().write(w1, taxaBlock, charactersBlock);
		System.err.println(w1);
    }

    @Test
    public void parseCharactersCells() throws IOException {

        NexmlFileParser nexmlFileParser = new NexmlFileParser();
        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();

        nexmlFileParser.parse("test/neXML/M4311_cell.xml", taxaBlock, charactersBlock);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        new TaxaNexusOutput().write(w1, taxaBlock);
		new CharactersNexusOutput().write(w1, taxaBlock, charactersBlock);
		System.err.println(w1);
    }

    @Test
    public void parseTrees() throws IOException {

        NexmlFileParser nexmlFileParser = new NexmlFileParser();
        TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();

        nexmlFileParser.parse("test/neXML/simple.xml", taxaBlock, treesBlock);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        new TaxaNexusOutput().write(w1, taxaBlock);
        TreesNexusFormat treesNexusFormat = new TreesNexusFormat();
        treesNexusFormat.setOptionTranslate(false);
		new TreesNexusOutput().write(w1, taxaBlock, treesBlock);
		System.err.println(w1);
    }

}