/*
 * ImportService.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.dialogs.importer;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.layout.Pane;
import jloda.fx.control.ProgressPane;
import jloda.fx.util.ProgramExecutorService;
import jloda.fx.util.TaskWithProgressListener;
import jloda.fx.window.NotificationManager;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.Pair;
import jloda.util.ProgressListener;
import splitstree5.core.datablocks.*;
import splitstree5.io.imports.interfaces.*;
import splitstree5.io.imports.nexus.TraitsNexusImporter;
import splitstree5.main.MainWindow;

import java.io.IOException;

/**
 * performs import as service in separate thread
 * Daniel Huson, 1.2018
 */
public class ImportService extends Service<Boolean> {
    private MainWindow parentMainWindow;
    private IImporter importer;
    private String fileName;
    private String title;

    private boolean reload = false;

    /**
     * constructor
     */
    public ImportService() {
        setExecutor(ProgramExecutorService.getInstance());
    }

    /**
     * setup a task
     *
     * @param reload
     * @param parentMainWindow
     * @param importer
     * @param fileName
     * @param progressBarParent
     */
    public void setup(boolean reload, MainWindow parentMainWindow, IImporter importer, String fileName, String title, Pane progressBarParent) {
        this.reload = reload;
        this.parentMainWindow = parentMainWindow;
        this.importer = importer;
        this.fileName = fileName;
        this.title = title;

        final ProgressPane progressPane = new ProgressPane(titleProperty(), messageProperty(), progressProperty(), runningProperty(), this::cancel);
        if (progressBarParent != null)
            progressBarParent.getChildren().add(progressPane);

        setOnFailed((e) -> {
            NotificationManager.showError("Import failed: " + getException().getMessage());
        });
    }

    @Override
    protected Task<Boolean> createTask() {
        return new TaskWithProgressListener<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    updateTitle(title);
                    getProgressListener().setMaximum(0);
                    getProgressListener().setProgress(0);

                    final Pair<TaxaBlock, DataBlock> pair = apply(getProgressListener(), importer, fileName);
                    DataLoader.load(reload, fileName, pair.getFirst(), pair.getSecond(), parentMainWindow);
                    return true;
                } catch (Exception ex) {
                    throw ex;
                }
            }
        };
    }

    /**
     * import from a file
     *
     * @param importer
     * @param fileName
     * @return taxa block and data block, or null
     */
    public static Pair<TaxaBlock, DataBlock> apply(ProgressListener progress, IImporter importer, String fileName) throws IOException, CanceledException {
        if (importer == null)
            throw new IOException("No suitable importer found");
        TaxaBlock taxaBlock = new TaxaBlock();
        DataBlock dataBlock;

        if (importer instanceof IImportCharacters) {
            dataBlock = new CharactersBlock();
            ((IImportCharacters) importer).parse(progress, fileName, taxaBlock, (CharactersBlock) dataBlock);
        } else if (importer instanceof IImportDistances) {
            dataBlock = new DistancesBlock();
            ((IImportDistances) importer).parse(progress, fileName, taxaBlock, (DistancesBlock) dataBlock);
        } else if (importer instanceof IImportTrees) {
            dataBlock = new TreesBlock();
            ((IImportTrees) importer).parse(progress, fileName, taxaBlock, (TreesBlock) dataBlock);
        } else if (importer instanceof IImportSplits) {
            dataBlock = new SplitsBlock();
            ((IImportSplits) importer).parse(progress, fileName, taxaBlock, (SplitsBlock) dataBlock);
        } else if (importer instanceof IImportNetwork) {
            dataBlock = new NetworkBlock();
            ((IImportNetwork) importer).parse(progress, fileName, taxaBlock, (NetworkBlock) dataBlock);
        } else
            throw new IOException("Import not implemented for: " + Basic.getShortName(importer.getClass()));
        if (new TraitsNexusImporter().isApplicable(fileName)) {
            final TraitsBlock traitsBlock = new TraitsBlock();
            taxaBlock.setTraitsBlock(traitsBlock);
            new TraitsNexusImporter().parse(progress, fileName, taxaBlock, traitsBlock);
        }
        return new Pair<>(taxaBlock, dataBlock);
    }
}
