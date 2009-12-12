package org.ihtsdo.db.bdb;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.ConceptBdb;

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
	
	private static ExecutorService executorPool;
	
	public static void commit() throws IOException {
		long commitTime = System.currentTimeMillis();
		statusAtPositionDb.commit(commitTime);
	}
	
	public static void setup() throws IOException {
		readOnly = new Bdb(true, "berkeley-db/read-only");
		readWrite = new Bdb(false, "berkeley-db/read-write");
		statusAtPositionDb = new StatusAtPositionBdb(readOnly, readWrite);
		conceptDb = new ConceptBdb(readOnly, readWrite);
		executorPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
	}
	
	public static Database setupDatabase(boolean readOnly, String dbName, Bdb bdb) throws IOException {
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setReadOnly(readOnly);
		dbConfig.setAllowCreate(!readOnly);
		try {
			return bdb.bdbEnv.openDatabase(null,
					dbName,
					dbConfig);
		} catch (DatabaseException e) {
			throw new IOException(e);
		}
	}

	private Environment bdbEnv;

	private Bdb(boolean readOnly, String directory) throws IOException {
		try {
			EnvironmentConfig envConfig = new EnvironmentConfig();
			envConfig.setSharedCache(true);
			envConfig.setReadOnly(readOnly);
			envConfig.setAllowCreate(!readOnly);
			bdbEnv = new Environment(new File(directory), envConfig);
		} catch (EnvironmentLockedException e) {
			throw new IOException(e);
		} catch (DatabaseException e) {
			throw new IOException(e);
		}
	}

	// Close the environment
	public void close() {
		if (bdbEnv != null) {
			try {
				bdbEnv.close();
			} catch (DatabaseException dbe) {
				AceLog.getAppLog().alertAndLogException(dbe);
			}
		}
	}

	public static StatusAtPositionBdb getStatusAtPositionDb() {
		return statusAtPositionDb;
	}
	
	public static ConceptBdb getConceptDb() {
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
}
