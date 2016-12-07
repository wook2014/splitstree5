package nexus.bootstrap;

/**
 * Created by Daria on 06.10.2016.
 */
public final class Format {

    // labels
    private boolean labels;
    // List the splits, or just the bootstrap values. This has to be true if there are splits not present in the
    // documents splits block. Perhaps it should always be true, since otherwise we'd lose information.
    private boolean showSplits;
    //??
    private boolean all;

    /*the constructor of Format */
    public Format() {
        labels = false;
        showSplits = true;
        all = true;
    }

    /**
     * Gets the value of labels
     *
     * @return labels
     */
    public boolean getLabels() {
        return labels;
    }

    /**
     * Sets the value of labels
     *
     * @param lab
     */
    public void setLabels(boolean lab) {
        this.labels = lab;
    }

    /**
     * Gets the value of show splits
     *
     * @return showSplits
     */
    public boolean getShowSplits() {
        return showSplits;
    }

    /**
     * Sets the value of show splits
     *
     * @param s
     */
    public void setShowSplits(boolean s) {
        this.showSplits = s;
    }

    /**
     * Gets the value of all
     *
     * @return all
     */
    public boolean getAll() {
        return all;
    }

    /**
     * Sets the value of all
     *
     * @param all
     */
    public void setAll(boolean all) {
        this.all = all;
    }
}
