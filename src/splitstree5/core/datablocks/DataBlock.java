/*
 * DataBlock.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.core.datablocks;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import jloda.util.Basic;
import jloda.util.PluginClassLoader;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.Document;
import splitstree5.core.algorithms.interfaces.IFrom;
import splitstree5.core.algorithms.interfaces.ITo;
import splitstree5.core.workflow.DataNode;
import splitstree5.core.workflow.UpdateState;
import splitstree5.io.exports.NexusExporter;
import splitstree5.io.nexus.INexusFormat;
import splitstree5.io.nexus.NexusParser;
import splitstree5.utils.NameableBase;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * The base datablock class
 * Daniel Huson, 12/21/16.
 */
abstract public class DataBlock extends NameableBase {
    private Document document; // the document associated with this datablock
    private DataNode dataNode; // the node associated with this datablock
    // todo: remove the two above
    protected INexusFormat format; // text display format
    private final ChangeListener<UpdateState> stateChangeListener;

    public DataBlock() {
        stateChangeListener = (c, o, n) -> {
            if (n == UpdateState.VALID) {
                setShortDescription(getInfo());
            } else
                setShortDescription(getName());
        };
        setName(Basic.getShortName(getClass()).replaceAll("Block$", ""));
    }

    /**
     * creates a new instance
     *
     * @return new instance
     */
    public DataBlock newInstance() {
        try {
            return getClass().getConstructor().newInstance();
        } catch (Exception e) {
            Basic.caught(e);
            return null;
        }
    }

    abstract public int size();

    abstract public Class<? extends IFrom> getFromInterface();

    abstract public Class<? extends ITo> getToInterface();

    public String toString() {
        return getName();
    }

    /**
     * gets instances of all known data blocks
     *
     * @return
     */
    public static ArrayList<DataBlock> getAllDataBlocks() {
        final ArrayList<DataBlock> list = new ArrayList<>();
        for (Object object : PluginClassLoader.getInstances(DataBlock.class, "splitstree5.core.datablocks")) {
            list.add((DataBlock) object);
        }
        return list;
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

    public DataNode getDataNode() {
        return dataNode;
    }

    public void setDataNode(DataNode dataNode) {
        this.dataNode = dataNode;
        if (dataNode != null) {
            dataNode.stateProperty().addListener(new WeakChangeListener<>(stateChangeListener));
        }
    }

    /**
     * gets the text for interactive display
     *
     * @return display text
     */
    public String getDisplayText() {
        final StringWriter w = new StringWriter();
        try {
            final NexusExporter nexusExporter = new NexusExporter();
            nexusExporter.setPrependTaxa(false);
            if (this instanceof TaxaBlock) {
                nexusExporter.export(w, (TaxaBlock) this);
            } else if (this instanceof AnalysisBlock) {
                nexusExporter.export(w, (AnalysisBlock) this);
            } else {
                final TaxaBlock taxaBlock;
                if (document.getWorkflow().getTopNodes().contains(dataNode))
                    taxaBlock = document.getWorkflow().getTopTaxaNode().getDataBlock();
                else
                    taxaBlock = document.getWorkflow().getWorkingTaxaBlock();
                nexusExporter.export(w, taxaBlock, this);
            }
        } catch (IOException ex) {
            Basic.caught(ex);
        }
        return w.toString();
    }

    public INexusFormat getFormat() {
        return format;
    }

    public void setFormat(INexusFormat format) {
        this.format = format;
    }

    /**
     * copy a datablock
     *
     * @param taxaBlock
     * @param dataBlock
     */
    public void copy(TaxaBlock taxaBlock, DataBlock dataBlock) {
        try (StringWriter w = new StringWriter()) {
            final NexusExporter nexusExporter = new NexusExporter();
            nexusExporter.setPrependTaxa(false);
            nexusExporter.export(w, taxaBlock, dataBlock);
            try (NexusStreamParser np = new NexusStreamParser(new StringReader(w.toString()))) {
                NexusParser.parse(np, taxaBlock, this);
            }
        } catch (IOException e) {
            Basic.caught(e);
        }
    }

    abstract public String getBlockName();
}
