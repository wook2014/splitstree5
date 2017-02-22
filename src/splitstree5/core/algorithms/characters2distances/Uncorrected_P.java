package splitstree5.core.algorithms.characters2distances;

public class Uncorrected_P extends HammingDistances {

    public final static String DESCRIPTION = "Calculates uncorrected (observed, \"P\") distances.";
    protected final String TASK = "Uncorrected P Distance";

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
