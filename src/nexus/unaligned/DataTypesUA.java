package nexus.unaligned;

/**
 * Created by Daria on 19.09.2016.
 */
public enum DataTypesUA {

    STANDARD("01"),
    DNA("atgc"),
    RNA("augc"),
    NUCLEOTIDE("atugc"),
    PROTEIN("arndcqeghilkmfpstwyvz");

    private final String symbols;

    DataTypesUA(String symbols) {
        this.symbols = symbols;
    }

    //GETTER
    public String getSymbols() {
        return this.symbols;
    }
}
