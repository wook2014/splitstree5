/*
 * NexmlNetworkImporterTest.java Copyright (C) 2022 Daniel H. Huson
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
import splitstree5.core.datablocks.NetworkBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.NetworkNexusOutput;
import splitstree5.io.nexus.TaxaNexusOutput;

import java.io.StringWriter;

public class NexmlNetworkImporterTest {

    private NexmlNetworkImporter nexmlNetworkImporter = new NexmlNetworkImporter();

    @Test
    public void parse() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        NetworkBlock networkBlock = new NetworkBlock();
        ProgressListener pl = new ProgressPercentage();

        nexmlNetworkImporter.parse(pl, "test/neXML/trees.xml", taxaBlock, networkBlock);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        new TaxaNexusOutput().write(w1, taxaBlock);
        new NetworkNexusOutput().write(w1, taxaBlock, networkBlock);
        System.err.println(w1.toString());

    }

}