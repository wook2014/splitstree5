/*
 *  Copyright (C) 2019 Daniel H. Huson
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
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.distances2splits.NeighborNet;

import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

/**
 * test algorithm io
 * Daniel Huson, 12/30/16.
 */
public class AlgorithmNexusIOTest {

    @Test
    public void testParse() throws Exception {
        Algorithm neighborNet = new NeighborNet();

        StringWriter w = new StringWriter();
        new AlgorithmNexusOutput().write(w, neighborNet);
        String output = w.toString();
        System.err.println(output);

        output = output.replaceAll("ols", "estimated");
        System.err.println("Edited:");
        //output=output.replaceAll("estimated;","estimated;\nbla=bla;");
        System.err.println(output);

        StringReader reader = new StringReader(output);
        neighborNet = new AlgorithmNexusInput().parse(new NexusStreamParser(reader));

        StringWriter w2 = new StringWriter();
        new AlgorithmNexusOutput().write(w2, neighborNet);
        String output2 = w2.toString();
        System.err.println(output2);

        assertEquals(output, output2);
    }
}