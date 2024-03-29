/*
 * EmbeddingService.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.gui.graph3dtab;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Point3D;
import jloda.fx.util.ProgramExecutorService;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloSplitsGraph;
import splitstree5.gui.graphtab.base.NodeViewBase;

/**
 * computes an embedding
 * Daniel Huson, 1.2018
 */
public class EmbeddingService extends Service<NodeArray<Point3D>> {
    private PhyloSplitsGraph splitsGraph;
    private NodeArray<NodeViewBase> node2view;
    private int numOfSteps;
    private boolean linear;
    private boolean withZpush;

    public EmbeddingService() {
        setExecutor(ProgramExecutorService.getInstance());
    }

    /**
     * setup the task
     *
	 */
    public void setup(PhyloSplitsGraph splitsGraph, NodeArray<NodeViewBase> node2view, int numOfSteps, boolean linear, boolean withZpush) {
        this.splitsGraph = splitsGraph;
        this.node2view = node2view;
        this.numOfSteps = numOfSteps;
        this.linear = linear;
        this.withZpush = withZpush;
    }

    @Override
    protected Task<NodeArray<Point3D>> createTask() {
        return new ForceCalculatorTask(splitsGraph, node2view, numOfSteps, linear, withZpush);
    }
}
