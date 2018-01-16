package splitstree5.io.imports;

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusFormat;
import splitstree5.io.nexus.CharactersNexusIO;
import splitstree5.io.nexus.TaxaNexusIO;

import java.io.StringWriter;

public class StockholmInTest {

    private StockholmIn stockholmIn = new StockholmIn();

    @Test
    public void parse() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();
        ProgressListener pl = new ProgressPercentage();
        CharactersNexusFormat format = new CharactersNexusFormat();
        stockholmIn.parse(pl,"test/notNexusFiles/PF02171_seed.txt", taxaBlock, charactersBlock, format);

        // printing
        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        TaxaNexusIO.write(w, taxaBlock);
        CharactersNexusIO.write(w, taxaBlock, charactersBlock, null);
        System.err.println(w.toString());
    }

}