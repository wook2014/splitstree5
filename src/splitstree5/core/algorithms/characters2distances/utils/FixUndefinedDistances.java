/*
 * FixUndefinedDistances.java Copyright (C) 2022 Daniel H. Huson
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

package splitstree5.core.algorithms.characters2distances.utils;

import jloda.fx.window.NotificationManager;
import splitstree5.core.datablocks.DistancesBlock;

/**
 * fix undefined distances
 * Daniel Huson, 2.2018
 */
public class FixUndefinedDistances {
    /**
     * apply
     *
	 */
    public static void apply(int ntax, double maxDist, DistancesBlock distancesBlock) {
        int numUndefined = 0;
        for (int s = 1; s <= ntax; s++)
            for (int t = s + 1; t <= ntax; t++) {
                if (distancesBlock.get(s, t) < 0) {
                    distancesBlock.set(s, t, 2.0 * maxDist);
                    distancesBlock.set(t, s, 2.0 * maxDist);
                    numUndefined++;
                }
            }
        if (numUndefined > 0)
            NotificationManager.showWarning("Distance matrix contains " + numUndefined + " undefined " +
                    "distances. These have been arbitrarily set to 2 times the maximum" +
                    " defined distance (= " + (2.0 * maxDist) + ").");

    }
}
