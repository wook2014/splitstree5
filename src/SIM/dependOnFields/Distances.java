package SIM.dependOnFields;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class Distances{

    private boolean isSet = false;

    private SimpleIntegerProperty ntax;
    private double[][] matrix;
    private double[][] variance;
    private DFormat format;

    private StringProperty changes;

    public Distances() {
        this.ntax = new SimpleIntegerProperty(0);
        this.changes = new SimpleStringProperty("");
        this.matrix = null;
        this.variance = null;
        this.format = new DFormat();

        this.ntax.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.out.println("Distances changed! new ntax is "+newValue);
                recalculateMatrixAndVariance();
            }
        });

        this.format.addListener(new InvalidationListener() {
            @Override
            public void invalidated(javafx.beans.Observable observable) {
                System.out.println();
                System.out.println("Distances format is changed");
                System.out.println("Send the new format to the GUI");
            }
        });
        //format = new Format();
    }

    // private functions
    private void recalculateMatrixAndVariance(){
        System.out.println("recalculate the matrix and the variance according to: "+this.changes.getValue());
    }


    // GETTERS
    public SimpleIntegerProperty getNtax(){
        return this.ntax;
    }

    public StringProperty getChanges(){
        return this.changes;
    }

    public DFormat getFormat(){
        return this.format;
    }
}
