package splitstree5.utils.nexus;

import splitstree4.core.SplitsException;
import splitstree4.core.TaxaSet;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;

/**
 * Created by Daria on 23.01.2017.
 */
public class SplitsUtilities {

    //todo : replace classes imported from st4
    /**
     * verify that all splits are proper and are contained in the taxon set
     *
     * @param splits
     * @param taxa
     * @throws SplitsException
     */
    public static void verifySplits(SplitsBlock splits, TaxaBlock taxa) throws SplitsException {
        for (int s = 1; s <= splits.getNsplits(); s++) {
            //TaxaSet split = splits.get(s);
            TaxaSet split = new TaxaSet(splits.getSplits().get(s).getA());

            if (split.cardinality() == 0 || split.cardinality() == taxa.getNtax())
                throw new SplitsException
                        ("Split <" + split + "<: not proper, size is 0 or ntax");
            if (!taxa.getTaxaSet().contains(split))
                throw new SplitsException("Split <" + split + "> not contained in taxa set <"
                        + taxa.getTaxaSet() + ">");
        }
    }
}
