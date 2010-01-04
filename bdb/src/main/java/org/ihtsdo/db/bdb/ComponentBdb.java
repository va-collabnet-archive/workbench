package org.ihtsdo.db.bdb;

import java.io.IOException;

import com.sleepycat.je.Database;

/**
 * 
 * @author kec
 * 
 */
public abstract class ComponentBdb {
	protected Database readOnly;
	protected Database readWrite;

	public ComponentBdb(Bdb readOnlyBdbEnv, Bdb readWriteBdbEnv)
			throws IOException {
		readOnly = Bdb.setupDatabase(readOnlyBdbEnv.bdbEnv.getConfig().getReadOnly(), 
				getDbName(), readOnlyBdbEnv);
		readWrite = Bdb.setupDatabase(false, getDbName(), readWriteBdbEnv);
		init();
	}
	
	protected abstract void init() throws IOException;
	protected abstract String getDbName();
	
	public void close() {
		readOnly.close();
		readWrite.sync();
		readWrite.close();
	}

	public void sync() {
		if (readOnly.getConfig().getReadOnly() == false) {
			readOnly.sync();
		}
		readWrite.sync();
	}

	public Database getReadOnly() {
		return readOnly;
	}

	public Database getReadWrite() {
		return readWrite;
	}
}
