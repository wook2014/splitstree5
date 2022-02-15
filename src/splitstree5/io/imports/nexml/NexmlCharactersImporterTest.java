/*
 * NexmlCharactersImporterTest.java Copyright (C) 2022 Daniel H. Huson
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

import jloda.util.CanceledException;
import jloda.util.progress.ProgressListener;
import jloda.util.progress.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusOutput;
import splitstree5.io.nexus.TaxaNexusOutput;

import java.io.IOException;
import java.io.StringWriter;

public class NexmlCharactersImporterTest {

	private final NexmlCharactersImporter nexmlCharactersIn = new NexmlCharactersImporter();

    @Test
    public void parseSeq() throws IOException, CanceledException {

        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();
        ProgressListener pl = new ProgressPercentage();

        nexmlCharactersIn.parse(pl, "test/neXML/M4097_seq.xml", taxaBlock, charactersBlock);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        new TaxaNexusOutput().write(w1, taxaBlock);
		new CharactersNexusOutput().write(w1, taxaBlock, charactersBlock);
		System.err.println(w1);

    }

    @Test
    public void parseCells() throws IOException, CanceledException {

        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();
        ProgressListener pl = new ProgressPercentage();

        nexmlCharactersIn.parse(pl, "test/neXML/M4311_cell.xml", taxaBlock, charactersBlock);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        new TaxaNexusOutput().write(w1, taxaBlock);
		new CharactersNexusOutput().write(w1, taxaBlock, charactersBlock);
		System.err.println(w1);
    }

}