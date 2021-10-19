/*
 *  CopySketches.java Copyright (C) 2021 Daniel H. Huson
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

package splitstree5.xtra;

import jloda.util.StringUtils;
import splitstree5.dialogs.analyzegenomes.AccessReferenceDatabase;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;


public class CopySketches {
    public static void main(String[] args) throws IOException, SQLException {
        var srcFile = "/Users/huson/data/gtdb/banu2/gtdb_k21_s10000.db";
        var tarFile = "/Users/huson/data/gtdb/banu2/gtdb_rep_k21_s10000.db";

        var tmpFile = "/Users/huson/data/gtdb/banu2/tmp.sql";

        ArrayList<String> taxa;
        try (var tarDB = new AccessReferenceDatabase(tarFile, 1)) {
            System.err.println("Running: \"select taxon_id from mash_sketches;\"");
            taxa = tarDB.executeQueryString("select taxon_id from mash_sketches;", 1);
        }


        try (var srcDB = new AccessReferenceDatabase(srcFile, 1)) {
			var srcQuery = "select taxon_id, mash_sketch from mash_sketches where taxon_id in (" + StringUtils.toString(taxa, ",") + ");";
            System.err.println("Running: " + srcQuery.substring(0, 40) + "...");
            final ResultSet rs = srcDB.getConnection().createStatement().executeQuery(srcQuery);

            System.err.print("Writing file: " + tmpFile + " ");
            try (Writer w = new BufferedWriter(new FileWriter(tmpFile))) {
                var seen = new HashSet<Integer>();
                w.write("PRAGMA foreign_keys=OFF;\n");
                w.write("drop table if exists bloom_filters;\n");
                w.write("BEGIN TRANSACTION;\n");
                w.write("CREATE TABLE mash_sketches (taxon_id INTEGER PRIMARY KEY, mash_sketch TEXT NOT NULL);\n");
                var lines = 0;
                while (rs.next()) {
                    var taxonId = rs.getInt(1);
                    if (seen.contains(taxonId))
                        System.err.println("Taxon not unique: " + taxonId);
                    else
                        seen.add(taxonId);

                    w.write(String.format("INSERT INTO mash_sketches VALUES(%d,'%s');%n", taxonId, rs.getString(2)));
                    lines++;
                    if (lines % 5000 == 0) {
                        System.err.print(". ");
                        System.err.flush();
                    }
                }
                w.write("COMMIT;\n");
                System.err.println("done");

            }
        }
    }
}
