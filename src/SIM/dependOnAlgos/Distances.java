package SIM.dependOnAlgos;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class Distances {
    private boolean isSet = false;

    private SimpleIntegerProperty ntax;
    private double[][] matrix;
    private double[][] variance;

    private StringProperty changes;

    public Distances() {
        this.ntax = new SimpleIntegerProperty(0);
        this.changes = new SimpleStringProperty("");
        this.matrix = null;
        this.variance = null;

        this.ntax.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.out.println("Ntax in Distances is changed :" + newValue);
                recalculateMatrixAndVariance();
            }
        });

        this.changes.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.equals("gapDist") || newValue.equals("Jukes Cantor") ||
                        newValue.equals("Kimura 2P") || newValue.equals("Hamming")) {
                    System.out.println("Distances : update with new algorithm " + changes.getValue());
                } else {
                    System.out.println("Update Distances according to " + changes.getValue());
                }
            }
        });
    }

    // private functions
    private void recalculateMatrixAndVariance() {
        System.out.println("recalculate the matrix and the variance according to: " + this.changes.getValue());
    }

    //Setters
    public void setChanges(String changes) {
        this.changes.setValue(changes);
    }


    // GETTERS
    public SimpleIntegerProperty getNtax() {
        return this.ntax;
    }

    public StringProperty getChanges() {
        return this.changes;
    }
}
