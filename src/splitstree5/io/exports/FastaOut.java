package splitstree5.io.exports;

import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.io.*;

public class FastaOut implements IFromChararacters {

    public static void export(Writer w, TaxaBlock taxa, CharactersBlock characters)
            throws IOException {

        jloda.util.FastA fasta = new jloda.util.FastA();

        int ntax = taxa.getNtax();
        int nchar = characters.getNchar();

        for(int i=1; i<=ntax; i++){
            StringBuilder sequence = new StringBuilder("");
            for(int j=1; j<=nchar; j++){
                sequence.append(characters.get(i,j));
            }
            fasta.add(taxa.getLabel(i), sequence.toString().toUpperCase());
        }

        fasta.write(w);
    }
}
