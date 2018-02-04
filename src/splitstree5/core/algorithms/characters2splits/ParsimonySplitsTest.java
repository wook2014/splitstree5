package splitstree5.core.algorithms.characters2splits;

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusInput;
import splitstree5.io.nexus.SplitsNexusOutput;
import splitstree5.io.nexus.TaxaNexusInput;
import splitstree5.io.nexus.TaxaNexusOutput;

import java.io.FileReader;
import java.io.StringWriter;

public class ParsimonySplitsTest {

    private final ParsimonySplits parsimonySplits = new ParsimonySplits();

    @Test
    public void compute() throws Exception {

        TaxaBlock taxa = new TaxaBlock();
        SplitsBlock splits = new SplitsBlock();
        CharactersBlock characters = new CharactersBlock();
        ProgressListener pl = new ProgressPercentage();

        NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/algae_char.nex"));
        //NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/small_test.nex"));
        np.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np, taxa);
        new CharactersNexusInput().parse(np, taxa, characters, null);

        parsimonySplits.compute(pl, taxa, characters, splits);

        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        new TaxaNexusOutput().write(w1, taxa);
        new SplitsNexusOutput().write(w1, taxa, splits, null);
        System.err.println(w1.toString());

        /*TaxaBlock taxaFromST4 = new TaxaBlock();
        SplitsBlock splitsFromSt4 = new SplitsBlock();
        NexusStreamParser np1 = new NexusStreamParser(new FileReader("test/nexus/algae.nex"));
        np1.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np1, taxaFromST4);
        new SplitsNexusInput().parse(np1, taxaFromST4, splitsFromSt4, null);*/

    }

}