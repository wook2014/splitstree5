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

package splitstree5.gui.graphtab;


import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.layout.Border;
import javafx.scene.layout.GridPane;
import jloda.fx.ZoomableScrollPane;
import jloda.phylo.PhyloTree;
import jloda.util.ResourceManager;
import splitstree5.gui.graphtab.base.GraphLayout;
import splitstree5.gui.graphtab.base.GraphTabBase;
import splitstree5.gui.graphtab.commands.ZoomCommand;
import splitstree5.menu.MenuController;

/**
 * Trees grid tab
 * Daniel Huson, 3.2018
 */
public class TreesGridTab extends GraphTabBase<PhyloTree> {
    private final IntegerProperty rows = new SimpleIntegerProperty(1);
    private final IntegerProperty cols = new SimpleIntegerProperty(1);

    private final BooleanProperty maintainAspectRatio = new SimpleBooleanProperty(true);

    private GridPane gridPane;

    /**
     * constructor
     */
    public TreesGridTab() {
        setName("TreesGrid");
        setIcon(ResourceManager.getIcon("TreeViewer16.gif"));
    }

    /**
     * setup the shape
     *
     * @param rows
     * @param cols
     */
    public void setShape(int rows, int cols) {
        setRows(rows);
        setCols(cols);
        gridPane = new GridPane();
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.setGridLinesVisible(false);
    }

    /**
     * gets the current tree graph
     *
     * @param current, between 1 and rows*cols
     */
    public TreeViewTab getCurrent(int current) {
        final TreeViewTab treeViewTab = new TreeViewTab(false);
        treeViewTab.getContent().setUserData(treeViewTab);
        treeViewTab.getBorderPane().setBorder(Border.EMPTY);
        gridPane.add(treeViewTab.getContent(), current % getCols(), current / getCols());
        return treeViewTab;
    }

    /**
     * show the trees
     */
    public void show() {
        Platform.runLater(() -> {
            final ZoomableScrollPane zoomableScrollPane = new ZoomableScrollPane(gridPane) {
                @Override // override node scaling to use coordinate scaling
                public void updateScale() {
                    for (javafx.scene.Node child : gridPane.getChildren()) {
                        if (child.getUserData() instanceof TreeViewTab) {
                            final TreeViewTab treeViewTab = ((TreeViewTab) child.getUserData());
                            if (treeViewTab.getLayout() == GraphLayout.Radial) {
                                getUndoManager().doAndAdd(new ZoomCommand(getZoomFactorY(), getZoomFactorY(), treeViewTab));
                            } else {
                                getUndoManager().doAndAdd(new ZoomCommand(getZoomFactorX(), getZoomFactorY(), treeViewTab));
                            }
                        }
                    }
                }
            };
            zoomableScrollPane.setLockAspectRatio(isMaintainAspectRatio());

            setContent(zoomableScrollPane);
            for (javafx.scene.Node child : gridPane.getChildren()) {
                if (child.getUserData() instanceof TreeViewTab) {
                    ((TreeViewTab) child.getUserData()).show();
                }
            }

            zoomableScrollPane.zoomBy(2, 2);
        });
    }

    public String getInfo() {
        return "grid of " + getRows() + " x " + getCols() + " trees";
    }

    public int getRows() {
        return rows.get();
    }

    public void setRows(int rows) {
        this.rows.set(rows);
    }

    public int getCols() {
        return cols.get();
    }

    public void setCols(int cols) {
        this.cols.set(cols);
    }

    public int size() {
        return getRows() * getCols();
    }

    @Override
    public void updateMenus(MenuController controller) {
    }

    public boolean isMaintainAspectRatio() {
        return maintainAspectRatio.get();
    }

    public void setMaintainAspectRatio(boolean maintainAspectRatio) {
        this.maintainAspectRatio.set(maintainAspectRatio);
    }
}
