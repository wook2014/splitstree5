/*
 *  Copyright (C) 2017 Daniel H. Huson
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

import jloda.util.Basic;
import jloda.util.PluginClassLoader;
import splitstree5.core.Document;
import splitstree5.utils.OptionableBase;

import java.util.ArrayList;

/**
 * A data block
 * Created by huson on 12/21/16.
 */
abstract public class ADataBlock extends OptionableBase {
    private Document document; // the document associated with this datablock

    /**
     * default constructor
     */
    public ADataBlock() {
    }

    /**
     * creates a new instance
     *
     * @return new instance
     */
    public ADataBlock newInstance() {
        try {
            return getClass().newInstance();
        } catch (Exception e) {
            Basic.caught(e);
            return null;
        }
    }

    abstract public int size();

    abstract public Class getFromInterface();

    abstract public Class getToInterface();

    public String toString() {
        return getName();
    }


    /**
     * gets instances of all known datablocks
     *
     * @return
     */
    public static ArrayList<ADataBlock> getAllDataBlocks() {
        final ArrayList<ADataBlock> list = new ArrayList<>();
        for (Object object : PluginClassLoader.getInstances(ADataBlock.class, "splitstree5.core.datablocks")) {
            list.add((ADataBlock) object);
        }
        return list;
    }

    public String getName() {
        return super.getName();
    }

    /**
     * gets the info for this block
     *
     * @return info
     */
    abstract public String getInfo();

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
