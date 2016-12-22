package SIM.dependOnAlgos;


public class T2C {

    private Taxa taxa;
    private Characters characters;

    public T2C(Taxa taxa, Characters characters){
        this.taxa= taxa;
        this.characters = characters;

        this.taxa.getNtax().bindBidirectional(this.characters.getNtax());
        this.taxa.getChanges().bindBidirectional(this.characters.getChanges());
    }
}
