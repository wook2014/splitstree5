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

package splitstree5.dialogs.analyzegenomes;

import javafx.collections.ObservableList;
import jloda.fx.util.ProgramExecutorService;
import jloda.fx.window.NotificationManager;
import jloda.kmers.bloomfilter.BloomFilter;
import jloda.kmers.mash.MashDistance;
import jloda.kmers.mash.MashSketch;
import jloda.thirdparty.HexUtils;
import jloda.util.*;
import org.sqlite.SQLiteConfig;
import splitstree5.dialogs.UrlUtilities;

import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * provides next to a reference database
 * Daniel Huson, 8.2020
 */
public class AccessReferenceDatabase implements Closeable {
    private final AccessReferenceDatabase[] copiesForSearching;
    private final AtomicInteger which = new AtomicInteger(0);

    private static boolean verbose = false;

    private final Set<Integer> unusableTaxa = new TreeSet<>();

    private final Connection connection;
    private final File dbFile;

    /**
     * open the database
     * Schema:
     * CREATE TABLE mash_sketches (taxon_id INTEGER PRIMARY KEY, mash_sketch TEXT NOT NULL);
     * CREATE TABLE bloom_filters (taxon_id INTEGER PRIMARY KEY, bloom_filter TEXT NOT NULL);
     * CREATE TABLE taxa (taxon_id INTEGER PRIMARY KEY, taxon_name TEXT, taxon_display_name TEXT, parent_id INTEGER REFERENCES taxa(taxon_id));
     * CREATE TABLE info (key TEXT PRIMARY KEY, value TEXT NOT NULL);
     * CREATE TABLE genomes (taxon_id INTEGER PRIMARY KEY, genome_accession TEXT NOT NULL, genome_size INTEGER, fasta_url TEXT);
     */
    public AccessReferenceDatabase(String dbFile, int copies) throws IOException, SQLException {
        this.dbFile = new File(dbFile);
        copiesForSearching = new AccessReferenceDatabase[copies];
        for (int i = 0; i < copiesForSearching.length; i++)
            copiesForSearching[i] = new AccessReferenceDatabase(dbFile, 0);

        if (!Basic.fileExistsAndIsNonEmpty(dbFile))
            throw new IOException("File not found or unreadable: " + dbFile);

        final SQLiteConfig config = new SQLiteConfig();
        config.setCacheSize(10000);
        config.setReadOnly(true);

        connection = config.createConnection("jdbc:sqlite:" + dbFile);

        unusableTaxa.addAll(executeQueryInt("SELECT taxon_id FROM genomes WHERE fasta_url is NULL or fasta_url='';", 1));
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
        for (AccessReferenceDatabase copy : copiesForSearching) {
            copy.close();
        }
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
        final String query = String.format("SELECT taxon_id,bloom_filter FROM bloom_filters WHERE taxon_id IN('%s');", Basic.toString(taxonIds, "','"));

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
        final String query = String.format("SELECT taxon_id,taxon_name FROM taxa WHERE taxon_id IN('%s');", Basic.toString(taxonIds, "','"));

        final ResultSet rs = connection.createStatement().executeQuery(query);

        final Map<Integer, String> result = new TreeMap<>();
        while (rs.next()) {
            result.put(rs.getInt(1), rs.getString(2));
        }
        return result;
    }

    public Map<Integer, List<Integer>> getAncestors(Collection<Integer> taxonIds) throws SQLException {
        final String queryFormat = "\n" +
                "WITH RECURSIVE\n" +
                "parent_of(taxon_id, parent) AS\n" +
                "(SELECT taxon_id, parent_id FROM taxa),\n" +
                "ancestor(taxon_id) AS\n" +
                "(SELECT parent FROM parent_of WHERE taxon_id=%d\n" +
                "UNION ALL\n" +
                "SELECT parent FROM parent_of JOIN ancestor USING(taxon_id))\n" +
                "SELECT taxa.taxon_id FROM ancestor, taxa\n" +
                "WHERE ancestor.taxon_id=taxa.taxon_id;\n";

        final Map<Integer, List<Integer>> map = new HashMap<>();
        for (var taxonId : taxonIds) {
            final ResultSet rs = connection.createStatement().executeQuery(String.format(queryFormat, taxonId));
            final LinkedList<Integer> ancestors = new LinkedList<>();
            while (rs.next()) {
                ancestors.add(0, rs.getInt(1));
            }
            map.put(taxonId, ancestors);
        }
        return map;
    }

    public ArrayList<Integer> getAllTaxonIds() throws SQLException {
        final String query = "SELECT taxon_id FROM taxa;";

        final ResultSet rs = connection.createStatement().executeQuery(query);

        final ArrayList<Integer> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rs.getInt(1));
        }
        return result;
    }

    public Map<Integer, String> getFiles(Collection<Integer> taxonIds) throws SQLException, IOException {
        final String query = String.format("SELECT taxon_id,fasta_url FROM genomes WHERE taxon_id IN('%s');", Basic.toString(taxonIds, "','"));

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
        return executeQueryInt("SELECT count(*) FROM genomes;", 1).get(0);
    }

    public int countBloomFilters() throws SQLException {
        return executeQueryInt("SELECT count(*) FROM bloom_filters;", 1).get(0);
    }

    public int countMashSketches() throws SQLException {
        return executeQueryInt("SELECT count(*) FROM mash_sketches;", 1).get(0);
    }

    public int getMashK() throws SQLException {
        return executeQueryInt("SELECT value FROM info WHERE key='mash_k';", 1).get(0);
    }

    public int getMashS() throws SQLException {
        return executeQueryInt("SELECT value FROM info WHERE key='mash_s';", 1).get(0);
    }

    public int getMashSeed() throws SQLException {
        return executeQueryInt("SELECT value FROM info WHERE key='mash_seed';", 1).get(0);
    }

    public int getTaxonomyRoot() throws SQLException {
        return executeQueryInt("SELECT taxon_id FROM taxa WHERE parent_id=0;", 1).get(0);
    }

    public Collection<Integer> getTaxonomyChildren(int parent_id) throws SQLException {
        return new ArrayList<>(executeQueryInt("SELECT taxon_id FROM taxa WHERE parent_id=" + parent_id + ";", 1));
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

                try (InputStream ins = (new URL(id2file.get(taxonId)).openStream()); OutputStream outs = new FileOutputStream(cacheFile)) {
                    ins.transferTo(outs);
                } catch (IOException ex) {
                    NotificationManager.showError("Failed to cache file: " + id2file.get(taxonId));
                    if (cacheFile.exists())
                        cacheFile.delete();
                }
            }
            result.put(cacheFile.getPath(), "<c GRAY>" + id2name.get(taxonId) + "</c>");
            progress.incrementProgress();
        }
        return result;
    }

    /**
     * find all genomes that have non-zero Jaccard index when compared with the query
     */
    public Collection<Map.Entry<Integer, Double>> findSimilar(ProgressListener progress, int minSharedKMers, Collection<byte[]> query, boolean ignoreUnusableTaxa) throws SQLException, IOException {
        final int mash_k = getMashK();
        final int mash_s = getMashS();
        final int mash_seed = getMashSeed();

        if (verbose)
            System.err.println("Using mash_k=" + mash_k + ", mash_s=" + mash_s + ", mash_seed=" + mash_seed);

        progress.setTasks("Find similar", "Sketching");
        progress.setMaximum(query.size());
        progress.setProgress(0);
        final List<MashSketch> querySketches = new ArrayList<>();
        try {
            ExecuteInParallel.apply(query,
                    q -> Collections.singleton(MashSketch.compute("", Collections.singletonList(q), true, mash_s, mash_k, mash_seed, false, true, progress)),
                    querySketches, ProgramExecutorService.getNumberOfCoresToUse());
        } catch (Exception e) {
            throw new IOException(e);
        }

        final Set<String> kmers = new HashSet<>();
        for (MashSketch sketch : querySketches) {
            for (byte[] kmer : sketch.getKmers()) {
                kmers.add(Basic.toString(kmer));
            }
        }

        progress.setSubtask("Searching");
        progress.setMaximum(countBloomFilters());
        progress.setProgress(0);

        final ConcurrentHashMap<Integer, Double> id2distance = new ConcurrentHashMap<>();

        final Single<Exception> exception = new Single<>();
        final AtomicInteger jobs = new AtomicInteger(1);

        final ExecutorService service = Executors.newFixedThreadPool(ProgramExecutorService.getNumberOfCoresToUse());
        try {
            service.submit(createTasksRec(getTaxonomyRoot(), querySketches, kmers, minSharedKMers, id2distance, progress, exception, jobs, service));

            if (exception.get() != null)
                throw new IOException(exception.get());
            try {
                service.awaitTermination(1000, TimeUnit.DAYS);
            } catch (InterruptedException ignored) {
            }
        } finally {
            service.shutdown();
        }

        final ArrayList<Map.Entry<Integer, Double>> result = new ArrayList<>(id2distance.entrySet());
        result.sort(Comparator.comparingDouble(Map.Entry::getValue));
        if (ignoreUnusableTaxa && getUnusableTaxa().size() > 0) {
            return result.stream().filter(entry -> !getUnusableTaxa().contains(entry.getKey())).collect(Collectors.toList());
        }
        return result;
    }

    /**
     * creates a task to submitted to the service. This task will recursively submit further tasks and will call shutdown() once all tasks have been completed
     */
    private Runnable createTasksRec(int taxonId, Collection<MashSketch> querySketches, Set<String> kmers, int minSharedKMers, ConcurrentHashMap<Integer, Double> id2distance,
                                    ProgressListener progress, Single<Exception> exception, AtomicInteger jobCount, ExecutorService service) {
        return () -> {
            if (exception.get() == null) {
                try {
                    final AccessReferenceDatabase database = getCopy();
                    final Collection<Integer> ids = database.getTaxonomyChildren(taxonId);
                    final ArrayList<Pair<Integer, BloomFilter>> bloomFilters = database.getBloomFilters(ids);
                    for (Pair<Integer, BloomFilter> pair : bloomFilters) {
                        final BloomFilter bloomFilter = pair.getSecond();
                        if (bloomFilter.countContainedProbably(kmers) >= minSharedKMers) {
                            final int id = pair.getFirst();
                            jobCount.incrementAndGet();
                            service.submit(createTasksRec(id, querySketches, kmers, minSharedKMers, id2distance, progress, exception, jobCount, service));
                            //System.err.println("Adding bloom filter for " + id);
                        }
                        progress.incrementProgress();
                    }
                    final ArrayList<Pair<Integer, MashSketch>> mashSketches = database.getMashSketches(ids);

                    for (Pair<Integer, MashSketch> pair : mashSketches) {
                        final MashSketch mashSketch = pair.getSecond();
                        for (MashSketch sketch : querySketches) {
                            if (MashDistance.computeIntersection(mashSketch, sketch) >= minSharedKMers) {
                                final int id = pair.getFirst();
                                final double distance = MashDistance.compute(mashSketch, sketch);
                                synchronized (id2distance) {
                                    if (!id2distance.containsKey(id) || id2distance.get(id) > distance) {
                                        id2distance.put(id, distance);
                                    }
                                }
                                if (verbose)
                                    System.err.printf("Found similar: " + id + " JI: %f dist: %.8f%n", MashDistance.computeJaccardIndex(mashSketch, sketch), distance);
                            }
                        }
                        progress.incrementProgress();
                    }
                } catch (IOException | SQLException ex) {
                    exception.setIfCurrentValueIsNull(ex);
                }
            }
            if (jobCount.decrementAndGet() <= 0)
                service.shutdown();
        };
    }

    public Set<Integer> getUnusableTaxa() {
        return unusableTaxa;
    }

    public File getDbFile() {
        return dbFile;
    }

    private AccessReferenceDatabase getCopy() {
        return copiesForSearching[which.incrementAndGet() % copiesForSearching.length];
    }
}
