package SIM.bindingAsField;

import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.Vector;

public class Taxa {

    private SimpleIntegerProperty ntax;
    private ObservableList<String> taxLabels;
    private ObservableList<String> taxInfos;

    private boolean mustDetectLabels;

    IntegerBinding binding;

    public Taxa() {

        mustDetectLabels = false;
        this.ntax = new SimpleIntegerProperty(0);
        binding = new IntegerBinding() {
            @Override
            protected int computeValue() {
                return ntax.getValue();
            }
        };
        this.taxLabels = FXCollections.observableList(new Vector<>());
        this.taxInfos = FXCollections.observableList(new Vector<>());

        this.taxLabels.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                System.out.println("Detected a change in Taxa List! " + c);
                ntax.set(taxLabels.size());
                System.out.println("new ntax = " + getNtax().getValue());
            }
        });
        this.taxInfos.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                System.out.println("Detected a change in Info List! ");
            }
        });
    }

    // functions
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

    void setNtax(int n) {
        this.ntax.set(n);
    }

    // GETTER

    public SimpleIntegerProperty getNtax() {
        return this.ntax;
    }

    public String getLabel(int i) {
        return this.taxLabels.get(i);
    }
}
