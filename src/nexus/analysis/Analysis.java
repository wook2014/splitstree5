package nexus.analysis;

import jloda.util.parse.NexusStreamParser;
import nexus.bootstrap.Bootstrap;
import splitstree4.core.Document;
import splitstree4.nexus.*;

import java.io.PrintStream;
import java.io.Writer;

public interface Analysis {

    // ID String
    String NAME = "st_Analysis";

    String apply(Document doc);

    String apply(Document doc, Taxa taxa, String blockName);

    /***************
     * INPUT OUTPUT *
     ***************/

    void read(NexusStreamParser np);

    void write(Writer w);

    void write(Writer w, Taxa taxa);

    static void showUsage(PrintStream ps) {
        ps.println("BEGIN ST_ANALYSIS;");
        ps.println("\t[clear;]");
        ps.println("\t[" + Unaligned.NAME + " [{ON|OFF|ONCE}] name [parameters];]");
        ps.println("\t[" + Characters.NAME + " [{ON|OFF|ONCE}] name [parameters];]");
        ps.println("\t[" + Distances.NAME + " [{ON|OFF|ONCE}] name [parameters];]");
        ps.println("\t[" + Quartets.NAME + " [{ON|OFF|ONCE}] name [parameters];]");
        ps.println("\t[" + Trees.NAME + " [{ON|OFF|ONCE}] name [parameters];]");
        ps.println("\t[" + Splits.NAME + " [{ON|OFF|ONCE}] name [parameters];]");
        ps.println("\t[" + Network.NAME + " [{ON|OFF|ONCE}] name [parameters];]");
        ps.println("\t[" + Bootstrap.NAME + " [{ON|OFF|ONCE}] name [parameters];]");

        ps.println("\t[ALL [{ON|OFF|ONCE}] name [parameters];]");
        ps.println("END;");
    }

    /********************
    * GETTER AND SETTER *
     ********************/

    int getNanalyzers();

}