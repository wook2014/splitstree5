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

package splitstree5.core.datablocks;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import splitstree5.core.misc.ASplit;

/**
 * A splits block
 * Created by huson on 12/21/16.
 */
public class SplitsBlock extends ADataBlock {
    private final ObservableList<ASplit> splits;

    // some properties that a set of splits has:
    public enum COMPATIBILITY {
        Compatible, cyclic, weaklyCompatible, incompatible, unknown
    }

    private COMPATIBILITY compatibility = COMPATIBILITY.unknown;
    private float fit = -1;
    private float leastSquaresFit = -1;
    private boolean weightsRepresentLeastSquares = false;

    /**
     * default constructor
     */
    public SplitsBlock() {
        splits = FXCollections.observableArrayList();
    }

    /**
     * named constructor
     *
     * @param name
     */
    public SplitsBlock(String name) {
        this();
        setName(name);
    }

    /**
     * access the splits
     *
     * @return splits
     */
    public ObservableList<ASplit> getSplits() {
        return splits;
    }

    public String getInfo() {
        return "Number of splits: " + getSplits().size();
    }

    public COMPATIBILITY getCompatibility() {
        return compatibility;
    }

    public void setCompatibility(COMPATIBILITY compatibility) {
        this.compatibility = compatibility;
    }

    public float getFit() {
        return fit;
    }

    public void setFit(float fit) {
        this.fit = fit;
    }

    public float getLeastSquaresFit() {
        return leastSquaresFit;
    }

    public void setLeastSquaresFit(float leastSquaresFit) {
        this.leastSquaresFit = leastSquaresFit;
    }

    public boolean isWeightsRepresentLeastSquares() {
        return weightsRepresentLeastSquares;
    }

    public void setWeightsRepresentLeastSquares(boolean weightsRepresentLeastSquares) {
        this.weightsRepresentLeastSquares = weightsRepresentLeastSquares;
    }
}
