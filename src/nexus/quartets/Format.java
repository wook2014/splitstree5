package nexus.quartets;

public final class Format {

    /**
     * Weights present or not
     */
    protected boolean weights;
    /**
     * Are the quartets labeld*
     */
    protected boolean labels;

    /**
     * the Constructor
     */
    public Format() {
        weights = true;
        labels = true;

    }

    /**
     * Gets the weights format
     *
     * @return true, if weights are to be displayed
     */
    public boolean getWeights() {
        return this.weights;
    }

    /**
     * Sets the weights format
     *
     * @param weights
     */
    public void setWeights(boolean weights) {
        this.weights = weights;
    }


    /**
     * Get the value of labels
     *
     * @return the value of labels
     */
    public boolean getLabels() {
        return labels;
    }

    /**
     * Set the value of labels.
     *
     * @param labels the value of labels
     */
    public void setLabels(boolean labels) {
        this.labels = labels;
    }
}
