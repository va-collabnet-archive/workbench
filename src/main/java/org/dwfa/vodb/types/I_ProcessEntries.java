package org.dwfa.vodb.types;

import com.sleepycat.je.DatabaseEntry;

public interface I_ProcessEntries {
	public DatabaseEntry getKeyEntry();
	public DatabaseEntry getDataEntry();

}
