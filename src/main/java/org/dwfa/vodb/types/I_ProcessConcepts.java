package org.dwfa.vodb.types;


import com.sleepycat.je.DatabaseEntry;


public interface I_ProcessConcepts extends I_ProcessEntries {
	public void processConcept(DatabaseEntry key, DatabaseEntry value) throws Exception;
}
