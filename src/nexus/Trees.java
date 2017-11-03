package nexus;

import jloda.phylo.PhyloTree;
import jloda.util.parse.NexusStreamParser;
import splitstree4.core.TaxaSet;

import java.io.PrintStream;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

/**
 * Created by Daria on 15.09.2016.
 */
public interface Trees {

    /**
     * Identification String
     */
    String NAME = "Trees";

    /**
     * clears all the data associated with this trees block
     */
    void clear();

    /**
     * clones a trees object
     *
     * @param taxa
     * @return a clone
     */
    Trees clone(Taxa taxa) ;


    /**
     * copies a trees object
     *
     * @param taxa
     * @param src  source tree
     */
    void copy(Taxa taxa, Trees src);

    /**
     * returns the index of the named tree.
     * Trees are numbered 1 to ntrees
     *
     * @param name
     * @return index of named tree
     */
    int indexOf(String name);


    /**
     * remove the tree at index i.
     * The index must be between 1 and ntrees
     *
     * @param i
     */
    void removeTree(int i) ;

    /**
     * Adds a tree to the list of trees. If this is called to add the first
     * tree to the trees block, then the tree nodes must be labeled with
     * taxon names or integers 1..ntax. If this is not the case, then use
     * the other addTree method described below. Subsequent trees can be
     * added by this method regardless of which labels are used for nodes,
     * as long as they are compatible with the initial translation table.
     *
     * @param name the name of the tree
     * @param tree the phylogenetic tree
     * @param taxa the taxa block
     */
    void addTree(String name, PhyloTree tree, Taxa taxa);


    /**
     * Determines whether a given tree and given translation map are
     * compatible with one another
     * If is partial, sets the trees block partial variable
     *
     * @param tree  the phylogenetic tree
     * @param trans the map from taxon labels to node labels
     */

    void checkTranslation(PhyloTree tree, Map trans);

    /**
     * is translate one-to-one?
     *
     * @param translate
     * @return true, if one-to-one
     */
    boolean translateIsOneToOne(Map<String, String> translate);

    /**
     * Returns true if tree i is rooted, else false
     *
     * @param i number of the tree
     * @return true if tree i is rooted
     */
    boolean isRooted(int i) ;

    /**
     * induces trees not containing the hidden taxa
     *
     * @param origTaxa
     * @param hiddenTaxa
     */
    void hideTaxa(Taxa origTaxa, TaxaSet hiddenTaxa) ;

    /**
     * restores the original splits
     *
     * @param originalTaxa
     */
    void restoreOriginal(Taxa originalTaxa) ;

    /**
     * changes all node labels using the mapping old-to-new
     *
     * @param old2new maps old names to new names
     */
    void changeNodeLabels(Map old2new) ;

    /***************
     * INPUT OUTPUT *
     ***************/

    /**
     * Writes trees taxa object in nexus format
     *
     * @param w a writer
     */
    void write(Writer w, Taxa taxa) ;

    /**
     * Reads a tree object in NexusBlock format
     *
     * @param np   nexus stream parser
     * @param taxa the taxa block
     */
    void read(NexusStreamParser np, Taxa taxa) ;

    /**
     * Produces a string representation of a NexusBlock object
     *
     * @return object in nexus format
     */
    String toString(Taxa taxa) ;

    /**
     * show the usage of this block
     *
     * @param ps the print stream
     */
    void showUsage(PrintStream ps);

    /*********************
     * GETTER AND SETTER *
     *********************/

    /**
     * Get the number of trees
     *
     * @return number of trees
     */
    int getNtrees();

    /**
     * Returns the i-th tree name.
     * Trees are numbered 1 to ntrees
     *
     * @param i the number of the tree
     * @return the i-th tree name
     */
    String getName(int i);

    /**
     * sets the i-th tree name
     *
     * @param i
     * @param name
     */
    void setName(int i, String name);

    /**
     * Returns the i-th tree taxaset.
     * Trees are numbered 1 to ntrees
     *
     * @param i the number of the tree
     * @return the i-th tree taxaset
     */
    TaxaSet getTaxaSet(int i);

    /**
     * sets the i-th tree taxaset
     *
     * @param i
     * @param taxaset
     */
    void setTaxaSet(int i, TaxaSet taxaset);

    /**
     * Returns the i-th tree
     *
     * @param i the number of the tree
     * @return the i-th tree
     */

    PhyloTree getTree(int i);

    /**
     * Returns the nexus flag [&R] indicating whether the tree should be considered
     * as rooted
     *
     * @param i
     * @return String  Returns [&R] if rooted, and "" otherwise.
     */
    String getFlags(int i);

    /**
     * Gets the taxon-label to node-label map
     *
     * @return the map
     */
    Map getTranslate() ;

    /**
     * sets the node-label to taxon translation map
     *
     * @param trans
     */
    void setTranslate(Map<String, String> trans);

    /**
     * Returns the set of taxa associated with a given node-label
     *
     * @param nlab the node label
     * @return the set of taxa mapped to the given node label
     */
    TaxaSet getTaxaForLabel(Taxa taxa, String nlab);

    /**
     * are trees considered rooted? If yes, then any divertex root is preserved
     *
     * @return true, if rooted
     */
    boolean getRooted() ;

    /**
     * are trees considered rooted?
     *
     * @param rooted
     */
    void setRooted(boolean rooted) ;

    /**
     * does trees block contain a partial tree?
     *
     * @return true, if contains a partial tree
     */
    boolean getPartial() ;

    /**
     * does trees block contain a partial tree
     *
     * @param partial
     */
    void setPartial(boolean partial);

    /**
     * returns the set of taxa contained in this tree.
     *
     * @param taxa  original taxa
     * @param which tree
     * @return set of taxa not present in this tree. Uses original numbering
     */
    TaxaSet getTaxaInTree(Taxa taxa, int which);

    /**
     * gets the support of this tree, that is, the set of taxa mentioned in it
     *
     * @param taxa
     * @param which tree
     * @return the set of taxa mentioned in this tree
     */
    TaxaSet getSupport(Taxa taxa, int which);

    /**
     * gets the value of a format switch
     *
     * @param name
     * @return value of format switch
     */
    boolean getFormatSwitchValue(String name);

    /**
     * Determine the set of taxa for partial trees.
     * If the block contains partial trees, then the translate statement must mention all
     * taxa. We use this info to build a taxa block
     *
     * @param taxa
     * //@throws SplitsException
     */
    void setTaxaFromPartialTrees(Taxa taxa) ;

    /**
     * returns the original set of trees or null
     *
     * @return original trees or null
     */
    Trees getOriginal();

    /**
     * save the original trees
     *
     * @param originalTaxa
     */
    void setOriginal(Taxa originalTaxa) ;


    /**
     * sets the translate map to the identity mapping taxa->taxa
     *
     * @param taxa
     */
    void setIdentityTranslate(Taxa taxa) ;

    /**
     * Sets the translate map to taxaid->taxa
     *
     * @param taxa
     */
    void setNumberedIdentityTranslate(Taxa taxa) ;

}
