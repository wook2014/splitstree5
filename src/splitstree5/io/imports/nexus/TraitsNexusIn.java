/*
 *  Copyright (C) 2018 Daniel H. Huson
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
package splitstree5.io.imports.nexus;

import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TraitsBlock;
import splitstree5.io.imports.interfaces.IImportNoAutoDetect;
import splitstree5.io.imports.interfaces.IImportTraits;
import splitstree5.io.nexus.TraitsNexusInput;

import java.io.IOException;
import java.util.List;

/**
 * nexus traits importer
 * daniel huson, 2.2018
 */
public class TraitsNexusIn extends NexusImporter<TraitsBlock> implements IImportTraits, IImportNoAutoDetect {

    @Override
    public boolean isApplicable(String fileName) throws IOException {
        return isApplicable(fileName, TraitsNexusInput.NAME);
    }

    @Override
    public List<String> parseBlock(NexusStreamParser np, TaxaBlock taxaBlock, TraitsBlock dataBlock) throws IOException {
        try {
            while (!np.peekMatchIgnoreCase("begin traits;")) {
                np.getWordRespectCase();
            }
            return new TraitsNexusInput().parse(np, taxaBlock, dataBlock);
        } catch (Exception ex) {
            return null;
        }
    }
}