package splitstree5.core.algorithms.characters2distances;

public class Uncorrected_P extends HammingDistances {

    public final static String DESCRIPTION = "Calculates uncorrected (observed, \"P\") distances.";
    protected final String TASK = "Uncorrected P Distance";

    @Override
    public String getCitation() {
        return "Hamming 1950; " +
                "Hamming, Richard W. \"Error detecting and error correcting codes\". " +
                "Bell System Technical Journal. 29 (2): 147–160. MR 0035935, 1950.";
    }

    protected String getTask() {
        return TASK;
    }


    /**
     * gets a short description of the algorithm
     *
     * @return a description
     */
    public String getDescription() {
        return DESCRIPTION;
    }
}
