/*
 *  Copyright (C) 2019 Daniel H. Huson
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
package splitstree5.io.nexus.workflow;

import jloda.util.IOExceptionWithLineNumber;
import jloda.util.Pair;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.*;
import splitstree5.io.nexus.*;

import java.io.IOException;

/**
 * inputs a nexus block
 * Daniel Huson, 3.2018
 */
public class NexusDataBlockInput {
    private String title;
    private Pair<String, String> link;

    /**
     * parse a nexus datablock
     *
     * @param np
     * @param taxa
     * @return datablock
     * @throws IOException
     */
    public DataBlock parse(NexusStreamParser np, TaxaBlock taxa) throws IOException {
        if (np.peekMatchBeginBlock(TaxaBlock.BLOCK_NAME)) {
            final TaxaNexusInput input = new TaxaNexusInput();
            final TaxaBlock dataBlock = new TaxaBlock();
            input.parse(np, dataBlock);
            title = input.getTitle();
            link = input.getLink();
            return dataBlock;
        } else if (np.peekMatchBeginBlock(TraitsBlock.BLOCK_NAME)) {
            final TraitsNexusInput input = new TraitsNexusInput();
            final TraitsBlock dataBlock = new TraitsBlock();
            input.parse(np, taxa, dataBlock);
            title = input.getTitle();
            link = input.getLink();
            return dataBlock;
        } else if (np.peekMatchBeginBlock(AnalysisBlock.BLOCK_NAME)) {
            final AnalysisNexusInput input = new AnalysisNexusInput();
            final AnalysisBlock dataBlock = new AnalysisBlock();
            input.parse(np, dataBlock);
            title = input.getTitle();
            link = input.getLink();
            return dataBlock;
        } else if (np.peekMatchBeginBlock(CharactersBlock.BLOCK_NAME)) {
            final CharactersNexusInput input = new CharactersNexusInput();
            final CharactersBlock dataBlock = new CharactersBlock();
            input.parse(np, taxa, dataBlock);
            title = input.getTitle();
            link = input.getLink();
            return dataBlock;
        } else if (np.peekMatchBeginBlock(DistancesBlock.BLOCK_NAME)) {
            final DistancesNexusInput input = new DistancesNexusInput();
            final DistancesBlock dataBlock = new DistancesBlock();
            input.parse(np, taxa, dataBlock);
            title = input.getTitle();
            link = input.getLink();
            return dataBlock;
        } else if (np.peekMatchBeginBlock(SplitsBlock.BLOCK_NAME)) {
            final SplitsNexusInput input = new SplitsNexusInput();
            final SplitsBlock dataBlock = new SplitsBlock();
            input.parse(np, taxa, dataBlock);
            title = input.getTitle();
            link = input.getLink();
            return dataBlock;
        } else if (np.peekMatchBeginBlock(TreesBlock.BLOCK_NAME)) {
            final TreesNexusInput input = new TreesNexusInput();
            final TreesBlock dataBlock = new TreesBlock();
            input.parse(np, taxa, dataBlock);
            title = input.getTitle();
            link = input.getLink();
            return dataBlock;
        } else if (np.peekMatchBeginBlock(NetworkBlock.BLOCK_NAME)) {
            final NetworkNexusInput input = new NetworkNexusInput();
            final NetworkBlock dataBlock = new NetworkBlock();
            input.parse(np, taxa, dataBlock);
            title = input.getTitle();
            link = input.getLink();
            return dataBlock;
        } else if (np.peekMatchBeginBlock(ViewerBlock.BLOCK_NAME)) {
            final ViewerNexusInput input = new ViewerNexusInput();
            final ViewerBlock dataBlock = input.parse(np, taxa);
            title = input.getTitle();
            link = input.getLink();
            return dataBlock;
        } else
            throw new IOExceptionWithLineNumber("Unknown block type", np.lineno());
    }

    public String getTitle() {
        return title;
    }

    public Pair<String, String> getLink() {
        return link;
    }
}
