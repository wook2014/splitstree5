package SIM.dependOnClass;

import nexus.distances.Format;

import java.util.Observable;
import java.util.Observer;

public class Distances extends Observable implements Observer {

    private boolean isSet;

    private int ntax;
    private double[][] matrix;
    private double[][] variance;

    private Format format;

    public Distances() {
        this.ntax = 0;
        this.matrix = new double[0][0];
        this.variance = new double[0][0];
        this.format = new Format();
    }

    @Override
    public void update(Observable o, Object arg) {
        System.out.println("Distances: changes detected: " + arg);
        System.out.println("Distances: Update fields");

        // new values
        this.ntax = 0;
        this.matrix = new double[0][0];
        this.variance = new double[0][0];
        this.format = new Format();
    }
}
