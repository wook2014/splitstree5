/*
 * NexmlTreesImporterTest.java Copyright (C) 2022 Daniel H. Huson
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

package splitstree5.io.imports.NeXML;

import jloda.util.progress.ProgressListener;
import jloda.util.progress.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.nexus.TaxaNexusOutput;
import splitstree5.io.nexus.TreesNexusFormat;
import splitstree5.io.nexus.TreesNexusOutput;

import java.io.StringWriter;

public class NexmlTreesImporterTest {

    private NexmlTreesImporter nexmlTreesImporter = new NexmlTreesImporter();

    @Test
    public void parse() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();
        ProgressListener pl = new ProgressPercentage();

        //nexmlTreesImporter.parse(pl, "test/neXML/trees.xml", taxaBlock, treesBlock);
        nexmlTreesImporter.parse(pl, "test/neXML/trees_partial.xml", taxaBlock, treesBlock);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        new TaxaNexusOutput().write(w1, taxaBlock);
        TreesNexusFormat treesNexusFormat = new TreesNexusFormat();
        treesNexusFormat.setOptionTranslate(false);
        new TreesNexusOutput().write(w1, taxaBlock, treesBlock);
        System.err.println(w1.toString());
        System.err.println("Partial: " + treesBlock.isPartial());

    }

}