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

package splitstree5.core.connectors;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.dag.UpdateState;
import splitstree5.core.datablocks.ADataBlock;
import splitstree5.core.misc.ProgramExecutorService;

/**
 * a service used by a method node
 * Created by huson on 12/21/16.
 */
public class ConnectorService<P extends ADataBlock, C extends ADataBlock> extends Service<Boolean> {
    public static boolean verbose = true;
    private AConnector<P, C> connectorNode;

    public ConnectorService(AConnector<P, C> connectorNode) {
        this.connectorNode = connectorNode;
        executorProperty().set(ProgramExecutorService.getExecutorService());
    }

    @Override
    protected Task<Boolean> createTask() {
        return new MyTask();
    }

    public String getMethodName() {
        return connectorNode.getAlgorithm().getName();
    }

    /**
     * create a task that also provides support for the old progress listener interface
     */
    private class MyTask extends Task<Boolean> {
        @Override
        protected Boolean call() throws Exception {
            System.err.println("--- Compute " + getMethodName() + " called");
            try {
                connectorNode.getChild().getDataBlock().clear();
                connectorNode.getAlgorithm().compute(getProgressListener(), connectorNode.getTaxaBlock(), connectorNode.getParent().getDataBlock(), connectorNode.getChild().getDataBlock());
            } catch (CanceledException ex) {
                System.err.println("USER CANCELED");
            }
            System.err.println("--- Compute " + getMethodName() + " done");
            return true;
        }

        @Override
        protected void running() {
            if (verbose)
                System.err.println("Compute " + getMethodName() + " task running");
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
            connectorNode.setState(UpdateState.VALID);
            connectorNode.getChild().setState(UpdateState.VALID); // child is presumably valid once method has completed...
        }

        @Override
        protected void failed() {
            if (verbose)
                System.err.println("Compute " + getMethodName() + " task failed: " + getException());
            connectorNode.setState(UpdateState.FAILED);
        }

        @Override
        protected void cancelled() {
            if (verbose)
                System.err.println("Compute " + getMethodName() + " task canceled");
            connectorNode.setState(UpdateState.FAILED);
        }

        public ProgressListener getProgressListener() {
            return new ProgressListener() {
                private long currentProgress = 0;
                private long maxProgress = 0;
                private boolean cancelable = true;
                private boolean isCanceled = false;
                private boolean debug = false;

                @Override
                public void setMaximum(long maxProgress) {
                    if (debug)
                        System.err.println("progress.setMaximum(" + maxProgress + ")");
                    this.maxProgress = maxProgress;
                    MyTask.this.updateProgress(currentProgress, maxProgress);
                }

                @Override
                public void setProgress(long currentProgress) throws CanceledException {
                    checkForCancel();
                    this.currentProgress = currentProgress;
                    if (debug)
                        System.err.println("progress.setProgress(" + currentProgress + ")");
                    MyTask.this.updateProgress(currentProgress, maxProgress);
                }

                @Override
                public long getProgress() {
                    return currentProgress;
                }

                @Override
                public void checkForCancel() throws CanceledException {
                    isCanceled = MyTask.this.cancel();
                    if (cancelable && isCanceled) {
                        if (debug)
                            System.err.println("progress.checkForCancel()=true");
                        throw new CanceledException();
                    }
                }

                @Override
                public void setTasks(String taskName, String subtaskName) {
                    MyTask.this.updateTitle(taskName);
                    MyTask.this.updateMessage(subtaskName);
                    if (debug)
                        System.err.println("progress.setTasks(" + taskName + "," + subtaskName + ")");
                }

                @Override
                public void setSubtask(String subtaskName) {
                    MyTask.this.updateMessage(subtaskName);
                    if (debug)
                        System.err.println("progress.setSubtask(" + subtaskName + ")");
                }

                @Override
                public void setCancelable(boolean enabled) {
                    this.cancelable = enabled;
                }

                @Override
                public boolean isUserCancelled() {
                    return false;
                }

                @Override
                public void setUserCancelled(boolean userCancelled) {
                    this.isCanceled = userCancelled;
                }

                @Override
                public void incrementProgress() throws CanceledException {
                    if (debug)
                        System.err.println("progress.incrementProgress()");
                    MyTask.this.updateProgress(++currentProgress, maxProgress);
                }

                @Override
                public void close() {
                    if (debug)
                        System.err.println("progress.close()");
                }

                @Override
                public boolean isCancelable() {
                    return cancelable;
                }

                @Override
                public void setDebug(boolean debug) {
                    this.debug = debug;
                }
            };
        }
    }

}
