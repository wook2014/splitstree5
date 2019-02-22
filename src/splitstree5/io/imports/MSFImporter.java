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
    @Override
    public void parse(ProgressListener progressListener, String fileName, TaxaBlock taxaBlock, CharactersBlock dataBlock) throws CanceledException, IOException {

        taxaBlock.clear();
        dataBlock.clear();

        Map<String, String> taxa2seq = new LinkedHashMap<>();
        boolean charStarted = false;

        try (FileInputIterator it = new FileInputIterator(fileName)){

            progressListener.setMaximum(it.getMaximumProgress());
            progressListener.setProgress(0);

            while (it.hasNext()) {
                final String line = it.next();

                if (!charStarted && line.contains("Name:")){
                    StringTokenizer tokens = new StringTokenizer(line);
                    tokens.nextToken();
                    String taxon = tokens.nextToken();

                    taxaBlock.add(new Taxon(taxon));
                    taxa2seq.put(taxon, "");
                    //System.err.println(taxon);
                }

                if (line.replaceAll(" ", "").equals("//")){
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

        // todo bring together 2 loops?, use setCharacters function
        for (String t : taxa2seq.keySet()){
            if (taxa2seq.get(t).length() != nchars)
                throw new IOException("The sequences in the alignment have different lengths!" +
                        "Length of sequence: "+t+" differ from previous sequences.");
        }

        // set characters
        dataBlock.setDimension(ntax, nchars);
        String[] seqs = taxa2seq.values().toArray(new String[taxa2seq.size()]);
        for (int i = 1; i <= ntax; i++){
            for (int j = 1; j <= nchars; j++){
                dataBlock.set(i, j, seqs[i-1].charAt(j-1));
            }
        }
        dataBlock.setDataType(CharactersType.DNA); //todo estimate!
        dataBlock.setMissingCharacter('.'); //todo estimate??

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
        return line != null && line.toUpperCase().equals("!!NA_MULTIPLE_ALIGNMENT 1.0");
    }
}
