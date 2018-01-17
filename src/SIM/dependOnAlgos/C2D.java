package SIM.dependOnAlgos;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class C2D {

    private Distances distances;
    private Characters characters;
    private SimpleStringProperty currentAlgorithm;

    public C2D(Distances distances, Characters characters, String currentAlgorithm) {
        this.distances = distances;
        this.characters = characters;
        this.distances.getChanges().bindBidirectional(this.characters.getChanges());

        this.currentAlgorithm = new SimpleStringProperty(currentAlgorithm);
        this.currentAlgorithm.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                switch (newValue) {
                    case "gapDist":
                        gapDist();
                        break;
                    case "Jukes Cantor":
                        JukesCantor();
                        break;
                    case "Kimura 2P":
                        kimura2P();
                        break;
                    case "Hamming":
                        hamming();
                        break;
                    default:
                        System.out.println("Invalid algorithm");
                        break;
                }
            }
        });
    }

    public void gapDist() {
        System.out.println("recalculate distances using gapDist");
        this.distances.setChanges(currentAlgorithm.getValue());
    }

    public void JukesCantor() {
        System.out.println("recalculate distances using Jukes Cantor");
        this.distances.setChanges(currentAlgorithm.getValue());
    }

    public void kimura2P() {
        System.out.println("recalculate distances using Kimura 2P");
        this.distances.setChanges(currentAlgorithm.getValue());
    }

    public void hamming() {
        System.out.println("recalculate distances using Hamming");
        this.distances.setChanges(currentAlgorithm.getValue());
    }

    // SETTERS
    public void setCurrentAlgorithm(String currentAlgorithm) {
        this.currentAlgorithm.setValue(currentAlgorithm);
    }

}
