package splitstree5.core.algorithms.characters2distances;

import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;

public abstract class SequenceBasedDistance extends Algorithm<CharactersBlock, DistancesBlock> {

    abstract public double getOptionPInvar();

    abstract public void setOptionPInvar(double pinvar);

    abstract public double getOptionGamma();

    abstract public void setOptionGamma(double gamma);

}
