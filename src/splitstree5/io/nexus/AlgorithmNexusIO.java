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
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.ADataBlock;

import java.io.IOException;
import java.io.Writer;

/**
 * performs nexus input and output for an algorithm
 * Created by huson on 12/27/16.
 */
public class AlgorithmNexusIO<P extends ADataBlock, C extends ADataBlock> {
    public static final String NAME = "ST_ALGORITHM";

    private final Algorithm<P, C> algorithm;

    public AlgorithmNexusIO(Algorithm<P, C> algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * parse a description of the algorithm
     *
     * @param np
     * @throws IOException
     */
    public void parse(NexusStreamParser np) throws IOException {


    }


    /**
     * write a description of the algorithm
     *
     * @param w
     * @throws IOException
     */
    public void write(Writer w) throws IOException {
        w.write("begin " + NAME + ";\n");
        w.write("name='" + algorithm.getName() + "';\n");
        w.write("[Todo: Use reflection to report all parameters here]\n"); // todo: Use reflection to report all parameters here
        w.write("end; [" + NAME + "]\n");
    }

    public String getUsage() {
        return "BEGIN " + NAME + ";\n" +
                "NAME=algorithm-name;\n" +
                "[Todo: Use reflection to report all parameters here]\n" + // todo: Use reflection to report all parameters here
                "END; [" + NAME + "]\n";
    }
}
