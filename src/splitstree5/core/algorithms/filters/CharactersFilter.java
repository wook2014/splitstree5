/*
 *  Copyright (C) 2016 Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
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
 */

package splitstree5.core.algorithms.filters;

import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToChararacters;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.gui.connectorview.AlgorithmPane;

import java.util.*;

/**
 * removes columns from a character alignment
 * Daniel Huson, 1.2018
 */
public class CharactersFilter extends Algorithm<CharactersBlock, CharactersBlock> implements IFromChararacters, IToChararacters {
    private final BitSet columnMask = new BitSet(); // positions set here are ignored

    private boolean optionExcludeGapSites = false;
    private boolean optionExcludeParsimonyUninformativeSites = false;
    private boolean optionExcludeConstantSites = false;

    private boolean optionExcludeFirstCodonPosition = false;
    private boolean optionExcludeSecondCodonPosition = false;
    private boolean optionExcludeThirdCodonPosition = false;

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxa, CharactersBlock parent, CharactersBlock child) throws InterruptedException, CanceledException {
        child.clear();

        int totalColumns = parent.getNchar();

        final Map<Integer, Integer> ch2count = new HashMap<>();

        columnMask.clear();

        for (int col = 1; col <= parent.getNchar(); col++) {
            if (isOptionExcludeFirstCodonPosition() && (col % 3) == 1)
                columnMask.set(col);
            else if (isOptionExcludeSecondCodonPosition() && (col % 3) == 2)
                columnMask.set(col);
            else if (isOptionExcludeThirdCodonPosition() && (col % 3) == 0)
                columnMask.set(col);
            else if (isOptionExcludeConstantSites() || isOptionExcludeParsimonyUninformativeSites()) {
                ch2count.clear();
                for (int tax = 1; tax <= taxa.getNtax(); tax++) {
                    int ch = parent.get(tax, col);
                    if (ch == parent.getGapCharacter()) {
                        if (isOptionExcludeGapSites()) {
                            columnMask.set(col);
                            break;
                        }
                    }
                    if (ch2count.containsKey(ch))
                        ch2count.put(ch, ch2count.get(ch) + 1);
                    else
                        ch2count.put(ch, 1);
                }
                if (isOptionExcludeConstantSites() && ch2count.size() == 1) {
                    columnMask.set(col);
                } else if (isOptionExcludeParsimonyUninformativeSites() && ch2count.size() == 2 && ch2count.values().contains(1)) {
                    columnMask.set(col);
                }
            }
        }
        System.err.println("Mask size: " + columnMask.cardinality());

        child.setDimension(taxa.getNtax(), parent.getNchar() - columnMask.cardinality());
        // set this after setting the dimension:
        child.setStateLabeler(parent.getStateLabeler());
        child.setDataType(parent.getDataType());
        child.setDiploid(parent.isDiploid());
        child.setGammaParam(parent.getGammaParam());
        child.setGapCharacter(parent.getGapCharacter());
        child.setHasAmbiguousStates(parent.isHasAmbiguousStates());
        child.setMissingCharacter(parent.getMissingCharacter());
        child.setSymbols(parent.getSymbols());
        child.setRespectCase(parent.isRespectCase());
        child.setUseCharacterWeights(parent.isUseCharacterWeights());

        if (parent.getCharLabeler() != null)
            child.setCharLabeler(new HashMap<>());

        int pos = 1;
        for (int c = 1; c <= parent.getNchar(); c++) {
            if (!columnMask.get(c)) {
                for (int tax = 1; tax <= taxa.getNtax(); tax++) {
                    child.set(tax, pos, parent.get(tax, c));
                }
                if (parent.isUseCharacterWeights())
                    child.setCharacterWeight(pos, parent.getCharacterWeight(c));
                if (parent.getCharLabeler() != null) {
                    final String label = parent.getCharLabeler().get(c);
                    if (label != null)
                        child.getCharLabeler().put(pos, label);
                }
                pos++;
            }
        }

        if (totalColumns == 0)
            setShortDescription(null);
        else if (columnMask.cardinality() > 0)
            setShortDescription("Enabled: " + (totalColumns - columnMask.cardinality()) + " (of " + totalColumns + ") chars");
        else
            setShortDescription("Enabled: all " + totalColumns + " chars");
    }

    @Override
    public void clear() {
        super.clear();
    }

    public AlgorithmPane getControl() {
        return super.getControl();
    }

    public boolean isOptionExcludeGapSites() {
        return optionExcludeGapSites;
    }

    public void setOptionExcludeGapSites(boolean optionExcludeGapSites) {
        this.optionExcludeGapSites = optionExcludeGapSites;
    }

    public boolean isOptionExcludeParsimonyUninformativeSites() {
        return optionExcludeParsimonyUninformativeSites;
    }

    public void setOptionExcludeParsimonyUninformativeSites(boolean optionExcludeParsimonyUninformativeSites) {
        this.optionExcludeParsimonyUninformativeSites = optionExcludeParsimonyUninformativeSites;
    }

    public boolean isOptionExcludeConstantSites() {
        return optionExcludeConstantSites;
    }

    public void setOptionExcludeConstantSites(boolean optionExcludeConstantSites) {
        this.optionExcludeConstantSites = optionExcludeConstantSites;
    }

    public boolean isOptionExcludeFirstCodonPosition() {
        return optionExcludeFirstCodonPosition;
    }

    public void setOptionExcludeFirstCodonPosition(boolean optionExcludeFirstCodonPosition) {
        this.optionExcludeFirstCodonPosition = optionExcludeFirstCodonPosition;
    }

    public boolean isOptionExcludeSecondCodonPosition() {
        return optionExcludeSecondCodonPosition;
    }

    public void setOptionExcludeSecondCodonPosition(boolean optionExcludeSecondCodonPosition) {
        this.optionExcludeSecondCodonPosition = optionExcludeSecondCodonPosition;
    }

    public boolean isOptionExcludeThirdCodonPosition() {
        return optionExcludeThirdCodonPosition;
    }

    public void setOptionExcludeThirdCodonPosition(boolean optionExcludeThirdCodonPosition) {
        this.optionExcludeThirdCodonPosition = optionExcludeThirdCodonPosition;
    }

    public List<String> listOptions() {
        return Arrays.asList("optionExcludeGapSites", "optionExcludeConstantSites", "optionExcludeParsimonyUninformativeSites", "optionExcludeFirstCodonPosition", "optionExcludeSecondCodonPosition", "optionExcludeThirdCodonPosition");
    }
}