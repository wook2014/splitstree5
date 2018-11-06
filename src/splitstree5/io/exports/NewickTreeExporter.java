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
                w.write(trees.getTrees().get(i).toString() + ";\n");
            }
        }
    }

    @Override
    public List<String> getExtensions() {
        return Arrays.asList("new", "nwk", "tree", "tre", "treefile");
    }
}
