package SIM.dependOnFields;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.Vector;

public class Taxa {

    private SimpleIntegerProperty ntax;
    private ObservableList<String> taxLabels;
    private ObservableList<String> taxInfos;

    // Save changes as a list to implement REDO-button for many steps
    private StringProperty changes;

    private boolean mustDetectLabels;

    /*** Constructors ***/
    public Taxa() {
        mustDetectLabels = false;
        this.ntax = new SimpleIntegerProperty(0);
        this.changes = new SimpleStringProperty("");
        this.taxLabels = FXCollections.observableList(new Vector<>());
        this.taxLabels.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                System.out.println("Detected a change in Taxa List! " + c);
                changes.setValue(c.toString());
                ntax.set(taxLabels.size());
                System.out.println("new ntax = " + getNtax().getValue());
                printTaxa();
            }
        });

        this.taxInfos = FXCollections.observableList(new Vector<>());
        this.taxInfos.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                System.out.println("Detected a change in Info List! ");
            }
        });
    }

    // CANNOT HAVE THE SAME NAME WITH ARGUMENT
    public Taxa(int nTax) {
        mustDetectLabels = false;
        this.ntax = new SimpleIntegerProperty(nTax);
        this.taxLabels = FXCollections.observableList(new Vector<>());
        this.taxInfos = FXCollections.observableList(new Vector<>());

        this.taxLabels.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                System.out.println("Detected a change in Taxa List! " + c);
                ntax.set(taxLabels.size());
                System.out.println("new ntax = " + getNtax().getValue());
                printTaxa();
            }
        });
        this.taxInfos.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                System.out.println("Detected a change in Info List! ");
            }
        });
    }

    void add(String taxonLabel) {
        System.out.println();
        System.out.println("Added a new Taxon");
        this.taxLabels.add(taxonLabel);
    }

    void add(String taxonLabel, String info) {
        System.out.println();
        System.out.println("Added a new Taxon with Info");
        this.taxLabels.add(taxonLabel);
        this.taxInfos.add(info);

    }

    //new
    void delete(String taxonLabel) {
        System.out.println();
        System.out.println("Deleted a Taxon");
        this.taxLabels.remove(taxonLabel);

    }

    void delete(String taxonLabel, String info) {
        System.out.println();
        System.out.println("Deleted a Taxon with Info");
        this.taxLabels.remove(taxonLabel);
        this.taxInfos.remove(info);

    }

    // printing
    public void printTaxa() {
        System.out.println("LIST OF TAXA:");
        for (String l : this.taxLabels) {
            System.out.println(l);
        }
    }

    //Setters
    void setTaxLabels(Vector<String> labels) {
        for (String l : labels) {
            this.taxLabels.add(l);
        }
    }

    void setTaxInfos(Vector<String> infos) {
        for (String i : infos) {
            this.taxLabels.add(i);
        }
    }

    // GETTER

    public SimpleIntegerProperty getNtax() {
        return this.ntax;
    }

    public String getLabel(int i) {
        return this.taxLabels.get(i);
    }

    public StringProperty getChanges() {
        return this.changes;
    }

}
