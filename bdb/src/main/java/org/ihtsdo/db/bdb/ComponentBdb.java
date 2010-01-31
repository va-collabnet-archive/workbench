package org.ihtsdo.db.bdb;

import java.io.IOException;
import java.util.logging.Level;

import org.dwfa.ace.log.AceLog;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseException;

/**
 * 
 * @author kec
 * 
 */
public abstract class ComponentBdb {
	protected Database readOnly;
	protected Database mutable;

	public ComponentBdb(Bdb readOnlyBdbEnv, Bdb mutableBdbEnv)
			throws IOException {
		try {
			readOnly = Bdb.setupDatabase(readOnlyBdbEnv.bdbEnv.getConfig().getReadOnly(), 
					getDbName(), readOnlyBdbEnv);
		} catch (DatabaseException e) {
			AceLog.getAppLog().warning(e.getLocalizedMessage());
		}
		mutable = Bdb.setupDatabase(false, getDbName(), mutableBdbEnv);
		init();
	}
	
	protected abstract void init() throws IOException;
	protected abstract String getDbName();
	
	public void close() {
		try {
			sync();
			readOnly.close();
			mutable.close();
		} catch (IllegalStateException ex) {
			if (AceLog.getAppLog().isLoggable(Level.FINE)) {
				AceLog.getAppLog().warning(ex.toString());
			}
		} catch (IOException e) {
			if (AceLog.getAppLog().isLoggable(Level.FINE)) {
				AceLog.getAppLog().severe(e.toString());
			}
		}
	}

	public void sync() throws IOException {
		if (readOnly.getConfig().getReadOnly() == false) {
			readOnly.sync();
		}
		mutable.sync();
	}

	public Database getReadOnly() {
		return readOnly;
	}

	public Database getReadWrite() {
		return mutable;
	}
}
