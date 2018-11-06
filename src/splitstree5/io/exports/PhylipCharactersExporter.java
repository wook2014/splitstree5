package splitstree5.io.exports;

import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.exports.interfaces.IExportCharacters;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

public class PhylipCharactersExporter implements IFromChararacters, IExportCharacters {

    private boolean optionInterleaved = true;
    private boolean optionInterleavedMultiLabels = true;
    private int optionLineLength = 40;

    public void export(Writer w, TaxaBlock taxa, CharactersBlock characters) throws IOException {

        int ntax = taxa.getNtax();
        int nchar = characters.getNchar();
        w.write("\t" + ntax + "\t" + nchar + "\n");

        if (optionInterleaved) {
            int iterations = nchar / optionLineLength + 1;
            for (int i = 1; i <= iterations; i++) {
                int startIndex = optionLineLength * (i - 1) + 1;
                for (int t = 1; t <= ntax; t++) {
                    StringBuilder sequence = new StringBuilder("");
                    for (int j = startIndex; j <= optionLineLength * i && j <= nchar; j++) {
                        if ((j - 1) % 10 == 0 && (j - 1) != 0) sequence.append(" "); // set space after every 10 chars
                        sequence.append(characters.get(t, j));
                    }
                    if (i == 1 || optionInterleavedMultiLabels)
                        w.write(get10charLabel(taxa.getLabel(t)) + "\t" + sequence.toString().toUpperCase() + "\n");
                    else
                        w.write(sequence.toString().toUpperCase() + "\n");
                }
                w.write("\n");
            }
        } else {
            for (int t = 1; t <= ntax; t++) {
                StringBuilder sequence = new StringBuilder("");
                for (int j = 1; j <= nchar; j++) {
                    if ((j - 1) % 10 == 0 && (j - 1) != 0) sequence.append(" "); // set space after every 10 chars
                    sequence.append(characters.get(t, j));
                }
                w.write(get10charLabel(taxa.getLabel(t)) + "\t" + sequence.toString().toUpperCase() + "\n");
            }
        }
    }


    private static String get10charLabel(String label) {
        if (label.length() >= 10)
            return label.substring(0, 10);
        else {
            StringBuilder s = new StringBuilder(label);
            for (int k = 0; k < 10 - label.length(); k++) {
                s.append(" ");
            }
            return s.toString();
        }
    }

    public boolean getOptionInterleaved() {
        return optionInterleaved;
    }

    public void setOptionInterleaved(boolean interleaved) {
        optionInterleaved = interleaved;
    }

    public boolean getoptionInterleavedMultiLabels() {
        return optionInterleavedMultiLabels;
    }

    public void setoptionInterleavedMultiLabels(boolean multi) {
        optionInterleavedMultiLabels = multi;
        if (multi) optionInterleaved = true;
    }

    public int getOptionoptionLineLength() {
        return optionLineLength;
    }

    public void setOptionoptionLineLength(int length) {
        optionLineLength = length;
    }


    @Override
    public List<String> getExtensions() {
        return Arrays.asList("phy", "phylip");
    }
}
