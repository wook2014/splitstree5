package nexus.characters;

import jloda.util.parse.NexusStreamParser;
import jloda.util.parse.NexusStreamTokenizer;
import nexus.Taxa;
import splitstree4.core.SplitsException;
import splitstree4.core.TaxaSet;

import javax.swing.text.Document;
import java.io.PrintStream;
import java.io.Writer;
import java.util.List;

/**
 * Created by Daria on 15.09.2016.
 */
public interface Characters {

    String NAME = "Characters";

    int EXCLUDE_ALL_CONSTANT = -1; // 0: don't exclude, positive: exclude some, -1: exclude all
    //todo : private const
    //private static final boolean treatUnknownAsError = false;

    /**
     * clones a characters object
     *
     * @param taxa Taxa block that this characters block is associated to
     * @return a clone
     */
    Characters clone(Taxa taxa);


    /**
     * return the induced object obtained by hiding taxa
     *
     * @param origTaxa   original (full?) taxa block
     * @param hiddenTaxa set of taxa to be hidden
     */
    void hideTaxa(Taxa origTaxa, TaxaSet hiddenTaxa);


    /************************Character weights and state labels******************************/

    /**
     * hasCharweights
     * <p/>
     * Determines whether character weights are stored for this block
     *
     * @return boolean true if character weights are being stored for this block (they could still be
     *         all constant)
     */
    boolean hasCharweights() ;


    /************************ Colouring  and states ******************************/

    /**
     * Is this nucleotide data.
     *
     * @return boolean. True if datatype is RNA or DNA
     */
    boolean isNucleotides() ;

    /**
     * Computes the colors2symbols and symbols2colors maps
     * changed(17.08.2003) the following:  Since symbols are case sensetive we want to change this and assign colors to
     * to symbols wich assigns one color to the same symbols ignoring the case.
     * This maps are fixed for known datatypes.
     */
    void computeColors();


    boolean hasAmbigStates();


    /*****************************Ambiguity strings*******************************************************/

    /**
     * hasAmbigString
     * <p/>
     * In the matrix, any ambiguity characters are coded as a '?' and the set of states they represent
     * is stored as a string. To check wether there is ambiguity information stored for a particular
     * sequence and site, use 'hasAmbigString'.
     *
     * @param seq sequence
     * @param c   character (site)
     * @return boolean  true if there is an ambiguity string associated with this character
     */
    boolean hasAmbigString(int seq, int c);

    /*****************************Masks*******************************************************/

    /**
     * Returns true, if specified site is masked
     *
     * @param p the index of that site
     * @return true, if site is masked
     */
    boolean isMasked(int p) ;

    /**
     * Clears the mask
     */
    void clearMask() ;

    /**
     * remove all masked sites from the dataset
     *
     * @return number removed
     */
    int removeMaskedSites();

    /**
     * remove all sites mentioned in the mask
     *
     * @param theMask boolean array, with true indicating that the site is masked
     * @return number of sites removed
     */
    int removeMaskedSites(boolean[] theMask);

    /**
     * **********************************INPUT OUTPUT******************************************************
     */

    /**
     * Show the usage of this block
     *
     * @param ps the print stream
     */
    void showUsage(PrintStream ps) ;

    /**
     * read the characters block
     *
     * @param np   NexusStreamParser
     * @param taxa taxa block
     */
    void read(NexusStreamParser np, Taxa taxa) ;


    /**
     * Read the characters block
     *
     * @param np   the nexus streamparser
     * @param taxa the taxa block
     * @param doc  needed for progress bar   //ToDO: Replace with progress bar reference
     */
    void read(NexusStreamParser np, Taxa taxa, Document doc) ;

    /**
     * Write the characters block
     *
     * @param w    the writer
     * @param taxa the taxa
     */
    void write(Writer w, Taxa taxa);


    /**
     * Write the characters and taxa in a data block. Added it for backward compatibility and
     * for MrBayes.
     *
     * @param w    the writer
     * @param taxa the taxa
     */
    void writeDataBlock(Writer w, Taxa taxa);

    /**
     * produces a full string representation of this nexus block
     *
     * @param taxa Taxa block
     * @return object in necus
     */
    String toString(Taxa taxa);

    /**
     * returns a row of data as a string
     *
     * @param t sequence number
     * @return a sequence
     */
    String getRowAsString(int t) ;


    /*********************
     * GETTER AND SETTER *
     *********************/

    void setFormat(Format format);
    Format getFormat() ;

    Properties getProperties();
    void setProperties(Properties properties);

    int getNtax();
    void setNtax(int ntax);

    int getNchar();
    void setNchar(int i) ;

    boolean getFormatSwitchValue(String name);

    /******************** Character weights and state labels ***********************/

    /**
     * Get the characterweight of a specific character.
     *
     * @param c the index of that character
     * @return weight for that character
     */
    double getCharWeight(int c);

    /**
     * Set the characterweight of a specific character.
     *
     * @param c the index of that character
     * @param x the weight
     */
    void setCharWeight(int c, double x);

    /*********************** Colouring and states ***********************/

    /**
     * Gets the name of a character, as specified in the CharStateLabels
     *
     * @param c number of character
     * @return String name of character, of null if there is non specified.
     */
    String getCharLabel(int c) ;


    /**
     * Returns the color of a character. Colors start at 1
     *
     * @param ch a character
     * @return the color of the character or -1 if the character is not found.
     */
    int getColor(char ch) ;

    /**
     * Returns a Array with the symbols of the color
     *
     * @param color a color*
     * @return an Array with the List of DataTypesChar matching the color
     */
    Object[] getSymbols(int color);

    /**
     * Gets the number of colors.
     *
     * @return the number of colors
     */
    int getNcolors() ;

    /**************************Access to elements in the alignment ****************/


    /**
     * Get the matrix value.
     *
     * @param seq  the taxon
     * @param site the position
     * @return the matrix value  matrix[t][p]
     */
    char get(int seq, int site);


    /**
     * Set the matrix value.
     *
     * @param seq  the row
     * @param site the colum
     * @param val  the matix value at row seq and colum site
     */
    void set(int seq, int site, char val);


    /**
     * Ambiguous states are replaced by the missing character, but stored in replacedStates.
     * This routine returns the state that was in this sequence at this position in the original
     * file.
     *
     * @param seq  sequence number
     * @param site the site (character)
     * @return char.  missing character returned.
     */
    char getOriginal(int seq, int site) ;


    /**
     * Get a copy of row seq of matrix (seq.e. the sequence for tax seq). NOTE: This is different than in previous versions.
     *
     * @param seq the row
     * @return the matix row seq
     */
    char[] getRow(int seq) ;

    /**
     * gets a copy of the named column of the alignment
     *
     * @param pos
     * @return column of characters block
     */
    String getColumn(int pos) ;


    /**
     * returns a string consisting of all states for this row that are listed in toShow
     *
     * @param seq    The number of the sequence
     * @param toShow List (of Integer) specifying sites to show
     * @return boolean
     */
    String getRowSubset(int seq, List toShow) ;

    /***********************Ambiguity strings************************/

    /**
     * getAmbigString
     * <p/>
     * In the matrix, any ambiguity characters are coded as a '?' and the set of states they represent
     * is stored as a string. This method gets the string of characters associated to a position in a given
     * sequence, or null if there are none.
     * //TODo: Maybe should return original character?
     *
     * @param seq sequence
     * @param c   character (site)
     * @return string containing original states, or null if there are no ambiguity characters stored.
     */
    String getAmbigString(int seq, int c) ;

    /*********************** Masks ***********************/

    /**
     * Get the number of active, ie unmasked, characters.
     *
     * @return the number of active characters
     */
    int getNactive();


    /**
     * Sets the masked status of a site
     *
     * @param p      the index of the site
     * @param masked the status
     */
    void setMasked(int p, boolean masked);

    /**
     * Gets the mask array
     *
     * @return the mask
     */
    boolean[] getMask() ;

}
