package splitstree5.io.exports;

import splitstree5.core.algorithms.interfaces.IFromDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.io.IOException;
import java.io.Writer;

public class PhylipDistancesOut implements IFromDistances {

    public static boolean optionTriangular = false; //todo

    public static void export(Writer w, TaxaBlock taxa, DistancesBlock distances)
            throws IOException {

        int ntax = taxa.getNtax();

        int maxLabelLength = taxa.getLabel(1).length();
        for(int i=2; i<=ntax; i++){
            if(taxa.getLabel(i).length()>maxLabelLength)
                maxLabelLength = taxa.getLabel(i).length();
        }

        w.write(ntax+"\n");
        for(int i=1; i<=distances.getDistances().length; i++){
            StringBuilder sequence = new StringBuilder("");
            for(int j=1; j<=distances.getDistances()[i-1].length; j++){
                sequence.append(distances.get(i, j));
                sequence.append(" ");
            }
            w.write(taxa.getLabel(i));
            for(int k=0; k<maxLabelLength-taxa.getLabel(i).length(); k++){
                w.write(" ");
            }
            w.write("\t"+sequence+"\n");
        }
        w.close();
    }
}
