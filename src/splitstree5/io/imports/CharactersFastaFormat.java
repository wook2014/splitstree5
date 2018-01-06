package splitstree5.io.imports;

import splitstree5.core.datablocks.characters.CharactersType;

/**
 * Created by Daria on 02.07.2017.
 */
public enum CharactersFastaFormat {

    //ncbi,
    gb(CharactersType.DNA),  // GenBank
    emb(CharactersType.DNA), // EMBL Data Library
    dbj(CharactersType.DNA), // DDBJ, DNA Database of Japan
    pir(CharactersType.protein), // NBRF PIR todo one commentar line after ">"
    prf(CharactersType.protein), // Protein Research Foundation
    sp(CharactersType.protein),  // SWISS-PROT
    pdb(CharactersType.protein), // Brookhaven Protein Data Bank
    pat(CharactersType.unknown), // Patents
    /*bbs(CharactersType.DNA),
    gnl(CharactersType.DNA),
    ref(CharactersType.DNA),
    lcl(CharactersType.DNA),*/
    unknown(CharactersType.unknown);


    private final CharactersType dataType;

    CharactersFastaFormat(CharactersType dataType) {
        this.dataType = dataType;
    }

    public CharactersType getDataType() {
        return this.dataType;
    }

    public static CharactersFastaFormat findID(String infoLine) {
        return CharactersFastaFormat.dbj;
    }
}
