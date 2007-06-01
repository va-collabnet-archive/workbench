package org.dwfa.vodb.types;


import com.sleepycat.je.DatabaseEntry;

public interface I_ProcessIdEntries extends I_ProcessEntries {
	public void processId(DatabaseEntry key, DatabaseEntry value) throws Exception;
}
