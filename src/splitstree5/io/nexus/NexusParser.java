/*
 *  NexusParser.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.io.nexus;

import jloda.util.Basic;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.*;

import java.io.IOException;

/**
 * parses a single datablock
 * Daniel Huson, 2.2018
 */
public class NexusParser {
    /**
     * parse a single datablock
     *
     * @param np
     * @param taxaBlock
     * @param dataBlock
     * @throws IOException
     */
    public static void parse(NexusStreamParser np, TaxaBlock taxaBlock, DataBlock dataBlock) throws IOException {
        if (dataBlock instanceof CharactersBlock)
            new CharactersNexusInput().parse(np, taxaBlock, (CharactersBlock) dataBlock);
        else if (dataBlock instanceof DistancesBlock)
            new DistancesNexusInput().parse(np, taxaBlock, (DistancesBlock) dataBlock);
        else if (dataBlock instanceof SplitsBlock)
            new SplitsNexusInput().parse(np, taxaBlock, (SplitsBlock) dataBlock);
        else if (dataBlock instanceof TreesBlock)
            new TreesNexusInput().parse(np, taxaBlock, (TreesBlock) dataBlock);
        else if (dataBlock instanceof NetworkBlock)
            new NetworkNexusInput().parse(np, taxaBlock, (NetworkBlock) dataBlock);
        else if (dataBlock instanceof TraitsBlock)
            new TraitsNexusInput().parse(np, taxaBlock, (TraitsBlock) dataBlock);
        else
            throw new IOException("Can't parse block of type: " + Basic.getShortName(dataBlock.getClass()));
    }
}
