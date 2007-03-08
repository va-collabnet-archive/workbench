package org.dwfa.vodb.types;


import com.sleepycat.je.DatabaseEntry;

public interface I_ProcessImages extends I_ProcessEntries {
	public void processImages(DatabaseEntry key, DatabaseEntry value) throws Exception;
}
