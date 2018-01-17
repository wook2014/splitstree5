package nexus.unaligned;

import jloda.util.parse.NexusStreamParser;
import nexus.Taxa;
import splitstree4.core.TaxaSet;

import java.io.PrintStream;
import java.io.Writer;

/**
 * Created by Daria on 15.09.2016.
 */
public interface Unaligned {

    /**
     * Identification string
     */
    String NAME = "Unaligned";

    /**********************************
     * FUNCTIONS FROM "SPLITS TREE 4" *
     **********************************/

    // MAIN DATA

    void hideTaxa(Taxa origTaxa, TaxaSet hiddenTaxa);

    Unaligned clone(Taxa taxa);

    /***************
     * INPUT OUTPUT *
     ***************/

    String toString();

    void read(NexusStreamParser np, Taxa taxa);

    void write(Writer w, Taxa taxa); //Write the characters block

    static void showUsage(PrintStream ps) {
        ps.println("BEGIN UNALIGNED;");
        ps.println("\t[DIMENSIONS NTAX=number-of-taxa;]");
        ps.println("\t[FORMAT");
        ps.println("\t    [DATATYPE={STANDARD|DNA|RNA|NUCLEOTIDE|PROTEIN}]");
        ps.println("\t    [RESPECTCASE]");
        ps.println("\t    [MISSING=symbol]");
        ps.println("\t    [SYMBOLS=\"symbol symbol ...\"]");
        ps.println("\t    [LABELS={LEFT|NO}]");
        ps.println("\t;]");
        ps.println("\tMATRIX");
        ps.println("\t    data-matrix");
        ps.println("\t;");
        ps.println("END;");
    }


    /*********************
     * GETTER AND SETTER *
     *********************/
    Format getFormat();

    int getNtax();

    void setNtax(int ntax);

    char get(int t, int p); //Get the matrix value.

    void set(int i, int j, char val); //Set the matrix value.

    char[] getRow(int i); //Get the row i of matrix (i.e. the sequence for tax i).

    String getRowAsString(int t);

    int getMaxLength(); //Returns the max number of characters in a row

    boolean getFormatSwitchValue(String name);

}
