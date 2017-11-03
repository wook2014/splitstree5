package SIM.bindingAsField;


public class Test {

    public static void main(String[] args) {

        System.out.println("Test on groups dependencies");
        System.out.println();

        /*Taxa t = new Taxa();
        //Distances d = new Distances(t.getNtax());
        Distances d = new Distances(t.binding);
        System.out.println(d.getNtax().get());

        // add stuff
        t.add("dog");
        System.out.println(d.getNtax().get());
        t.add("cat", "black");
        System.out.println(d.getNtax().get());
        t.delete("cat", "black");

        t.setNtax(12);
        System.out.println(d.getNtax().get());*/


        /*SimpleIntegerProperty n = new SimpleIntegerProperty(2);
        IntegerBinding b = new IntegerBinding() {
            @Override
            protected int computeValue() {
                return n.getValue();
            }
        };
        n.set(5);
        System.out.println(b.get());*/

    }
}
