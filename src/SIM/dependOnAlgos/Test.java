package SIM.dependOnAlgos;

public class Test {

    public static void main(String[] args) {

        System.out.println("Test on algorithms dependencies");
        System.out.println();

        Taxa t = new Taxa();
        Distances d = new Distances();
        Characters c = new Characters();
        /*T2C t2c = new T2C(t,c);
        T2D t2d = new T2D(t,d);
        t.add("dog");
        t.add("cat", "black");
        t.delete("cat", "black");*/


        C2D c2d = new C2D(d, c, "Hamming");
        c2d.setCurrentAlgorithm("Kimura 2P");
        c.setNchar(10);
    }
}
