package org.dwfa.vodb.types;


import com.sleepycat.je.DatabaseEntry;


public interface I_ProcessConceptAttributeEntries extends I_ProcessEntries {
	public void processConceptAttributeEntry(DatabaseEntry key, DatabaseEntry value) throws Exception;
}
