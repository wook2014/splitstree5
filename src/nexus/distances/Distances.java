package nexus.distances;

import jloda.util.parse.NexusStreamParser;
import nexus.Taxa;
import splitstree4.core.TaxaSet;

import java.io.PrintStream;
import java.io.Writer;

/**
 * Created by Daria on 15.09.2016.
 */
public interface Distances {

    /**
     * Identification string
     */
    String NAME = "Distances";

    /**
     * clones a distances object
     *
     * @param taxa
     * @return a clone
     */
    Distances clone(Taxa taxa) ;

    /**
     * return the induced object obtained by hiding taxa
     *
     * @param origTaxa
     * @param hiddenTaxa
     */
    void hideTaxa(Taxa origTaxa, TaxaSet hiddenTaxa);

    /***************
     * INPUT OUTPUT *
     ***************/

    /**
     * Show the usage of distances block
     *
     * @param ps the print stream
     */
    void showUsage(PrintStream ps) ;

    /**
     * Produces a string representation of the distances object
     *
     * @return string representation
     */
    String toString() ;
    /**
     * Produces a string representation of the distances object
     *
     * @return string representation
     */
    String toString(Taxa taxa);
    /**
     * Write out matrix according to the specified format
     *
     * @param w    the writer
     * @param taxa the Taxa object
     */
    void write(Writer w, Taxa taxa);

    /**
     * Read a matrics of distances.
     *
     * @param np   the nexus streamparser
     * @param taxa the taxa
     */
    void read(NexusStreamParser np, Taxa taxa);

    /********************
    * GETTER AND SETTER *
     ********************/

    void setFormat(Format format);
    Format getFormat() ;

    int getNtax();
    void setNtax(int ntax) ;

    /**
     * Get the matrix value.
     *
     * @param i the row
     * @param j the colum
     * @return the matix value  matrix[i][j]
     */
    double get(int i, int j) ;
    /**
     * Set the matrix value.
     *
     * @param i   the row
     * @param j   the colum
     * @param val the matix value at row i and colum j
     */
    void set(int i, int j, double val);

    /**
     * Returns the complete matrix
     *
     * @return double[][] matrix
     */
    double[][] getMatrix();


    /**
     * Get the variance estimate
     *
     * @param i the row
     * @param j the colum
     * @return the variance estimate on the matrix[i][j];
     */
    double getVar(int i, int j) ;

    void setVar(int i, int j, double var) ;


    /**
     * gets the value of a format switch
     *
     * @param name
     * @return value of format switch
     */
    boolean getFormatSwitchValue(String name);

}
