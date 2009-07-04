package org.dwfa.vodb;

import java.io.IOException;
import java.util.Iterator;

import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.vodb.types.I_ProcessConceptAttributeEntries;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;

public interface I_StoreConceptAttributes extends I_StoreInBdb {

	public void writeConceptAttributes(
			I_ConceptAttributeVersioned concept) throws DatabaseException, IOException;

	public boolean hasConcept(int conceptId) throws DatabaseException;

	public I_ConceptAttributeVersioned getConceptAttributes(
			int conceptId) throws IOException;

	public Iterator<I_GetConceptData> getConceptIterator()
			throws IOException;

	public void iterateConceptAttributeEntries(
			I_ProcessConceptAttributeEntries processor) throws Exception;

	public I_ConceptAttributeVersioned conAttrEntryToObject(DatabaseEntry key, DatabaseEntry value);

	public int getConceptCount() throws DatabaseException;

}