package SIM.dependOnAlgos;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import nexus.characters.Format;
import nexus.characters.Properties;

public class Characters {

    private SimpleIntegerProperty ntax;
    private int nchar;
    private int nactive;
    private char[][] matrix;
    private boolean[] mask;
    private double[] charWeights;

    private StringProperty changes;

    // TODO: ObjectProperty classes analog to DFormat
    private Format fmt;
    private Properties properties;

    public Characters(){
        this.ntax = new SimpleIntegerProperty(0);
        this.nchar = 0;
        this.nactive = 0;
        this.matrix = null;
        this.mask = null;
        this.charWeights = null;
        this.changes = new SimpleStringProperty("");

        this.ntax.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.out.println("Ntax in Characters class is changed: "+newValue);
            }
        });
        this.changes.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println("Update Characters according to "+changes.getValue());
            }
        });

    }
    //SETTERS
    public void setNchar(int nchar){
        this.nchar=nchar;
        this.changes.setValue("Changed number of characters to "+nchar);
    }
    //GETTERS
    public SimpleIntegerProperty getNtax(){
        return this.ntax;
    }
    public StringProperty getChanges(){
        return this.changes;
    }

}
