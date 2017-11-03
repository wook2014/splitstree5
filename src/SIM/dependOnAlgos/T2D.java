package SIM.dependOnAlgos;

public class T2D  {

    private Taxa taxa;
    private Distances distances;

    public T2D(Taxa taxa, Distances distances){
        this.taxa= taxa;
        this.distances = distances;

        this.taxa.getNtax().bindBidirectional(this.distances.getNtax());
        this.taxa.getChanges().bindBidirectional(this.distances.getChanges());

    }

}
