package splitstree5.io.exports;

import splitstree5.core.algorithms.interfaces.IFromDistances;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.exports.interfaces.IExportDistances;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

public class PhylipDistancesOut implements IFromDistances, IExportDistances {

    private boolean optionTriangular = false;

    public void export(Writer w, TaxaBlock taxa, DistancesBlock distances)
            throws IOException {

        int ntax = taxa.getNtax();

        int maxLabelLength = taxa.getLabel(1).length();
        for (int i = 2; i <= ntax; i++) {
            if (taxa.getLabel(i).length() > maxLabelLength)
                maxLabelLength = taxa.getLabel(i).length();
        }

        w.write("\t" + ntax + "\n");

        if (!optionTriangular) {
            System.err.println("standard");
            for (int i = 1; i <= distances.getDistances().length; i++) {
                StringBuilder sequence = new StringBuilder("");
                for (int j = 1; j <= distances.getDistances()[i - 1].length; j++) {
                    sequence.append(distances.get(i, j));
                    sequence.append(" ");
                }
                if (taxa.getLabel(i).length() >= 10)
                    w.write(taxa.getLabel(i).substring(0, 10));
                else {
                    w.write(taxa.getLabel(i));
                    for (int k = 0; k < 10 - taxa.getLabel(i).length(); k++) {
                        w.write(" ");
                    }
                }
                w.write("\t" + sequence + "\n");
            }
        } else {
            System.err.println("triangular");
            w.write(taxa.getLabel(1) + "\n");
            for (int i = 2; i <= distances.getDistances().length; i++) {
                StringBuilder sequence = new StringBuilder("");
                for (int j = 1; j <= i - 1; j++) {
                    sequence.append(distances.get(i, j));
                    sequence.append(" ");
                }
                if (taxa.getLabel(i).length() >= 10)
                    w.write(taxa.getLabel(i).substring(0, 10));
                else {
                    w.write(taxa.getLabel(i));
                    for (int k = 0; k < 10 - taxa.getLabel(i).length(); k++) {
                        w.write(" ");
                    }
                }
                w.write("\t" + sequence + "\n");
            }
        }
    }

    public void setOptionTriangular(boolean optionTriangular) {
        this.optionTriangular = optionTriangular;
    }

    public boolean getOptionTriangular() {
        return this.optionTriangular;
    }

    @Override
    public List<String> getExtensions() {
        return Arrays.asList("dist", "dst");
    }
}
