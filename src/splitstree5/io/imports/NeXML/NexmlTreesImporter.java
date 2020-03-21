/*
 * NexmlTreesImporter.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.io.imports.NeXML;

import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.imports.NeXML.handlers.NexmlTreesHandler;
import splitstree5.io.imports.interfaces.IImportTrees;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

public class NexmlTreesImporter implements IToTrees, IImportTrees {

    // todo : check partial trees
    // network :nodedata add (id, ..), (label, ...), (taxalabel, ...)
    // output x,y coordinate as metadata, check in importer


    @Override
    public void parse(ProgressListener progressListener, String fileName, TaxaBlock taxa, TreesBlock trees)
            throws CanceledException, IOException {
        try {
            progressListener.setProgress(-1);

            File file = new File(fileName);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            NexmlTreesHandler handler = new NexmlTreesHandler();
            saxParser.parse(file, handler);
            taxa.addTaxaByNames(handler.getTaxaLabels());
            for (PhyloTree t : handler.getTrees()) {
                trees.getTrees().add(t); // todo: problem with multiple trees import?
            }
            trees.setPartial(handler.isPartial());
            trees.setRooted(handler.isRooted());

            progressListener.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getExtensions() {
        return Collections.singletonList("xml");
    }

    @Override
    public boolean isApplicable(String fileName) throws IOException {

        String firstLine = Basic.getFirstLineFromFile(new File(fileName));
        if (firstLine == null || !firstLine.equals("<nex:nexml") && !firstLine.startsWith("<?xml version="))
            return false;

        try (BufferedReader ins =
                     new BufferedReader(new InputStreamReader(Basic.getInputStreamPossiblyZIPorGZIP(fileName)))) {
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
