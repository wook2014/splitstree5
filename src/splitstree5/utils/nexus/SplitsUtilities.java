package splitstree5.utils.nexus;

import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ASplit;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Tools for analyzing a splits block
 * Daniel Huson, 2005
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
        final Set<BitSet> seen = new HashSet<>();

        for (ASplit split : splits.getSplits()) {
            final BitSet aSet = split.getA();
            if (seen.contains(aSet))
                throw new SplitsException("Split " + aSet + " occurs multiple times");
            if (aSet.cardinality() == 0)
                throw new SplitsException("Split " + aSet + " not proper, size is 0");
            if (aSet.cardinality() == taxa.getNtax())
                throw new SplitsException("Split " + aSet + " not proper, size is ntax");
            if (aSet.nextSetBit(0) == 0 || aSet.nextSetBit(taxa.getNtax() + 1) != -1)
                throw new SplitsException("Split " + aSet + " not contained in taxa set <" + taxa.getTaxaSet() + ">");
            seen.add(aSet);
        }
    }
}
