package splitstree5.io.imports.utils;

import java.util.function.DoubleUnaryOperator;

public enum DistanceSimilarityCalculator implements DoubleUnaryOperator{

    one_minus ("D = 1-S", (s) -> 1 - s),
    one_minus_normalize("D = (1-S)/S", (s) -> (1 - s)/s),
    sqrt1("D = sqrt(1-S)", (s) -> Math.sqrt(1-s)),
    sqrt2("D = sqrt(2(1-S))", (s) -> Math.sqrt(2*(1-s))),
    cos("D = arccos(S)", Math::acos),
    log ("D = -ln(S)", (s) -> -Math.log(s));

    private final String label;
    private final DoubleUnaryOperator operator;

    DistanceSimilarityCalculator(String label, DoubleUnaryOperator operator) {
        this.label = label;
        this.operator = operator;
    }

    public String getLabel(){
        return this.label;
    }

    @Override
    public double applyAsDouble(double similarity_value) {
        return operator.applyAsDouble(similarity_value);
    }
}