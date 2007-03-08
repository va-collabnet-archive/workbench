package org.dwfa.vodb.types;

import com.sleepycat.je.DatabaseEntry;

public interface I_ProcessPaths extends I_ProcessEntries {
	public void processPath(DatabaseEntry key, DatabaseEntry value) throws Exception;
}
