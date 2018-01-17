package nexus.bootstrap;

import jloda.util.parse.NexusStreamParser;
import nexus.Taxa;
import nexus.characters.Characters;
import nexus.splits.Splits;
import splitstree4.algorithms.util.PaupNode;
import splitstree4.models.SubstitutionModel;
import splitstree4.util.SplitMatrix;

import javax.swing.text.Document;
import java.io.PrintStream;
import java.io.Writer;

/**
 * Created by Daria on 19.09.2016.
 */
public interface Bootstrap {

    /**
     * Identification string
     */
    String NAME = "st_Bootstrap";


    /**
     * Computes the boostrap analysis given the document
     *
     * @param doc
     */
    void compute(Document doc);

    /**
     * Performs a parametric bootstrap, generating replicate alignments on the tree T with model M.
     *
     * @param doc
     * @param T
     * @param M
     */
    void computeParametric(Document doc, PaupNode T, SubstitutionModel M);


    /**
     * IO Handling
     */

    /**
     * Show the usage of bootstrap.
     *
     * @param ps the print stream
     */
    void showUsage(PrintStream ps);

    /**
     * Read the st_bootstrap
     *
     * @param np   NexusStreamParser
     * @param taxa Taxa
     */
    void read(NexusStreamParser np, Taxa taxa, Characters characters,
              Splits splits);


    /**
     * write a matrix to the given writer
     *
     * @param w    The writer to which the matrix should be written to.
     * @param taxa
     * @throws java.io.IOException
     */
    void write(Writer w, Taxa taxa);


    /**
     * Getter
     */

    /**
     * Return the format object
     *
     * @return the format object
     */
    Format getFormat();

    /**
     * Gets the number of cycles
     *
     * @return the number of runs
     */
    int getRuns();

    /**
     * Gets the random seed
     *
     * @return the random seed
     */
    int getSeed();

    /**
     * Gets the split matrix.
     *
     * @return the matrix of all split weights over all replicates
     */
    SplitMatrix getSplitMatrix();

    /**
     * Gets the number of characters
     *
     * @return the number of characters
     */
    int getNchar();

    /**
     * Gets the ntax
     *
     * @return ntax the number of taxa
     */
    int getNtax();

    /**
     * Gets the nsplits
     *
     * @return nsplits the number of splits
     */
    int getNsplits();

    /**
     * Gets the number of resampled characters
     *
     * @return length
     */

    int getLength();

    /**
     * Gets the bootstrap splits
     *
     * @return bsplits
     */
    Splits getBsplits();


    /**
     * Get the flag indicating whether the user should have the option to save trees
     *
     * @return boolean flag indicating whether the user should have the option to save trees
     */
    boolean getCanSaveTrees();

    /**
     * Get the flag indicating whether the user should have the option to save trees
     *
     * @param canSavetrees flag indicating whether the user should have the option to save trees
     */
    void setCanSaveTrees(boolean canSavetrees);


    /**
     * Get the flag indicating whether a new document should be opened with the bootstrap trees
     *
     * @return boolean flag indicating whether a new document should be opened with the bootstrap trees.
     */
    boolean getSaveTrees();

    /**
     * Get the flag indicating whether a new document should be opened with the bootstrap trees
     *
     * @param savetrees flag indicating whether a new document should be opened with the bootstrap trees.
     */
    void setSaveTrees(boolean savetrees);


    /**
     * Sets the number of cycles
     *
     * @param n the number of runs
     */
    void setRuns(int n);

    /**
     * Sets the random seed
     *
     * @param n the random seed
     */
    void setSeed(int n);

    /**
     * Sets the split matrix
     *
     * @param splitMatrix
     */
    void setSplitMatrix(SplitMatrix splitMatrix);

    /**
     * Sets the number of characters
     *
     * @param n the number of characters
     */
    void setNchar(int n);

    /**
     * Sets the ntax
     *
     * @param n
     */
    void setNtax(int n);

    /**
     * Sets the nsplits
     *
     * @param n
     */
    void setNsplits(int n);

    /*Sets the length
     *@param len
     */
    void setLength(int len);

    /**
     * Sets the bootstrap splits
     *
     * @param bsplits the splits computed by bootstrapping
     */
    void setBsplits(Splits bsplits);

    /**
     * gets the value of a format switch
     *
     * @param name
     * @return value of format switch
     */
    boolean getFormatSwitchValue(String name);
}
