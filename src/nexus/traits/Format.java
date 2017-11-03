package nexus.traits;

public class Format {

    public final String COMMA = "COMMA";
    public final String SPACES = "SPACES";
    public final String TAB = "TAB";

    private boolean taxonLabels; //flag indicating whether first column contains taxon labels
    private char missingTrait;
    private String separator;

    /**
     * Constructor
     */
    public Format() {
        taxonLabels = false;
        missingTrait = '?';
        separator = TAB;
    }


    public boolean hasTaxonLabels() {
        return taxonLabels;
    }

    public void setTaxonLabels(boolean taxonLabels) {
        this.taxonLabels = taxonLabels;
    }

    public char getMissingTrait() {
        return missingTrait;
    }

    public void setMissingTrait(char missingTrait) {
        this.missingTrait = missingTrait;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }
}
