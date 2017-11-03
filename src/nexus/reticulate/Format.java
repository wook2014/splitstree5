package nexus.reticulate;

public class Format implements Cloneable {

    private boolean nettedComponents = false;
    private boolean interleaved = false;
    private boolean internalLabels = false;
    private boolean TreeComponentLabels = false;
    private boolean nettedCompLabels = false;

    /**
     * Constructor
     */
    public Format() {
    }

    /**
     * use the interleaved format
     *
     * @return
     */
    public boolean useInterleaved() {
        return interleaved;
    }

    /**
     * @param interleaved
     */
    public void setUseInterleaved(boolean interleaved) {
        this.interleaved = interleaved;
    }

    /**
     * are netted components used
     *
     * @return
     */
    public boolean useNettedComponents() {
        return nettedComponents;
    }

    /**
     * @param nettedComponents
     */
    public void setUseNettedComponents(boolean nettedComponents) {
        this.nettedComponents = nettedComponents;
    }

    /**
     * show internal labels of the graph
     *
     * @return
     */
    public boolean labelInternalLabels() {
        return internalLabels;
    }

    /**
     * if true all internal nodes are labeled
     *
     * @param show
     */
    public void setLabelInternalLabels(boolean show) {
        this.internalLabels = show;
    }

    /**
     * if true the roots of the TreeComponents are labeled
     *
     * @param show
     */
    public void setLabelTreeComponentRoots(boolean show) {
        TreeComponentLabels = show;
    }

    /**
     * @return
     */
    public boolean labelTreeComponentRoots() {
        return TreeComponentLabels;
    }

    /**
     * if true the roots of the netted components are labeled
     *
     * @param show
     */
    public void setLabelNettedComponentsLabels(boolean show) {
        nettedCompLabels = show;
    }

    /**
     * @return
     */
    public boolean labelNettedComponentsLabels() {
        return nettedCompLabels;
    }
}
