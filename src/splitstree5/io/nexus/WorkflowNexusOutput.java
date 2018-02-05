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

import splitstree5.core.Document;
import splitstree5.core.workflow.Workflow;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * write workflow in nexus
 * Daniel Huson, 2.2018
 */
public class WorkflowNexusOutput {
    /**
     * save the workflow in nexus format
     *
     * @param document
     * @throws IOException
     */
    public void save(Document document) throws IOException {
        String fileName = document.getFileName();
        Workflow workflow = document.getWorkflow();
        try (Writer w = new BufferedWriter(new FileWriter(fileName))) {
            write(w, workflow);
        }
        // todo: RecentFilesManager.getInstance().addRecentFile(fileName);
    }

    /**
     * write the whole workflow in nexus format
     *
     * @param w
     * @param workflow
     */
    public void write(Writer w, Workflow workflow) throws IOException {
        System.err.println("Write workflow: not implemented");
    }
}
