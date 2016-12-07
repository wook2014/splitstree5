package nexus.analysis;

import jloda.util.parse.NexusStreamParser;
import splitstree4.algorithms.util.Configurator;
import splitstree4.analysis.bootstrap.BootstrapAnalysisMethod;
import splitstree4.analysis.characters.CharactersAnalysisMethod;
import splitstree4.analysis.distances.DistancesAnalysisMethod;
import splitstree4.analysis.network.NetworkAnalysisMethod;
import splitstree4.analysis.quartets.QuartetsAnalysisMethod;
import splitstree4.analysis.splits.SplitsAnalysisMethod;
import splitstree4.analysis.trees.TreesAnalysisMethod;
import splitstree4.analysis.unaligned.UnalignedAnalysisMethod;
import splitstree4.core.Document;
import splitstree4.nexus.*;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;

/*****************************
 * COPIED FROM SPLITS TREE 4 *
 *****************************/

public class Analyzer {
    private String kind = null;  // the kind of data to apply the analyzer to
    private String name = null;  // the name of the analyzer class
    private String params = null; // the parameter string
    private String state = "once"; // off, on or once

    /**
     * Constructor
     */
    Analyzer() {
    }

    /**
     * Constructor an analyzer from a string
     *
     * @param str the string
     */
    Analyzer(String str) {
        NexusStreamParser np = new NexusStreamParser(new StringReader(str));
        try {
            read(np);
        } catch (IOException ex) {
            jloda.util.Basic.caught(ex);
        }
    }

    /**
     * Get the kind
     *
     * @return kind
     */
    String getKind() {
        return kind;
    }

    /**
     * Get the name
     *
     * @return name
     */
    String getName() {
        return name;
    }

    /**
     * Get the parameters
     *
     * @return the parameters
     */
    String getParameters() {
        return params;
    }

    /**
     * Gets the state on, off or once
     *
     * @return the state
     */
    String getState() {
        return state;
    }

    /**
     * Sets the state, must be off, on or once
     *
     * @param state the new state
     */
    void setState(String state) {
        this.state = state;
    }

    /**
     * reads an analyzer
     *
     * @param np a nexus parser
     */
    void read(NexusStreamParser np) throws IOException {
        np.peekMatchAnyTokenIgnoreCase
                (Unaligned.NAME + " " + Characters.NAME + " " + Distances.NAME +
                        " " + Quartets.NAME + " " + Splits.NAME + " " + Trees.NAME + " " + Network.NAME + " " + Bootstrap.NAME);
        kind = np.getWordRespectCase();
        if (np.peekMatchAnyTokenIgnoreCase("off on once"))
            state = np.getWordRespectCase();
        name = np.getWordRespectCase();
        if (!np.peekMatchIgnoreCase(";"))
            params = np.getTokensStringRespectCase(";");
        else
            np.matchIgnoreCase(";");
    }

    /**
     * Writes the analyzer
     *
     * @param w writer
     */
    void write(Writer w) throws IOException {
        w.write(kind + " " + state + " " + name);
        if (params != null)
            w.write(" " + params);
        w.write(";\n");
    }

    /**
     * gets string representation
     *
     * @return string representation
     */
    public String toString() {
        return kind + " " + state + " " + name;
    }

    /**
     * Applies the analyzer
     *
     * @param doc  the document block
     * @param taxa the taxa
     */
    String apply(Document doc, Taxa taxa) throws Exception {
        String prefix = "splitstree4.analysis." + kind.toLowerCase() + ".";
        Class theClass;
        if (!getName().contains("."))
            theClass = Class.forName(prefix + getName());
        else
            theClass = Class.forName(getName());

        splitstree4.analysis.AnalysisMethod plugin = (splitstree4.analysis.AnalysisMethod) theClass.newInstance();
        Configurator.setOptions(plugin, getParameters());

        String result = "";
        if (getKind().equalsIgnoreCase(Unaligned.NAME))
            result = ((UnalignedAnalysisMethod) plugin).apply(doc, taxa, doc.getUnaligned());
        else if (getKind().equalsIgnoreCase(Characters.NAME))
            result = ((CharactersAnalysisMethod) plugin).apply(doc);
        else if (getKind().equalsIgnoreCase(Distances.NAME))
            result = ((DistancesAnalysisMethod) plugin).apply(doc, taxa, doc.getDistances());
        else if (getKind().equalsIgnoreCase(Quartets.NAME))
            result = ((QuartetsAnalysisMethod) plugin).apply(doc, taxa, doc.getQuartets());

        else if (getKind().equalsIgnoreCase(Trees.NAME))
            result = ((TreesAnalysisMethod) plugin).apply(doc, taxa, doc.getTrees());

        else if (getKind().equalsIgnoreCase(Splits.NAME))
            result = ((SplitsAnalysisMethod) plugin).apply(doc, taxa, doc.getSplits());

        else if (getKind().equalsIgnoreCase(Network.NAME))
            result = ((NetworkAnalysisMethod) plugin).apply(doc, taxa, doc.getNetwork());

        else if (getKind().equalsIgnoreCase(Bootstrap.NAME))
            result = ((BootstrapAnalysisMethod) plugin).apply(doc);

        else if (getKind().equalsIgnoreCase(Characters.NAME))
            result = ((CharactersAnalysisMethod) plugin).apply(doc);
        if (result != null) {
            System.err.println(result);
        }
        return result;
    }
}
