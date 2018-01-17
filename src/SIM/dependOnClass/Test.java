package SIM.dependOnClass;

import java.util.Vector;

public class Test {

    public static void main(String[] args) {

        System.out.println("Testing on class dependencies");
        System.out.println();

        Vector<String> TaxaSet = new Vector<>();
        TaxaSet.add("cat");

        Taxa t = new Taxa();
        Distances d = new Distances();
        Characters c = new Characters();
        t.addObserver(c);
        t.addObserver(d);
        c.addObserver(d);

        t.setTaxLabels(TaxaSet);
        t.setTaxInfos(TaxaSet);
        t.add("bear", "black");
        t.add("dog");
        t.delete("dog");
    }
}
