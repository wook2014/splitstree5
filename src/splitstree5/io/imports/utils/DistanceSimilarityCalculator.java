/*
 * DistanceSimilarityCalculator.java Copyright (C) 2022 Daniel H. Huson
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

package splitstree5.io.imports.utils;

import java.util.function.DoubleUnaryOperator;

public enum DistanceSimilarityCalculator implements DoubleUnaryOperator {

    one_minus("D = 1-S", (s) -> 1 - s),
    one_minus_normalize("D = (1-S)/S", (s) -> (1 - s) / s),
    sqrt1("D = sqrt(1-S)", (s) -> Math.sqrt(1 - s)),
    sqrt2("D = sqrt(2(1-S))", (s) -> Math.sqrt(2 * (1 - s))),
    cos("D = arccos(S)", Math::acos),
    log("D = -ln(S)", (s) -> -Math.log(s));

    private final String label;
    private final DoubleUnaryOperator operator;

    DistanceSimilarityCalculator(String label, DoubleUnaryOperator operator) {
        this.label = label;
        this.operator = operator;
    }

    public String getLabel() {
        return this.label;
    }

    @Override
    public double applyAsDouble(double similarity_value) {
        return operator.applyAsDouble(similarity_value);
    }
}