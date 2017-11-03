package nexus.network;

import jloda.graph.EdgeArray;
import jloda.graph.EdgeIntegerArray;
import jloda.phylo.PhyloGraphView;
import jloda.util.parse.NexusStreamParser;
import nexus.splits.Splits;
import nexus.Taxa;
import splitstree4.core.TaxaSet;
import splitstree4.nexus.EdgeDescription;
import splitstree4.nexus.VertexDescription;

import java.awt.*;
import java.io.PrintStream;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * Created by Daria on 19.09.2016.
 */
public interface Network {

    /**
     * Identification string
     */
    String NAME = "Network";
    /**
     * state of the network is circular
     */
    String CIRCULAR = "circular";
    /**
     * state of the network is rectilinear
     */
    String RECTILINEAR = "rectilinear";

    /**************************************
     * FUNCTIONS FROM THE "SPLITS TREE 4" *
     **************************************/

    /**
     * syncronizes the phylograph object to the Network representation of the graph
     */
    void syncPhyloGraphView2Network(Taxa taxa, PhyloGraphView graphView);

    /**
     * syncronizes the network to the PhyloGraphView
     *
     * @param graphView
     */
    void syncNetwork2PhyloGraphView(Taxa taxa, Splits splits, PhyloGraphView graphView) ;

    /**
     * syncs the viewer's respresentation of node labels after a change
     *
     * @param GV the viewer
     */
    void syncNetworkToNodeLabels(PhyloGraphView GV);

    /**
     * syncs the viewer's respresentation of edge labels after a change
     *
     * @param GV the viewer
     */
    void syncNetworkToEdgeLabels(PhyloGraphView GV) ;

    /**
     * set taxa labels to show names or ids or both
     *
     * @param showNames
     * @param showIDs
     * @param taxa
     * @param graphView
     * @param selectedOnly apply only to selected nodes?
     */
    void modifyNodeLabels(boolean showNames, boolean showIDs, Taxa taxa, PhyloGraphView graphView, boolean selectedOnly) ;

    /**
     * modify edge labels
     *
     * @param showWeight
     * @param showEClass
     * @param showConfidence
     * @param splits
     * @param graphView      the PhyloGraphView
     * @param selectedOnly   apply only to selected edges?
     */
    void modifyEdgeLabels(boolean showWeight, boolean showEClass, boolean showConfidence,
                                 boolean showInterval, Splits splits, PhyloGraphView graphView, boolean selectedOnly) ;

    /**
     * get widths and colors for edge confidence rendering
     *
     * @param edgeWidth
     * @param edgeShading
     * @param splits
     * @param graphView
     * @param selectedOnly
     * @param widths       will return the new edge widths here
     * @param colors       will return the new edge colors here
     */
    void getEdgeConfidenceHightlighting(boolean edgeWidth, boolean edgeShading,
                                               Splits splits, PhyloGraphView graphView, boolean selectedOnly,
                                               EdgeIntegerArray widths, EdgeArray<Color> colors) ;

    /**
     * apply edge widths and colors
     *
     * @param widths to apply to edges
     * @param colors to apply to edges
     */
    void applyWidthsColors(PhyloGraphView graphView, EdgeIntegerArray widths, EdgeArray colors) ;

    /**
     * hide some taxa
     *
     * @param origTaxa
     * @param exTaxa
     */
    void hideTaxa(Taxa origTaxa, TaxaSet exTaxa) ;


    /**
     * creates the taxon 2 vertex description map for the document to keep
     */
    void updateTaxon2VertexDescriptionMap(Map<String, VertexDescription> taxon2VertexDescription) ;

    /**
     * apply a created taxon2 vertex description map
     *
     * @param taxon2VertexDescription
     */
    void applyTaxon2VertexDescription(Map taxon2VertexDescription) ;

    /*********
     *  I/O  *
     *********/
    /**
     * Writes a network object in nexus format
     *
     * @param w    a writer
     * @param taxa the taxa
     */
    void write(Writer w, Taxa taxa) ;

    /**
     * Reads a splits object in NexusBlock format
     *
     * @param np   nexus stream parser
     * @param taxa the taxa
     */
    void read(NexusStreamParser np, Taxa taxa) ;

    /**
     * Reads a network object in OLD 4beta-1-3 NexusBlock format
     *
     * @param np nexus stream parser
     */
    void readOld(NexusStreamParser np) ;

    /**
     * Produces a string representation of a NexusBlock object
     *
     * @param taxa the taxa block
     * @return object in nexus format
     */
    String toString(Taxa taxa);

    /**
     * Show the usage of this block
     *
     * @param ps the print stream
     */
    void showUsage(PrintStream ps) ;

    String toString() ;


    /*********************
     * GETTER AND SETTER *
     *********************/
    Draw getDraw() ;
    void setDraw(Draw draw) ;

    int getNtax() ;
    void setNtax(int ntax) ;

    int getNvertices() ;
    void setNvertices(int n);

    VertexDescription[] getVertices() ;

    int getNedges() ;
    void setNedges(int n) ;

    EdgeDescription[] getEdges() ;

    /**
     * gets the node to taxalabels  map
     */
    Map getTranslate() ;
    /**
     * gets the list of taxon labels that a node translates into
     */
    List<String> getTranslate(int nodeId) ;

    String getLayout() ;

    void setLayout(String layout) ;

    /**
     * sets the node to taxalabels map
     *
     * @param taxa        the taxon block
     * @param nodeId      the integer id of the node
     * @param taxonLabels the labels of the taxa as strings or ids
     */
    void setTranslate(Taxa taxa, int nodeId, List<Integer> taxonLabels) ;

    /**
     * if network is phylogenetic tree, return the tree
     *
     * @return tree
     */
    String getNewick();
}
