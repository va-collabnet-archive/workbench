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

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.vodb.PathManager;
import org.ihtsdo.db.bdb.concept.BdbLegacyFixedFactory;
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
	private static Bdb mutable;
	private static UuidBdb uuidDb;
	private static UuidsToNidMapBdb uuidsToNidMapDb;
	private static NidCNidMapBdb nidCidMapDb;
	private static StatusAtPositionBdb statusAtPositionDb;
	private static ConceptBdb conceptDb;
	
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
			executorPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
			File buildDirectory = new File(dbRoot);
			buildDirectory.mkdirs();
			File bdbDirectory = new File(buildDirectory, "berkeley-db");
			bdbDirectory.mkdirs();
			mutable = new Bdb(false, new File(bdbDirectory, "mutable"));
			File readOnlyDir = new File(bdbDirectory, "read-only");
			boolean readOnlyExists = readOnlyDir.exists();
			readOnly = new Bdb(readOnlyExists, readOnlyDir);
			uuidDb = new UuidBdb(readOnly, mutable);
			uuidsToNidMapDb = new UuidsToNidMapBdb(readOnly, mutable);
			nidCidMapDb = new NidCNidMapBdb(readOnly, mutable);
			statusAtPositionDb = new StatusAtPositionBdb(readOnly, mutable);
			conceptDb = new ConceptBdb(readOnly, mutable);
			BdbTermFactory tf = new BdbTermFactory();
			Terms.set(tf);
			LocalFixedTerminology.setStore(new BdbLegacyFixedFactory());
			tf.setPathManager(new PathManager());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (TerminologyException e) {
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
		return statusAtPositionDb.getSapNid(
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
		return nidCidMapDb.getCNid(componentNid);
	}
	public static Concept getConceptForComponent(int componentNid) {
		throw new UnsupportedOperationException();
	}
	
	public static UuidsToNidMapBdb getUuidsToNidMap() {
		return uuidsToNidMapDb;
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
			uuidDb.sync();
			uuidsToNidMapDb.sync();
			nidCidMapDb.sync();
			statusAtPositionDb.sync();
			conceptDb.sync();
			mutable.bdbEnv.sync();
			if (readOnly.bdbEnv.getConfig().getReadOnly() == false) {
				readOnly.bdbEnv.sync();
			}
			return Boolean.TRUE;
		}
		
	}
	// Close the environment
	public static void close() throws InterruptedException, ExecutionException {
		if (mutable.bdbEnv != null) {
			try {
				if (syncFuture != null  &&
						syncFuture.isDone() != true) {
					AceLog.getAppLog().info("Waiting for syncFuture to finish.");
					syncFuture.get();
					AceLog.getAppLog().info("SyncFuture finished.");
				}
				uuidDb.close();
				uuidsToNidMapDb.close();
				nidCidMapDb.close();
				statusAtPositionDb.close();
				conceptDb.close();
				mutable.bdbEnv.sync();
				mutable.bdbEnv.close();
			} catch (DatabaseException dbe) {
				AceLog.getAppLog().alertAndLogException(dbe);
			}
		}
		if (readOnly.bdbEnv != null) {
			readOnly.bdbEnv.close();
		}
		executorPool.shutdown();
		executorPool = null;
		mutable = null;
		readOnly = null;
		uuidDb = null;
		uuidsToNidMapDb = null;
		nidCidMapDb = null;
		statusAtPositionDb = null;
		conceptDb = null;
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
		Concept c = Bdb.getConceptDb().getConcept(cNid);
		if (cNid == nid) {
			return c;
		}
		return c.getComponent(nid);
	}


}
