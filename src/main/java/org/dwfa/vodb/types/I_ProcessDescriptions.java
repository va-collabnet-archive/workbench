package org.dwfa.vodb.types;


import com.sleepycat.je.DatabaseEntry;

public interface I_ProcessDescriptions extends I_ProcessEntries {
	
	public void processDesc(DatabaseEntry key, DatabaseEntry value) throws Exception;
}
