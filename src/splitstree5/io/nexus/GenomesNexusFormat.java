/*
 * DistancesNexusFormat.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.io.nexus;

import java.util.List;

/**
 * Genomes format
 * Daniel Huson, 3.2020
 */
public class GenomesNexusFormat implements INexusFormat {
    private boolean optionLabels;
    private boolean optionAccessions;
    private boolean optionMultiPart;
    private boolean optionFiles;

    /**
     * the Constructor
     */
    public GenomesNexusFormat() {
    }

    public boolean isOptionLabels() {
        return optionLabels;
    }

    public void setOptionLabels(boolean optionLabels) {
        this.optionLabels = optionLabels;
    }

    public boolean isOptionAccessions() {
        return optionAccessions;
    }

    public void setOptionAccessions(boolean optionAccessions) {
        this.optionAccessions = optionAccessions;
    }

    public boolean isOptionMultiPart() {
        return optionMultiPart;
    }

    public void setOptionMultiPart(boolean optionMultiPart) {
        this.optionMultiPart = optionMultiPart;
    }

    public boolean isOptionFiles() {
        return optionFiles;
    }

    public void setOptionFiles(boolean optionFiles) {
        this.optionFiles = optionFiles;
    }

    @Override
    public List<String> listOptions() {
        return null;
    }
}
