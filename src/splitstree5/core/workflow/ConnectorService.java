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

package splitstree5.core.workflow;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import jloda.fx.NotificationManager;
import jloda.util.Basic;
import jloda.util.CanceledException;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.misc.ProgramExecutorService;
import splitstree5.dialogs.ProgressPane;

/**
 * Service used by connector nodes to run algorithms
 * Daniel Huson, 12/21/16.
 */
public class ConnectorService<P extends DataBlock, C extends DataBlock> extends Service<Boolean> {
    public static boolean verbose = false;
    private Connector<P, C> connector;

    public ConnectorService(Connector<P, C> connector) {
        this.connector = connector;
        executorProperty().set(ProgramExecutorService.getExecutorService());
    }

    @Override
    protected Task<Boolean> createTask() {
        return new MyTask();
    }

    public String getMethodName() {
        return connector.getAlgorithm().getName();
    }

    /**
     * create a task that also provides support for the old progress listener interface
     */
    private class MyTask extends TaskWithProgressListener<Boolean> {
        @Override
        public Boolean call() throws Exception {
            synchronized (connector.getChild().getDataBlock()) { // make sure that we only ever have one task working on a given datablock
                try {
                    if (verbose)
                        System.err.println("--- Compute " + getMethodName() + " called");
                    connector.getChild().stateProperty().set(UpdateState.INVALID);
                    connector.stateProperty().set(UpdateState.COMPUTING);

                    connector.getChild().getDataBlock().clear(); // always start with a fresh datablock
                    Thread.sleep(100);
                    getProgressListener().setTasks(connector.getAlgorithm().getName(), "Running");
                    connector.getAlgorithm().compute(getProgressListener(), connector.getTaxaBlock(), connector.getParent().getDataBlock(), connector.getChild().getDataBlock());
                } catch (CanceledException ex) {
                    if (verbose)
                        System.err.println("USER CANCELED");
                    connector.stateProperty().set(UpdateState.FAILED);
                } finally {
                    if (verbose)
                        System.err.println("--- Compute " + getMethodName() + " done");
                }
                return true;
            }
        }

        @Override
        protected void running() {
            connector.getParent().getDataBlock().getDocument().getMainWindow().getMainWindowController().getBottomPane().getChildren().add(new ProgressPane(ConnectorService.this));
        }

        @Override
        protected void scheduled() {
            if (verbose)
                System.err.println("Compute " + getMethodName() + " task scheduled");
        }

        @Override
        protected void succeeded() {
            if (verbose)
                System.err.println("Compute " + getMethodName() + " task succeeded");
            connector.setState(UpdateState.VALID);
            connector.getChild().setState(UpdateState.VALID); // child is presumably valid once method has completed...
        }

        @Override
        protected void failed() {
            if (verbose) {
                System.err.println("Algorithm " + getMethodName() + " failed: " + getException());
            }
            NotificationManager.showError("Algorithm " + getMethodName() + " failed: " + getException().getMessage());
            Basic.caught(getException());
            connector.setState(UpdateState.FAILED);
        }

        @Override
        protected void cancelled() {
            if (verbose)
                System.err.println("Algorithm " + getMethodName() + " canceled");
            connector.setState(UpdateState.FAILED);
            NotificationManager.showWarning("Algorithm " + getMethodName() + " canceled");
        }
    }
}