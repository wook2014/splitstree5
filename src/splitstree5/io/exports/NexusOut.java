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

package splitstree5.io.exports;

import jloda.util.Basic;
import splitstree5.core.datablocks.*;
import splitstree5.io.exports.interfaces.*;
import splitstree5.io.imports.nexus.NexusImporter;
import splitstree5.io.nexus.*;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * exports in Nexus format
 * Daniel Huson, 2.2018
 */
public class NexusOut implements IExportAnalysis, IExportTaxa, IExportCharacters, IExportDistances, IExportTrees, IExportSplits, IExportNetwork {
    @Override
    public void export(Writer w, TaxaBlock taxa) throws IOException {
        w.write("#nexus\n");
        new TaxaNexusOutput().write(w, taxa);
    }

    @Override
    public void export(Writer w, AnalysisBlock analysis) throws IOException {
        new AnalysisNexusOutput().write(w, analysis);
    }

    @Override
    public void export(Writer w, TaxaBlock taxa, CharactersBlock characters) throws IOException {
        export(w, taxa);
        new CharactersNexusOutput().write(w, taxa, characters, null);
    }

    @Override
    public void export(Writer w, TaxaBlock taxa, DistancesBlock distances) throws IOException {
        export(w, taxa);
        new DistancesNexusOutput().write(w, taxa, distances, null);
    }

    @Override
    public void export(Writer w, TaxaBlock taxa, NetworkBlock network) throws IOException {
        export(w, taxa);
        new NetworkNexusOutput().write(w, taxa, network, null);
    }

    @Override
    public void export(Writer w, TaxaBlock taxa, SplitsBlock splitsBlock) throws IOException {
        export(w, taxa);
        new SplitsNexusOutput().write(w, taxa, splitsBlock, null);
    }

    @Override
    public void export(Writer w, TaxaBlock taxa, TreesBlock trees) throws IOException {
        export(w, taxa);
        new TreesNexusOutput().write(w, taxa, trees, null);
    }

    @Override
    public List<String> getExtensions() {
        return NexusImporter.extensions;
    }

    public void export(Writer w, TaxaBlock taxaBlock, DataBlock dataBlock) throws IOException {
        if (dataBlock instanceof CharactersBlock)
            export(w, taxaBlock, (CharactersBlock) dataBlock);
        else if (dataBlock instanceof DistancesBlock)
            export(w, taxaBlock, (DistancesBlock) dataBlock);
        else if (dataBlock instanceof SplitsBlock)
            export(w, taxaBlock, (SplitsBlock) dataBlock);
        else if (dataBlock instanceof TreesBlock)
            export(w, taxaBlock, (TreesBlock) dataBlock);
        else if (dataBlock instanceof NetworkBlock)
            export(w, taxaBlock, (NetworkBlock) dataBlock);
        else
            throw new IOException("Export " + Basic.getShortName(dataBlock.getClass()) + ": not implemented");
    }

}
