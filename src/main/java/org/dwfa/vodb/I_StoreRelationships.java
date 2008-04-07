package org.dwfa.vodb;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.vodb.types.I_ProcessRelationshipEntries;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;

public interface I_StoreRelationships extends I_StoreInBdb {

	public void writeRel(I_RelVersioned rel) throws IOException, DatabaseException;

	public boolean hasRel(int relId, int conceptId) throws DatabaseException, IOException;

	public I_RelVersioned getRel(int relId, int conceptId) throws DatabaseException, IOException;

	public boolean hasSrcRel(int conceptId, Set<Integer> srcRelTypeIds)
			throws DatabaseException, IOException;

	public boolean hasSrcRels(int conceptId) throws DatabaseException, IOException;

	public boolean hasSrcRelTuple(int conceptId,
			I_IntSet allowedStatus, I_IntSet sourceRelTypes,
			Set<I_Position> positions) throws DatabaseException, IOException;

	public List<I_RelVersioned> getSrcRels(int conceptId)
			throws DatabaseException, IOException;

	public boolean hasDestRel(int conceptId,
			Set<Integer> destRelTypeIds) throws DatabaseException, IOException;

	public boolean hasDestRels(int conceptId) throws DatabaseException;

	public List<I_RelVersioned> getDestRels(int conceptId)
			throws DatabaseException, IOException;

	public boolean hasDestRelTuple(int conceptId,
			I_IntSet allowedStatus, I_IntSet destRelTypes,
			Set<I_Position> positions) throws DatabaseException, IOException;
	
	public void iterateRelationshipEntries(I_ProcessRelationshipEntries processor)
			throws Exception;

	public void cleanupSNOMED(I_IntSet relsToIgnore, I_IntSet releases) throws Exception;
	
	public I_RelVersioned relEntryToObject(DatabaseEntry key, DatabaseEntry value);

	public Iterator<I_RelVersioned> getRelationshipIterator() throws IOException;
}