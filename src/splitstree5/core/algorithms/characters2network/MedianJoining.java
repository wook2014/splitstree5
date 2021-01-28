/*
 * MedianJoining.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.core.algorithms.characters2network;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromCharacters;
import splitstree5.core.algorithms.interfaces.IToNetwork;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.NetworkBlock;
import splitstree5.core.datablocks.TaxaBlock;

/**
 * run the median joining algorithm
 * Daniel Huson, 2.2018
 */
public class MedianJoining extends Algorithm<CharactersBlock, NetworkBlock> implements IFromCharacters, IToNetwork {
    /**
     * Determine whether given method can be applied to given data.
     *
     * @param taxa  the taxa
     * @param chars the characters matrix
     * @return true, if method applies to given data
     */
    public boolean isApplicable(TaxaBlock taxa, CharactersBlock chars) {
        return taxa != null && chars != null && chars.getNcolors() < 8; // not too  many different states
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock parent, NetworkBlock child) throws Exception {
        MedianJoiningCalculator medianJoiningCalculator = new MedianJoiningCalculator();
        medianJoiningCalculator.apply(progress, taxaBlock, parent, child);
    }

    @Override
    public String getCitation() {
        return "Bandelt et al, 1999;H. -J. Bandelt, P. Forster, and A. Röhl. Median-joining networks for inferring intraspecific phylogenies. Molecular Biology and Evolution, 16:37–48, 1999.";
    }
}