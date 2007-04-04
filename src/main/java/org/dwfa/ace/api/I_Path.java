package org.dwfa.ace.api;

import java.io.IOException;
import java.util.List;
import java.util.Set;


public interface I_Path {

	public int getConceptId();

	public List<I_Position> getOrigins();

	public I_Path getMatchingPath(int pathId);

	public void abort();

	public void commit(int version, Set<TimePathId> values) throws IOException;

	public void convertIds(I_MapNativeToNative jarToDbNativeMap);

}