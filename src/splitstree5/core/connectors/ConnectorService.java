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
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.misc.ANode;
import splitstree5.core.misc.ProgramExecutorService;

/**
 * a service used by a method node
 * Created by huson on 12/21/16.
 */
public class ConnectorService<P extends DataBlock, C extends DataBlock> extends Service<Boolean> {
    public static boolean verbose = true;
    private AConnectorNode<P, C> methodNode;

    public ConnectorService(AConnectorNode<P, C> methodNode) {
        this.methodNode = methodNode;
        executorProperty().set(ProgramExecutorService.getExecutorService());
    }

    @Override
    protected Task<Boolean> createTask() {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                System.err.println("--- Compute " + getMethodName() + " called");
                methodNode.getAlgorithm().compute(methodNode.getTaxaBlock(), methodNode.getParent().getDataBlock(), methodNode.getChild().getDataBlock());
                System.err.println("--- Compute " + getMethodName() + " done");
                methodNode.setState(ANode.State.VALID);
                methodNode.getChild().setState(ANode.State.VALID); // child is presumably valid once method has completed...

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
            }

            @Override
            protected void failed() {
                if (verbose)
                    System.err.println("Compute " + getMethodName() + " task failed: " + getException());
                methodNode.setState(ANode.State.INVALID);
            }

            @Override
            protected void cancelled() {
                if (verbose)
                    System.err.println("Compute " + getMethodName() + " task canceled");
                methodNode.setState(ANode.State.INVALID);
            }
        };

    }

    public String getMethodName() {
        return methodNode.getAlgorithm().getName();
    }

}
