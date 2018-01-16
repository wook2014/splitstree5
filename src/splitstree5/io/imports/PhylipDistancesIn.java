package splitstree5.io.imports;

import com.sun.istack.internal.Nullable;
import jloda.util.CanceledException;
import jloda.util.FileInputIterator;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.Taxon;
import splitstree5.io.imports.interfaces.IImportDistances;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Daria Evseeva,02.10.2017.
 */
public class PhylipDistancesIn implements IToDistances, IImportDistances {

    public static final List<String> extensions = new ArrayList<>(Arrays.asList("dist", "dst"));

    /*
    todo :
    -----------------------------------------
    final ArrayList<String> taxonNamesFound = new ArrayList<>();
    final ArrayList<String> matrix = new ArrayList<>();
    -----------------------------------------
    try (FileInputIterator it = new FileInputIterator(inputFile))
    -----------------------------------------
    progressListener.setMaximum(it.getMaximumProgress());
    progressListener.setProgress(0);
            ...
    progressListener.setProgress(it.getProgress());
    -----------------------------------------
    while (it.hasNext()) {
                final String line = it.next();
    -----------------------------------------
     */

    @Override
    public void parse(ProgressListener progressListener, String inputFile, TaxaBlock taxa, DistancesBlock distances) throws CanceledException, IOException {
        taxa.clear();
        distances.clear();
        int ntax;

        try (FileInputIterator it = new FileInputIterator(inputFile)) {

            //progressListener.setMaximum(it.getMaximumProgress());
            //progressListener.setProgress(0);
            if (it.hasNext()) {
                final String line = it.next();
                StringTokenizer st = new StringTokenizer(line);
                ntax = Integer.parseInt(st.nextToken());
                distances.setNtax(ntax);
            } else {
                throw new IOException("Empty File");
            }

            while (it.hasNext()) {
                final String line = it.next();
                StringTokenizer st = new StringTokenizer(line);
                while (st.hasMoreTokens()) {
                    System.err.println(st.nextToken());
                }
            }
        }
            /*StreamTokenizer st = new StreamTokenizer(in);
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
                System.err.println(st.sval);
                for (int j = 1; j <= ntax; j++) {
                    token = st.nextToken();
                    if (token != StreamTokenizer.TT_EOL && token != StreamTokenizer.TT_EOF) {
                        distances.set(i, j - shift, Double.parseDouble(st.sval));
                        System.err.println(i + "--" + (j - shift) + "----------" + st.sval);
                    } else {
                        if (j >= i) {
                            st.nextToken();
                            if (isNumeric(st.sval)) {
                                st.pushBack();
                                shift++;
                                j--;
                            } else {
                                st.pushBack();
                                break;
                            }
                        }
                        if (j < i - 1) shift++;
                    }
                }
            }

            if (st.nextToken() != StreamTokenizer.TT_EOF) {
                throw new IOException("Unexpected symbol at line " + st.lineno());
            }
        }

        // todo make both or set triangular format?
        if (distances.get(1, ntax) != distances.get(ntax, 1))
            makeTriangularBoth(distances);*/
    }

    @Override
    public List<String> getExtensions() {
        return null;
    }

    @Override
    public boolean isApplicable(String fileName) throws IOException {
        return false;
    }

    private static void makeTriangularBoth(DistancesBlock distances) {
        int ntax = distances.getNtax();
        for (int i = 1; i <= ntax; i++) {
            for (int j = 1; j <= i; j++) {
                distances.set(j, i, distances.get(i, j));
            }
        }
    }

    private static boolean isNumeric(@Nullable String str) {
        if (str == null) return false;
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
