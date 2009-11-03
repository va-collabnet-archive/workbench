package org.dwfa.vodb;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.collections.primitives.IntList;
import org.apache.lucene.queryParser.ParseException;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.IdentifierSet;
import org.dwfa.ace.search.I_TrackContinuation;
import org.dwfa.ace.task.search.I_TestSearchResults;
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

	public void searchConcepts(I_TrackContinuation tracker,
			IntList matches, CountDownLatch latch,
			List<I_TestSearchResults> checkList, I_ConfigAceFrame config) throws DatabaseException, IOException, ParseException;

	public I_IntSet getConceptNids() throws IOException;

	public IdentifierSet getConceptIdSet()  throws IOException;

	public IdentifierSet getEmptyIdSet() throws IOException;

	public I_RepresentIdSet getIdSetFromIntCollection(Collection<Integer> ids) throws IOException;

	public I_RepresentIdSet getIdSetfromTermCollection(
			Collection<? extends I_AmTermComponent> components)  throws IOException;

	public I_RepresentIdSet getReadOnlyConceptIdSet() throws IOException;

}