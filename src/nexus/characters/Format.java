package nexus.characters;

import jloda.util.parse.NexusStreamTokenizer;
import splitstree4.core.SplitsException;

/**
 * Created by Daria on 04.10.2016.
 */
public final class Format implements Cloneable {

    //ToDo: methods that change the format should be protected

    /*******************
     * CHANGED TO ENUM *
     *******************/
    private DataTypesChar datatype;
    // instead of
    // private String datatype;
    // private int datatypeID;
    // private String symbols;

    /**
     * boolean used to determine if the Case should be respected in calculations.
     */
    private boolean respectCase;
    /**
     * boolean used to determine if the matrix format is transpose.
     */
    private boolean transpose;
    /**
     * boolean used to determine if the matrix format is interleave.
     */
    private boolean interleave;
    /**
     * boolean used to determine if the characters have labels.
     */
    private boolean labels;
    /**
     * boolean used to determine if taxon labels should be surrounded by quotes
     */
    private boolean labelQuotes;

    /**
     * boolean used to determine if there is a token.
     */
    private boolean tokens;

    /**
     * boolean used to determine if data is diploid (otherwise assumed haploid)
     * //TODO: Could change to general ploidy number???
     */
    private boolean diploid;

    /**
     * char which holds the symbol for a missing value (standard value = '?').
     */
    private char missing;
    /**
     * char which holds the symbol for the gap (standard value = '-').
     */
    private char gap;
    /***
     * char which holds the symbol for an match (standard value = '0').
     */
    private char matchChar;

    /**
     * the Constructor
     */
    public Format() {
        datatype = DataTypesChar.UNKNOWN;
        respectCase = false;
        gap = '-';
        missing = '?';
        labels = true;
        labelQuotes = true;
        transpose = false;
        interleave = false;
        diploid = false;
        tokens = false;
        matchChar = 0;
    }

    /**
     * Clone the format object
     *
     * @return Format clone of object.
     */
    public Object clone() {
        Format result = new Format();

        result.datatype = datatype;
        result.respectCase = respectCase;
        result.gap = gap;
        result.missing = missing;
        result.labels = labels;
        result.labelQuotes = labelQuotes;
        result.transpose = transpose;
        result.interleave = interleave;
        result.diploid = diploid;
        result.tokens = tokens;
        result.matchChar = matchChar;

        return result;
    }

    /**
     * Get the datatype.
     *
     * @return datatype
     */
    public DataTypesChar getDatatype() {
        return this.datatype;
    }

    /**
     * Set the datatype.
     */
    /*******************
     * CHANGED TO ENUM *
     *******************/
    public void setDatatype(DataTypesChar datatype) {
        this.datatype = datatype;

        // not defined in interface, not static
        //computeColors();
    }


    /**
     * Get the value of labels
     *
     * @return the value of labels
     */
    public boolean getLabels() {
        return this.labels;
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
     * Get flag of whether taxon labels printed with quotes
     *
     * @return boolean
     */
    public boolean isLabelQuotes() {
        return labelQuotes;
    }

    /**
     * Set flag of whether labels are printed with quotes
     *
     * @param labelQuotes true if labels always printed in single quotes
     */
    public void setLabelQuotes(boolean labelQuotes) {
        this.labelQuotes = labelQuotes;
    }


    /**
     * Get the value of respectcase
     *
     * @return the value of respectcase
     */

    public boolean getRespectCase() {
        return this.respectCase;
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
     * Get the value of matchchar
     *
     * @return the value of matchar
     */
    public char getMatchchar() {
        return this.matchChar;
    }

    /**
     * Set the value of the matchchar
     *
     * @param matchChar char used to indicate a match (usually '.')
     * @throws SplitsException if the character is punctuation or a space.
     */
    public void setMatchchar(char matchChar) throws SplitsException {
        if (NexusStreamTokenizer.isLabelPunctuation(matchChar)
                || NexusStreamTokenizer.isSpace(matchChar))
            throw new SplitsException("illegal match-character: " + matchChar);
        this.matchChar = matchChar;
    }

    /**
     * Get the value of gap
     *
     * @return the value of gap
     */
    public char getGap() {
        return this.gap;
    }

    /**
     * Set the value of gap
     *
     * @param gap char character used to indicate a gap.
     * @throws SplitsException if the character is punctuation or a space.
     */
    public void setGap(char gap) throws SplitsException {
        if (NexusStreamTokenizer.isLabelPunctuation(gap)
                || NexusStreamTokenizer.isSpace(gap))
            throw new SplitsException("illegal gap-character:" + gap);
        this.gap = gap;

    }

    /**
     * Get the value of the missing character
     *
     * @return the value of the missing character
     */
    public char getMissing() {
        return this.missing;
    }

    /**
     * Set the value of missing
     *
     * @param missing sets character used to indicate a missing state
     * @throws SplitsException if character is illegal
     */
    public void setMissing(char missing) throws SplitsException {
        if (NexusStreamTokenizer.isLabelPunctuation(missing)
                || NexusStreamTokenizer.isSpace(missing))
            throw new SplitsException("illegal missing-character:" + missing);
        this.missing = missing;

    }

    /**
     * Get the value of transpose
     *
     * @return the value of transpose
     */
    public boolean getTranspose() {
        return this.transpose;
    }

    /**
     * Set the value of transpose
     *
     * @param transpose set flag, with true indicating that matrix is transposed with columns = taxa.
     */
    public void setTranspose(boolean transpose) {
        this.transpose = transpose;
    }

    /**
     * Get the value of interleave
     *
     * @return the value of interleave
     */
    public boolean getInterleave() {
        return interleave;
    }

    /**
     * Set the value of interleave
     *
     * @param interleave boolean flag. Set to true if matrix is interleaved
     */
    public void setInterleave(boolean interleave) {
        this.interleave = interleave;
    }

    /**
     * Get value of tokens
     *
     * @return value of tokens
     */
    public boolean getTokens() {
        return this.tokens;
    }

    /**
     * Set the value of tokens
     *
     * @param tokens boolean. Set to true if states in matrix identified by tokens (not properly supported yet)
     */
    public void setTokens(boolean tokens) {
        this.tokens = tokens;
    }

    /**
     * Set the flag indicating whether this is diploid or not
     *
     * @return vale of the flag diploid
     */
    public boolean isDiploid() {
        return diploid;
    }

    /**
     * Set the value of the flag diploid, indicating whether data is diploid or not
     *
     * @param diploid true if alternate sites from different strands
     */
    public void setDiploid(boolean diploid) {
        this.diploid = diploid;
    }

    /**
     * isUnknownType
     *
     * @return true if the datatype is unknown *
     */
    /*******************
     * CHANGED TO ENUM *
     *******************/
    public boolean isUnknownType() {
        return (datatype == DataTypesChar.UNKNOWN);
    }

    /**
     * isNucleotideType
     *
     * @return true if the datatype is DNA or RNA *
     */
    /*******************
     * CHANGED TO ENUM *
     *******************/
    public boolean isNucleotideType() {
        return (datatype == DataTypesChar.DNA || datatype == DataTypesChar.RNA);
    }
}
