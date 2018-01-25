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

package splitstree5.dialogs.exporter;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import jloda.util.CanceledException;
import splitstree5.core.connectors.TaskWithProgressListener;
import splitstree5.core.datablocks.ADataBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.dialogs.exports.ExportManager;

import java.io.IOException;

/**
 * performs import as service in separate thread
 * Daniel Huson, 1.2018
 */
public class ExporterService extends Service<Boolean> {
    private String fileName;
    private TaxaBlock taxaBlock;
    private ADataBlock aDataBlock;
    private String exportFormatName;


    private ExportDialog exportDialog;

    public void setup(String fileName, TaxaBlock taxaBlock, ADataBlock aDataBlock, String exportFormatName, ExportDialog exportDialog) {
        this.fileName = fileName;
        this.taxaBlock = taxaBlock;
        this.aDataBlock = aDataBlock;
        this.exportFormatName = exportFormatName;
        this.exportDialog = exportDialog;
    }

    @Override
    protected Task<Boolean> createTask() {
        return new TaskWithProgressListener<Boolean>() {
            @Override
            protected Boolean call() {
                try {
                    getProgressListener().setMaximum(0);
                    getProgressListener().setProgress(0);
                } catch (CanceledException e) {
                    cancel();
                }
                try {
                    ExportManager.getInstance().exportFile(fileName, taxaBlock, aDataBlock, exportFormatName);
                } catch (IOException ex) {
                    return false;
                }
                return true;
            }
        };
    }

    @Override
    protected void running() {
        exportDialog.getController().getProgressBar().setVisible(true);
        exportDialog.getController().getProgressBar().progressProperty().bind(progressProperty());
        exportDialog.getController().getCancelButton().setOnAction((c) -> {
            cancel();
            exportDialog.close();
        });
    }

    @Override
    protected void succeeded() {
        exportDialog.getController().getProgressBar().setVisible(false);
        exportDialog.getController().getProgressBar().progressProperty().unbind();
        exportDialog.getController().getCancelButton().setOnAction((c) -> {
            exportDialog.close();
        });
    }

    @Override
    protected void cancelled() {
        exportDialog.getController().getProgressBar().setVisible(false);
        exportDialog.getController().getProgressBar().progressProperty().unbind();
        exportDialog.getController().getCancelButton().setOnAction((c) -> {
            exportDialog.close();
        });
    }

    @Override
    protected void failed() {
        exportDialog.getController().getProgressBar().setVisible(false);
        exportDialog.getController().getProgressBar().progressProperty().unbind();
        exportDialog.getController().getCancelButton().setOnAction((c) -> {
            exportDialog.close();
        });
    }
}
