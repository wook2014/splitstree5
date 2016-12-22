package nexus.traits;

import jloda.util.parse.NexusStreamParser;
import splitstree4.core.TaxaSet;
import splitstree4.nexus.Taxa;

import java.io.PrintStream;
import java.io.Writer;

/**
 * Created by Daria on 11.10.2016.
 */
public interface Traits {

    String MISSING_TRAIT = "?";

    /**
     * Identification string
     */
    String NAME = "Traits";

    /**
     * clones a traits object
     *
     * @param taxa Taxa block that this characters block is associated to
     * @return a clone
     */
    Traits clone(Taxa taxa) ;
    /**
     * return the induced object obtained by hiding taxa
     *
     * @param origTaxa   original (full?) taxa block
     */
    void hideTaxa(Taxa origTaxa, TaxaSet hiddenTaxa) ;

    /***************
     * INPUT OUTPUT *
     ***************/

    void read(NexusStreamParser np, Taxa taxa);
    /**
     * write a block, blocks should override this
     *
     * @param w    Writer
     * @param taxa Taxa block for this document
     * @throws java.io.IOException
     */
    void write(Writer w, Taxa taxa) ;

    static void showUsage(PrintStream ps) {
        String Usage = "";
        Usage += "BEGIN TRAITS;\n";
        Usage += "\tDIMENSIONS NTRAITS=number-of-traits;\n";
        Usage += "\t[FORMAT\n";
        Usage += "\t\t[LABELS = {YES|NO}]\n";
        Usage += "\t\t[MISSING = symbol]\n";
        Usage += "\t\t[SEPARATOR = {COMMA|TAB|SPACES}]\n";
        Usage += "\t;]\n";
        Usage += "\tTRAITLABELS trait1 trait2 ... trait_ntraits;\n";
        Usage += "\tMATRIX\n";
        Usage += "\t\ttrait data as an array with rows corresponding to taxa\n";
        Usage += "\t\tand columns corresponding to traits. Adjacent cells\n";
        Usage += "\t\tdelimted by the SEPARATOR, and only row per line.\n";
        Usage += "\t;\n";
        Usage += "END;";
        ps.println(Usage);
    }

    /*** GETTER AND SETTER ***/

    Format getFormat() ;
    int getNtax() ;
    int getNtraits() ;
    String get(int taxon, String traitName) ;
    String get(int taxon, int traitNumber) ;
    String[] getTraitValues(String traitName) ;
    int getTraitNumber(String traitName) ;
    String getTraitName(int traitNumber) ;
    void setTraitName(int i, String lab) ;
    String[] getTraitNames() ;

}
