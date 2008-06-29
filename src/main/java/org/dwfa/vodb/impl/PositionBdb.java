package org.dwfa.vodb.impl;

import java.io.IOException;
import java.util.Set;

import org.dwfa.ace.api.TimePathId;
import org.dwfa.vodb.I_StoreInBdb;
import org.dwfa.vodb.I_StorePositions;
import org.dwfa.vodb.bind.BranchTimeBinder;
import org.dwfa.vodb.bind.TimePathIdBinder;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.I_ProcessTimeBranchEntries;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class PositionBdb implements I_StoreInBdb, I_StorePositions {

	private Database positionDb;

	private BranchTimeBinder btBinder = new BranchTimeBinder();

	private TimePathIdBinder tbBinder = new TimePathIdBinder();

	public PositionBdb(Environment env, DatabaseConfig dbConfig)
			throws DatabaseException {
		super();
		positionDb = env.openDatabase(null, "timeBranchDb", dbConfig);

	}

	public void close() throws DatabaseException {
		if (positionDb != null) {
			positionDb.close();
		}
	}

	public void sync() throws DatabaseException {
		if (positionDb != null) {
			if (!positionDb.getConfig().getReadOnly()) {
				positionDb.sync();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.impl.I_StorePositions#addTimeBranchValues(java.util.Set)
	 */
	public void addTimeBranchValues(Set<TimePathId> values)
			throws DatabaseException {
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		for (TimePathId tb : values) {
			btBinder.objectToEntry(tb, key);
			tbBinder.objectToEntry(tb, value);
			positionDb.put(BdbEnv.transaction, key, value);
		}
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.impl.I_StorePositions#writeTimePath(org.dwfa.ace.api.TimePathId)
	 */
	public void writeTimePath(TimePathId jarTimePath) throws DatabaseException {
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		btBinder.objectToEntry(jarTimePath, key);
		tbBinder.objectToEntry(jarTimePath, value);
		positionDb.put(BdbEnv.transaction, key, value);
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.impl.I_StorePositions#iterateTimeBranch(org.dwfa.vodb.types.I_ProcessTimeBranchEntries)
	 */
	public void iterateTimeBranch(I_ProcessTimeBranchEntries processor)
			throws Exception {
		Cursor timeBranchCursor = positionDb.openCursor(null, null);
		DatabaseEntry foundKey = processor.getKeyEntry();
		DatabaseEntry foundData = processor.getDataEntry();
		while (timeBranchCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			try {
				processor.processTimeBranch(foundKey, foundData);
			} catch (Exception e) {
				timeBranchCursor.close();
				throw e;
			}
		}
		timeBranchCursor.close();
	}

	public void commit(ConceptBean bean, int version, Set<TimePathId> values)
			throws DatabaseException {
		// nothing to do...
		
	}

	public void setupBean(ConceptBean cb) throws IOException {
		// nothing to do
	}

}
