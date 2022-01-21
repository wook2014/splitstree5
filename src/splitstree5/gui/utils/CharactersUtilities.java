/*
 * CharactersUtilities.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.gui.utils;

import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.Pair;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.Taxon;

import java.io.IOException;

public class CharactersUtilities {

    /**
     * Group identical haplotype function
     *
     * @param taxa     taxa to group
     * @param topBlock characters or distances block
     * @return pair of new taxa and new topblock, if collapsed and null, else
     */

    //todo test on: mash.dist, lugens-1.nex
    static public Pair<TaxaBlock, DataBlock> collapseByType(TaxaBlock taxa, DataBlock topBlock) throws IOException, CanceledException {
        final int ntax = taxa.getNtax();

        int typecount = 0;
        int numNonSingleClasses = 0;

        int[] taxaTypes = new int[taxa.getNtax() + 1];
        int[] representatives = new int[taxa.getNtax() + 1]; //Representative taxon of each type.

        TaxaBlock newTaxa = new TaxaBlock();

        //Use a breadth-first search to identify classes of identical sequences or distance matrix rows.
        //Build up new taxa block. Classes of size one give new taxa with the same name, larger classes
        //are named TYPEn for n=1,2,3...
        for (int i = 1; i <= ntax; i++) {
            if (taxaTypes[i] != 0)  //Already been 'typed'
                continue;
            typecount++;
            taxaTypes[i] = typecount;
            representatives[typecount] = i;
            int numberOfThisType = 1;
            String info = taxa.getLabel(i); //Start building up the info string for this taxon.


            for (int j = i + 1; j <= ntax; j++) {
                if (taxaTypes[j] != 0)
                    continue;
                boolean taxaIdentical;
                if (topBlock instanceof CharactersBlock)
                    taxaIdentical = taxaIdentical((CharactersBlock) topBlock, i, j);
                else
                    taxaIdentical = taxaIdentical((DistancesBlock) topBlock, i, j);
                if (taxaIdentical) {
                    taxaTypes[j] = typecount;
                    numberOfThisType++;
                    info += ", " + taxa.getLabel(j);
                }
            }

            if (numberOfThisType > 1) {
                numNonSingleClasses++;
                Taxon t = new Taxon("TYPE" + numNonSingleClasses);
                t.setInfo(info);
                newTaxa.add(t);
            } else {
                Taxon t = new Taxon(info);
                t.setInfo(info);
                newTaxa.add(t); //Info is the same as taxa label.
            }
        }

        if (newTaxa.getNtax() == taxa.getNtax())
            return null;

        //Set up the new characters block, if one exists.
        if (topBlock instanceof CharactersBlock) {
            final CharactersBlock characters = (CharactersBlock) topBlock;
            final CharactersBlock newCharacters = new CharactersBlock();
            newCharacters.copy(taxa, characters); // need to copy formating

            newCharacters.setDimension(newTaxa.getNtax(), characters.getNchar());
            for (int i = 1; i <= newTaxa.getNtax(); i++) {
                int old_i = representatives[i];
                for (int k = 1; k <= characters.getNchar(); k++)
                    newCharacters.set(i, k, characters.get(old_i, k));
            }
            return new Pair<>(newTaxa, newCharacters);

        } else if (topBlock instanceof DistancesBlock) {
            //Set up the new distances block
            final DistancesBlock distances = (DistancesBlock) topBlock;
            final DistancesBlock newDistances = new DistancesBlock();
            newDistances.copy(distances);
            newDistances.setNtax(newTaxa.getNtax());

            for (int i = 1; i <= newTaxa.getNtax(); i++) {
                int old_i = representatives[i];
                for (int j = 1; j < i; j++) {
                    int old_j = representatives[j];
                    double val = distances.get(old_i, old_j);
                    newDistances.set(i, j, val);
                    newDistances.set(j, i, val);
                }
            }

            return new Pair<>(newTaxa, newDistances);
        } else {
            throw new IOException("Group haplotypes not implemented for: " + Basic.getShortName(topBlock.getClass()));
        }

    }

    /**
     * Check to see if two sequences are identical on all the unmasked sites.
     *
     * @param characters
     * @param i
     * @param j
     * @return true is sequences are identical. False otherwise.
     */
    static public boolean taxaIdentical(CharactersBlock characters, int i, int j) {
        char[] seqi = characters.getRow1(i);
        char[] seqj = characters.getRow1(j);
        char missingchar = characters.getMissingCharacter();
        char gapchar = characters.getGapCharacter();

        for (int k = 1; k <= characters.getNchar(); k++) {
            char ci = seqi[k];
            char cj = seqj[k];

            //Convert to lower case if the respectCase option is not set
            if (!characters.isRespectCase()) {
                if (ci != missingchar && ci != gapchar)
                    ci = Character.toLowerCase(ci);
                if (cj != missingchar && cj != gapchar)
                    cj = Character.toLowerCase(cj);
            }
            if (ci != cj)
                return false;
        }
        return true;
    }

    /**
     * Check to see if two sequences are identical using the distance data
     *
     * @param distances
     * @param i
     * @param j
     * @return true if two rows in the distance matrix are identical and the taxa have distance 0
     */
    static private boolean taxaIdentical(DistancesBlock distances, int i, int j) {

        int ntax = distances.getNtax();

        if (distances.get(i, j) > 0)
            return false;
        for (int k = 1; k <= ntax; k++) {
            if (k == i || k == j)
                continue;
            if (distances.get(i, k) != distances.get(j, k))
                return false;
        }
        return true;
    }
}
