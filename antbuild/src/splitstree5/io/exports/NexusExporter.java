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
import jloda.util.Pair;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.*;
import splitstree5.io.exports.interfaces.*;
import splitstree5.io.imports.nexus.NexusImporterBase;
import splitstree5.io.nexus.*;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * exports in Nexus format
 * Daniel Huson, 2.2018
 */
public class NexusExporter implements IExportAnalysis, IExportTaxa, IExportCharacters, IExportDistances, IExportTrees, IExportSplits, IExportNetwork, IExportTraits, IExportViewer {
    private boolean prependTaxa = true;
    private String title;
    private Pair<String, String> link;

    @Override
    public void export(Writer w, TaxaBlock taxa) throws IOException {
        if (prependTaxa)
            w.write("#nexus\n");
        final TaxaNexusOutput output = new TaxaNexusOutput();
        output.setTitleAndLink(getTitle(), getLink());
        output.write(w, taxa);
    }

    @Override
    public void export(Writer w, AnalysisBlock analysis) throws IOException {
        final AnalysisNexusOutput output = new AnalysisNexusOutput();
        output.setTitleAndLink(getTitle(), getLink());
        output.write(w, analysis);
    }

    @Override
    public void export(Writer w, TaxaBlock taxa, CharactersBlock characters) throws IOException {
        if (prependTaxa)
            new TaxaNexusOutput().write(w, taxa);
        final CharactersNexusOutput output = new CharactersNexusOutput();
        output.setTitleAndLink(getTitle(), getLink());
        output.write(w, taxa, characters);
    }

    @Override
    public void export(Writer w, TaxaBlock taxa, DistancesBlock distances) throws IOException {
        if (prependTaxa)
            new TaxaNexusOutput().write(w, taxa);
        final DistancesNexusOutput output = new DistancesNexusOutput();
        output.setTitleAndLink(getTitle(), getLink());
        output.write(w, taxa, distances);
    }

    @Override
    public void export(Writer w, TaxaBlock taxa, NetworkBlock network) throws IOException {
        if (prependTaxa)
            new TaxaNexusOutput().write(w, taxa);
        final NetworkNexusOutput output = new NetworkNexusOutput();
        output.setTitleAndLink(getTitle(), getLink());
        output.write(w, taxa, network);
    }

    @Override
    public void export(Writer w, TaxaBlock taxa, SplitsBlock splitsBlock) throws IOException {
        if (prependTaxa)
            new TaxaNexusOutput().write(w, taxa);
        final SplitsNexusOutput output = new SplitsNexusOutput();
        output.setTitleAndLink(getTitle(), getLink());
        output.write(w, taxa, splitsBlock);
    }

    @Override
    public void export(Writer w, TaxaBlock taxa, TreesBlock trees) throws IOException {
        if (prependTaxa)
            new TaxaNexusOutput().write(w, taxa);
        final TreesNexusOutput output = new TreesNexusOutput();
        output.setTitleAndLink(getTitle(), getLink());
        output.write(w, taxa, trees);
    }

    @Override
    public void export(Writer w, TaxaBlock taxa, TraitsBlock traitsBlock) throws IOException {
        if (prependTaxa)
            new TaxaNexusOutput().write(w, taxa);
        final TraitsNexusOutput output = new TraitsNexusOutput();
        output.setTitleAndLink(getTitle(), getLink());
        output.write(w, taxa, traitsBlock);
    }

    @Override
    public void export(Writer w, TaxaBlock taxa, ViewerBlock viewerBlock) throws IOException {
        if (prependTaxa)
            new TaxaNexusOutput().write(w, taxa);
        final ViewerNexusOutput output = new ViewerNexusOutput();
        output.setTitleAndLink(getTitle(), getLink());
        output.write(w, taxa, viewerBlock);
    }

    /**
     * export an algorithms block
     *
     * @param w
     * @param algorithm
     * @throws IOException
     */
    public void export(Writer w, Algorithm algorithm) throws IOException {
        final AlgorithmNexusOutput output = new AlgorithmNexusOutput();
        output.setTitleAndLink(getTitle(), getLink());
        output.write(w, algorithm);
    }

    /**
     * export a datablock
     *
     * @param w
     * @param taxaBlock
     * @param dataBlock
     * @throws IOException
     */
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
        else if (dataBlock instanceof TraitsBlock)
            export(w, taxaBlock, (TraitsBlock) dataBlock);
        else if (dataBlock instanceof ViewerBlock)
            export(w, taxaBlock, (ViewerBlock) dataBlock);
        else
            throw new IOException("Export " + Basic.getShortName(dataBlock.getClass()) + ": not implemented");
    }

    @Override
    public List<String> getExtensions() {
        return NexusImporterBase.extensions;
    }


    public boolean isPrependTaxa() {
        return prependTaxa;
    }

    public void setPrependTaxa(boolean prependTaxa) {
        this.prependTaxa = prependTaxa;
    }

    /**
     * get the title of the block to be exported.
     *
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * set the title of the block to be exported.
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * the link to be exported with the block
     *
     * @return list of links
     */
    public Pair<String, String> getLink() {
        return link;
    }

    public void setLink(Pair<String, String> link) {
        this.link = link;
    }
}
