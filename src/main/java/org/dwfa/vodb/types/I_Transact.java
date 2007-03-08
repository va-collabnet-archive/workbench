package org.dwfa.vodb.types;

import java.util.Set;

import com.sleepycat.je.DatabaseException;

public interface I_Transact {

	public void commit(int version, Set<TimePathId> values)
			throws DatabaseException;

	public void abort() throws DatabaseException;

}