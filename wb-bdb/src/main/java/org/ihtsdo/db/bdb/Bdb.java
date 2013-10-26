package org.ihtsdo.db.bdb;

import com.sleepycat.je.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.JFrame;
import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.OpenFrames;
import org.dwfa.tapi.ComputationCanceled;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.ihtsdo.concept.BdbLegacyFixedFactory;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.ConceptBdb;
import org.ihtsdo.concept.OFFSETS;
import org.ihtsdo.concept.component.relationship.Relationship;
import org.ihtsdo.db.bdb.BdbMemoryMonitor.LowMemoryListener;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;
import org.ihtsdo.db.bdb.id.NidCNidMapBdb;
import org.ihtsdo.db.bdb.nidmaps.UuidToNidMapBdb;
import org.ihtsdo.db.bdb.sap.StatusAtPositionBdb;
import org.ihtsdo.db.util.ConsoleActivityViewer;
import org.ihtsdo.db.util.NidPair;
import org.ihtsdo.db.util.NidPairForRefex;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.helper.io.FileIO;
import org.ihtsdo.lucene.LuceneManager;
import org.ihtsdo.lucene.LuceneManager.LuceneSearchType;
import org.ihtsdo.thread.NamedThreadFactory;
import org.ihtsdo.time.TimeUtil;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.ComponentChronicleBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

public class Bdb {

    private static final String G_VERSION = "gVersion";
    public static AtomicLong gVersion = new AtomicLong();
    private static Bdb readOnly;
    private static Bdb mutable;
    private static UuidToNidMapBdb uuidsToNidMapDb;
    public static NidCNidMapBdb nidCidMapDb;
    private static StatusAtPositionBdb statusAtPositionDb;
    private static ConceptBdb conceptDb;
    private static PropertiesBdb propDb;
    public static ThreadGroup dbdThreadGroup =
            new ThreadGroup("db threads");
    private static ExecutorService syncService;
    public static ConcurrentHashMap<Integer, Integer> watchList =
            new ConcurrentHashMap<Integer, Integer>();
    private static BdbPathManager pathManager;
    private static boolean closed = true;
    private static BdbMemoryMonitor memoryMonitor = new BdbMemoryMonitor();
    public static boolean removeMemoryMonitorListener(LowMemoryListener listener) {
        return memoryMonitor.removeListener(listener);
    }

    public static boolean addMemoryMonitorListener(LowMemoryListener listener) {
        return memoryMonitor.addListener(listener);
    }

    static {
        memoryMonitor.setPercentageUsageThreshold(0.96);
    }

    public static boolean isClosed() {
        return closed;
    }

    public static void commit() throws IOException {
        long commitTime = System.currentTimeMillis();
        statusAtPositionDb.commit(commitTime);
    }

    public static void setup() {
        setup("berkeley-db");
    }

    public static void setup(String dbRoot) {
        setup(dbRoot, null);
    }

    public static void setCacheSize(String cacheSize) {
        long size = 0;
        if (cacheSize.toLowerCase().endsWith("m")) {
            cacheSize = cacheSize.substring(0, cacheSize.length() - 1);
            size = Integer.parseInt(cacheSize);
            size = size * 1000;
        } else if (cacheSize.toLowerCase().endsWith("g")) {
            cacheSize = cacheSize.substring(0, cacheSize.length() - 1);
            size = Integer.parseInt(cacheSize);
            size = size * 1000000;
        } else {
            size = Integer.parseInt(cacheSize);
        }
        EnvironmentMutableConfig mutableConfig = Bdb.mutable.bdbEnv.getMutableConfig();
        mutableConfig.setCacheSize(size);
        Bdb.mutable.bdbEnv.setMutableConfig(mutableConfig);
        Bdb.readOnly.bdbEnv.setMutableConfig(mutableConfig);
    }

    public static long getCacheSize() {
        return Bdb.mutable.bdbEnv.getMutableConfig().getCacheSize();
    }

    public static void setCachePercent(String cachePercent) {
        EnvironmentMutableConfig mutableConfig = Bdb.mutable.bdbEnv.getMutableConfig();
        mutableConfig.setCachePercent(Integer.parseInt(cachePercent));
        Bdb.mutable.bdbEnv.setMutableConfig(mutableConfig);
        Bdb.readOnly.bdbEnv.setMutableConfig(mutableConfig);
    }

    public static int getCachePercent() {
        return Bdb.mutable.bdbEnv.getMutableConfig().getCachePercent();
    }
    
    static ConcurrentSkipListSet<Concept> annotationConcepts;

    public static void xrefAnnotation(RefexChronicleBI annotation) throws IOException {
        Concept refexConcept = Concept.get(annotation.getRefexNid());
        if (refexConcept.isAnnotationIndex()) {
            if (refexConcept.addMemberNid(annotation.getNid())) {
                annotationConcepts.add(refexConcept);
            }
        }
    }

    static int getAuthorNidForSapNid(int sapNid) {
        return statusAtPositionDb.getAuthorNid(sapNid);
    }
    static int getPathNidForSapNid(int sapNid) {
        return statusAtPositionDb.getPathNid(sapNid);
    }
    static int getStatusNidForSapNid(int sapNid) {
        return statusAtPositionDb.getStatusNid(sapNid);
    }
    static int getModuleNidForSapNid(int sapNid) {
        return statusAtPositionDb.getModuleNid(sapNid);
    }
    static long getTimeForSapNid(int sapNid) {
        return statusAtPositionDb.getTime(sapNid);
    }

    private enum HeapSize {

        HEAP_1200("je-prop-options/1200.je.properties"),
        HEAP_1400("je-prop-options/1400.je.properties"),
        HEAP_2000("je-prop-options/2G.je.properties"),
        HEAP_4000("je-prop-options/4G.je.properties"),
        HEAP_6000("je-prop-options/6G.je.properties"),
        HEAP_8000("je-prop-options/8G.je.properties"),
        HEAP_10000("je-prop-options/10G.je.properties"),
        HEAP_12000("je-prop-options/12G.je.properties");
        String configFileName;

        private HeapSize(String configFileName) {
            this.configFileName = configFileName;
        }

        public InputStream getPropFile(File rootDir) throws IOException {
            File propFile = new File(rootDir, configFileName);
            if (propFile.exists()) {
                return propFile.toURI().toURL().openStream();
            }
            return Bdb.class.getResourceAsStream("/" + configFileName);
        }
    };
    private static HeapSize heapSize = HeapSize.HEAP_1200;

    public static void selectJeProperties(File configDir, File dbDir) throws IOException {
        long maxMem = Runtime.getRuntime().maxMemory();
        configDir.mkdirs();
        File jePropOptionsDir = new File(configDir, "je-prop-options");
        jePropOptionsDir.mkdirs();
        for (HeapSize size : HeapSize.values()) {
            File destFile =new File(configDir, size.configFileName);
            if (!destFile.exists()) {
                FileIO.copyFile(size.getPropFile(configDir), new File(configDir, size.configFileName));
            }
        }


        File mutableDir = new File(dbDir, "mutable");
        File readOnlyDir = new File(dbDir, "read-only");
        mutableDir.mkdirs();
        if (maxMem > 12000000000L) {
            heapSize = HeapSize.HEAP_12000;
            FileIO.copyFile(new File(configDir, HeapSize.HEAP_12000.configFileName),
                    new File(mutableDir, "je.properties"));
            if (readOnlyDir.exists()) {
                FileIO.copyFile(new File(configDir, HeapSize.HEAP_12000.configFileName),
                        new File(readOnlyDir, "je.properties"));
            }
        } else if (maxMem > 10000000000L) {
            heapSize = HeapSize.HEAP_10000;
            FileIO.copyFile(new File(configDir, HeapSize.HEAP_10000.configFileName),
                    new File(mutableDir, "je.properties"));
            if (readOnlyDir.exists()) {
                FileIO.copyFile(new File(configDir, HeapSize.HEAP_10000.configFileName),
                        new File(readOnlyDir, "je.properties"));
            }
        } else if (maxMem > 8000000000L) {
            heapSize = HeapSize.HEAP_8000;
            FileIO.copyFile(new File(configDir, HeapSize.HEAP_8000.configFileName),
                    new File(mutableDir, "je.properties"));
            if (readOnlyDir.exists()) {
                FileIO.copyFile(new File(configDir, HeapSize.HEAP_8000.configFileName),
                        new File(readOnlyDir, "je.properties"));
            }
        } else if (maxMem > 6000000000L) {
            heapSize = HeapSize.HEAP_6000;
            FileIO.copyFile(new File(configDir, HeapSize.HEAP_6000.configFileName),
                    new File(mutableDir, "je.properties"));
            if (readOnlyDir.exists()) {
                FileIO.copyFile(new File(configDir, HeapSize.HEAP_6000.configFileName),
                        new File(readOnlyDir, "je.properties"));
            }
        } else if (maxMem > 4000000000L) {
            heapSize = HeapSize.HEAP_4000;
            FileIO.copyFile(new File(configDir, HeapSize.HEAP_4000.configFileName),
                    new File(mutableDir, "je.properties"));
            if (readOnlyDir.exists()) {
                FileIO.copyFile(new File(configDir, HeapSize.HEAP_4000.configFileName),
                        new File(readOnlyDir, "je.properties"));
            }
        } else if (maxMem > 2000000000) {
            heapSize = HeapSize.HEAP_2000;
            FileIO.copyFile(new File(configDir, HeapSize.HEAP_2000.configFileName),
                    new File(mutableDir, "je.properties"));
            if (readOnlyDir.exists()) {
                FileIO.copyFile(new File(configDir, HeapSize.HEAP_2000.configFileName),
                        new File(readOnlyDir, "je.properties"));
            }
        } else if (maxMem > 1400000000) {
            heapSize = HeapSize.HEAP_1400;
            FileIO.copyFile(new File(configDir, HeapSize.HEAP_1400.configFileName),
                    new File(mutableDir, "je.properties"));
            if (readOnlyDir.exists()) {
                FileIO.copyFile(new File(configDir, HeapSize.HEAP_1400.configFileName),
                        new File(readOnlyDir, "je.properties"));
            }
        } else {
            heapSize = HeapSize.HEAP_1200;
            FileIO.copyFile(new File(configDir, HeapSize.HEAP_1200.configFileName),
                    new File(mutableDir, "je.properties"));
            if (readOnlyDir.exists()) {
                FileIO.copyFile(new File(configDir, HeapSize.HEAP_1200.configFileName),
                        new File(readOnlyDir, "je.properties"));
            }
        }

        System.out.println("!## maxMem: " + maxMem + " heapSize: " + heapSize);
    }

    public static void setup(String dbRoot, ActivityPanel activity) {
        sapNidCache = new ConcurrentHashMap<String, Integer>();
        annotationConcepts = new ConcurrentSkipListSet<>();
        watchList = new ConcurrentHashMap<Integer, Integer>();
        try {
            closed = false;
            syncService = Executors.newFixedThreadPool(1,
                    new NamedThreadFactory(dbdThreadGroup, "Sync service"));

            BdbCommitManager.reset();
            NidDataFromBdb.resetExecutorPool();
            BdbPathManager.reset();
            REFSET_TYPES.resetNids();
            
            for (@SuppressWarnings("unused") OFFSETS o : OFFSETS.values()) {
                // ensure all OFFSETS are initialized prior to multi-threading. 
            }
            File bdbDirectory = new File(dbRoot);
            bdbDirectory.mkdirs();
            LuceneManager.setLuceneRootDir(bdbDirectory, LuceneSearchType.DESCRIPTION);

            inform(activity, "Setting up database environment...");
            mutable = new Bdb(false, new File(bdbDirectory, "mutable"));
            inform(activity, "Berkeley DB Version: " + JEVersion.CURRENT_VERSION.getVersionString());
            File readOnlyDir = new File(bdbDirectory, "read-only");
            boolean readOnlyExists = readOnlyDir.exists();
            readOnly = new Bdb(readOnlyExists, readOnlyDir);
            inform(activity, "loading property database...");
            propDb = new PropertiesBdb(readOnly, mutable);



            inform(activity, "loading uuid to nid map database...");
            uuidsToNidMapDb = new UuidToNidMapBdb(readOnly, mutable);

            inform(activity, "loading nid->cid database...");
            nidCidMapDb = new NidCNidMapBdb(readOnly, mutable);
            inform(activity, "loading status@position database...");
            statusAtPositionDb = new StatusAtPositionBdb(readOnly, mutable);
            inform(activity, "loading concept database...");
            conceptDb = new ConceptBdb(readOnly, mutable);
            
           
            Concept.reset();

            ReferenceConcepts.reset();
            
            inform(activity, "setting up term factory...");

            String versionString = getProperty(G_VERSION);
            if (versionString != null) {
                gVersion.set(Long.parseLong(versionString));
            }

            BdbTermFactory tf = new BdbTermFactory();
            if (Terms.get() != null) {
                tf = (BdbTermFactory) Terms.get();
            } else {
                Terms.set(tf);
            }
            BdbTerminologyStore ts = new BdbTerminologyStore();
            if (Ts.get() == null) {
                Ts.set(ts);
            }
            LocalFixedTerminology.setStore(new BdbLegacyFixedFactory());

            //watchList.put(ReferenceConcepts.REFSET_PATH_ORIGINS.getNid(), ReferenceConcepts.REFSET_PATH_ORIGINS.getNid());

            inform(activity, "Loading paths...");
            pathManager = BdbPathManager.get();
            tf.setPathManager(pathManager);
            inform(activity, "Database open...");
            AceLog.getAppLog().info("mutable maxMem: "
                    + Bdb.mutable.bdbEnv.getConfig().getConfigParam("je.maxMemory"));
            AceLog.getAppLog().info("mutable shared cache: "
                    + Bdb.mutable.bdbEnv.getConfig().getSharedCache());
            AceLog.getAppLog().info("readOnly maxMem: "
                    + Bdb.readOnly.bdbEnv.getConfig().getConfigParam("je.maxMemory"));
            AceLog.getAppLog().info("readOnly shared cache: "
                    + Bdb.readOnly.bdbEnv.getConfig().getSharedCache());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void inform(ActivityPanel activity, String info) {
        if (activity != null) {
            activity.setProgressInfoLower(info);
        } else {
            AceLog.getAppLog().info(info);
        }

    }

    public static Database setupDatabase(boolean readOnly, String dbName, Bdb bdb) throws IOException, DatabaseException {
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setReadOnly(readOnly);
        dbConfig.setAllowCreate(!readOnly);
        dbConfig.setDeferredWrite(!readOnly);

        return bdb.bdbEnv.openDatabase(null,
                dbName, dbConfig);
    }
    protected Environment bdbEnv;

    private Bdb(boolean readOnly, File directory) throws IOException {
        try {
            directory.mkdirs();
            EnvironmentConfig envConfig = new EnvironmentConfig();
            envConfig.setSharedCache(true);
            envConfig.setReadOnly(readOnly);
            envConfig.setAllowCreate(!readOnly);
            //envConfig.setConfigParam(EnvironmentConfig.EVICTOR_LRU_ONLY, "false");
            envConfig.setConfigParam(EnvironmentConfig.EVICTOR_NODES_PER_SCAN, "4");

            /*
             * int primeForLockTable = SieveForPrimeNumbers.largestPrime(
             * Runtime.getRuntime().availableProcessors() - 1);
             *
             * envConfig.setConfigParam("je.lock.nLockTables", Integer.toString(primeForLockTable));
             * envConfig.setConfigParam("je.log.faultReadSize", "4096");
             *
             */


            bdbEnv = new Environment(directory, envConfig);
        } catch (EnvironmentLockedException e) {
            throw new IOException(e);
        } catch (DatabaseException e) {
            throw new IOException(e);
        }
    }
    private static ConcurrentHashMap<String, Integer> sapNidCache = new ConcurrentHashMap<String, Integer>();

    public static int getSapNid(TkRevision version) {
        assert version.getTime() != 0 : "Time is 0; was it initialized?";
        assert version.getStatusUuid() != null : "Status is null; was it initialized?";
        assert version.getPathUuid() != null : "Path is null; was it initialized?";
        assert version.getAuthorUuid() != null : "Author is null; was it initialized?";

        if (version.getTime() == Long.MIN_VALUE) {
            return -1;
        }
        String sapNidKey = version.getStatusUuid().toString() + version.getAuthorUuid() + 
                version.getPathUuid() + version.getTime() + version.getModuleUuid();
        Integer sapNid = sapNidCache.get(sapNidKey);
        if (sapNid != null) {
            return sapNid;
        }
        sapNid = statusAtPositionDb.getSapNid(
                uuidToNid(version.getStatusUuid()),
                version.getTime(),
                uuidToNid(version.getAuthorUuid()),
                uuidToNid(version.getModuleUuid()),
                uuidToNid(version.getPathUuid()));

        if (sapNidCache.size() > 500) {
            sapNidCache = new ConcurrentHashMap<String, Integer>();
        }
        sapNidCache.put(sapNidKey, sapNid);
        return sapNid;
    }

    public static int getSapNid(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
        assert time != 0 : "Time is 0; was it initialized?";
        assert statusNid != Integer.MIN_VALUE : "Status is Integer.MIN_VALUE; was it initialized?";
        assert pathNid != Integer.MIN_VALUE : "Path is Integer.MIN_VALUE; was it initialized?";
        if (time == Long.MIN_VALUE) {
            return -1;
        }
        return statusAtPositionDb.getSapNid(statusNid, time, authorNid, moduleNid, pathNid);
    }

    public static StatusAtPositionBdb getSapDb() {
        assert statusAtPositionDb != null;
        return statusAtPositionDb;
    }

    public static ConceptBdb getConceptDb() {
        assert conceptDb != null;
        return conceptDb;
    }

    public static void addAsAnnotations(List<TkRefexAbstractMember<?>> members) throws Exception {
        conceptDb.iterateConceptDataInParallel(new AnnotationAdder(members));
    }

    public static int getConceptNid(int componentNid) {
        if (nidCidMapDb == null) {
            return Integer.MAX_VALUE;
        }
        return nidCidMapDb.getCNid(componentNid);
    }

    public static Concept getConceptForComponent(int componentNid) throws IOException {
        int cNid = Bdb.getConceptNid(componentNid);
        if (cNid == Integer.MAX_VALUE) {
            return null;
        }
        return Concept.get(cNid);
    }

    public static UuidToNidMapBdb getUuidsToNidMap() {
        return uuidsToNidMapDb;
    }

    public static void sync()
            throws InterruptedException, ExecutionException, TerminologyException, IOException {
        syncService.execute(new Sync());
    }

    private static class Sync implements Runnable {

        private I_ShowActivity activity;
        private long startTime = System.currentTimeMillis();

        private Sync() throws TerminologyException, IOException {
            activity = Terms.get().newActivityPanel(true, Terms.get().getActiveAceFrameConfig(), "Database sync to disk...", false);
            activity.setStopButtonVisible(false);
            activity.setIndeterminate(true);
            activity.setProgressInfoUpper("Database sync to disk...");

            /*
             * try { Concept pathOrigins =
             * getConceptDb().getConcept(RefsetAuxiliary.Concept.REFSET_PATH_ORIGINS.localize().getNid()); if
             * (pathOrigins != null) { AceLog.getAppLog().info("Refset origins:\n\n" +
             * pathOrigins.toLongString()); } } catch (Exception e) {
             * AceLog.getAppLog().alertAndLogException(e); }
             */

            activity.setProgressInfoLower("Starting sync...");
        }

        @Override
        public void run() {
            try {
                activity.setIndeterminate(false);
                activity.setValue(0);
                activity.setMaximum(9);
                setProperty(G_VERSION, Long.toString(gVersion.incrementAndGet()));
                activity.setProgressInfoLower("Writing uuidDb... ");
                activity.setValue(1);
                activity.setProgressInfoLower("Writing uuidsToNidMapDb... ");
                uuidsToNidMapDb.sync();
                activity.setValue(2);
                nidCidMapDb.sync();
                activity.setValue(3);
                activity.setProgressInfoLower("Writing statusAtPositionDb... ");
                statusAtPositionDb.sync();
                activity.setValue(4);
                activity.setProgressInfoLower("Writing conceptDb... ");
                conceptDb.sync();
                activity.setValue(5);
                activity.setProgressInfoLower("Writing xref... ");
                activity.setValue(6);
                activity.setProgressInfoLower("Writing propDb... ");
                propDb.sync();
                activity.setProgressInfoLower("Writing mutable environment... ");
                activity.setValue(7);
                mutable.bdbEnv.sync();
                activity.setProgressInfoLower("Writing readonly environment... ");
                activity.setValue(8);
                if (readOnly.bdbEnv.getConfig().getReadOnly() == false) {
                    readOnly.bdbEnv.sync();
                }
                activity.setValue(9);
                long endTime = System.currentTimeMillis();

                long elapsed = endTime - startTime;
                String elapsedStr = TimeUtil.getElapsedTimeString(elapsed);

                activity.setProgressInfoUpper("Database sync complete.");
                activity.setProgressInfoLower("Elapsed: " + elapsedStr);
                activity.complete();
            } catch (DatabaseException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (ComputationCanceled e) {
                // Nothing to do 
            }
        }
    }

    /**
     * For unit test teardown. May corrupt database. 
     */
    public static void fastExit() {
        try {
            mutable.bdbEnv.close();
        } catch (Throwable e) {
        }
        try {
            mutable.bdbEnv.close();
        } catch (Throwable e) {
        }
    }

    // Close the environment
    public static void close() throws InterruptedException, ExecutionException {
        if (closed == false && mutable != null && mutable.bdbEnv != null) {
            closed = true;
            try {
                for (JFrame f : OpenFrames.getFrames()) {
                    if (f.isVisible() && ActivityViewer.getActivityFrame() != f) {
                        f.setVisible(false);
                        f.dispose();
                    }
                }
                ActivityViewer.toFront();
                I_ShowActivity activity = new ConsoleActivityViewer();
                activity.setStopButtonVisible(false);
                activity.setProgressInfoLower("2/11: Starting sync using service.");
                assert conceptDb != null : "conceptDb is null...";
                new Sync().run();
                activity.setProgressInfoLower("3/11: Shutting down sync service.");
                syncService.shutdown();

                activity.setProgressInfoLower("4/11: Awaiting termination of sync service.");
                syncService.awaitTermination(90, TimeUnit.MINUTES);


                activity.setProgressInfoLower("5/11: Starting PositionMapper close.");
                activity.setProgressInfoLower("6/11: Canceling uncommitted changes.");
                Terms.get().cancel();
                activity.setProgressInfoLower("7/11: Starting BdbCommitManager shutdown.");
                BdbCommitManager.shutdown();
                activity.setProgressInfoLower("8/11: Starting LuceneManager close.");
                LuceneManager.close(LuceneSearchType.DESCRIPTION);
                LuceneManager.close(LuceneSearchType.WORKFLOW_HISTORY);


                NidDataFromBdb.close();
                activity.setProgressInfoLower("9/11: Starting mutable.bdbEnv.sync().");
                mutable.bdbEnv.sync();
                activity.setProgressInfoLower("10/11: mutable.bdbEnv.sync() finished.");
                uuidsToNidMapDb.close();
                nidCidMapDb.close();
                statusAtPositionDb.close();
                conceptDb.close();
                propDb.close();
                mutable.bdbEnv.sync();
                mutable.bdbEnv.close();
                sapNidCache.clear();
                activity.setProgressInfoLower("11/11: Shutdown complete");
            } catch (DatabaseException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (Exception e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
        if (readOnly != null && readOnly.bdbEnv != null) {
            readOnly.bdbEnv.close();
        }
        annotationConcepts = null;
        conceptDb = null;
        mutable = null;
        nidCidMapDb = null;
        pathManager = null;
        propDb = null;
        readOnly = null;
        sapNidCache = null;
        statusAtPositionDb = null;
        uuidsToNidMapDb = null;
        watchList = null;
       
        Concept.reset();
        AceLog.getAppLog().info("bdb close finished.");
    }

    public static NidCNidMapBdb getNidCNidMap() {
        return nidCidMapDb;
    }

    public static int uuidsToNid(Collection<UUID> uuids) {
        return uuidsToNidMapDb.uuidsToNid(uuids);
    }

    public static int uuidToNid(UUID uuid) {
        return uuidsToNidMapDb.uuidToNid(uuid);
    }

    public static int uuidToNid(UUID... uuids) {
        return uuidsToNidMapDb.uuidsToNid(uuids);
    }

    public static ComponentBI getComponent(int nid) throws IOException {
        int cNid = Bdb.getConceptNid(nid);
        if (cNid == Integer.MAX_VALUE) {
            return null;
        }

        Concept c = Bdb.getConceptDb().getConcept(cNid);
        if (cNid == nid) {
            return c;
        }
        return c.getComponent(nid);
    }

    public static BdbPathManager getPathManager() {
        return pathManager;
    }

    public static Map<String, String> getProperties() throws IOException {
        return propDb.getProperties();
    }

    public static String getProperty(String key) throws IOException {
        return propDb.getProperty(key);
    }

    public static void setProperty(String key, String value) throws IOException {
        propDb.setProperty(key, value);
    }

    public static void compress(int utilization) throws IOException {
        try {

            String lookAheadCacheSize = mutable.bdbEnv.getConfig().getConfigParam("je.cleaner.lookAheadCacheSize");
            mutable.bdbEnv.getConfig().setConfigParam("je.cleaner.lookAheadCacheSize", "81920");

            String cluster = mutable.bdbEnv.getConfig().getConfigParam("je.cleaner.cluster");
            mutable.bdbEnv.getConfig().setConfigParam("je.cleaner.cluster", "true");

            String minFileUtilization = mutable.bdbEnv.getConfig().getConfigParam("je.cleaner.minFileUtilization");
            mutable.bdbEnv.getConfig().setConfigParam("je.cleaner.minFileUtilization", Integer.toString(50));

            String minUtilization = mutable.bdbEnv.getConfig().getConfigParam("je.cleaner.minUtilization");
            mutable.bdbEnv.getConfig().setConfigParam("je.cleaner.minUtilization", Integer.toString(utilization));

            String threads = mutable.bdbEnv.getConfig().getConfigParam("je.cleaner.threads");
            mutable.bdbEnv.getConfig().setConfigParam("je.cleaner.threads", "4");

            boolean anyCleaned = false;
            while (mutable.bdbEnv.cleanLog() > 0) {
                anyCleaned = true;
            }
            if (anyCleaned) {
                CheckpointConfig force = new CheckpointConfig();
                force.setForce(true);
                mutable.bdbEnv.checkpoint(force);
            }

            mutable.bdbEnv.getConfig().setConfigParam("je.cleaner.lookAheadCacheSize", lookAheadCacheSize);
            mutable.bdbEnv.getConfig().setConfigParam("je.cleaner.cluster", cluster);
            mutable.bdbEnv.getConfig().setConfigParam("je.cleaner.minFileUtilization", minFileUtilization);
            mutable.bdbEnv.getConfig().setConfigParam("je.cleaner.minUtilization", minUtilization);
            mutable.bdbEnv.getConfig().setConfigParam("je.cleaner.threads", threads);
        } catch (DatabaseException e) {
            throw new IOException(e);
        }
    }

    public static void addToWatchList(I_GetConceptData c) {
        watchList.put(c.getNid(), c.getConceptNid());
    }

    public static void removeFromWatchList(I_GetConceptData c) {
        watchList.remove(c.getNid());
    }

    public static UUID getPrimUuidForConcept(int cNid) throws IOException {
        assert cNid == Bdb.getConceptNid(cNid) : " Not a concept nid: " + cNid;
        return conceptDb.getConcept(cNid).getPrimUuid();
    }

    public static UUID getPrimUuidForComponent(int nid) throws IOException {
        int cNid = Bdb.getConceptNid(nid);
        assert cNid != Integer.MAX_VALUE : "No cNid for nid: " + nid;
        Concept c = Concept.get(cNid);
        ComponentChronicleBI<?> component = c.getComponent(nid);
        if (component != null) {
            return component.getPrimUuid();
        }
        String warning = "Can't find component: " + nid + " in concept: " + c.toLongString();
        AceLog.getAppLog().warning(warning);
        return null;
    }

    public static boolean isConcept(int cNid) {
        return cNid == Bdb.getConceptNid(cNid);
    }

    public static Concept getConcept(int cNid) throws IOException {
        assert cNid == Bdb.getConceptNid(cNid) :
                " Not a concept nid: " + cNid
                + " Bdb cNid:" + Bdb.getConceptNid(cNid) + " max nid: "
                + Bdb.getUuidsToNidMap().getCurrentMaxNid()
                + " (" + (Bdb.getUuidsToNidMap().getCurrentMaxNid() - cNid) + ")";
        return conceptDb.getConcept(cNid);
    }

    public static boolean hasUuid(UUID uuid) {
        return uuidsToNidMapDb.hasUuid(uuid);
    }

    public static String getStats() {
        StringBuilder statBuff = new StringBuilder();
        statBuff.append("<html>Mutable<br>");
        statBuff.append(mutable.bdbEnv.getStats(null).toStringVerbose());
        statBuff.append("<br><br>ReadOnly:<br><br>");
        statBuff.append(readOnly.bdbEnv.getStats(null).toStringVerbose());

        return statBuff.toString().replace("\n", "<br>");
    }
    
    public static List<NidPairForRefex> getRefsetPairs(int nid) {
        return Arrays.asList(Bdb.getNidCNidMap().getRefsetPairs(nid));
    }
    
    public static void addXrefPair(int nid, NidPairForRefex pair) throws IOException {
        Bdb.getNidCNidMap().addNidPairForRefex(nid, pair);
    }
    
    public static void forgetXrefPair(int nid, NidPairForRefex pair) {
        Bdb.getNidCNidMap().forgetNidPairForRefex(nid, pair);
    }
    
    public static void addRelOrigin(int destinationCNid, int originCNid) throws IOException {
        nidCidMapDb.addRelOrigin(destinationCNid, originCNid);
    }
    
    public static Collection<Relationship> getDestRels(int cNid) throws IOException {
      return getNidCNidMap().getDestRels(cNid);
   }
}
