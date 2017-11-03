package nexus.characters;

/**
 * Created by Daria on 23.09.2016.
 */
public enum DataTypesChar {

    STANDARD("01",1),
    DNA("atgc", 2),
    RNA("augc", 3),
    PROTEIN("arndcqeghilkmfpstwyvz", 4),
    MICROSAT("", 5),
    UNKNOWN("", 0);

    private final String symbols;
    private final int ID;

    DataTypesChar(String symbols, int id){
        this.symbols=symbols;
        this.ID = id;
    }

    //GETTER
    public String getSymbols(){ return this.symbols; }
    public int getID(){ return this.ID; }

    public int getMaxRepeat(){
        return Character.MAX_VALUE - 256;
    }

    //TODO: IUPAC Ambiguous DNA characters
}
