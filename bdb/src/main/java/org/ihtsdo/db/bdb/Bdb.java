package org.ihtsdo.db.bdb;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.ConceptBdb;
import org.ihtsdo.db.bdb.concept.OFFSETS;
import org.ihtsdo.etypes.EVersion;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentLockedException;

public class Bdb {

	private static Bdb readOnly;
	private static Bdb readWrite;
	private static StatusAtPositionBdb statusAtPositionDb;
	private static ConceptBdb conceptDb;
	private static UuidsToNidMap uuidsToNidMap;
	
	private static ExecutorService executorPool;
	
	public static void commit() throws IOException {
		long commitTime = System.currentTimeMillis();
		statusAtPositionDb.commit(commitTime);
	}
	
	public static void setup() {
		setup("target");
	}

	
	public static void setup(String dbRoot) {
		try {
			for (@SuppressWarnings("unused") OFFSETS o: OFFSETS.values()) {
				// ensure all OFFSETS are initialized prior to multi-threading. 
			}
			File buildDirectory = new File(dbRoot);
			buildDirectory.mkdirs();
			File bdbDirectory = new File(buildDirectory, "berkeley-db");
			bdbDirectory.mkdirs();
			readWrite = new Bdb(false, new File(bdbDirectory, "read-write"));
			File readOnlyDir = new File(bdbDirectory, "read-only");
			boolean readOnlyExists = readOnlyDir.exists();
			readOnly = new Bdb(readOnlyExists, readOnlyDir);
			statusAtPositionDb = new StatusAtPositionBdb(readOnly, readWrite);
			conceptDb = new ConceptBdb(readOnly, readWrite);
			executorPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
			uuidsToNidMap = new UuidsToNidMap(0, 100);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Database setupDatabase(boolean readOnly, String dbName, Bdb bdb) throws IOException {
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setReadOnly(readOnly);
		dbConfig.setAllowCreate(!readOnly);
		dbConfig.setDeferredWrite(!readOnly);
		try {
			return bdb.bdbEnv.openDatabase(null,
					dbName,
					dbConfig);
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

	
	public static int getStatusAtPositionNid(EVersion version) {
		assert version.getTime() != 0: "Time is 0; was it initialized?";
		assert version.getTime() != Long.MIN_VALUE: "Time is Long.MIN_VALUE; was it initialized?";
		assert version.getStatusUuid() != null: "Status is null; was it initialized?";
		assert version.getPathUuid() != null: "Path is null; was it initialized?";
		return statusAtPositionDb.getStatusAtPositionNid(
				uuidToNid(version.getStatusUuid()), 
				uuidToNid(version.getPathUuid()), 
				version.getTime());
	}

	public static StatusAtPositionBdb getStatusAtPositionDb() {
		assert statusAtPositionDb != null;
		return statusAtPositionDb;
	}
	
	public static ConceptBdb getConceptDb() {
		assert conceptDb != null;
		return conceptDb;
	}

	public static ExecutorService getExecutorPool() {
		return executorPool;
	}
	
	public static int getConceptNid(int componentNid) {
		throw new UnsupportedOperationException();
	}
	public static Concept getConceptForComponent(int componentNid) {
		throw new UnsupportedOperationException();
	}
	
	public static UuidsToNidMap getUuidsToNidMap() {
		return uuidsToNidMap;
	}

	public static void setUuidsToNidMap(UuidsToNidMap uuidsToNidMap) {
		Bdb.uuidsToNidMap = uuidsToNidMap;
	}

	private static Future<Boolean> syncFuture;
	public static Future<Boolean> sync() throws InterruptedException, ExecutionException {
		if (syncFuture != null) {
			if (syncFuture.isDone() != true) {
				syncFuture.get();
			}
		}
		syncFuture = executorPool.submit(new Sync());
		return syncFuture;
	}
	
	private static class Sync implements Callable<Boolean> {

		@Override
		public Boolean call() throws Exception {
			conceptDb.sync();
			statusAtPositionDb.sync();
			readWrite.bdbEnv.sync();
			if (readOnly.bdbEnv.getConfig().getReadOnly() == false) {
				readOnly.bdbEnv.sync();
			}
			return Boolean.TRUE;
		}
		
	}
	// Close the environment
	public static void close() throws InterruptedException, ExecutionException {
		if (readWrite.bdbEnv != null) {
			try {
				if (syncFuture != null  &&
						syncFuture.isDone() != true) {
					syncFuture.get();
				}
				conceptDb.close();
				statusAtPositionDb.close();
				readWrite.bdbEnv.close();
			} catch (DatabaseException dbe) {
				AceLog.getAppLog().alertAndLogException(dbe);
			}
		}
	}

	public static NidCNidMap getNidCNidMap() {
		return uuidsToNidMap.getNidCidMap();
	}

	public static int uuidsToNid(Collection<UUID> uuids) {
		return uuidsToNidMap.uuidsToNidWithGeneration(uuids);
	}

	public static int uuidToNid(UUID uuid) {
		return uuidsToNidMap.uuidToNidWithGeneration(uuid);
	}


}
