/*
 * NexmlTreesImporter.java Copyright (C) 2022 Daniel H. Huson
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

package splitstree5.io.imports.nexml;

import jloda.fx.window.NotificationManager;
import jloda.graph.Node;
import jloda.graph.algorithms.IsTree;
import jloda.phylo.PhyloTree;
import jloda.util.FileUtils;
import jloda.util.NumberUtils;
import jloda.util.progress.ProgressListener;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.imports.interfaces.IImportTrees;
import splitstree5.io.imports.nexml.handlers.NexmlTreesHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * nexml tree importer
 * Daria Evseeva, 2019, Daniel Huson, 2020
 */
public class NexmlTreesImporter implements IToTrees, IImportTrees {
    private long lastWarning = 0;

    // todo : check partial trees
    // network :nodedata add (id, ..), (label, ...), (taxalabel, ...)
    // output x,y coordinate as metadata, check in importer


    @Override
    public void parse(ProgressListener progressListener, String fileName, TaxaBlock taxa, TreesBlock trees) throws IOException {
        try {
            progressListener.setProgress(-1);
            final File file = new File(fileName);
            final SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            final NexmlTreesHandler handler = new NexmlTreesHandler();
            saxParser.parse(file, handler);
            taxa.addTaxaByNames(handler.getTaxaLabels());

            boolean hasRootWithOutdegree2 = false;

            for (PhyloTree t : handler.getTrees()) {
                if (IsTree.apply(t)) {
                    if (t.getRoot() == null) {
                        t.setRoot(t.getFirstNode());
                        for (var v : t.nodes()) {
                            if (v.getInDegree() == 0) {
                                t.setRoot(v);
                                break;
                            }
                        }
                        t.redirectEdgesAwayFromRoot();
                        if (!hasRootWithOutdegree2 && t.getRoot().getOutDegree() == 2)
                            hasRootWithOutdegree2 = true;
                    }
                    trees.getTrees().add(t); // todo: problem with multiple trees import?
                } else if (System.currentTimeMillis() > lastWarning + 5000) {
                    NotificationManager.showWarning("Skipping rooted network...");
                    lastWarning = System.currentTimeMillis();
                }
            }
            if (trees.size() == 0)
                throw new IOException("No trees found");
            trees.setPartial(handler.isPartial());
            trees.setRooted(hasRootWithOutdegree2 || handler.isRooted());

            if (taxa.size() == 0) { // try and setup all the taxa
                var labels = new HashSet<String>();
                for (var tree : trees.getTrees()) {
                    labels.addAll(tree.nodeStream()
                            .filter(v -> v.getLabel() != null && (v.getOutDegree() == 0 || !NumberUtils.isDouble(v.getLabel())))
                            .map(Node::getLabel).collect(Collectors.toList()));
                }
                taxa.addTaxaByNames(labels);
                for (var tree : trees.getTrees()) {
                    tree.nodeStream().filter(v -> v.getLabel() != null && (v.getOutDegree() == 0 || !NumberUtils.isDouble(v.getLabel()))).forEach(v -> {
                        tree.clearTaxa(v);
                        tree.addTaxon(v, taxa.indexOf(v.getLabel()));
                    });
                }
            }
            progressListener.reportTaskCompleted();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public List<String> getExtensions() {
        return Collections.singletonList("xml");
    }

    @Override
    public boolean isApplicable(String fileName) throws IOException {

		String firstLine = FileUtils.getFirstLineFromFile(new File(fileName));
        if (firstLine == null || !firstLine.equals("<nex:nexml") && !firstLine.startsWith("<?xml version="))
            return false;

		try (BufferedReader ins = new BufferedReader(new InputStreamReader(FileUtils.getInputStreamPossiblyZIPorGZIP(fileName)))) {
			String aLine;
			while ((aLine = ins.readLine()) != null) {
				if (aLine.contains("<tree"))
					return true;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

        return false;
    }
}
