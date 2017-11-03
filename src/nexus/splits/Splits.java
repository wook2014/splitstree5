package nexus.splits;

import jloda.util.parse.NexusStreamParser;
import nexus.Taxa;
import splitstree4.core.SplitsSet;
import splitstree4.core.TaxaSet;
import splitstree4.util.Interval;

import java.io.PrintStream;
import java.io.Writer;
import java.util.BitSet;

/**
 * Created by Daria on 19.09.2016.
 */
public interface Splits {

    /**
     * Identification string
     */
    String NAME = "Splits";

    /**
     * Adds splits from a splits set
     *
     * @param splitsset the splits to be added
     */
    void addSplitsSet(SplitsSet splitsset);


    /**
     * Clears the list of splits
     */
    void clear() ;

    /**
     * Adds a split
     *
     * @param A one side of the split
     */
    void add(TaxaSet A);

    /**
     * Adds a split
     *
     * @param A      one side of the split
     * @param weight the weight
     */
    void add(TaxaSet A, float weight) ;

    /**
     * Adds a split
     *
     * @param A          one side of the split
     * @param weight     the weight
     * @param confidence
     */
    void add(TaxaSet A, float weight, float confidence) ;

    /**
     * Adds a split
     *
     * @param A      one side of the split
     * @param weight the weight
     * @param lab    the label
     */
    void add(TaxaSet A, float weight, String lab);

    /**
     * Adds a split
     *
     * @param A          one side of the split
     * @param weight     the weight
     * @param confidence in split
     * @param lab        the label
     */
    void add(TaxaSet A, float weight, float confidence, String lab) ;

    /**
     * Adds a split
     *
     * @param A
     * @param weight
     * @param confidence
     * @param interval
     * @param lab
     */
    void add(TaxaSet A, float weight, float confidence, Interval interval, String lab) ;

    /**
     * Removes a split. This will change the indices of other splits in the block (after i)
     *
     * @param i index of split to remove.
     */
    void remove(int i);

    /**
     * Returns a human-readable string describing this split
     *
     * @param i
     * @return split
     */
     String toLogString(int i);
    /**
     * Returns a human-readable string describing this object
     *
     * @return object
     */
     String toLogString() ;


    /**
     * Gets the first index of a split with the given label
     *
     * @param lab the label
     * @return the index of the first split with the given index, or 0
     */
    int indexOf(String lab);

    /**
     * Returns a clone of this block
     *
     * @return a full clone
     */
    Splits clone(Taxa taxa);

    /**
     * copy splits
     *
     * @param taxa
     * @param source
     */
    void copy(Taxa taxa, Splits source);


    /**
     * induces splits not containing the hidden taxa
     *
     * @param origTaxa
     * @param hiddenTaxa
     */
    void hideTaxa(Taxa origTaxa, TaxaSet hiddenTaxa);
    /**
     * restores the original splits
     *
     * @param originalTaxa
     */
    void restoreOriginal(Taxa originalTaxa) ;

    /**
     * hide the named splits
     *
     * @param taxa
     * @param toHide
     */
    void hideSplits(Taxa taxa, BitSet toHide);


    /***************
    * INPUT OUTPUT *
     ***************/

    /**
     * Show the usage of splits block
     *
     * @param ps the print stream
     */
    void showUsage(PrintStream ps);

    /**
     * Writes a splits object in nexus format
     *
     * @param w a writer
     */
    void write(Writer w, Taxa taxa);

    /**
     * Writes a splits object in nexus format
     *
     * @param w a writer
     */
    void write(Writer w, int nTaxa);

    /**
     * Reads a splits object in NexusBlock format
     *
     * @param np   nexus stream parser
     * @param taxa the taxa
     */
    void read(NexusStreamParser np, Taxa taxa);

    /**
     * Produces a string representation of a NexusBlock object
     *
     * @return string representation
     */
    String toString() ;


    /**
     * Produces a string representation of a NexusBlock object
     *
     * @return string representation
     */
    String toString(Taxa taxa);



    /********************
    * GETTER AND SETTER *
     ********************/

    /**
     * Sets the weight of a split
     *
     * @param i   index of the split between 1..nsplits
     * @param wgt the weight
     */
    void setWeight(int i, float wgt) ;

    /**
     * Gets the splits set
     *
     * @return the splits set
     */
    SplitsSet getSplitsSet();

    /**
     * Return the format object
     *
     * @return the format object
     */
    Format getFormat() ;

    /**
     * Returns the properties object
     *
     * @return the properties object
     */
    Properties getProperties();

    /**
     * Gets the weight threshold set below which splits are ignored
     *
     * @return the threshold
     */
    float getThreshold();

    /**
     * Sets the weight threshold set below which splits are ignored
     *
     * @param t the threshold
     */
    void setThreshold(float t) ;

    /**
     * Get the number of taxa.
     *
     * @return number of taxa
     */
    int getNtax() ;
    /**
     * Set the number of taxa
     *
     * @param ntax the number of taxa
     */
    void setNtax(int ntax);

    /**
     * Get the number of splits.
     *
     * @return number of splits
     */
    int getNsplits() ;

    /**
     * Set the number of splits.
     *
     * @param nsplits number of splits
     */
    void setNsplits(int nsplits);

    /**
     * Sets the cyclic ordering of the taxa. Use indices 1...ntax
     *
     * @param cycle a permutation of the numbers 1...ntax
     */
    void setCycle(int[] cycle);

    /**
     * Gets the cyclic ordering of the taxa. Use indices 1...ntax
     *
     * @return the cyclic ordering as a permutation of 1..ntax
     */
    int[] getCycle();


    /**
     * Gets the label of the i-th split
     *
     * @param i the index of the split
     * @return the split label
     */
    String getLabel(int i);

    /**
     * Sets the label of the i-th taxon
     *
     * @param i   the index of the taxon
     * @param lab the label
     */
    void setLabel(int i, String lab);

    /**
     * Returns the i-th split
     *
     * @param i the index of the split between 1..nsplits
     * @return the taxa set of the split
     */
    TaxaSet get(int i);

    /**
     * Returns the i-th weight
     *
     * @param i the index of the weight between 1..nsplits
     * @return the taxa set of the weight
     */
    float getWeight(int i) ;

    /**
     * returns the confidence in splits i
     *
     * @param i split
     * @return confidence
     */
    float getConfidence(int i) ;

    void setConfidence(int i, float confidence);

    /**
     * Return the confidence interval for split i
     */
    Interval getInterval(int i);

    void setInterval(int i, Interval interval);

    /**
     * save a copy of myself into original splits.
     *
     * @param originalTaxa
     */
    void setOriginal(Taxa originalTaxa);

    /**
     * get the original splits
     *
     * @return original splits
     */
    Splits getOriginal();

    /**
     * gets the value of a format switch
     *
     * @param name
     * @return value of format switch
     */
    boolean getFormatSwitchValue(String name);

}
