package nexus;

import jloda.phylo.PhyloTree;
import jloda.util.parse.NexusStreamParser;
import nexus.characters.Characters;
import splitstree4.core.TaxaSet;
import splitstree4.util.Partition;

import java.io.PrintStream;
import java.io.Writer;
import java.util.Set;

/**
 * Created by Daria on 19.09.2016.
 */
public interface Sets {

    /**
     * Identification string
     */
    String NAME = "Sets";

    /**
     * Clear all  sets and partitions
     */
    void clear() ;

    /**
     * Clear all taxa sets
     */
    void clearTaxaSets() ;

    /**
     * adds a new tax set
     *
     * @param name   Name of taxa set being added
     * @param taxSet Taxa set being added
     * @param taxa   Taxa block
     * @return true if a set with this name already exists (
     *         in which case it is replaced)
     */
    boolean addTaxSet(String name, TaxaSet taxSet, Taxa taxa) ;

    /**
     * adds a new tax set
     *
     * @param name Name of taxa set being added
     * @param set  names of taxa
     * @return true if a set with this name already exists (
     *         in which case it is replaced)
     */
    boolean addTaxSet(String name, Set<String> set) ;

    /**
     * adds a a new taxonomy
     *
     * @param name     Name of taxa set being added
     * @param taxonomy Taxa set being added
     * @return true if a set with this name already exists (
     *         in which case it is replaced)
     */
    boolean addTaxonomy(String name, PhyloTree taxonomy) ;

    /**
     * add a character
     *
     * @param name
     * @param set
     * @return true, if name already used
     */
    boolean addCharSet(String name, Set<Integer> set) ;

    /**
     * add a character
     *
     * @param name
     * @param first
     * @param last
     * @return true, if name already used
     */
    boolean addCharSet(String name, int first, int last);

    /**
     * add character partition
     *
     * @param name
     * @param partition
     * @return true, if name already used
     */
    boolean addCharPartition(String name, Partition partition) ;

    /**
     * removes taxa set
     *
     * @param name
     * @return true iff set with that name was present
     */
    boolean removeTaxSet(String name) ;

    /**
     * removes taxonomy
     *
     * @param name
     * @return true iff set with that name was present
     */
    boolean removeTaxonomy(String name);

    /**
     * removes char set
     *
     * @param charSetName
     * @return true iff set with that name was present
     */
    boolean removeCharSet(String charSetName) ;
    /**
     * removes char partition
     *
     * @param charPartName
     * @return true iff set with that name was present
     */
    boolean removeCharPart(String charPartName);

    /***************
     * INPUT OUTPUT *
     ***************/

    // usage
    void showUsage(PrintStream ps) ;

    //TODO is private in splitstree4???
    String toString(Set<Integer> intSet) ;
    String toString(Partition partition) ;

    /**
     * write a sets block
     *
     * @param w
     * @param taxa
     * @throws java.io.IOException
     */
    void write(Writer w, Taxa taxa) ;

    /**
     * reads a sets block
     *
     * @param np
     * @param taxa
     * @param chars
     */
    void read(NexusStreamParser np, Taxa taxa, Characters chars) ;

    /********************
    * GETTER AND SETTER *
     ********************/

    /**
     * get number of taxa sets
     *
     * @return int number of taxa sets
     */
    int getNumTaxSets();

    /**
     * get number of taxonomys
     *
     * @return int - number of taxonomys
     */
    int getNumTaxonomys();

    /**
     * get number of character sets
     *
     * @return int number of character sets
     */
    int getNumCharSets() ;

    /**
     * get number of character partitions
     *
     * @return int number of character partitions
     */
    int getNumCharPartitions() ;

    /**
     * Returns the taxonomy associated with the given name
     *
     * @param name
     * @return taxa set. Will return null if name does not appear
     *         or if name is present, but there is no set for that name.
     */
    PhyloTree getTaxonomy(String name) ;

    /**
     * Returns the taxa set associated with the given name
     *
     * @param name
     * @return set of names of taxa. Will return null if name does not appear
     *         or if name is present, but there is no set for that name.
     */
    Set<String> getTaxSet(String name) ;

    /**
     * Returns the taxa set associated with the given name
     *
     * @param name
     * @param taxa
     * @return taxa set. Will return null if name does not appear
     *         or if name is present, but there is no set for that name.
     */
    TaxaSet getTaxSet(String name, Taxa taxa) ;

    /**
     * gets the set of character set names
     *
     * @return character set names
     */
    Set getCharSetNames() ;

    /**
     * gets the set of character partition names
     *
     * @return character partition names
     */
    Set getCharPartitionNames() ;


    /**
     * gets the set of taxa set names
     *
     * @return taxa set names
     */
    Set<String> getTaxaSetNames() ;

    /**
     * gets the set of taxonomy names
     *
     * @return taxonomy names
     */
    Set<String> getTaxonomyNames() ;

    /**
     * gets the named char set
     *
     * @param charSetName
     * @return Set
     */
    Set<Integer> getCharSet(String charSetName) ;

    /**
     * gets the named char partition
     *
     * @param charPartitionName
     * @return Partition
     */
    Partition getCharPartition(String charPartitionName) ;
}
