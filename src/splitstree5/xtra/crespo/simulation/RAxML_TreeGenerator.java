/*
 *  RAxML_TreeGenerator.java Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.xtra.crespo.simulation;


import splitstree5.xtra.crespo.util.MyNewickParser;
import splitstree5.xtra.crespo.util.MyNode;
import splitstree5.xtra.crespo.util.MyTree;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class RAxML_TreeGenerator {

    private final static String RAxML_EXE = "/Users/patricioburchard/IdeaProjects/splitstree5/src/splitstree5/core/algorithms/trees2splits/tools/raxmlHPC";


    public ArrayList<MyTree> run(ArrayList<HashMap<MyNode, String>> setOfEvolvedSeqs) throws IOException {

        ArrayList<MyTree> trees = new ArrayList<MyTree>();
        for (HashMap<MyNode, String> MSA : setOfEvolvedSeqs) {

            File inputFile = File.createTempFile("raxmlInput", ".txt");

            // generating input file
            StringBuilder buf = new StringBuilder();
            buf.append(MSA.keySet().size() + "\n");
            buf.append(MSA.values().iterator().next().length() + "\n");
            for (MyNode v : MSA.keySet())
                buf.append(v.getId() + " " + MSA.get(v) + "\n");
            FileWriter fW = new FileWriter(inputFile);
            fW.write(buf.toString());
            fW.close();

            // running RAxML
            String cmd = RAxML_EXE + " -p 100 -T 8 -s " + inputFile.getAbsolutePath() + " -n txt" + " -m GTRCAT";
            executingCommand(cmd);

            // reading output file

            MyTree t = parsingOutputFile(new File("RAxML_result.txt"));

            trees.add(t);


            // deleting files
            inputFile.delete();
            String[] suffices = {"bestTree", "info", "log", "parsimonyTree", "result"};
            for (String s : suffices)
                new File("RAxML_" + s + ".txt").delete();

            //System.out.println("deletion completed!");

        }

        return trees;

    }

    private MyTree parsingOutputFile(File f) throws IOException {

        BufferedReader buf = new BufferedReader(new FileReader(f));
        String l = buf.readLine();


        MyTree t = new MyNewickParser().run(l);
        buf.close();

        return t;

    }

    private int executingCommand(String command) {
        try {

            //System.out.println("OUTPUT>Executing " + command);
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(command);

            // checking error messages
            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");

            // checking error messages
            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

            errorGobbler.start();
            outputGobbler.start();
            //System.out.println("waiting ");
            int exitVal = proc.waitFor();

            System.out.println(exitVal);

            return exitVal;
            // return 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 1;
    }

    class StreamGobbler extends Thread {
        InputStream is;
        String type;

        StreamGobbler(InputStream is, String type) {
            this.is = is;
            this.type = type;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                 /*   if (type.equals("ERROR"))
                        System.out.println(type + ">" + line);
                    if (type.equals("OUTPUT"))
                    System.out.println(type + ">" + line);*/
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

}