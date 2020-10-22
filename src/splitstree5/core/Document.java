/*
 * Document.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.core;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import jloda.fx.control.ItemSelectionModel;
import jloda.util.Basic;
import splitstree5.core.misc.Taxon;
import splitstree5.core.workflow.Workflow;
import splitstree5.info.MethodsTextGenerator;
import splitstree5.main.MainWindow;

import java.util.List;

/**
 * a document
 * Daniel Huson, 12/25/16.
 */
public class Document {
    private static int untitledCount = 0;

    private final Workflow workflow;
    private MainWindow mainWindow;

    private final StringProperty fileName = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final ItemSelectionModel<Taxon> taxaSelectionModel = new ItemSelectionModel<>();

    private final StringProperty methodsText = new SimpleStringProperty();

    private final BooleanProperty dirty = new SimpleBooleanProperty();

    private final BooleanProperty hasSplitsTree5File = new SimpleBooleanProperty(false);

    private final BooleanProperty tmpFile = new SimpleBooleanProperty(true);

    /**
     * constructor
     */
    public Document() {
        name.set((++untitledCount < 2) ? "Untitled" : ("Untitled [" + untitledCount + "]"));
        workflow = new Workflow(this);
        workflow.updatingProperty().addListener((c, o, n) -> updateMethodsText());
        workflow.incrementTopologyChanged();

        fileName.addListener((c, o, n) -> {
            Platform.runLater(() -> {
                name.set(Basic.getFileNameWithoutPath(fileName.get()));
                tmpFile.set(n.endsWith(".tmp"));
            });
        });

        workflow.updatingProperty().addListener((InvalidationListener) c -> setDirty(true));
    }

    /**
     * setup the taxon selection model (after top and working taxa nodes have been set)
     */
    public void setupTaxonSelectionModel() {
        if (workflow.getTopTaxaNode() != null) {
            workflow.getTopTaxaNode().stateProperty().addListener((observable, oldValue, newValue) -> {
                final List<Taxon> taxa = workflow.getTopTaxaNode().getDataBlock().getTaxa();
            });
            final List<Taxon> taxa = workflow.getTopTaxaNode().getDataBlock().getTaxa();
        }

        // for debugging:
        if (false) {
            taxaSelectionModel.getSelectedItems().addListener((ListChangeListener<Taxon>) c -> System.err.println("Taxa selection is: " +
                    Basic.toString(taxaSelectionModel.getSelectedItems(), ",")));
        }
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public String getFileName() {
        return fileName.get();
    }

    public StringProperty fileNameProperty() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public ReadOnlyBooleanProperty updatingProperty() {
        return workflow.updatingProperty();
    }

    public ItemSelectionModel<Taxon> getTaxaSelectionModel() {
        return taxaSelectionModel;
    }

    public ReadOnlyStringProperty methodsTextProperty() {
        return methodsText;
    }

    public void updateMethodsText() {
        methodsText.setValue(MethodsTextGenerator.getInstance().apply(workflow));
    }

    public boolean isDirty() {
        return dirty.get();
    }

    public BooleanProperty dirtyProperty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty.set(dirty);
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public void setMainWindow(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    public ReadOnlyStringProperty nameProperty() {
        return name;
    }

    public boolean isHasSplitsTree5File() {
        return hasSplitsTree5File.get();
    }

    public BooleanProperty hasSplitsTree5FileProperty() {
        return hasSplitsTree5File;
    }

    public void setHasSplitsTree5File(boolean hasSplitsTree5File) {
        this.hasSplitsTree5File.set(hasSplitsTree5File);
    }

    public boolean isTmpFile() {
        return tmpFile.get();
    }

    public ReadOnlyBooleanProperty tmpFileProperty() {
        return tmpFile;
    }
}
