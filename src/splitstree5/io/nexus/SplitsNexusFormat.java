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

package splitstree5.io.nexus;

/**
 * splits format
 * Created by huson on 12/29/16.
 */
public class SplitsNexusFormat {

    private boolean labels = false;
    private boolean weights = true;
    private boolean confidences = false;

    /**
     * Constructor
     */
    public SplitsNexusFormat() {
    }

    /**
     * Show labels?
     *
     * @return true, if labels are to be printed
     */
    public boolean isLabels() {
        return labels;
    }

    /**
     * Show weights?
     *
     * @return true, if weights are to be printed
     */
    public boolean isWeights() {
        return weights;
    }

    /**
     * Show labels
     *
     * @param flag whether labels should be printed
     */
    public void setLabels(boolean flag) {
        labels = flag;
    }

    /**
     * Show weights
     *
     * @param flag whether weights should be printed
     */
    public void setWeights(boolean flag) {
        weights = flag;
    }

    /**
     * show confidences?
     *
     * @return confidence
     */
    public boolean isConfidences() {
        return confidences;
    }

    /**
     * show confidences?
     *
     * @param confidences
     */
    public void setConfidences(boolean confidences) {
        this.confidences = confidences;
    }

}
