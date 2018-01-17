package SIM.dependOnFields;

public class Test {
    public static void main(String[] args) {

        System.out.println("Test on fields dependencies");
        System.out.println();

        /*Vector<String> TaxaSet = new Vector<>();
        TaxaSet.add("cat");
        TaxaSet.add("dog");
        TaxaSet.add("mouse");
        t1.setTaxLabels(TaxaSet);*/

        Taxa t = new Taxa();
        Distances d = new Distances();

        //Bidirectional Binding
        t.getNtax().bindBidirectional(d.getNtax());
        t.getChanges().bindBidirectional(d.getChanges());

        // or use low Level Binding???
        /*IntegerBinding binding = new IntegerBinding() {
            {
                super.bind(t.getNtax(),d.getNtax());
            }
            @Override
            protected int computeValue() {
                return t.getNtax().get();
            }
        };*/

        // add stuff
        t.add("dog");
        t.add("cat", "black");
        t.delete("cat", "black");
        d.getFormat().setDiagonal(false);


    }
}
