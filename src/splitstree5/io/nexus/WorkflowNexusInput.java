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
import splitstree5.core.workflow.Workflow;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * read workflow in nexus format
 * Daniel Huson, 2.2018
 */
public class WorkflowNexusInput {
    /**
     * loads a workflow from a nexus file
     *
     * @param fileName
     * @param workflow
     * @throws IOException
     */
    public void load(String fileName, Workflow workflow) throws IOException {
        workflow.clear();
        try (NexusStreamParser np = new NexusStreamParser(new FileReader(fileName))) {
            parse(np, workflow);
        }

    }


    /**
     * parse a whole workflow in nexus format
     *
     * @param np
     * @param workflow
     */
    public void parse(NexusStreamParser np, Workflow workflow) throws IOException {
        System.err.println("Parse workflow: not implemented");
    }

    /**
     * does this look like a workflow file?
     *
     * @param fileName
     * @return true, if is workflow file
     */
    public boolean isWorkflowFile(String fileName) {
        final String aLine = Basic.getFirstLineFromFile(new File(fileName));
        return aLine != null && aLine.toLowerCase().endsWith("[workflow]");
    }
}
