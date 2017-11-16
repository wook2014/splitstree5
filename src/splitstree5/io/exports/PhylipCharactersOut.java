package splitstree5.io.exports;

import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.io.*;

public class PhylipCharactersOut implements IFromChararacters {

    private static boolean optionInterleaved = true;
    private static boolean optionInterleavedMultiLabels = false; //todo
    private static int optionLineLength = 40;

    // todo 10 char distance!

    public static void export(Writer w, TaxaBlock taxa, CharactersBlock characters) throws IOException {

        int ntax = taxa.getNtax();
        int nchar = characters.getNchar();
        w.write("\t"+ntax+"\t"+nchar+"\n");

        if(optionInterleaved){
            int iterations = nchar/optionLineLength + 1;
            for(int i=1; i<=iterations; i++){
                int startIndex = optionLineLength * (i-1) + 1;
                for(int t=1; t<=ntax; t++){
                    StringBuilder sequence = new StringBuilder("");
                    for(int j=startIndex; j<=optionLineLength*i && j<=nchar; j++){
                        sequence.append(characters.get(t,j));
                    }
                    if(i==1)
                        w.write(taxa.get(t)+" \t"+sequence.toString().toUpperCase()+"\n");
                    else
                        w.write(sequence.toString().toUpperCase()+"\n");
                }
                w.write("\n");
            }
            w.close();
        }else{
            for(int t=1; t<=ntax; t++){
                StringBuilder sequence = new StringBuilder("");
                for(int j=1; j<=nchar; j++){
                    sequence.append(characters.get(t,j));
                }
                w.write(taxa.get(t)+" \t"+sequence.toString().toUpperCase()+"\n");
            }
            w.close();
        }
    }


    public static boolean getOptionInterleaved(){
        return optionInterleaved;
    }

    public static void setOptionInterleaved(boolean interleaved){
        optionInterleaved=interleaved;
    }

}
