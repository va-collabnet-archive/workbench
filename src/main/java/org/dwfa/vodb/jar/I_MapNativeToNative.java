package org.dwfa.vodb.jar;

import com.sleepycat.je.DatabaseException;

public interface I_MapNativeToNative {

	public void add(int jarId, int dbId) throws DatabaseException;

	public int get(int jarId);

}