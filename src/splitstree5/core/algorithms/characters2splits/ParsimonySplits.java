package splitstree5.core.algorithms.characters2splits;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;

public class ParsimonySplits extends Algorithm<CharactersBlock, SplitsBlock> implements IFromChararacters, IToSplits {
    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, CharactersBlock parent, SplitsBlock child) throws Exception {

    }
}
