package splitstree5.io.otherFormats;

import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.Taxon;
import splitstree5.io.nexus.CharactersNexusFormat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;

/**
 * Created by Daria on 02.10.2017.
 */
public class PhylipDistancesIO {


    public static void parse(String inputFile, TaxaBlock taxa, DistancesBlock distances) throws IOException {

        taxa.clear();
        distances.clear();
        int ntax;

        try (BufferedReader in = new BufferedReader(new FileReader(inputFile))) {

            StreamTokenizer st = new StreamTokenizer(in);
            st.resetSyntax();
            st.eolIsSignificant(true);
            st.whitespaceChars(0, 32);
            st.wordChars(33, 126);
            st.nextToken();
            ntax = Integer.parseInt(st.sval);
            distances.setNtax(ntax);
            st.nextToken();
            for (int i = 1; i <= ntax; i++) {
                int shift = 0;
                int token = st.nextToken();
                if (token == StreamTokenizer.TT_EOL)
                    st.nextToken();
                Taxon taxon = new Taxon(st.sval);
                taxa.add(taxon);
                //System.err.println(st.sval);
                for (int j = 1; j <= ntax; j++) {
                    token = st.nextToken();
                    if (token != StreamTokenizer.TT_EOL){
                        distances.set(i, j-shift, Double.parseDouble(st.sval));
                        //System.err.println(i+"--"+(j-shift)+"----------"+st.sval);
                    } else {
                        if(j>=i) break;
                        if(j<i-1) shift++;
                    }
                }
            }

            if(st.nextToken() != StreamTokenizer.TT_EOF){
                throw new IOException("Unexpected symbol at line " + st.lineno());
            }
        }

        // todo make both or set triangular format?
        if(distances.get(1, ntax) != distances.get(ntax, 1))
            makeTriangularBoth(distances);

    }


    private static void makeTriangularBoth(DistancesBlock distances){
        int ntax = distances.getNtax();
        for(int i=1; i<=ntax; i++){
            for(int j=1; j<=i; j++){
                distances.set(j, i, distances.get(i,j));
            }
        }
    }

}
