package splitstree5.io.exports;

import splitstree5.core.algorithms.interfaces.IFromDistances;
import splitstree5.core.algorithms.interfaces.IFromSplits;
import splitstree5.core.algorithms.interfaces.IFromTaxa;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.BitSet;

public class MatlabOut implements IFromTaxa, IFromDistances, IFromSplits {

    private boolean optionExportDataBlockWithTaxa = false;
    private boolean bHeader = true;

    public void exportTaxa(Writer w, TaxaBlock taxa) throws IOException {

        if(bHeader){
            w.write("%%MATLAB%%\n");
            bHeader = false;
        }

        w.write("%%Number Taxa then taxon names\n");
        w.write("" + taxa.getNtax() + "\n");
        for (int i = 1; i <= taxa.getNtax(); i++) {
            w.write("\t" + taxa.getLabel(i) + "\n");
        }
        w.write("\n");
        w.flush();
    }

    public void exportDistances(Writer w, TaxaBlock taxa, DistancesBlock distances) throws IOException {

        if(bHeader){
            w.write("%%MATLAB%%\n");
            bHeader = false;
        }

        DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
        dfs.setDecimalSeparator('.');
        DecimalFormat dec = new DecimalFormat("#.0#####", dfs);

        if (optionExportDataBlockWithTaxa)
            exportTaxa(w, taxa);

        //Export the distances as a matrix then as a column vector.
        int ntax = distances.getNtax();
        w.write("%%Distance matrix as column vector. (1,2),(1,3),..,(1,n),(2,3),...\n");
        for (int i = 1; i <= ntax; i++) {
            for (int j = i + 1; j <= ntax; j++)
                w.write(dec.format(distances.get(i, j)) + "\n");
        }
        w.write("\n");
        w.flush();
    }

    public void exportSplits(Writer w, TaxaBlock taxa, SplitsBlock splits) throws IOException {

        if(bHeader){
            w.write("%%MATLAB%%\n");
            bHeader = false;
        }

        if (optionExportDataBlockWithTaxa)
            exportTaxa(w, taxa);

        w.write("%%Number of splits, then row of split weights, then design matrix, same row ordering as distances\n");

        int ntax;
        if(splits.getSplits().isEmpty())
            throw new IOException("SplitsBlock is empty");
        else
            ntax = splits.get(0).ntax();
        int nsplits = splits.getNsplits();
        w.write("%%Number of splits\n");
        w.write("" + nsplits + "\n");
        w.write("%% Split weights\n");
        for (int j = 0; j < nsplits; j++)
            w.write(" "+ (float) splits.getWeight(j)); // todo output format: float - double
        w.write("\n");

        //int ntax = splits.getNtax();
        for (int i = 1; i < ntax; i++) {
            for (int j = i + 1; j <= ntax; j++) {
                for (int k = 0; k < nsplits; k++) {
                    BitSet S = splits.get(k).getA();
                    if (S.get(i) != S.get(j))
                        w.write("\t" + 1);
                    else
                        w.write("\t" + 0);
                }
                w.write("\n");
            }
        }
        w.write("\n");
        w.flush();
    }

    public boolean getOptionExportDataBlockWithTaxa(){
        return this.optionExportDataBlockWithTaxa;
    }

    public void setOptionExportDataBlockWithTaxa(boolean exportDataBlockWithTaxa){
        this.optionExportDataBlockWithTaxa=exportDataBlockWithTaxa;
    }
}
