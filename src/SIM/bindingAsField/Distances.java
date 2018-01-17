package SIM.bindingAsField;


import javafx.beans.binding.IntegerBinding;

public class Distances {

    private IntegerBinding ntax;

    public Distances(IntegerBinding ntax) {

        /*this.ntax = new IntegerBinding() {
            @Override
            protected int computeValue() {
                return n.getValue();
            }
        };*/
        this.ntax = ntax;
    }

    // GETTERS
    public IntegerBinding getNtax() {
        return this.ntax;
    }
}
