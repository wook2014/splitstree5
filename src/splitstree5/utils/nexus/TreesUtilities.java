package splitstree5.utils.nexus;

import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import jloda.util.NotOwnerException;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.Taxon;

import java.io.IOException;
import java.util.BitSet;
import java.util.Iterator;

/**
 * some computations on trees
 *
 * @author huson Date: 29-Feb-2004
 *         Created by Daria on 23.01.2017.
 */
public class TreesUtilities {

    // no translate, ioexeption
    /**
     * verify that tree, translation and taxa fit together
     *
     * @param tree
     * @param taxa
     * @param allowAddTaxa if taxa-block is missing taxa in tree, do we allow them to be added to taxa block?
     * @throws IOException
     */
    public static void verifyTree(PhyloTree tree, TaxaBlock taxa, boolean allowAddTaxa) throws IOException {
        final BitSet seen = new BitSet();
        Iterator it = tree.nodeIterator();
        while (it.hasNext()) {
            try {
                String taxonLabel = tree.getLabel((Node) it.next());

                //if (taxa.indexOf(taxonLabel) == -1) {
                if (taxa.getLabels().indexOf(taxonLabel) == -1) {
                    if (allowAddTaxa) {
                        //taxa.add(taxonLabel);
                        Taxon t = new Taxon(taxonLabel);
                        taxa.add(t);
                    } else {
                        //Taxa.show("current taxon block", taxa);
                        throw new IOException("Taxon-label not contained in taxa-block: " + taxonLabel);
                    }
                }
                //seen.set(taxa.indexOf(taxonLabel));
                seen.set(taxa.getLabels().indexOf(taxonLabel));
            } catch (NotOwnerException ex) {
                Basic.caught(ex);
            }
        }
        if (seen.cardinality() != taxa.getNtax())
            throw new IOException("Taxa " + taxa + " and seen <" + seen + "> differ");
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
                int id = taxa.indexOf(label);
                if (id > 0) {
                    tree.setNode2Taxa(v, id);
                    tree.setTaxon2Node(id, v);
                }
            }
        }
    }

    /**
     * gets all taxa in tree, if node to taxa mapping has been set
     *
     * @param tree
     * @return all taxa in tree
     */
    public static BitSet getTaxa(PhyloTree tree) {
        BitSet taxa = new BitSet();
        for (Node v = tree.getFirstNode(); v != null; v = tree.getNextNode(v)) {
            if (tree.getNode2Taxa(v) != null) {
                for (Integer t : tree.getNode2Taxa(v)) {
                    taxa.set(t);
                }
            }
        }
        return taxa;
    }
}
