package splitstree5.io.exports;

import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.exports.interfaces.IExportCharacters;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

public class ClustalOut implements IFromChararacters, IExportCharacters {

    private int optionLineLength = 40;

    public void export(Writer w, TaxaBlock taxa, CharactersBlock characters)
            throws IOException {

        final String description = "CLUSTAL W (1.82) multiple sequence alignment";
        w.write(description + "\n" + "\n" + "\n");
        int ntax = taxa.getNtax();
        int nchar = characters.getNchar();

        int iterations = nchar / optionLineLength + 1;
        for (int i = 1; i <= iterations; i++) {
            int startIndex = optionLineLength * (i - 1) + 1;
            for (int t = 1; t <= ntax; t++) {
                StringBuilder sequence = new StringBuilder("");
                int stopIndex = optionLineLength;
                for (int j = startIndex; j <= optionLineLength * i && j <= nchar; j++) {
                    sequence.append(characters.get(t, j));
                    stopIndex = j;
                }
                w.write(taxa.get(t) + " \t" + sequence.toString().toUpperCase() + " \t" + stopIndex + "\n");
            }
            w.write("\n");
        }
    }

    public int getOptionLineLength() {
        return this.optionLineLength;
    }

    public void setOptionLineLength(int lineLength) {
        this.optionLineLength = lineLength;
    }

    @Override
    public List<String> getExtensions() {
        return Arrays.asList("aln", "clustal");
    }
}