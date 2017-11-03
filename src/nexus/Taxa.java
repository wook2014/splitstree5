package nexus;

import jloda.util.parse.NexusStreamParser;
import splitstree4.core.TaxaSet;

import java.io.PrintStream;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by Daria on 15.09.2016.
 */

public interface Taxa {

    /**
     * Identification string
     */
    String NAME = "Taxa";
    /**
     * special taxon label for which indexOf always returns 1
     */
    String FIRSTTAXON = "First-Taxon";

    /**************************************
     * FUNCTIONS FROM THE "SPLITS TREE 4" *
     **************************************/

    boolean equals(Taxa taxa); //returns true, if lists of taxa are equal
    /**
     * additionally hide more taxa. This is used in the presence of partial trees.
     * Note that these numbers are given with respect to the current taxa set,
     * not the original one!
     */
    void hideAdditionalTaxa(TaxaSet additionalHidden);
    void hideTaxa(TaxaSet hiddenTaxa); //hides the given set of taxa
    void show(String label, Taxa taxa); //print to System.err

    //add a taxon to the taxa block
    void add(String taxonLabel);
    void add(String taxonLabel, String info);

    boolean contains(Collection labels) ;//returns true, if taxa block contains given set of labels
    void checkLabelsAreUnique();

    /*********************
     * GENERAL FUNCTIONS *
     *********************/

    void clear(); //clears the block
    Object clone(); //returns a clone of the block

    // INPUT OUTPUT

    void write(Writer w); //writes the taxa object in nexus format
    void write(Writer w, Taxa taxa); //overrides method in NexusBlock
    void read(NexusStreamParser np); //Reads a taxa object in NexusBlock format

    String toString(); // produces a full string representation of this nexus block

    //Show the usage of taxa block
    static void showUsage(PrintStream ps) {
        ps.println("BEGIN TAXA;");
        ps.println("DIMENSIONS NTAX=number-of-taxa;");
        ps.println("[TAXLABELS taxon_1 taxon_2 ... taxon_ntax;]");
        ps.println("[TAXINFO info_1 info_2 ... info_ntax;]");
        ps.println("END;");
    }

    /*********************
     * GETTER AND SETTER *
     *********************/

    int getNtax();
    void setNtax(int ntax);

    Set<String> getLabels(TaxaSet taxaSet);
    String getLabel(int i);
    void setLabel(int i, String label);
    List getAllLabels();

    int getIndexOf(String label);

    String getInfo(int taxonNum);
    void setInfo(int taxonNum, String info);

    boolean getMustDetectLabels(); //returns true, if we need to obtain taxon labels from other block
    void setMustDetectLabels(boolean mustDetectLabels);

    TaxaSet getTaxaSet(); //Returns the set of all taxa
    Taxa getInduced(Taxa origTaxa, TaxaSet hiddenTaxa); //gets the induced taxon set
    Taxa getOriginalTaxa(); //returns the original taxa set (that existed before hidding of taxa)
    void setOriginalTaxa(Taxa taxa); //set the original taxa
    TaxaSet getHiddenTaxa(); //gets the hidden taxa
}
