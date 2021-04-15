/*
 * TraitsBlock.java Copyright (C) 2021. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package splitstree5.core.datablocks;

import splitstree5.core.algorithms.interfaces.IFromTraits;
import splitstree5.core.algorithms.interfaces.IToTraits;
import splitstree5.core.misc.Taxon;
import splitstree5.io.nexus.TraitsNexusFormat;

import java.util.Collection;

/**
 * traits block
 * daniel Huson, 2.2018
 */
public class TraitsBlock extends DataBlock implements IAdditionalBlock {
    public static final String BLOCK_NAME = "TRAITS";

    private int[][] matrix = {};  // computation is done on values
    private String[][] matrixOfLabels = null; // values have labels
    private String[] labels = {};
    private float[] traitLatitude = null;
    private float[] traitLongitude = null;

    /**
     * default constructor
     */
    public TraitsBlock() {
        format = new TraitsNexusFormat();
    }

    public void setDimensions(int ntax, int ntraits) {
        matrix = new int[ntax][ntraits];
        labels = new String[ntraits];
    }

    public void clear() {
        setDimensions(0, 0);
        matrixOfLabels = null;
        traitLongitude = null;
        traitLatitude = null;
    }

    public void setTraitValue(int taxonId, int traitId, int value) {
        matrix[taxonId - 1][traitId - 1] = value;
    }

    public void setTraitValueLabel(int taxonId, int traitId, String label) {
        if (matrixOfLabels == null)
            matrixOfLabels = new String[matrix.length][getNTraits()]; // lazy setup

        matrixOfLabels[taxonId - 1][traitId - 1] = label;
    }

    public int getTraitValue(int taxonId, int traitId) {
        return matrix[taxonId - 1][traitId - 1];
    }

    public String getTraitValueLabel(int taxonId, int traitId) {
        if (matrixOfLabels == null || matrixOfLabels[taxonId - 1][traitId - 1] == null)
            return "" + getTraitValue(taxonId, traitId);
        else
            return matrixOfLabels[taxonId - 1][traitId - 1];
    }

    public String getTraitLabel(int traitId) {
        return labels[traitId - 1];
    }

    public void setTraitLabel(int traitId, String label) {
        labels[traitId - 1] = label;
    }

    public float getTraitLongitude(int traitId) {
        return traitLongitude == null ? 0 : traitLongitude[traitId - 1];
    }

    public void setTraitLongitude(int traitId, float value) {
        if (!isSetLatitudeLongitude()) {
            traitLatitude = new float[getNTraits()];
            traitLongitude = new float[getNTraits()];
        }
        traitLongitude[traitId - 1] = value;
    }

    public float getTraitLatitude(int traitId) {
        return traitLatitude == null ? 0 : traitLatitude[traitId - 1];
    }

    public void setTraitLatitude(int traitId, float value) {
        if (!isSetLatitudeLongitude()) {
            traitLatitude = new float[getNTraits()];
            traitLongitude = new float[getNTraits()];
        }
        traitLatitude[traitId - 1] = value;
    }

    @Override
    public int size() {
        return getNTraits();
    }

    public int getNTraits() {
        return matrix.length == 0 ? 0 : matrix[0].length;
    }

    @Override
    public Class<IFromTraits> getFromInterface() {
        return IFromTraits.class;
    }

    @Override
    public Class<IToTraits> getToInterface() {
        return IToTraits.class;
    }

    @Override
    public String getInfo() {
        return "";
    }

    public boolean isSetLatitudeLongitude() {
        return traitLatitude != null;
    }

    public void clearLatitudeLongitude() {
        traitLatitude = null;
        traitLongitude = null;
    }

    /**
     * copy subset of taxa
     *
     * @param srcTaxa
     * @param srcTraits
     * @param enabledTaxa
     */
    public void copySubset(TaxaBlock srcTaxa, TraitsBlock srcTraits, Collection<Taxon> enabledTaxa) {
        labels = srcTraits.labels;
        traitLongitude = srcTraits.traitLongitude;
        traitLatitude = srcTraits.traitLatitude;
        matrix = new int[enabledTaxa.size()][srcTraits.getNTraits()];
        matrixOfLabels = null; // will be set in setTraitValueLabel if required
        int tarTaxonIdx = 1;
        for (Taxon taxon : enabledTaxa) {
            final int srcTaxonIdx = srcTaxa.indexOf(taxon);
            for (int traitIdx = 1; traitIdx <= srcTraits.getNTraits(); traitIdx++) {
                setTraitValue(tarTaxonIdx, traitIdx, srcTraits.getTraitValue(srcTaxonIdx, traitIdx));
                if (srcTraits.matrixOfLabels != null)
                    setTraitValueLabel(tarTaxonIdx, traitIdx, srcTraits.getTraitValueLabel(srcTaxonIdx, traitIdx));

            }
            tarTaxonIdx++;
        }
    }

    @Override
    public String getBlockName() {
        return BLOCK_NAME;
    }
}
