package nexus.quartets;

import jloda.util.parse.NexusStreamParser;
import nexus.Taxa;
import splitstree4.core.Quartet;
import splitstree4.core.TaxaSet;

import java.io.PrintStream;
import java.io.Writer;
import java.util.Iterator;

/**
 * Created by Daria on 19.09.2016.
 */
public interface Quartets {

    /**
     * Identification string
     */
    String NAME = "Quartets";

    /**
     * clones these quartets (deep clone)
     *
     * @param taxa
     * @return the clone
     */
    Quartets clone(Taxa taxa);

    /**
     * Appends the specified quartet to the end of this list.
     *
     * @param q quartet to be appended to this list.
     * @return <tt>true</tt> (as per the general contract of Collection.add).
     */
    boolean add(Quartet q);

    /**
     * Returns <tt>true</tt> if this set contains the specified element.  More
     * formally, returns <tt>true</tt> if and only if this set contains an
     * element <code>e</code> such that <code>(o==null ? e==null :
     * o.equals(e))</code>.
     *
     * @param o element whose presence in this set is to be tested.
     * @return <tt>true</tt> if this set contains the specified element.
     * @throws java.lang.ClassCastException   if the type of the specified element
     *                                        is incompatible with this set (optional).
     * @throws java.lang.NullPointerException if the specified element is null and this
     *                                        set does not support null elements (optional).
     */
    boolean contains(Object o);

    /**
     * Returns an iterator over the elements in this set.  The elements are
     * returned in no particular order (unless this set is an instance of some
     * class that provides a guarantee).
     *
     * @return an iterator over the elements in this set.
     */
    Iterator iterator() ;

    /**
     * Returns an array containing all of the quartets in this set.
     * Obeys the general contract of the <tt>Collection.toArray</tt> method.
     *
     * @return an array containing all of the elements in this set.
     */
    Quartet[] toArray() ;


    /**
     * hide some taxa
     *
     * @param origTaxa
     * @param exTaxa
     */
    void hideTaxa(Taxa origTaxa, TaxaSet exTaxa);

    /**
     * Returns a human-readable string describing this object
     *
     * @return log string
     */
    String toLogString();



    /***************
     * IO Handling *
     ***************/

    /**
     * Show the usage of this block
     *
     * @param ps the print stream
     */
    void showUsage(PrintStream ps);

    /**
     * Produces a string representation of the quartets object
     *
     * @return string representation
     */
    String toString() ;

    /**
     * Read a matrics of quartets
     *
     * @param np   the nexus streamparser
     * @param taxa the taxa
     */
    void read(NexusStreamParser np, Taxa taxa);

    /**
     * Write the characters block
     *
     * @param w    the writer
     * @param taxa the taxa
     */
    void write(Writer w, Taxa taxa) ;


    /**
     * Write the characters block
     *
     * @param w    the writer
     * @param nTaxa the number of taxa in the object
     */
    void write(Writer w, int nTaxa);


    /**
     * Write a matrics in standard format.
     *
     * @param w the writer
     */
    void writeMatrix(Writer w);

    /**
     * Read a matrix in standard format
     *
     * @param np   the nexus parser
     * @param taxa the taxa
     */
    void readMatrix(NexusStreamParser np, Taxa taxa, int expectedQuartets);

    /****************
     * Input output *
     ****************/

    /**
     * Return the format object
     *
     * @return the format object
     */
    Format getFormat() ;

    /**
     * Get the number of quartets
     *
     * @return the number of quartets
     */
    int getSize() ;

    /**
     * sets a aquartet
     *
     * @param index the index
     * @param q     the quartet
     */
    void set(int index, Quartet q);

    /**
     * Returns the quartet at the specified position in this list.
     *
     * @param index the number of the quartet 1..size()
     * @return the quartet at the specified position in this list.
     */
    Quartet get(int index);

    /**
     * gets the value of a format switch
     *
     * @param name
     * @return value of format switch
     */
    boolean getFormatSwitchValue(String name);

}
