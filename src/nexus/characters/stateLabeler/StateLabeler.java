package nexus.characters.stateLabeler;

import splitstree4.core.SplitsException;

import java.util.List;

/**
 * Created by Daria on 30.10.2016.
 */
public interface StateLabeler {

    int OFFSET = 256; //Offset for chars used to store microsattelite alleles (to avoid conflicts)

    /**
     * Takes a token and site. If the token has appeared at that site, returns corresponding char.
     * Otherwise, adds token to the map and returns a newly assigned char.
     *
     * @param site  site in the characters block
     * @param token name os token
     * @return char used to encode that token
     * @throws SplitsException if there are too many allels at a site.
     */
    char token2char(int site, String token) throws SplitsException;

    /**
     * Returns token associated to a given char value at a particular site, or null if
     * there is none.
     *
     * @param site number of the site
     * @param ch   char
     * @return token name, or null if ch not stored for this site.
     */
    String char2token(int site, char ch);

    /**
     * Takes a list of tokens and converts them into a string of associated char values.
     * It uses the token maps stored at each site; hence the need to know which site we
     * are start at, and if we are reading a transposed matrix or not.
     *
     * @param tokens     list of tokens
     * @param firstSite  site that first token is read for.
     * @param transposed true if the tokens all come from the same character/site
     * @return String of encoded chars.
     * @throws SplitsException if there are too many allels at a site.
     */
    String parseSequence(List<String> tokens, int firstSite, boolean transposed) throws SplitsException;

    // TODO: functions between apply only in standard/unknown ?
    //todo: deleted removeMaskedSites function (never used)

    /**
     * Return the length of the longest token.
     *
     * @return int the longest token
     */
    int getMaximumLabelLength();

    String getSymbolsUsed();
}
