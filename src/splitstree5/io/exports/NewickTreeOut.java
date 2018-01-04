package splitstree5.io.exports;

import com.sun.istack.internal.Nullable;
import splitstree5.core.algorithms.interfaces.IFromSplits;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.Compatibility;

import java.io.IOException;
import java.io.Writer;

public class NewickTreeOut implements IFromTrees, IFromSplits {

    // todo function for for every block + taxa block, not static

    public void export(Writer w, @Nullable TreesBlock trees, @Nullable SplitsBlock splits) throws IOException {

        // todo network

        if (trees != null) {
            for (int i = 0; i < trees.getNTrees(); i++) {
                w.write(trees.getTrees().get(i).toString() + ";\n");
            }
        } else if (splits != null
                && splits.getCompatibility().equals(Compatibility.compatible)) {
            // todo move to algorithms ?
            //PhyloTree tree = TreesUtilities.treeFromSplits(taxa, splits, null);
            //w.write(tree.toString() + "\n");
        }
    }
}
