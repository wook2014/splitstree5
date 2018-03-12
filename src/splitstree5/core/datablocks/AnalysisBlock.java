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

package splitstree5.core.datablocks;


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import splitstree5.core.algorithms.interfaces.IFromAnalysis;
import splitstree5.core.algorithms.interfaces.IToAnalysis;


/**
 * This block saves the result of an analysis in its info variable
 * Daniel Huson 12/2016
 */
public class AnalysisBlock extends DataBlock {
    public static final String BLOCK_NAME = "ANALYSIS";

    public AnalysisBlock() {
        super(BLOCK_NAME);
    }

    private final StringProperty info = new SimpleStringProperty("analysis results");

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public int size() {
        return getShortDescription() == null ? 0 : getShortDescription().length();
    }

    @Override
    public Class getFromInterface() {
        return IFromAnalysis.class;
    }

    @Override
    public Class getToInterface() {
        return IToAnalysis.class;
    }

    @Override
    public String getInfo() {
        return info.get();
    }

    public StringProperty infoProperty() {
        return info;
    }

    public void setInfo(String info) {
        this.info.set(info);
    }
}
