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

package splitstree5.core.algorithms;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import jloda.util.Basic;
import jloda.util.ProgressListener;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.workflow.Connector;
import splitstree5.gui.algorithmtab.next.OptionNext;
import splitstree5.gui.algorithmtab.next.OptionValueType;
import splitstree5.utils.NameableBase;
import splitstree5.utils.Option;
import splitstree5.utils.OptionsAccessor;

/**
 * An algorithm
 * Daniel Huson 12.2016
 */
abstract public class Algorithm<P extends DataBlock, C extends DataBlock> extends NameableBase {
    public static final String BLOCK_NAME = "ALGORITHM";

    final private ObjectProperty<Connector<P, C>> connector = new SimpleObjectProperty<>();

    /**
     * constructor
     */
    public Algorithm() {
        setName(Basic.getShortName(getClass()));
        if (getName().endsWith("Filter"))
            setShortDescription(Basic.fromCamelCase(getName()).replaceAll("Filter", "filter"));
        else
            setShortDescription(Basic.fromCamelCase(getName()) + " algorithm");
    }

    /**
     * named constructor
     *
     * @param name
     */
    public Algorithm(String name) {
        setName(name);
    }

    /**
     * named constructor
     *
     * @param name
     */
    public Algorithm(String name, String shortDescription) {
        super(name, shortDescription);
    }

    /**
     * setup properties here, called just before algorithm pane is displayed
     *
     * @param taxaBlock
     * @param parent
     * @throws Exception
     */
    public void setupBeforeDisplay(TaxaBlock taxaBlock, P parent) {
    }

    /**
     * perform the computation
     */
    abstract public void compute(ProgressListener progress, TaxaBlock taxaBlock, P parent, C child) throws Exception;

    /**
     * clear temporary data
     */
    public void clear() {
        super.clear();
    }

    /**
     * determines whether applicable
     *
     * @param taxaBlock
     * @param parent
     */
    public boolean isApplicable(TaxaBlock taxaBlock, P parent) {
        return true;
    }

    /**
     * gets the name of this algorithm
     *
     * @return name
     */
    public String toString() {
        return getName();
    }

    /**
     * reports the parameters used by this algorithm
     *
     * @return parameters
     */
    final public String getParameters() {
        final StringBuilder buf = new StringBuilder();
        {
            for (OptionNext option : OptionNext.getAllOptions(this)) {
                if (buf.length() > 0)
                    buf.append(", ");
                buf.append(option.getName()).append(" = ").append(OptionValueType.toStringType(option.getOptionValueType(), option.getProperty().getValue()));
            }
        }
        if (buf.length() == 0) { // todo: stop using this
            for (Option option : OptionsAccessor.getAllOptions(this)) {
                if (buf.length() > 0)
                    buf.append(", ");
                buf.append(option.getName()).append(" = ").append(option.getValue().toString());
            }
        }
        return buf.toString();
    }

    /**
     * gets the associated connector
     */
    public Connector<P, C> getConnector() {
        return connector.getValue();
    }

    /**
     * sets the associated connector
     */
    public void setConnector(Connector<P, C> connector) {
        this.connector.setValue(connector);
    }

    public ObjectProperty<Connector<P, C>> connectorProperty() {
        return connector;
    }

    /**
     * gets a new instance of this algorithm
     *
     * @return new instance
     */
    public Algorithm newInstance() {
        try {
            return getClass().newInstance();
        } catch (Exception e) {
            Basic.caught(e);
            return null;
        }

    }

    /**
     * gets the citations for this method
     *
     * @return citations in the format   Key; Citation; Key; Citation; ..
     * e.g. Huson et al 2012; D.H. Huson, R. Rupp and C. Scornavacca, Phylogenetic Networks, Cambridge University Press, 2012
     */
    public String getCitation() {
        return null;
    }

    public String getBlockName() {
        return BLOCK_NAME;
    }
}
