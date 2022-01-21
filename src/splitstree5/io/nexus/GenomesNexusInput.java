/*
 * GenomesNexusInput.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.io.nexus;

import jloda.util.IOExceptionWithLineNumber;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.data.Genome;
import splitstree5.core.datablocks.GenomesBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * nexus input parser
 * Daniel Huson, 3.2020
 */
public class GenomesNexusInput extends NexusIOBase implements INexusInput<GenomesBlock> {
    public static final String SYNTAX = "BEGIN " + GenomesBlock.BLOCK_NAME + ";\n" +

            "\t[TITLE {title};]\n" +
            "\t[LINK {type} = {title};]\n" +
            "\t[DIMENSIONS NTAX=number-of-taxa;]\n" +
            "\t[FORMAT\n" +
            "\t\t[LABELS={YES|NO}]\n" +
            "\t\t[ACCESSIONS={YES|NO}]\n" +
            "\t\t[MULTIPART={YES|NO}]\n" +
            "\t\t[FILES={YES|NO}]\n" +
            "\t;]\n" +
            "\tMATRIX\n" +
            "\t\t[label] [accession] length {sequence | [number-of-parts] length {sequence|{file://file offset}} ...  length {sequence|{file offset}}},\n" +
            "\t\t...\n" +
            "\t\t[label] [accession] length {sequence | [number-of-parts] length {sequence|{file://file offset}} ...  length {sequence|{file offset}}}\n" +
            "\t;]\n" +
            "END;\n";


    @Override
    public String getSyntax() {
        return SYNTAX;
    }

    /**
     * parse a genomes block
     *
     * @param np
     * @param taxaBlock
     * @param genomesBlock
     * @return taxon names, if found
     * @throws IOException
     */
    @Override
    public List<String> parse(NexusStreamParser np, TaxaBlock taxaBlock, GenomesBlock genomesBlock) throws IOException {
        try {
            genomesBlock.clear();

            final GenomesNexusFormat format = (GenomesNexusFormat) genomesBlock.getFormat();

            np.matchBeginBlock(GenomesBlock.BLOCK_NAME);
            parseTitleAndLink(np);

            if (taxaBlock.getNtax() == 0) {
                np.matchIgnoreCase("dimensions nTax=");
                taxaBlock.setNtax(np.getInt());
                np.matchIgnoreCase(";");
            } else if (np.peekMatchIgnoreCase("dimensions")) {
                np.matchIgnoreCase("dimensions nTax=" + taxaBlock.getNtax() + ";");
            }

            if (np.peekMatchIgnoreCase("FORMAT")) {
                final List<String> tokens = np.getTokensLowerCase("format", ";");

                format.setOptionLabels(np.findIgnoreCase(tokens, "labels=yes", true, format.isOptionLabels()));
                format.setOptionLabels(np.findIgnoreCase(tokens, "labels=no", false, format.isOptionLabels()));

                format.setOptionAccessions(np.findIgnoreCase(tokens, "accessions=yes", true, format.isOptionAccessions()));
                format.setOptionAccessions(np.findIgnoreCase(tokens, "accessions=no", false, format.isOptionAccessions()));

                format.setOptionMultiPart(np.findIgnoreCase(tokens, "multiPart=yes", true, format.isOptionMultiPart()));
                format.setOptionMultiPart(np.findIgnoreCase(tokens, "multiPart=no", false, format.isOptionMultiPart()));

                if (np.findIgnoreCase(tokens, "dataType=dna", true, format.getCharactersType() == GenomesNexusFormat.CharactersType.dna))
                    format.setCharactersType(GenomesNexusFormat.CharactersType.dna);
                if (np.findIgnoreCase(tokens, "dataType=protein", true, format.getCharactersType() == GenomesNexusFormat.CharactersType.protein))
                    format.setCharactersType(GenomesNexusFormat.CharactersType.protein);

                if (tokens.size() != 0)
                    throw new IOExceptionWithLineNumber(np.lineno(), "'" + tokens + "' unexpected in FORMAT");
            }

            final ArrayList<String> taxonNamesFound = new ArrayList<>(taxaBlock.getNtax());

            {
                np.matchIgnoreCase("MATRIX");
                final boolean hasTaxonNames = taxaBlock.size() > 0;
                int taxon = 0;

                while (!np.peekMatchIgnoreCase(";")) {
                    taxon++;
                    final Genome genome = new Genome();
                    if (format.isOptionLabels()) {
                        final String name;
                        if (hasTaxonNames) {
                            name = taxaBlock.getLabel(taxon);
                            np.matchLabelRespectCase(name);
                        } else {
                            name = np.getLabelRespectCase();
                        }
                        taxonNamesFound.add(name);
                        genome.setName(name);
                    } else
                        genome.setName(taxaBlock.getLabel(taxon));
                    if (format.isOptionAccessions()) {
                        genome.setAccession(np.getLabelRespectCase());
                    }

                    if (format.isOptionMultiPart()) {
                        genome.setLength(np.getInt());

                        final int numberOfParts = np.getInt();

                        for (int p = 0; p < numberOfParts; p++) {
                            final Genome.GenomePart part = new Genome.GenomePart();

                            final int partLength = np.getInt();

                            final String word = np.getWordFileNamePunctuation();

                            if (word.startsWith("file://")) {
                                part.setFile(word.replaceFirst("file://", ""), np.getLong(), partLength);
                            } else {
                                // todo: scan for partLength number of letters
                                part.setSequence(word.replaceAll("\\s+", "").getBytes(), partLength);
                            }
                            genome.getParts().add(part);
                        }
                    } else {
                        genome.setLength(np.getInt());
                        final Genome.GenomePart part = new Genome.GenomePart();
                        final String word = np.getWordFileNamePunctuation();

                        if (word.startsWith("file://")) {
                            part.setFile(word.replaceFirst("file://", ""), np.getLong(), genome.getLength());
                        } else {
                            // todo: scan for partLength number of letters
                            part.setSequence(word.replaceAll("\\s+", "").getBytes(), genome.getLength());
                        }
                        genome.getParts().add(part);
                    }
                    genomesBlock.getGenomes().addAll(genome);
                    if (np.peekMatchIgnoreCase(",")) {
                        np.matchIgnoreCase(",");
                    } else
                        break;
                }
                np.matchIgnoreCase(";");
            }
            np.matchEndBlock();
            return taxonNamesFound;
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    /**
     * is the parser at the beginning of a block that this class can parse?
     *
     * @param np
     * @return true, if can parse from here
     */
    public boolean atBeginOfBlock(NexusStreamParser np) {
        return np.peekMatchIgnoreCase("begin " + GenomesBlock.BLOCK_NAME + ";");
    }
}
