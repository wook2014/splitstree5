/*
 * DatabaseAccessGTDB.java Copyright (C) 2020. Daniel H. Huson
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
 *
 */

package splitstree5.dialogs.importgenomes;

import javafx.collections.ObservableList;
import jloda.fx.window.NotificationManager;
import jloda.kmers.bloomfilter.BloomFilter;
import jloda.kmers.mash.MashDistance;
import jloda.kmers.mash.MashSketch;
import jloda.thirdparty.HexUtils;
import jloda.util.*;
import org.sqlite.SQLiteConfig;
import splitstree5.dialogs.UrlUtilities;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * provides access to the st5db database representing a reference database
 * Daniel Huson, 8.2020
 */
public class AccessReferenceDatabase implements Closeable {

    private final Connection connection;
    private final File dbFile;

    /**
     * open the database
     * Schema:
     * CREATE TABLE info (key TEXT PRIMARY KEY, value TEXT NOT NULL);
     * CREATE TABLE taxa (taxon_id INTEGER PRIMARY KEY, taxon_name TEXT NOT NULL, taxon_display_name TEXT) WITHOUT ROWID;
     * CREATE TABLE genomes (taxon_id INTEGER PRIMARY KEY, genome_accession TEXT NOT NULL, genome_size INTEGER, fasta_url TEXT) WITHOUT ROWID;
     * CREATE TABLE mash_sketches (taxon_id INTEGER PRIMARY KEY, mash_sketch TEXT NOT NULL) WITHOUT ROWID;
     * CREATE TABLE bloom_filters (taxon_id INTEGER PRIMARY KEY, bloom_filter TEXT NOT NULL) WITHOUT ROWID;
     * CREATE TABLE taxonomy (taxon_id INTEGER PRIMARY KEY, parent_id INTEGER NOT NULL) WITHOUT ROWID;
     *
     * @param dbFile
     * @throws IOException
     * @throws SQLException
     */
    public AccessReferenceDatabase(String dbFile) throws IOException, SQLException {
        this.dbFile = new File(dbFile);

        if (!Basic.fileExistsAndIsNonEmpty(dbFile))
            throw new IOException("File not found or unreadable: " + dbFile);

        final SQLiteConfig config = new SQLiteConfig();
        config.setCacheSize(10000);
        config.setReadOnly(true);

        connection = config.createConnection("jdbc:sqlite:" + dbFile);
    }

    public static boolean isDatabaseFile(String fileName) {
        return Basic.fileExistsAndIsNonEmpty(fileName) && (fileName.endsWith(".db") || fileName.endsWith(".st5db"));
    }

    /**
     * generic method for executing queries with results of type int/Integer
     *
     * @param query the SQL query
     * @return ArrayList containing all query results of the specified type
     * @throws SQLException if something went wrong with the database
     */
    private ArrayList<Integer> executeQueryInt(String query, int index) throws SQLException {
        final ResultSet rs = connection.createStatement().executeQuery(query);
        final ArrayList<Integer> resultlist = new ArrayList<>();
        while (rs.next()) {
            resultlist.add(rs.getInt(index));
        }
        return resultlist;
    }

    /**
     * generic method for executing queries with results of type String
     *
     * @param query the SQL query
     * @param index the index of the result of interest
     * @return ArrayList containing all query results of the specified type
     * @throws SQLException if something went wrong with the database
     */
    private ArrayList<String> executeQueryString(String query, int index) throws SQLException {
        final ResultSet rs = connection.createStatement().executeQuery(query);
        final ArrayList<String> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rs.getString(index));
        }
        return result;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                Basic.caught(e);
            }
        }
    }

    public String getNewick() throws SQLException {
        return executeQueryString("select value from tree where key='newick';", 1).get(0);
    }

    public ArrayList<Pair<Integer, MashSketch>> getMashSketches(Collection<Integer> taxonIds) throws SQLException, IOException {
        final String query = String.format("select taxon_id,mash_sketch from mash_sketches where taxon_id in('%s');", Basic.toString(taxonIds, "','"));

        final ResultSet rs = connection.createStatement().executeQuery(query);
        final int columnCount = rs.getMetaData().getColumnCount();

        final ArrayList<Pair<Integer, MashSketch>> result = new ArrayList<>();
        if (columnCount >= 2) {
            while (rs.next()) {
                final int taxon = rs.getInt(1);
                final String hexString = rs.getString(2);
                final MashSketch mashSketch = MashSketch.parse(HexUtils.decodeHexString(hexString));
                result.add(new Pair<>(taxon, mashSketch));
            }
        }
        return result;
    }

    public ArrayList<Pair<Integer, BloomFilter>> getBloomFilters(Collection<Integer> taxonIds) throws SQLException, IOException {
        final String query = String.format("select taxon_id,bloom_filter from bloom_filters where taxon_id in('%s');", Basic.toString(taxonIds, "','"));

        final ResultSet rs = connection.createStatement().executeQuery(query);

        final ArrayList<Pair<Integer, BloomFilter>> result = new ArrayList<>();
        while (rs.next()) {
            final int taxon = rs.getInt(1);
            final String hexString = rs.getString(2);
            final BloomFilter bloomFilter = BloomFilter.parseBytes(HexUtils.decodeHexString(hexString));
            result.add(new Pair<>(taxon, bloomFilter));
        }
        return result;
    }

    public Map<Integer, String> getNames(Collection<Integer> taxonIds) throws SQLException {
        final String query = String.format("select taxon_id,taxon_name from taxa where taxon_id in('%s');", Basic.toString(taxonIds, "','"));

        final ResultSet rs = connection.createStatement().executeQuery(query);

        final Map<Integer, String> result = new TreeMap<>();
        while (rs.next()) {
            result.put(rs.getInt(1), rs.getString(2));
        }
        return result;
    }

    public ArrayList<Integer> getAllTaxonIds() throws SQLException {
        final String query = "select taxon_id from taxa;";

        final ResultSet rs = connection.createStatement().executeQuery(query);

        final ArrayList<Integer> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rs.getInt(1));
        }
        return result;
    }

    public Map<Integer, String> getFiles(Collection<Integer> taxonIds) throws SQLException, IOException {
        final String query = String.format("select taxon_id,fasta_url from genomes where taxon_id in('%s');", Basic.toString(taxonIds, "','"));

        final ResultSet rs = connection.createStatement().executeQuery(query);

        final Map<Integer, String> result = new HashMap<>();
        while (rs.next()) {
            final int taxon = rs.getInt(1);
            final String fileURL = UrlUtilities.getFileForFtpUrl(rs.getString(2), ".*_cds_.*", ".*fna.gz$");
            result.put(taxon, fileURL);
        }
        return result;
    }

    public int countGenomes() throws SQLException {
        return executeQueryInt("select count(*) from genomes;", 1).get(0);
    }

    public int countBloomFilters() throws SQLException {
        return executeQueryInt("select count(*) from bloom_filters;", 1).get(0);
    }

    public int countMashSketches() throws SQLException {
        return executeQueryInt("select count(*) from mash_sketches;", 1).get(0);
    }


    public int getMashK() throws SQLException {
        return executeQueryInt("select value from info where key='mash_k';", 1).get(0);
    }

    public int getMashS() throws SQLException {
        return executeQueryInt("select value from info where key='mash_s';", 1).get(0);
    }

    public int getMashSeed() throws SQLException {
        return executeQueryInt("select value from info where key='mash_seed';", 1).get(0);
    }

    public int getTaxonomyRoot() throws SQLException {
        return executeQueryInt("select taxon_id from taxonomy where parent_id=0;", 1).get(0);
    }

    public Collection<Integer> getTaxonomyChildren(int parent_id) throws SQLException {
        return new ArrayList<>(executeQueryInt("select taxon_id from taxonomy where parent_id=" + parent_id + ";", 1));
    }

    public Map<String, String> getReferenceFile2Name(ObservableList<Integer> taxonIds, ProgressListener progress) throws SQLException, IOException {
        progress.setSubtask("Acquiring reference files");
        progress.setMaximum(taxonIds.size());
        progress.setProgress(0);

        final Map<Integer, String> id2name = getNames(taxonIds);
        final Map<Integer, String> id2file = getFiles(taxonIds);

        String fileCacheDirectory = ProgramProperties.get("fileCacheDirectory", "");
        if (fileCacheDirectory.equals("")) {
            NotificationManager.showError("File cache directory not set");
        }
        final Map<String, String> result = new HashMap<>();

        for (int taxonId : taxonIds) {
            final File cacheFile = new File(fileCacheDirectory, Basic.getFileNameWithoutPath(id2file.get(taxonId)));
            if (!Basic.fileExistsAndIsNonEmpty(cacheFile)) {
                System.err.println("Caching file: " + id2file.get(taxonId));
                Basic.copy(id2file.get(taxonId), cacheFile.getPath());
            }
            result.put(cacheFile.getPath(), "<c GRAY>" + id2name.get(taxonId) + "</c>");
            progress.incrementProgress();

        }
        return result;
    }

    /**
     * find all genomes that have non-zero Jaccard index
     *
     * @param queries
     * @return taxa in decreasing order of min distance from queries
     * @throws SQLException
     * @throws IOException
     */
    public Collection<Map.Entry<Integer, Double>> findSimilar(ProgressListener progress, int minSharedKMers, Collection<byte[]> queries) throws SQLException, IOException {
        final int mash_k = getMashK();
        final int mash_s = getMashS();
        final int mash_seed = getMashSeed();

        System.err.println("Using mash_k=" + mash_k + ", mash_s=" + mash_s + ", mash_seed=" + mash_seed);

        progress.setTasks("Find references", "Sketching");
        progress.setMaximum(queries.size());
        progress.setProgress(0);
        final List<MashSketch> querySketches = queries.parallelStream()
                .map(q -> MashSketch.compute("", Collections.singletonList(q), true, mash_s, mash_k, mash_seed, false, true, progress))
                .peek(s -> {
                    try {
                        progress.incrementProgress();
                    } catch (CanceledException ignored) {
                    }
                }).collect(Collectors.toList());
        if (progress.isUserCancelled())
            throw new CanceledException();

        final Set<String> kmers = new HashSet<>();
        for (MashSketch sketch : querySketches) {
            for (byte[] kmer : sketch.getKmers()) {
                kmers.add(Basic.toString(kmer));
            }
        }

        progress.setSubtask("Searching");
        progress.setMaximum(countBloomFilters());
        progress.setProgress(0);

        final Map<Integer, Double> id2distance = new HashMap<>();

        final Queue<Integer> queue = new LinkedList<>();
        queue.add(getTaxonomyRoot());
        while (queue.size() > 0) {
            final Collection<Integer> ids = getTaxonomyChildren(queue.poll());
            final ArrayList<Pair<Integer, BloomFilter>> bloomFilters = getBloomFilters(ids);
            for (Pair<Integer, BloomFilter> pair : bloomFilters) {
                final BloomFilter bloomFilter = pair.getSecond();
                if (bloomFilter.countContainedProbably(kmers) >= minSharedKMers) {
                    final int id = pair.getFirst();
                    queue.add(id);
                    //System.err.println("Adding bloom filter for " + id);
                }
                progress.incrementProgress();
            }
            final ArrayList<Pair<Integer, MashSketch>> mashSketches = getMashSketches(ids);

            for (Pair<Integer, MashSketch> pair : mashSketches) {
                final MashSketch mashSketch = pair.getSecond();
                for (MashSketch sketch : querySketches) {
                    if (MashDistance.computeJaccardIndex(mashSketch, sketch, false) >= minSharedKMers) {
                        final int id = pair.getFirst();
                        final double distance = MashDistance.compute(mashSketch, sketch);
                        if (!id2distance.containsKey(id) || id2distance.get(id) > distance)
                            id2distance.put(id, distance);
                        System.err.println("Found similar: " + id + " JI: " + MashDistance.computeJaccardIndex(mashSketch, sketch) + " dist: " + distance);
                    }
                }
                progress.incrementProgress();
            }
        }

        final ArrayList<Map.Entry<Integer, Double>> result = new ArrayList<>(id2distance.entrySet());
        result.sort(Comparator.comparingDouble(Map.Entry::getValue));
        return result;
    }

    public File getDbFile() {
        return dbFile;
    }
}
