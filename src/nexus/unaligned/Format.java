package nexus.unaligned;

import jloda.util.parse.NexusStreamTokenizer;
import splitstree4.core.SplitsException;

/*******************
 * CHANGED TO ENUM *
 *******************/

public final class Format {

    private DataTypesUA datatype;
    // instead of
    //private String datatype, symbols;
    private boolean respectCase, labels;
    private char missing;

    /**
     * the Constructor
     */
    public Format() {
        datatype = DataTypesUA.STANDARD;
        respectCase = false;
        labels = true;
        missing = '?';
    }

    /**
     * Gets the datatype
     *
     * @return datatype
     */
    public DataTypesUA getDatatype() {
        return this.datatype;
    }

    /**
     * Sets the datatype
     *
     * @param datatype the datatype
     */
    private void setDatatype(DataTypesUA datatype) {
        this.datatype = datatype;
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

    /**
     * Get the value of respectcase
     *
     * @return the value of respectcase
     */

    public boolean getRespectCase() {
        return respectCase;
    }

    /**
     * Set the value of respectcase
     *
     * @param respectCase the value of respectCase
     */
    public void setRespectCase(boolean respectCase) {
        this.respectCase = respectCase;
    }

    /**
     * Get the value of the missing character
     *
     * @return the value of the missing character
     */
    public char getMissing() {
        return missing;
    }

    /**
     * Set the value of missing
     *
     * @param missing
     */
    public void setMissing(char missing) throws SplitsException {
        if (NexusStreamTokenizer.isLabelPunctuation(missing)
                || NexusStreamTokenizer.isSpace(missing))
            throw new SplitsException("illegal missing-character:" + missing);
        this.missing = missing;

    }
}
