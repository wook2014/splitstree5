/*
 *  Copyright (C) 2016 Daniel H. Huson
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

import com.sun.istack.internal.Nullable;
import jloda.util.Alert;
import jloda.util.Basic;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.characters.AmbiguityCodes;
import splitstree5.core.datablocks.characters.CharactersType;
import splitstree5.io.nexus.stateLabeler.MicrosatSL;
import splitstree5.io.nexus.stateLabeler.ProteinSL;
import splitstree5.io.nexus.stateLabeler.StandardUnknownSL;
import splitstree5.io.nexus.stateLabeler.StateLabeler;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.Writer;
import java.util.*;

/**
 * input and output of a distance block in Nexus format
 * Created by huson on 12/28/16.
 */
public class CharactersNexusIO {
    public static final String NAME = "CHARACTERS";

    public static final String SYNTAX =
            "BEGIN CHARACTERS;\n" +
                    "\tDIMENSIONS [NTAX=number-of-taxa] NCHAR=number-of-characters;\n" +
                    "\t[PROPERTIES [GAMMASHAPE=shape-parameter] [PINVAR=proportion-invar];]\n" +
                    "\t[FORMAT\n" +
                    "\t    [DATATYPE={STANDARD|DNA|RNA|PROTEIN|MICROSAT}]\n" +
                    "\t    [RESPECTCASE]\n" +
                    "\t    [MISSING=symbol]\n" +
                    "\t    [GAP=symbol]\n" +
                    "\t    [MatchChar=symbol]\n" +
                    "\t    [SYMBOLS=\"symbol symbol ...\"]\n" +
                    "\t    [LABELS={NO|LEFT}]\n" +
                    "\t    [TRANSPOSE={NO|YES}]\n" +
                    "\t    [INTERLEAVE={NO|YES}]\n" +
                    "\t    [TOKENS=NO]\n" +
                    "\t;]\n" +
                    "\t[CHARWEIGHTS wgt_1 wgt_2 ... wgt_nchar;]\n" +
                    "\t[CHARSTATELABELS character-number [ character-name ][ /state-name [ state-name... ] ], ...;]\n" +
                    "\tMATRIX\n" +
                    "\t    sequence data in specified format\n" +
                    "\t;\n" +
                    "END;\n";

    private static boolean treatUnknownAsError = false;


    /**
     * report the syntax for this block
     *
     * @return syntax string
     */
    public String getSyntax() {
        return SYNTAX;
    }

    /**
     * parse a distances block
     *
     * @param np
     * @param taxaBlock
     * @param charactersBlock
     * @param format
     * @return taxon names found in this block
     * @throws IOException
     */
    public static ArrayList<String> parse(NexusStreamParser np, TaxaBlock taxaBlock, CharactersBlock charactersBlock, @Nullable CharactersNexusFormat format) throws IOException {
        charactersBlock.clear();

        if (format == null)
            format = new CharactersNexusFormat();

        np.matchBeginBlock(NAME);

        if (taxaBlock.getNtax() == 0) {
            np.matchIgnoreCase("dimensions ntax=");
            int ntax = np.getInt(1, Integer.MAX_VALUE);
            np.matchIgnoreCase("nchar=");
            int nchar = np.getInt(1, Integer.MAX_VALUE);
            charactersBlock.setDimension(ntax, nchar);
            np.matchIgnoreCase(";");
        } else {
            np.matchIgnoreCase("dimensions ntax=" + taxaBlock.getNtax());
            np.matchIgnoreCase("nchar=");
            int nchar = np.getInt(1, Integer.MAX_VALUE);
            charactersBlock.setDimension(taxaBlock.getNtax(), nchar);
            np.matchIgnoreCase(";");
        }


        if (np.peekMatchIgnoreCase("PROPERTIES")) {
            final List<String> tokens = np.getTokensLowerCase("properties", ";");
            charactersBlock.setGammaParam(np.findIgnoreCase(tokens, "gammaShape=", Float.MAX_VALUE));
            charactersBlock.setPInvar(np.findIgnoreCase(tokens, "PINVAR=", Float.MAX_VALUE));
            if (tokens.size() != 0)
                throw new IOException("line " + np.lineno() + ": '" + tokens + "' unexpected in PROPERTIES");
        } else {
            charactersBlock.setGammaParam(Float.MAX_VALUE);
            charactersBlock.setPInvar(Float.MAX_VALUE);
        }

        if (np.peekMatchIgnoreCase("FORMAT")) {
            final List<String> formatTokens = np.getTokensLowerCase("format", ";");
            {
                final String dataType = np.findIgnoreCase(formatTokens, "dataType=", Basic.toString(CharactersType.values(), " "), CharactersType.unknown.toString());
                charactersBlock.setDataType(CharactersType.valueOfIgnoreCase(dataType));
            }

            // we ignore respect case:
            {
                boolean respectCase = np.findIgnoreCase(formatTokens, "respectcase=yes", true, false);
                respectCase = np.findIgnoreCase(formatTokens, "respectcase=no", false, respectCase);
                respectCase = np.findIgnoreCase(formatTokens, "respectcase", true, respectCase);
                respectCase = np.findIgnoreCase(formatTokens, "no respectcase", false, respectCase);
                if (respectCase)
                    System.err.println("WARNING: Format 'RespectCase' not implemented."
                            + " All character-states will be converted to lower case");
            }

            charactersBlock.setMissingCharacter(Character.toLowerCase(np.findIgnoreCase(formatTokens, "missing=", null, '?')));
            charactersBlock.setGapCharacter(Character.toLowerCase(np.findIgnoreCase(formatTokens, "gap=", null, '-')));


            {
                boolean nomatchchar = np.findIgnoreCase(formatTokens, "no matchChar", true, false);
                if (nomatchchar)
                    format.setMatchChar((char) 0);
            }
            format.setMatchChar(np.findIgnoreCase(formatTokens, "matchChar=", null, (char) 0));

            {
                String symbols = np.findIgnoreCase(formatTokens, "symbols=", "\"", "\"", charactersBlock.getLetters());
                charactersBlock.setLettersForStandardOrMicrosatData(symbols);
            }

            {
                boolean labels = np.findIgnoreCase(formatTokens, "labels=no", false, false);
                labels = np.findIgnoreCase(formatTokens, "labels=left", true, labels);
                labels = np.findIgnoreCase(formatTokens, "no labels", false, labels);
                labels = np.findIgnoreCase(formatTokens, "labels", true, labels);
                format.setLabels(labels);

                if (taxaBlock.getNtax() == 0 && !format.isLabels())
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
                charactersBlock.setDiploid(diploid);
            }

            if (formatTokens.size() != 0)
                throw new IOException("line " + np.lineno() + ": '" + formatTokens + "' unexpected in FORMAT");
        }

        if (np.peekMatchIgnoreCase("CharWeights")) {
            np.matchIgnoreCase("CharWeights");
            double[] charWeights = new double[charactersBlock.getNchar() + 1];
            for (int i = 1; i <= charactersBlock.getNchar(); i++)
                charWeights[i] = np.getDouble();
            np.matchIgnoreCase(";");
            charactersBlock.setCharacterWeights(charWeights);
        } else
            charactersBlock.setCharacterWeights(null);
        // adding CharStateLabels

        final StateLabeler stateLabeler;
        if (np.peekMatchIgnoreCase("CharStateLabels")) {
            np.matchIgnoreCase("CharStateLabels");
            switch (charactersBlock.getDataType()) {
                case protein:
                    stateLabeler = new ProteinSL();
                    break;
                case microsat:
                    stateLabeler = new MicrosatSL();
                    break;
                default:
                case unknown:
                    stateLabeler = new StandardUnknownSL(charactersBlock.getNchar(), charactersBlock.getMissingCharacter(), format.getMatchChar(), charactersBlock.getGapCharacter());
                    break;
            }

            final Hashtable<Integer, String> charLabeler = new Hashtable<>();
            readCharStateLabels(np, charLabeler, stateLabeler);
            np.matchIgnoreCase(";");
        } else
            stateLabeler = null;

        ArrayList<String> taxonNamesFound;
        final TreeSet<Character> unknownStates = new TreeSet<>();
        {
            np.matchIgnoreCase("MATRIX");
            if (!format.isTranspose() && !format.isInterleave()) {
                taxonNamesFound = readMatrix(np, taxaBlock, charactersBlock, format, stateLabeler, unknownStates);
            } else if (format.isTranspose() && !format.isInterleave()) {
                taxonNamesFound = readMatrixTransposed(np, taxaBlock, charactersBlock, format, stateLabeler, unknownStates);
            } else if (!format.isTranspose() && format.isInterleave()) {
                taxonNamesFound = readMatrixInterleaved(np, taxaBlock, charactersBlock, format, stateLabeler, unknownStates);
            } else
                throw new IOException("line " + np.lineno() + ": can't read matrix!");
            np.matchIgnoreCase(";");
        }
        np.matchEndBlock();

        if (unknownStates.size() > 0)  // warn that stuff has been replaced!
        {
            new Alert("Unknown states encountered in matrix:\n" + Basic.toString(unknownStates, " ") + "\n"
                    + "All replaced by the gap-char '" + charactersBlock.getGapCharacter() + "'");
        }

        return taxonNamesFound;
    }

    /**
     * read the matrix
     *
     * @param np
     * @param taxa
     * @param charactersBlock
     * @param format
     * @param stateLabeler
     * @param unknownStates
     * @return
     * @throws IOException
     */
    private static ArrayList<String> readMatrix(NexusStreamParser np, TaxaBlock taxa, CharactersBlock charactersBlock, CharactersNexusFormat format,
                                                StateLabeler stateLabeler, Set<Character> unknownStates) throws IOException {
        final boolean checkStates = charactersBlock.getDataType() == CharactersType.protein ||
                charactersBlock.getDataType() == CharactersType.DNA || charactersBlock.getDataType() == CharactersType.RNA;
        final ArrayList<String> taxonNamesFound = new ArrayList<>(charactersBlock.getNtax());

        for (int t = 1; t <= charactersBlock.getNtax(); t++) {
            if (format.isLabels()) {
                if (taxa.getNtax() > 0) {
                    np.matchLabelRespectCase(taxa.getLabel(t));
                    taxonNamesFound.add(taxa.getLabel(t));
                } else
                    taxonNamesFound.add(np.getLabelRespectCase());
            }

            String str = "";
            int length = 0;

            final List<String> tokenList = (format.isTokens() ? new LinkedList<>() : null);

            while (length < charactersBlock.getNchar()) {
                final String word = np.getWordRespectCase();
                if (tokenList != null) {
                    tokenList.add(word);
                    length++;
                } else {
                    length += word.length();
                    str += word;
                }
            }
            if (tokenList != null) {
                str = stateLabeler.parseSequence(tokenList, 1, false);
            }


            if (str.length() != charactersBlock.getNchar())
                throw new IOException("line " + np.lineno() + ": wrong number of chars: " + str.length());

            for (int i = 1; i <= str.length(); i++) {
                // @todo: until we know that respectcase works, fold all characters to lower-case
                //TODo clean this up.
                char ch;
                if (tokenList == null)
                    ch = Character.toLowerCase(str.charAt(i - 1));
                else
                    ch = str.charAt(i - 1);

                if (ch == format.getMatchChar()) {
                    if (t == 1)
                        throw new IOException("line " + np.lineno() + " matchchar illegal in first sequence");
                    else
                        charactersBlock.set(t, i, charactersBlock.get(1, i));
                } else {
                    if (!checkStates || isValidState(charactersBlock, format, ch))
                        charactersBlock.set(t, i, ch);
                    else if (treatUnknownAsError)
                        throw new IOException("line " + np.lineno() + " invalid character: " + ch);
                    else  // don't know this, replace by gap
                    {
                        charactersBlock.set(t, i, charactersBlock.getGapCharacter());
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
     * @param charactersBlock
     * @param format
     * @param stateLabeler
     * @param unknownStates
     * @return
     * @throws IOException
     */
    private static ArrayList<String> readMatrixTransposed(NexusStreamParser np, TaxaBlock taxa, CharactersBlock charactersBlock, CharactersNexusFormat format,
                                                          StateLabeler stateLabeler, Set<Character> unknownStates) throws IOException {
        final boolean checkStates = charactersBlock.getDataType() == CharactersType.protein ||
                charactersBlock.getDataType() == CharactersType.DNA || charactersBlock.getDataType() == CharactersType.RNA;
        final ArrayList<String> taxonNamesFound = new ArrayList<>(charactersBlock.getNtax());

        if (format.isLabels()) {
            for (int t = 1; t <= charactersBlock.getNtax(); t++) {
                if (taxa.getNtax() > 0) {
                    np.matchLabelRespectCase(taxa.getLabel(t));
                    taxonNamesFound.add(taxa.getLabel(t));
                } else
                    taxonNamesFound.add(np.getLabelRespectCase());
            }
        }
        // read the matrix:
        for (int i = 1; i <= charactersBlock.getNchar(); i++) {
            String str = "";
            int length = 0;
            List<String> tokenList = null;
            if (format.isTokens())
                tokenList = new LinkedList<>();

            while (length < charactersBlock.getNtax()) {
                String tmp = np.getWordRespectCase();
                if (tokenList != null) {
                    tokenList.add(tmp);
                    length++;
                } else {
                    length += tmp.length();
                    str += tmp;
                }
            }

            if (tokenList != null) {
                str = stateLabeler.parseSequence(tokenList, i, true);
            }

            if (str.length() != charactersBlock.getNtax())
                throw new IOException("line " + np.lineno() +
                        ": wrong number of chars: " + str.length());
            for (int t = 1; t <= charactersBlock.getNtax(); t++) {
                //char ch = str.getRowSubset(t - 1);
                // @todo: until we now that respectcase works, fold all characters to lower-case
                char ch;
                if (tokenList == null)
                    ch = Character.toLowerCase(str.charAt(t - 1));
                else
                    ch = str.charAt(t - 1);

                if (ch == format.getMatchChar()) {
                    if (i == 1)
                        throw new IOException("line " + np.lineno() + ": matchchar illegal in first line");
                    else
                        charactersBlock.set(t, i, charactersBlock.get(t, 1));
                } else {
                    if (!checkStates || isValidState(charactersBlock, format, ch))
                        charactersBlock.set(t, i, ch);
                    else if (treatUnknownAsError)
                        throw new IOException("line " + np.lineno() + " invalid character: " + ch);
                    else  // don't know this, replace by gap
                    {
                        charactersBlock.set(t, i, charactersBlock.getGapCharacter());
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
     * @param charactersBlock
     * @param format
     * @param stateLabeler
     * @param unknownStates
     * @return
     * @throws IOException
     */
    private static ArrayList<String> readMatrixInterleaved(NexusStreamParser np, TaxaBlock taxa, CharactersBlock charactersBlock, CharactersNexusFormat format,
                                                           StateLabeler stateLabeler, Set<Character> unknownStates) throws IOException {
        final boolean checkStates = charactersBlock.getDataType() == CharactersType.protein ||
                charactersBlock.getDataType() == CharactersType.DNA || charactersBlock.getDataType() == CharactersType.RNA;
        final ArrayList<String> taxonNamesFound = new ArrayList<>(charactersBlock.getNtax());

        try {
            int c = 0;
            while (c < charactersBlock.getNchar()) {
                int linelength = 0;
                for (int t = 1; t <= charactersBlock.getNtax(); t++) {
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

                    String str = "";
                    LinkedList<String> tokenList;
                    if (format.isTokens()) {
                        tokenList = new LinkedList<>();
                        while (np.peekNextToken() != StreamTokenizer.TT_EOL && np.peekNextToken() != StreamTokenizer.TT_EOF) {
                            tokenList.add(np.getWordRespectCase());
                        }
                        str = stateLabeler.parseSequence(tokenList, c + 1, false);
                    } else {
                        while (np.peekNextToken() != StreamTokenizer.TT_EOL && np.peekNextToken() != StreamTokenizer.TT_EOF) {
                            str += np.getWordRespectCase();
                        }
                    }
                    np.nextToken(); // consume the eol
                    np.setEolIsSignificant(false);
                    if (t == 1) { // first line in this block
                        linelength = str.length();
                    } else if (linelength != str.length())
                        throw new IOException("line " + np.lineno() +
                                ": wrong number of chars: " + str.length() + " should be: " + linelength);

                    for (int d = 1; d <= linelength; d++) {
                        int i = c + d;
                        if (i > charactersBlock.getNchar())
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
                                charactersBlock.set(t, i, charactersBlock.get(1, i));
                        } else {
                            if (!checkStates || isValidState(charactersBlock, format, ch))
                                charactersBlock.set(t, i, ch);
                            else if (treatUnknownAsError)
                                throw new IOException("line " + np.lineno() + " invalid character: " + ch);
                            else  // don't know this, replace by gap
                            {
                                charactersBlock.set(t, i, charactersBlock.getGapCharacter());
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
    private static void readCharStateLabels(NexusStreamParser np, Hashtable<Integer, String> charLabeler, StateLabeler stateLabeler) throws IOException {

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
    private static boolean isValidState(CharactersBlock charactersBlock, CharactersNexusFormat format, char ch) {
        if (charactersBlock.getDataType() == CharactersType.unknown)
            return true;
        if (ch == charactersBlock.getMissingCharacter() || ch == charactersBlock.getGapCharacter() || ch == format.getMatchChar())
            return true;
        if (charactersBlock.getLetters().indexOf(ch) >= 0)
            return true;
        return charactersBlock.getDataType() == CharactersType.DNA && AmbiguityCodes.getInstance().getDNAForCode(ch) != null;
    }


    /**
     * write a block in nexus format
     *
     * @param w
     * @param taxaBlock
     * @param charactersBlock
     * @param charactersNexusFormat - if null
     * @throws IOException
     */
    public static void write(Writer w, TaxaBlock taxaBlock, CharactersBlock charactersBlock, @Nullable CharactersNexusFormat charactersNexusFormat) throws IOException {
        if (charactersNexusFormat == null)
            charactersNexusFormat = new CharactersNexusFormat();

        w.write("\nBEGIN " + NAME + ";\n");
        w.write("DIMENSIONS ntax=" + charactersBlock.getNtax() + " nchar=" + charactersBlock.getNchar() + ";\n");
        w.write("FORMAT");

        // write matrix:
        {
            w.write("MATRIX\n");

            w.write(";\n");
        }

        w.write("END; [" + NAME + "]\n");
    }
}
