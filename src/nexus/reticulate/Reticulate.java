package nexus.reticulate;

import jloda.graph.Node;
import jloda.phylo.PhyloGraph;
import jloda.util.parse.NexusStreamParser;
import splitstree4.core.SplitsException;
import splitstree4.nexus.Taxa;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;

/**
 * Created by Daria on 11.10.2016.
 */
public interface Reticulate {

    /**
     * Identification string
     */
    String NAME = "Reticulate";

    /**
     * clone the object
     *
     * @return
     */
    Object clone();

    /************************ ROOT COMPONENT *****************************/

    /**
     * add a root component to the list of root components
     *
     * @param label   the label of the new root component
     * @param eNewick the eNewick representation of the root component
     * @param active  is this the active root component
     * @return
     */
    boolean addRootComponent(String label, String eNewick, boolean active);

    /**
     * deletes a root component from the list
     *
     * @param which which root component
     * @return
     */
    boolean deleteRootComponent(int which);

    /**
     * deletes a root component from the list
     *
     * @param label which root component
     * @return
     */
    boolean deleteRootComponent(String label);


    /**************** TREE COMPONENT*************************/

    /**
     * add a TreeComponent to the list of TreeComponents
     *
     * @param label   the label of the TreeComponent (must be unique)
     * @param eNewick the eNewick string of the TreeComponent
     * @return
     */
    boolean addTreeComponent(String label, String eNewick);

    /**
     * remove a TreeComponent from the list
     *
     * @param which which TreeComponent
     * @return
     */
    boolean deleteTreeComponent(int which);

    /**
     * remove a TreeComponent from the list
     *
     * @param label which TreeComponent
     * @return
     */
    boolean deleteTreeComponent(String label);


    /********************** the netted components **********************/
    /**
     * add a netted component
     *
     * @param componentLabel the label of the netted component
     * @return
     */
    boolean addNettedComponent(String componentLabel);

    /**
     * delete a netted component from the list
     *
     * @param which which netted component
     * @return
     */
    boolean deleteNettedComponent(int which);

    /**
     * deleta a netted component from the list
     *
     * @param label which netted component
     * @return
     */
    boolean deleteNettedComponent(String label);

    /*************** backbone in a netted component *******************************/

    /**
     * add a backbone to a netted component
     *
     * @param nettedComponent which netted component
     * @param backboneLabel   the label of the backbone
     * @param eNewick         the eNewick representation
     * @return
     */
    boolean addNettedComponentBackbone(int nettedComponent, String backboneLabel, String eNewick, boolean active);

    /**
     * deletes a backbone from a netted component
     *
     * @param nettedComponent which netted component
     * @param which           which backbone
     * @return
     */
    boolean deleteNettedComponentBackbone(int nettedComponent, int which);

    /**
     * delets a backbone from a netted component
     *
     * @param nettedComponent which netted component
     * @param label           which backbone
     * @return
     */
    boolean deleteNettedComponentBackbone(int nettedComponent, String label);

    /***************
     * INPUT OUTPUT *
     ****************/

    /**
     * print the usage to the stream 'out'
     *
     * @param ps the output stream
     */
    static void showUsage(PrintStream ps) {
        ps.println("BEGIN RETICULATE;");
        ps.println("DIMENSIONS [NTAX=number-of-taxa] nRootComponents=number-of-root-components nNettedComponents=number-of-netted-components nTreeComponents=number-of-tree-components;");
        ps.println("FORMAT");
        ps.println("\t[ACTIVEROOT=id-of-active-root-component;]");
        ps.println("\t[ACTIVENETTEDCOMPONENTS=list-of-active-netted-components-ids;]");
        ps.println("\t[SHOWLABELS= {INTERNAL|TREECOMPONENTS|NETTEDCOMPONENTS};]");
        ps.println("[TREECOMPONENTS");
        ps.println("\t[name1 = eNewick-string of tree-component 1;]");
        ps.println("\t[name2 = eNewick-string of tree-component 2;]");
        ps.println("\t[...                                    ]");
        ps.println("\t[nameM = eNewick-string of tree-component M;]");
        ps.println("]");
        ps.println("[NETTEDCOMPONENTS");
        ps.println("\t[netted-components-name1 =");
        ps.println("\t\t[netted-components-1-backbone-name1 = eNewick-string of netted components 1 backbone 1;]");
        ps.println("\t\t[netted-components-1-backbone-name2 = eNewick-string of netted components 1 backbone 2;]");
        ps.println("\t\t[...                                                                                   ]");
        ps.println("\t\t[netted-components-1-backbone-nameM = eNewick-string of netted components 1 backbone M;]");
        ps.println("\t;]");
        ps.println("\t[netted-components-name2 =");
        ps.println("\t\t[netted-components-2-backbone-name1 = eNewick-string of netted components 2 backbone 1;]");
        ps.println("\t\t[netted-components-2-backbone-name2 = eNewick-string of netted components 2 backbone 2;]");
        ps.println("\t\t[...                                                                                   ]");
        ps.println("\t\t[netted-components-2-backbone-nameM = eNewick-string of netted components 2 backbone M;]");
        ps.println("\t;]");
        ps.println("\t[...]");
        ps.println("\t[netted-components-nameL =");
        ps.println("\t\t[netted-components-L-backbone-name1 = eNewick-string of netted components L backbone 1;]");
        ps.println("\t\t[netted-components-L-backbone-name2 = eNewick-string of netted components L backbone 2;]");
        ps.println("\t\t[...                                                                                   ]");
        ps.println("\t\t[netted-components-L-backbone-nameM = eNewick-string of netted components L backbone M;]");
        ps.println("\t;]");
        ps.println("]");
        ps.println("ROOTCOMPONENTS");
        ps.println("\t[backbone-name1 = eNewick-string of backbone1;]");
        ps.println("\t[backbone-name2 = eNewick-string of backbone2;]");
        ps.println("\t[...                                          ]");
        ps.println("\t[backbone-nameM = eNewick-string of backboneM;]");
        ps.println("END; [Reticulate]");
    }

    /**
     * write the reticulate block to the given writer
     *
     * @param w    the writer to which the reticulate block should be written
     * @param taxa the nexus taxa object associated with this reticulate object
     * @throws java.io.IOException
     */
    void write(Writer w, Taxa taxa) throws IOException;

    /**
     * write the reticulate block to the given writer
     *
     * @param w    the writer to which the reticulate block should be written
     * @param nTax the number taxa object associated with this reticulate object
     * @throws IOException
     */
    void write(Writer w, int nTax) throws IOException;

    /**
     * read a reticulate nexus block from the given nexus stream parser
     *
     * @param np   the nexus stream parser from which the block should be read
     * @param taxa the nexus taxa object associated with this reticulate object
     * @throws SplitsException
     * @throws IOException
     */
    void read(NexusStreamParser np, Taxa taxa) throws SplitsException, IOException;

    /**
     * read a reticulate nexus block from the given nexus stream parser
     *
     * @param np   the nexus stream parser from which the block should be read
     * @param nTax the number taxa object associated with this reticulate object
     * @throws SplitsException
     * @throws IOException
     */
    void read(NexusStreamParser np, int nTax) throws SplitsException, IOException;

    /****************
     * GETTER SETTER *
     ****************/
    /**
     * return the format subclass of this nexus class
     *
     * @return
     */
    Format getFormat();

    /**
     * return the reticulate object with full taxa set
     *
     * @return
     */
    Reticulate getOriginal();

    /**
     * set the original reticulate object
     *
     * @param originalReticulate
     */
    void setOriginal(Reticulate originalReticulate);

    /**
     * @param name
     * @return
     */
    boolean getFormatSwitchValue(String name);

    /**
     * get the number of taxa
     *
     * @return
     */
    int getNtax();
    /**
     * get the number of root components
     *
     * @return
     */

    /**
     * get the root node of the reticulate network
     *
     * @return
     * @throws Exception
     */
    Node getRoot() throws Exception;

    /**
     * return the phlyograph that represents the reticulate network given its configuration
     *
     * @return
     * @throws java.io.IOException
     */
    PhyloGraph getReticulateNetwork() throws IOException;

    /***************** ROOT COMPONENT ***********************/

    int getNRootComponents();
    /**
     * get the number of TreeComponents
     *
     * @return
     */

    /**
     * return the active root component
     *
     * @return
     */
    int getActiveRootComponent();

    /**
     * set the active root component
     *
     * @param active which root component
     * @return
     */
    boolean setActiveRootComponent(int active);
    /**
     * get the active backbone of the netted component
     *
     * @param nettedComponent which netted component
     * @return
     */

    /**
     * returns the label of a root component
     *
     * @param which which root component
     * @return
     */
    String getRootComponentLabel(int which);

    /**
     * set the label of a root component
     *
     * @param which which root component
     * @param label the label
     * @return
     */
    boolean setRootComponentLabel(int which, String label);

    /**
     * return the index of the root component
     *
     * @param label which root component
     * @return
     */
    int indexOfRootComponentLabel(String label);

    /**
     * return the eNewick representation of the root component
     *
     * @param which which root component
     * @return
     */
    String getRootComponent(int which);

    /**
     * sets the eNewick representation of the root component
     *
     * @param which   which root component
     * @param eNewick the eNewick string
     * @return
     */
    boolean setRootComponent(int which, String eNewick);

    /*********************** TREE COMPONENT ************************/

    int getNTreeComponents();

    /**
     * get the number of netted components
     *
     * @return
     */
    int getNNettedComponents();

    // everything for the treeComponents

    /**
     * get the label of the TreeComponent
     *
     * @param which which TreeComponent
     * @return
     */
    String getTreeComponentLabel(int which);

    /**
     * set the label of the TreeComponent
     *
     * @param which which TreeComponent
     * @param label the label
     * @return
     */
    boolean setTreeComponentLabel(int which, String label);

    /**
     * return the index of the TreeComponent label
     *
     * @param label
     * @return
     */
    int indexOfTreeComponentLabel(String label);

    /**
     * return a eNewick representation of the TreeComponent
     *
     * @param which which TreeComponent
     * @return
     */
    String getTreeComponent(int which);

    /**
     * set the eNewick string of the TreeComponent
     *
     * @param which   which TreeComponent
     * @param eNewick the eNewick string
     * @return
     */
    boolean setTreeComponent(int which, String eNewick);

    /******************* NETTED COMPONENT ***********************/

    /**
     * get the label of the netted component
     *
     * @param which which netted component
     * @return
     */
    String getNettedComponentLabel(int which);

    /**
     * sets the label of the netted component
     *
     * @param which which netted component
     * @param label the label
     * @return
     */
    boolean setNettedComponentLabel(int which, String label);

    /**
     * get the index of the netted component given the label
     *
     * @param label which netted component
     * @return
     */
    int indexOfNettedComponentLabel(String label);

    /**
     * return a array of integers containing the netted components contained in the selected root component (array values start at 1!)
     *
     * @param which which root component
     * @return
     * @throws java.io.IOException
     */
    int[] getContainedNettedComponentsOfRootComponent(int which) throws IOException;

    /**
     * return a array of integers containing the netted components contained in the active root component  array values start at 1!)
     *
     * @return
     */
    int[] getContainedNettedComponentsOfActiveRootComponent();

    /************************ NETTED COMPONENT BACKBONE **************************/

    int getActiveNettedComponentBackbone(int nettedComponent);

    /**
     * set the active backbone of the netted component
     *
     * @param nettedComponent which netted component
     * @return
     */
    boolean setActiveNettedComponentBackbone(int nettedComponent, int active);

    /**
     * get the number of backbones within the netted component
     *
     * @param nettedComponent which netted component
     * @return
     */
    int getNumberOfNettedComponentBackbones(int nettedComponent);

    /**
     * get the label of a backbone that is contained in a netted component
     *
     * @param nettedComponent which netted component
     * @param which           which backbone
     * @return
     */
    String getNettedComponentBackboneLabel(int nettedComponent, int which);

    /**
     * set the label of a backbone that is contained in a netted component
     *
     * @param nettedComponent which netted component
     * @param which           which backbone
     * @param label           the label for the backbone
     * @return
     */
    boolean setNettedComponentBackboneLabel(int nettedComponent, int which, String label);

    /**
     * get the index of a backbone in the list of backbones within the netted component
     *
     * @param nettedComponent which netted component
     * @param label           the label for the backbone
     * @return
     */
    int indexOfNettedComponentBackboneLabel(int nettedComponent, String label);

    /**
     * get the eNewick representation of a backbone contained in a netted component
     *
     * @param nettedComponent which netted component
     * @param which           which backbone
     * @return
     */
    String getNettedComponentBackbone(int nettedComponent, int which);

    /**
     * set the eNewick representation of a backbone contained in a netted component
     *
     * @param nettedComponent which netted component
     * @param which           which backbone
     * @param eNewick         the eNewick string
     * @return
     */
    boolean setNettedComponentBackbone(int nettedComponent, int which, String eNewick);

}
