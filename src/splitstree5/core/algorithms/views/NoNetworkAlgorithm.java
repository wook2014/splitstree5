/*
 * SplitsNetworkAlgorithm.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.core.algorithms.views;

import javafx.application.Platform;
import jloda.phylo.PhyloSplitsGraph;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.ViewerBlock;
import splitstree5.gui.graphtab.ISplitsViewTab;
import splitstree5.gui.graphtab.base.Graph2DTab;

import java.io.IOException;

/**
 * show no network
 * Daniel Huson, 5.2021
 */
public class NoNetworkAlgorithm extends Algorithm<SplitsBlock, ViewerBlock> /* implements IFromSplits, IToViewer */ {
    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock0, SplitsBlock splitsBlock0, ViewerBlock viewerBlock) throws Exception {
        if (splitsBlock0.getNsplits() == 0)
            throw new IOException("No splits in input");
        progress.setTasks("Clear network", "Init.");
        final ISplitsViewTab viewTab = (ISplitsViewTab) viewerBlock.getTab();
        //splitsViewTab.setNodeLabel2Style(nodeLabel2Style);

        Platform.runLater(() -> {
            viewerBlock.getTab().setText(viewerBlock.getName());
        });

        viewTab.init(new PhyloSplitsGraph());
        Platform.runLater(() -> {

            ((Graph2DTab) viewTab).getFitLabel().setText("");
            ((Graph2DTab) viewTab).getScaleBar().setVisible(false);
        });

        viewTab.show();
    }
}
