package org.dwfa.vodb;

import java.util.Set;

import org.dwfa.ace.api.TimePathId;
import org.dwfa.vodb.types.I_ProcessTimeBranchEntries;

import com.sleepycat.je.DatabaseException;

public interface I_StorePositions extends I_StoreInBdb {

	public abstract void addTimeBranchValues(Set<TimePathId> values)
			throws DatabaseException;

	public abstract void writeTimePath(TimePathId jarTimePath)
			throws DatabaseException;

	public abstract void iterateTimeBranch(I_ProcessTimeBranchEntries processor)
			throws Exception;

}