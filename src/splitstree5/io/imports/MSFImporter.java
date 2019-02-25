package splitstree5.io.imports;

import jloda.util.*;
import splitstree5.core.algorithms.interfaces.IToCharacters;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.characters.CharactersType;
import splitstree5.core.misc.Taxon;
import splitstree5.io.imports.interfaces.IImportCharacters;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MSFImporter extends CharactersFormat implements IToCharacters, IImportCharacters {

    private CharactersType dataType = CharactersType.Unknown;

    @Override
    public void parse(ProgressListener progressListener, String fileName, TaxaBlock taxaBlock, CharactersBlock dataBlock) throws CanceledException, IOException {

        taxaBlock.clear();

        Map<String, String> taxa2seq = new LinkedHashMap<>();
        boolean charStarted = false;

        try (FileInputIterator it = new FileInputIterator(fileName)){

            progressListener.setMaximum(it.getMaximumProgress());
            progressListener.setProgress(0);
            int linesCounter = 0;

            while (it.hasNext()) {

                linesCounter += 1;
                final String line = it.next();
                final String line_no_spaces = line.replaceAll(" ", "");

                if (line_no_spaces.startsWith("!!NA"))
                    dataType = CharactersType.DNA;
                if (line_no_spaces.startsWith("!!AA"))
                    dataType = CharactersType.Protein;

                if (!charStarted && line.contains("Name:")){
                    StringTokenizer tokens = new StringTokenizer(line);
                    tokens.nextToken();
                    String taxon = tokens.nextToken();

                    if (taxa2seq.keySet().contains(taxon))
                        throw new IOExceptionWithLineNumber("Repeated taxon name", linesCounter);

                    taxaBlock.add(new Taxon(taxon));
                    taxa2seq.put(taxon, "");
                }

                if (line_no_spaces.equals("//")){
                    charStarted = true;
                }

                if(charStarted){
                    String taxon = cutTaxonFromLine(line, taxa2seq.keySet());
                    if (!taxon.equals("")){
                        String chars = line.replaceAll(" ", "");
                        chars = chars.substring(taxon.length());
                        taxa2seq.replace(taxon, taxa2seq.get(taxon)+chars);
                    }
                }

                progressListener.setProgress(it.getProgress());
            }
        }

        String firstKey = (String) taxa2seq.keySet().toArray()[0];
        int nchars = taxa2seq.get(firstKey).length();
        int ntax = taxa2seq.size();

        setCharacters(taxa2seq, ntax, nchars, dataBlock);

    }

    private void setCharacters(Map<String, String> taxa2seq, int ntax, int nchar, CharactersBlock characters)
            throws IOException {

        characters.clear();
        characters.setDimension(ntax, nchar);
        characters.setDataType(this.dataType);
        characters.setMissingCharacter('.'); //todo estimate??

        int labelsCounter = 1;

        for (String label : taxa2seq.keySet()) {
            if (taxa2seq.get(label).length() != nchar)
                throw new IOException("The sequences in the alignment have different lengths! " +
                        "Length of sequence: "+label+" differ from the length of previous sequences :"+nchar);

            for (int j = 1; j <= nchar; j++) {
                char symbol = Character.toLowerCase(taxa2seq.get(label).charAt(j - 1));
                characters.set(labelsCounter, j, symbol);
            }
            labelsCounter++;
        }
    }

    private String cutTaxonFromLine(String line, Set<String> taxaKeys){
        for (String t : taxaKeys) {
            if (line.contains(t))
                return t;
        }
        return "";
    }

    @Override
    public List<String> getExtensions() {
        return Arrays.asList("msf");
    }

    @Override
    public boolean isApplicable(String fileName) throws IOException {
        String line = Basic.getFirstLineFromFile(new File(fileName));
        return line != null &&
                (line.toUpperCase().equals("!!NA_MULTIPLE_ALIGNMENT 1.0")
                || line.toUpperCase().equals("!!AA_MULTIPLE_ALIGNMENT 1.0") );
    }
}
