package org.dwfa.vodb;

import java.io.IOException;
import java.util.Set;

import org.dwfa.ace.api.TimePathId;
import org.dwfa.vodb.types.ConceptBean;

import com.sleepycat.je.DatabaseException;

public interface I_StoreInBdb {

	public void sync() throws DatabaseException;

	public void close() throws DatabaseException;
	
	public void commit(ConceptBean bean, int version, Set<TimePathId> values) throws DatabaseException, IOException;

	public void setupBean(ConceptBean cb) throws IOException;
	


}
