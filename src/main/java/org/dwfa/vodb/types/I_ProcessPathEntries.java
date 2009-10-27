package org.dwfa.vodb.types;

import com.sleepycat.je.DatabaseEntry;

@Deprecated
public interface I_ProcessPathEntries extends I_ProcessEntries {
	public void processPath(DatabaseEntry key, DatabaseEntry value) throws Exception;
}
