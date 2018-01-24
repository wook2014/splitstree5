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

package splitstree5.dialogs.exports;

import jloda.util.Basic;
import jloda.util.PluginClassLoader;
import splitstree5.core.datablocks.*;
import splitstree5.io.exports.interfaces.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ExportManager {
    private final ArrayList<IExporter> exporters;

    private static ExportManager instance;

    private ExportManager() {
        exporters = new ArrayList<>(PluginClassLoader.getInstances(IExporter.class, "splitstree5.io.exports"));

    }

    public static ExportManager getInstance() {
        if (instance == null)
            instance = new ExportManager();

        return instance;
    }

    /**
     * gets the list of names of all exporters suitable for this data
     *
     * @param dataBlock
     * @return
     */
    public ArrayList<String> getExporterNames(ADataBlock dataBlock) {
        final ArrayList<String> list = new ArrayList<>();

        for (IExporter exporter : exporters) {
            if (dataBlock.getFromInterface().isAssignableFrom(exporter.getClass())) {
                list.add(Basic.getShortName(exporter.getClass()));
            }
        }
        return list;
    }

    /**
     * add a file suffix, if missing
     *
     * @param selectedFile
     * @param exporterName
     * @return file with suffix added, if necessary
     */
    public File ensureFileSuffix(File selectedFile, String exporterName) {
        String suffix = Basic.getSuffix(selectedFile.getName());
        if (suffix.length() == 0) {
            IExporter exporter = getExporterByName(exporterName);
            if (exporter != null && exporter.getExtensions().size() > 0) {
                return Basic.replaceFileSuffix(selectedFile, "." + exporter.getExtensions().get(0));
            }
        }
        return selectedFile;
    }

    public IExporter getExporterByName(String exporterName) {
        for (IExporter exporter : exporters) {
            if (exporterName.equals(Basic.getShortName(exporter.getClass())))
                return exporter;
        }
        return null;
    }

    /**
     * export a datablock in the named foramt
     *
     * @param fileName
     * @param taxaBlock
     * @param dataBlock
     * @param exporterName
     * @throws IOException
     */
    public void exportFile(String fileName, TaxaBlock taxaBlock, ADataBlock dataBlock, String exporterName) throws IOException {
        IExporter exporter = getExporterByName(exporterName);
        if (exporter != null) {
            try (BufferedWriter w = new BufferedWriter(new FileWriter(fileName))) {
                if (exporter instanceof IExportCharacters && dataBlock instanceof CharactersBlock)
                    ((IExportCharacters) exporter).export(w, taxaBlock, (CharactersBlock) dataBlock);
                else if (exporter instanceof IExportDistances && dataBlock instanceof DistancesBlock)
                    ((IExportDistances) exporter).export(w, taxaBlock, (DistancesBlock) dataBlock);
                else if (exporter instanceof IExportTrees && dataBlock instanceof TreesBlock)
                    ((IExportTrees) exporter).export(w, taxaBlock, (TreesBlock) dataBlock);
                else if (exporter instanceof IExportSplits && dataBlock instanceof SplitsBlock)
                    ((IExportSplits) exporter).export(w, taxaBlock, (SplitsBlock) dataBlock);
                else if (exporter instanceof IExportTaxa)
                    ((IExportTaxa) exporter).export(w, taxaBlock);
                else
                    throw new IOException("Export failed: invalid combination of exporter and data");
            }
        }
    }
}
