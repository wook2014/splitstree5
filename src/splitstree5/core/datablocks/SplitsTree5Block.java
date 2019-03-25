/*
 *  Copyright (C) 2019 Daniel H. Huson
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

import javafx.beans.property.*;
import splitstree5.main.Version;

/**
 * SplitsTree5 block is used to characterize a fully saved workflow
 * daniel Huson, 3.2018
 */
public class SplitsTree5Block extends DataBlock implements IAdditionalBlock {
    public static final String BLOCK_NAME = "SPLITSTREE5";

    final private IntegerProperty optionNumberOfDataNodes = new SimpleIntegerProperty(0);
    final private IntegerProperty optionNumberOfAlgorithms = new SimpleIntegerProperty(0);
    final private StringProperty optionVersion = new SimpleStringProperty(Version.SHORT_DESCRIPTION);
    final private LongProperty optionCreationDate = new SimpleLongProperty(System.currentTimeMillis());

    public SplitsTree5Block() {
    }

    public void clear() {
        super.clear();
        setOptionNumberOfDataNodes(0);
        setOptionNumberOfAlgorithms(0);
        setOptionCreationDate(System.currentTimeMillis());
        setOptionVersion(Version.SHORT_DESCRIPTION);
    }

    @Override
    public int size() {
        return getOptionNumberOfDataNodes() + getOptionNumberOfAlgorithms();
    }

    public String getOptionVersion() {
        return optionVersion.get();
    }

    public StringProperty optionVersionProperty() {
        return optionVersion;
    }

    public void setOptionVersion(String optionVersion) {
        this.optionVersion.set(optionVersion);
    }

    public long getOptionCreationDate() {
        return optionCreationDate.get();
    }

    public LongProperty optionCreationDateProperty() {
        return optionCreationDate;
    }

    public void setOptionCreationDate(long optionCreationDate) {
        this.optionCreationDate.set(optionCreationDate);
    }

    public int getOptionNumberOfDataNodes() {
        return optionNumberOfDataNodes.get();
    }

    public IntegerProperty optionNumberOfDataNodesProperty() {
        return optionNumberOfDataNodes;
    }

    public void setOptionNumberOfDataNodes(int optionNumberOfDataNodes) {
        this.optionNumberOfDataNodes.set(optionNumberOfDataNodes);
    }

    public int getOptionNumberOfAlgorithms() {
        return optionNumberOfAlgorithms.get();
    }

    public IntegerProperty optionNumberOfAlgorithmsProperty() {
        return optionNumberOfAlgorithms;
    }

    public void setOptionNumberOfAlgorithms(int optionNumberOfAlgorithms) {
        this.optionNumberOfAlgorithms.set(optionNumberOfAlgorithms);
    }

    @Override
    public Class getFromInterface() {
        return null;
    }

    @Override
    public Class getToInterface() {
        return null;
    }

    @Override
    public String getInfo() {
        return "Workflow of size " + size();
    }

    @Override
    public String getBlockName() {
        return BLOCK_NAME;
    }
}
