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

package splitstree5.main.auxwindow;

import javafx.scene.control.Tab;
import javafx.stage.Stage;

/**
 * stage supplier for tabs that can undocked to their own stage
 * daniel Huson 1.2018
 */
public interface IStageSupplier {
    /**
     * supplies a stage to put the tab in
     *
     * @param tab
     * @return stage
     */
    Stage supplyStage(Tab tab, double width, double height);

    /**
     * handle opened stage
     *
     * @param stage
     */
    void openedStage(Stage stage, Tab tab);

    /**
     * handle closed stage
     *
     * @param stage
     */
    void closedStage(Stage stage, Tab tab);
}
