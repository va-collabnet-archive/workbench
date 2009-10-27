package org.dwfa.vodb;

import org.dwfa.ace.api.I_Path;
import org.dwfa.vodb.types.I_ProcessPathEntries;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;

@Deprecated
public interface I_StorePaths extends I_StoreInBdb {

	public void writePath(I_Path p) throws DatabaseException;

	public I_Path getPath(int nativeId) throws DatabaseException;

	public boolean hasPath(int nativeId) throws DatabaseException;

	public void iteratePaths(I_ProcessPathEntries processor) throws Exception;

	public I_Path pathEntryToObject(DatabaseEntry key, DatabaseEntry value);

}