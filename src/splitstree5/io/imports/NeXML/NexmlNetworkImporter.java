/*
 * NexmlNetworkImporter.java Copyright (C) 2022 Daniel H. Huson
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

package splitstree5.io.imports.NeXML;

import jloda.util.CanceledException;
import jloda.util.FileUtils;
import jloda.util.progress.ProgressListener;
import splitstree5.core.datablocks.NetworkBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.imports.NeXML.handlers.NexmlNetworkHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

/**
 * nexml network importer
 * Daria Evseeva, 2019
 * todo: debug and activate
 */
public class NexmlNetworkImporter { //  implements IToNetwork, IImportNetwork {

    public void parse(ProgressListener progressListener, String fileName, TaxaBlock taxaBlock, NetworkBlock dataBlock)
            throws CanceledException, IOException {

        try {
            progressListener.setProgress(-1);

            File file = new File(fileName);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            NexmlNetworkHandler handler = new NexmlNetworkHandler();
            dataBlock.clear();
            handler.setNetworkBlock(dataBlock);
            saxParser.parse(file, handler);
            taxaBlock.addTaxaByNames(handler.getTaxaLabels());

            progressListener.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public List<String> getExtensions() {
        return Collections.singletonList("xml");
    }

    public boolean isApplicable(String fileName) throws IOException {
		String firstLine = FileUtils.getFirstLineFromFile(new File(fileName));
        if (firstLine == null || !firstLine.equals("<nex:nexml") && !firstLine.startsWith("<?xml version="))
            return false;

		try (BufferedReader ins =
					 new BufferedReader(new InputStreamReader(FileUtils.getInputStreamPossiblyZIPorGZIP(fileName)))) {
			String aLine;
			while ((aLine = ins.readLine()) != null) {
				if (aLine.contains("<network"))
					return true;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

        return false;
    }
}
