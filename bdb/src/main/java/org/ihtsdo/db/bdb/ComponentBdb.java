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
		readOnly = Bdb.setupDatabase(true, getDbName(), readOnlyBdbEnv);
		readWrite = Bdb.setupDatabase(false, getDbName(), readWriteBdbEnv);
		init();
	}
	
	protected abstract void init() throws IOException;
	protected abstract String getDbName();

	public Database getReadOnly() {
		return readOnly;
	}

	public Database getReadWrite() {
		return readWrite;
	}
}
