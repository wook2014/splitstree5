/*
 * NewickTreeExporter.java Copyright (C) 2021. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package splitstree5.io.exports;

import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.exports.interfaces.IExportTrees;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

public class NewickTreeExporter implements IFromTrees, IExportTrees {

    public void export(Writer w, TaxaBlock taxa, TreesBlock trees) throws IOException {

        if (trees != null) {
            for (int i = 0; i < trees.getNTrees(); i++) {
                w.write(trees.getTrees().get(i).toBracketString() + ";\n");
            }
        }
    }

    @Override
    public List<String> getExtensions() {
        return Arrays.asList("tree", "tre", "new", "nwk", "treefile");
    }
}
