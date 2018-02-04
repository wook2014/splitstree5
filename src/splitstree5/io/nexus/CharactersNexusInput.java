/*
 *  Copyright (C) 2018 Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.io.nexus;

import jloda.util.Basic;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.characters.AmbiguityCodes;
import splitstree5.core.datablocks.characters.CharactersType;
import splitstree5.gui.utils.Alert;
import splitstree5.io.nexus.stateLabeler.MicrostatStateLabeler;
import splitstree5.io.nexus.stateLabeler.ProteinStateLabeler;
import splitstree5.io.nexus.stateLabeler.StandardStateLabeler;
import splitstree5.io.nexus.stateLabeler.StateLabeler;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.*;

/**
 * nexus input parser
 * Daniel Huson, 2.2018
 */
public class CharactersNexusInput implements INexusInput<CharactersBlock, CharactersNexusFormat> {
    public static final String NAME = "CHARACTERS";

    private boolean treatUnknownAsError = false;

    /**
     * is the parser at the beginning of a block that this class can parse?
     *
     * @param np
     * @return true, if can parse from here
     */
    public boolean atBeginOfBlock(NexusStreamParser np) {
        return np.peekMatchIgnoreCase("begin " + NAME + ";");
    }

    public static final String SYNTAX = "BEGIN " + NAME + ";\n" +
            "\t[TITLE title;]\n" +
            "\t[LINK name = title;]\n" +
            "\tDIMENSIONS [NTAX=number-of-taxa] NCHAR=number-of-characters;\n" +
            "\t[PROPERTIES [GAMMASHAPE=shape-parameter] [PINVAR=proportion-invar];]\n" +
            "\t[FORMAT\n" +
            "\t\t[DATATYPE={STANDARD|DNA|RNA|PROTEIN|MICROSAT}]\n" +
            "\t\t[RESPECTCASE]\n" +
            "\t\t[MISSING=symbol]\n" +
            "\t\t[GAP=symbol]\n" +
            "\t\t[MatchChar=symbol]\n" +
            "\t\t[SYMBOLS=\"symbol symbol ...\"]\n" +
            "\t\t[LABELS={NO|LEFT}]\n" +
            "\t\t[TRANSPOSE={NO|YES}]\n" +
            "\t\t[INTERLEAVE={NO|YES}]\n" +
            "\t\t[TOKENS=NO]\n" +
            "\t;]\n" +
            "\t[CHARWEIGHTS wgt_1 wgt_2 ... wgt_nchar;]\n" +
            "\t[CHARSTATELABELS character-number [ character-name ][ /state-name [ state-name... ] ], ...;]\n" +
            "\tMATRIX\n" +
            "\t\tsequence data in specified format\n" +
            "\t;\n" +
            "END;\n";

    @Override
    public String getSyntax() {
        return SYNTAX;
    }

    /**
     * parse a characters block
     *
     * @param np
     * @param taxa
     * @param characters
     * @param format
     * @return taxon names, if found
     * @throws IOException
     */
    @Override
    public List<String> parse(NexusStreamParser np, TaxaBlock taxa, CharactersBlock characters, CharactersNexusFormat format) throws IOException {
        characters.clear();

        if (np.peekMatchIgnoreCase("#nexus"))
            np.matchIgnoreCase("#nexus");

        if (format == null)
            format = new CharactersNexusFormat();

        if (np.peekMatchIgnoreCase("begin " + NAME))
            np.matchBeginBlock(NAME);
        else if (np.peekMatchIgnoreCase("begin Data"))
            np.matchBeginBlock("begin Data");

        UtilitiesNexusIO.readTitleLinks(np, characters);

        if (taxa.getNtax() == 0) {
            np.matchIgnoreCase("dimensions ntax=");
            int ntax = np.getInt(1, Integer.MAX_VALUE);
            np.matchIgnoreCase("nchar=");
            int nchar = np.getInt(1, Integer.MAX_VALUE);
            if (format.isIgnoreMatrix())
                characters.setDimension(ntax, 0);
            else
                characters.setDimension(ntax, nchar);
            np.matchIgnoreCase(";");
        } else {
            np.matchIgnoreCase("dimensions");
            if (np.peekMatchIgnoreCase("ntax"))
                np.matchIgnoreCase("ntax=" + taxa.getNtax());
            np.matchIgnoreCase("nchar=");
            int nchar = np.getInt(1, Integer.MAX_VALUE);
            if (format.isIgnoreMatrix())
                characters.setDimension(taxa.getNtax(), 0);
            else
                characters.setDimension(taxa.getNtax(), nchar);
            np.matchIgnoreCase(";");
        }


        if (np.peekMatchIgnoreCase("PROPERTIES")) {
            final List<String> tokens = np.getTokensLowerCase("properties", ";");
            characters.setGammaParam(np.findIgnoreCase(tokens, "gammaShape=", Float.MAX_VALUE));
            characters.setPInvar(np.findIgnoreCase(tokens, "PINVAR=", Float.MAX_VALUE));
            if (tokens.size() != 0)
                throw new IOException("line " + np.lineno() + ": '" + tokens + "' unexpected in PROPERTIES");
        } else {
            characters.setGammaParam(Float.MAX_VALUE);
            characters.setPInvar(Float.MAX_VALUE);
        }

        if (np.peekMatchIgnoreCase("FORMAT")) {
            final List<String> formatTokens = np.getTokensLowerCase("FORMAT", ";");
            {
                final String dataType = np.findIgnoreCase(formatTokens, "dataType=", Basic.toString(CharactersType.values(), " "), CharactersType.unknown.toString());
                characters.setDataType(CharactersType.valueOfIgnoreCase(dataType));
            }

            // we ignore respect case:
            {
                boolean respectCase = np.findIgnoreCase(formatTokens, "respectcase=yes", true, false);
                respectCase = np.findIgnoreCase(formatTokens, "respectcase=no", false, respectCase);
                respectCase = np.findIgnoreCase(formatTokens, "respectcase", true, respectCase);
                respectCase = np.findIgnoreCase(formatTokens, "no respectcase", false, respectCase);
                if (respectCase)
                    System.err.println("WARNING: Format 'RespectCase' not implemented. All character-states will be converted to lower case");
            }

            characters.setMissingCharacter(Character.toLowerCase(np.findIgnoreCase(formatTokens, "missing=", null, '?')));
            characters.setGapCharacter(Character.toLowerCase(np.findIgnoreCase(formatTokens, "gap=", null, '-')));

            {
                boolean nomatchchar = np.findIgnoreCase(formatTokens, "no matchChar", true, false);
                if (nomatchchar)
                    format.setMatchChar((char) 0);
            }
            format.setMatchChar(np.findIgnoreCase(formatTokens, "matchChar=", null, (char) 0));

            {
                String symbols = np.findIgnoreCase(formatTokens, "symbols=", "\"", "\"", characters.getSymbols());
                if (characters.getDataType() == CharactersType.standard || characters.getDataType() == CharactersType.microsat || characters.getDataType() == CharactersType.unknown) {
                    characters.setSymbols(symbols.replaceAll("\\s", "").toLowerCase());
                }
            }

            {
                boolean labels = np.findIgnoreCase(formatTokens, "labels=no", false, false);
                labels = np.findIgnoreCase(formatTokens, "labels=left", true, labels);
                labels = np.findIgnoreCase(formatTokens, "no labels", false, labels);
                labels = np.findIgnoreCase(formatTokens, "labels", true, labels);
                format.setLabels(labels);

                if (taxa.getNtax() == 0 && !format.isLabels())
                    throw new IOException("line " + np.lineno() + ": 'no labels' invalid because no taxlabels given in TAXA block");
            }

            {
                boolean transpose = np.findIgnoreCase(formatTokens, "transpose=no", false, false);
                transpose = np.findIgnoreCase(formatTokens, "transpose=yes", true, transpose);
                transpose = np.findIgnoreCase(formatTokens, "no transpose", false, transpose);
                transpose = np.findIgnoreCase(formatTokens, "transpose", true, transpose);
                format.setTranspose(transpose);

                boolean interleave = np.findIgnoreCase(formatTokens, "interleave=no", false, false);
                interleave = np.findIgnoreCase(formatTokens, "interleave=yes", true, interleave);
                interleave = np.findIgnoreCase(formatTokens, "no interleave", false, interleave);
                interleave = np.findIgnoreCase(formatTokens, "interleave", true, interleave);
                format.setInterleave(interleave);

                boolean tokens = np.findIgnoreCase(formatTokens, "tokens=no", false, false);
                tokens = np.findIgnoreCase(formatTokens, "tokens=yes", true, tokens);
                tokens = np.findIgnoreCase(formatTokens, "no tokens", false, tokens);
                tokens = np.findIgnoreCase(formatTokens, "tokens", true, tokens);
                format.setTokens(tokens);

                boolean diploid = np.findIgnoreCase(formatTokens, "diploid=no", false, false);
                diploid = np.findIgnoreCase(formatTokens, "diploid=yes", true, diploid);
                diploid = np.findIgnoreCase(formatTokens, "diploid", true, diploid);
                characters.setDiploid(diploid);
            }

            if (formatTokens.size() != 0)
                throw new IOException("line " + np.lineno() + ": '" + formatTokens + "' unexpected in FORMAT");
        }

        if (np.peekMatchIgnoreCase("CharWeights")) {
            np.matchIgnoreCase("CharWeights");
            double[] charWeights = new double[characters.getNchar() + 1];
            for (int i = 1; i <= characters.getNchar(); i++)
                charWeights[i] = np.getDouble();
            np.matchIgnoreCase(";");
            characters.setCharacterWeights(charWeights);
        } else
            characters.setCharacterWeights(null);
        // adding CharStateLabels

        characters.setCharLabeler(null);
        characters.setStateLabeler(null);
        if (np.peekMatchIgnoreCase("CharStateLabels")) { // todo is false for ferment4-diploid (microsat data)
            np.matchIgnoreCase("CharStateLabels");
            switch (characters.getDataType()) {
                case protein:
                    characters.setStateLabeler(new ProteinStateLabeler());
                    break;
                case microsat:
                    characters.setStateLabeler(new MicrostatStateLabeler());
                    break;
                default:
                case unknown:
                    characters.setStateLabeler(new StandardStateLabeler(characters.getNchar(), characters.getMissingCharacter(), format.getMatchChar(), characters.getGapCharacter()));
                    break;
            }

            characters.setCharLabeler(new HashMap<>());
            readCharStateLabels(np, characters.getCharLabeler(), characters.getStateLabeler());
            np.matchIgnoreCase(";");
        }

        ArrayList<String> taxonNamesFound;
        final TreeSet<Character> unknownStates = new TreeSet<>();
        {
            np.matchIgnoreCase("MATRIX");
            if (!format.isIgnoreMatrix()) {
                if (!format.isTranspose() && !format.isInterleave()) {
                    taxonNamesFound = readMatrix(np, taxa, characters, format, unknownStates);
                } else if (format.isTranspose() && !format.isInterleave()) {
                    taxonNamesFound = readMatrixTransposed(np, taxa, characters, format, unknownStates);
                } else if (!format.isTranspose() && format.isInterleave()) {
                    taxonNamesFound = readMatrixInterleaved(np, taxa, characters, format, unknownStates);
                } else
                    throw new IOException("line " + np.lineno() + ": can't read matrix!");
                np.matchIgnoreCase(";");
            } else
                taxonNamesFound = new ArrayList<>();
        }
        np.matchEndBlock();

        if (unknownStates.size() > 0)  // warn that stuff has been replaced!
        {
            new Alert("Unknown states encountered in matrix:\n" + Basic.toString(unknownStates, " ") + "\n"
                    + "All replaced by the gap-char '" + characters.getGapCharacter() + "'");
        }

        return taxonNamesFound;
    }

    /**
     * read the matrix
     *
     * @param np
     * @param taxa
     * @param characters
     * @param format
     * @param unknownStates
     * @return
     * @throws IOException
     */
    private ArrayList<String> readMatrix(NexusStreamParser np, TaxaBlock taxa, CharactersBlock characters, CharactersNexusFormat format,
                                         Set<Character> unknownStates) throws IOException {
        final boolean checkStates = characters.getDataType() == CharactersType.protein ||
                characters.getDataType() == CharactersType.DNA || characters.getDataType() == CharactersType.RNA;
        final ArrayList<String> taxonNamesFound = new ArrayList<>(characters.getNtax());

        for (int t = 1; t <= characters.getNtax(); t++) {
            if (format.isLabels()) {
                if (taxa.getNtax() > 0) {
                    np.matchLabelRespectCase(taxa.getLabel(t));
                    taxonNamesFound.add(taxa.getLabel(t));
                } else
                    taxonNamesFound.add(np.getLabelRespectCase());
            }

            final String str;
            int length = 0;

            if (format.isTokens()) {
                if (characters.getStateLabeler() == null)
                    characters.setStateLabeler(new StandardStateLabeler(characters.getNchar(), characters.getMissingCharacter(), format.getMatchChar(), characters.getGapCharacter()));
                final List<String> tokenList = new LinkedList<>();
                while (length < characters.getNchar()) {
                    final String word = np.getWordRespectCase();
                    tokenList.add(word);
                    length++;
                }
                System.err.print("StateLabeler is " + characters.getStateLabeler());
                str = characters.getStateLabeler().parseSequence(tokenList, 1, false);
            } else {
                final StringBuilder buf = new StringBuilder();
                while (length < characters.getNchar()) {
                    final String word = np.getWordRespectCase();
                    length += word.length();
                    buf.append(word);
                }
                str = buf.toString().toLowerCase(); // @todo: until we know that respectcase works, fold all characters to lower-case
            }


            if (str.length() != characters.getNchar())
                throw new IOException("line " + np.lineno() + ": wrong number of chars: " + str.length() + ", expected: " + characters.getNchar());

            for (int i = 1; i <= str.length(); i++) {

                //TODo clean this up.
                final char ch = str.charAt(i - 1);

                if (ch == format.getMatchChar()) {
                    if (t == 1)
                        throw new IOException("line " + np.lineno() + " matchchar illegal in first sequence");
                    else
                        characters.set(t, i, characters.get(1, i));
                } else {
                    if (!checkStates || isValidState(characters, format, ch))
                        characters.set(t, i, ch);
                    else if (treatUnknownAsError)
                        throw new IOException("line " + np.lineno() + " invalid character: " + ch);
                    else  // don't know this, replace by gap
                    {
                        characters.set(t, i, characters.getGapCharacter());
                        unknownStates.add(ch);
                    }
                }
            }
        }
        return taxonNamesFound;
    }

    /**
     * read the matrix
     *
     * @param np
     * @param taxa
     * @param characters
     * @param format
     * @param unknownStates
     * @return
     * @throws IOException
     */
    private ArrayList<String> readMatrixTransposed(NexusStreamParser np, TaxaBlock taxa, CharactersBlock characters, CharactersNexusFormat format,
                                                   Set<Character> unknownStates) throws IOException {
        final boolean checkStates = characters.getDataType() == CharactersType.protein ||
                characters.getDataType() == CharactersType.DNA || characters.getDataType() == CharactersType.RNA;
        final ArrayList<String> taxonNamesFound = new ArrayList<>(characters.getNtax());

        if (format.isLabels()) {
            for (int t = 1; t <= characters.getNtax(); t++) {
                if (taxa.getNtax() > 0) {
                    np.matchLabelRespectCase(taxa.getLabel(t));
                    taxonNamesFound.add(taxa.getLabel(t));
                } else
                    taxonNamesFound.add(np.getLabelRespectCase());
            }
        }
        // read the matrix:
        for (int i = 1; i <= characters.getNchar(); i++) {
            int length = 0;

            final String str;
            if (format.isTokens()) {
                if (characters.getStateLabeler() == null)
                    characters.setStateLabeler(new StandardStateLabeler(characters.getNchar(), characters.getMissingCharacter(), format.getMatchChar(), characters.getGapCharacter()));
                final List<String> tokenList = new LinkedList<>();
                while (length < characters.getNtax()) {
                    String tmp = np.getWordRespectCase();
                    tokenList.add(tmp);
                    length++;
                }
                str = characters.getStateLabeler().parseSequence(tokenList, i, true);
            } else {
                final StringBuilder buf = new StringBuilder();
                while (length < characters.getNtax()) {
                    String tmp = np.getWordRespectCase();
                    length += tmp.length();
                    buf.append(tmp);
                }
                str = buf.toString();
            }


            if (str.length() != characters.getNtax())
                throw new IOException("line " + np.lineno() + ": wrong number of chars: " + str.length());
            for (int t = 1; t <= characters.getNtax(); t++) {
                //char ch = str.getRowSubset(t - 1);
                // @todo: until we know that respectcase works, fold all characters to lower-case
                char ch;
                if (!format.isTokens())
                    ch = Character.toLowerCase(str.charAt(t - 1));
                else
                    ch = str.charAt(t - 1);

                if (ch == format.getMatchChar()) {
                    if (t == 1)
                        throw new IOException("line " + np.lineno() + ": matchchar illegal in first col");
                    else
                        characters.set(t, i, characters.get(1, i));
                } else {
                    if (!checkStates || isValidState(characters, format, ch))
                        characters.set(t, i, ch);
                    else if (treatUnknownAsError)
                        throw new IOException("line " + np.lineno() + " invalid character: " + ch);
                    else  // don't know this, replace by gap
                    {
                        characters.set(t, i, characters.getGapCharacter());
                        unknownStates.add(ch);
                    }
                }
            }
        }
        return taxonNamesFound;
    }

    /**
     * read the matrix
     *
     * @param np
     * @param taxa
     * @param characters
     * @param format
     * @param unknownStates
     * @return
     * @throws IOException
     */
    private ArrayList<String> readMatrixInterleaved(NexusStreamParser np, TaxaBlock taxa, CharactersBlock characters, CharactersNexusFormat format,
                                                    Set<Character> unknownStates) throws IOException {
        final boolean checkStates = characters.getDataType() == CharactersType.protein ||
                characters.getDataType() == CharactersType.DNA || characters.getDataType() == CharactersType.RNA;
        final ArrayList<String> taxonNamesFound = new ArrayList<>(characters.getNtax());

        try {
            int c = 0;
            while (c < characters.getNchar()) {
                int linelength = 0;
                for (int t = 1; t <= characters.getNtax(); t++) {
                    if (format.isLabels()) {
                        if (taxa.getNtax() == 0) {
                            taxonNamesFound.add(np.getLabelRespectCase());
                        } else {
                            np.matchLabelRespectCase(taxa.getLabel(t));
                            taxonNamesFound.add(taxa.getLabel(t));
                        }
                        np.setEolIsSignificant(true);
                    } else {
                        np.setEolIsSignificant(true);
                        if (t == 1 && np.nextToken() != StreamTokenizer.TT_EOL) //cosume eol
                            throw new IOException("line " + np.lineno() + ": EOL expected");
                    }

                    final String str;
                    if (format.isTokens()) {
                        if (characters.getStateLabeler() == null)
                            characters.setStateLabeler(new StandardStateLabeler(characters.getNchar(), characters.getMissingCharacter(), format.getMatchChar(), characters.getGapCharacter()));
                        final LinkedList<String> tokenList = new LinkedList<>();
                        while (np.peekNextToken() != StreamTokenizer.TT_EOL && np.peekNextToken() != StreamTokenizer.TT_EOF) {
                            tokenList.add(np.getWordRespectCase());
                        }
                        str = characters.getStateLabeler().parseSequence(tokenList, c + 1, false);
                    } else {
                        final StringBuilder buf = new StringBuilder();
                        while (np.peekNextToken() != StreamTokenizer.TT_EOL && np.peekNextToken() != StreamTokenizer.TT_EOF) {
                            buf.append(np.getWordRespectCase());
                        }
                        str = buf.toString();
                    }

                    np.nextToken(); // consume the eol
                    np.setEolIsSignificant(false);
                    if (t == 1) { // first line in this block
                        linelength = str.length();
                    } else if (linelength != str.length())
                        throw new IOException("line " + np.lineno() + ": wrong number of chars: " + str.length() + " should be: " + linelength);

                    for (int d = 1; d <= linelength; d++) {
                        int i = c + d;
                        if (i > characters.getNchar())
                            throw new IOException("line " + np.lineno() + ": too many chars");

//char ch = str.getRowSubset(d - 1);
// @todo: until we now that respectcase works, fold all characters to lower-case
                        char ch;
                        if (!format.isTokens())
                            ch = Character.toLowerCase(str.charAt(d - 1));
                        else
                            ch = str.charAt(d - 1);

                        if (ch == format.getMatchChar()) {
                            if (t == 1) {
                                throw new IOException("line " + np.lineno() + ": matchchar illegal in first sequence");
                            } else
                                characters.set(t, i, characters.get(1, i));
                        } else {
                            if (!checkStates || isValidState(characters, format, ch))
                                characters.set(t, i, ch);
                            else if (treatUnknownAsError)
                                throw new IOException("line " + np.lineno() + " invalid character: " + ch);
                            else  // don't know this, replace by gap
                            {
                                characters.set(t, i, characters.getGapCharacter());
                                unknownStates.add(ch);
                            }
                        }
                    }
                }
                c += linelength;
            }
        } finally {
            np.setEolIsSignificant(false);
        }
        return taxonNamesFound;
    }

    /**
     * read the character state labels
     *
     * @param np
     * @param charLabeler
     * @param stateLabeler
     * @throws IOException
     */
    private void readCharStateLabels(NexusStreamParser np, Map<Integer, String> charLabeler, StateLabeler stateLabeler) throws IOException {

        while (np.peekNextToken() != (int) ';') {
            int charNumber = np.getInt(); //get the number in front of the label

            // Deal with the fact that it is possible to not have a label for some nubmer.
            if (np.peekNextToken() == ',' || np.peekNextToken() == '/') {
            } else {
                String charLabel = np.getWordRespectCase();   //get the label otherwise
                charLabeler.put(charNumber, charLabel);
            }

            if (np.peekMatchIgnoreCase(",")) {
                np.nextToken(); //Skipping the ',' between labels
            } else if (np.peekMatchIgnoreCase("/")) {
                np.nextToken(); //Skipping the '/' between label and states
                while (np.peekNextToken() != (int) ',' && np.peekNextToken() != (int) ';') {
                    stateLabeler.token2char(charNumber, np.getWordRespectCase());
                }
                if (np.peekNextToken() == (int) ',')
                    np.nextToken(); //Skipping the ',' between labels
            }
        }
    }


    /**
     * Checks if the character is a valid state symbol. Will always return
     * true if the datatype is UNKNOWN.
     *
     * @param ch character to check
     * @return boolean  true if character consistent with the symbol list of the block's datatype
     */
    private boolean isValidState(CharactersBlock characters, CharactersNexusFormat format, char ch) {
        return characters.getDataType() == CharactersType.unknown || ch == characters.getMissingCharacter() || ch == characters.getGapCharacter() || ch == format.getMatchChar()
                || characters.getSymbols().indexOf(ch) >= 0
                || (characters.getDataType() == CharactersType.DNA && AmbiguityCodes.isAmbiguityCode(ch));
    }

}
