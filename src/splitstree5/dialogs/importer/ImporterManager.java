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

/*
 *  Copyright (C) 2016 Daniel H. Huson
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

package splitstree5.dialogs.importer;

import javafx.stage.FileChooser;
import jloda.util.Basic;
import jloda.util.PluginClassLoader;
import splitstree5.io.imports.interfaces.*;

import java.io.IOException;
import java.util.*;

/**
 * manages all import classes
 * Daniel Huson, 1.2018
 */
public class ImporterManager {
    public static final String UNKNOWN = "Unknown";

    private final ArrayList<IImporter> importers;
    private final ArrayList<FileChooser.ExtensionFilter> extensionFilters;

    private static ImporterManager instance;

    private ImporterManager() {
        importers = new ArrayList<>(PluginClassLoader.getInstances(IImporter.class, "splitstree5.io.imports"));
        extensionFilters = new ArrayList<>();
        final Map<String, Set<String>> dataType2Extensions = new TreeMap<>();
        for (IImporter importer : importers) {
            String dataType = getDataType(importer);
            Collection<String> add = importer.getExtensions();
            if (add != null) {
                if (dataType2Extensions.containsKey(dataType))
                    dataType2Extensions.get(dataType).addAll(add);
                else
                    dataType2Extensions.put(dataType, new TreeSet<>(add));
            }
        }
        for (String dataType : dataType2Extensions.keySet()) {
            extensionFilters.add(new FileChooser.ExtensionFilter(dataType + ": "
                    + Basic.toString(completeExtensions(dataType2Extensions.get(dataType), false), " "),
                    completeExtensions(dataType2Extensions.get(dataType), true)));
        }
        extensionFilters.sort(Comparator.comparing(FileChooser.ExtensionFilter::getDescription));
        extensionFilters.add(0, new FileChooser.ExtensionFilter("All files: *.* *.gz", "*.*", "*.gz"));
    }

    /**
     * complete extensions by adding prefix *. and also adding an extension with suffix .gz, if requested
     *
     * @param extensions
     * @param includeGZ
     * @return completed extensions
     */
    public static List<String> completeExtensions(Collection<String> extensions, boolean includeGZ) {
        if (extensions == null)
            return null;
        final Set<String> set = new TreeSet<>();
        for (String ext : extensions) {
            if (!ext.startsWith("*."))
                ext = "*." + ext;
            set.add(ext);
        }

        final ArrayList<String> list = new ArrayList<>(set);
        if (includeGZ) {
            for (String ex : set) {
                if (!ex.endsWith(".gz") && !set.contains(ex + ".gz"))
                    list.add(ex + ".gz");
            }
        }
        return list;
    }

    public static ImporterManager getInstance() {
        if (instance == null)
            instance = new ImporterManager();
        return instance;
    }

    /**
     * get all defined importers
     *
     * @return importers
     */
    public ArrayList<IImporter> getImporters() {
        return importers;
    }

    /**
     * get all extension filters
     *
     * @return extension filters
     */
    public Collection<FileChooser.ExtensionFilter> getAllExtensionFilters() {
        return extensionFilters;
    }

    public Collection<? extends String> getAllDataTypes() {
        final Set<String> set = new TreeSet<>();
        for (IImporter importer : importers) {
            set.add(getDataType(importer));
        }
        final ArrayList<String> result = new ArrayList<>();
        result.add(UNKNOWN);
        result.addAll(set);
        return result;
    }

    public Collection<? extends String> getAllFileFormats() {
        final Set<String> set = new TreeSet<>();
        for (IImporter importer : importers) {
            set.add(getFileFormat(importer));
        }
        final ArrayList<String> result = new ArrayList<>();
        result.add(UNKNOWN);
        result.addAll(set);
        return result;
    }

    private static String getDataType(IImporter importer) {
        if (importer instanceof IImportCharacters) {
            return "Characters";
        } else if (importer instanceof IImportDistances) {
            return "Distances";
        } else if (importer instanceof IImportTrees) {
            return "Trees";
        } else if (importer instanceof IImportSplits) {
            return "Splits";
        } else if (importer instanceof IImportNetwork) {
            return "Network";
        } else
            return UNKNOWN;
    }

    private static String getFileFormat(IImporter importer) {
        String name = Basic.getShortName(importer.getClass());
        if (name.endsWith("In"))
            return name.substring(0, name.length() - 2);
        else if (name.endsWith("Importer"))
            return name.substring(0, name.length() - 8);
        else
            return name;
    }


    /**
     * get the data type for the named file
     *
     * @param fileName
     * @return data type
     */
    public String getDataType(String fileName) {
        String dataType = null;

        for (IImporter importer : importers) {
            try {
                if (!(importer instanceof IImportNoAutoDetect) && importer.isApplicable(fileName)) {
                    String type = getDataType(importer);
                    if (!type.equals(UNKNOWN)) {
                        if (dataType == null)
                            dataType = type;
                        else if (!dataType.equals(type))
                            return UNKNOWN;
                    }
                }
            } catch (IOException ex) {
            }
        }
        if (dataType == null)
            return UNKNOWN;
        else
            return dataType;
    }

    public String getFileFormat(String fileName) {
        String fileFormat = null;

        for (IImporter importer : importers) {
            try {
                if (!(importer instanceof IImportNoAutoDetect) && importer.isApplicable(fileName)) {
                    String format = getFileFormat(importer);
                    if (fileFormat == null)
                        fileFormat = format;
                    else if (!fileFormat.equals(format))
                        return UNKNOWN;
                }
            } catch (IOException ex) {
            }
        }
        if (fileFormat == null)
            return UNKNOWN;
        else
            return fileFormat;
    }

    /**
     * gets the importer by type and file format
     *
     * @param dataType
     * @param fileFormat
     * @return importer or null
     */
    public IImporter getImporterByDataTypeAndFileFormat(String dataType, String fileFormat) {
        for (IImporter importer : importers) {
            if (getDataType(importer).equals(dataType) && getFileFormat(importer).equals(fileFormat))
                return importer;
        }
        return null;
    }
}
