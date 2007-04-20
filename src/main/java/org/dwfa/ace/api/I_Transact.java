package org.dwfa.ace.api;

import java.io.IOException;
import java.util.Set;

import org.dwfa.ace.api.TimePathId;

public interface I_Transact {

	public void commit(int version, Set<TimePathId> values)
			throws IOException;

	public void abort() throws IOException;

}