package splitstree5.io.exports;

import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.io.*;

public class PhylipCharactersOut implements IFromChararacters {

    private static boolean optionInterleaved = true;
    private static boolean optionInterleavedMultiLabels = true;
    private static int optionLineLength = 40;

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
                        if((j-1)%10 == 0 && (j-1) != 0) sequence.append(" "); // set space after every 10 chars
                        sequence.append(characters.get(t,j));
                    }
                    if(i==1 || optionInterleavedMultiLabels)
                        w.write(get10charLabel(taxa.getLabel(t))+"\t"+sequence.toString().toUpperCase()+"\n");
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
                    if((j-1)%10 == 0 && (j-1) != 0) sequence.append(" "); // set space after every 10 chars
                    sequence.append(characters.get(t,j));
                }
                w.write(get10charLabel(taxa.getLabel(t))+"\t"+sequence.toString().toUpperCase()+"\n");
            }
            w.close();
        }
    }


    private static String get10charLabel(String label){
        if(label.length() >= 10)
            return label.substring(0, 10);
        else {
            StringBuilder s = new StringBuilder(label);
            for (int k = 0; k < 10 - label.length(); k++) {
                s.append(" ");
            }
            return s.toString();
        }
    }

    public static boolean getOptionInterleaved(){
        return optionInterleaved;
    }

    public static void setOptionInterleaved(boolean interleaved){
        optionInterleaved=interleaved;
    }

    public static boolean getoptionInterleavedMultiLabels(){
        return optionInterleavedMultiLabels;
    }

    public static void setoptionInterleavedMultiLabels(boolean multi){
        optionInterleavedMultiLabels=multi;
        if(multi) optionInterleaved = true;
    }

    public static int getOptionoptionLineLength(){
        return optionLineLength;
    }

    public static void setOptionoptionLineLength(int length){
        optionLineLength=length;
    }



}
