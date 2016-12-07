package nexus.splits;

/**
 * Created by Daria on 10.10.2016.
 */
public class Format implements Cloneable {

    private boolean labels = false;
    private boolean weights = true;
    private boolean confidences = false;
    private boolean intervals = false;

    /**
     * Constructor
     */
    public Format() {
    }

    /**
     * Show labels?
     *
     * @return true, if labels are to be printed
     */
    public boolean getLabels() {
        return labels;
    }

    /**
     * Show weights?
     *
     * @return true, if weights are to be printed
     */
    public boolean getWeights() {
        return weights;
    }

    /**
     * Show labels
     *
     * @param flag whether labels should be printed
     */
    public void setLabels(boolean flag) {
        labels = flag;
    }

    /**
     * Show weights
     *
     * @param flag whether weights should be printed
     */
    public void setWeights(boolean flag) {
        weights = flag;
    }

    /**
     * show confidences?
     *
     * @return confidence
     */
    public boolean getConfidences() {
        return confidences;
    }

    /**
     * show confidences?
     *
     * @param confidences
     */
    public void setConfidences(boolean confidences) {
        this.confidences = confidences;
    }

    /**
     * show confidence intervals?
     */
    public boolean getIntervals() {
        return intervals;
    }

    /**
     * show confidence intervals?
     *
     * @param intervals
     */
    public void setIntervals(boolean intervals) {
        this.intervals = intervals;
    }
}
