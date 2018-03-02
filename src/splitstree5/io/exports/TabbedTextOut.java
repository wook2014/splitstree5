package splitstree5.io.exports;

import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IFromDistances;
import splitstree5.core.algorithms.interfaces.IFromSplits;
import splitstree5.core.algorithms.interfaces.IFromTaxa;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.exports.interfaces.IExportCharacters;
import splitstree5.io.exports.interfaces.IExportDistances;
import splitstree5.io.exports.interfaces.IExportSplits;
import splitstree5.io.exports.interfaces.IExportTaxa;

import java.io.IOException;
import java.io.Writer;
import java.util.BitSet;
import java.util.List;

public class TabbedTextOut implements
        IFromTaxa, IFromChararacters, IFromDistances, IFromSplits,
        IExportTaxa, IExportCharacters, IExportDistances, IExportSplits {

    public void export(Writer w, TaxaBlock taxa) throws IOException {

        w.write("Taxa\n");
        for (int i = 1; i <= taxa.getNtax(); i++) {
            w.write(i + "\t" + taxa.getLabel(i) + "\n");
        }
        w.write("\n");
        w.flush();
    }

    public void export(Writer w, TaxaBlock taxa, CharactersBlock characters) throws IOException {

        w.write("Characters\n");
        for (int i = 1; i <= taxa.getNtax(); i++)
            w.write(taxa.getLabel(i) + "\t");
        w.write("\n");

        for (int j = 1; j <= characters.getNchar(); j++) {
            w.write(j + "");
            for (int i = 1; i <= taxa.getNtax(); i++) {
                w.write("\t" + characters.get(i, j));
            }
            w.write("\n");
        }

        w.write("\n");
        w.flush();
    }

    public void export(Writer w, TaxaBlock taxa, DistancesBlock distances) throws IOException {

        int ntax = distances.getNtax();

        w.write("Distance matrix\n");
        for (int i = 1; i <= ntax; i++) {
            for (int j = 1; j <= ntax; j++)
                //w.write(dec.format(distances.get(i, j)) + "\t");
                w.write((float) distances.get(i, j) + "\t");
            w.write("\n");
        }
        w.write("\n");

        //Export the distances as a matrix then as a column vector.
        w.write("Distance matrix as column vector. (1,2),(1,3),..,(1,n),(2,3),...\n");
        for (int i = 1; i <= ntax; i++) {
            for (int j = i + 1; j <= ntax; j++)
                //w.write(dec.format(distances.get(i, j)) + "\n");
                w.write((float) distances.get(i, j) + "\n");
        }
        w.write("\n");
        w.flush();
    }

    public void export(Writer w, TaxaBlock taxa, SplitsBlock splits) throws IOException {

        w.write("Splits\n");
        w.write("\tWeights");

        for (int i = 1; i <= taxa.getNtax(); i++)
            w.write("\t" + taxa.getLabel(i));
        w.write("\n");

        //Now we loop through the splits, one split per row.
        int nsplits = splits.getNsplits();
        int ntax = taxa.getNtax();
        for (int i = 1; i <= nsplits; i++) {

            //Split number
            w.write(Integer.toString(i));
            w.write("\t" + splits.getWeight(i - 1));
            BitSet A = splits.get(i - 1).getA();
            for (int j = 1; j <= ntax; j++) {
                char ch = A.get(j) ? '1' : '0';
                w.write("\t" + ch);
            }

            w.write("\n");
        }
        w.write("\n");
    }

    @Override
    public List<String> getExtensions() {
        return null;
    }
}
