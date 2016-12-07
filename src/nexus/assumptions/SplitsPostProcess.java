package nexus.assumptions;

import jloda.util.parse.NexusStreamParser;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class SplitsPostProcess {

    boolean leastSquares;
    String filter;
    int dimensionValue;
    float weightThresholdValue;
    float confidenceThresholdValue;

    SplitsPostProcess() {
        leastSquares = false;
        filter = "dimension";
        weightThresholdValue = 0;
        confidenceThresholdValue = 0;
        dimensionValue = 4;
    }

    void write(Writer w) throws IOException {
        w.write("SplitsPostProcess");
        if (leastSquares)
            w.write(" leastSquares");
        w.write(" filter=" + filter);
        if (filter.equalsIgnoreCase("weight"))
            w.write(" value=" + weightThresholdValue);
        if (filter.equalsIgnoreCase("confidence"))
            w.write(" value=" + confidenceThresholdValue);
        if (filter.equalsIgnoreCase("dimension"))
            w.write(" value=" + dimensionValue);
        w.write(";\n");
    }

    void read(NexusStreamParser np) throws IOException {

        if (np.peekMatchIgnoreCase("SplitsPostProcess")) {
            List<String> tokens = np.getTokensLowerCase("SplitsPostProcess", ";");

            leastSquares = false;
            np.findIgnoreCase(tokens, "no leastquares");
            leastSquares = np.findIgnoreCase(tokens, "leastsquares");
            np.findIgnoreCase(tokens, "no leastsquaresagain");
            filter = np.findIgnoreCase(tokens, "filter=", "closesttree greedycompatible greedyWC weight confidence dimension none", "none");
            if (filter.equalsIgnoreCase("weight"))
                weightThresholdValue = (float) np.findIgnoreCase(tokens, "value=", 0, 1000000.0, weightThresholdValue);
            if (filter.equalsIgnoreCase("confidence"))
                confidenceThresholdValue = (float) np.findIgnoreCase(tokens, "value=", 0, 1000000.0, confidenceThresholdValue);
            if (filter.equalsIgnoreCase("dimension"))
                dimensionValue = (int) np.findIgnoreCase(tokens, "value=", 0, 1000000.0, dimensionValue);

            if (tokens.size() != 0)
                throw new IOException("line " + np.lineno() + ": `" + tokens + "' unexpected in SplitsPostProcess");
        }
    }

    public boolean isLeastSquares() {
        return leastSquares;
    }

    public void setLeastSquares(boolean leastSquares) {
        this.leastSquares = leastSquares;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public float getWeightThresholdValue() {
        return weightThresholdValue;
    }

    public void setWeightThresholdValue(float threshold) {
        this.weightThresholdValue = threshold;
    }

    public float getConfidenceThresholdValue() {
        return confidenceThresholdValue;
    }

    public void setConfidenceThresholdValue(float threshold) {
        this.confidenceThresholdValue = threshold;
    }

    public boolean getGreedyCompatible() {
        return filter.equalsIgnoreCase("greedycompatible");
    }

    public boolean getClosestTree() {
        return filter.equalsIgnoreCase("closesttree");
    }

    public boolean getGreedyWC() {
        return filter.equalsIgnoreCase("greedyWC");
    }

    public boolean getWeightThreshold() {
        return filter.equalsIgnoreCase("weight");
    }

    public boolean getConfidenceThreshold() {
        return filter.equalsIgnoreCase("confidence");
    }

    public int getDimensionValue() {
        return dimensionValue;
    }

    public void setDimensionValue(int dimensionValue) {
        this.dimensionValue = dimensionValue;
    }

    public boolean getDimensionFilter() {
        return filter.equalsIgnoreCase("dimension");
    }
}
