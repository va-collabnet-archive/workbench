package org.ihtsdo.db.bdb;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JFrame;

import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.api.I_AmTermComponent;
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
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.computer.version.PositionMapper;
import org.ihtsdo.db.bdb.id.NidCNidMapBdb;
import org.ihtsdo.db.bdb.id.UuidBdb;
import org.ihtsdo.db.bdb.id.UuidsToNidMapBdb;
import org.ihtsdo.db.bdb.sap.StatusAtPositionBdb;
import org.ihtsdo.etypes.ERevision;
import org.ihtsdo.lucene.LuceneManager;
import org.ihtsdo.thread.NamedThreadFactory;
import org.ihtsdo.time.TimeUtil;

import com.sleepycat.je.CheckpointConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentLockedException;
import com.sleepycat.je.EnvironmentMutableConfig;

public class Bdb {

	private static final String G_VERSION = "gVersion";
	public static AtomicLong gVersion = new AtomicLong();
	private static Bdb readOnly;
	private static Bdb mutable;
	private static UuidBdb uuidDb;
	private static UuidsToNidMapBdb uuidsToNidMapDb;
	private static NidCNidMapBdb nidCidMapDb;
	private static StatusAtPositionBdb statusAtPositionDb;
	private static ConceptBdb conceptDb;
	private static PropertiesBdb propDb;
	private static ThreadGroup dbdThreadGroup  = 
		new ThreadGroup("db threads");

	private static ExecutorService syncService = Executors.newFixedThreadPool(1, new NamedThreadFactory(dbdThreadGroup,
	"Sync service"));
	
	public static ConcurrentHashMap<Integer, Integer> watchList = 
		new ConcurrentHashMap<Integer, Integer>();
			
	private static BdbPathManager pathManager;
	
	private static boolean closed = true;
	
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

	public static void setup(String dbRoot, ActivityPanel activity) {
		try {
			closed = false;
			BdbCommitManager.reset();
			for (@SuppressWarnings("unused") OFFSETS o: OFFSETS.values()) {
				// ensure all OFFSETS are initialized prior to multi-threading. 
			}
			File bdbDirectory = new File(dbRoot);
			bdbDirectory.mkdirs();
			LuceneManager.luceneDirFile = new File(bdbDirectory, "lucene");
			inform(activity, "Setting up database environment...");
			mutable = new Bdb(false, new File(bdbDirectory, "mutable"));
			File readOnlyDir = new File(bdbDirectory, "read-only");
			boolean readOnlyExists = readOnlyDir.exists();
			readOnly = new Bdb(readOnlyExists, readOnlyDir);
			inform(activity, "loading property database...");
			propDb = new PropertiesBdb(readOnly, mutable);
			inform(activity, "loading uuid database...");
			uuidDb = new UuidBdb(readOnly, mutable);
			inform(activity, "loading uuid to nid map database...");
			uuidsToNidMapDb = new UuidsToNidMapBdb(readOnly, mutable);

			inform(activity, "loading nid->cid database...");
			nidCidMapDb = new NidCNidMapBdb(readOnly, mutable);
			inform(activity, "loading status@position database...");
			statusAtPositionDb = new StatusAtPositionBdb(readOnly, mutable);
			inform(activity, "loading concept database...");
			conceptDb = new ConceptBdb(readOnly, mutable);
			inform(activity, "loaded concept database...");
			
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
			LocalFixedTerminology.setStore(new BdbLegacyFixedFactory());
			pathManager = BdbPathManager.get();
			tf.setPathManager(pathManager);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (TerminologyException e) {
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

	public static Database setupDatabase(boolean readOnly, String dbName, Bdb bdb) throws IOException {
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setReadOnly(readOnly);
		dbConfig.setAllowCreate(!readOnly);
		dbConfig.setDeferredWrite(!readOnly);
		try {
			return bdb.bdbEnv.openDatabase(null,
					dbName, dbConfig);
		} catch (DatabaseException e) {
			throw new IOException(e);
		}
	}

	protected Environment bdbEnv;

	private Bdb(boolean readOnly, File directory) throws IOException {
		try {
			directory.mkdirs();
			EnvironmentConfig envConfig = new EnvironmentConfig();
			envConfig.setSharedCache(true);
			envConfig.setReadOnly(readOnly);
			envConfig.setAllowCreate(!readOnly);
			bdbEnv = new Environment(directory, envConfig);
		} catch (EnvironmentLockedException e) {
			throw new IOException(e);
		} catch (DatabaseException e) {
			throw new IOException(e);
		}
	}

	
	public static int getSapNid(ERevision version) {
		assert version.getTime() != 0: "Time is 0; was it initialized?";
		assert version.getTime() != Long.MIN_VALUE: "Time is Long.MIN_VALUE; was it initialized?";
		assert version.getStatusUuid() != null: "Status is null; was it initialized?";
		assert version.getPathUuid() != null: "Path is null; was it initialized?";
		return statusAtPositionDb.getSapNid(
				uuidToNid(version.getStatusUuid()), 
				uuidToNid(version.getPathUuid()), 
				version.getTime());
	}

	public static int getSapNid(int statusNid, int pathNid, long time) {
		assert time != 0: "Time is 0; was it initialized?";
		assert time != Long.MIN_VALUE: "Time is Long.MIN_VALUE; was it initialized?";
		assert statusNid != Integer.MIN_VALUE: "Status is Integer.MIN_VALUE; was it initialized?";
		assert pathNid != Integer.MIN_VALUE: "Path is Integer.MIN_VALUE; was it initialized?";
		return statusAtPositionDb.getSapNid(statusNid, pathNid, time);
	}

	public static StatusAtPositionBdb getSapDb() {
		assert statusAtPositionDb != null;
		return statusAtPositionDb;
	}
	
	public static ConceptBdb getConceptDb() {
		assert conceptDb != null;
		return conceptDb;
	}
	
	public static int getConceptNid(int componentNid) {
		return nidCidMapDb.getCNid(componentNid);
	}
	public static Concept getConceptForComponent(int componentNid) throws IOException {
		int cNid = Bdb.getConceptNid(componentNid);
		if (cNid == Integer.MAX_VALUE) {
			return null;
		}
		return Concept.get(cNid);
	}
	
	public static UuidsToNidMapBdb getUuidsToNidMap() {
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
	        activity.setProgressInfoLower("Starting sync...");
	    }

		@Override
		public void run() {
			try {
	            activity.setIndeterminate(false);
	            activity.setValue(0);
	            activity.setMaximum(8);
				setProperty(G_VERSION, Long.toString(gVersion.incrementAndGet()));
                activity.setProgressInfoLower("Writing uuidDb... ");
				uuidDb.sync();
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
                activity.setProgressInfoLower("Writing propDb... ");
				propDb.sync();
                activity.setProgressInfoLower("Writing mutable environment... ");
                activity.setValue(6);
				mutable.bdbEnv.sync();
                activity.setProgressInfoLower("Writing readonly environment... ");
                activity.setValue(7);
				if (readOnly.bdbEnv.getConfig().getReadOnly() == false) {
					readOnly.bdbEnv.sync();
				}
                activity.setValue(8);
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
                AceLog.getAppLog().alertAndLogException(e);
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
                for (JFrame f: OpenFrames.getFrames()) {
                    if (f.isVisible() && ActivityViewer.getActivityFrame() != f) {
                        f.setVisible(false);
                        f.dispose();
                    }
                }
			    ActivityViewer.toFront();
			    I_ShowActivity activity = Terms.get().newActivityPanel(true,
			        Terms.get().getActiveAceFrameConfig(), "Executing shutdown sequence", false);
			    activity.setStopButtonVisible(false);
                activity.setProgressInfoLower("1/10: Starting sync using service.");
                assert conceptDb != null: "conceptDb is null...";
                new Sync().run();
                activity.setProgressInfoLower("2/10: Shutting down sync service.");
                syncService.shutdown();
			    
                activity.setProgressInfoLower("3/10: Awaiting termination of sync service.");
                syncService.awaitTermination(90, TimeUnit.MINUTES);

                activity.setProgressInfoLower("4/10: Starting LuceneManager close.");
		        LuceneManager.close();

                activity.setProgressInfoLower("5/10: Starting PositionMapper close.");
				PositionMapper.close();
				activity.setProgressInfoLower("6/10: Canceling uncommitted changes.");
				Terms.get().cancel();
				 activity.setProgressInfoLower("7/10: Starting BdbCommitManager shutdown.");
				BdbCommitManager.shutdown();
                NidDataFromBdb.close();
                activity.setProgressInfoLower("8/10: Starting mutable.bdbEnv.sync().");
				mutable.bdbEnv.sync();
				activity.setProgressInfoLower("9/10: mutable.bdbEnv.sync() finished.");
				uuidDb.close();
				uuidsToNidMapDb.close();
				nidCidMapDb.close();
				statusAtPositionDb.close();
				conceptDb.close();
				propDb.close();
				mutable.bdbEnv.sync();
				mutable.bdbEnv.close();
                activity.setProgressInfoLower("10/10: Shutdown complete");
			} catch (DatabaseException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (Exception e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}
		if (readOnly != null && readOnly.bdbEnv != null) {
			readOnly.bdbEnv.close();
		}
		mutable = null;
		readOnly = null;
		uuidDb = null;
		uuidsToNidMapDb = null;
		nidCidMapDb = null;
		statusAtPositionDb = null;
		conceptDb = null;
		propDb= null;
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

	public static UuidBdb getUuidDb() {
		return uuidDb;
	}

	public static I_AmTermComponent getComponent(int nid) throws IOException {
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
		watchList.put(c.getNid(), c.getConceptId());
	}

	public static void removeFromWatchList(I_GetConceptData c) {
		watchList.remove(c.getNid());
	}
	
	public static UUID getPrimUuidForConcept(int cNid) throws IOException {
		assert cNid == Bdb.getConceptNid(cNid): " Not a concept nid: " + cNid;
		return conceptDb.getConcept(cNid).getPrimUuid();
	}
	public static UUID getPrimUuidForComponent(int nid) throws IOException {
		int cNid = Bdb.getConceptNid(nid);
		assert cNid != Integer.MAX_VALUE: "No cNid for nid: " + nid;
		Concept c = Concept.get(cNid);
		ConceptComponent<?, ?> component = c.getComponent(nid);
		if (component != null) {
	        return component.getPrimUuid();
		}
		String warning = "Can't find component: " + nid + " in concept: " + c.toLongString();
		AceLog.getAppLog().warning(warning);
		return null;
	}
	public static Concept getConcept(int cNid) throws IOException {
		assert cNid == Bdb.getConceptNid(cNid): " Not a concept nid: " + cNid;
		return conceptDb.getConcept(cNid);
	}

    public static boolean hasUuid(UUID uuid) {
        return uuidsToNidMapDb.hasUuid(uuid);
    }

    public static String getStats() {
        StringBuffer statBuff = new StringBuffer();
        statBuff.append("<html>Mutable<br>");
        statBuff.append(mutable.bdbEnv.getStats(null).toStringVerbose());
        statBuff.append("<br><br>ReadOnly:<br><br>");
        statBuff.append(readOnly.bdbEnv.getStats(null).toStringVerbose());
    
        return statBuff.toString().replace("\n", "<br>");
    }
}
