package splitstree5.utils.nexus;

import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import jloda.util.NotOwnerException;
import splitstree4.core.SplitsException;
import splitstree4.core.TaxaSet;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.Taxon;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Daria on 23.01.2017.
 */
public class TreesUtilities {

    //todo : replace classes imported from st4

    /**
     * verify that tree, translation and taxa fit together
     *
     * @param tree
     * @param translate
     * @param taxa
     * @param allowAddTaxa if taxa-block is missing taxa in tree, do we allow them to be added to taxa block?
     * @throws SplitsException
     */
    public static void verifyTree(PhyloTree tree, Map translate, TaxaBlock taxa, boolean allowAddTaxa) throws SplitsException, IOException {
        TaxaSet seen = new TaxaSet();
        Iterator it = tree.nodeIterator();
        while (it.hasNext()) {
            try {
                String nodeLabel = tree.getLabel((Node) it.next());
                if (nodeLabel != null) {
                    String taxonLabel = (String) translate.get(nodeLabel);
                    if (taxonLabel == null)
                        throw new SplitsException("Node-label not contained in translate: "
                                + nodeLabel);
                    //if (taxa.indexOf(taxonLabel) == -1) {
                    if(taxa.getLabels().indexOf(taxonLabel) == -1){
                        if (allowAddTaxa){
                            //taxa.add(taxonLabel);
                            Taxon t = new Taxon(taxonLabel);
                            taxa.add(t);
                        } else {
                            //Taxa.show("current taxon block", taxa);
                            throw new SplitsException("Taxon-label not contained in taxa-block: "
                                    + taxonLabel);
                        }
                    }
                    //seen.set(taxa.indexOf(taxonLabel));
                    seen.set(taxa.getLabels().indexOf(taxonLabel));
                }
            } catch (NotOwnerException ex) {
                Basic.caught(ex);
            }
        }
        if (seen.cardinality() != taxa.getNtax())
            throw new SplitsException("Taxa " + taxa + " and seen <" + seen + "> differ");
    }

    /**
     * sets the node2taxa and taxon2node maps for a tree
     *
     * @param tree
     * @param taxa
     */
    public static void setNode2taxa(PhyloTree tree, TaxaBlock taxa) {
        tree.clearTaxon2Node();
        for (Node v = tree.getFirstNode(); v != null; v = v.getNext()) {
            tree.clearNode2Taxa(v);
            String label = tree.getLabel(v);
            if (label != null) {
                //int id = taxa.indexOf(label);
                int id = taxa.getLabels().indexOf(label);
                if (id > 0) {
                    tree.setNode2Taxa(v, id);
                    tree.setTaxon2Node(id, v);
                }
            }
        }
    }


}
