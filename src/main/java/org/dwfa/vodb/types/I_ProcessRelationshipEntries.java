package org.dwfa.vodb.types;


import com.sleepycat.je.DatabaseEntry;

public interface I_ProcessRelationshipEntries extends I_ProcessEntries {
	public void processRel(DatabaseEntry key, DatabaseEntry value) throws Exception;

}
