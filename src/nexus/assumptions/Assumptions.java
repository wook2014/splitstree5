package nexus.assumptions;

import jloda.util.parse.NexusStreamParser;
import nexus.Taxa;
import splitstree4.algorithms.Transformation;
import splitstree4.algorithms.characters.CharactersTransform;
import splitstree4.algorithms.distances.DistancesTransform;
import splitstree4.algorithms.quartets.QuartetsTransform;
import splitstree4.algorithms.reticulate.ReticulateTransform;
import splitstree4.algorithms.splits.SplitsTransform;
import splitstree4.algorithms.trees.TreesTransform;
import splitstree4.algorithms.unaligned.UnalignedTransform;
import splitstree4.core.TaxaSet;

import javax.swing.text.Document;
import java.io.PrintStream;
import java.io.Writer;
import java.util.List;

/**
 * Created by Daria on 19.09.2016.
 */
public interface Assumptions {

    String NAME = "st_Assumptions"; // Identification string

    String USE_ALL = "all"; // use all taxa or characters

    // graph layout strategies:
    int RECOMPUTE = 0;
    int STABILIZE = 1;
    int SNOWBALL = 2;
    int KEEP = 3;

    /**************************************
     * FUNCTIONS FROM THE "SPLITS TREE 4" *
     **************************************/

    /**
     * has data been marked uptodate i.e. in a complete input file?
     *
     * @return true, if data doesn't need immediate updating
     */
    boolean isUptodate() ;

    /**
     * clear the list of dirty blocks
     */
    void clearFirstDirtyBlock() ;

    /**
     * update the first dirty block with the given name
     *
     * @param name the name of the dirty block
     */
    void updateFirstDirtyBlock(String name) ;


    /**
     * clone the assumptions block
     *
     * @param taxa
     * @return a clone
     */
    Assumptions clone(Taxa taxa) ;

    /**
     * determines whether this is a currently set transform
     *
     * @param trans
     * @return true if currently set
     */
    boolean isSetTransform(Transformation trans) ;

    /***************
     * INPUT OUTPUT *
     ***************/

    /**
     * Read the assumptions block.
     *
     * @param np the nexus parser
     */
    void read(NexusStreamParser np, Taxa taxa) ;

    /**
     * Write the assumptions block.   Suppress assumptions for undefined blocks.
     *
     * @param w   the writer
     * @param doc the document
     */
    void write(Writer w, Document doc) ;

    /**
     * writeInfoFile the assumptions block in full.  Show all assumptions, whether blocks are defined or not
     *
     * @param w
     * @param taxa
     */
    void write(Writer w, Taxa taxa) ;

    /**
     * Produces a string representation of a NexusBlock object
     *
     * @return object in nexus format
     */
    String toString(Document doc) ;

    /**
     * Shows assumptions. Shows all assumptions, whether blocks are defined or not
     *
     * @return object in nexus format
     */
    String toString(Taxa taxa) ;

    /**
     * Show the usage of this block
     *
     * @param ps the PrintStream
     */
    void showUsage(PrintStream ps) ;


    /********************
    * GETTER NAD SETTER *
     ********************/

    /**
     * data is update-to-date and next call to update will be ignored
     *
     * @param uptodate
     */
    void setUptodate(boolean uptodate) ;

    /**
     * Gets the name of the unaligned transform
     *
     * @return the unaligned transform
     */
    String getUnalignedTransformName() ;

    /**
     * Sets the unaligned transform
     *
     * @param trans the transform
     */
    void setUnalignedTransformName(String trans) ;

    /**
     * Sets the unaligned transform parameters
     *
     * @param param the transform parameters
     */
    void setUnalignedTransformParam(String param) ;

    /**
     * Returns the current unaligned transform parameters
     *
     * @return current unaligned transform parameters
     */
    String getUnalignedTransformParam() ;

    /**
     * Returns the current characters transform parameters
     *
     * @return current characters transform parameters
     */
    String getCharactersTransformParam() ;

    /**
     * Returns the current distances transform parameters
     *
     * @return current distances transform parameters
     */
    String getDistancesTransformParam() ;

    /**
     * Returns the current splits transform parameters
     *
     * @return current splits transform parameters
     */
    String getSplitsTransformParam() ;


    /**
     * Gets charTransform
     *
     * @return the charTransform
     */
    String getCharactersTransformName() ;

    /**
     * Sets the charTransform
     *
     * @param transform is charTransform
     */
    void setCharactersTransformName(String transform) ;

    /**
     * Sets the charTransformParameter
     *
     * @param param is charTransformParameter
     */
    void setCharactersTransformParam(String param) ;

    /**
     * Gets distTransform
     *
     * @return the distTransform
     */
    String getDistancesTransformName() ;

    /**
     * Sets the distTransform
     *
     * @param transform is distTransform
     */
    void setDistancesTransformName(String transform) ;

    /**
     * Sets the distTransformParam
     *
     * @param param is distTransformParam
     */
    void setDistancesTransformParam(String param) ;

    /**
     * Gets the quartets transform name
     *
     * @return the quartets transform
     */
    String getQuartetsTransformName() ;

    /**
     * Sets the quartests transform
     *
     * @param trans the quartets transform
     */
    void setQuartetsTransformName(String trans) ;

    /**
     * Gets the quarets transforma parameter
     *
     * @return the quartets trajsform parameter
     */
    String getQuartetsTransformParam() ;

    /**
     * Sets the quartet transform parameters
     *
     * @param param the parameter
     */
    void setQuartetsTransformParam(String param) ;

    /**
     * gets trees transform parameter string
     *
     * @return the parameters
     */
    String getTreesTransformParam() ;

    /**
     * sets the trees transform paramters
     *
     * @param param
     */
    void setTreesTransformParam(String param) ;

    /**
     * Gets the tree transform name
     *
     * @return the name
     */
    String getTreesTransformName() ;

    /**
     * Sets the trees transform name
     *
     * @param name
     */
    void setTreesTransformName(String name) ;

    /**
     * Gets splitsTransform
     *
     * @return the splitsTransform
     */
    String getSplitsTransformName() ;

    /**
     * Sets the splitsTransform
     *
     * @param trans is splitsTransform
     */
    void setSplitsTransformName(String trans) ;

    /**
     * Sets the splitsTransformParam
     *
     * @param param is splitsTransformParam
     */
    void setSplitsTransformParam(String param) ;

    /**
     * Gets the name of the reticulate transform
     *
     * @return the reticulate transform
     */
    String getReticulateTransformName() ;

    /**
     * Sets the reticulateTransform
     *
     * @param trans is reticulateTransform
     */
    void setReticulateTrasformName(String trans) ;

    /**
     * Returns the current reticulate transform parameters
     *
     * @return current reticulate transform parameters
     */
    String getReticulateTransformParam() ;

    /**
     * Sets the reticulateTransformParam
     *
     * @param param is reticulateTransformParam
     */
    void setReticulateTransformParam(String param) ;


    /**
     * Gets the excludeGaps
     *
     * @return the excludeGaps
     */
    boolean getExcludeGaps() ;

    /**
     * Sets the excludeGaps
     *
     * @param excGaps is excludeGaps
     */
    void setExcludeGaps(boolean excGaps) ;

    /**
     * Gets the excludeMissing
     * Threshold for missing data in characters. Characters with more than this proportion of missing
     * data are excluded. Hence 1.0 means none are excluded.
     * @return the excludeMissing
     */
    double getExcludeMissing() ;

    /**
     * Sets the excludeMissing
     * Threshold for missing data in characters. Characters with more than this proportion of missing
     * data are excluded. Hence 1.0 means none are excluded.
     * @param excMissing is excludeMissing
     */
    void setExcludeMissing(double excMissing) ;

    /**
     * Gets the excludeNonParsimony
     *
     * @return the excludeNonParsimony
     */
    boolean getExcludeNonParsimony() ;

    /**
     * Sets the excludeNonParsimony
     *
     * @param excNonParsi is excludeNonParsimony
     */
    void setExcludeNonParsimony(boolean excNonParsi) ;

    /**
     * Gets the excludeCodon1
     *
     * @return the excludeCodon1
     */
    boolean getExcludeCodon1() ;

    /**
     * Sets the excludeCodon1
     *
     * @param excCodon1 is excludeCodon1
     */
    void setExcludeCodon1(boolean excCodon1) ;

    /**
     * Gets the excludeCodon2
     *
     * @return the excludeCodon2
     */
    boolean getExcludeCodon2() ;

    /**
     * Sets the excludeCodon2
     *
     * @param excCodon2 is excludeCodon2
     */
    void setExcludeCodon2(boolean excCodon2) ;

    /**
     * Gets the excludeCodon3
     *
     * @return the excludeCodon3
     */
    boolean getExcludeCodon3() ;

    /**
     * Sets the excludeCodon3
     *
     * @param excCodon3 is excludeCodon3
     */
    void setExcludeCodon3(boolean excCodon3);

    /**
     * Gets the excludeConstant
     *
     * @return the excludeConstant
     */
    int getExcludeConstant();

    /**
     * Sets the excludeConstant
     *
     * @param excConstant is excludeConstant
     */
    void setExcludeConstant(int excConstant) ;

    /**
     * Gets the set of taxa that are excluded from all computations
     *
     * @return the list of excluded taxa
     */
    TaxaSet getExTaxa() ;

    /**
     * Gets the set of character positions that are excluded from all
     * computations
     *
     * @return the list of excluded characters
     */
    List<Integer> getExChar() ;

    /**
     * Sets the set of taxa that are excluded from all computations
     *
     * @param extaxa the set of excluded taxa
     */
    void setExTaxa(TaxaSet extaxa) ;

    List<String> getUseTaxaSets() ;

    void setUseTaxaSets(List<String> useTaxaSets) ;

    /**
     * Sets the list of character positions that are excluded from all
     * computations
     *
     * @param exchar the list of excluded characters
     */
    void setExChar(List<Integer> exchar);


    List<String> getUseCharSets() ;

    void setUseCharSets(List<String> useCharSets);

    /**
     * sets the list of trees to be excluded.
     * List must contain names of trees
     *
     * @param extrees
     */
    void setExTrees(List<String> extrees);

    /**
     * returns the list of excluded trees
     *
     * @return extrees
     */
    List<String> getExTrees() ;

    /**
     * sets the list of splits to be excluded.
     * List must contain names of splits
     *
     * @param exSplits
     */
    void setExSplits(List<Integer> exSplits) ;

    /**
     * returns the list of excluded splits
     *
     * @return exsplits
     */
    List<Integer> getExSplits() ;

    /**
     * are we using a heuristic to stabilize the layout of trees?
     * Pairwise stabilize=1, snowball stabilize=2
     *
     * @return stabilize layout?
     */
    int getLayoutStrategy() ;

    /**
     * are we using a heuristic to stabilize the layout of trees?
     * * Pairwise stabilize=1, snowball stabilize=2
     *
     * @param layoutStrategy
     */
    void setLayoutStrategy(int layoutStrategy);

    /**
     * is auto layout of node labels on?
     *
     * @return auto layout node labels?
     */
    boolean getAutoLayoutNodeLabels();

    /**
     * set auto layout of node labels
     *
     * @param autoLayoutNodeLabels
     */
    void setAutoLayoutNodeLabels(boolean autoLayoutNodeLabels) ;


    boolean getRadiallyLayoutNodeLabels() ;

    void setRadiallyLayoutNodeLabels(boolean radiallyLayoutNodeLabels) ;

    /**
     * Returns the current unaligned transformation
     *
     * @return an instance of the set unaligned transformation
     */
    UnalignedTransform getUnalignedTransform() ;

    /**
     * Returns the current characters transformation
     *
     * @return an instance of the set characters transformation
     */
    CharactersTransform getCharactersTransform() ;
    /**
     * Returns the current distances transformation
     *
     * @return an instance of the set distances transformation
     */
    DistancesTransform getDistancesTransform() ;

    /**
     * Returns the current quartets transformation
     *
     * @return an instance of the set quartets transformation
     */
    QuartetsTransform getQuartetsTransform() ;

    /**
     * Returns the current splits transformation
     *
     * @return an instance of the set splits transformation
     */
    SplitsTransform getSplitsTransform() ;

    /**
     * Returns the current trees transformation
     *
     * @return an instance of the set trees transformation
     */
    TreesTransform getTreesTransform() ;
    /**
     * Returns the current reticulate transformation
     *
     * @return an instance of the set reticulate transformation
     */
    ReticulateTransform getReticulateTransform() ;

    /**
     * returns the first nexus blocks that were made dirty in the last call
     * to read
     *
     * @return the list of dirty block names
     */
    String getFirstDirtyBlock() ;

    /**
     * returns the splits post process subclass
     *
     * @return splits post process
     */
    SplitsPostProcess getSplitsPostProcess() ;


}
