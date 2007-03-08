package org.dwfa.vodb.types;

import com.sleepycat.je.DatabaseEntry;

public interface I_ProcessTimeBranch extends I_ProcessEntries {
	public void processTimeBranch(DatabaseEntry key, DatabaseEntry value) throws Exception;

}
