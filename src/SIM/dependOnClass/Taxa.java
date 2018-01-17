package SIM.dependOnClass;

import java.util.Observable;
import java.util.Vector;

public class Taxa extends Observable {

    private int ntax;
    private Vector<String> taxLabels;
    private Vector<String> taxInfos;
    private boolean mustDetectLabels;

    public Taxa() {
        mustDetectLabels = false;
        this.ntax = 0;
        this.taxLabels = new Vector<>();
        this.taxInfos = new Vector<>();
    }

    public Taxa(int ntax) {
        mustDetectLabels = false;
        this.ntax = ntax;
        this.taxLabels = new Vector<>();
        this.taxInfos = new Vector<>();
    }

    // SETTERS
    public void setNtax(int ntax) {
        System.out.println();
        this.ntax = ntax;
        setChanged();
        notifyObservers("set ntax in Taxa");
    }

    public void setTaxLabels(Vector<String> labels) {
        System.out.println();
        System.out.println("Taxa : set labels");
        this.taxLabels = labels;
        this.ntax = this.taxLabels.size();
        setChanged();
        notifyObservers("set labels in Taxa");
    }

    public void setTaxInfos(Vector<String> infos) {
        System.out.println();
        System.out.println("Taxa : set info");
        this.taxInfos = infos;
        setChanged();
        notifyObservers("set infos in Taxa");
    }

    public void add(String taxonLabel) {
        System.out.println();
        this.taxLabels.add(taxonLabel);
        this.ntax++;
        System.out.println("Taxa : Added a new Taxon");
        setChanged();
        notifyObservers("Added a new Taxon at position " + (this.ntax - 1));
    }

    public void add(String taxonLabel, String info) {
        System.out.println();
        this.taxLabels.add(taxonLabel);
        this.taxInfos.add(info);
        this.ntax++;
        System.out.println("Taxa : Added a new Taxon with info");
        setChanged();
        notifyObservers("Added a new Taxon with info at position " + (this.ntax - 1));
    }

    //new, not in splitstree 4
    public void delete(String taxonLabel) {
        System.out.println();
        this.taxLabels.remove(taxonLabel);
        this.ntax--;
        System.out.println("Taxa : Deleted a Taxon");
        setChanged();
        notifyObservers("Deleted a Taxon at position " + (this.ntax - 1));
    }

    public void delete(String taxonLabel, String info) {
        System.out.println();
        this.taxLabels.remove(taxonLabel);
        this.taxInfos.remove(info);
        this.ntax--;
        System.out.println("Taxa : Deleted a Taxon with info");
        setChanged();
        notifyObservers("Deleted a Taxon with info at position " + (this.ntax - 1));
    }
}
