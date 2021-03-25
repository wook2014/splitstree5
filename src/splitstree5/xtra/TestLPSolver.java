/*
 *  TestLPSolver.java Copyright (C) 2021 Daniel H. Huson
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

package splitstree5.xtra;

import junit.framework.TestCase;
import lpsolve.LPSolver;

public class TestLPSolver extends TestCase {

    public void testRunLP() throws Exception {
        final double[][] A = {
                {1, 1, 0, 0, 0, 1},
                {1, 0, 1, 0, 1, 1},
                {1, 0, 0, 1, 1, 0},
                {0, 1, 1, 0, 1, 0},
                {0, 1, 0, 1, 1, 1},
                {0, 0, 1, 1, 0, 1}
        };
        final double[] b = {4, 5, 4, 5, 6, 3};
        final double[] c = {-3, -3, -3, -3, -4, -4};
        final LPSolver lpSolver = new LPSolver(A.length, A[0].length);

        for (int i = 0; i < A.length; i++) {
            lpSolver.addConstraint(A[i], b[i]);
        }
        lpSolver.setObjectiveFunction(c);

        final double[] x = lpSolver.solve();

        System.out.print("Solution:");

        for (double v : x) {
            System.out.printf(" %.6f", v);
        }
        System.out.println();

        final double[] correct = {1, 2, 1, 1, 2, 1};
        for (int i = 0; i < x.length; i++)
            assertEquals(correct[i], x[i], 0.0001);
    }

}
